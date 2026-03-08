package com.example.service;

import com.example.model.Salle;
import com.example.repository.SalleRepository;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class SalleServiceImpl implements SalleService {

    private final EntityManager em;
    private final SalleRepository salleRepository;

    public SalleServiceImpl(EntityManager em, SalleRepository salleRepository) {
        this.em = em;
        this.salleRepository = salleRepository;
    }

    @Override
    public List<Salle> findAvailableRooms(LocalDateTime debut, LocalDateTime fin) {
        return salleRepository.findAvailableRooms(debut, fin);
    }

    @Override
    public List<Salle> searchRooms(Map<String, Object> criteres) {
        return salleRepository.searchRooms(criteres);
    }

    @Override
    public List<Salle> getPaginatedRooms(int page, int pageSize) {
        return salleRepository.getPaginatedRooms(page, pageSize);
    }

    @Override
    public long countRooms() {
        return salleRepository.countRooms();
    }

    @Override
    public int getTotalPages(int pageSize) {
        long total = salleRepository.countRooms();
        return (int) Math.ceil((double) total / pageSize);
    }
}