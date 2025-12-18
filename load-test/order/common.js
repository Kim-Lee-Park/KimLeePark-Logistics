import http from 'k6/http';
import {check, sleep} from 'k6';

export const cfg = {
  baseUrl: '',
  hubBaseUrl: '',
  supplierId: '',
  productId: '',
  hubId: '',
  userId: '',
  addressId: '',
  authToken: '',
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
