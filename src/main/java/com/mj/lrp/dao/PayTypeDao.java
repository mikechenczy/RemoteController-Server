package com.mj.lrp.dao;

import com.mj.lrp.model.PayType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PayTypeDao {

    PayType getByPayType(@Param("payType") int payType);

    List<PayType> getAll();

    int insert(PayType payType);

    int delete(@Param("payType") String payType);
}
