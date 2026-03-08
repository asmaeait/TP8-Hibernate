package com.example.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.stream.Collectors;

public class DatabaseMigrationTool {

    private final String jdbcUrl;
    private final String utilisateur;
    private final String motDePasse;

    public DatabaseMigrationTool(String jdbcUrl, String utilisateur, String motDePasse) {
        this.jdbcUrl = jdbcUrl;
        this.utilisateur = utilisateur;
        this.motDePasse = motDePasse;
    }

    public void executerMigration() {
        System.out.println("Démarrage de la migration de la base de données...");

        try (Connection connexion = DriverManager.getConnection(jdbcUrl, utilisateur, motDePasse)) {
            InputStream flux = getClass().getClassLoader()
                    .getResourceAsStream("migration_v2.sql");

            if (flux == null) {
                throw new RuntimeException("Fichier migration_v2.sql introuvable dans les ressources.");
            }

            String script = new BufferedReader(new InputStreamReader(flux))
                    .lines().collect(Collectors.joining("\n"));

            String[] instructions = script.split(";");

            try (Statement stmt = connexion.createStatement()) {
                for (String instruction : instructions) {
                    String sql = instruction.trim();
                    if (!sql.isEmpty()) {
                        System.out.println("Exécution: " + sql.substring(0, Math.min(60, sql.length())) + "...");
                        stmt.execute(sql);
                    }
                }
            }

            System.out.println("Migration terminée avec succès !");

        } catch (Exception e) {
            System.err.println("Erreur lors de la migration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Utilisation avec une base externe (MySQL par exemple)
        DatabaseMigrationTool outil = new DatabaseMigrationTool(
                "jdbc:mysql://localhost:3306/reservation_salles",
                "root",
                "motdepasse"
        );
        outil.executerMigration();
    }
}
