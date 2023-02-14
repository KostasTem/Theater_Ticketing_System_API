package com.unipi.mpsp.ticket_api.Services;

import com.unipi.mpsp.ticket_api.DataClasses.Reservation;
import com.unipi.mpsp.ticket_api.DataClasses.Show;
import com.unipi.mpsp.ticket_api.DataClasses.Ticket;
import com.unipi.mpsp.ticket_api.Repositories.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;

    @Autowired
    public TicketServiceImpl(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Override
    public Ticket saveTicket(Ticket ticket) {
        return ticketRepository.save(ticket);
    }

    @Override
    public List<Ticket> getTicketsByReservation(Reservation reservation) {
        return ticketRepository.findByReservation(reservation);
    }

    @Override
    public List<Ticket> getTicketsByShow(Show show) {
        return ticketRepository.findByShow(show);
    }
}
