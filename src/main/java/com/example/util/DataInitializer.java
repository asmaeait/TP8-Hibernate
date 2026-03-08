package com.example.util;

import com.example.model.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import java.util.Random;

public class DataInitializer {

    private final EntityManagerFactory emf;
    private final Random random = new Random();

    public DataInitializer(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public void initializeData() {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            Equipement[] equipements = creerEquipements(em);
            Utilisateur[] utilisateurs = creerUtilisateurs(em);
            Salle[] salles = creerSalles(em, equipements);
            creerReservations(em, utilisateurs, salles);

            em.getTransaction().commit();
            System.out.println("Jeu de données initialisé avec succès !");

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    private Equipement[] creerEquipements(EntityManager em) {
        System.out.println("Création des équipements...");

        Equipement[] equipements = new Equipement[10];

        equipements[0] = new Equipement("Vidéoprojecteur 4K", "Vidéoprojecteur ultra haute définition");
        equipements[0].setReference("VPJ-4K-001");

        equipements[1] = new Equipement("Tableau numérique interactif", "Tableau tactile 70 pouces connecté");
        equipements[1].setReference("TNI-70-002");

        equipements[2] = new Equipement("Kit visioconférence Pro", "Caméra HD et micro intégrés");
        equipements[2].setReference("KVC-PRO-003");

        equipements[3] = new Equipement("Tableau blanc effaçable", "Tableau magnétique 180x90 cm");
        equipements[3].setReference("TBE-180-004");

        equipements[4] = new Equipement("Sono de salle", "Système audio stéréo 6 haut-parleurs");
        equipements[4].setReference("SON-6HP-005");

        equipements[5] = new Equipement("Micros HF sans fil", "Lot de 6 microphones haute fréquence");
        equipements[5].setReference("MHF-6-006");

        equipements[6] = new Equipement("Poste informatique", "PC bureautique Windows 11 Pro");
        equipements[6].setReference("PC-W11-007");

        equipements[7] = new Equipement("Réseau WiFi 6", "Borne Wi-Fi ultra rapide 2.4 Gbps");
        equipements[7].setReference("WF6-2G-008");

        equipements[8] = new Equipement("Climatisation centralisée", "Système CVC intelligent réglable");
        equipements[8].setReference("CVC-INT-009");

        equipements[9] = new Equipement("Multiprise murale", "Bandeau de 12 prises avec USB-C");
        equipements[9].setReference("MPR-12-010");

        for (Equipement e : equipements) {
            em.persist(e);
        }

        return equipements;
    }

    private Utilisateur[] creerUtilisateurs(EntityManager em) {
        System.out.println("Création des utilisateurs...");

        Utilisateur[] utilisateurs = new Utilisateur[20];

        // Noms et prénoms différents de l'énoncé
        String[] noms = {"Dupont", "Leclerc", "Fontaine", "Girard", "Bonnet",
                "Chevalier", "Rousseau", "Marchand", "Lemaire", "Renard",
                "Garnier", "Faure", "Morin", "Perrin", "Blanc",
                "Guerin", "Muller", "Briand", "Colin", "Barbe"};

        String[] prenoms = {"Alexandre", "Camille", "Théo", "Laura", "Maxime",
                "Lucie", "Hugo", "Emma", "Romain", "Léa",
                "Antoine", "Chloé", "Julien", "Sarah", "Florian",
                "Manon", "Baptiste", "Inès", "Quentin", "Pauline"};

        String[] departements = {"Ressources Humaines", "Informatique", "Finance",
                "Marketing", "Commercial", "Production",
                "Recherche et Développement", "Juridique",
                "Communication", "Direction"};

        for (int i = 0; i < 20; i++) {
            utilisateurs[i] = new Utilisateur(
                    noms[i], prenoms[i],
                    prenoms[i].toLowerCase() + "." + noms[i].toLowerCase() + "@entreprise.fr"
            );
            utilisateurs[i].setTelephone("07" + (10000000 + random.nextInt(90000000)));
            utilisateurs[i].setDepartement(departements[i % 10]);
            em.persist(utilisateurs[i]);
        }

        return utilisateurs;
    }

    private Salle[] creerSalles(EntityManager em, Equipement[] equipements) {
        System.out.println("Création des salles...");

        Salle[] salles = new Salle[15];

        // Bloc A - Petites salles de réunion
        for (int i = 0; i < 5; i++) {
            salles[i] = new Salle("Salle A" + (i + 1), 8 + i * 2);
            salles[i].setDescription("Petite salle de réunion");
            salles[i].setBatiment("Bloc A");
            salles[i].setEtage(i % 3 + 1);
            salles[i].setNumero("A0" + (i + 1));

            salles[i].addEquipement(equipements[3]); // Tableau blanc
            salles[i].addEquipement(equipements[7]); // WiFi 6
            salles[i].addEquipement(equipements[9]); // Multiprise

            if (i % 2 == 0) salles[i].addEquipement(equipements[0]); // Vidéoprojecteur
            if (i % 3 == 0) salles[i].addEquipement(equipements[4]); // Sono

            em.persist(salles[i]);
        }

        // Bloc B - Salles de formation
        for (int i = 5; i < 10; i++) {
            salles[i] = new Salle("Salle B" + (i - 4), 25 + (i - 5) * 5);
            salles[i].setDescription("Salle de formation équipée");
            salles[i].setBatiment("Bloc B");
            salles[i].setEtage(i % 4 + 1);
            salles[i].setNumero("B0" + (i - 4));

            salles[i].addEquipement(equipements[0]); // Vidéoprojecteur
            salles[i].addEquipement(equipements[3]); // Tableau blanc
            salles[i].addEquipement(equipements[6]); // Poste informatique
            salles[i].addEquipement(equipements[7]); // WiFi
            salles[i].addEquipement(equipements[9]); // Multiprise

            if (i % 2 == 0) salles[i].addEquipement(equipements[1]); // Tableau numérique

            em.persist(salles[i]);
        }

        // Bloc C - Grandes salles de conférence
        for (int i = 10; i < 15; i++) {
            salles[i] = new Salle("Salle C" + (i - 9), 60 + (i - 10) * 20);
            salles[i].setDescription("Grande salle de conférence premium");
            salles[i].setBatiment("Bloc C");
            salles[i].setEtage(i % 3 + 1);
            salles[i].setNumero("C0" + (i - 9));

            salles[i].addEquipement(equipements[0]); // Vidéoprojecteur
            salles[i].addEquipement(equipements[2]); // Kit visio
            salles[i].addEquipement(equipements[4]); // Sono
            salles[i].addEquipement(equipements[5]); // Micros HF
            salles[i].addEquipement(equipements[7]); // WiFi
            salles[i].addEquipement(equipements[8]); // Climatisation
            salles[i].addEquipement(equipements[9]); // Multiprise

            em.persist(salles[i]);
        }

        return salles;
    }

    private void creerReservations(EntityManager em, Utilisateur[] utilisateurs, Salle[] salles) {
        System.out.println("Création des réservations...");

        LocalDateTime maintenant = LocalDateTime.now();

        String[] motifs = {
                "Point d'équipe hebdomadaire", "Entretien annuel", "Session de formation",
                "Présentation aux partenaires", "Atelier créatif", "Revue de sprint",
                "Webinaire interne", "Workshop design thinking",
                "Réunion budgétaire", "Comité de direction", "Démonstration prototype"
        };

        // 100 réservations sur les 3 prochains mois
        for (int i = 0; i < 100; i++) {
            int jourOffset = random.nextInt(90);
            int heureDebut = 8 + random.nextInt(9);
            int duree = 1 + random.nextInt(3);

            LocalDateTime dateDebut = maintenant.plusDays(jourOffset)
                    .withHour(heureDebut).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime dateFin = dateDebut.plusHours(duree);

            Utilisateur utilisateur = utilisateurs[random.nextInt(utilisateurs.length)];
            Salle salle = salles[random.nextInt(salles.length)];

            Reservation reservation = new Reservation(
                    dateDebut, dateFin, motifs[random.nextInt(motifs.length)]);

            // 80% confirmées, 10% en attente, 10% annulées
            int tirage = random.nextInt(10);
            if (tirage < 8) {
                reservation.setStatut(StatutReservation.CONFIRMEE);
            } else if (tirage < 9) {
                reservation.setStatut(StatutReservation.EN_ATTENTE);
            } else {
                reservation.setStatut(StatutReservation.ANNULEE);
            }

            utilisateur.addReservation(reservation);
            salle.addReservation(reservation);

            em.persist(reservation);
        }
    }
}