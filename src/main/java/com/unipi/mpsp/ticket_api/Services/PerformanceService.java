package com.unipi.mpsp.ticket_api.Services;

import com.unipi.mpsp.ticket_api.DataClasses.Performance;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface PerformanceService {
    Performance savePerformance(Performance performance);
    Performance getPerformance(String name);
    Performance getPerformanceByID(Long id);
    List<Performance> getPerformances();
}
