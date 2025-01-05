package ca.umontreal.menu;

import ca.umontreal.user.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class MenuConsulterTravaux extends Menu {
    /**
     * Liste des quartiers
     */
    private static final String[] QUARTIERS = {
        "Ahuntsic-Cartierville", "Anjou", "Côte-des-Neiges-Notre-Dame-de-Grâce",
        "LaSalle", "Lachine", "Le Plateau-Mont-Royal", "Le Sud-Ouest",
        "L'Île-Bizard-Sainte-Geneviève", "Mercier-Hochelaga-Maisonneuve",
        "Montréal-Nord", "Outremont", "Pierrefonds-Roxboro",
        "Rivière-des-Prairies-Pointe-aux-Trembles", "Rosemont-La Petite-Patrie",
        "Saint-Laurent", "Saint-Léonard", "Verdun", "Ville-Marie",
        "Villeray-Saint-Michel-Parc-Extension"
    };

    public MenuConsulterTravaux() {
        super("consulterTravaux");
        this.setElements(new ArrayList<>(Arrays.asList("Afficher les travaux (1)", "Retour au menu principal (2)")));
    }

    /**
     * Affiche le menu
     */
    @Override
    public void showMenu(Scanner scanner, User user) {
        System.out.println("\nConsultez la liste des travaux actuels et prévus! " +
                "Entrez le numéro entre parenthèses pour choisir une option:\n");
        for (String option : this.getElements()) {
            System.out.println(option);
        }
        System.out.println();
    }
    /**
     * Met à jour le menu en fonction du choix de l'utilisateur.
     */
    @Override
    public Menu updateMenu(int choix, User user, Scanner scanner) {
        switch (choix) {
            case 1:
                return handleConsulterTravaux(scanner);
            case 2:
                return new MenuPrincipal();
            default:
                System.out.println("\nEntrez un nombre valide (1 ou 2) pour choisir une option.\n");
                return this;
        }
    }
    /**
     * Gère la consultation des travaux en cours ou prévus pour les 3 prochains mois.
     * @param scanner le scanner pour la saisie utilisateur.
     * @return le menu actuel après l'exécution de l'action choisie.
     */
    private Menu handleConsulterTravaux(Scanner scanner) {
        String type = null;

        displayTravauxAndProjets(type);

        System.out.println("\nVoulez-vous voir les travaux en cours ou prévus pour les 3 prochains mois?\n" +
                "(1) En cours\n(2) Prévus\n(3) Revenir en arrière");
        int choixDeVision = validateChoiceInput(1, 3, scanner);

        if (choixDeVision == 1) {
            handleTravauxEnCours(scanner, type);
        } else if (choixDeVision == 2) {
            handleTravauxPrevus(scanner, type);
        }
        return this;
    }
    /**
     * Affiche les travaux et les projets disponibles.
     * @param type le type de travaux ou projets à afficher
     */
    private void displayTravauxAndProjets(String type) {
        HttpResponse<String> response = getTravauxApi();
        showTravaux(response.body(), type, 0);

        response = getProjetApi();
        showProjet(response.body(), type, 0);
    }
    /**
     * Gère l'affichage des travaux et projets en cours, avec une option pour filtrer par quartier.
     * @param scanner le scanner pour la saisie utilisateur.
     * @param type le type de filtre à appliquer
     */
    private void handleTravauxEnCours(Scanner scanner, String type) {
        System.out.println("\nTravaux en cours:\n");
    
        // Fetch travaux from city's API
        HttpResponse<String> travauxResponse = getTravauxApi();
        showTravaux(travauxResponse.body(), type, 0);
    
        // Fetch projets with "en cours" status
        HttpResponse<String> projetsResponse = getProjetApi();
        System.out.println("\nProjets en cours:\n");
        showProjet(projetsResponse.body(), "en cours", 2); // Filter for "en cours" status
    
        // Optional: Filter by quartier
        if (promptFilterByQuartier(scanner)) {
            String quartier = promptQuartierSelection(scanner);
    
            System.out.println("\nTravaux en cours pour " + quartier + ":\n");
            showTravaux(travauxResponse.body(), quartier, 1);
    
            System.out.println("\nProjets en cours pour " + quartier + ":\n");
            showProjet(projetsResponse.body(), quartier, 3); // Filter for "en cours" + quartier
        }
    }

    /**
     * Gère l'affichage  projets prévus, avec une option pour filtrer par quartier.
     * @param scanner le scanner pour la saisie utilisateur.
     * @param type le type de filtre à appliquer
     */
    private void handleTravauxPrevus(Scanner scanner, String type) {
        System.out.println("\nTravaux prévus:\n");
    
        // Fetch projets with "prévu" status
        HttpResponse<String> projetsResponse = getProjetApi();
        showProjet(projetsResponse.body(), "prévu", 1); // Filter for "prévu" status
    
        // Optional: Filter by quartier
        if (promptFilterByQuartier(scanner)) {
            String quartier = promptQuartierSelection(scanner);
            System.out.println("\nTravaux prévus pour " + quartier + ":\n");
            showProjet(projetsResponse.body(), quartier, 4); // Filter for "prévu" + quartier
        }
    }

    /**
     * Demande si l'utilisateur veut filtrer par quartier.
     * @param scanner le scanner pour la saisie utilisateur.
     * @return
     */
    private boolean promptFilterByQuartier(Scanner scanner) {
        System.out.println("\nVoulez-vous filtrer par quartier?\n(1) Oui\n(2) Revenir en arrière");
        return validateChoiceInput(1, 2, scanner) == 1;
    }

    /**
     * Guide l'utilisateur pour le choix des quartiers
     * @param scanner le scanner pour la saisie utilisateur.
     * @return le choix de l'utilisateur
     */
    private String promptQuartierSelection(Scanner scanner) {
        System.out.println("\nVoici une liste des quartiers disponibles pour filtrer:\n");
        for (String quartier : QUARTIERS) {
            System.out.println(quartier);
        }
        System.out.print("\nPar quel quartier voulez-vous filtrer: ");
        return scanner.nextLine();
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
     * Récupère les données des travaux en cours via un appel à l'API.
     * @return réponse HTTP
     */
    private HttpResponse<String> getTravauxApi() {
        return sendApiRequest("http://localhost:7070/travaux-en-cours");
    }
    /**
     * Récupère les données des projets futurs via un appel à l'API.
     * @return une réponse HTTP
     */
    private HttpResponse<String> getProjetApi() {
        return sendApiRequest("http://localhost:7070/projets_futurs");
    }

    /**
     * Envoie une requête GET à l'URL spécifiée.
     * @param url l'URL de la requete
     * @return une réponse HTTP
     */
    private HttpResponse<String> sendApiRequest(String url) {
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
     * Affiche la liste des travaux en fonction des données et du filtre spécifiés.
     * @param body contient les travaux
     * @param filter filtre utilisé
     * @param filterType type du filtre
     */
    public void showTravaux(String body, String filter, int filterType) {
        if (body == null) {
            System.out.println("\nErreur... Veuillez réessayer plus tard!\n");
            return;
        }
        JSONArray records = new JSONObject(body).getJSONObject("result").getJSONArray("records");
        records.forEach(record -> {
            JSONObject rec = (JSONObject) record;
            if (filterMatches(rec, filter, filterType)) {
                printDetails("Identifiant du travail", rec.optString("id"),
                        "Arrondissement", rec.optString("boroughid"),
                        "Status", rec.optString("currentstatus"),
                        "Motif du travail", rec.optString("reason_category"),
                        "Catégorie d'intervenant", rec.optString("submittercategory"),
                        "Nom de l'intervenant", rec.optString("organizationname"));
            }
        });
    }

    /**
     * Vérifie si un enregistrement correspond au filtre spécifié.
     * @param record enregistrement
     * @param filter filtre
     * @param filterType type de filtre
     * @return boolean
     */
    private boolean filterMatches(JSONObject record, String filter, int filterType) {
        if (filterType == 1) {
            return record.optString("boroughid").equalsIgnoreCase(filter);
        } else if (filterType == 2) {
            return record.optString("reason_category").equalsIgnoreCase(filter);
        }
        return true; // No filter applied
    }
    /**
    * Affiche la liste des projets en fonction des données et du filtre spécifiés.
    */
    public void showProjet(String body, String filter, int filterType) {
        if (body == null) {
            System.out.println("\nPas de projets correspondants.\n");
            return;
        }
        JSONArray projets = new JSONArray(body);
        projets.forEach(projet -> {
            JSONObject proj = (JSONObject) projet;
            String statut = proj.optString("statut", "").trim();
            String quartiersAffecte = proj.optString("quartiersAffecte", "").replace("\"", "").trim();

            boolean showAll = (filterType == 0); // Show all projects
            boolean showEnCours = (filterType == 2 && statut.equalsIgnoreCase("en cours")); // "en cours" only
            boolean showEnCoursAndQuartier = (filterType == 3 && statut.equalsIgnoreCase("en cours") && quartiersAffecte.contains(filter)); // "en cours" + quartier
            boolean showPrevu = (filterType == 1 && statut.equalsIgnoreCase("prévu")); // "prévu" only
            boolean showPrevuAndQuartier = (filterType == 4 && statut.equalsIgnoreCase("prévu") && quartiersAffecte.contains(filter)); // "prévu" + quartier

            // Determine if this project should be displayed based on the filter
            if (showAll || showEnCours || showEnCoursAndQuartier || showPrevu || showPrevuAndQuartier) {
                printDetails(
                    "Titre du projet", proj.optString("titreProjet", "N/A"),
                    "Description", proj.optString("description", "N/A"),
                    "Type de travaux", proj.optString("typeDeTravaux", "N/A"),
                    "Quartiers affectés", quartiersAffecte,
                    "Date de début", proj.optString("dateDebut", "N/A"),
                    "Date de fin", proj.optString("dateFin", "N/A"),
                    "Horaire", proj.optString("horaire", "N/A"),
                    "Statut", statut
                );
            }
        });
    }

    /**
     * Affiche les details
     * @param details details
     */
    private void printDetails(String... details) {
        System.out.println("\n=================================================");
        for (int i = 0; i < details.length; i += 2) {
            System.out.println(details[i] + ": " + details[i + 1]);
        }
        System.out.println("=================================================\n");
    }
}