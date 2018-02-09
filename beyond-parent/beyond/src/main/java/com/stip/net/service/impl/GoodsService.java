package com.stip.net.service.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.stip.net.dao.GoodsSeckillDao;
import com.stip.net.entity.GoodsSeckill;

@Service
public class GoodsService {
	@Resource
	private GoodsSeckillDao goodsSeckillDao;
	
	public void updateGoodsCount(long count,long id) {
		GoodsSeckill record=goodsSeckillDao.selectByPrimaryKey(id);
		record.setNumber(Integer.parseInt(count+""));
		
		goodsSeckillDao.updateByPrimaryKey(record);
	}
}
