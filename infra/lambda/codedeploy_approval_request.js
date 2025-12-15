const https = require("https");
const AWS = require("aws-sdk");
const dynamodb = new AWS.DynamoDB.DocumentClient();

const SLACK_WEBHOOK_URL = process.env.SLACK_WEBHOOK_URL;
const SERVICE_NAME = process.env.SERVICE_NAME || "";
const CODEDEPLOY_APP = process.env.CODEDEPLOY_APP || "";
const CODEDEPLOY_DEPLOYMENT_GROUP = process.env.CODEDEPLOY_DEPLOYMENT_GROUP || "";
const TABLE_NAME = process.env.TABLE_NAME;

exports.handler = async (event) => {
  const deploymentId = event?.deploymentId || event?.DeploymentId;
  const hookId = event?.lifecycleEventHookExecutionId || event?.LifecycleEventHookExecutionId;

  if (!deploymentId || !hookId) {
    console.error("Missing DeploymentId or LifecycleEventHookExecutionId", event);
    return { statusCode: 400, body: "Invalid CodeDeploy event" };
  }

  const payload = {
    deploymentId,
    hookId,
    appName: CODEDEPLOY_APP,
    deploymentGroup: CODEDEPLOY_DEPLOYMENT_GROUP,
    serviceName: SERVICE_NAME,
    action: "pending",
  };

  const text = `🚀 *${SERVICE_NAME || CODEDEPLOY_DEPLOYMENT_GROUP} 배포 승인 요청*\n` +
    `DeploymentId: \`${deploymentId}\`\n` +
    `App: \`${CODEDEPLOY_APP}\` / Group: \`${CODEDEPLOY_DEPLOYMENT_GROUP}\``;

  const blocks = [
    {
      type: "section",
      text: { type: "mrkdwn", text },
    },
    {
      type: "actions",
      elements: [
        {
          type: "button",
          text: { type: "plain_text", text: "✅ 승인" },
          style: "primary",
          value: JSON.stringify({ ...payload, action: "approve" }),
          action_id: "approve_deploy",
        },
        {
          type: "button",
          text: { type: "plain_text", text: "❌ 거절" },
          style: "danger",
          value: JSON.stringify({ ...payload, action: "reject" }),
          action_id: "reject_deploy",
        },
      ],
    },
  ];

  if (TABLE_NAME) {
    await dynamodb.put({
      TableName: TABLE_NAME,
      Item: {
        deploymentId,
        hookId,
        appName: CODEDEPLOY_APP,
        deploymentGroup: CODEDEPLOY_DEPLOYMENT_GROUP,
        serviceName: SERVICE_NAME,
        status: "PENDING",
        createdAt: new Date().toISOString(),
      },
    }).promise();
  }

  await postToSlack({ text, blocks });

  // CodeDeploy waits for the lifecycle hook result from the callback Lambda.
  return { statusCode: 200, body: "Slack approval requested" };
};

async function postToSlack(body) {
  if (!SLACK_WEBHOOK_URL) {
    throw new Error("SLACK_WEBHOOK_URL is not set");
  }
  const data = JSON.stringify(body);
  const url = new URL(SLACK_WEBHOOK_URL);
  const options = {
    method: "POST",
    hostname: url.hostname,
    path: url.pathname + url.search,
    headers: {
      "Content-Type": "application/json",
      "Content-Length": Buffer.byteLength(data),
    },
  };
  await new Promise((resolve, reject) => {
    const req = https.request(options, (res) => {
      res.on("data", () => {});
      res.on("end", () => {
        if (res.statusCode < 200 || res.statusCode >= 300) {
          return reject(new Error(`Slack webhook failed: ${res.statusCode}`));
        }
        return resolve();
      });
    });
    req.on("error", reject);
    req.write(data);
    req.end();
  });
}
