package com.unipi.mpsp.ticket_api.DataClasses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import jakarta.persistence.*;

@Entity
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String seat;
    @ManyToOne
    @JoinColumn(name = "show_id", referencedColumnName = "id")
    @JsonIgnore
    private Show show;
    @ManyToOne
    @JoinColumn(name = "reservation_id", referencedColumnName = "id")
    @JsonIncludeProperties(value = {"id"})
    private Reservation reservation;
    private Boolean checkedIn;

    public Ticket(Long id, String seat, Show show, Boolean checkedIn) {
        this.id = id;
        this.seat = seat;
        this.show = show;
        this.reservation = null;
        this.checkedIn = checkedIn;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSeat() {
        return seat;
    }

    public void setSeat(String seat) {
        this.seat = seat;
    }

    public Show getShow() {
        return show;
    }

    public void setShow(Show show) {
        this.show = show;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public Boolean getCheckedIn() {
        return checkedIn;
    }

    public void setCheckedIn(Boolean checkedIn) {
        this.checkedIn = checkedIn;
    }

    public Ticket() {
    }

    @Override
    public String toString() {
        return "Ticket{" +
                "id=" + id +
                ", seat='" + seat + '\'' +
                ", show=" + show.getId() +
                ", reservation=" + reservation.getId() +
                ", checkedIn=" + checkedIn +
                '}';
    }
}
