package com.example.mainapp.model;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalTime;
import java.util.Objects;

public class TimeSlot implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private LocalTime arrivee;
    private LocalTime depart;

    public TimeSlot() {
        this.arrivee = LocalTime.of(8, 0);
        this.depart = LocalTime.of(17, 0);
    }

    public TimeSlot(LocalTime arrivee, LocalTime depart) {
        if (depart.isBefore(arrivee) || depart.equals(arrivee)) {
            throw new IllegalArgumentException("L'heure de départ doit être après l'heure d'arrivée");
        }
        this.arrivee = arrivee;
        this.depart = depart;
    }

    public LocalTime getArrivee() {
        return arrivee;
    }

    public void setArrivee(LocalTime arrivee) {
        if (this.depart != null && this.depart.isBefore(arrivee)) {
            throw new IllegalArgumentException("L'heure d'arrivée doit être avant l'heure de départ");
        }
        this.arrivee = arrivee;
    }

    public LocalTime getDepart() {
        return depart;
    }

    public void setDepart(LocalTime depart) {
        if (this.arrivee != null && depart.isBefore(this.arrivee)) {
            throw new IllegalArgumentException("L'heure de départ doit être après l'heure d'arrivée");
        }
        this.depart = depart;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeSlot timeSlot = (TimeSlot) o;
        return Objects.equals(arrivee, timeSlot.arrivee) && Objects.equals(depart, timeSlot.depart);
    }

    @Override
    public int hashCode() {
        return Objects.hash(arrivee, depart);
    }

    @Override
    public String toString() {
        return arrivee + " - " + depart;
    }
}