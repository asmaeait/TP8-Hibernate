# TP Capstone — Application de Réservation de Salles

## Description

Application Java complète de gestion des réservations de salles de réunion, développée avec **JPA/Hibernate** et une base de données **H2 en mémoire**. Ce projet capstone intègre tous les concepts vus dans les TP précédents : entités JPA, relations, optimistic locking, cache de second niveau, pagination et requêtes avancées.

---

## Environnement de développement

| Élément | Version |
|---|---|
| JDK | OpenJDK 8 |
| IDE | IntelliJ IDEA |
| Maven | 3.x |
| Hibernate | 5.6.5.Final |
| Base de données | H2 (in-memory) |
| Cache | EhCache |
| OS | Windows 10/11 x64 |

---

## Structure du projet

<img width="640" height="759" alt="image" src="https://github.com/user-attachments/assets/cae1a171-dcbb-445c-91b0-c143bd088a7e" />

<img width="643" height="765" alt="image" src="https://github.com/user-attachments/assets/b55a45e1-cad4-48fd-8a4e-2f1bf6c6f8d3" />

---

## Modèle de données

### Entités et relations

- **Utilisateur** ↔ **Reservation** : OneToMany (un utilisateur peut avoir plusieurs réservations)
- **Salle** ↔ **Reservation** : OneToMany (une salle peut avoir plusieurs réservations)
- **Salle** ↔ **Equipement** : ManyToMany (une salle peut avoir plusieurs équipements)

### Données de test

**20 utilisateurs** répartis dans 10 départements :
- Ressources Humaines, Informatique, Finance, Marketing, Commercial
- Production, Recherche et Développement, Juridique, Communication, Direction

**15 salles** réparties en 3 blocs :
- **Bloc A** (5 salles) : Petites salles de réunion, capacité 8 à 16 personnes
- **Bloc B** (5 salles) : Salles de formation, capacité 25 à 45 personnes
- **Bloc C** (5 salles) : Grandes salles de conférence, capacité 60 à 140 personnes

**10 équipements** :
- Vidéoprojecteur 4K, Tableau numérique interactif, Kit visioconférence Pro
- Tableau blanc effaçable, Sono de salle, Micros HF sans fil
- Poste informatique, Réseau WiFi 6, Climatisation centralisée, Multiprise murale

**100 réservations** sur 3 mois avec 11 motifs différents :
- 80% CONFIRMÉE, 10% EN_ATTENTE, 10% ANNULÉE

---

## Configuration

### Cache de second niveau (EhCache)

```xml
<property name="hibernate.cache.use_second_level_cache" value="true"/>
<property name="hibernate.cache.region.factory_class"
          value="org.hibernate.cache.ehcache.EhCacheRegionFactory"/>
<property name="hibernate.cache.use_query_cache" value="true"/>
<property name="hibernate.generate_statistics" value="true"/>
```

Caches configurés dans `ehcache.xml` :
- `com.example.model.Utilisateur` — 1000 éléments, TTL 600s
- `com.example.model.Salle` — 500 éléments, TTL 600s
- `com.example.model.Reservation` — 5000 éléments, TTL 600s
- `com.example.model.Equipement` — 100 éléments, TTL 600s
- `com.example.model.Salle.equipements` — 1000 éléments, TTL 600s

---

## Lancement de l'application

### Prérequis

```bash
java -version   # OpenJDK 11+
mvn -version    # Maven 3+
```

### Compilation et exécution

```bash
mvn clean install
mvn exec:java -Dexec.mainClass="com.example.App"
```

Ou directement depuis IntelliJ : **Run → App.java**

### Menu principal

```
=== APPLICATION DE RÉSERVATION DE SALLES ===

=== MENU PRINCIPAL ===
1. Initialiser les données de test
2. Exécuter les scénarios de test
3. Simuler la migration de base de données
4. Générer un rapport de performance
5. Quitter
```

**Ordre recommandé :** 1 → 2 → 4 → 5

---

## Scénarios de test

### Test 1 — Recherche de disponibilité

Recherche les salles disponibles sur un créneau donné en excluant les salles ayant une réservation CONFIRMÉE qui chevauche ce créneau.

### Test 2 — Recherche multi-critères

Filtrage dynamique par capacité minimale, capacité maximale, bâtiment, étage et équipement. La requête JPQL est construite dynamiquement selon les critères fournis.

### Test 3 — Pagination

Affichage des 15 salles paginées par 5. Utilisation de `PaginationResult<T>` pour gérer les métadonnées de pagination (page courante, total de pages, hasNext, hasPrevious).

### Test 4 — Optimistic Locking

Simulation de deux threads modifiant la même réservation simultanément. Le thread 1 modifie le motif (avec délai de 1 seconde), le thread 2 modifie la date de fin. L'un des deux threads déclenche une `OptimisticLockException` grâce au champ `@Version`.

### Test 5 — Performance du cache

Comparaison des temps d'accès avec et sans cache de second niveau sur 100 itérations, ainsi que comparaison des requêtes JPQL avec et sans cache de requête.

---


---

## Script de migration (migration_v2.sql)

Le script `migration_v2.sql` permet de mettre à jour une base de données existante vers la version 2.0 :

1. Sauvegarde des tables existantes
2. Ajout de nouvelles colonnes (departement, numero, reference, statut, version)
3. Mise à jour des données existantes
4. Création d'index de performance
5. Ajout de contraintes métier
6. Création d'une vue `vue_reservations_completes`
7. Table de versioning `db_version`

---

## Concepts mis en œuvre

- **JPA/Hibernate** : Entités, relations, annotations de validation
- **Optimistic Locking** : `@Version` pour gérer les accès concurrents
- **Cache L2** : EhCache pour réduire les accès base de données
- **Pagination** : `setFirstResult` / `setMaxResults` avec `PaginationResult<T>`
- **Requêtes dynamiques** : Construction JPQL avec critères variables
- **JOIN FETCH** : Chargement optimisé pour éviter le problème N+1
- **Pattern Repository/Service** : Architecture en couches
- **Transactions** : Gestion manuelle avec rollback en cas d'erreur

---

## Démonstration vidéo

Voir le fichier `demo/video_demo.mp4` joint au projet.

---

https://github.com/user-attachments/assets/b265a7e9-4d6a-4d88-8d57-cc204d742b18


