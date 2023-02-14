package com.unipi.mpsp.ticket_api.Controllers;

import com.unipi.mpsp.ticket_api.DataClasses.Show;
import com.unipi.mpsp.ticket_api.DataClasses.Ticket;
import com.unipi.mpsp.ticket_api.Services.ShowService;
import com.unipi.mpsp.ticket_api.Services.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ticket")
@RequiredArgsConstructor
public class TicketController {
    private final TicketService ticketService;
    private final ShowService showService;

    @GetMapping("/{showID}")
    public ResponseEntity<List<Ticket>> getTicketsForShow(@PathVariable Long showID){
        Show show = showService.getShow(showID);
        if(show==null){
            return ResponseEntity.notFound().header("Error-Message","Show Not Found").build();
        }
        List<Ticket> tickets = ticketService.getTicketsByShow(show);
        return ResponseEntity.ok().body(tickets);
    }
}
