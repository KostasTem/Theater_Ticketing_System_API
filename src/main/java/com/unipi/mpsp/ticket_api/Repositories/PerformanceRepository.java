package com.unipi.mpsp.ticket_api.Repositories;

import com.unipi.mpsp.ticket_api.DataClasses.Performance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PerformanceRepository extends JpaRepository<Performance,Long> {
    Performance findByName(String name);
}
