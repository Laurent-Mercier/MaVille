package ca.umontreal.menu;

import ca.umontreal.user.User;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class MenuNotification extends Menu {

    public MenuNotification() {
        super("notification");
        String[] options = {"Voir mes notifications (1)", "Retour au menu principal (2)"};
        this.setElements(new ArrayList<>(Arrays.asList(options)));
    }

    /**
     * Affiche menu
     */
    @Override
    public void showMenu(Scanner scanner, User user) {
        JSONArray notifications = fetchNotificationsFromApi(user);
        String resident = user.getCourriel();

        long newNotificationsCount = getNewNotificationsCount(resident, notifications);

        System.out.println("Nouvelles notifications: " + newNotificationsCount);
        System.out.println("Entrez le numéro entre parenthèses pour choisir une option:\n");
        for (String option : this.getElements()) {
            System.out.println(option);
        }
        System.out.println();
    }

    /**
     * Affiche le menu selon le choix de l'usager
     * @param choix choix de l'usager
     * @param user
     * @param scanner
     * @return un menu
     */
    @Override
    public Menu updateMenu(int choix, User user, Scanner scanner) {
        switch (choix) {
            case 1:
                int menuChoice = viewNotifications(user, scanner);
                if (menuChoice==1) {
                    return this;
                } else {
                    return new MenuPrincipal();
                }

            case 2:
                return new MenuPrincipal();

            default:
                System.out.println("\nEntrez un nombre valide entre 1 et 2.\n");
        }
        return this;
    }
    /**
     * Affiche les notifications de l'utilisateur
     */
    private int viewNotifications(User user, Scanner scanner) {
        JSONArray notifications = fetchNotificationsFromApi(user);
        String resident = user.getCourriel();
    
        // Filter notifications for the resident
        JSONArray residentNotifications = new JSONArray();
        for (int i = 0; i < notifications.length(); i++) {
            JSONObject notification = notifications.getJSONObject(i);
            if (resident.equals(notification.getString("resident"))) {
                residentNotifications.put(notification);
            }
        }
    
        if (residentNotifications.isEmpty()) {
            System.out.println("\nVous n'avez aucune notification pour le moment.\n");
        } else {
            // Sort notifications by ID in descending order
            residentNotifications = sortNotificationsById(residentNotifications);
    
            System.out.println("\nVos notifications :");
            for (int i = 0; i < residentNotifications.length(); i++) {
                JSONObject notification = residentNotifications.getJSONObject(i);
                System.out.println("\n=================================================");
                System.out.println("ID: " + notification.getInt("id"));
                System.out.println("Description: " + notification.getString("description"));
                System.out.println("Vue: " + (notification.getBoolean("vue") ? "Oui" : "Non"));
                System.out.println("=================================================");
            }
    
            // Mark all notifications as viewed
            markNotificationsAsViewed(resident);
        }
    
        System.out.println("\nEntrez 1 pour revenir au menu précédent ou 2 pour revenir au menu principal:\n");
        int choix = validateChoiceInput(1, 2, scanner);
    
        return choix == 1 ? 1 : 0;
    }

    /**
     * Récupère les notifications pour un utilisateur spécifique via un appel à l'API.
     * @param user utilisateur
     * @return le résultat de l'appel
     */
    private JSONArray fetchNotificationsFromApi(User user) {
        String url = "http://localhost:7070/consulter-notifications?resident=" + encodeValue(user.getCourriel());
        HttpClient client = HttpClient.newHttpClient();
    
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET()
                .build();
    
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return new JSONArray(response.body());
            } else if (response.statusCode() == 404) {
                System.out.println("\nAucune notification trouvée pour ce résident.");
            } else {
                System.out.println("\nErreur lors de la récupération des notifications.");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    
        return new JSONArray();
    }

    /**
     * Compte le nombre de nouvelles notifications non vues pour un résident
     * @param resident resident
     * @param notifications liste des notifications
     */
    private long getNewNotificationsCount(String resident, JSONArray notifications) {
        return notifications.toList().stream()
                .map(obj -> new JSONObject((java.util.Map<?, ?>) obj))
                .filter(notification ->
                        resident.equals(notification.getString("resident")) &&
                                !notification.getBoolean("vue")
                )
                .count();
    }

    /**
     * Arrange les notifications par ID
     * @param notifications liste de notificiations
     * @return liste de notifications sorted
     */
    private JSONArray sortNotificationsById(JSONArray notifications) {
        ArrayList<JSONObject> list = new ArrayList<>();
        for (int i = 0; i < notifications.length(); i++) {
            list.add(notifications.getJSONObject(i));
        }
        list.sort((n1, n2) -> Integer.compare(n2.getInt("id"), n1.getInt("id"))); // Descending order by ID

        return new JSONArray(list);
    }

    /**
     * Marque toutes les notifications comme vues pour un résident
     * @param resident
     */
    private void markNotificationsAsViewed(String resident) {
        String url = "http://localhost:7070/marquer-notifications-vues";
        HttpClient client = HttpClient.newHttpClient();

        String body = String.format("resident=%s", encodeValue(resident));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                System.out.println("\nErreur lors de la mise à jour des notifications comme vues.\n");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            System.out.println("\nErreur de communication avec le serveur.\n");
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

    private String encodeValue(String value) {
        return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
    }
}
