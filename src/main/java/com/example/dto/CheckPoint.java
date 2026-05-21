package com.example.dto;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public class CheckPoint implements Serializable {
    private static final long serialVersionUID = 1L;

    private final UUID employeeId;
    private final LocalDateTime time;
    private final boolean isCheckIn;

    public CheckPoint(UUID employeeId, LocalDateTime time, boolean isCheckIn) {
        this.employeeId = employeeId;
        this.time = time;
        this.isCheckIn = isCheckIn;
    }

    public UUID getEmployeeId() { return employeeId; }
    public LocalDateTime getTime() { return time; }
    public boolean isCheckIn() { return isCheckIn; }
}