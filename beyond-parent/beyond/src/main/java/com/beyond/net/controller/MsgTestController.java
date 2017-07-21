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
import org.springframework.web.servlet.ModelAndView;

import com.beyond.net.service.RedisService;

@Scope("request")
@RequestMapping("/msg")
@RestController
public class MsgTestController {
	@Resource
	private RedisService redisService;
	
	@RequestMapping(value = "/talk.do", method = { RequestMethod.PUT,RequestMethod.GET})
	public ModelAndView talk(HttpServletRequest request, HttpServletResponse response){
		Map<String,Object> jsonResult=new HashMap<String, Object>();
		String name=request.getParameter("name");
		String userId=request.getParameter("userId");
		
		request.getSession().setAttribute("uid",userId);
		request.getSession().setAttribute("name",name);
		
		redisService.putCache("uid", new Date());
		
		return new ModelAndView("redirect:/talk.jsp");
	}
}
