package ca.umontreal.menu;

import ca.umontreal.user.User;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

public class MenuSeConnecter extends Menu {

    public MenuSeConnecter() {
        super("seConnecter");
    }

    /**
     * Affiche l'interface de connexion pour l'utilisateur, permettant de vérifier l'adresse courriel
     * et le mot de passe. L'utilisateur a un nombre limité de tentatives pour chaque étape.
     * @param scanner
     * @param user
     */
    public void showMenu(Scanner scanner, User user) {
        System.out.println("\nConnexion\n");

        boolean isUserFound = false;
        boolean isPasswordCorrect = false;
        int emailAttempts = 3;
        int passwordAttempts = 3;

        try {
            while (!isUserFound && emailAttempts > 0) {
                System.out.println("\nEntrez votre adresse courriel:\n");
                String courriel = scanner.nextLine();
                isUserFound = checkCredentialsApiCall(courriel, user);

                if (!isUserFound) {
                    emailAttempts--;
                    if (emailAttempts > 0) {
                        System.out.println("\nCompte inexistant. Veuillez vérifier votre courriel. Il vous reste " + emailAttempts + " tentatives.\n");
                    }
                }
            }

            if (!isUserFound) {
                System.out.println("\nNombre maximum de tentatives atteint.\n");
                return;
            }

            while (!isPasswordCorrect && passwordAttempts > 0) {
                System.out.println("\nEntrez votre mot de passe:\n");
                String password = scanner.nextLine();

                if (user.getPassword().equals(password)) {
                    isPasswordCorrect = true;
                    System.out.println("\nConnexion réussie!\n");
                } else {
                    passwordAttempts--;
                    if (passwordAttempts > 0) {
                        System.out.println("\nMot de passe incorrect. Il vous reste " + passwordAttempts + " tentatives.\n");
                    }
                }
            }

            if (!isPasswordCorrect) {
                System.out.println("\nNombre maximum de tentatives atteint.\n");
                user.setNom("");
                return;
            }

        } catch (Exception e) {
            System.out.println("\nErreur lors de la connexion. Veuillez réessayer plus tard.\n");
            e.printStackTrace();
        }
    }

    /**
     * Vérifie si l'adresse courriel correspond à un utilisateur existant via des appels à deux API :
     * une pour les résidents et une pour les intervenants.
     * @param courriel
     * @param user
     * @return boolean
     */
    public boolean checkCredentialsApiCall(String courriel, User user) {
        return checkUserType(courriel, user, "http://localhost:7070/getResidents", "resident") ||
               checkUserType(courriel, user, "http://localhost:7070/getIntervenants", "intervenant");
    }

    /**
     * Vérifie si une adresse courriel correspond à un utilisateur d'un type spécifique via un API.
     * @param courriel
     * @param user
     * @param url
     * @param userType
     * @return boolean
     */
    private boolean checkUserType(String courriel, User user, String url, String userType) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONArray records = new JSONArray(response.body());

            for (int i = 0; i < records.length(); i++) {
                JSONObject record = records.getJSONObject(i);
                if (record.optString("courriel").equals(courriel)) {
                    populateUserDetails(user, record, userType);
                    return true;
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Remplit les détails de l'utilisateur à partir d'un enregistrement
     * @param user
     * @param record
     * @param userType
     */
    private void populateUserDetails(User user, JSONObject record, String userType) {
        user.setNom(record.optString("nom"));
        user.setCourriel(record.optString("courriel"));
        user.setPassword(record.optString("password"));
        user.setUserType(userType);

        if (userType.equals("resident")) {
            user.setDateNaissance(record.optString("dateNaissance"));
            user.setTelephone(record.optString("telephone"));
            user.setAdresse(record.optString("adresse"));
            user.setArrondissement(record.optString("arrondissement"));
            user.setHoraire(record.optString("horaire", "")); // Handle new horaire field
        } else if (userType.equals("intervenant")) {
            user.setTypeIntervenant(record.optString("typeIntervenant"));
            user.setIdVille(record.optString("idVille"));
        }
    }

    /**
     * Met à jour le menu en fonction de l'état de connexion de l'utilisateur.
     * @param choix
     * @param user
     * @param scanner
     * @return un menu
     */
    public Menu updateMenu(int choix, User user, Scanner scanner) {
        if (!(user.getNom().isEmpty() || user.getNom().equals(""))) {
            return new MenuPrincipal();
        }
        return new MenuConnexion();
    }
}