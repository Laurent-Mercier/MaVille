package ca.umontreal.menu;

import ca.umontreal.user.User;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class MenuSoumettreRequete extends Menu {

    public MenuSoumettreRequete() {
        super("soumettreRequete");
        String[] options = {"Nouvelle requête (1)", "Suivi (2)", "Retour (3)"};
        this.setElements(new ArrayList<>(Arrays.asList(options)));
    }

    /**
     * Affiche le menu permettant à l'utilisateur de soumettre une requête pour un nouveau chantier.
     * @param scanner
     * @param user
     */
    public void showMenu(Scanner scanner, User user) {
        System.out.println("\nSoumettez une requête pour un nouveau chantier! Entrez le numéro entre parenthèses pour choisir une option:\n");
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
                handleNewRequest(scanner, user);
                break;

            case 2:
                handleSuivi(scanner, user);
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
     * Gère la soumission d'une nouvelle requête pour un chantier.
     * @param scanner
     * @param user
     */
    private void handleNewRequest(Scanner scanner, User user) {
        System.out.println("\nQuel est le titre du travail réalisé:\n");
        String titreDuTravail = scanner.nextLine();

        System.out.println("\nDonnez une description détaillée:\n");
        String description = scanner.nextLine();

        System.out.println("\nQuel est le type de travaux:\n");
        String typeDeTravaux = scanner.nextLine();

        String dateDebut;
        while (true) {
            System.out.println("\nQuel est la date de début espéré (dd/MM/yyyy):\n");
            dateDebut = scanner.nextLine();
            if (validateDate(dateDebut)) {
                break;
            } else {
                System.out.println("\nDate invalide. Elle doit être une date future (format: dd/MM/yyyy).\n");
            }
        }

        String quartier = user.getArrondissement();
        String resident = user.getNom();
        String intervenant = ""; // Default empty intervenant

        boolean success = submitRequestToApi(titreDuTravail, description, typeDeTravaux, dateDebut, quartier, resident, intervenant);
        if (success) {
            System.out.println("\nRequête soumise avec succès!\n");
        } else {
            System.out.println("\nÉchec de la soumission de la requête.\n");
        }
    }

    /**
     * Affiche la liste des requêtes associées à l'utilisateur et permet de consulter
     * les candidatures liées à une requête spécifique.
     * @param scanner
     * @param user
     */
    private void handleSuivi(Scanner scanner, User user) {
        String url = "http://localhost:7070/consulter-requetes";

        try {
            HttpResponse<String> response = fetchApiData(url);

            if (response.statusCode() == 200) {
                JSONArray requetes = new JSONArray(response.body());
                boolean hasRequetes = false;
                for (int i = 0; i < requetes.length(); i++) {
                    JSONObject requete = requetes.getJSONObject(i);
                    if (requete.getString("resident").equals(user.getNom())) {
                        hasRequetes = true;
                        System.out.println("\n=================================================");
                        System.out.println("ID de la requête: " + requete.getString("requeteId"));
                        System.out.println("Titre: " + requete.getString("titreDuTravail"));
                        System.out.println("Description: " + requete.getString("description"));
                        System.out.println("Type de travaux: " + requete.getString("typeDeTravaux"));
                        System.out.println("Date de début: " + requete.getString("dateDebut"));
                        System.out.println("Quartier: " + requete.getString("quartier"));
                        System.out.println("=================================================");
                    }
                }

                if (!hasRequetes) {
                    System.out.println("\nVous n'avez soumis aucune requête.");
                    return;
                }

                System.out.println("\nEntrez l'ID de la requête pour voir les candidatures:\n");
                String requeteId = scanner.nextLine();

                viewAndManageCandidatures(requeteId, scanner);
            } else {
                System.out.println("\nErreur lors de la récupération des requêtes.\n");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            System.out.println("\nErreur lors de la communication avec le serveur.\n");
        }
    }

    /**
     * Affiche et gère les candidatures associées à une requête spécifique.
     * Permet à l'utilisateur de consulter les candidatures pour une requête donnée,
     * et de prendre une décision pour une candidature spécifique.
     * @param requeteId
     * @param scanner
     */
    private void viewAndManageCandidatures(String requeteId, Scanner scanner) {
        String url = "http://localhost:7070/consulter-candidatures?requeteId=" + encodeValue(requeteId);
    
        try {
            HttpResponse<String> response = fetchApiData(url);
    
            if (response.statusCode() == 200) {
                JSONArray candidatures = new JSONArray(response.body());
    
                if (candidatures.isEmpty()) {
                    System.out.println("\nAucune candidature trouvée pour cette requête.\n");
                    return;
                }
    
                for (int i = 0; i < candidatures.length(); i++) {
                    JSONObject candidature = candidatures.getJSONObject(i);
                    System.out.println("\n=================================================");
                    System.out.println("Intervenant: " + candidature.getString("intervenant"));
                    System.out.println("Accepté: " + (candidature.getBoolean("accepted")? "Oui":"Non"));
                    System.out.println("=================================================");
                }
    
                System.out.println("\nEntrez le courriel de l'intervenant pour gérer sa candidature:\n");
                String intervenant = scanner.nextLine();
    
                if (!isValidIntervenantForRequete(requeteId, intervenant)) {
                    System.out.println("\nLe courriel fourni ne correspond à aucune candidature existante pour cette requête.\n");
                    return;
                }
    
                System.out.println("\nVoulez-vous accepter ou refuser cette candidature ? (accepter/refuser):\n");
                String decision = scanner.nextLine().trim();
    
                if (decision.equalsIgnoreCase("accepter")) {
                    boolean success = acceptCandidature(requeteId, intervenant);
                    if (success) {
                        System.out.println("\nCandidature acceptée avec succès!\n");
                    } else {
                        System.out.println("\nÉchec de l'acceptation de la candidature.\n");
                    }
                } else if (decision.equalsIgnoreCase("refuser")) {
                    boolean success = refuseCandidature(requeteId, intervenant);
                    if (success) {
                        System.out.println("\nCandidature refusée avec succès!\n");
                    } else {
                        System.out.println("\nÉchec du refus de la candidature.\n");
                    }
                } else {
                    System.out.println("\nAction invalide.\n");
                }
            } else {
                System.out.println("\nAucune candidature trouvée pour cette requête.\n");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            System.out.println("\nErreur lors de la récupération des candidatures.\n");
        }
    }

    /**
     * Vérifie si un intervenant donné a soumis une candidature pour une requête spécifique.
     * @param requeteId
     * @param intervenant
     * @return boolean
     */
    private boolean isValidIntervenantForRequete(String requeteId, String intervenant) {
        String url = "http://localhost:7070/consulter-candidatures?requeteId=" + encodeValue(requeteId);
    
        try {
            // Fetch the list of candidatures for the given request ID
            HttpResponse<String> response = fetchApiData(url);
            if (response.statusCode() == 200) {
                JSONArray candidatures = new JSONArray(response.body());
                for (int i = 0; i < candidatures.length(); i++) {
                    JSONObject candidature = candidatures.getJSONObject(i);
                    if (candidature.getString("intervenant").equalsIgnoreCase(intervenant)) {
                        return true;
                    }
                }
            } else {
                System.out.println("\nErreur lors de la récupération des candidatures. Code de réponse: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            System.out.println("\nErreur lors de la communication avec le serveur.");
        }
        return false;
    }

    /**
     * Soumet une requête pour une nouvelle requete via un appel à l'API.
     * @return booolean
     */
    private boolean submitRequestToApi(String titreDuTravail, String description, String typeDeTravaux, String dateDebut, String quartier, String resident, String intervenant) {
        String url = "http://localhost:7070/soumettre-requetes";
        HttpClient client = HttpClient.newHttpClient();

        String body = String.format(
                "titreDuTravail=%s&description=%s&typeDeTravaux=%s&dateDebut=%s&quartier=%s&resident=%s&intervenant=%s",
                encodeValue(titreDuTravail), encodeValue(description), encodeValue(typeDeTravaux), encodeValue(dateDebut), encodeValue(quartier), encodeValue(resident), encodeValue(intervenant)
        );

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
     * Accepte une candidature pour une requête spécifique via un appel à l'API.
     * @param requeteId
     * @param intervenant
     */
    private boolean acceptCandidature(String requeteId, String intervenant) {
        String url = "http://localhost:7070/accepter-candidature";
        HttpClient client = HttpClient.newHttpClient();

        String body = String.format("requeteId=%s&intervenant=%s", encodeValue(requeteId), encodeValue(intervenant));

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
     * Refuse une candidature pour une requête spécifique via un appel à l'API.
     * @param requeteId
     * @param intervenant
     */
    private boolean refuseCandidature(String requeteId, String intervenant) {
        String url = "http://localhost:7070/refuser-candidature";
        HttpClient client = HttpClient.newHttpClient();

        String body = String.format("requeteId=%s&intervenant=%s", encodeValue(requeteId), encodeValue(intervenant));

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
     * Effectue une requête GET à l'URL spécifiée et retourne la réponse de l'API.
     * @param url
     * @return
     * @throws IOException
     * @throws InterruptedException
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

    private String encodeValue(String value) {
        return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
    }

    /**
     * Valide une date
     * @param date
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