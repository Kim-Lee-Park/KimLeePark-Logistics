import http from 'k6/http';
import {check, sleep} from 'k6';

export const cfg = {
  baseUrl: 'http://klp-logistics-stage-alb-1488967859.ap-northeast-2.elb.amazonaws.com',
  hubBaseUrl: 'http://klp-logistics-stage-alb-1488967859.ap-northeast-2.elb.amazonaws.com',
  supplierId: '37c2d3c6-43fe-4935-b1bb-69326daa6f09',
  productId: 'd2950c5b-5b9f-474d-b7a7-0271748fa31e',
  hubId: 'd2649be4-02a1-4a07-93b2-8e18a3b11631',
  userId: '1',
  addressId: 'f4255fff-88f4-410e-8df4-dc92cc3b8b94',
  authToken: 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJLTFAtQkUiLCJzdWIiOiIxIiwidG9rZW5UeXBlIjoiYWNjZXNzIiwidXNlcm5hbWUiOiJjdXN0b21lcjAxIiwicm9sZSI6IkNVU1RPTUVSIiwiaWF0IjoxNzY1OTQwNzc0LCJleHAiOjE3NjU5NDQzNzR9.MlBENz7DvuMsY-MFHZlCkqDRsFGG2AvAhPc-zQTj4G8',
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
  if (!cfg.supplierId) {
    missing.push('SUPPLIER_ID');
  }
  if (!cfg.productId) {
    missing.push('PRODUCT_ID');
  }
  if (!cfg.hubId) {
    missing.push('HUB_ID');
  }
  if (!cfg.addressId) {
    missing.push('ADDRESS_ID');
  }
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
      {headers}
  );
  check(res, {'status 201/200': (r) => r.status === 201 || r.status === 200});
  sleep(0.05);
  return res;
}

