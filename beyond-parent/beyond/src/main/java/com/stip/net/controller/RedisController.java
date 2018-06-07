package com.stip.net.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.stip.net.entity.SalesRecords;
import com.stip.net.pojo.GoodsRecords;
import com.stip.net.pojo.GoodsSalesRecords;
import com.stip.net.queue.JmsQueueSender;
import com.stip.net.service.RedisService;
import com.stip.net.service.impl.GoodsService;

@Scope("request")
@RequestMapping("/test")
@RestController
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { RuntimeException.class, Exception.class })
public class RedisController {
	@Resource
	private RedisService redisService;
	@Resource
	private GoodsService goodsService;
	@Resource
	private ThreadPoolTaskExecutor threadPoolTaskExecutor;
	@Resource
	private JmsQueueSender jmsQueueSender;
	
	private static int threadCount = 1;
	private volatile long a=100;
	
	@RequestMapping(value = "/test.do", method = { RequestMethod.PUT,RequestMethod.GET})
	public Map<String, Object> seckill(HttpServletRequest request) throws InterruptedException {  
		final Map<String,Object> jsonResult=new HashMap<String, Object>();
        final CountDownLatch latch = new CountDownLatch(threadCount);  
        final String id=request.getParameter("id");
        final String count=request.getParameter("count");
	        
    	threadPoolTaskExecutor.execute(new Runnable() {  
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
	        
		threadPoolTaskExecutor.execute(new Runnable() {
			public void run() {
				final int currentCount = Integer.parseInt(redisService.getCache("storage_seckill") + "");
				if (a >= 0) {
					SalesRecords record = new SalesRecords();
					record.setUserId(Integer.parseInt(id));
					record.setUpdateTime(new Date());
					record.setGoodsCount(Integer.parseInt(count));
					jmsQueueSender.send("recordsQueue", record);

					if (a == 1) {
						GoodsRecords records = new GoodsRecords();
						records.setCount(currentCount);
						records.setGoodsId(1000);
						jmsQueueSender.send("storageQueue", records);
					}
					jsonResult.put("result", "购买成功");
				} else {
					if (a == -2) {
						GoodsRecords records = new GoodsRecords();
						records.setCount(currentCount);
						records.setGoodsId(1000);
						jmsQueueSender.send("storageQueue", records);
					}
					jsonResult.put("result", "秒杀完了");
				}
			}
		});
	        
	        return jsonResult;
	 }
	
	@RequestMapping(value = "/testAt.do", method = { RequestMethod.PUT,RequestMethod.GET})
	public Map<String, Object> testAt(HttpServletRequest request) throws InterruptedException {  
		final Map<String,Object> jsonResult=new HashMap<String, Object>();
        final String id=request.getParameter("id");
        final String count=request.getParameter("count");
        
    	threadPoolTaskExecutor.execute(new Runnable() {
    		public void run() {
    			SalesRecords record = new SalesRecords();
    			record.setUserId(new Random().nextInt(10000));
    			record.setUpdateTime(new Date());
    			record.setGoodsCount(new Random().nextInt(10)+1);
    			record.setGoogsId(id);
    			jmsQueueSender.send("userQueue", record);
    			
    			jsonResult.put("result", "购买成功");
    		}
    	});
	        
        return jsonResult;
	 } 
	
}
