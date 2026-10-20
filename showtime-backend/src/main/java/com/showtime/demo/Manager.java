package com.showtime.demo;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "managers")
public class Manager extends Staff {

    private String department;

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
}
