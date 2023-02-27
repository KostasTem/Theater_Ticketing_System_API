package com.unipi.mpsp.ticket_api.Controllers;


import com.unipi.mpsp.ticket_api.DataClasses.Performance;
import com.unipi.mpsp.ticket_api.Services.AppUserService;
import com.unipi.mpsp.ticket_api.Services.PerformanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/performance")
@RequiredArgsConstructor
public class PerformanceController {
    private final AppUserService appUserService;
    private final PerformanceService performanceService;

    @GetMapping("/")
    @Secured({"ROLE_ADMIN"})
    public ResponseEntity<Performance> getPer(Principal principal){
        Performance performance = appUserService.getUser(principal.getName()).getPerformance();
        performance.setUser(null);
        return ResponseEntity.ok().body(performance);
    }

    @GetMapping("/available/")
    @Secured({"ROLE_SYSTEM_ADMIN"})
    public ResponseEntity<List<Performance>> getAvailable(){
        List<Performance> performances = performanceService.getPerformances();
        performances = performances.stream().filter(performance -> performance.getUser()==null).collect(Collectors.toList());
        return ResponseEntity.ok().body(performances);
    }
}
