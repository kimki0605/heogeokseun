package com.heogeokseun.controller;

import com.heogeokseun.mapper.TestMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DbTestController {

    private final TestMapper testMapper;

    public DbTestController(TestMapper testMapper) {
        this.testMapper = testMapper;
    }

    @GetMapping("/db-test")
    public String dbTest() {
        String databaseName = testMapper.getDatabaseName();
        return "Oracle DB 연결 성공! DB Name: " + databaseName;
    }
}