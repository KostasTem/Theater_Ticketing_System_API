package com.unipi.mpsp.ticket_api.Services;

import com.unipi.mpsp.ticket_api.DataClasses.AppUser;
import com.unipi.mpsp.ticket_api.DataClasses.Reservation;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ReservationService {
    Reservation saveReservation(Reservation reservation);
    List<Reservation> getReservations(AppUser appUser);
    void deleteReservation(Reservation reservation);
    Reservation getReservation(Long id);
}
