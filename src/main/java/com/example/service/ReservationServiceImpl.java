package com.example.service;

import com.example.model.Reservation;
import com.example.model.StatutReservation;
import com.example.repository.ReservationRepository;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

public class ReservationServiceImpl implements ReservationService {

    private final EntityManager em;
    private final ReservationRepository reservationRepository;

    public ReservationServiceImpl(EntityManager em, ReservationRepository reservationRepository) {
        this.em = em;
        this.reservationRepository = reservationRepository;
    }

    @Override
    public List<Reservation> findByStatut(StatutReservation statut) {
        return reservationRepository.findByStatut(statut);
    }

    @Override
    public List<Reservation> findByPeriode(LocalDateTime debut, LocalDateTime fin) {
        return reservationRepository.findByPeriode(debut, fin);
    }

    @Override
    public void annuler(Long id) {
        Reservation reservation = reservationRepository.findById(id);
        if (reservation != null) {
            reservation.setStatut(StatutReservation.ANNULEE);
            reservationRepository.update(reservation);
        }
    }
}