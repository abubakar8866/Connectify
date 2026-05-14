package com.abubakar.connectify.controller;

import com.abubakar.connectify.dto.response.AdminDashboardResponse;
import com.abubakar.connectify.service.AdminDashboardService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/dashboard")
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
                "Admin dashboard request received"
        );

        AdminDashboardResponse response =
                adminDashboardService
                        .getDashboardData();

        logger.info(
                "Admin dashboard data fetched successfully"
        );

        return ResponseEntity.ok(response);
    }

}

