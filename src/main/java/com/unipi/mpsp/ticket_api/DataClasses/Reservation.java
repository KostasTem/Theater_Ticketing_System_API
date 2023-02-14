package com.unipi.mpsp.ticket_api.DataClasses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

@Entity
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "app_user_id", referencedColumnName = "id")
    @JsonIgnore
    private AppUser appUser;
    @OneToMany(fetch = FetchType.EAGER,mappedBy = "reservation",cascade = {CascadeType.PERSIST})
    @JsonIncludeProperties(value = {"seat"})
    private List<Ticket> tickets;
    private LocalDateTime timestamp;

    public Reservation(Long id, AppUser appUser, List<Ticket> tickets, LocalDateTime timestamp) {
        this.id = id;
        this.appUser = appUser;
        this.tickets = tickets;
        this.timestamp = timestamp;
    }

    public Reservation() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AppUser getAppUser() {
        return appUser;
    }

    public void setAppUser(AppUser appUser) {
        this.appUser = appUser;
    }

    public List<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
