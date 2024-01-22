package com.unipi.mpsp.ticket_api.Controllers;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.unipi.mpsp.ticket_api.DataClasses.AppUser;
import com.unipi.mpsp.ticket_api.DataClasses.Performance;
import com.unipi.mpsp.ticket_api.Services.AppUserService;
import com.unipi.mpsp.ticket_api.Services.PerformanceService;
import com.unipi.mpsp.ticket_api.Utils.NewPerformanceRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/performance")
@RequiredArgsConstructor
public class PerformanceController {
    private final AppUserService appUserService;
    private final PerformanceService performanceService;

    @GetMapping("/")
    @Secured({"ROLE_ADMIN"})
    public ResponseEntity<Performance> getPer(Principal principal){
        Performance performance = appUserService.getUser(principal.getName()).getPerformance();
        performance.setUser(null);
        return ResponseEntity.ok().body(performance);
    }

    @GetMapping("/available/")
    @Secured({"ROLE_SYSTEM_ADMIN"})
    public ResponseEntity<List<Performance>> getAvailable(){
        List<Performance> performances = performanceService.getPerformances();
        performances = performances.stream().filter(performance -> performance.getUser()==null).collect(Collectors.toList());
        return ResponseEntity.ok().body(performances);
    }

    @PostMapping("/addImage/{id}")
    public ResponseEntity<String> setImage(@PathVariable Long id,@RequestBody String image){
        Performance performance = performanceService.getPerformanceByID(id);
        performance.setImage(image);
        performanceService.savePerformance(performance);
        return ResponseEntity.ok().body("Success");
    }

    @PostMapping("/")
    @Secured({"ROLE_SYSTEM_ADMIN"})
    public ResponseEntity<Performance> savePerformance(@RequestBody NewPerformanceRequest request){
        Performance performance = request.getPerformance();
        String email = request.getEmail();
        AppUser appUser = !Objects.equals(email, null) ? appUserService.getUser(email): null;
        if(performanceService.getPerformance(performance.getName())==null){
            if(appUser==null) {
                return ResponseEntity.ok().body(performanceService.savePerformance(performance));
            }
            else {
                if(appUser.getPerformance()==null) {
                    appUser.setPerformance(performance);
                    performance.setUser(appUser);
                    appUser.setRoles(List.of("USER","ADMIN"));
                    return ResponseEntity.ok().body(appUserService.saveUser(appUser,false).getPerformance());
                }
                else{
                    return ResponseEntity.status(HttpStatus.SEE_OTHER).header("Error-Message","User already is an admin of another performance").build();
                }
            }
        }
        return ResponseEntity.status(HttpStatus.SEE_OTHER).header("Error-Message","Performance with provided name already exists").build();
    }

    @PatchMapping("/{id}")
    @Secured({"ROLE_ADMIN"})
    public ResponseEntity<Performance> updatePerformance(@PathVariable Long id, @RequestBody Performance performance,Principal principal){
        Performance oldPerformance = performanceService.getPerformanceByID(id);
        if(!principal.getName().equals(oldPerformance.getUser().getEmail())){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).header("Error-Message","You can't edit this performance").build();
        }
        oldPerformance.setImage(performance.getImage());
        oldPerformance.setName(performance.getName());
        if(oldPerformance.getShows().stream().filter(show -> show.getDateTime().isAfter(LocalDateTime.now())).collect(Collectors.toList()).size()>0 && (!Objects.equals(performance.getDuration(), oldPerformance.getDuration()) || !Objects.equals(performance.getTicketPrice(), oldPerformance.getTicketPrice()))){
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).header("Error-Message","All future show must be deleted before changing the ticket price or duration of the performance").build();
        }
        oldPerformance.setTicketPrice(performance.getTicketPrice());
        oldPerformance.setDuration(performance.getDuration());
        return ResponseEntity.ok().body(performanceService.savePerformance(oldPerformance));
    }
}
