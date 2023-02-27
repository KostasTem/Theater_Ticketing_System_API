package com.unipi.mpsp.ticket_api;

import com.unipi.mpsp.ticket_api.DataClasses.*;
import com.unipi.mpsp.ticket_api.Security.RsaKeyProperties;
import com.unipi.mpsp.ticket_api.Services.AppUserService;
import com.unipi.mpsp.ticket_api.Services.AuditoriumService;
import com.unipi.mpsp.ticket_api.Services.PerformanceService;
import com.unipi.mpsp.ticket_api.Services.ReservationService;
import org.checkerframework.checker.units.qual.A;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication()
@EnableConfigurationProperties(RsaKeyProperties.class)
public class TicketApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketApiApplication.class, args);
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }


    @Bean
    CommandLineRunner run(PerformanceService performanceService, AppUserService appUserService, AuditoriumService auditoriumService, ReservationService reservationService){
        return args -> {
            Performance performance1 = new Performance(null,"Performance 1",90,15.9);
            Performance performance2 = new Performance(null,"Performance 2",120,10.9);
            LocalDateTime aDateTime = LocalDateTime.of(2023, Month.JANUARY, 29, 19, 0, 0);
            LocalDateTime bDateTime = LocalDateTime.of(2023, Month.MARCH, 15, 19, 0, 0);
            LocalDateTime cDateTime = LocalDateTime.of(2023, Month.FEBRUARY, 27, 22, 0, 0);
            LocalDateTime dDateTime = LocalDateTime.of(2023, Month.MARCH, 3, 22, 0, 0);
            Auditorium a1 = new Auditorium(null,"A1",15,12);
            Auditorium a2 = new Auditorium(null,"A2",13,9);
            auditoriumService.saveAuditorium(a1);
            auditoriumService.saveAuditorium(a2);
            Show show1 = new Show(null,performance1,aDateTime,a1);
            Show show2 = new Show(null,performance1,bDateTime,a1);
            Show show3 = new Show(null,performance2,cDateTime,a1);
            Show show4 = new Show(null,performance2,dDateTime,a2);
            performance1.getShows().add(show1);
            performance1.getShows().add(show2);
            performance2.getShows().add(show3);
            performance2.getShows().add(show4);
            Performance performance3 = new Performance(null,"Performance 3",60,9.9);
            performanceService.savePerformance(performance3);
            //performance1 = performanceService.savePerformance(performance1);
            AppUser appUser = new AppUser(null,"admin@admin.com","12345","Admin","Admin",25,"LOCAL",List.of("USER","ADMIN","SYSTEM_ADMIN"),performance1);
            performance1.setUser(appUser);
            appUserService.saveUser(appUser);
            AppUser appUser1 = new AppUser(null,"user1@test.com","12345","User","1",20,"LOCAL",List.of("USER"),null);
            AppUser appUser2 = new AppUser(null,"user2@test.com","12345","User","2",20,"LOCAL",List.of("USER","ADMIN"),performance2);
            performance2.setUser(appUser2);
            appUserService.saveUser(appUser1);
            appUserService.saveUser(appUser2);
            //performanceService.savePerformance(performance1);
            //appUserService.saveUser(appUser);
        };
    }


}
