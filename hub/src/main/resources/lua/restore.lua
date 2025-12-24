-- KEYS[1]: inventory:hot:{productId}:{hubId}
-- ARGV[1]: 복구 수량
-- 반환값: {상태코드, 복구후재고}

local key = KEYS[1]
local restoreQty = tonumber(ARGV[1])

local currentStock = redis.call('GET', key)
if not currentStock then
    return { -2, 0 }
end

currentStock = tonumber(currentStock)

-- 원자적 증가 (TTL 유지)
local newStock = currentStock + restoreQty
redis.call('SET', key, newStock, 'KEEPTTL')

return { 0, newStock }
