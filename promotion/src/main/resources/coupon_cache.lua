-- 키 존재 여부 확인
local exists = redis.call("EXISTS", KEYS[1])
if exists == 0 then
  return "-1"
end

-- 재고 차감 (원자적 연산)
local remain = redis.call("DECR", KEYS[1])

-- 차감 후 재고가 음수가 되면 원상복구하고 실패 반환
if remain < 0 then
  redis.call("INCR", KEYS[1])
  return "-1"
end

-- 성공 시 남은 재고 반환
return tostring(remain)