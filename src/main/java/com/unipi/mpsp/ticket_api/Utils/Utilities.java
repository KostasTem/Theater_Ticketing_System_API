package com.unipi.mpsp.ticket_api.Utils;

import java.util.ArrayList;
import java.util.List;

public class Utilities {
    public static List<String> calculateNewSeats(Integer rows, Integer seatsPerRow){
        List<String> seats = new ArrayList<>();
        int counter = 0;
        for (char alphabet = 'A'; alphabet <= 'Z'; alphabet++) {
            for (int i = 1; i < seatsPerRow + 1; i++) {
                String seat = alphabet + String.valueOf(i);
                seats.add(seat);
            }
            counter += 1;
            if (counter >= rows) {
                break;
            }
        }
        return seats;
    }
}
