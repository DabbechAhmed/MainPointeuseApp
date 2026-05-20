package com.example.dto;

import java.io.Serializable;
import java.util.UUID;

public class EmployeeDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private final UUID id;
    private final String fullName;

    public EmployeeDTO(UUID id, String fullName) {
        this.id = id;
        this.fullName = fullName;
    }

    public UUID getId() { return id; }

    @Override
    public String toString() {
        return fullName;
    }
}