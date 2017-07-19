package com.beyond.net;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

/**
 * 客户端和服务端握手建立通信
 * 
 * @author chenjunan 2016-01-27
 */
public class HandShake extends HttpSessionHandshakeInterceptor {
	@Override
	public boolean beforeHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
		ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) serverHttpRequest;
		HttpServletRequest request = servletRequest.getServletRequest();

		String userName = request.getParameter("userId");
		if (!StringUtils.isEmpty(userName)) {
			attributes.put("userId", userName);
			System.out.println("a client userName=" + userName);
			super.beforeHandshake(serverHttpRequest, serverHttpResponse, wsHandler, attributes);

			return true;
		} else {
			return false;
		}
	}

	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception ex) {
		super.afterHandshake(request, response, wsHandler, ex);
	}

}
