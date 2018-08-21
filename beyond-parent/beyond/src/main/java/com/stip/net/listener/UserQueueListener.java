package com.stip.net.listener;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.jms.JMSException;
import javax.jms.Message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.listener.adapter.MessageListenerAdapter;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.stip.net.entity.SalesRecords;
import com.stip.net.service.RedisService;
import com.stip.net.service.impl.GoodsService;

@Component
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { RuntimeException.class, Exception.class })
public class UserQueueListener extends MessageListenerAdapter{
	@Autowired
	private GoodsService goodsService;
	@Autowired
	private RedisService redisService;
	
	private static final String SALES_RECORDS_QUEUE = "userQueue";
	
	private Lock lock = new ReentrantLock();
	
	private int k=0;
	 
	@JmsListener(destination = SALES_RECORDS_QUEUE,concurrency="5-10")
	public void onMessage(final Message message) {
		try {
			SalesRecords record = (SalesRecords) getMessageConverter().fromMessage(message);
			rennSaleGoods(record);
			message.acknowledge();
		} catch (MessageConversionException e) {
			e.printStackTrace();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * redis setNx 实现分布式锁
	 * 减库存，增加销售记录
	 * @param record
	 */
	public void saleGoods(SalesRecords record) {
		StringBuffer sb = new StringBuffer("storage_seckill");
		sb.append(record.getGoodsId().toString());
		
		int allaCount =Integer.valueOf(String.valueOf(redisService.getCache(sb.toString())));

		if (redisService.setNx(String.valueOf(record.getGoodsId()), "exist", 3000L,TimeUnit.MILLISECONDS)) {
			System.out.println("获取锁成功");
			if (allaCount >= record.getGoodsCount()) {
				redisService.decr(sb.toString(), 1L);
				goodsService.updateGoodsCount(allaCount - record.getGoodsCount(),Long.valueOf(record.getGoodsId()));
				goodsService.addRecords(record);
			} else {
				System.out.println("库存不足");
			}
			
			redisService.remove(record.getGoodsId());
		}else {
			System.out.println("获取锁失败");
		}
	}
	
	/**
	 * 使用synchronized实现悲观锁 减库存，增加销售记录
	 * 
	 * @param record
	 */
	public synchronized void syncSaleGoods(SalesRecords record) {
		int allaCount = goodsService.getGoodsById(Long.valueOf(record.getGoodsId()));
		if (allaCount >= record.getGoodsCount()) {
			goodsService.updateGoodsCount(allaCount - record.getGoodsCount(), Long.valueOf(record.getGoodsId()));
			goodsService.addRecords(record);
		} else {
			System.out.println("库存不足");
		}
	}
	
	/**
	 * 使用ReentrantLock实现悲观锁 减库存，增加销售记录
	 * 
	 * @param record
	 */
	public void rennSaleGoods(SalesRecords record) {
		if (lock.tryLock()) {
			k++;
			System.out.println(k);
			try {
				int allaCount = goodsService.getGoodsById(Long.valueOf(record.getGoodsId()));
				if (allaCount >= record.getGoodsCount()) {
					goodsService.updateGoodsCount(allaCount - record.getGoodsCount(), Long.valueOf(record.getGoodsId()));
					goodsService.addRecords(record);
				} else {
					System.out.println("库存不足");
				}
			}finally {
				lock.unlock();
			}
		}
		
	}
	
}
