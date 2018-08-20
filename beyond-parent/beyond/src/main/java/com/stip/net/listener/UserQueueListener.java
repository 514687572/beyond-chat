package com.stip.net.listener;

import java.util.concurrent.TimeUnit;

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
	private RedisService redisService;
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
	
	/**
	 * 减库存，增加销售记录
	 * @param record
	 */
	public void saleGoods(SalesRecords record) {
		StringBuffer sb = new StringBuffer("storage_seckill");
		sb.append(record.getGoodsId().toString());
		
		int allaCount =Integer.valueOf(String.valueOf(redisService.getCache(sb.toString())));

		if (redisService.setNx(String.valueOf(record.getGoodsId()), "exist", 3000L,TimeUnit.MILLISECONDS)) {
			if (allaCount >= record.getGoodsCount()) {
				redisService.decr(sb.toString(), 1L);
				goodsService.updateGoodsCount(allaCount - record.getGoodsCount(),Long.valueOf(record.getGoodsId()));
				goodsService.addRecords(record);
			} else {
				System.out.println(allaCount - record.getGoodsCount() + "------------------------------------->>>>问题少年2");
			}
			
			redisService.remove(record.getGoodsId());
		}
	}
	
}
