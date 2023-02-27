package com.unipi.mpsp.ticket_api.Services;

import com.unipi.mpsp.ticket_api.DataClasses.Auditorium;
import com.unipi.mpsp.ticket_api.DataClasses.Performance;
import com.unipi.mpsp.ticket_api.DataClasses.Show;
import com.unipi.mpsp.ticket_api.Repositories.ShowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ShowServiceImpl implements ShowService {

    private final ShowRepository showRepository;

    @Autowired
    public ShowServiceImpl(ShowRepository showRepository) {
        this.showRepository = showRepository;
    }

    @Override
    public Show saveShow(Show show) {
        return showRepository.save(show);
    }

    @Override
    public List<Show> getShows(Performance performance) {
        return showRepository.findByPerformance(performance);
    }

    @Override
    public Show getShow(Long id) {
        return showRepository.findById(id).orElse(null);
    }

    @Override
    public List<Show> getShowByAuditorium(Auditorium auditorium) {
        return showRepository.findByAuditorium(auditorium);
    }

    @Override
    public List<Show> getAllShows() {
        return showRepository.findAll();
    }

    @Override
    public void deleteShow(Show show) {
        showRepository.delete(show);
    }
}
