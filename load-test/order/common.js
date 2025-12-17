import http from 'k6/http';
import {check, sleep} from 'k6';

export const cfg = {
  baseUrl: 'http://klp-logistics-stage-alb-304086239.ap-northeast-2.elb.amazonaws.com',
  hubBaseUrl: 'http://klp-logistics-stage-alb-304086239.ap-northeast-2.elb.amazonaws.com',
  supplierId: 'ea4b7edd-bad8-4406-b719-c1b7ebb60210',
  productId: '54af4539-0e82-4d22-9ba3-231e50981091',
  hubId: '57a2944f-7301-4b63-a3cc-96f2868058ba',
  userId: '1',
  addressId: '40a7fb15-8e6f-4c5e-9c7d-1149203c0532',
  authToken: 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJLTFAtQkUiLCJzdWIiOiIxIiwidG9rZW5UeXBlIjoiYWNjZXNzIiwidXNlcm5hbWUiOiJjdXN0b21lcjAxIiwicm9sZSI6IkNVU1RPTUVSIiwiaWF0IjoxNzY1OTU5Mjk0LCJleHAiOjE3NjU5NjI4OTR9.pqN4YyQgvMLwdjLFbRr3w0F57K5EVH2DGm36K_VJ3GA',
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
      `${cfg.baseUrl}/v1/orders/v2`,
      JSON.stringify(buildOrderPayload()),
      {headers}
  );
  check(res, {'status 201/200': (r) => r.status === 201 || r.status === 200});
  sleep(0.05);
  return res;
}
