package com.stip.net.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.stip.net.entity.SalesRecords;
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
	@Resource
	private ThreadPoolTaskExecutor threadPoolTaskExecutor;
	
	private static int threadCount = 1;
	private volatile long a=100;
	
	@RequestMapping(value = "/updateVistiTime.do", method = { RequestMethod.PUT,RequestMethod.GET})
	public Map<String,Object> updateVistiTime(HttpServletRequest request, HttpServletResponse response){
		Map<String,Object> jsonResult=new HashMap<String, Object>();
		
		redisService.setCache("newTime", new Date());
		jsonResult.put("updateTime",redisService.getCache("newTime"));
		
		return jsonResult;
	}
	
	@RequestMapping(value = "/test.do", method = { RequestMethod.PUT,RequestMethod.GET})
	public void seckill(HttpServletRequest request) throws InterruptedException {  
		Map<String,Object> jsonResult=new HashMap<String, Object>();
			//StopWatch watch = new StopWatch();
	        final CountDownLatch latch = new CountDownLatch(threadCount);  
	        String id=request.getParameter("id");
	        final String count=request.getParameter("count");
        	//watch.start();  
	        
        	threadPoolTaskExecutor.execute(new Thread() {  
	                public void run() {  
	            		String luaScript="local storage = redis.call('get',KEYS[1]) " + 
	            				"if storage ~= false then " + 
	            				"	if storage ~= nil then " + 
	            				"		if (tonumber(storage) > 0 and tonumber(storage)>=tonumber(ARGV[1])) then " + 
	            				"			if (tonumber(storage) > 0 and tonumber(storage)==tonumber(ARGV[1])) then " + 
	            				"				redis.call('set',KEYS[1],tonumber(storage)-tonumber(ARGV[1])) " + 
	            				"				return 1" + 
	            				"			else " + 
	            				"				redis.call('set',KEYS[1],tonumber(storage)-tonumber(ARGV[1])) " + 
	            				"				return 0" + 
	            				"			end " + 
	            				"		else " + 
	            				"			return -2 " + 
	            				"		end " + 
	               				"	else " + 
	            				"		return -1 " + 
	            				"	end " + 
	            				"else " + 
	            				"	return -1 " + 
	            				"end";
	            		
	            		int args= Integer.parseInt(count);
	            		List<Serializable> list=new ArrayList<Serializable>();
	            		list.add("storage_seckill");
	            		a=redisService.executeScript(luaScript,list,args);
	                    latch.countDown();  
	                }  
	            }); 
	          
	        latch.await();  
	        if(a>=0) {
	        	SalesRecords record=new SalesRecords();
	        	record.setUserId(Integer.parseInt(id));
	        	record.setUpdateTime(new Date());
	        	record.setGoodsCount(Integer.parseInt(count));
	        	goodsService.addRecords(record);
	        	
	        	jsonResult.put("result","购买成功，还剩"+a);
	        	
	        	if(a==1) {
	        		goodsService.updateGoodsCount(Integer.parseInt(redisService.getCache("storage_seckill")+""), 1000);
	        	}
	        }else {
	        	if(a==-2) {
	        		goodsService.updateGoodsCount(Integer.parseInt(redisService.getCache("storage_seckill")+""), 1000);
	        	}
	        	jsonResult.put("result","秒杀完了");
	        }
	        //watch.stop();  
	          
	       // System.err.println("time:" + watch.getTotalTimeSeconds());  
	    } 
	
}
