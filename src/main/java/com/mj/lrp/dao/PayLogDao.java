package com.mj.lrp.dao;

import com.mj.lrp.model.PayLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PayLogDao {

    PayLog getByPayNo(@Param("payNo") String payNo);

    List<PayLog> getAll();

    int insert(PayLog payLog);

    int delete(@Param("payNo") String payNo);
}
