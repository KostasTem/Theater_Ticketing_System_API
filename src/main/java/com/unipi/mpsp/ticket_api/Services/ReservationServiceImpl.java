package com.unipi.mpsp.ticket_api.Services;

import com.unipi.mpsp.ticket_api.DataClasses.AppUser;
import com.unipi.mpsp.ticket_api.DataClasses.Reservation;
import com.unipi.mpsp.ticket_api.Repositories.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ReservationServiceImpl implements ReservationService{

    private final ReservationRepository reservationRepository;

    @Autowired
    public ReservationServiceImpl(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Override
    public Reservation saveReservation(Reservation reservation) {
        return reservationRepository.save(reservation);
    }

    @Override
    public List<Reservation> getReservations(AppUser appUser) {
        return reservationRepository.findByAppUser(appUser);
    }

    @Override
    public void deleteReservation(Reservation reservation) {
        reservationRepository.delete(reservation);
    }

    @Override
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    @Override
    public Reservation getReservation(Long id) {
        return reservationRepository.findById(id).orElse(null);
    }
}
