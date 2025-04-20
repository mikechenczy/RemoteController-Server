package com.mj.lrp.dao;

import com.mj.lrp.model.Pay;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PayDao {

    Pay getByPayNo(@Param("payNo") String payNo);

    List<Pay> getPayByUserId(@Param("userId") int userId);

    List<Pay> getAll();

    int insert(Pay payment);

    int delete(@Param("payNo") String payNo);

    int deleteAllByUserId(@Param("userId") int userId);
}
