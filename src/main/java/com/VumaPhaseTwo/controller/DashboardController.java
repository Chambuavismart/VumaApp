package com.VumaPhaseTwo.controller;

import com.VumaPhaseTwo.dto.dashboard.DashboardOverviewDto;
import com.VumaPhaseTwo.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200", "http://localhost:8080", "http://127.0.0.1:8080"}, allowCredentials = "true")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/overview")
    public ResponseEntity<DashboardOverviewDto> getOverview() {
        return ResponseEntity.ok(dashboardService.getDashboardOverview());
    }
}
