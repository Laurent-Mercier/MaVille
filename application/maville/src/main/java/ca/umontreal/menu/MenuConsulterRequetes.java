package ca.umontreal.menu;

import ca.umontreal.user.User;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.URLEncoder.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class MenuConsulterRequetes extends Menu {

    public MenuConsulterRequetes() {
        super("consulterRequetes");
        String[] options = {
                "Afficher les requêtes (1)",
                "Lister mes candidatures (2)",
                "Retour au menu principal (3)"
        };
        this.setElements(new ArrayList<>(Arrays.asList(options)));
    }

    /**
     * Affiche le menu
     */
    public void showMenu(Scanner scanner, User user) {
        System.out.println("\nConsultez les requêtes ou gérez les candidatures. Entrez le numéro entre parenthèses pour choisir une option:\n");
        for (String option : this.getElements()) {
            System.out.println(option);
        }
        System.out.println();
    }
    /**
     * Met à jour le menu en fonction du choix de l'utilisateur.
     */
    public Menu updateMenu(int choix, User user, Scanner scanner) {
        switch (choix) {
            case 1:
                handleConsultRequetes(scanner, user);
                break;

            case 2:
                if (user.getUserType().equalsIgnoreCase("intervenant")) {
                    listMyCandidatures(user);
                } else {
                    System.out.println("\nOption non disponible pour les résidents.\n");
                }
                break;

            case 3:
                return new MenuPrincipal();

            default:
                System.out.println("\nEntrez un nombre valide entre 1 et 3.\n");
        }
        return this;
    }
    /**
     * Gère la consultation des requêtes disponibles et permet à l'utilisateur de filtrer
     * par quartier ou de sélectionner une requête pour poser une candidature.
     * @param scanner le scanner pour la saisie utilisateur.
     * @param user l'utilisateur actuel effectuant la consultation.
     */
    private void handleConsultRequetes(Scanner scanner, User user) {
        String url = "http://localhost:7070/consulter-requetes";
        try {
            HttpResponse<String> response = fetchApiData(url);
            if (response.statusCode() == 200) {
                JSONArray requests = new JSONArray(response.body());
                displayRequests(requests);

                System.out.println("\nVoulez-vous filtrer par quartier ? (Oui/Non):\n");
                String filterDecision = scanner.nextLine().trim();
                if (filterDecision.equalsIgnoreCase("Oui")) {
                    System.out.println("\nEntrez le nom du quartier pour filtrer les requêtes:\n");
                    String quartier = scanner.nextLine();
                    filterByQuartier(requests, quartier, scanner, user);
                } else {
                    handleRequestSelection(requests, scanner, user);
                }
            } else {
                System.out.println("\nErreur lors de la récupération des requêtes.\n");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            System.out.println("\nErreur de communication avec le serveur.\n");
        }
    }
    /**
     * Gère la sélection d'une requête par l'utilisateur et la soumission d'une candidature.
     * @param requests les requêtes disponibles parmi lesquelles l'utilisateur peut choisir.
     * @param scanner le scanner pour la saisie utilisateur.
     * @param user l'utilisateur actuel qui soumet la candidature.
     */
    private void handleRequestSelection(JSONArray requests, Scanner scanner, User user) {
        System.out.println("\nEntrez l'ID de la requête pour poser votre candidature:\n");
        String requeteId = scanner.nextLine();

        JSONObject selectedRequest = null;
        for (int i = 0; i < requests.length(); i++) {
            JSONObject request = requests.getJSONObject(i);
            if (request.optString("requeteId").equals(requeteId)) {
                selectedRequest = request;
                break;
            }
        }
        if (selectedRequest == null) {
            System.out.println("\nID de requête invalide. Retour au menu principal.\n");
            return;
        }
        System.out.println("\nVoulez-vous poser une candidature pour cette requête ? (Oui/Non):\n");
        String decision = scanner.nextLine().trim();

        if (decision.equalsIgnoreCase("Oui")) {
            boolean success = manageCandidatureApi(requeteId, user.getCourriel(), "http://localhost:7070/poser-candidature");
            if (success) {
                System.out.println("\nCandidature posée avec succès!\n");
            } else {
                System.out.println("\nÉchec de la soumission de la candidature.\n");
            }
        } else {
            System.out.println("\nRetour au menu principal.\n");
        }
    }

    /**
     * Applique un filtre sur les requêtes en fonction d'un quartier donné.
     * @param requests les requêtes à filtrer.
     * @param quartier le quartier utilisé comme critère de filtrage.
     * @param scanner le scanner pour la saisie utilisateur.
     * @param user l'utilisateur actuel qui effectue l'action.
     */
    private void filterByQuartier(JSONArray requests, String quartier, Scanner scanner, User user) {
        JSONArray filteredRequests = new JSONArray();
        
        // Filter requests by the specified quartier
        for (int i = 0; i < requests.length(); i++) {
            JSONObject request = requests.getJSONObject(i);
            if (request.optString("quartier").equalsIgnoreCase(quartier)) {
                filteredRequests.put(request);
            }
        }
        if (filteredRequests.isEmpty()) {
            System.out.println("\nAucune requête trouvée pour le quartier spécifié.\n");
            return;
        }
        // Display the filtered requests
        displayRequests(filteredRequests);
        // Handle request selection
        handleRequestSelection(filteredRequests, scanner, user);
    }


    /**
     * Affiche les candidatures soumises par l'utilisateur.
     * @param user l'utilisateur actuel dont les candidatures sont récupérées et affichées.
     */
    private void listMyCandidatures( User user) {
        String url = "http://localhost:7070/lister-candidatures";
        try {
            HttpResponse<String> response = fetchApiData(url + "?intervenant=" +
                    URLEncoder.encode(user.getCourriel(),StandardCharsets.UTF_8));
            if (response.statusCode() == 200) {
                JSONArray candidatures = new JSONArray(response.body());
                if (candidatures.isEmpty()) {
                    System.out.println("\nVous n'avez soumis aucune candidature.\n");
                    return;
                }
                System.out.println("\nVoici vos candidatures soumises :\n");
                displayCandidatures(candidatures);
            } else {
                System.out.println("\nErreur lors de la récupération de vos candidatures.\n");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            System.out.println("\nErreur de communication avec le serveur.\n");
        }
    }

    /**
     * Fonction qui permet d'afficher les requêtes.
     * @param requests Array de requetes
     */
    private void displayRequests(JSONArray requests) {
        for (int i = 0; i < requests.length(); i++) {
            JSONObject request = requests.getJSONObject(i);
            System.out.println("\n=================================================");
            System.out.println("ID: " + request.getString("requeteId"));
            System.out.println("Titre: " + request.getString("titreDuTravail"));
            System.out.println("Description: " + request.getString("description"));
            System.out.println("Type de travaux: " + request.getString("typeDeTravaux"));
            System.out.println("Date de début: " + request.getString("dateDebut"));
            System.out.println("Quartier: " + request.getString("quartier"));
            System.out.println("=================================================");
        }
    }

    /**
     * Affiche les candidatures
     * @param candidatures Array de candidatures
     */
    private void displayCandidatures(JSONArray candidatures) {
        for (int i = 0; i < candidatures.length(); i++) {
            JSONObject candidature = candidatures.getJSONObject(i);
            System.out.println("\n=================================================");
            System.out.println("Requête ID: " + candidature.getString("requeteId"));
            System.out.println("Intervenant: " + candidature.getString("intervenant"));
            System.out.println("Accepté: " + (candidature.getBoolean("accepted")? "Oui":"Non"));
            System.out.println("=================================================");
        }
    }

    /**
     *  Helper Function qui permet de faire une HTTP request de type Accept
     * @param url Url auquel on veut faire la requete
     * @return le résultat de la requête
     */
    private HttpResponse<String> fetchApiData(String url) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Fonction qui permet de faire une HTTP request afin de gérer les candidatures
     * @param requeteId id de la requête
     * @param intervenant courriel de l'intervenant
     * @param url url de la requête
     * @return vrai ou faux
     */
    private boolean manageCandidatureApi(String requeteId, String intervenant, String url) {
        HttpClient client = HttpClient.newHttpClient();
        String body = String.format("requeteId=%s&intervenant=%s",
                URLEncoder.encode(requeteId, StandardCharsets.UTF_8),
                URLEncoder.encode(intervenant, StandardCharsets.UTF_8));

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

}
