package com.stip.net.pojo;

import java.util.Date;

public class GoodsSalesRecords extends StipMessage {
	private static final long serialVersionUID = 1L;
	
    private Integer userId;

    private Integer goodsCount;

    private Date updateTime;

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Integer getGoodsCount() {
		return goodsCount;
	}

	public void setGoodsCount(Integer goodsCount) {
		this.goodsCount = goodsCount;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

}
