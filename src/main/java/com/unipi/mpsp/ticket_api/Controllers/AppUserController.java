package com.unipi.mpsp.ticket_api.Controllers;

import com.unipi.mpsp.ticket_api.DataClasses.AppUser;
import com.unipi.mpsp.ticket_api.DataClasses.Performance;
import com.unipi.mpsp.ticket_api.Services.AppUserService;
import com.unipi.mpsp.ticket_api.Services.PerformanceService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Secured("ROLE_SYSTEM_ADMIN")
public class AppUserController {

    private final AppUserService appUserService;
    private final PerformanceService performanceService;
    private static final Logger log = LoggerFactory.getLogger(AppUserController.class);

    @GetMapping("/")
    public ResponseEntity<List<AppUser>> getUsers(){
        List<AppUser> appUsers = appUserService.getUsers().stream().filter(appUser -> !appUser.getRoles().contains("SYSTEM_ADMIN")).toList();
        appUsers.forEach(appUser ->  {if(appUser.getPerformance()!=null){appUser.getPerformance().setUser(null);}});
        return ResponseEntity.ok().body(appUsers);
    }

    @GetMapping("/{email}")
    public ResponseEntity<AppUser> getUser(@PathVariable String email){
        return ResponseEntity.ok().body(appUserService.getUser(email));
    }

    @PatchMapping("/{email}")
    public ResponseEntity<AppUser> updateUser(@PathVariable String email, @RequestBody Map<String,Object> req, Principal principal){
        List<String> updatedRoles = (List<String>) req.get("roles");
        List<String> update = new ArrayList<>();
        update.add("USER");
        AppUser appUser = appUserService.getUser(email);
        if(appUser==null){
            log.error("Invalid User Email {} for patch",email);
            return ResponseEntity.notFound().header("Error-Message","User Not Found").build();
        }
        if(appUser.getRoles().contains("SYSTEM_ADMIN")){
            log.error("Attempt to change roles of user {} who is a System Admin by {}",appUser.getEmail(),principal.getName());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).header("Error-Message","Can't change roles of a System Admin").body(null);
        }
        if(appUser.getRoles().equals(updatedRoles)){
            return ResponseEntity.ok().body(appUser);
        }
        if(updatedRoles.contains("SYSTEM_ADMIN")){
            log.error("Attempt to make user {} System Admin by {}",appUser.getEmail(),principal.getName());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).header("Error-Message","Can't make user System Admins").body(null);
        }
        if(updatedRoles.contains("ADMIN")){
            Performance performance = performanceService.getPerformanceByID(Long.valueOf((Integer) req.get("showID")));
            if(performance==null){
                return ResponseEntity.notFound().header("Error-Message","Performance Not Found").build();
            }
            update.add("ADMIN");
            appUser.setPerformance(performance);
            performance.setUser(appUser);
            performanceService.savePerformance(performance);
        }
        else{
            Performance performance = appUser.getPerformance();
            if(performance!=null){
                performance.setUser(null);
                performanceService.savePerformance(performance);
            }
            appUser.setPerformance(null);
        }
        appUser.setRoles(update);
        appUser = appUserService.saveUser(appUser);
        return ResponseEntity.ok().body(appUser);
    }
}
