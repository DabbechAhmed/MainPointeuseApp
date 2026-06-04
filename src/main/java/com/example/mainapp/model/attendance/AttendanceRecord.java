package com.example.mainapp.model.attendance;

import com.example.mainapp.model.employee.Employee;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public class AttendanceRecord implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private UUID id;

    private Employee employee;

    private LocalDateTime time;
    private boolean isCheckIn;
    private String status;

    public AttendanceRecord(Employee employee, LocalDateTime time, boolean isCheckIn) {
        this.id = UUID.randomUUID();
        this.employee = employee;
        this.time = time;
        this.isCheckIn = isCheckIn;
        this.status = "Normal";
    }

    public UUID getId() { return id; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public LocalDateTime getTime() { return time; }
    public void setTime(LocalDateTime time) { this.time = time; }

    public boolean isCheckIn() { return isCheckIn; }
    public void setCheckIn(boolean checkIn) { isCheckIn = checkIn; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}