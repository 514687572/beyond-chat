package com.stip.net.service;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Service;

@Service
public class RedisService<T> {
	@Autowired
	private RedisTemplate<Serializable, T> redisTemplate;
	
	/**
	 * 批量删除相应的value
	 * 
	 * @param keys
	 */
	public void remove(final String... keys) {
		for (String key : keys) {
			remove(key);
		}
	}

	/**
	 * 批量删除key
	 * 
	 * @param pattern
	 */
	public void removePattern(final String pattern) {
		Set<Serializable> keys = redisTemplate.keys(pattern);
		if (keys.size() > 0)
			redisTemplate.delete(keys);
	}

	/**
	 * 删除相应的value
	 * 
	 * @param key
	 */
	public void remove(final String key) {
		if (exists(key)) {
			redisTemplate.delete(key);
		}
	}

	/**
	 * 推断缓存中是否有相应的value
	 * 
	 * @param key
	 * @return
	 */
	public boolean exists(final String key) {
		return redisTemplate.hasKey(key);
	}

	/**
	 * 读取缓存
	 * 
	 * @param key
	 * @return
	 */
	public T getCache(final String key) {
		T result = null;
		ValueOperations<Serializable, T> operations = redisTemplate.opsForValue();
		result = operations.get(key);
		
		return result;
	}
	
	/**
	 * 写入缓存
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean setCache(final String key, T value) {
		boolean result = false;
		try {
			ValueOperations<Serializable, T> operations = redisTemplate.opsForValue();
			operations.set(key, value);
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 写入缓存
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean setCacheExpir(final String key, T value, Long expireTime) {
		boolean result = false;
		try {
			ValueOperations<Serializable, T> operations = redisTemplate.opsForValue();
			operations.set(key, value);
			redisTemplate.expire(key, expireTime, TimeUnit.SECONDS);
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * 执行lua脚本
	 * 
	 * @return
	 */
	public Long executeScript(String script,List<Serializable> list,int args) {
		RedisScript<Long> redisScript = new DefaultRedisScript<Long>(script, Long.class);
		Long a = redisTemplate.execute(redisScript, list,args);

		return a;
	}
	
	/**
	 * 递增
	 * 
	 * @param key键
	 * @param by要增加几(大于0)
	 * @return
	 */
	public long incr(String key, long delta) {
		if (delta < 0) {
			throw new RuntimeException("递增因子必须大于0");
		}
		
		return redisTemplate.opsForValue().increment(key, delta);
	}

	/**
	 * 递减
	 * 
	 * @param key键
	 * @param by要减少几(小于0)
	 * @return
	 */
	public long decr(String key, long delta) {
		if (delta < 0) {
			throw new RuntimeException("递减因子必须大于0");
		}
		
		redisTemplate.opsForValue().increment(key,-delta);
		
		return 1;
	}
	
	/**
	 * 如果key值不存在则设置值为value并返回true
	 * 已存在和其他异常情况返回NULL
	 * @param key
	 * @param value
	 * @param expireTime
	 * @return
	 */
    public Boolean setnx(String key, T value,Long expireTime) {  
        try {  
        	ValueOperations<Serializable, T> operations = redisTemplate.opsForValue();
        	boolean result=operations.setIfAbsent(key, value); 
        	
        	if(result) {
        		redisTemplate.expire(key, expireTime, TimeUnit.MILLISECONDS);
        	}
        	
            return operations.setIfAbsent(key, value);  
        } catch (Exception e) {  
            return null;  
        }  
    } 
    
    /**
     * 根据批量key返回批量值
     * @param keys
     * @return
     */
	public List<T> getKeys(final List<String> keys) {
		List<T> valueList = (List<T>) redisTemplate.executePipelined(new RedisCallback<T>() {
			public T doInRedis(RedisConnection conn) {
				ValueOperations<Serializable, T> operations = redisTemplate.opsForValue();
				for (String key : keys) {
					return operations.get(key);
				}
				
				return null;
			}
		});

		return valueList;
	}
	
}
