package com.unipi.mpsp.ticket_api.DataClasses;

import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.unipi.mpsp.ticket_api.Utils.StringListConverter;
import jakarta.persistence.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Entity
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(unique = true,nullable = false)
    private String email;
    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    private String firstName;
    private String lastName;
    @Column(length = 10000024)
    private String image;
    private Integer age;
    private String provider;
    @Convert(converter = StringListConverter.class)
    @Column(columnDefinition="TEXT",nullable = false)
    private List<String> roles;
    @OneToMany(fetch = FetchType.EAGER,mappedBy = "appUser",cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIncludeProperties(value = {"id"})
    private List<Reservation> reservations;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "performance_id", referencedColumnName = "id")
    @JsonIncludeProperties(value = {"id","name","duration","image"})
    private Performance performance;

    public AppUser(Long id, String email, String password, String firstName, String lastName, Integer age, String provider,List<String> roles, Performance performance) {
        this.id = id;
        this.password = password;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.provider = provider;
        this.roles = roles;
        this.reservations = new ArrayList<>();
        this.performance = performance;
    }

    public AppUser() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public List<Reservation> getReservations() {
        return reservations;
    }

    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations;
    }

    public Performance getPerformance() {
        return performance;
    }

    public void setPerformance(Performance performance) {
        this.performance = performance;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) throws IOException {
        this.image = image;
    }
}
