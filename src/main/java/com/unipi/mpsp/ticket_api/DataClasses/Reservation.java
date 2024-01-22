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
    @OneToMany(fetch = FetchType.EAGER,mappedBy = "reservation",cascade = {CascadeType.MERGE})
    @JsonIncludeProperties(value = {"seat","id","checkedIn"})
    private List<Ticket> tickets;
    private LocalDateTime timestamp;
    private Double price;

    public Reservation(Long id, AppUser appUser, List<Ticket> tickets, LocalDateTime timestamp) {
        this.id = id;
        this.appUser = appUser;
        this.tickets = tickets;
        this.timestamp = timestamp;
        this.price = calculatePrice();
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

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    private Double calculatePrice(){
        if(this.tickets.size()>0) {
            return this.tickets.get(0).getShow().getPerformance().getTicketPrice() * this.tickets.size();
        }
        return 0D;
    }
}
