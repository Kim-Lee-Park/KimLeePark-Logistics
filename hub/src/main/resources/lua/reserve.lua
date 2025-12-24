-- KEYS[1]: inventory:hot:{productId}:{hubId}
-- ARGV[1]: 요청 수량
-- 반환값: {상태코드, 남은재고}
--   상태코드: 0=성공, -1=재고부족, -2=키없음

local key = KEYS[1]
local requestedQty = tonumber(ARGV[1])

-- 키 존재 확인
local currentStock = redis.call('GET', key)
if not currentStock then
    return { -2, 0 }
end

currentStock = tonumber(currentStock)

-- 재고 부족 확인
if currentStock < requestedQty then
    return { -1, currentStock }
end

-- 원자적 차감 (TTL 유지)
local newStock = currentStock - requestedQty
redis.call('SET', key, newStock, 'KEEPTTL')

return { 0, newStock }
