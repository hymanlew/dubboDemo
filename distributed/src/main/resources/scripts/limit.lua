

// 分布式限流 lua，下标从 1 开始

// 限流 key
local key = KEYS[1]

//限流大小，限流的数量
local limit = tonumber(ARGV[1])

// 获取当前流量大小，然后判断其值是否为nil，如果为nil的话需要赋值为0
local curentLimit = tonumber(redis.call('get', key) or "0")

// 然后进行加 1 并且和 limit 进行比对，如果大于 limt 即返回0，说明限流了
if curentLimit + 1 > limit then return 0;

// 如果小于 limit 则需要使用 Redis的 INCRBY key 1,就是将key进行加 1 命令
else
   redis.call("INCRBY", key, 1)

   // 并且设置超时时间，超时时间是秒，并且如果有需要的话这个秒也是可以用参数进行设置，EXPIRE后边的单位是秒
   redis.call("EXPIRE", key, 10)

   return curentLimit + 1
end
