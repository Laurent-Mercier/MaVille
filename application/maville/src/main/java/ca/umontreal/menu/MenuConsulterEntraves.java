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

public class MenuConsulterEntraves extends Menu {

    public MenuConsulterEntraves() {
        super("Consulter Entraves");
        this.setElements(new ArrayList<>(Arrays.asList(
            "Afficher les entraves (1)",
            "Retour au menu (2)"
        )));
    }
    /**
     * Affiche le menu
     */
    public void showMenu(Scanner scanner, User user) {
        System.out.println("\nConsultez la liste des entraves actuelles! " +
                           "Entrez le numéro entre parenthèses pour choisir une option:\n");
        for (String option : this.getElements()) {
            System.out.println(option);
        }
        System.out.println();
    }

    /**
     * Met à jour le menu en fonction du choix de l'utilisateur pour afficher ou filtrer
     * les entraves, ou revenir au menu principal.
     * @param choix le choix de l'utilisateur
     * @param user l'utilisateur
     * @param scanner le scanner pour la saisie
     * @return le menu suivant après l'exécution de l'action choisie
     */
    public Menu updateMenu(int choix, User user, Scanner scanner) {
        HttpResponse<String> response = getEntravesApi();

        switch (choix) {
            case 1:
                System.out.println("\nListe des entraves:\n");
                showEntraves(response.body(), null, 0);

                System.out.println("\nVoulez-vous filtrer par identifiant ou rue?\n" +
                                   "(1) Par identifiant\n" +
                                   "(2) Par rue\n" +
                                   "(3) Revenir en arrière");
                int choixFiltre = validateChoiceInput(1,3,scanner);

                if (choixFiltre == 1) {
                    System.out.println("\nPar quel identifiant de travail voulez-vous filtrer: \n");
                    String idTravail = scanner.nextLine();
                    System.out.println("\nChargement...\n");
                    showEntraves(response.body(), idTravail, 1);
                } else if (choixFiltre == 2) {
                    System.out.println("\nPar quelle rue voulez-vous filtrer: \n");
                    String rue = scanner.nextLine();
                    System.out.println("\nChargement...\n");
                    showEntraves(response.body(), rue, 2);
                } else if (choixFiltre == 3) {
                    return this;
                }
                break;

            case 2:
                return new MenuPrincipal();

            default:
                System.out.println("\nEntrez un nombre valide (1 ou 2) pour choisir une option.\n");
                return this;
        }
        return this;
    }

    /**
     * Envoie une requête GET à l'API des entraves pour récupérer les données.
     * @return une réponse HTTP contenant les données des entraves.
     */
    private HttpResponse<String> getEntravesApi() {
        String url = "http://localhost:7070/entraves";
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Accept", "application/json")
            .GET()
            .build();

        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Affiche la liste des entraves, avec ou sans filtrage, en fonction des paramètres fournis.
     * @param body le corps de la réponse contenant les données
     * @param filter le critère de filtrage
     * @param filterType le type de filtre à appliquer
     */
    public void showEntraves(String body, String filter, int filterType) {
        System.out.println("\nListe des entraves:\n");

        if (body == null) {
            System.out.println("\nErreur... veuillez revenir plus tard!\n");
            return;
        }

        JSONObject data = new JSONObject(body);
        JSONArray recordsArray = data.getJSONObject("result").getJSONArray("records");

        for (int i = 0; i < recordsArray.length(); i++) {
            JSONObject record = recordsArray.getJSONObject(i);
            String identifiantDuTravail = record.optString("id_request");
            String identifiantDeRue = record.optString("streetid");
            String nomDeRue = record.optString("shortname");
            String impactDuTravail = record.optString("streetimpacttype");

            boolean matchesFilter = (filterType == 0) ||
                                    (filterType == 1 && identifiantDuTravail.equals(filter)) ||
                                    (filterType == 2 && nomDeRue.equals(filter));

            if (matchesFilter) {
                printDetailsEntraves(identifiantDuTravail, identifiantDeRue, nomDeRue, impactDuTravail);
            }
        }
    }

    /**
     * Affiche les détails d'une entrave.
     * @param identifiantDuTravail
     * @param identifiantDeRue
     * @param nomDeRue
     * @param impactDuTravail
     */
    private void printDetailsEntraves(String identifiantDuTravail, String identifiantDeRue,
                                      String nomDeRue, String impactDuTravail) {
        System.out.println("\n=================================================");
        System.out.println("Identifiant du travail: " + identifiantDuTravail);
        System.out.println("Identifiant de rue: " + identifiantDeRue);
        System.out.println("Nom de rue: " + nomDeRue);
        System.out.println("Impact du travail: " + impactDuTravail);
        System.out.println("=================================================\n");
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