package com.unipi.mpsp.ticket_api.Controllers;

import com.unipi.mpsp.ticket_api.DataClasses.*;
import com.unipi.mpsp.ticket_api.Services.*;
import com.unipi.mpsp.ticket_api.Utils.Utilities;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.RequestContext;

import java.security.Principal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

@RestController
@RequestMapping("/api/show")
@RequiredArgsConstructor
public class ShowController {
    private static final Logger log = LoggerFactory.getLogger(ShowController.class);
    private final ShowService showService;
    private final PerformanceService performanceService;
    private final AppUserService appUserService;
    private final ReservationService reservationService;
    private final AuditoriumService auditoriumService;

    @GetMapping("/future")
    public ResponseEntity<List<Map<String,Object>>> getFutureShows(){
        List<Map<String,Object>> res = new ArrayList<>();
        List<Show> shows = showService.getAllShows();
        shows = shows.stream().filter(show -> show.getDateTime().isAfter(LocalDateTime.now())).collect(Collectors.toList());
        shows.sort(Comparator.comparing(Show::getDateTime));
        Map<LocalDate,List<Show>> groupedShows = shows.stream().collect(Collectors.groupingBy(show -> show.getDateTime().toLocalDate()));
        for(LocalDate localDate: groupedShows.keySet()){
            Map<String,Object> temp = new HashMap<>();
            temp.put("date",localDate);
            temp.put("shows",groupedShows.get(localDate));
            res.add(temp);
        }
        return ResponseEntity.ok().body(res);
    }

    @GetMapping("/")
    public ResponseEntity<List<Show>> getShows(){
        log.info("All shows retrieved");
        return ResponseEntity.ok().body(showService.getAllShows());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Show> getShow(@PathVariable Long id){
        log.info("Show {} retrieved at {}", id ,LocalDateTime.now());
        Show show = showService.getShow(id);
        if(show==null){
            log.error("Show with id {} not found ",id);
            return ResponseEntity.notFound().header("Error-Message","Show Not Found").build();
        }
        return ResponseEntity.ok().body(showService.getShow(id));
    }

    @GetMapping("/performance/{id}")
    public ResponseEntity<List<Show>> getShowByPerformance(@PathVariable Long id){
        Performance performance = performanceService.getPerformanceByID(id);
        if(performance!=null) {
            List<Show> shows = performance.getShows();
            if (shows.size()==0) {
                log.error("Performance {} has no shows", performance.getName());
                return ResponseEntity.notFound().header("Error-Message", "No Shows Found For Performance").build();
            }
            log.info("Shows for performance {} retrieved", id);
            return ResponseEntity.ok().body(shows);
        }
        log.error("Performance with id {} not found", id);
        return ResponseEntity.notFound().header("Error-Message","Performance Not Found").build();
    }
    @PostMapping("/available")
    public ResponseEntity<List<Auditorium>> getAvailableAuditoriums(@RequestBody Show carrier, Principal principal){
        LocalDateTime date = carrier.getDateTime();
        date = date.plusHours(2);
        Performance performance = appUserService.getUser(principal.getName()).getPerformance();
        List<Auditorium> unavailable = new ArrayList<>();
        List<Auditorium> auditoriums = auditoriumService.getAuditoriums();
        for(Auditorium auditorium:auditoriums){
            for(Show show:showService.getShowByAuditorium(auditorium)){
                if ((date.isAfter(show.getDateTime()) && date.isBefore(show.getDateTime().plusMinutes(show.getPerformance().getDuration() + 60))) ||
                        date.plusMinutes(performance.getDuration() + 60).isAfter(show.getDateTime()) && date.plusMinutes(performance.getDuration()).isBefore(show.getDateTime().plusMinutes(show.getPerformance().getDuration()))){
                    unavailable.add(auditorium);
                    break;
                }
            }
        }
        auditoriums.removeAll(unavailable);
        return ResponseEntity.ok().body(auditoriums);
    }

    @PostMapping("/")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Show> saveShow(@RequestBody Show newShow, Principal principal){
        Performance performance = performanceService.getPerformance(newShow.getPerformance().getName());
        newShow.setDateTime(newShow.getDateTime().plusHours(2));
        if(performance==null){
            return ResponseEntity.notFound().header("Error-Message","Referenced Performance Doesn't Exist").build();
        }
        if(performance.getUser() != appUserService.getUser(principal.getName())){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).header("Error-Message","Admin Tried To Add Show Of Performance They Don't Control").build();
        }
        if(newShow.getDateTime().isBefore(LocalDateTime.now().plusDays(1))){
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).header("Error-Message","New Show Must Be At Least 24 Hours In The Future").body(null);
        }
        List<Show> shows = showService.getAllShows();
        for(Show show:shows) {
            if(show.getAuditorium().getName().equals(newShow.getAuditorium().getName())) {
                if ((newShow.getDateTime().isAfter(show.getDateTime()) && newShow.getDateTime().isBefore(show.getDateTime().plusMinutes(show.getPerformance().getDuration() + 60))) ||
                        newShow.getDateTime().plusMinutes(performance.getDuration() + 60).isAfter(show.getDateTime()) && newShow.getDateTime().plusMinutes(performance.getDuration()).isBefore(show.getDateTime().plusMinutes(show.getPerformance().getDuration()))) {
                    log.error("User {} tried to add show during another show", performance.getUser().getEmail());
                    return ResponseEntity.badRequest().header("Error-Message", "Show Already Exists During The Given Time Slot").build();
                }
            }
        }
        newShow.setPerformance(performance);
        newShow.generateTickets();
        return ResponseEntity.ok().body(showService.saveShow(newShow));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Show> updateShow(@PathVariable Long id,@RequestBody Show updatedShowTime,Principal principal){
        Show updatedShow = showService.getShow(id);
        updatedShowTime.setDateTime(updatedShowTime.getDateTime().plusHours(2));
        if(updatedShow!=null) {
            if(updatedShow.getPerformance().getUser() != appUserService.getUser(principal.getName())){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).header("Error-Message","Admin Tried To Edit Show Of Performance They Don't Control").build();
            }
            if(updatedShowTime.getDateTime().isBefore(updatedShow.getDateTime())){
                return ResponseEntity.badRequest().header("Error-Message","Updated Show Time Can't Be Before Original").body(null);
            }
            if(updatedShowTime.getAuditorium().getName().equals(updatedShow.getAuditorium().getName()) && updatedShowTime.getDateTime().equals(updatedShow.getDateTime())){
                return ResponseEntity.ok().body(updatedShow);
            }
            for (Show show : showService.getAllShows()) {
                if (show.getAuditorium().getName().equals(updatedShowTime.getAuditorium().getName())) {
                    if ((updatedShowTime.getDateTime().isAfter(show.getDateTime()) && updatedShowTime.getDateTime().isBefore(show.getDateTime().plusMinutes(show.getPerformance().getDuration() + 60))) ||
                            updatedShowTime.getDateTime().plusMinutes(updatedShow.getPerformance().getDuration() + 60).isAfter(show.getDateTime()) && updatedShowTime.getDateTime().plusMinutes(updatedShow.getPerformance().getDuration()).isBefore(show.getDateTime().plusMinutes(show.getPerformance().getDuration()))) {
                        log.error("User {} tried to add show during another show", principal.getName());
                        return ResponseEntity.badRequest().header("Error-Message", "Show Already Exists During The Given Time Slot").build();
                    }
                }
            }
            List<AppUser> notifiedUsers = new ArrayList<>();
            for (Ticket ticket : updatedShow.getTickets()) {
                if (ticket.getReservation() != null) {
                    if (!notifiedUsers.contains(ticket.getReservation().getAppUser())) {
                        //Notify User
                        notifiedUsers.add(ticket.getReservation().getAppUser());
                    }
                }
            }
            updatedShow.setDateTime(updatedShowTime.getDateTime());
            return ResponseEntity.ok().body(showService.saveShow(updatedShow));
        }
        log.error("Show with id {} doesn't exist",id);
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Show ID Not Valid.");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<String> deleteShow(@PathVariable Long id, Principal principal){
        Show show = showService.getShow(id);
        if(show!=null) {
            Performance performance = show.getPerformance();
            if(!performance.getUser().getEmail().equals(principal.getName())){
                log.error("User {} tried to delete show of performance they are not admin of", principal.getName());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).header("Error-Message","Admin Tried To Delete Show Of Performance They Don't Control").build();
            }
            List<Reservation> reservationsToDelete = new ArrayList<>();
            for(Ticket ticket:show.getTickets()){
                if(ticket.getReservation()!=null){
                    if(!reservationsToDelete.contains(ticket.getReservation())){
                        reservationsToDelete.add(ticket.getReservation());
                    }
                }
            }
            for(Reservation reservation: reservationsToDelete){
                for(Ticket ticket: reservation.getTickets()){
                    ticket.setReservation(null);
                }
                //Notify User
                AppUser appUser= reservation.getAppUser();
                appUser.getReservations().remove(reservation);
                appUserService.saveUser(appUser);
            }
            performance.getShows().remove(show);
            performanceService.savePerformance(performance);
            return ResponseEntity.ok().body("Success");
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Show ID Not Valid.");
    }
}
