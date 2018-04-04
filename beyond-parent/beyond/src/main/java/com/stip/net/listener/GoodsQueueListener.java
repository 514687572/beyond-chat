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

import com.stip.net.pojo.GoodsRecords;
import com.stip.net.service.impl.GoodsService;

@Component
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { RuntimeException.class, Exception.class })
public class GoodsQueueListener extends MessageListenerAdapter{
	@Resource
	private GoodsService goodsService;
	@Resource
	private ThreadPoolTaskExecutor threadPoolTaskExecutor;
	 
	private static final String GOODS_RECORDS_QUEUE = "storageQueue";
	 
	@JmsListener(destination=GOODS_RECORDS_QUEUE)
    public void onMessage(final Message message) {  
		threadPoolTaskExecutor.execute(new Runnable() {  
            public void run() {   
            	try {
            		GoodsRecords record = (GoodsRecords) getMessageConverter().fromMessage(message);
            		goodsService.updateGoodsCount(record.getCount(), record.getGoodsId());
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
