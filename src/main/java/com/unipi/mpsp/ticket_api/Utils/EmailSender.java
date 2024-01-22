package com.unipi.mpsp.ticket_api.Utils;
import com.unipi.mpsp.ticket_api.DataClasses.Show;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class EmailSender implements Runnable{
    private final JavaMailSender javaMailSender;
    private final String email;
    private final Boolean canceled;

    private final Show updatedShow;

    public EmailSender(JavaMailSender javaMailSender, String email, Boolean canceled,Show updatedShow) {
        this.javaMailSender = javaMailSender;
        this.email = email;
        this.canceled = canceled;
        this.updatedShow = updatedShow;
    }

    @Override
    public void run() {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(email);
        String text = "";
        if(canceled!=null) {
            if (this.canceled) {
                msg.setSubject("Reservation Canceled");
                text = "Your reservation for " + updatedShow.getPerformance().getName() + " has been canceled due to a change in the capacity of the auditorium or because the show has been canceled.";
            } else {
                msg.setSubject("Reservation Changed");
                text = "Your reservation for " + updatedShow.getPerformance().getName() + " has been changed. The show will now be performed at " + updatedShow.getDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + " in auditorium " + updatedShow.getAuditorium().getName() + ".";
            }
        }
        else{
            msg.setSubject("Theater Invitation");
            text = "You have been invited to view the performance "+updatedShow.getPerformance().getName()+ " at " + updatedShow.getDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + " in auditorium " + updatedShow.getAuditorium().getName();
        }
        msg.setText(text);
        javaMailSender.send(msg);
    }
}
