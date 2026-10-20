package com.showtime.demo;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "ushers")
public class Usher extends Staff {

    private String screenAssigned;

    public String getScreenAssigned() { return screenAssigned; }
    public void setScreenAssigned(String screenAssigned) { this.screenAssigned = screenAssigned; }
}
