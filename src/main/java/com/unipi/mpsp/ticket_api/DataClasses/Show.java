package com.unipi.mpsp.ticket_api.DataClasses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Show {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "performance_id", referencedColumnName = "id")
    @JsonIncludeProperties(value = {"id","name"})
    private Performance performance;
    private LocalDateTime dateTime;
    @OneToMany(fetch = FetchType.EAGER,mappedBy = "show",cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Ticket> tickets;
    @ManyToOne
    @JoinColumn(name = "auditorium_id", referencedColumnName = "id")
    private Auditorium auditorium;

    public Show(Long id,Performance performance, LocalDateTime dateTime, Auditorium auditorium) {
        this.id = id;
        this.performance = performance;
        this.dateTime = dateTime;
        this.auditorium = auditorium;
        this.tickets = generateTickets();
    }

    public Show() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public List<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets;
    }

    public Performance getPerformance() {
        return performance;
    }

    public void setPerformance(Performance performance) {
        this.performance = performance;
    }

    public Auditorium getAuditorium() {
        return auditorium;
    }

    public void setAuditorium(Auditorium auditorium) {
        this.auditorium = auditorium;
    }

    public List<Ticket> generateTickets(){
        List<Ticket> tickets = new ArrayList<>();
        if(auditorium!=null) {
            int counter = 0;
            for (char alphabet = 'A'; alphabet <= 'Z'; alphabet++) {
                for (int i = 1; i < auditorium.getSeatsPerRow() + 1; i++) {
                    String seat = alphabet + String.valueOf(i);
                    tickets.add(new Ticket(null, seat, this, false));
                }
                counter += 1;
                if (counter >= auditorium.getRows()) {
                    break;
                }
            }
        }
        return tickets;
    }
}
