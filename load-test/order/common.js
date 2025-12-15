import http from 'k6/http';
import { check, sleep } from 'k6';

export const cfg = {
  baseUrl: 'http://klp-logistics-stage-alb-327891870.ap-northeast-2.elb.amazonaws.com',
  hubBaseUrl: 'http://klp-logistics-stage-alb-327891870.ap-northeast-2.elb.amazonaws.com',
  supplierId: '6ab0699f-97a1-4cfb-9e6f-2ef1096ba7f5',
  productId: 'b99f3b2b-6835-493a-a2ca-45b182d14b35',
  hubId: '8d18aac8-92ef-4f30-9c54-27e603da3dfc',
  userId: '1',
  addressId: '7307af26-0853-4e5e-976e-ee1abb244cfb',
  authToken: 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJLTFAtQkUiLCJzdWIiOiIyIiwidG9rZW5UeXBlIjoiYWNjZXNzIiwidXNlcm5hbWUiOiJjdXN0b21lcjAxIiwicm9sZSI6IkNVU1RPTUVSIiwiaWF0IjoxNzY1Nzg3NzI2LCJleHAiOjE3NjU3OTEzMjZ9.83j5vLe2TjxGGABVJfYnHroulR5Z-i1VroUg-owoCAM',
  expectedOrders: 10,
};

const headers = {
  'Content-Type': 'application/json',
};

if (cfg.authToken) {
  headers.Authorization = `Bearer ${cfg.authToken}`;
}

function requireEnv() {
  const missing = [];
  if (!cfg.supplierId) missing.push('SUPPLIER_ID');
  if (!cfg.productId) missing.push('PRODUCT_ID');
  if (!cfg.hubId) missing.push('HUB_ID');
  if (!cfg.addressId) missing.push('ADDRESS_ID');
  if (missing.length) {
    throw new Error(`Missing env vars: ${missing.join(', ')}`);
  }
}

export function buildOrderPayload() {
  requireEnv();
  return {
    userId: Number(cfg.userId),
    supplierId: cfg.supplierId,
    userCouponId: null,
    comment: 'load test',
    addressId: cfg.addressId,
    deliveryLatitude: 37.5665,
    deliveryLongitude: 126.9780,
    orderItems: [
      {
        productId: cfg.productId,
        productName: 'load-test-item',
        hubId: cfg.hubId,
        quantity: 1,
        price: 10000,
      },
    ],
  };
}

export function postOrder() {
  const res = http.post(
    `${cfg.baseUrl}/v1/orders`,
    JSON.stringify(buildOrderPayload()),
    { headers }
  );
  check(res, { 'status 201/200': (r) => r.status === 201 || r.status === 200 });
  sleep(0.05);
  return res;
}

export function getInventoryQty() {
  const res = http.get(
    `${cfg.hubBaseUrl}/v1/inventories/${cfg.productId}`,
    { headers }
  );
  if (!check(res, { 'inv status 200': (r) => r.status === 200 })) {
    return null;
  }
  const body = JSON.parse(res.body);
  return body?.quantity ?? null;
}

export function teardownCheck(initialQty) {
  if (initialQty == null) {
    return;
  }
  const finalQty = getInventoryQty();
  if (finalQty == null) {
    return;
  }

  const expectedOrders = cfg.expectedOrders ?? initialQty;
  const expectedFinal = initialQty - expectedOrders;
  check({ finalQty }, {
    'inventory matches expectation': () => finalQty === expectedFinal,
  });
}
