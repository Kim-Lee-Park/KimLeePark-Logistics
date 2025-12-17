import http from 'k6/http';
import {check, sleep} from 'k6';

export const cfg = {
  baseUrl: 'http://klp-logistics-stage-alb-304086239.ap-northeast-2.elb.amazonaws.com',
  hubBaseUrl: 'http://klp-logistics-stage-alb-304086239.ap-northeast-2.elb.amazonaws.com',
  supplierId: '8d4432e4-3c63-4620-901c-e2bc1775d2e2',
  productId: '7e503383-6519-4c0e-85eb-0ab90e878c41',
  hubId: '2c59b68d-f870-4245-9404-9dd84951a76f',
  userId: '1',
  addressId: '1a10a164-6b74-42f0-bf17-01c15b717982',
  authToken: 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJLTFAtQkUiLCJzdWIiOiIxIiwidG9rZW5UeXBlIjoiYWNjZXNzIiwidXNlcm5hbWUiOiJjdXN0b21lcjAxIiwicm9sZSI6IkNVU1RPTUVSIiwiaWF0IjoxNzY1OTQ5MjIxLCJleHAiOjE3NjU5NTI4MjF9.XFWOXRw56A3uuZtohabCJSc-75avPgap54vIcb762ak',
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

