package com.unipi.mpsp.ticket_api.Services;

import com.unipi.mpsp.ticket_api.DataClasses.Auditorium;
import com.unipi.mpsp.ticket_api.DataClasses.Performance;
import com.unipi.mpsp.ticket_api.DataClasses.Show;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ShowService {
    Show saveShow(Show show);
    Show getShow(Long id);
    List<Show> getShowByAuditorium(Auditorium auditorium);
    List<Show> getShows(Performance performance);
    List<Show> getAllShows();
    void deleteShow(Show show);
}
