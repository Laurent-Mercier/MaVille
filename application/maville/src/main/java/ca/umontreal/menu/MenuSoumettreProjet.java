package ca.umontreal.menu;

import ca.umontreal.user.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class MenuSoumettreProjet extends Menu {
    public MenuSoumettreProjet() {
        super("soumettreProjet");
        String[] options = {"Soumettre un nouveau projet (1)", "Faire un suivi (2)", "Retour (3)"};
        this.setElements(new ArrayList<>(Arrays.asList(options)));
    }

    /**
     * Affiche le menu permettant à l'utilisateur de soumettre un nouveau projet ou de suivre des projets existants.
     * @param scanner
     * @param user
     */
    public void showMenu(Scanner scanner, User user) {
        System.out.println("\nSoumettez un nouveau projet ou faites un suivi des projets existants! " +
                "Entrez le numéro entre parenthèses pour choisir une option:\n");
        for (String option : this.getElements()) {
            System.out.println(option);
        }
        System.out.println();
    }

    /**
     * Met à jour le menu en fonction du choix de l'utilisateur.
     * @param choix
     * @param user
     * @param scanner
     * @return un menu
     */
    public Menu updateMenu(int choix, User user, Scanner scanner) {
        switch (choix) {
            case 1:
                handleNewProject(scanner, user);
                break;
            case 2:
                handleProjectFollowUp(scanner, user);
                break;
            case 3:
                return new MenuPrincipal();
            default:
                System.out.println("\nEntrez un nombre de 1 à 3 pour choisir une option:\n");
                return this;
        }

        System.out.println("\nEntrez 1 pour revenir en arrière ou 2 pour revenir au menu principal:\n");
        int choix2 = validateChoiceInput(1, 2, scanner);
        return choix2 == 1 ? this : new MenuPrincipal();
    }

    /**
     * * Gère la soumission d'un nouveau projet par l'utilisateur.
     * @param scanner
     * @param user
     */
    private void handleNewProject(Scanner scanner, User user) {
        System.out.println("\nEntrez les détails du projet:\n");
    
        System.out.println("Titre du projet:");
        String titreProjet = scanner.nextLine();
    
        System.out.println("Description:");
        String description = scanner.nextLine();
    
        System.out.println("Type de travaux:");
        String typeDeTravaux = scanner.nextLine();
    
        System.out.println("Quartiers affectés (séparés par des virgules):");
        String quartiersAffecte = scanner.nextLine().replace("\"", "") // Remove excessive quotes
        .trim();
    
        String dateDebut;
        while (true) {
            System.out.println("\nQuel est la date de début (dd/MM/yyyy):\n");
            dateDebut = scanner.nextLine();
            if (validateDate(dateDebut)) {
                break;
            } else {
                System.out.println("\nDate invalide. Elle doit être une date future (format: dd/MM/yyyy).\n");
            }
        }

        String dateFin;
        while (true) {
            System.out.println("\nQuel est la date de fin (dd/MM/yyyy):\n");
            dateFin = scanner.nextLine();
            if (validateDate(dateDebut)) {
                break;
            } else {
                System.out.println("\nDate invalide. Elle doit être une date future (format: dd/MM/yyyy).\n");
            }
        }
        System.out.println("Fournissez l'horaire du projet pour chaque jour de la semaine (format: HH:MM->HH:MM ou Aucun):\n");
        StringBuilder horaire = new StringBuilder();
    
        for (String day : new String[]{"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"}) {
            System.out.println(day + ":");
            while (true) {
                String input = scanner.nextLine().trim();
                if (input.equalsIgnoreCase("Aucun") || isValidHourRange(input)) {
                    horaire.append(day).append("=").append(input).append(";");
                    break;
                } else {
                    System.out.println("Format invalide. Entrez HH:MM->HH:MM ou Aucun:\n");
                }
            }
        }
    
        String validatedHoraire=validateAgainstResidentPreferences(quartiersAffecte, horaire.toString(), scanner);
        horaire = new StringBuilder(validatedHoraire);
        boolean success = submitProjectToApi(user.getCourriel(), titreProjet, description, typeDeTravaux, quartiersAffecte, dateDebut, dateFin, horaire.toString());
        if (success) {
            System.out.println("\nProjet soumis avec succès!\n");
            sendNotificationsToResidents(quartiersAffecte, titreProjet, description);
        } else {
            System.out.println("\nErreur lors de la soumission du projet. Veuillez réessayer plus tard.\n");
        }
    }

    /**
     * Gère le suivi des projets existants pour l'utilisateur.
     * @param scanner
     * @param user
     */
    private void handleProjectFollowUp(Scanner scanner, User user) {
        System.out.println("\nProjets existants:\n");
        String url = "http://localhost:7070/getProjets";
        HttpClient client = HttpClient.newHttpClient();
    
        try {
            HttpResponse<String> response = client.send(HttpRequest.newBuilder().uri(URI.create(url)).GET().build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JSONArray projets = new JSONArray(response.body());
                boolean hasProjects = false;
    
                for (int i = 0; i < projets.length(); i++) {
                    JSONObject projet = projets.getJSONObject(i);
                    if (projet.optString("intervenant").equals(user.getCourriel())) {
                        hasProjects = true;
                        System.out.println("ID: " + projet.getInt("id"));
                        System.out.println("Titre: " + projet.getString("titreProjet"));
                        System.out.println("Statut: " + projet.getString("statut"));
                        System.out.println("--------------------------------------");
                    }
                }
    
                if (!hasProjects) {
                    System.out.println("Vous n'avez aucun projet pour le moment.\n");
                    return;
                }
    
                System.out.println("\nEntrez l'ID du projet pour modifier son statut:");
                String projectIdStr = scanner.nextLine();
    
                System.out.println("Nouveau statut (prévu/en cours):");
                String newStatus = scanner.nextLine();
    
                boolean updated = updateProjectStatusApiCall(projectIdStr, newStatus);
                if (updated) {
                    System.out.println("\nStatut mis à jour avec succès!\n");
    
                    // Fetch project details to send notifications
                    JSONObject updatedProject = fetchProjectById(projectIdStr);
                    if (updatedProject != null) {
                        String quartiersAffecte = updatedProject.getString("quartiersAffecte")
                                       .replace("\"", "") // Remove extra quotes
                                       .trim(); // Remove leading/trailing spaces
                        String titreProjet = updatedProject.getString("titreProjet");
                        String description = "Le projet '" + titreProjet + "' a changé de statut: " + newStatus;
    
                        // Send notifications to residents
                        sendNotificationsToResidents(quartiersAffecte, titreProjet, description);
                    }
                } else {
                    System.out.println("\nErreur lors de la mise à jour du statut.\n");
                }
            } else {
                System.out.println("\nErreur lors de la récupération des projets. Veuillez réessayer plus tard.\n");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            System.out.println("\nErreur lors de la consultation des projets.\n");
        }

    }

    /**
     * Envoie des notifications aux résidents concernés par un projet spécifique.
     * @param quartiersAffecte liste des quartiers affectés
     * @param titreProjet
     * @param description
     */
    private void sendNotificationsToResidents(String quartiersAffecte, String titreProjet, String description) {
        String url = "http://localhost:7070/sendNotifications";
        HttpClient client = HttpClient.newHttpClient();
    
        String body = String.format("quartiersAffecte=%s&titreProjet=%s&description=%s",
                encodeValue(quartiersAffecte), encodeValue(titreProjet), encodeValue(description));
    
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
    
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                System.out.println("\nNotifications envoyées avec succès!\n");
            } else {
                System.out.println("\nErreur lors de l'envoi des notifications: " + response.body() + "\n");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            System.out.println("\nErreur lors de la communication avec le serveur de notifications.\n");
        }
    }

    /**
     * Valide les horaires d'un projet par rapport aux préférences des résidents affectés par le projet.
     * @param quartiersAffecte
     * @param horaire
     * @param scanner
     * @return l'horaire
     */
    private String validateAgainstResidentPreferences(String quartiersAffecte, String horaire, Scanner scanner) {
        String url = "http://localhost:7070/getResidents";
        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpResponse<String> response = client.send(HttpRequest.newBuilder().uri(URI.create(url)).GET().build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JSONArray residents = new JSONArray(response.body());
                StringBuilder newHoraire = new StringBuilder();
    
                for (String entry : horaire.split(";")) {
                    String[] parts = entry.split("=");
                    if (parts.length == 2) {
                        String day = parts[0];
                        String hours = parts[1];
                        boolean modified = false;
    
                        if (!hours.equalsIgnoreCase("Aucun")) { // Only check overlap if hours are specified
                            for (int i = 0; i < residents.length(); i++) {
                                JSONObject resident = residents.getJSONObject(i);
                                String residentHoraire = resident.optString("horaire", "");
                                if (!residentHoraire.isEmpty() && quartiersAffecte.contains(resident.optString("arrondissement"))) {
                                    for (String residentEntry : residentHoraire.split(";")) {
                                        String[] residentParts = residentEntry.split("=");
                                        if (residentParts.length == 2 && residentParts[0].equals(day)) {
                                            String residentHours = residentParts[1];
                                            if (!residentHours.equalsIgnoreCase("Aucun") && !hoursOverlap(hours, residentHours)) {
                                                System.out.println("\nLe calendrier pour " + day + " (" + hours + ") est en dehors des préférences de " + resident.getString("courriel") + " (" + residentHours + "). Voulez-vous le modifier? (Oui/Non):");
                                                String decision = scanner.nextLine().trim();
                                                if (decision.equalsIgnoreCase("Oui")) {
                                                    System.out.println("Entrez un nouvel horaire pour " + day + " (HH:MM->HH:MM ou Aucun):");
                                                    while (true) {
                                                        String newInput = scanner.nextLine().trim();
                                                        if (newInput.equalsIgnoreCase("Aucun") || isValidHourRange(newInput)) {
                                                            hours = newInput;
                                                            modified = true;
                                                            break;
                                                        } else {
                                                            System.out.println("Format invalide. Entrez HH:MM->HH:MM ou Aucun:");
                                                        }
                                                    }
                                                }
                                                break; // Break inner loop after modification
                                            }
                                        }
                                    }
                                }
                            }
                        }
    
                        newHoraire.append(day).append("=").append(hours).append(";");
                    }
                }
    
                return newHoraire.toString();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            System.out.println("\nErreur lors de la vérification des préférences des résidents.\n");
        }
        return horaire;
    }

    /**
     * Valide une date
     * @param date
     * @return boolean
     */
    private boolean validateDate(String date) {
        try {
            LocalDate parsedDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            return parsedDate.isAfter(LocalDate.now());
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Vérifie si deux plages horaires se chevauchent.
     * @param hours1
     * @param hours2
     * @return boolean
     */
    private boolean hoursOverlap(String hours1, String hours2) {
        if (hours1.equalsIgnoreCase("Aucun") || hours2.equalsIgnoreCase("Aucun")) {
            // If either schedule is "Aucun", there is no overlap
            return false;
        }
    
        String[] range1 = hours1.split("->");
        String[] range2 = hours2.split("->");
    
        int start1 = Integer.parseInt(range1[0].replace(":", ""));
        int end1 = Integer.parseInt(range1[1].replace(":", ""));
        int start2 = Integer.parseInt(range2[0].replace(":", ""));
        int end2 = Integer.parseInt(range2[1].replace(":", ""));
    
        // Check for overlap
        return !(end1 <= start2 || end2 <= start1);
    }

    /**
     * Soumet un projet à l'API avec les informations spécifiées.
     * @return boolean
     */
    private boolean submitProjectToApi(String intervenant, String titreProjet, String description, String typeDeTravaux, String quartiersAffecte, String dateDebut, String dateFin, String horaire) {
        String url = "http://localhost:7070/soumettre-projet";
        HttpClient client = HttpClient.newHttpClient();
        String body = String.format("intervenant=%s&titreProjet=%s&description=%s&typeDeTravaux=%s&quartiersAffecte=%s&dateDebut=%s&dateFin=%s&horaire=%s",
                encodeValue(intervenant), encodeValue(titreProjet), encodeValue(description), encodeValue(typeDeTravaux),
                encodeValue(quartiersAffecte), encodeValue(dateDebut), encodeValue(dateFin), encodeValue(horaire));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Récupère les détails d'un projet spécifique en fonction de son identifiant.
     * @param projectId
     */
    private JSONObject fetchProjectById(String projectId) {
        String url = "http://localhost:7070/getProjets";
        HttpClient client = HttpClient.newHttpClient();
    
        try {
            // Send GET request to fetch all projects
            HttpResponse<String> response = client.send(
                    HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .header("Accept", "application/json")
                            .GET()
                            .build(),
                    HttpResponse.BodyHandlers.ofString()
            );
    
            // Check if the response is successful
            if (response.statusCode() == 200) {
                // Parse the response body as a JSON array
                JSONArray projets = new JSONArray(response.body());
    
                // Iterate through the array to find the project with the matching ID
                for (int i = 0; i < projets.length(); i++) {
                    JSONObject projet = projets.getJSONObject(i);
                    if (projet.getString("id").equals(projectId)) {
                        return projet; // Return the matching project
                    }
                }
            } else {
                System.out.println("Erreur lors de la récupération des projets: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Erreur lors de la communication avec le serveur.");
            e.printStackTrace();
        }
    
        return null; // Return null if the project is not found or an error occurs
    }

    /**
     * Met à jour le statut d'un projet via un appel à l'API.
     * @param projectId
     * @param newStatus
     * @return boolean
     */
    private boolean updateProjectStatusApiCall(String projectId, String newStatus) {
        String url = "http://localhost:7070/updateProjetStatus";
        HttpClient client = HttpClient.newHttpClient();
        String body = String.format("projectId=%s&statut=%s",
                encodeValue(projectId), encodeValue(newStatus));
    
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
    
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Valide si input représente une plage horaire au format correct.
     * @param input
     * @return boolean
     */
    private boolean isValidHourRange(String input) {
        if (input.matches("\\d{2}:\\d{2}->\\d{2}:\\d{2}")) {
            String[] hours = input.split("->");
            String[] start = hours[0].split(":");
            String[] end = hours[1].split(":");
    
            int startHour = Integer.parseInt(start[0]);
            int startMinute = Integer.parseInt(start[1]);
            int endHour = Integer.parseInt(end[0]);
            int endMinute = Integer.parseInt(end[1]);
    
            boolean isValidStart = startHour >= 0 && startHour < 24 && startMinute >= 0 && startMinute < 60;
            boolean isValidEnd = endHour >= 0 && endHour < 24 && endMinute >= 0 && endMinute < 60;
    
            return isValidStart && isValidEnd && ((startHour < endHour) || (startHour == endHour && startMinute < endMinute));
        }
        return false;
    }
    
    private String encodeValue(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
    /**
     * Valide l'input du scanner
     * @param scanner le scanner pour la saisie
     * @return le choix validé
     */
    private int validateChoiceInput(int lowerBound, int upperBound, Scanner scanner) {
        while (true) {
            try {
                int choix = Integer.parseInt(scanner.nextLine());
                if (choix >= lowerBound && choix <= upperBound) {
                    return choix;
                }
            } catch (NumberFormatException ignored) {
            }
            System.out.println("\nVeuillez entrer un numéro entre " + lowerBound + " et " + upperBound + ".\n");
        }
    }    
}
