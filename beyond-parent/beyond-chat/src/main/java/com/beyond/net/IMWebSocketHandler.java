package com.beyond.net;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.adapter.AbstractWebSocketSession;

import com.beyond.net.vo.Message;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Socket处理器
 * 
 * @author chenjunan
 * @Date 2016年1月11日 下午1:19:50
 */
@Component
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { RuntimeException.class, Exception.class })
public class IMWebSocketHandler implements WebSocketHandler {
	public static final Map<String, WebSocketSession> userSocketSessionMap;

	static {
		userSocketSessionMap = new HashMap<String, WebSocketSession>();
	}

	/**
	 * 建立连接后
	 */
	public void afterConnectionEstablished(WebSocketSession session) {
		try {
			String uid = session.getAttributes().get("userId") + "";
			if (userSocketSessionMap.get(uid) == null) {
				userSocketSessionMap.put(uid, session);
			}else{
				userSocketSessionMap.remove(uid);
				userSocketSessionMap.put(uid, session);
			}
			
			final AbstractWebSocketSession sessions = (AbstractWebSocketSession)userSocketSessionMap.get(uid);
			new Thread(new Runnable() {
	            public void run() {
	            	while(sessions.isOpen()){
	            		try {
	            			Thread.sleep(1000 * 5);
	            		} catch (InterruptedException e) {
	            			e.printStackTrace();
	            		}
	            		try {
	            			sessions.sendMessage(new PingMessage());
	            		} catch (IOException e) {
	            			Thread.currentThread().stop();
	            		}
	            	}
	            }
	        }).start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 消息处理，在客户端通过Websocket API发送的消息会经过这里，然后进行相应的处理
	 */
	public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
		if (message instanceof TextMessage) {
			try {
				if (!message.isLast()) return;
				Message msg = new Gson().fromJson(message.getPayload().toString(), Message.class);
				msg.setDate(new Date());
				sendMessageToUser(msg.getTo(), new TextMessage(new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create().toJson(msg)));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else if (message instanceof PongMessage) {
			handlePongMessage(session, (PongMessage) message);
		}else if (message instanceof PingMessage) {
			handlePingMessage(session, (PingMessage) message);
		}else {
			throw new IllegalStateException("Unexpected WebSocket message type: " + message);
		}
		
	}

	/**
	 * 消息传输错误处理
	 */
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		if (session.isOpen()) {
			session.close();
		}
		Iterator<Entry<String, WebSocketSession>> it = userSocketSessionMap.entrySet().iterator();
		// 移除Socket会话
		while (it.hasNext()) {
			Entry<String, WebSocketSession> entry = it.next();
			if (entry.getValue().getId().equals(session.getId())) {
				userSocketSessionMap.remove(entry.getKey());
				System.out.println("Socket会话已经移除:用户ID" + entry.getKey());
				break;
			}
		}
	}

	/**
	 * 关闭连接后
	 */
	public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
		System.out.println("Websocket:" + session.getId() + "已经关闭"+closeStatus);
		Iterator<Entry<String, WebSocketSession>> it = userSocketSessionMap.entrySet().iterator();
		// 移除Socket会话
		while (it.hasNext()) {
			Entry<String, WebSocketSession> entry = it.next();
			if (entry.getValue().getId().equals(session.getId())) {
				userSocketSessionMap.remove(entry.getKey());
				System.out.println("Socket会话已经移除:用户ID" + entry.getKey());
				break;
			}
		}
	}

	public boolean supportsPartialMessages() {
		return false;
	}

	/**
	 * 给所有在线用户发送消息
	 * 
	 * @param message
	 * @throws IOException
	 */
	public void broadcast(final TextMessage message) throws IOException {
		Iterator<Entry<String, WebSocketSession>> it = userSocketSessionMap.entrySet().iterator();

		// 多线程群发
		while (it.hasNext()) {

			final Entry<String, WebSocketSession> entry = it.next();

			if (entry.getValue().isOpen()) {
				// entry.getValue().sendMessage(message);
				new Thread(new Runnable() {

					public void run() {
						try {
							if (entry.getValue().isOpen()) {
								entry.getValue().sendMessage(message);
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}

				}).start();
			}

		}
	}

	/**
	 * 给某个用户发送消息
	 * 
	 * @param userName
	 * @param message
	 * @throws IOException
	 */
	public void sendMessageToUser(String uid, TextMessage message) throws IOException {
		final AbstractWebSocketSession session = (AbstractWebSocketSession) userSocketSessionMap.get(uid);
		if (session != null && session.isOpen()) {
			synchronized (session) {
				session.sendMessage(message);
			}
		}
	}
	
	public void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
		System.out.println("pong----------------------------------");
	}
	
	public void handlePingMessage(final WebSocketSession session, PingMessage message) throws Exception {
		System.out.println("ping----------------------------------");
	}

}
