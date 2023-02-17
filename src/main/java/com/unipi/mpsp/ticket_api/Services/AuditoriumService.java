package com.unipi.mpsp.ticket_api.Services;

import com.unipi.mpsp.ticket_api.DataClasses.Auditorium;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface AuditoriumService {
    Auditorium saveAuditorium(Auditorium auditorium);
    Auditorium getAuditorium(Long id);
    Auditorium getAuditoriumByName(String name);
    List<Auditorium> getAuditoriums();
    void deleteAuditorium(Long id);
}
