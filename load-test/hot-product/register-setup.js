/**
 * Hot Product 등록 (테스트 전 실행)
 * 
 * 실행:
 * k6 run load-test/hot-product/register-setup.js
 * 
 * 환경변수:
 * - HUB_BASE_URL: Hub 서비스 URL (기본: http://localhost:8030)
 * - PRODUCT_ID: 테스트 상품 ID (기본: bbbbbbbb-0000-0000-0000-000000000001)
 * - HUB_ID: 테스트 허브 ID (기본: aaaaaaaa-0000-0000-0002-000000000001)
 * - TTL_SECONDS: 캐시 TTL (기본: 3600)
 */
import {registerHotProduct, getHotProductQuantity} from './common.js';

export const options = {
  scenarios: {
    setup: {
      executor: 'shared-iterations',
      iterations: 1,
      vus: 1,
    },
  },
};

export default function () {
  console.log('Hot Product 등록 중...');
  
  const registerRes = registerHotProduct();
  
  if (registerRes.status === 200) {
    console.log('Hot Product 등록 성공');
    
    const quantityRes = getHotProductQuantity();
    console.log(`현재 재고: ${quantityRes.body}`);
  } else {
    console.log(`Hot Product 등록 실패: ${registerRes.status} - ${registerRes.body}`);
  }
}
