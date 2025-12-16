const https = require("https");
const qs = require("querystring");

const {
  CodeDeployClient,
  PutLifecycleEventHookExecutionStatusCommand,
} = require("@aws-sdk/client-codedeploy");
const {DynamoDBClient} = require("@aws-sdk/client-dynamodb");
const {DynamoDBDocumentClient, GetCommand} = require("@aws-sdk/lib-dynamodb");

const codedeploy = new CodeDeployClient({});
const dynamo = DynamoDBDocumentClient.from(new DynamoDBClient({}));

const TABLE_NAME = process.env.TABLE_NAME;

exports.handler = async (event) => {
  let payload;
  try {
    payload = parseSlackPayload(event);

    const responseUrl = payload?.response_url;
    const raw = payload?.actions?.[0]?.value;
    const action = raw ? JSON.parse(raw) : null;

    if (!action?.deploymentId) {
      await safeUpdateViaResponseUrl(responseUrl, {
        replace_original: false,
        response_type: "ephemeral",
        text: "Invalid Slack payload (deploymentId 없음)",
      });
      return ok();
    }

    let hookId = action.hookId;

    if (!hookId && TABLE_NAME) {
      const out = await dynamo.send(
          new GetCommand({
            TableName: TABLE_NAME,
            Key: {deploymentId: action.deploymentId},
          })
      );
      hookId = out?.Item?.hookId;
    }

    if (!hookId) {
      await safeUpdateViaResponseUrl(responseUrl, {
        replace_original: false,
        response_type: "ephemeral",
        text: "hookId not found",
      });
      return ok();
    }

    const status = action.action === "approve" ? "Succeeded" : "Failed";

    await codedeploy.send(
        new PutLifecycleEventHookExecutionStatusCommand({
          deploymentId: action.deploymentId,
          lifecycleEventHookExecutionId: hookId,
          status,
        })
    );

    await safeUpdateViaResponseUrl(responseUrl, {
      replace_original: true,
      text:
          status === "Succeeded"
              ? "✅ 승인 처리했습니다."
              : "❌ 거절 처리했습니다.",
    });

    return ok();
  } catch (err) {
    console.error("ERROR:", err);

    const responseUrl = payload?.response_url; // parse 성공했으면 있음
    await safeUpdateViaResponseUrl(responseUrl, {
      replace_original: false,
      response_type: "ephemeral",
      text: `처리 중 오류가 발생했습니다: ${err?.message || err}`,
    });

    return ok();
  }
};

function parseSlackPayload(event) {
  let body = event.body || "";
  if (event.isBase64Encoded) {
    body = Buffer.from(body, "base64").toString("utf-8");
  }
  const parsed = qs.parse(body);
  return parsed.payload ? JSON.parse(parsed.payload) : null;
}

function ok() {
  return {
    statusCode: 200,
    headers: {"Content-Type": "text/plain"},
    body: "OK",
  };
}

async function safeUpdateViaResponseUrl(responseUrl, message) {
  if (!responseUrl) {
    return;
  }

  await postJson(responseUrl, message);
}

function postJson(urlString, bodyObj) {
  const data = JSON.stringify(bodyObj);
  const url = new URL(urlString);

  const options = {
    method: "POST",
    hostname: url.hostname,
    path: url.pathname + url.search,
    headers: {
      "Content-Type": "application/json",
      "Content-Length": Buffer.byteLength(data),
    },
  };

  return new Promise((resolve, reject) => {
    const req = https.request(options, (res) => {
      res.on("data", () => {
      });
      res.on("end", resolve);
    });
    req.on("error", reject);
    req.write(data);
    req.end();
  });
}
