-- 获取商品库存信息
local counts = redis.call("HMGET", KEYS[1], "total", "ordered");
-- 请求的库存量
local k = tonumber(ARGV[1])
-- 将总库存转换为数值
local total = tonumber(counts[1])
-- 将已被秒杀的库存转换为数值
local ordered = tonumber(counts[2])
-- 如果当前请求的库存量加上已被秒杀的库存量仍然小于总库存量，就可以更新库存
if ordered + k <= total then
   -- 更新已秒杀的库存量
    redis.call("HINCRBY",KEYS[1],"ordered",k)
    return k;
end
return 0