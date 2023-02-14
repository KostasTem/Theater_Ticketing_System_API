package com.unipi.mpsp.ticket_api.Repositories;

import com.unipi.mpsp.ticket_api.DataClasses.AppUser;
import com.unipi.mpsp.ticket_api.DataClasses.Performance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser,Long> {
    AppUser findByEmail(String email);
    AppUser findByPerformance(Performance performance);
}
