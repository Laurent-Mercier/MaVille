package ca.umontreal.restApi.controllers;

import ca.umontreal.restApi.csvManager.CsvManager;
import io.javalin.Javalin;
import org.json.JSONArray;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONObject;

public class CityController {
    private final CsvManager csvManager;

    public CityController(Javalin app) {
        this.csvManager = new CsvManager();

        app.get("/travaux-en-cours", ctx -> handleFetchData(ctx, "https://donnees.montreal.ca/api/3/action/datastore_search?resource_id=cc41b532-f12d-40fb-9f55-eb58c9a2b12b"));
        app.get("/entraves", ctx -> handleFetchData(ctx, "https://donnees.montreal.ca/api/3/action/datastore_search?resource_id=a2bc8014-488c-495d-941b-e7ae1999d1bd"));
        app.get("/projets_futurs", ctx -> handleFetchProjets(ctx));
        app.post("/soumettre-projet", ctx -> handleSubmitProjet(ctx));
        app.get("/getProjets", ctx -> handleGetProjets(ctx));
        app.post("/updateProjetStatus", ctx -> handleUpdateProjetStatus(ctx));
        app.post("/sendNotifications", ctx -> handleSendNotifications(ctx));
    }

    /**
     * Gère la récupération de données depuis une API et renvoie les résultats au client via le contexte Javalin.
     * @param ctx
     * @param url
     */
    private void handleFetchData(io.javalin.http.Context ctx, String url) {
        try {
            String responseBody = fetchDataFromApi(url);
            if (responseBody != null) {
                ctx.json(responseBody);
                ctx.status(200);
            } else {
                ctx.status(500).result("Erreur lors de la récupération des données.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Erreur lors de la récupération des données.");
        }
    }

    /**
     * Gère la récupération des projets depuis un fichier CSV et
     * renvoie les résultats au client via le contexte Javalin.
     * @param ctx
     */
    private void handleFetchProjets(io.javalin.http.Context ctx) {
        try {
            JSONArray jsonArray = csvManager.readProjetsFromCsv();
            ctx.result(jsonArray.toString());
            ctx.status(200);
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Erreur lors de la lecture des projets futurs.");
        }
    }

    /**
     * Gère la soumission d'un projet en recevant les paramètres du formulaire via le contexte Javalin.
     * @param ctx
     */
    private void handleSubmitProjet(io.javalin.http.Context ctx) {
        String intervenant = ctx.formParam("intervenant");
        String titreProjet = ctx.formParam("titreProjet");
        String description = ctx.formParam("description");
        String typeDeTravaux = ctx.formParam("typeDeTravaux");
        String quartiersAffecte = ctx.formParam("quartiersAffecte");
        String dateDebut = ctx.formParam("dateDebut");
        String dateFin = ctx.formParam("dateFin");
        String horaire = ctx.formParam("horaire");

        try {
            csvManager.writeProjetToCsv(intervenant, titreProjet, description, typeDeTravaux, quartiersAffecte, dateDebut, dateFin, horaire, "prévu");
            ctx.status(200).result("Projet soumis avec succès.");
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Erreur lors de la soumission du projet.");
        }
    }

    /**
     * Gère la récupération des projets en lisant les données depuis un fichier CSV et
     * les renvoie au client sous forme de JSON via le contexte Javalin.
     * @param ctx
     */
    private void handleGetProjets(io.javalin.http.Context ctx) {
        try {
            JSONArray jsonArray = csvManager.readProjetsFromCsv();
            ctx.result(jsonArray.toString());
            ctx.status(200);
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Erreur lors de la lecture des projets.");
        }
    }

    /**
     * Gère la mise à jour du statut d'un projet en recevant les paramètres via le contexte Javalin.
     * Le statut du projet est mis à jour dans le fichier CSV correspondant.
     * @param ctx
     */
    private void handleUpdateProjetStatus(io.javalin.http.Context ctx) {
        String projectIdStr = ctx.formParam("projectId");
        String newStatus = ctx.formParam("statut");
    
        System.out.println("Project ID: " + projectIdStr);
        System.out.println("Nouveau statut: " + newStatus);
    
        try {
            int projectId = Integer.parseInt(projectIdStr); // Parse project ID
            boolean updated = csvManager.updateProjetStatus(projectId, newStatus); // Update by ID
            System.out.println("Update success: " + updated);
            if (updated) {
                ctx.status(200).result("Statut mis à jour avec succès.");
            } else {
                ctx.status(404).result("Projet introuvable ou non autorisé.");
            }
        } catch (NumberFormatException e) {
            ctx.status(400).result("ID de projet invalide.");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Erreur lors de la mise à jour du statut.");
        }
    }

    /**
     * Gère l'envoi de notifications aux résidents des quartiers affectés par un projet.
     * Les notifications sont générées et enregistrées dans un fichier CSV.
     * @param ctx
     */
    private void handleSendNotifications(io.javalin.http.Context ctx) {
        String quartiersAffecte = ctx.formParam("quartiersAffecte");
        String titreProjet = ctx.formParam("titreProjet");
        String description = ctx.formParam("description");
    
        if (quartiersAffecte == null || titreProjet == null || description == null) {
            ctx.status(400).result("Paramètres manquants.");
            return;
        }
    
        try {
            JSONArray residents = csvManager.readResidentFromCSV();
            String[] quartiers = quartiersAffecte.split(",");
            int nextNotificationId = csvManager.getNextNotificationId();
    
            for (int i = 0; i < residents.length(); i++) {
                JSONObject resident = residents.getJSONObject(i);
                String residentArrondissement = resident.optString("arrondissement");
    
                for (String quartier : quartiers) {
                    if (residentArrondissement.equalsIgnoreCase(quartier.trim())) {
                        csvManager.writeNotificationToCsv(nextNotificationId++, resident.getString("courriel"), 
                            "Nouveau projet: " + titreProjet + " - " + description);
                        break;
                    }
                }
            }
    
            ctx.status(200).result("Notifications envoyées.");
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Erreur lors de l'envoi des notifications.");
        }
    }

    /**
     * Récupère les données depuis une API via une requête GET.
     */
    private String fetchDataFromApi(String url) throws Exception {
        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder().uri(URI.create(url)).header("Accept", "application/json").GET().build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return response.body();
        } else {
            System.err.println("Erreur: " + response.statusCode());
            return null;
        }
    }
}