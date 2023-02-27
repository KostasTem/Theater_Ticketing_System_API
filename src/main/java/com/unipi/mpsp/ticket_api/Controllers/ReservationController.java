package com.unipi.mpsp.ticket_api.Controllers;

import com.unipi.mpsp.ticket_api.DataClasses.AppUser;
import com.unipi.mpsp.ticket_api.DataClasses.Reservation;
import com.unipi.mpsp.ticket_api.DataClasses.Show;
import com.unipi.mpsp.ticket_api.DataClasses.Ticket;
import com.unipi.mpsp.ticket_api.Services.AppUserService;
import com.unipi.mpsp.ticket_api.Services.ReservationService;
import com.unipi.mpsp.ticket_api.Services.ShowService;
import com.unipi.mpsp.ticket_api.Services.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reservation")
@RequiredArgsConstructor
@Secured("ROLE_USER")
public class ReservationController {
    private final static Logger log = LoggerFactory.getLogger(ReservationController.class);
    private final ReservationService reservationService;
    private final AppUserService appUserService;
    private final ShowService showService;
    private final TicketService ticketService;
    @GetMapping("/")
    public ResponseEntity<List<Map<String,Object>>> getReservations(Principal principal){
        List<Map<String, Object>> res = new ArrayList<>();
        AppUser appUser = appUserService.getUser(principal.getName());
        if(appUser==null){
            log.error("User {} requested their reservation but wasn't found in the database",principal.getName());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).header("Error-Message","User not found").body(null);
        }
        log.info("User {} retrieved reservations at {}",appUser.getEmail(), LocalDateTime.now());
        for (Reservation reservation: appUser.getReservations()) {
            Map<String, Object> temp = new HashMap<>();
            temp.put("show", reservation.getTickets().get(0).getShow());
            temp.put("reservation",reservation);
            res.add(temp);
        }
        return ResponseEntity.ok().body(res);
    }

    @PostMapping("/")
    public ResponseEntity<Reservation> saveReservation(@RequestBody Map<String,Object> req, Principal principal){
        List<String> ticketSeats;
        Long showID;
        //String[] date;
        //Month month;
        Show show = null;
        try {
            ticketSeats = (List<String>) req.get("tickets");
            showID = Long.valueOf((Integer) req.get("showID"));
            //date = ((String) req.get("timestamp")).split(":");
            //month = Month.of(Integer.parseInt(date[1]));
            //localDateTime = LocalDateTime.of(Integer.parseInt(date[0]), month, Integer.parseInt(date[2]), Integer.parseInt(date[3]), Integer.parseInt(date[4]), Integer.parseInt(date[5]));
            show = showService.getShow(showID);
        }catch (Exception e){
            log.error("Invalid request data format during reservation attempt at {} by {} with exception {}. Data {}", LocalDateTime.now(),principal.getName(),e.getMessage(),Arrays.toString(req.entrySet().toArray()));
            return ResponseEntity.badRequest().header("Access-Control-Allow-Origin","*").header("Error-Message","Invalid Data In Request.").body(null);
        }
        if(show == null){
            log.error("Invalid show id {} during reservation attempt at {} by {}", showID, LocalDateTime.now(),principal.getName());
            return ResponseEntity.badRequest().header("Error-Message","Invalid Show ID").body(null);
        }
        List<Ticket> tickets = show.getTickets().stream().filter(ticket -> ticketSeats.contains(ticket.getSeat()) && ticket.getReservation()==null).collect(Collectors.toList());
        if(tickets.size() != ticketSeats.size()){
           log.error("Some of the tickets in reservation by user {} weren't available",principal.getName());
           return ResponseEntity.status(HttpStatus.CONFLICT).header("Error-Message","Requested Tickets Not Available.").build();
        }
        AppUser appUser = appUserService.getUser(principal.getName());
        if(appUser==null){
            log.error("User {} requested their reservation but wasn't found in the database",principal.getName());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).header("Error-Message","User not found").body(null);
        }
        Reservation reservation = new Reservation(null,appUser,tickets,LocalDateTime.now());
        //appUser.getReservations().add(reservation);
        reservation = reservationService.saveReservation(reservation);
        if(reservation != null ) {
            for(Ticket ticket:tickets){
                ticket.setReservation(reservation);
                ticketService.saveTicket(ticket);
            }
            log.info("User {} made reservation {} at {}",appUser.getEmail(),reservation.getId(),LocalDateTime.now());
            return ResponseEntity.ok().body(reservation);
        }
        log.error("Unable to save reservation for user {} at {}",appUser.getEmail(),LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).header("Error-Message","Error Saving Reservation").body(null);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteReservation(@PathVariable Long id, Principal principal){
        Reservation reservation = reservationService.getReservation(id);
        AppUser appUser = appUserService.getUser(principal.getName());
        if(reservation!=null && reservation.getAppUser().equals(appUser)){
            for(Ticket ticket: appUser.getReservations().get(appUser.getReservations().indexOf(reservation)).getTickets()){
                ticket.setReservation(null);
                //ticketService.saveTicket(ticket);
            }
            appUser.getReservations().remove(reservation);
            if(appUserService.saveUser(appUser) == appUser) {
                log.info("User {} deleted reservation {} at {}",appUser.getEmail(),reservation.getId(),LocalDateTime.now());
                return ResponseEntity.ok().body("Success");
            }
            else{
                log.error("Unable to delete reservation {} for user {} at {}",reservation.getId(),appUser.getEmail(),LocalDateTime.now());
                return ResponseEntity.internalServerError().header("Error-Message","Error Deleting Requested Reservation").body("Error");
            }
        }
        if(reservation==null){
            log.error("User {} tried to delete non-existent reservation with id {} at {}",appUser.getEmail(),id,LocalDateTime.now());
            return ResponseEntity.badRequest().header("Error-Message","Reservation Doesn't Exist").body("Reservation Doesn't Exist");
        }
        log.error("User {} tried to delete reservation that isn't theirs at {}",appUser.getEmail(),LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).header("Error-Message","Unauthorized Request").body("Invalid User");
    }
}
