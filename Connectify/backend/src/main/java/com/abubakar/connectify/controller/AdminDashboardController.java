package com.abubakar.connectify.controller;

import com.abubakar.connectify.dto.response.AdminDashboardResponse;
import com.abubakar.connectify.service.AdminDashboardService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
public class AdminDashboardController {

    private static final Logger logger =
            LoggerFactory.getLogger(
                    AdminDashboardController.class
            );

    @Autowired
    private AdminDashboardService adminDashboardService;

    @GetMapping
    public ResponseEntity<AdminDashboardResponse>
    getDashboardData() {

        logger.info(
                "Admin dashboard API called"
        );

        return ResponseEntity.ok(
                adminDashboardService.getDashboardData()
        );
    }

}

