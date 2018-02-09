package com.stip.net.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

import com.stip.net.utils.SerializationUtil;

@Service
public class RedisService {
	public final static String CAHCENAME = "niitcache";// 缓存名
	public final static int CAHCETIME = 60;// 默认缓存时间 60S
	public final static int CAHCEHOUR = 60 * 60;// 默认缓存时间 1hr
	public final static int CAHCEDAY = 60 * 60 * 24;// 默认缓存时间 1Day
	public final static int CAHCEWEEK = 60 * 60 * 24 * 7;// 默认缓存时间 1week
	public final static int CAHCEMONTH = 60 * 60 * 24 * 7 * 30;// 默认缓存时间 1month

	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	public <T> boolean putCache(String key, T obj) {
		final byte[] bkey = key.getBytes();
		final byte[] bvalue = SerializationUtil.serialize(obj);
		boolean result = redisTemplate.execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection connection)throws DataAccessException {
				connection.set(bkey, bvalue);
				return true;
			}
		});
		return result;
	}

	public <T> void putCacheWithExpireTime(String key, T obj,final long expireTime) {
		final byte[] bkey = key.getBytes();
		final byte[] bvalue = SerializationUtil.serialize(obj);
		redisTemplate.execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
				connection.setEx(bkey, expireTime, bvalue);
				return true;
			}
		});
	}

	public <T> boolean putListCache(String key, List<T> objList) {
		final byte[] bkey = key.getBytes();
		final byte[] bvalue = SerializationUtil.serialize(objList);
		boolean result = redisTemplate.execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection connection)throws DataAccessException {
				return connection.setNX(bkey, bvalue);
			}
		});
		return result;
	}

	/**
	 * 存入缓存并指定过期时间
	 * @param key
	 * @param objList
	 * @param expireTime
	 * @return
	 */
	public <T> boolean putListCacheWithExpireTime(String key, List<T> objList, final long expireTime) {
		final byte[] bkey = key.getBytes();
		final byte[] bvalue = SerializationUtil.serialize(objList);
		boolean result = redisTemplate.execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection connection)throws DataAccessException {
				connection.setEx(bkey, expireTime, bvalue);
				return true;
			}
		});
		return result;
	}

	public <T> T getCache(final String key, Class<T> cls) {
		byte[] result = redisTemplate.execute(new RedisCallback<byte[]>() {
			@Override
			public byte[] doInRedis(RedisConnection connection) throws DataAccessException {
				return connection.get(key.getBytes());
			}
		});
		System.out.println(result);
		if (result == null) {
			return null;
		}
		return SerializationUtil.deserialize(result,cls);
	}

	public <T> T getListCache(final String key, Class<T> targetClass) {
		byte[] result = redisTemplate.execute(new RedisCallback<byte[]>() {
			@Override
			public byte[] doInRedis(RedisConnection connection)
					throws DataAccessException {
				return connection.get(key.getBytes());
			}
		});
		if (result == null) {
			return null;
		}
		return SerializationUtil.deserialize(result, targetClass);
	}
	
	/**
	 * 执行lua脚本
	 * 
	 * @return
	 */
	public Long executeScript(String script,List<String> list,String[] args) {
		RedisScript<Long> redisScript = new DefaultRedisScript<Long>(script, Long.class);
		Long a = redisTemplate.execute(redisScript, list,args);

		return a;
	}

	/**
	 * 精确删除key
	 * 
	 * @param key
	 */
	public void deleteCache(String key) {
		redisTemplate.delete(key);
	}

	/**
	 * 模糊删除key
	 * 
	 * @param pattern
	 */
	public void deleteCacheWithPattern(String pattern) {
		Set<String> keys = redisTemplate.keys(pattern);
		redisTemplate.delete(keys);
	}

	/**
	 * 清空所有缓存
	 */
	public void clearCache() {
		deleteCacheWithPattern(RedisService.CAHCENAME + "|*");
	}
}
