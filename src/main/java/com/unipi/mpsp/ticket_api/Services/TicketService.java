package com.unipi.mpsp.ticket_api.Services;

import com.unipi.mpsp.ticket_api.DataClasses.Reservation;
import com.unipi.mpsp.ticket_api.DataClasses.Show;
import com.unipi.mpsp.ticket_api.DataClasses.Ticket;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface TicketService {
    Ticket saveTicket(Ticket ticket);
    List<Ticket> getTicketsByReservation(Reservation reservation);
    List<Ticket> getTicketsByShow(Show show);
}
