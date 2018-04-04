package com.stip.net.pojo;

import java.io.Serializable;

public class GoodsRecords extends StipMessage implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private int goodsId;
	private int count;
	
	public int getGoodsId() {
		return goodsId;
	}
	public void setGoodsId(int goodsId) {
		this.goodsId = goodsId;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	
}
