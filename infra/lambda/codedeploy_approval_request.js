const https = require("https");

const {CodeDeployClient, GetDeploymentCommand} =
    require("@aws-sdk/client-codedeploy");
const {DynamoDBClient} = require("@aws-sdk/client-dynamodb");
const {DynamoDBDocumentClient, PutCommand} = require("@aws-sdk/lib-dynamodb");

const codedeploy = new CodeDeployClient({});
const dynamo = DynamoDBDocumentClient.from(new DynamoDBClient({}));

const SLACK_WEBHOOK_URL = process.env.SLACK_WEBHOOK_URL;
const SERVICE_NAME = process.env.SERVICE_NAME || "";
const CODEDEPLOY_APP = process.env.CODEDEPLOY_APP || "";
const CODEDEPLOY_DEPLOYMENT_GROUP = process.env.CODEDEPLOY_DEPLOYMENT_GROUP
    || "";
const TABLE_NAME = process.env.TABLE_NAME;

exports.handler = async (event) => {
  const deploymentId = event?.deploymentId || event?.DeploymentId;
  const hookId = event?.lifecycleEventHookExecutionId
      || event?.LifecycleEventHookExecutionId;

  if (!deploymentId || !hookId) {
    console.error("Missing deploymentId/hookId", JSON.stringify(event));
    return {statusCode: 400, body: "Invalid CodeDeploy event"};
  }

  const deploymentInfo = await fetchDeploymentInfo(deploymentId);
  const deploymentGroupName =
      deploymentInfo?.deploymentGroupName || CODEDEPLOY_DEPLOYMENT_GROUP;
  const appName = deploymentInfo?.applicationName || CODEDEPLOY_APP;
  const serviceName = deploymentInfo?.applicationName || deploymentGroupName
      || SERVICE_NAME;

  const payload = {
    deploymentId,
    hookId,
    appName,
    deploymentGroup: deploymentGroupName,
    serviceName
  };

  const text =
      `🚀 *${serviceName || deploymentGroupName} 배포 승인 요청*\n` +
      (serviceName ? `도메인: \`${serviceName}\`\n` : "") +
      `DeploymentId: \`${deploymentId}\`\n` +
      `App: \`${appName}\` / Group: \`${deploymentGroupName}\``;

  const blocks = [
    {type: "section", text: {type: "mrkdwn", text}},
    {
      type: "actions",
      elements: [
        {
          type: "button",
          text: {type: "plain_text", text: "✅ 승인"},
          style: "primary",
          value: JSON.stringify({...payload, action: "approve"}),
          action_id: "approve_deploy"
        },
        {
          type: "button",
          text: {type: "plain_text", text: "❌ 거절"},
          style: "danger",
          value: JSON.stringify({...payload, action: "reject"}),
          action_id: "reject_deploy"
        },
      ],
    },
  ];

  if (TABLE_NAME) {
    await dynamo.send(new PutCommand({
      TableName: TABLE_NAME,
      Item: {
        deploymentId,
        hookId,
        appName,
        deploymentGroup: deploymentGroupName,
        serviceName,
        status: "PENDING",
        createdAt: new Date().toISOString(),
      },
    }));
  }

  await postToSlack({text, blocks});
  return {statusCode: 200, body: "Slack approval requested"};
};

async function fetchDeploymentInfo(deploymentId) {
  if (!deploymentId) {
    return null;
  }

  try {
    const res = await codedeploy.send(
        new GetDeploymentCommand({deploymentId}));
    return res?.deploymentInfo || null;
  } catch (err) {
    console.error("Failed to fetch deployment info", err);
    return null;
  }
}

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
      "Content-Length": Buffer.byteLength(data)
    },
  };

  await new Promise((resolve, reject) => {
    const req = https.request(options, (res) => {
      let buf = "";
      res.on("data", (d) => (buf += d));
      res.on("end", () => {
        if (res.statusCode < 200 || res.statusCode >= 300) {
          return reject(
              new Error(`Slack webhook failed: ${res.statusCode} body=${buf}`));
        }
        resolve();
      });
    });
    req.on("error", reject);
    req.write(data);
    req.end();
  });
}
