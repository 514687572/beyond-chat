local storage = redis.call('get','storage_seckill')
if  storage ~= false then
	if tonumber(storage) > 0 then
		return redis.call('decr','storage_seckill')
	else
		return "storage is zero now, can't perform decr action"
	end
else
	return redis.call('set','storage_seckill',10)
end
