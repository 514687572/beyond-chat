package com.stip.net.listener;

import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.jms.Message;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.listener.adapter.MessageListenerAdapter;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.stip.net.entity.SalesRecords;
import com.stip.net.service.impl.GoodsService;

@Component
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { RuntimeException.class, Exception.class })
public class RecordsQueueListener extends MessageListenerAdapter{
	@Resource
	private GoodsService goodsService;
	@Resource
	private ThreadPoolTaskExecutor threadPoolTaskExecutor;
	
	private static final String SALES_RECORDS_QUEUE = "recordsQueue";
	 
	@JmsListener(destination=SALES_RECORDS_QUEUE)
    public void onMessage(final Message message) {  
    	threadPoolTaskExecutor.execute(new Runnable() {  
            public void run() {   
            	try {
            		SalesRecords record = (SalesRecords) getMessageConverter().fromMessage(message);
            		goodsService.addRecords(record);
            		message.acknowledge();
            	} catch (MessageConversionException e) {
            		e.printStackTrace();
            	} catch (JMSException e) {
            		e.printStackTrace();
            	} 
            }  
        }); 
    }
}
