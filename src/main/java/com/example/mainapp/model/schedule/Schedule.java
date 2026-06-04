package com.example.mainapp.model.schedule;

import java.io.Serial;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public class Schedule implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private EnumMap<DayOfWeek, TimeSlot> horaires;

    public Schedule() {
        this.horaires = new EnumMap<>(DayOfWeek.class);
        initializeDefault();
    }

    private void initializeDefault() {
        TimeSlot defaultSlot = new TimeSlot();
        for (DayOfWeek day : DayOfWeek.values()) {
            horaires.put(day, new TimeSlot(defaultSlot.getArrivee(), defaultSlot.getDepart()));
        }
    }

    public void definirJournee(DayOfWeek jour, TimeSlot timeSlot) {
        if (jour == null || timeSlot == null) {
            throw new IllegalArgumentException("Le jour et la plage horaire ne peuvent pas être null");
        }
        horaires.put(jour, timeSlot);
    }

    public TimeSlot getHorairePourJour(DayOfWeek jour) {
        if (jour == null) {
            throw new IllegalArgumentException("Le jour ne peut pas être null");
        }
        return horaires.get(jour);
    }

    public EnumMap<DayOfWeek, TimeSlot> getHoraires() {
        return horaires;
    }

    public void setHoraires(EnumMap<DayOfWeek, TimeSlot> horaires) {
        if (horaires == null) {
            throw new IllegalArgumentException("Les horaires ne peuvent pas être null");
        }
        this.horaires = horaires;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Schedule schedule = (Schedule) o;
        return Objects.equals(horaires, schedule.horaires);
    }

    @Override
    public int hashCode() {
        return Objects.hash(horaires);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Planning:\n");
        for (Map.Entry<DayOfWeek, TimeSlot> entry : horaires.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }

    public long getMinutesPourCeJour(DayOfWeek dayOfWeek) {
        if (dayOfWeek == null) {
            throw new IllegalArgumentException("Le jour ne peut pas être null");
        }
        TimeSlot timeSlot = horaires.get(dayOfWeek);
        if (timeSlot == null) {
            return 0;
        }
        return ChronoUnit.MINUTES.between(timeSlot.getArrivee(), timeSlot.getDepart());
    }
}