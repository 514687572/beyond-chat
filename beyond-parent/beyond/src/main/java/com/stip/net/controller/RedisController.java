package com.stip.net.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.stip.net.service.RedisService;
import com.stip.net.service.impl.GoodsService;

@Scope("request")
@RequestMapping("/test")
@RestController
public class RedisController {
	@Resource
	private RedisService redisService;
	@Resource
	private GoodsService goodsService;
	
	@RequestMapping(value = "/updateVistiTime.do", method = { RequestMethod.PUT})
	public Map<String,Object> updateVistiTime(HttpServletRequest request, HttpServletResponse response){
		Map<String,Object> jsonResult=new HashMap<String, Object>();
		
		redisService.putCache("newTime", new Date());
		jsonResult.put("updateTime",redisService.getCache("newTime", Date.class));
		
		return jsonResult;
	}
	
	@RequestMapping(value = "/test.do", method = { RequestMethod.PUT,RequestMethod.GET})
	public Map<String,Object> test(HttpServletRequest request, HttpServletResponse response) throws Exception{
		String luaScript="local storage = redis.call('get',KEYS[1]) " + 
				"if  storage ~= false then " + 
				"	if tonumber(storage) > 0 then " + 
				"		return redis.call('decr',KEYS[1]) " + 
				"	else " + 
				"		return -1 " + 
				"	end " + 
				"else " + 
				"	return redis.call('set',KEYS[1],101) " + 
				"end";
		Map<String,Object> jsonResult=new HashMap<String, Object>();
		String[] args= new String[] {"storage_seckill"};
		List<String> list=new ArrayList<String>();
		list.add("storage_seckill");
		Long a=redisService.executeScript(luaScript,list,args);
		System.out.println(a);
		if(a>=0) {
			goodsService.updateGoodsCount(redisService.getCache("storage_seckill", Integer.class), 1000);
			jsonResult.put("result","购买成功，还剩"+ a);
		}else {
			jsonResult.put("result","秒杀完了");
		}
		
		return jsonResult;
	}
}
