package com.mj.lrp.dao;

import com.mj.lrp.model.SignIn;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SignInDao {

    SignIn getByUserId(@Param("userId") String userId);

    List<SignIn> getAll();

    int insert(SignIn info);

    int delete(@Param("userId") String userId);

    int deleteAll();
}
