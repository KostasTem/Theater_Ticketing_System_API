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
    /** */
    @ManyToOne
    @JoinColumn(name = "performance_id", referencedColumnName = "id")
    @JsonIncludeProperties(value = {"id","name"})
    private Performance performance;
    private LocalDateTime dateTime;
    @OneToMany(fetch = FetchType.LAZY,mappedBy = "show",cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Ticket> tickets;

    public Show(Long id,Performance performance, LocalDateTime dateTime) {
        this.id = id;
        this.performance = performance;
        this.dateTime = dateTime;
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

    public void setTickets(ArrayList<Ticket> tickets) {
        this.tickets = tickets;
    }

    public Performance getPerformance() {
        return performance;
    }

    public void setPerformance(Performance performance) {
        this.performance = performance;
    }

    public List<Ticket> generateTickets(){
        List<Ticket> tickets = new ArrayList<>();
        for(char alphabet = 'A'; alphabet <='L'; alphabet++ )
        {
            for(int i=1;i<13;i++) {
                String seat = alphabet + String.valueOf(i);
                tickets.add(new Ticket(null, seat, this, false));
            }
        }
        return tickets;
    }
}
