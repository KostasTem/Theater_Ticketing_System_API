package com.unipi.mpsp.ticket_api.Services;

import com.unipi.mpsp.ticket_api.DataClasses.Auditorium;
import com.unipi.mpsp.ticket_api.Repositories.AuditoriumRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class AuditoriumServiceImpl implements AuditoriumService{
    private final AuditoriumRepository auditoriumRepository;

    @Override
    public Auditorium saveAuditorium(Auditorium auditorium) {
        return auditoriumRepository.save(auditorium);
    }

    @Override
    public Auditorium getAuditorium(Long id) {
        return auditoriumRepository.findById(id).orElse(null);
    }

    @Override
    public Auditorium getAuditoriumByName(String name) {
        return auditoriumRepository.findByName(name);
    }

    @Override
    public void deleteAuditorium(Long id) {
        auditoriumRepository.deleteById(id);
    }

    @Override
    public List<Auditorium> getAuditoriums() {
        return auditoriumRepository.findAll();
    }
}
