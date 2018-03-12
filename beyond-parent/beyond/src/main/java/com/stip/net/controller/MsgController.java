package com.stip.net.controller;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.socket.TextMessage;

import com.google.gson.GsonBuilder;
import com.stip.net.imessage.IMWebSocketHandler;
import com.stip.net.service.RedisService;
import com.stip.net.vo.Message;

@Scope("request")
@RequestMapping("/msg")
@RestController
public class MsgController {
	@Resource
	private RedisService redisService;
	@Resource
	private IMWebSocketHandler handler;
	
	@RequestMapping(value = "/talk.do", method = { RequestMethod.PUT})
	public ModelAndView talk(HttpServletRequest request, HttpServletResponse response){
		Map<String,Object> jsonResult=new HashMap<String, Object>();
		String name=request.getParameter("name");
		String userId=request.getParameter("userId");
		
		request.getSession().setAttribute("uid",userId);
		request.getSession().setAttribute("name",name);
		
		//redisService.putCache("uid", new Date());
		
		return new ModelAndView("redirect:/talk.jsp");
	}
	
	// 发布系统广播（群发）
	@ResponseBody
	@RequestMapping(value = "broadcast.do", method = RequestMethod.POST)
	public void broadcast(String text,String from) throws IOException {
		Message msg = new Message();
		msg.setDate(new Date());
		msg.setFrom(from);
		msg.setFromName(from);
		msg.setText(text);
		handler.broadcast(new TextMessage(new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create().toJson(msg)));
	}
}
