package com.beyond.net.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.beyond.net.service.RedisService;

@Scope("request")
@RequestMapping("/test")
@RestController
public class TestController {
	@Resource
	private RedisService redisService;
	
	@RequestMapping(value = "/updateVistiTime.do", method = { RequestMethod.PUT,RequestMethod.GET})
	public Map<String,Object> updateVistiTime(HttpServletRequest request, HttpServletResponse response){
		Map<String,Object> jsonResult=new HashMap<String, Object>();
		
		redisService.putCache("newTime", new Date());
		jsonResult.put("updateTime",redisService.getCache("newTime", Date.class));
		
		return jsonResult;
	}
}
