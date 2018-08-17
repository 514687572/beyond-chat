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
import com.stip.net.service.RedisService;
import com.stip.net.service.impl.GoodsService;

@Component
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { RuntimeException.class, Exception.class })
public class UserQueueListener extends MessageListenerAdapter{
	@Resource
	private GoodsService goodsService;
	@Resource
	private RedisService<Integer> redisService;
	@Resource
	private ThreadPoolTaskExecutor threadPoolTaskExecutor;
	
	private static final String SALES_RECORDS_QUEUE = "userQueue";
	 
	@JmsListener(destination = SALES_RECORDS_QUEUE,concurrency="5-10")
	public void onMessage(final Message message) {
		try {
			SalesRecords record = (SalesRecords) getMessageConverter().fromMessage(message);
			saleGoods(record);
			message.acknowledge();
		} catch (MessageConversionException e) {
			e.printStackTrace();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized void saleGoods(SalesRecords record) {
		int allaCount = redisService.getCache("storage_seckill");
		System.out.println(allaCount+"------------------------------------->>>>");
		if (allaCount >= record.getGoodsCount()) {
			redisService.decr("storage_seckill", 1L);
			//goodsService.updateGoodsCount(allaCount - record.getGoodsCount(), Long.parseLong(record.getGoodsId()));
			goodsService.addRecords(record);
		}else {
			System.out.println(allaCount - record.getGoodsCount()+"------------------------------------->>>>问题少年");
		}
	}
	
}
