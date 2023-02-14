package com.unipi.mpsp.ticket_api.Repositories;

import com.unipi.mpsp.ticket_api.DataClasses.Reservation;
import com.unipi.mpsp.ticket_api.DataClasses.Show;
import com.unipi.mpsp.ticket_api.DataClasses.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket,Long> {
    List<Ticket> findByReservation(Reservation reservation);
    List<Ticket> findByShow(Show show);
}
