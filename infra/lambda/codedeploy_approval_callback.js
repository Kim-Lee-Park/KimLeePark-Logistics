const qs = require("querystring");
const AWS = require("aws-sdk");
const codedeploy = new AWS.CodeDeploy();
const dynamodb = new AWS.DynamoDB.DocumentClient();

const TABLE_NAME = process.env.TABLE_NAME;

exports.handler = async (event) => {
  try {
    const payload = parseSlackPayload(event);
    const action = payload?.actions?.[0]?.value ? JSON.parse(payload.actions[0].value) : null;
    if (!action || !action.deploymentId) {
      return response(400, "Invalid Slack payload");
    }

    let hookId = action.hookId;
    if (!hookId && TABLE_NAME) {
      const item = await dynamodb.get({
        TableName: TABLE_NAME,
        Key: { deploymentId: action.deploymentId }
      }).promise();
      hookId = item?.Item?.hookId;
    }

    if (!hookId) {
      return response(400, "hookId not found");
    }

    const status = action.action === "approve" ? "Succeeded" : "Failed";
    await codedeploy.putLifecycleEventHookExecutionStatus({
      deploymentId: action.deploymentId,
      lifecycleEventHookExecutionId: hookId,
      status,
    }).promise();

    // Respond quickly to Slack
    return response(200, { text: `배포 ${status === "Succeeded" ? "승인" : "거절"} 처리했습니다.` });
  } catch (err) {
    console.error(err);
    return response(500, "Error handling approval");
  }
};

function parseSlackPayload(event) {
  // Slack sends body as application/x-www-form-urlencoded with payload=
  const body = event.body || "";
  const parsed = qs.parse(body);
  if (parsed.payload) {
    return JSON.parse(parsed.payload);
  }
  return null;
}

function response(statusCode, body) {
  return {
    statusCode,
    headers: { "Content-Type": "application/json" },
    body: typeof body === "string" ? JSON.stringify({ message: body }) : JSON.stringify(body),
  };
}
