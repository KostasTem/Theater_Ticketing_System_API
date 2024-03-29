package com.unipi.mpsp.ticket_api.Services;

import com.unipi.mpsp.ticket_api.DataClasses.AppUser;
import com.unipi.mpsp.ticket_api.DataClasses.Performance;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface AppUserService {
    AppUser saveUser(AppUser appUser,boolean encode);
    AppUser getUser(String email);
    AppUser getUserByPerformance(Performance performance);
    AppUser getById(Long id);
    List<AppUser> getUsers();
}
