import http from 'k6/http';
import {check, sleep} from 'k6';
import {uuidv4} from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';

export const cfg = {
  hubBaseUrl: __ENV.HUB_BASE_URL || 'http://localhost:8030',
  productId: __ENV.PRODUCT_ID || 'bbbbbbbb-0000-0000-0000-000000000001',
  hubId: __ENV.HUB_ID || 'aaaaaaaa-0000-0000-0002-000000000001',
  ttlSeconds: __ENV.TTL_SECONDS || '3600',
  reserveQuantity: __ENV.RESERVE_QUANTITY || '1',
};

const headers = {
  'Content-Type': 'application/json',
};

// Hot Product 등록
export function registerHotProduct() {
  const payload = {
    productId: cfg.productId,
    hubId: cfg.hubId,
    ttlSeconds: Number(cfg.ttlSeconds),
  };

  const res = http.post(
      `${cfg.hubBaseUrl}/v1/inventories/hot`,
      JSON.stringify(payload),
      {headers}
  );
  check(res, {'register status 200': (r) => r.status === 200});
  return res;
}

// Hot Product 재고 조회
export function getHotProductQuantity() {
  const res = http.get(
      `${cfg.hubBaseUrl}/v1/inventories/hot/quantity?productId=${cfg.productId}&hubId=${cfg.hubId}`,
      {headers}
  );
  check(res, {'get quantity status 200': (r) => r.status === 200});
  return res;
}

// 재고 선점 (Hot Product)
export function reserveInventory() {
  const orderId = uuidv4();
  const idempotencyKey = `load-test-${orderId}`;

  const payload = {
    orderId: orderId,
    idempotencyKey: idempotencyKey,
    items: [
      {
        productId: cfg.productId,
        hubId: cfg.hubId,
        quantity: Number(cfg.reserveQuantity),
      },
    ],
  };

  const res = http.post(
      `${cfg.hubBaseUrl}/internal/v1/inventories/reserve`,
      JSON.stringify(payload),
      {headers}
  );

  const success = check(res, {
    'reserve status 200': (r) => r.status === 200,
    'reserve not failed': (r) => r.status !== 400,
  });

  sleep(0.01);
  return {res, orderId, success};
}

// 선점 확정
export function confirmReservation(orderId) {
  const res = http.post(
      `${cfg.hubBaseUrl}/internal/v1/inventories/confirm/${orderId}`,
      null,
      {headers}
  );
  check(res, {'confirm status 200': (r) => r.status === 200});
  return res;
}

// 선점 해제
export function releaseReservation(orderId) {
  const res = http.post(
      `${cfg.hubBaseUrl}/internal/v1/inventories/release/${orderId}`,
      null,
      {headers}
  );
  check(res, {'release status 200': (r) => r.status === 200});
  return res;
}

// Hot Product 해제
export function unregisterHotProduct() {
  const payload = {
    productId: cfg.productId,
    hubId: cfg.hubId,
  };

  const res = http.del(
      `${cfg.hubBaseUrl}/v1/inventories/hot`,
      JSON.stringify(payload),
      {headers}
  );
  check(res, {'unregister status 200': (r) => r.status === 200});
  return res;
}
