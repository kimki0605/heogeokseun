package com.heogeokseun.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TestMapper {

    String getDatabaseName();
}