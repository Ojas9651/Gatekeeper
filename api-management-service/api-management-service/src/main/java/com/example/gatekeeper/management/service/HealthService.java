package com.example.gatekeeper.management.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HealthService {

    private final JdbcTemplate jdbcTemplate;

    public String healthCheck(){
        String dbStatus = "DOWN";

        Integer res = jdbcTemplate.queryForObject("SELECT 1", Integer.class);

        try{
            if(Integer.valueOf(1).equals(res)) dbStatus = "UP";
        }
        catch(Exception e){
            System.out.println("Connection can't be established with DataBase");
        }
        return dbStatus;
    }
}
