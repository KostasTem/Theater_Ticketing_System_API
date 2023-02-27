package com.unipi.mpsp.ticket_api.Controllers;

import com.unipi.mpsp.ticket_api.DataClasses.*;
import com.unipi.mpsp.ticket_api.Services.AppUserService;
import com.unipi.mpsp.ticket_api.Services.AuditoriumService;
import com.unipi.mpsp.ticket_api.Services.PerformanceService;
import com.unipi.mpsp.ticket_api.Services.ShowService;
import com.unipi.mpsp.ticket_api.Utils.Utilities;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auditorium")
@RequiredArgsConstructor
@Secured("ROLE_SYSTEM_ADMIN")
public class AuditoriumController {
    private final ShowService showService;
    private final AuditoriumService auditoriumService;
    private final PerformanceService performanceService;
    private final AppUserService appUserService;

    @GetMapping("/")
    public ResponseEntity<List<Auditorium>> getAuditoriums(){
        return ResponseEntity.ok().body(auditoriumService.getAuditoriums());
    }


    @GetMapping("/{id}")
    public ResponseEntity<Auditorium> getAuditorium(@PathVariable Long id){
        if(auditoriumService.getAuditorium(id)==null){
            return ResponseEntity.notFound().header("Error-Message","Auditorium Not Found").build();
        }
        return ResponseEntity.ok().body(auditoriumService.getAuditorium(id));
    }

    @PostMapping("/")
    public ResponseEntity<Auditorium> saveAuditorium(@RequestBody Auditorium auditorium){
        if(auditoriumService.getAuditoriumByName(auditorium.getName())==null){
            auditorium.setId(null);
            return ResponseEntity.ok().body(auditoriumService.saveAuditorium(auditorium));
        }
        return ResponseEntity.status(HttpStatus.SEE_OTHER).body(auditoriumService.getAuditoriumByName(auditorium.getName()));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Auditorium> updateAuditorium(@PathVariable Long id,@RequestBody Auditorium updatedAuditorium){
        Auditorium auditorium = auditoriumService.getAuditorium(id);
        if(auditorium==null){
            return ResponseEntity.notFound().header("Error-Message","Auditorium Not Found").build();
        }
        auditorium.setSeatsPerRow(updatedAuditorium.getSeatsPerRow());
        auditorium.setRows(updatedAuditorium.getRows());
        handleAuditoriumEdit(auditorium,updatedAuditorium.getRows(),updatedAuditorium.getSeatsPerRow());
        return ResponseEntity.ok().body(auditoriumService.saveAuditorium(auditorium));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAuditorium(@PathVariable Long id){
        Auditorium auditorium = auditoriumService.getAuditorium(id);
        if(auditorium==null){
            return ResponseEntity.notFound().header("Error-Message","Auditorium Not Found").build();
        }
        handleAuditoriumDeletion(auditorium);
        return ResponseEntity.ok().body("Deleted");
    }

    private void handleAuditoriumDeletion(Auditorium auditorium){
        List<Show> shows = showService.getAllShows();
        List<Reservation> toCancel = new ArrayList<>();
        for(Show show:shows){
            if(Objects.equals(show.getAuditorium().getId(), auditorium.getId())){
                for(Ticket ticket:show.getTickets()){
                    if(ticket.getReservation()!=null){
                        if(!toCancel.contains(ticket.getReservation())){
                            toCancel.add(ticket.getReservation());
                        }
                        //NOTIFY USER BY EMAIL OF SHOW CANCELATION
                    }
                }
                for(Reservation res:toCancel){
                    AppUser appUser = res.getAppUser();
                    res.getTickets().forEach(ticket -> ticket.setReservation(null));
                    appUser.getReservations().remove(res);
                    appUserService.saveUser(appUser);
                }
                toCancel.clear();
                Performance performance = show.getPerformance();
                performance.getShows().remove(show);
                performanceService.savePerformance(performance);
            }
        }
        auditoriumService.deleteAuditorium(auditorium.getId());
    }
    private void handleAuditoriumEdit(Auditorium auditorium, Integer newRows, Integer newSeatsPerRow){
        List<Show> shows = showService.getAllShows();
        List<String> newSeats = Utilities.calculateNewSeats(newRows, newSeatsPerRow);
        List<Reservation> toCancel = new ArrayList<>();
        for(Show show:shows){
            List<Ticket> tickets = new ArrayList<>();
            if(Objects.equals(show.getAuditorium().getId(), auditorium.getId())){
                for(Ticket ticket:show.getTickets()){
                    if(!newSeats.contains(ticket.getSeat())){
                        if(ticket.getReservation()!=null) {
                            if(!toCancel.contains(ticket.getReservation())){
                                toCancel.add(ticket.getReservation());
                            }
                            ticket.setReservation(null);
                            //NOTIFY USER BY EMAIL OF SHOW CANCELATION
                        }
                        tickets.add(ticket);
                    }
                }
                for(Ticket ticket:tickets){
                    ticket.setReservation(null);
                    show.getTickets().remove(ticket);
                }
                for(Reservation res:toCancel){
                    AppUser appUser = res.getAppUser();
                    res.getTickets().forEach(ticket -> ticket.setReservation(null));
                    appUser.getReservations().remove(res);
                    appUserService.saveUser(appUser);
                }
                toCancel.clear();
                showService.saveShow(show);
            }
        }
        auditorium.setRows(newRows);
        auditorium.setSeatsPerRow(newSeatsPerRow);
        auditoriumService.saveAuditorium(auditorium);
    }
}
