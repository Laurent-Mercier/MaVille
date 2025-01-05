package ca.umontreal.restApi.controllers;

import ca.umontreal.restApi.csvManager.CsvManager;
import io.javalin.Javalin;
import io.javalin.http.Context;
import org.json.JSONArray;
import org.json.JSONObject;

public class UserController {
    private final CsvManager csvManager;

    public UserController(Javalin app) {
        this.csvManager = new CsvManager();

        app.post("/isCourrielTaken", this::handleIsCourrielTaken);
        app.post("/creer-compte", this::handleCreateAccount);
        app.get("/verifArrondissement", this::handleVerifyArrondissement);
        app.get("/getResidents", this::handleGetResidents);
        app.get("/getIntervenants", this::handleGetIntervenants);
        app.post("/updatePreferences", this::handleUpdatePreferences);
        app.get("/consulter-notifications", this::handleConsultNotifications);
        app.post("/marquer-notifications-vues", this::handleMarkNotificationsAsViewed);
    }

    /**
     * Vérifie si une adresse courriel est déjà utilisée.
     * @param ctx
     */
    private void handleIsCourrielTaken(Context ctx) {
        String courriel = ctx.formParam("courriel");
        try {
            if (!csvManager.isCourrielTaken(courriel)) {
                ctx.status(200).result("\nCourriel valide\n");
            } else {
                ctx.status(409).result("\nCourriel invalide\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("\nErreur lors de la vérification du courriel\n");
        }
    }

    /**
     * Gère la création d'un nouveau compte utilisateur.
     * @param ctx
     */
    private void handleCreateAccount(Context ctx) {
        String typeUsager = ctx.formParam("typeUsager");
        String nomComplet = ctx.formParam("nomComplet");
        String dateNaissance = ctx.formParam("dateNaissance");
        String courriel = ctx.formParam("courriel");
        String password = ctx.formParam("password");
        String telephone = ctx.formParam("telephone");
        String adresse = ctx.formParam("adresse");
        String arrondissement = ctx.formParam("arrondissement");
        String horaire = ctx.formParam("horaire");
        String typeInter = ctx.formParam("typeInter");
        String idVille = ctx.formParam("idVille");

        try {
            csvManager.writeUserToCSV(typeUsager, nomComplet, dateNaissance, courriel, password, telephone, adresse,
                    arrondissement, horaire, typeInter, idVille);
            ctx.status(201).result("\nCompte créé avec succès\n");
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("\nErreur lors de la création du compte\n");
        }
    }

    /**
     * Gère la vérification des arrondissements disponibles.
     * @param ctx
     */
    private void handleVerifyArrondissement(Context ctx) {
        try {
            JSONArray jsonArray = csvManager.readArrondissementFromCSV();
            ctx.result(jsonArray.toString());
            ctx.status(200);
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("\nErreur lors de la lecture des arrondissements\n");
        }
    }

    /**
     * Gère la récupération de la liste des résidents.
     * @param ctx
     */
    private void handleGetResidents(Context ctx) {
        try {
            JSONArray jsonArray = csvManager.readResidentFromCSV();
            ctx.result(jsonArray.toString());
            ctx.status(200);
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("\nErreur lors de la lecture des résidents\n");
        }
    }

    /**
     * Gère la récupération de la liste des Intervenants.
     * @param ctx
     */
    private void handleGetIntervenants(Context ctx) {
        try {
            JSONArray jsonArray = csvManager.readIntervenantFromCSV();
            ctx.result(jsonArray.toString());
            ctx.status(200);
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("\nErreur lors de la lecture des intervenants\n");
        }
    }

    /**
     * Gère la mise à jour des préférences horaires d'un utilisateur.
     * @param ctx
     */
    private void handleUpdatePreferences(Context ctx) {
        String courriel = ctx.formParam("courriel");
        String preferences = ctx.formParam("preferences");

        try {
            boolean updated = csvManager.updateUserPreferences(courriel, preferences);
            if (updated) {
                ctx.status(200).result("\nPréférences mises à jour avec succès\n");
            } else {
                ctx.status(404).result("\nUtilisateur introuvable\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("\nErreur lors de la mise à jour des préférences\n");
        }
    }

    /**
     * Gère la consultation des notifications d'un résident spécifique.
     * @param ctx
     */
    private void handleConsultNotifications(Context ctx) {
        String resident = ctx.queryParam("resident");

        if (resident == null || resident.isEmpty()) {
            ctx.status(400).result("\nParamètre 'resident' manquant ou vide\n");
            return;
        }

        try {
            JSONArray notifications = csvManager.readNotificationsFromCsv();
            JSONArray filteredNotifications = new JSONArray();

            for (int i = 0; i < notifications.length(); i++) {
                JSONObject notification = notifications.getJSONObject(i);
                if (resident.equalsIgnoreCase(notification.optString("resident"))) {
                    filteredNotifications.put(notification);
                }
            }

            if (filteredNotifications.isEmpty()) {
                ctx.status(404).result("\nAucune notification trouvée pour ce résident\n");
            } else {
                ctx.result(filteredNotifications.toString());
                ctx.status(200);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("\nErreur lors de la consultation des notifications\n");
        }
    }

    /**
     * Gère la mise à jour des notifications pour un résident spécifique en les marquant comme vues.
     * @param ctx
     */
    private void handleMarkNotificationsAsViewed(Context ctx) {
        String resident = ctx.formParam("resident");

        if (resident == null || resident.isEmpty()) {
            ctx.status(400).result("\nParamètre 'resident' manquant ou vide\n");
            return;
        }

        try {
            boolean updated = csvManager.markNotificationsAsViewed(resident);
            if (updated) {
                ctx.status(200).result("\nNotifications marquées comme vues\n");
            } else {
                ctx.status(404).result("\nAucune notification trouvée pour ce résident\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("\nErreur lors de la mise à jour des notifications\n");
        }
    }
}
