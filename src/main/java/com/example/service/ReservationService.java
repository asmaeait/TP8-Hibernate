package com.example.service;

import com.example.model.Reservation;
import com.example.model.StatutReservation;
import java.time.LocalDateTime;
import java.util.List;

public interface ReservationService {
    List<Reservation> findByStatut(StatutReservation statut);
    List<Reservation> findByPeriode(LocalDateTime debut, LocalDateTime fin);
    void annuler(Long id);
}