package com.example.repository;

import com.example.model.Reservation;
import com.example.model.StatutReservation;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

public class ReservationRepositoryImpl implements ReservationRepository {

    private final EntityManager em;

    public ReservationRepositoryImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public Reservation findById(Long id) {
        return em.find(Reservation.class, id);
    }

    @Override
    public List<Reservation> findAll() {
        return em.createQuery(
                        "SELECT r FROM Reservation r ORDER BY r.dateDebut", Reservation.class)
                .getResultList();
    }

    @Override
    public List<Reservation> findByStatut(StatutReservation statut) {
        return em.createQuery(
                        "SELECT r FROM Reservation r WHERE r.statut = :statut ORDER BY r.dateDebut",
                        Reservation.class)
                .setParameter("statut", statut)
                .getResultList();
    }

    @Override
    public List<Reservation> findByPeriode(LocalDateTime debut, LocalDateTime fin) {
        return em.createQuery(
                        "SELECT r FROM Reservation r WHERE r.dateDebut >= :debut AND r.dateFin <= :fin",
                        Reservation.class)
                .setParameter("debut", debut)
                .setParameter("fin", fin)
                .getResultList();
    }

    @Override
    public void save(Reservation reservation) {
        em.persist(reservation);
    }

    @Override
    public void update(Reservation reservation) {
        em.merge(reservation);
    }

    @Override
    public void delete(Long id) {
        Reservation reservation = findById(id);
        if (reservation != null) {
            em.remove(reservation);
        }
    }
}