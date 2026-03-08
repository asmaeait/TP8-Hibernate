package com.example.util;

import com.example.model.Salle;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class PerformanceReport {

    private final EntityManagerFactory emf;
    private final Map<String, ResultatTest> resultats = new LinkedHashMap<>();

    public PerformanceReport(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public void runPerformanceTests() {
        System.out.println("Lancement des tests de performance...");

        reinitialiserStatistiques();

        // Test 1 : Disponibilité des salles
        testerPerformance("Recherche de salles disponibles", () -> {
            EntityManager em = emf.createEntityManager();
            try {
                LocalDateTime debut = LocalDateTime.now().plusDays(1);
                LocalDateTime fin = debut.plusHours(2);
                return em.createQuery(
                                "SELECT DISTINCT s FROM Salle s WHERE s.id NOT IN " +
                                        "(SELECT r.salle.id FROM Reservation r " +
                                        "WHERE r.dateDebut < :fin AND r.dateFin > :debut)")
                        .setParameter("debut", debut)
                        .setParameter("fin", fin)
                        .getResultList();
            } finally {
                em.close();
            }
        });

        // Test 2 : Recherche multi-critères
        testerPerformance("Recherche multi-critères", () -> {
            EntityManager em = emf.createEntityManager();
            try {
                return em.createQuery(
                                "SELECT DISTINCT s FROM Salle s JOIN s.equipements e " +
                                        "WHERE s.capacite >= :capacite AND s.batiment = :batiment AND e.id = :eid")
                        .setParameter("capacite", 25)
                        .setParameter("batiment", "Bloc B")
                        .setParameter("eid", 1L)
                        .getResultList();
            } finally {
                em.close();
            }
        });

        // Test 3 : Pagination
        testerPerformance("Pagination", () -> {
            EntityManager em = emf.createEntityManager();
            try {
                return em.createQuery("SELECT s FROM Salle s ORDER BY s.id")
                        .setFirstResult(0)
                        .setMaxResults(10)
                        .getResultList();
            } finally {
                em.close();
            }
        });

        // Test 4 : Accès répété avec cache
        testerPerformance("Accès répété avec cache", () -> {
            Object dernierResultat = null;
            for (int i = 0; i < 100; i++) {
                EntityManager em = emf.createEntityManager();
                try {
                    dernierResultat = em.find(Salle.class, 1L);
                } finally {
                    em.close();
                }
            }
            return dernierResultat;
        });

        // Test 5 : JOIN FETCH
        testerPerformance("Requête avec JOIN FETCH", () -> {
            EntityManager em = emf.createEntityManager();
            try {
                return em.createQuery(
                                "SELECT DISTINCT s FROM Salle s LEFT JOIN FETCH s.equipements " +
                                        "WHERE s.capacite > 20")
                        .getResultList();
            } finally {
                em.close();
            }
        });

        genererRapport();
    }

    private void testerPerformance(String nomTest, Supplier<?> fonction) {
        System.out.println("Test en cours: " + nomTest);
        reinitialiserStatistiques();

        long debut = System.currentTimeMillis();
        Object resultat = fonction.get();
        long duree = System.currentTimeMillis() - debut;

        Session session = emf.createEntityManager().unwrap(Session.class);
        Statistics stats = session.getSessionFactory().getStatistics();

        ResultatTest res = new ResultatTest();
        res.duree = duree;
        res.nbRequetes = stats.getQueryExecutionCount();
        res.nbEntites = stats.getEntityLoadCount();
        res.cacheHits = stats.getSecondLevelCacheHitCount();
        res.cacheMiss = stats.getSecondLevelCacheMissCount();
        res.tailleResultat = (resultat instanceof java.util.Collection)
                ? ((java.util.Collection<?>) resultat).size()
                : (resultat != null ? 1 : 0);

        resultats.put(nomTest, res);
        System.out.println("Terminé en " + duree + "ms");
    }

    private void reinitialiserStatistiques() {
        Session session = emf.createEntityManager().unwrap(Session.class);
        session.getSessionFactory().getStatistics().clear();
    }

    private void genererRapport() {
        System.out.println("Génération du rapport...");

        String nomFichier = "rapport_performance_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt";

        try (PrintWriter writer = new PrintWriter(new FileWriter(nomFichier))) {
            writer.println("=== RAPPORT DE PERFORMANCE ===");
            writer.println("Date: " + LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            writer.println("=================================\n");

            for (Map.Entry<String, ResultatTest> entree : resultats.entrySet()) {
                ResultatTest r = entree.getValue();
                writer.println("Test: " + entree.getKey());
                writer.println("Temps d'exécution: " + r.duree + "ms");
                writer.println("Nombre de requêtes: " + r.nbRequetes);
                writer.println("Entités chargées: " + r.nbEntites);
                writer.println("Hits du cache: " + r.cacheHits);
                writer.println("Miss du cache: " + r.cacheMiss);
                writer.println("Taille du résultat: " + r.tailleResultat);

                long totalCache = r.cacheHits + r.cacheMiss;
                double ratio = totalCache > 0 ? (double) r.cacheHits / totalCache : 0;
                writer.println("Ratio de hit du cache: " + String.format("%.2f", ratio * 100) + "%");
                writer.println("----------------------------------\n");
            }

            writer.println("\n=== RECOMMANDATIONS ===");

            boolean optimiserCache = resultats.values().stream()
                    .anyMatch(r -> r.cacheHits < r.cacheMiss && r.nbRequetes > 5);

            boolean optimiserRequetes = resultats.values().stream()
                    .anyMatch(r -> r.nbRequetes > 10 || r.duree > 500);

            if (optimiserCache) {
                writer.println("1. Optimisation du cache recommandée:");
                writer.println("   - Vérifier la configuration EhCache");
                writer.println("   - Augmenter le TTL pour les entités peu modifiées");
                writer.println("   - Activer le cache de requête pour les JPQL répétitifs");
            }

            if (optimiserRequetes) {
                writer.println("2. Optimisation des requêtes recommandée:");
                writer.println("   - Utiliser JOIN FETCH pour réduire le problème N+1");
                writer.println("   - Ajouter des index sur les colonnes fréquemment filtrées");
                writer.println("   - Revoir les requêtes dépassant 500ms");
            }

            writer.println("\n3. Recommandations générales:");
            writer.println("   - Monitorer les performances avec JProfiler ou VisualVM");
            writer.println("   - Mettre en place des alertes de performance en production");
            writer.println("   - Utiliser un pool de connexions HikariCP en production");

            System.out.println("Rapport généré : " + nomFichier);

        } catch (IOException e) {
            System.err.println("Erreur lors de la génération du rapport: " + e.getMessage());
        }
    }

    private static class ResultatTest {
        long duree;
        long nbRequetes;
        long nbEntites;
        long cacheHits;
        long cacheMiss;
        int tailleResultat;
    }
}