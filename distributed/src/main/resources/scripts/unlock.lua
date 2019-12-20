
# 分布式锁-解锁-lua

redis.call('del', 'result')
if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1])
else
return 0
end
return "";
