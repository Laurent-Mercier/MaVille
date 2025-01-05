package ca.umontreal.menu;

import ca.umontreal.user.*;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class MenuPreferencesTravaux extends Menu {

    public MenuPreferencesTravaux() {
        super("preferencesTravaux");
        String[] options = {"Fournir vos préférences (1)", "Consulter les préférences (2)", "Retour (3)"};
        this.setElements(new ArrayList<>(Arrays.asList(options)));
    }

    /**
     * Affiche menu
     */
    public void showMenu(Scanner scanner, User user) {
        System.out.println("\nFournissez vos préférences ou consultez celles des autres usagers de votre quartier! " +
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
     * @return
     */
    public Menu updateMenu(int choix, User user, Scanner scanner) {
        switch (choix) {
            case 1:
                handleProvidePreferences(scanner, user);
                break;
            case 2:
                handleConsultPreferences(user);
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
     * Permet à l'utilisateur de fournir ses préférences horaires pour chaque jour de la semaine.
     * @param scanner
     * @param user
     */
    private void handleProvidePreferences(Scanner scanner, User user) {
        System.out.println("\nFournissez vos préférences horaires pour chaque jour de la semaine (format: HH:MM->HH:MM ou Aucun):\n");
        StringBuilder preferences = new StringBuilder();

        for (String day : new String[]{"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"}) {
            System.out.println(day + ":");
            while (true) {
                String input = scanner.nextLine().trim();
                if (input.equalsIgnoreCase("Aucun") || isValidHourRange(input)) {
                    preferences.append(day).append("=").append(input).append(";");
                    break;
                } else {
                    System.out.println("Format invalide. Entrez HH:MM->HH:MM ou Aucun:\n");
                }
            }
        }

        boolean success = updatePreferencesApiCall(user.getCourriel(), preferences.toString());
        if (success) {
            System.out.println("\nPréférences horaires mises à jour avec succès!\n");
            user.setHoraire(preferences.toString());
        } else {
            System.out.println("\nErreur lors de la mise à jour des préférences. Veuillez réessayer plus tard.\n");
        }
    }

    /**
     * Affiche les préférences horaires actuelles de l'utilisateur.
     * @param user
     */
    private void handleConsultPreferences(User user) {
        System.out.println("\nVos préférences horaires actuelles:\n");

        String preferences = user.getHoraire();
        if (preferences.isEmpty()) {
            System.out.println("Aucune préférence horaire enregistrée.\n");
        } else {
            for (String entry : preferences.split(";")) {
                String[] parts = entry.split("=");
                if (parts.length == 2) {
                    System.out.println(parts[0] + ": " + parts[1]);
                }
            }
        }
    }

    /**
     * Envoie une requête à l'API pour mettre à jour les préférences horaires de l'utilisateur.
     * @param courriel
     * @param preferences
     * @return boolean
     */
    private boolean updatePreferencesApiCall(String courriel, String preferences) {
        String url = "http://localhost:7070/updatePreferences";
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("courriel="
                        + URLEncoder.encode(courriel, StandardCharsets.UTF_8)
                        + "&preferences=" + URLEncoder.encode(preferences,StandardCharsets.UTF_8)))
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

    /**
     * Vérifie si input représente une plage horaire valid
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
}