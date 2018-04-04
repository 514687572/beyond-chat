package com.stip.net.queue;

import java.io.Serializable;

import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

@Component
public class JmsQueueSender {
	@Resource
	private JmsTemplate jmsQueueTemplate;

	public void send(String destination, final Serializable message) {
		jmsQueueTemplate.send(destination,new MessageCreator(){
            public Message createMessage(Session session) throws JMSException{
                ObjectMessage objectMessage = session.createObjectMessage(message);
                return objectMessage;
            }
        });
	}
	
}
