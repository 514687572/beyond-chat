package com.stip.net.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.stip.net.dao.GoodsSeckillDao;
import com.stip.net.dao.SalesRecordsDao;
import com.stip.net.entity.GoodsSeckill;
import com.stip.net.entity.GoodsSeckillExample;
import com.stip.net.entity.SalesRecords;

@Service
public class GoodsService {
	@Resource
	private GoodsSeckillDao goodsSeckillDao;
	@Resource
	private SalesRecordsDao salesRecordsDao;
	
	public void updateGoodsCount(long count,long id) {
		GoodsSeckill record=goodsSeckillDao.selectByPrimaryKey(id);
		record.setNumber(Integer.parseInt(count+""));
		
		goodsSeckillDao.updateByPrimaryKey(record);
	}
	
	public void addRecords(SalesRecords record) {
		salesRecordsDao.insert(record);
	}
	
	public Integer getGoodsById(long id) {
		GoodsSeckillExample example=new GoodsSeckillExample();
		example.createCriteria().andSeckillIdEqualTo(id);
		
		List<GoodsSeckill> list=goodsSeckillDao.selectByExample(example);
		
		if(list!=null&&list.size()>0) {
			return list.get(0).getNumber();
		}else {
			return null;
		}
	}
	
}
