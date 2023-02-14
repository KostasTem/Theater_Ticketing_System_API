package com.unipi.mpsp.ticket_api;

import com.unipi.mpsp.ticket_api.DataClasses.AppUser;
import com.unipi.mpsp.ticket_api.DataClasses.Performance;
import com.unipi.mpsp.ticket_api.DataClasses.Show;
import com.unipi.mpsp.ticket_api.Security.RsaKeyProperties;
import com.unipi.mpsp.ticket_api.Services.AppUserService;
import com.unipi.mpsp.ticket_api.Services.PerformanceService;
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
    CommandLineRunner run(PerformanceService performanceService,AppUserService appUserService){
        return args -> {
            Performance performance1 = new Performance(null,"Performance 2",90,15.9);
            Performance performance2 = new Performance(null,"Performance 1",120,10.9);
            LocalDateTime aDateTime = LocalDateTime.of(2023, Month.JANUARY, 29, 19, 0, 0);
            LocalDateTime bDateTime = LocalDateTime.of(2023, Month.FEBRUARY, 20, 19, 0, 0);
            LocalDateTime cDateTime = LocalDateTime.of(2023, Month.FEBRUARY, 27, 22, 0, 0);
            LocalDateTime dDateTime = LocalDateTime.of(2023, Month.FEBRUARY, 27, 17, 0, 0);
            Show show1 = new Show(null,performance1,aDateTime);
            Show show2 = new Show(null,performance1,bDateTime);
            Show show3 = new Show(null,performance2,cDateTime);
            Show show4 = new Show(null,performance2,dDateTime);
            performance1.getShows().add(show1);
            performance1.getShows().add(show2);
            performance2.getShows().add(show3);
            performance2.getShows().add(show4);
            performanceService.savePerformance(performance1);
            performanceService.savePerformance(performance2);
            //appUserService.saveUser(appUser);
        };
    }


}
