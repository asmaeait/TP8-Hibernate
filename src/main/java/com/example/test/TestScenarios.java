package com.example.test;

import com.example.model.*;
import com.example.service.ReservationService;
import com.example.service.SalleService;
import com.example.util.PaginationResult;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.OptimisticLockException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TestScenarios {

    private final EntityManagerFactory emf;
    private final SalleService salleService;
    private final ReservationService reservationService;

    public TestScenarios(EntityManagerFactory emf, SalleService salleService,
                         ReservationService reservationService) {
        this.emf = emf;
        this.salleService = salleService;
        this.reservationService = reservationService;
    }

    public void runAllTests() {
        System.out.println("\n=== EXÉCUTION DES SCÉNARIOS DE TEST ===\n");

        testerDisponibilite();
        testerRechercheMultiCriteres();
        testerPagination();
        testerOptimisticLocking();
        testerPerformanceCache();

        System.out.println("\n=== TOUS LES TESTS TERMINÉS ===\n");
    }

    // Test 1 : Vérification de la disponibilité des salles
    private void testerDisponibilite() {
        System.out.println("\n=== TEST 1: RECHERCHE DE DISPONIBILITÉ ===");

        LocalDateTime demainMatin = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0);
        LocalDateTime demainMidi = demainMatin.plusHours(3);

        System.out.println("Recherche entre " + demainMatin + " et " + demainMidi);
        List<Salle> sallesDisponibles = salleService.findAvailableRooms(demainMatin, demainMidi);

        System.out.println("Nombre de salles disponibles: " + sallesDisponibles.size());
        for (int i = 0; i < Math.min(5, sallesDisponibles.size()); i++) {
            Salle s = sallesDisponibles.get(i);
            System.out.println("- " + s.getNom() + " | Capacité: " + s.getCapacite()
                    + " | " + s.getBatiment());
        }
        if (sallesDisponibles.size() > 5) {
            System.out.println("... et " + (sallesDisponibles.size() - 5) + " autres salles.");
        }

        // Vérification sur un créneau déjà réservé
        EntityManager em = emf.createEntityManager();
        try {
            Reservation resa = em.createQuery(
                            "SELECT r FROM Reservation r WHERE r.statut = :statut",
                            Reservation.class)
                    .setParameter("statut", StatutReservation.CONFIRMEE)
                    .setMaxResults(1)
                    .getSingleResult();

            System.out.println("\nCréneau déjà réservé: " + resa.getDateDebut()
                    + " → " + resa.getDateFin());
            System.out.println("Salle occupée: " + resa.getSalle().getNom());

            List<Salle> disponibles = salleService.findAvailableRooms(
                    resa.getDateDebut(), resa.getDateFin());

            System.out.println("Salles disponibles sur ce créneau: " + disponibles.size());
            System.out.println("La salle occupée est bien exclue: "
                    + !disponibles.contains(resa.getSalle()));

        } finally {
            em.close();
        }
    }

    // Test 2 : Recherche multi-critères
    private void testerRechercheMultiCriteres() {
        System.out.println("\n=== TEST 2: RECHERCHE MULTI-CRITÈRES ===");

        // Capacité >= 30 + équipement ID 2 (Tableau numérique interactif)
        Map<String, Object> criteres1 = new HashMap<>();
        criteres1.put("capaciteMin", 30);
        criteres1.put("equipement", 2L);

        System.out.println("Salles avec capacité >= 30 et tableau numérique interactif:");
        List<Salle> res1 = salleService.searchRooms(criteres1);
        System.out.println("Résultats: " + res1.size() + " salle(s)");
        res1.forEach(s -> System.out.println("- " + s.getNom()
                + " | Capacité: " + s.getCapacite()
                + " | Équipements: " + s.getEquipements().size()));

        // Bloc C, étage 2
        Map<String, Object> criteres2 = new HashMap<>();
        criteres2.put("batiment", "Bloc C");
        criteres2.put("etage", 2);

        System.out.println("\nSalles dans le Bloc C à l'étage 2:");
        List<Salle> res2 = salleService.searchRooms(criteres2);
        System.out.println("Résultats: " + res2.size() + " salle(s)");
        res2.forEach(s -> System.out.println("- " + s.getNom()
                + " | Étage: " + s.getEtage()));

        // Recherche complexe
        Map<String, Object> criteres3 = new HashMap<>();
        criteres3.put("capaciteMin", 20);
        criteres3.put("capaciteMax", 50);
        criteres3.put("batiment", "Bloc B");
        criteres3.put("equipement", 7L); // Poste informatique

        System.out.println("\nRecherche complexe: capacité 20-50, Bloc B, avec poste informatique:");
        List<Salle> res3 = salleService.searchRooms(criteres3);
        System.out.println("Résultats: " + res3.size() + " salle(s)");
        res3.forEach(s -> System.out.println("- " + s.getNom()
                + " | Capacité: " + s.getCapacite()
                + " | " + s.getBatiment()));
    }

    // Test 3 : Pagination des salles
    private void testerPagination() {
        System.out.println("\n=== TEST 3: PAGINATION ===");

        int taillePage = 5;
        System.out.println("Affichage des salles par pages de " + taillePage + ":");

        int totalPages = salleService.getTotalPages(taillePage);
        System.out.println("Nombre total de pages: " + totalPages);

        for (int page = 1; page <= totalPages; page++) {
            System.out.println("\n--- Page " + page + " ---");
            List<Salle> sallesPage = salleService.getPaginatedRooms(page, taillePage);
            sallesPage.forEach(s -> System.out.println("  * " + s.getNom()
                    + " | Capacité: " + s.getCapacite()
                    + " | " + s.getBatiment()));
        }

        // Test avec PaginationResult
        System.out.println("\nInformations de pagination:");
        long totalElements = salleService.countRooms();
        List<Salle> premierePage = salleService.getPaginatedRooms(1, taillePage);

        PaginationResult<Salle> pagination = new PaginationResult<>(
                premierePage, 1, taillePage, totalElements);

        System.out.println("Page courante: " + pagination.getCurrentPage());
        System.out.println("Taille de page: " + pagination.getPageSize());
        System.out.println("Total de pages: " + pagination.getTotalPages());
        System.out.println("Total d'éléments: " + pagination.getTotalItems());
        System.out.println("Page suivante dispo: " + pagination.hasNext());
        System.out.println("Page précédente dispo: " + pagination.hasPrevious());
    }

    // Test 4 : Optimistic locking - conflit de mise à jour simultanée
    private void testerOptimisticLocking() {
        System.out.println("\n=== TEST 4: OPTIMISTIC LOCKING ===");

        EntityManager em = emf.createEntityManager();
        Reservation reservation = null;

        try {
            reservation = em.createQuery(
                            "SELECT r FROM Reservation r WHERE r.statut = :statut",
                            Reservation.class)
                    .setParameter("statut", StatutReservation.CONFIRMEE)
                    .setMaxResults(1)
                    .getSingleResult();

            System.out.println("Réservation cible: ID=" + reservation.getId()
                    + " | Salle=" + reservation.getSalle().getNom()
                    + " | Date=" + reservation.getDateDebut());
        } finally {
            em.close();
        }

        if (reservation == null) {
            System.out.println("Aucune réservation disponible pour ce test.");
            return;
        }

        final Long resaId = reservation.getId();
        CountDownLatch latch = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // Thread 1 : modification du motif (lent)
        executor.submit(() -> {
            try {
                latch.await();
                EntityManager em1 = emf.createEntityManager();
                try {
                    em1.getTransaction().begin();
                    Reservation r1 = em1.find(Reservation.class, resaId);
                    System.out.println("Thread 1 - Version lue: " + r1.getVersion());
                    Thread.sleep(1000); // Simule un traitement long
                    r1.setMotif("Mise à jour du motif par Thread 1");
                    em1.merge(r1);
                    em1.getTransaction().commit();
                    System.out.println("Thread 1 - Mise à jour réussie !");
                } catch (OptimisticLockException e) {
                    System.out.println("Thread 1 - Conflit détecté, rollback effectué.");
                    if (em1.getTransaction().isActive()) em1.getTransaction().rollback();
                } finally {
                    em1.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Thread 2 : modification de la date de fin (rapide)
        executor.submit(() -> {
            try {
                latch.await();
                Thread.sleep(100);
                EntityManager em2 = emf.createEntityManager();
                try {
                    em2.getTransaction().begin();
                    Reservation r2 = em2.find(Reservation.class, resaId);
                    System.out.println("Thread 2 - Version lue: " + r2.getVersion());
                    r2.setDateFin(r2.getDateFin().plusHours(1));
                    em2.merge(r2);
                    em2.getTransaction().commit();
                    System.out.println("Thread 2 - Mise à jour réussie !");
                } catch (OptimisticLockException e) {
                    System.out.println("Thread 2 - Conflit détecté, rollback effectué.");
                    if (em2.getTransaction().isActive()) em2.getTransaction().rollback();
                } finally {
                    em2.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        latch.countDown(); // Lancer les deux threads simultanément

        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Vérification de l'état final
        EntityManager emFinal = emf.createEntityManager();
        try {
            Reservation finale = emFinal.find(Reservation.class, resaId);
            System.out.println("\nÉtat final:");
            System.out.println("Motif: " + finale.getMotif());
            System.out.println("Date fin: " + finale.getDateFin());
            System.out.println("Version: " + finale.getVersion());
        } finally {
            emFinal.close();
        }
    }

    // Test 5 : Comparaison des performances avec et sans cache
    private void testerPerformanceCache() {
        System.out.println("\n=== TEST 5: PERFORMANCE DU CACHE ===");

        // Sans cache
        emf.getCache().evictAll();
        System.out.println("\nAccès sans cache (100 itérations):");
        long debut = System.currentTimeMillis();

        for (int i = 0; i < 100; i++) {
            EntityManager em = emf.createEntityManager();
            try {
                Salle salle = em.find(Salle.class, (i % 15) + 1L);
                salle.getEquipements().size();
            } finally {
                em.close();
            }
        }

        System.out.println("Temps sans cache: " + (System.currentTimeMillis() - debut) + "ms");

        // Avec cache
        System.out.println("\nAccès avec cache (100 itérations):");
        debut = System.currentTimeMillis();

        for (int i = 0; i < 100; i++) {
            EntityManager em = emf.createEntityManager();
            try {
                Salle salle = em.find(Salle.class, (i % 15) + 1L);
                salle.getEquipements().size();
            } finally {
                em.close();
            }
        }

        System.out.println("Temps avec cache: " + (System.currentTimeMillis() - debut) + "ms");

        // Requêtes avec et sans cache de requête
        emf.getCache().evictAll();
        System.out.println("\nRequêtes sans cache de requête (20 itérations):");
        debut = System.currentTimeMillis();

        for (int i = 0; i < 20; i++) {
            EntityManager em = emf.createEntityManager();
            try {
                em.createQuery(
                                "SELECT s FROM Salle s WHERE s.capacite >= :capacite", Salle.class)
                        .setParameter("capacite", 30)
                        .getResultList();
            } finally {
                em.close();
            }
        }

        System.out.println("Temps sans cache requête: " + (System.currentTimeMillis() - debut) + "ms");

        System.out.println("\nRequêtes avec cache de requête (20 itérations):");
        debut = System.currentTimeMillis();

        for (int i = 0; i < 20; i++) {
            EntityManager em = emf.createEntityManager();
            try {
                em.createQuery(
                                "SELECT s FROM Salle s WHERE s.capacite >= :capacite", Salle.class)
                        .setParameter("capacite", 30)
                        .setHint("org.hibernate.cacheable", "true")
                        .getResultList();
            } finally {
                em.close();
            }
        }

        System.out.println("Temps avec cache requête: " + (System.currentTimeMillis() - debut) + "ms");
    }
}
