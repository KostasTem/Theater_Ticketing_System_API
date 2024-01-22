package com.unipi.mpsp.ticket_api.Utils;

import com.unipi.mpsp.ticket_api.DataClasses.Performance;

public class NewPerformanceRequest {
    private Performance performance;
    private String email;

    public NewPerformanceRequest() {
    }

    public NewPerformanceRequest(Performance performance, String email) {
        this.performance = performance;
        this.email = email;
    }

    public Performance getPerformance() {
        return performance;
    }

    public void setPerformance(Performance performance) {
        this.performance = performance;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
