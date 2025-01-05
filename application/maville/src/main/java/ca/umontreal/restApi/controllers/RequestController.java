package ca.umontreal.restApi.controllers;

import ca.umontreal.restApi.csvManager.CsvManager;
import io.javalin.Javalin;
import io.javalin.http.Context;
import org.json.JSONArray;
import org.json.JSONObject;

public class RequestController {
    private final CsvManager csvManager;

    public RequestController(Javalin app) {
        this.csvManager = new CsvManager();

        app.post("/soumettre-requetes", this::handleSubmitRequest);
        app.get("/consulter-requetes", this::handleConsultRequests);
        app.get("/consulter-requetes-filtre", this::handleFilteredRequests); // New endpoint for filtering requests
        app.get("/consulter-candidatures", this::handleConsultCandidatures);
        app.post("/poser-candidature", this::handlePoseCandidature);
        app.post("/retirer-candidature", this::handleRetirerCandidature);
        app.post("/refuser-candidature", this::handleRefuserCandidature);
        app.post("/accepter-candidature", this::handleAcceptCandidature);
        app.get("/lister-candidatures", this::handleListerCandidatures);
    }

    /**
     * Gère la soumission d'une requête.
     * @param ctx
     */
    private void handleSubmitRequest(Context ctx) {
        String titreDuTravail = ctx.formParam("titreDuTravail");
        String description = ctx.formParam("description");
        String typeDeTravaux = ctx.formParam("typeDeTravaux");
        String dateDebut = ctx.formParam("dateDebut");
        String quartier = ctx.formParam("quartier");
        String resident = ctx.formParam("resident");
        String intervenant = ctx.formParam("intervenant");

        try {
            int nextRequeteId = getNextRequeteId();
            csvManager.writeRequestToCsv(titreDuTravail, description, typeDeTravaux, dateDebut, quartier, resident, intervenant, nextRequeteId);
            ctx.status(200).result("Requête soumise avec succès.");
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Erreur lors de la soumission de la requête");
        }
    }

    /**
     * Gère la liste des candidatures pour un intervenant spécifique.
     * @param ctx
     */
    private void handleListerCandidatures(Context ctx) {
        String intervenant = ctx.queryParam("intervenant");

        if (intervenant == null || intervenant.isEmpty()) {
            ctx.status(400).result("Paramètre 'intervenant' manquant ou vide.");
            return;
        }

        try {
            JSONArray candidatures = csvManager.readCandidaturesFromCsv();

            // Check if there are any candidatures at all
            if (candidatures.isEmpty()) {
                ctx.status(404).result("Aucune candidature n'a été soumise pour le moment.");
                return;
            }

            // Filter candidatures for the specific intervenant
            JSONArray filteredCandidatures = new JSONArray();

            for (int i = 0; i < candidatures.length(); i++) {
                JSONObject candidature = candidatures.getJSONObject(i);
                if (intervenant.equalsIgnoreCase(candidature.optString("intervenant"))) {
                    filteredCandidatures.put(candidature);
                }
            }

            // Respond if no candidatures exist for the specific intervenant
            if (filteredCandidatures.isEmpty()) {
                ctx.status(404).result("Aucune candidature trouvée pour cet intervenant.");
            } else {
                ctx.result(filteredCandidatures.toString());
                ctx.status(200);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Erreur lors de la lecture des candidatures.");
        }
    }

    /**
     * Gère la consultation des requêtes soumises.
     * @param ctx
     */
    private void handleConsultRequests(Context ctx) {
        try {
            JSONArray jsonArray = csvManager.readRequestFromCsv();
            ctx.result(jsonArray.toString());
            ctx.status(200);
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Erreur lors de la lecture des requêtes.");
        }
    }

    /**
     * Gère la récupération des requêtes filtrées par quartier.
     * @param ctx
     */
    private void handleFilteredRequests(Context ctx) {
        String quartier = ctx.queryParam("quartier");

        if (quartier == null || quartier.isEmpty()) {
            ctx.status(400).result("Paramètre 'quartier' manquant ou vide.");
            return;
        }

        try {
            JSONArray requests = csvManager.readRequestFromCsv();
            JSONArray filteredRequests = new JSONArray();

            for (int i = 0; i < requests.length(); i++) {
                JSONObject request = requests.getJSONObject(i);
                if (quartier.equalsIgnoreCase(request.optString("quartier"))) {
                    filteredRequests.put(request);
                }
            }

            if (filteredRequests.isEmpty()) {
                ctx.status(404).result("Aucune requête trouvée pour ce quartier.");
            } else {
                ctx.result(filteredRequests.toString());
                ctx.status(200);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Erreur lors de la lecture des requêtes filtrées.");
        }
    }

    /**
     * Gère la consultation des candidatures pour une requête spécifique.
     * @param ctx
     */
    private void handleConsultCandidatures(Context ctx) {
        String requeteId = ctx.queryParam("requeteId");
        if (requeteId == null || requeteId.isEmpty()) {
            ctx.status(400).result("ID de requête manquant.");
            return;
        }

        try {
            JSONArray candidatures = csvManager.readCandidaturesFromCsv();
            JSONArray filteredCandidatures = new JSONArray();

            for (int i = 0; i < candidatures.length(); i++) {
                JSONObject candidature = candidatures.getJSONObject(i);
                if (requeteId.equals(candidature.getString("requeteId"))) {
                    filteredCandidatures.put(candidature);
                }
            }

            if (filteredCandidatures.isEmpty()) {
                ctx.status(404).result("Aucune candidature trouvée pour cette requête.");
            } else {
                ctx.result(filteredCandidatures.toString());
                ctx.status(200);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Erreur lors de la lecture des candidatures.");
        }
    }

    /**
     * Gère la soumission d'une candidature pour une requête spécifique.
     * @param ctx
     */
    private void handlePoseCandidature(Context ctx) {
        String requeteId = ctx.formParam("requeteId");
        String intervenant = ctx.formParam("intervenant");

        if (requeteId == null || intervenant == null || requeteId.isEmpty() || intervenant.isEmpty()) {
            ctx.status(400).result("Données manquantes pour poser une candidature.");
            return;
        }

        try {
            csvManager.writeCandidatureToCsv(requeteId, intervenant, false);
            ctx.status(200).result("Candidature posée avec succès.");
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Erreur lors de la soumission de la candidature.");
        }
    }

    /**
     * Gère le retrait d'une candidature pour une requête spécifique.
     * @param ctx
     */
    private void handleRetirerCandidature(Context ctx) {
        String requeteId = ctx.formParam("requeteId");
        String intervenant = ctx.formParam("intervenant");

        if (requeteId == null || intervenant == null || requeteId.isEmpty() || intervenant.isEmpty()) {
            ctx.status(400).result("Données manquantes pour retirer une candidature.");
            return;
        }

        try {
            boolean success = csvManager.removeCandidatureFromCsv(requeteId, intervenant);
            if (success) {
                ctx.status(200).result("Candidature retirée avec succès.");
            } else {
                ctx.status(404).result("Candidature introuvable.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Erreur lors du retrait de la candidature.");
        }
    }

    /**
     * Gère le refus d'une candidature pour une requête spécifique.
     * @param ctx
     */
    private void handleRefuserCandidature(Context ctx) {
        String requeteId = ctx.formParam("requeteId");
        String intervenant = ctx.formParam("intervenant");
    
        if (requeteId == null || intervenant == null || requeteId.isEmpty() || intervenant.isEmpty()) {
            ctx.status(400).result("Données manquantes pour refuser une candidature.");
            return;
        }
    
        try {
            csvManager.writeCandidatureToCsv(requeteId, intervenant, false);
            ctx.status(200).result("Candidature refusée avec succès.");
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Erreur lors du refus de la candidature.");
        }
    }

    /**
     * Gère l'acceptation d'une candidature pour une requête spécifique.
     * @param ctx
     */
    private void handleAcceptCandidature(Context ctx) {
        String requeteId = ctx.formParam("requeteId");
        String intervenant = ctx.formParam("intervenant");
    
        if (requeteId == null || intervenant == null || requeteId.isEmpty() || intervenant.isEmpty()) {
            ctx.status(400).result("Données manquantes pour accepter une candidature.");
            return;
        }
    
        try {
            csvManager.writeCandidatureToCsv(requeteId, intervenant, true);
            ctx.status(200).result("Candidature acceptée avec succès.");
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Erreur lors de l'acceptation de la candidature.");
        }
    }

    /**
     * Génère le prochain identifiant unique pour une nouvelle requête.
     * @return
     */
    private int getNextRequeteId() {
        try {
            JSONArray requests = csvManager.readRequestFromCsv();
            int maxId = 0;
            for (int i = 0; i < requests.length(); i++) {
                JSONObject request = requests.getJSONObject(i);
                int currentId = request.optInt("requeteId", 0);
                if (currentId > maxId) {
                    maxId = currentId;
                }
            }
            return maxId + 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 1; // Start from 1 if there are no entries or an error occurs
        }
    }
}