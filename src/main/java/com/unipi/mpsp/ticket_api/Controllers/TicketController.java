package com.unipi.mpsp.ticket_api.Controllers;

import com.unipi.mpsp.ticket_api.DataClasses.Reservation;
import com.unipi.mpsp.ticket_api.DataClasses.Show;
import com.unipi.mpsp.ticket_api.DataClasses.Ticket;
import com.unipi.mpsp.ticket_api.Services.ReservationService;
import com.unipi.mpsp.ticket_api.Services.ShowService;
import com.unipi.mpsp.ticket_api.Services.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ticket")
@RequiredArgsConstructor
public class TicketController {
    private final TicketService ticketService;
    private final ShowService showService;
    private final ReservationService reservationService;

    @GetMapping("/{showID}")
    @Secured("ROLE_USER")
    public ResponseEntity<Map<String,Object>> getTicketsForShow(@PathVariable Long showID){
        Show show = showService.getShow(showID);
        if(show==null){
            return ResponseEntity.notFound().header("Error-Message","Show Not Found").build();
        }
        List<Ticket> tickets = ticketService.getTicketsByShow(show);
        Map<String,Object> res = new HashMap<>();
        res.put("show",show);
        res.put("tickets",tickets);
        return ResponseEntity.ok().body(res);
    }

    @GetMapping("/checkIn/{reservationID}")
    @Secured("ROLE_USER")
    public ResponseEntity<Reservation> checkIn(@PathVariable Long reservationID, Principal principal){
        Reservation reservation = reservationService.getReservation(reservationID);
        if(reservation!=null && reservation.getTickets().size()>0 && !reservation.getTickets().get(0).getCheckedIn()){
            if(!reservation.getAppUser().getEmail().equals(principal.getName())){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).header("Error-Message","This Reservation Doesn't Belong To The Logged In User").body(null);
            }
            reservation.getTickets().forEach(ticket -> ticket.setCheckedIn(true));
            return ResponseEntity.ok().body(reservationService.saveReservation(reservation));
        }
        return ResponseEntity.notFound().header("Error-Message","Reservation Not Found").build();
    }

    @GetMapping("/info/{showID}")
    public ResponseEntity<Map<String,Integer>> getShowTicketInfo(@PathVariable Long showID){
        Map<String,Integer> res = new HashMap<>();
        Show show = showService.getShow(showID);
        if(show==null){
            return ResponseEntity.notFound().header("Error-Message","Show Not Found").build();
        }
        List<Ticket> tickets = ticketService.getTicketsByShow(show);
        res.put("ticketsSum",tickets.size());
        res.put("ticketsReserved",tickets.stream().filter(ticket -> ticket.getReservation()!=null).collect(Collectors.toList()).size());
        res.put("ticketsCheckedIn",tickets.stream().filter(ticket -> ticket.getReservation()!=null && ticket.getCheckedIn()).collect(Collectors.toList()).size());
        return ResponseEntity.ok().body(res);
    }
}
