package com.liuwy.task;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Value;

import com.liuwy.dao.StockDao;
import com.liuwy.dao.StockOrderDao;
import com.liuwy.pojo.Stock;
import com.liuwy.pojo.StockOrder;

import cn.hutool.core.collection.CollectionUtil;

/**
 * @author:
 * @date: created in 20:13 2021/4/26
 * @version:
 */
public class CreateOrderTask implements Callable {
    private SqlSessionFactory sqlSessionFactory;
    private int commitSize;
    private List<Stock> objList;

    @Value("${jdbcType:MySQL}")
    private String jdbcType;

    public CreateOrderTask(SqlSessionFactory sqlSessionFactory, int commitSize, List<Stock> objList) {
        this.sqlSessionFactory = sqlSessionFactory;
        // 控制批量操作的大小不超过一千
        this.commitSize = commitSize > 1000 ? 1000 : commitSize;
        this.objList = objList;
    }

    @Override
    public Object call() throws Exception {
        return doLoader(objList);
    }

    private int doLoader(List<Stock> stockList) {

        saveStock(stockList);
        return stockList.size();
    }

    private void saveStock(List<Stock> stockList) {
        if (CollectionUtil.isNotEmpty(stockList)) {
            SqlSession sqlSession = null;
            try {
                sqlSession = openSession(this.sqlSessionFactory);
                StockDao stockDao = sqlSession.getMapper(StockDao.class);
                StockOrderDao stockOrderDao = sqlSession.getMapper(StockOrderDao.class);
                for (Stock stock : stockList) {
                    // 乐观锁更新
                    Integer updateCount = stockDao.updateByOptimistic(stock);
                    StockOrder stockOrder = new StockOrder();
                    stockOrder.setCreateTime(new Date());
                    stockOrder.setName(stock.getName());
                    stockOrder.setSid(stock.getId());
                    stockOrderDao.insertSelective(stockOrder);
                }
                commit(sqlSession);
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                sqlSession.close();
            }
        }
    }

    private void commit(SqlSession sqlSession) throws SQLException {
        if ("Oracle".equalsIgnoreCase(this.jdbcType)) {
            sqlSession.commit();
        } else {
            sqlSession.getConnection().commit();
        }
    }

    private SqlSession openSession(SqlSessionFactory sf) throws SQLException {
        SqlSession sqlSession;
        if ("Oracle".equalsIgnoreCase(this.jdbcType)) {
            sqlSession = sf.openSession(ExecutorType.BATCH, false);
        } else {
            sqlSession = sf.openSession();
            sqlSession.getConnection().setAutoCommit(false);
        }
        return sqlSession;
    }
}