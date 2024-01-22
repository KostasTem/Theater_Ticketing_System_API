package com.unipi.mpsp.ticket_api.Controllers;

import com.unipi.mpsp.ticket_api.DataClasses.AppUser;
import com.unipi.mpsp.ticket_api.DataClasses.Performance;
import com.unipi.mpsp.ticket_api.Services.AppUserService;
import com.unipi.mpsp.ticket_api.Services.PerformanceService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
public class AppUserController {

    private final AppUserService appUserService;
    private final PerformanceService performanceService;
    private final BCryptPasswordEncoder passwordEncoder;
    private static final Logger log = LoggerFactory.getLogger(AppUserController.class);

    public AppUserController(AppUserService appUserService, PerformanceService performanceService, @Lazy BCryptPasswordEncoder passwordEncoder) {
        this.appUserService = appUserService;
        this.performanceService = performanceService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/all")
    @Secured("ROLE_SYSTEM_ADMIN")
    public ResponseEntity<List<AppUser>> getUsers(){
        List<AppUser> appUsers = appUserService.getUsers().stream().filter(appUser -> !appUser.getRoles().contains("SYSTEM_ADMIN")).toList();
        appUsers.forEach(appUser ->  {if(appUser.getPerformance()!=null){appUser.getPerformance().setUser(null);}});
        return ResponseEntity.ok().body(appUsers);
    }

    @GetMapping("/")
    @Secured("ROLE_USER")
    public ResponseEntity<AppUser> getUser(Principal principal){
       AppUser appUser = appUserService.getUser(principal.getName());
       if(appUser.getPerformance()!=null){
           appUser.getPerformance().setUser(null);
       }
       appUser.setReservations(null);
       return ResponseEntity.ok().body(appUser);
    }

    @GetMapping("/nonAdmins")
    @Secured("ROLE_SYSTEM_ADMIN")
    public ResponseEntity<List<AppUser>> getNonAdmins(){
        List<AppUser> appUsers = appUserService.getUsers().stream().filter(appUser -> !appUser.getRoles().contains("SYSTEM_ADMIN") && appUser.getPerformance()==null).toList();
        return ResponseEntity.ok().body(appUsers);
    }

    @GetMapping("/{email}")
    @Secured("ROLE_SYSTEM_ADMIN")
    public ResponseEntity<AppUser> getUser(@PathVariable String email){
        return ResponseEntity.ok().body(appUserService.getUser(email));
    }

    @PatchMapping("/{email}")
    @Secured("ROLE_SYSTEM_ADMIN")
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
            if(req.get("showID")!=null && updatedRoles.contains("ADMIN")){
                Performance performance = performanceService.getPerformanceByID(Long.valueOf((Integer) req.get("showID")));
                if(performance!=null && performance.getUser()==null){
                    if(appUser.getPerformance() != null){
                        Performance per = performanceService.getPerformanceByID(appUser.getPerformance().getId());
                        per.setUser(null);
                        performanceService.savePerformance(per);
                    }
                    appUser.setPerformance(performance);
                    performance.setUser(appUser);
                    performanceService.savePerformance(performance);
                    return ResponseEntity.ok().body(appUserService.saveUser(appUser,false));
                }
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).header("Error-Message","The performance you are trying to update already has an admin").build();
            }
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
        appUser = appUserService.saveUser(appUser,false);
        return ResponseEntity.ok().body(appUser);
    }

    @PatchMapping("/updatePassword")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<String> updatePassword(@RequestBody Map<String,String> req, Principal principal){
        String oldPass = req.get("oldPass");
        String newPass = req.get("newPass");
        AppUser appUser = appUserService.getUser(principal.getName());
        if(passwordEncoder.matches(oldPass,appUser.getPassword())){
            appUser.setPassword(newPass);
            appUserService.saveUser(appUser,true);
            log.info("User {} changed their password",appUser.getEmail());
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).header("Error-Message","Old Password Incorrect").build();
    }

    @PatchMapping("/updateImage")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<String> updateImage(@RequestBody String image, Principal principal){
        AppUser appUser = appUserService.getUser(principal.getName());
        appUser.setImage(image);
        appUserService.saveUser(appUser,false);
        log.info("User {} changed their profile picture",principal.getName());
        return ResponseEntity.ok().build();
    }
}
