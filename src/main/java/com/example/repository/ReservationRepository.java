package com.example.repository;

import com.example.model.Reservation;
import com.example.model.StatutReservation;
import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository {
    Reservation findById(Long id);
    List<Reservation> findAll();
    List<Reservation> findByStatut(StatutReservation statut);
    List<Reservation> findByPeriode(LocalDateTime debut, LocalDateTime fin);
    void save(Reservation reservation);
    void update(Reservation reservation);
    void delete(Long id);
}