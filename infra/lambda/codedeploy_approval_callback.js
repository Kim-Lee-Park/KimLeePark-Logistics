const qs = require("querystring");

const {
  CodeDeployClient,
  PutLifecycleEventHookExecutionStatusCommand
} = require("@aws-sdk/client-codedeploy");
const {DynamoDBClient} = require("@aws-sdk/client-dynamodb");
const {DynamoDBDocumentClient, GetCommand} = require("@aws-sdk/lib-dynamodb");

const codedeploy = new CodeDeployClient({});
const dynamo = DynamoDBDocumentClient.from(new DynamoDBClient({}));

const TABLE_NAME = process.env.TABLE_NAME;

exports.handler = async (event) => {
  console.log("RAW EVENT:", JSON.stringify(event));
  console.log("IS_BASE64:", event?.isBase64Encoded);
  console.log("RAW BODY:", event?.body);
  console.log("HEADERS:", JSON.stringify(event?.headers));

  try {
    const payload = parseSlackPayload(event);

    console.log("PARSED PAYLOAD:", JSON.stringify(payload));

    const raw = payload?.actions?.[0]?.value;
    const action = raw ? JSON.parse(raw) : null;

    if (!action?.deploymentId) {
      return response(400,
          `Invalid Slack payload. payload=${JSON.stringify(payload)}`);
    }

    let hookId = action.hookId;

    if (!hookId && TABLE_NAME) {
      const out = await dynamo.send(new GetCommand({
        TableName: TABLE_NAME,
        Key: {deploymentId: action.deploymentId},
      }));
      hookId = out?.Item?.hookId;
    }

    if (!hookId) {
      return response(400,
          `hookId not found. action=${JSON.stringify(action)}`);
    }

    const status = action.action === "approve" ? "Succeeded" : "Failed";

    await codedeploy.send(new PutLifecycleEventHookExecutionStatusCommand({
      deploymentId: action.deploymentId,
      lifecycleEventHookExecutionId: hookId,
      status,
    }));

    return response(200, {
      replace_original: true,
      text: `${status === "Succeeded" ? "✅ 승인" : "❌ 거절"} 처리했습니다.`,
    });
  } catch (err) {
    console.error("ERROR:", err);
    return response(500, `Error handling approval: ${err?.message || err}`);
  }
};

function parseSlackPayload(event) {
  let body = event.body || "";

  if (event.isBase64Encoded) {
    body = Buffer.from(body, "base64").toString("utf-8");
  }

  console.log("DECODED BODY:", body);

  const parsed = qs.parse(body);
  console.log("FORM PARSED:", JSON.stringify(parsed));

  return parsed.payload ? JSON.parse(parsed.payload) : null;
}

function response(statusCode, body) {
  const slackBody =
      typeof body === "string"
          ? {response_type: "ephemeral", text: body}
          : body;

  return {
    statusCode,
    headers: {"Content-Type": "application/json"},
    body: JSON.stringify(slackBody),
  };
}
