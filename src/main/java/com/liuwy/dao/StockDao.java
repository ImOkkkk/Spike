package com.liuwy.dao;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import com.liuwy.pojo.Stock;

/**
 * @author:
 * @date: created in 20:59 2021/4/15
 * @version:
 */
@Repository
@Mapper
public interface StockDao {

    Stock selectStockById(String id);

    Stock selectStockByCode(String code);

    void initStockBefore(String id, Integer count);


    void createStock(Stock stock);
}