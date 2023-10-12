package com.spring.biz.orderset;

import com.spring.biz.order.OrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("orderSetService")
public class OrderSetServiceImpl implements OrderSetService {

    // Service 레이어가 관념적으로 존재하는데, 그것을 구현한 클래스

    @Autowired
    private OrderSetDAO orderSetDAO;

    @Override
    public boolean insert(OrderVO order) {
        return false;
    }

    @Override
    public List<OrderSet> selectAll(OrderVO order) {
        return orderSetDAO.selectAll(order);
    }

    @Override
    public OrderSet selectOne(OrderVO order) {
        return null;
    }

    @Override
    public boolean update(OrderVO order) {
        return false;
    }

    @Override
    public boolean delete(OrderVO order) {
        return false;
    }

}    //	OrderSetServiceImpl
