package com.liuwy.dao;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import com.liuwy.pojo.StockOrder;

@Repository
@Mapper
public interface StockOrderDao {
    void initOrderBefore(String sid);

    Integer insertSelective(StockOrder order);
}
