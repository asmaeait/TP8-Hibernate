package com.example;

import com.example.repository.ReservationRepository;
import com.example.repository.ReservationRepositoryImpl;
import com.example.repository.SalleRepository;
import com.example.repository.SalleRepositoryImpl;
import com.example.service.ReservationService;
import com.example.service.ReservationServiceImpl;
import com.example.service.SalleService;
import com.example.service.SalleServiceImpl;
import com.example.test.TestScenarios;
import com.example.util.DataInitializer;
import com.example.util.PerformanceReport;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.Scanner;

public class App {

    public static void main(String[] args) {
        System.out.println("=== APPLICATION DE RÉSERVATION DE SALLES ===");

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("gestion-reservations");
        EntityManager em = emf.createEntityManager();

        try {
            SalleRepository salleRepository = new SalleRepositoryImpl(em);
            SalleService salleService = new SalleServiceImpl(em, salleRepository);

            ReservationRepository reservationRepository = new ReservationRepositoryImpl(em);
            ReservationService reservationService = new ReservationServiceImpl(em, reservationRepository);

            Scanner scanner = new Scanner(System.in);
            boolean quitter = false;

            while (!quitter) {
                System.out.println("\n=== MENU PRINCIPAL ===");
                System.out.println("1. Initialiser les données de test");
                System.out.println("2. Exécuter les scénarios de test");
                System.out.println("3. Simuler la migration de base de données");
                System.out.println("4. Générer un rapport de performance");
                System.out.println("5. Quitter");
                System.out.print("Votre choix: ");

                int choix = scanner.nextInt();
                scanner.nextLine();

                switch (choix) {
                    case 1:
                        DataInitializer init = new DataInitializer(emf);
                        init.initializeData();
                        break;

                    case 2:
                        TestScenarios tests = new TestScenarios(emf, salleService, reservationService);
                        tests.runAllTests();
                        break;

                    case 3:
                        System.out.println("Simulation de migration (base H2 en mémoire).");
                        System.out.println("En production, utilisez DatabaseMigrationTool avec MySQL/PostgreSQL.");
                        break;

                    case 4:
                        PerformanceReport rapport = new PerformanceReport(emf);
                        rapport.runPerformanceTests();
                        break;

                    case 5:
                        quitter = true;
                        System.out.println("Fermeture de l'application. À bientôt !");
                        break;

                    default:
                        System.out.println("Choix invalide, veuillez réessayer.");
                }
            }

        } finally {
            em.close();
            emf.close();
        }
    }
}
