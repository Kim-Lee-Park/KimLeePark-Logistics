import {postOrder} from './common.js';

export const options = {
  scenarios: {
    order_rps: {
      executor: 'ramping-arrival-rate',
      startRate: 10,
      timeUnit: '1s',

      // 목표 RPS 미달의 1순위 원인(VU 부족) 제거
      preAllocatedVUs: 500,
      maxVUs: 2000,

      stages: [
        {target: 30, duration: '45s'},
        {target: 100, duration: '90s'},
        {target: 0, duration: '30s'},
      ],
    },
  },
  thresholds: {
    // ✅ 목표 RPS 달성 실패면 바로 알 수 있게
    dropped_iterations: ['count==0'],

    // 기존 유지
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<800'],
  },
};

export default function () {
  postOrder();
}
