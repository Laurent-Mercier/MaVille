package ca.umontreal.restApi.csvManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.Arrays;
public class CsvManager {

    private static String PROJETS_FILE_PATH = "src/main/resources/projets.csv";
    private static String RESIDENTS_FILE_PATH = "src/main/resources/residents.csv";
    private static String INTERVENANTS_FILE_PATH = "src/main/resources/intervenants.csv";
    private static String REQUETES_FILE_PATH = "src/main/resources/requetes.csv";
    private static String CANDIDATURES_FILE_PATH = "src/main/resources/candidatures.csv";
    private static  String NOTIFICATIONS_FILE_PATH = "src/main/resources/notifications.csv";

    public void setProjetsFilePath(String filePath) {
        PROJETS_FILE_PATH = filePath;
    }

    public void setResidentsFilePath(String filePath) {
        RESIDENTS_FILE_PATH = filePath;
    }

    public void setIntervenantsFilePath(String filePath) {
        INTERVENANTS_FILE_PATH = filePath;
    }

    public void setRequetesFilePath(String filePath) {
        REQUETES_FILE_PATH = filePath;
    }

    public void setCandidaturesFilePath(String filePath) {
        CANDIDATURES_FILE_PATH = filePath;
    }

    public void setNotificationsFilePath(String filePath) {
        NOTIFICATIONS_FILE_PATH = filePath;
    }

    /**
     * Écrit un nouvel utilisateur dans le fichier CSV approprié
     *
     * @param typeUsager le type de l'utilisateur ("resident" ou "intervenant").
     * @param nomComplet le nom complet de l'utilisateur.
     * @param dateNaissance la date de naissance de l'utilisateur (uniquement pour les résidents).
     * @param courriel l'adresse courriel de l'utilisateur.
     * @param password le mot de passe de l'utilisateur.
     * @param telephone le numéro de téléphone de l'utilisateur (uniquement pour les résidents).
     * @param adresse l'adresse de l'utilisateur (uniquement pour les résidents).
     * @param arrondissement l'arrondissement de l'utilisateur (uniquement pour les résidents).
     * @param horaire les préférences horaires de l'utilisateur (uniquement pour les résidents).
     * @param typeInter le type d'intervenant (uniquement pour les intervenants).
     * @param idVille l'identifiant de la ville de l'intervenant (uniquement pour les intervenants).
     */
    public void writeUserToCSV(String typeUsager, String nomComplet, String dateNaissance,
                               String courriel, String password, String telephone,
                               String adresse, String arrondissement, String horaire, String typeInter, String idVille) {
        String filePath = typeUsager.equals("resident") ? RESIDENTS_FILE_PATH : INTERVENANTS_FILE_PATH;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            String csvLine = typeUsager.equals("resident")
                    ? formatCsvLine(nomComplet, courriel, password, dateNaissance, telephone, adresse, arrondissement, horaire)
                    : formatCsvLine(nomComplet, courriel, password, typeInter, idVille);
            writer.write(csvLine);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Erreur lors de l'écriture dans le fichier CSV.");
            e.printStackTrace();
        }
    }

    /**
     * Écrit un nouveau projet dans le fichier CSV des projets.
     * @param intervenant l'adresse courriel de l'intervenant responsable du projet.
     * @param titreProjet le titre du projet.
     * @param description une description détaillée du projet.
     * @param typeDeTravaux le type de travaux associés au projet.
     * @param quartiersAffecte les quartiers affectés par le projet, séparés par des virgules.
     * @param dateDebut la date de début prévue du projet.
     * @param dateFin la date de fin prévue du projet.
     * @param horaire les horaires associés au projet.
     * @param statut le statut initial du projet
     */
    public void writeProjetToCsv(String intervenant, String titreProjet, String description, String typeDeTravaux, 
                             String quartiersAffecte, String dateDebut, String dateFin, String horaire, String statut) throws IOException {
        int id = getNextProjectId(); // Generate a unique ID
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(PROJETS_FILE_PATH, true))) {
            String csvLine = formatCsvLine(String.valueOf(id), titreProjet, description, typeDeTravaux, quartiersAffecte, 
                                        dateDebut, dateFin, horaire, statut, intervenant);
            writer.write(csvLine);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Erreur lors de l'écriture dans le fichier projets.csv.");
            throw e;
        }
    }
    /**
     * Lit toutes les requêtes depuis le fichier CSV des requêtes.
     * @return un tableau JSON contenant toutes les requêtes présentes dans le fichier CSV.
     */
    public JSONArray readRequestFromCsv() {
        return readCsvFile(REQUETES_FILE_PATH);
    }

    /**
     * Écrit une nouvelle requête dans le fichier CSV des requêtes.
     * @param titreDuTravail le titre du travail demandé.
     * @param description une description détaillée de la requête.
     * @param typeDeTravaux le type de travaux associés à la requête.
     * @param dateDebut la date de début prévue pour la réalisation de la requête.
     * @param quartier le quartier où la requête s'applique.
     * @param resident le nom du résident ayant soumis la requête.
     * @param intervenant l'intervenant associé à la requête (peut être vide).
     * @param requeteId l'identifiant unique de la requête.
     */
    public void writeRequestToCsv(String titreDuTravail, String description, String typeDeTravaux,
                                  String dateDebut, String quartier, String resident, String intervenant, int requeteId) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(REQUETES_FILE_PATH, true))) {
            String csvLine = formatCsvLine(
                    String.valueOf(requeteId), titreDuTravail, description, typeDeTravaux,
                    dateDebut, quartier, resident, intervenant
            );
            writer.write(csvLine);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Erreur lors de l'écriture dans le fichier requetes.csv.");
            throw e;
        }
    }
    /**
     * Lit toutes les candidatures depuis le fichier CSV des requêtes.
     * @return un tableau JSON contenant toutes les candidatures présentes dans le fichier CSV.
     */
    public JSONArray readCandidaturesFromCsv() {
        return readCsvFile(CANDIDATURES_FILE_PATH);
    }

    /**
     * Écrit ou met à jour une candidature dans le fichier CSV des candidatures.
     * @param requeteId l'identifiant de la requête associée à la candidature.
     * @param intervenant l'adresse courriel de l'intervenant soumettant la candidature.
     * @param accepted indique si la candidature est acceptée.
     * @throws IOException
     */
    public void writeCandidatureToCsv(String requeteId, String intervenant, boolean accepted) throws IOException {
        File tempFile = new File("src/main/resources/candidatures_temp.csv");
        boolean updated = false;
    
        try (BufferedReader reader = new BufferedReader(new FileReader(CANDIDATURES_FILE_PATH));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
    
            String header = reader.readLine();
            writer.write(header);
            writer.newLine();
    
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (fields.length >= 3 && fields[0].equals(requeteId) && fields[1].equals(intervenant)) {
                    fields[2] = String.valueOf(accepted); // Update the "accepted" field
                    updated = true;
                }
                writer.write(formatCsvLine(fields));
                writer.newLine();
            }

            // If the candidature wasn't found, append it
            if (!updated) {
                writer.write(formatCsvLine(requeteId, intervenant, String.valueOf(accepted)));
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de l'écriture dans le fichier candidatures.csv.");
            throw e;
        }
    
        if (!tempFile.renameTo(new File(CANDIDATURES_FILE_PATH))) {
            throw new IOException("Erreur lors du remplacement du fichier candidatures.csv.");
        }
    }

    /**
     * Supprime une candidature spécifique du fichier CSV des candidatures.
     * @param requeteId l'identifiant de la requête associée à la candidature.
     * @param intervenant l'adresse courriel de l'intervenant dont la candidature doit être supprimée.
     * @return boolean
     */
    public boolean removeCandidatureFromCsv(String requeteId, String intervenant) {
        File tempFile = new File("src/main/resources/candidatures_temp.csv");
        boolean found = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(CANDIDATURES_FILE_PATH));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            String header = reader.readLine();
            writer.write(header);
            writer.newLine();

            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (fields.length >= 2 && fields[0].equals(requeteId) && fields[1].equals(intervenant)) {
                    found = true;
                    continue; // Skip writing this line to the temp file
                }
                writer.write(formatCsvLine(fields));
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la suppression de la candidature dans candidatures.csv.");
            e.printStackTrace();
            return false;
        }

        if (found) {
            if (!tempFile.renameTo(new File(CANDIDATURES_FILE_PATH))) {
                System.err.println("Erreur lors du remplacement du fichier candidatures.csv.");
                return false;
            }
        } else {
            tempFile.delete();
        }

        return found;
    }
    /**
     * Lit toutes les notifications depuis le fichier CSV des notifications.
     * @return un tableau JSON contenant toutes les notifications présentes dans le fichier CSV.
     */
    public JSONArray readNotificationsFromCsv() {
        return readCsvFile(NOTIFICATIONS_FILE_PATH);
    }

    /**
     * Marque toutes les notifications d'un résident spécifique comme vues dans le fichier CSV des notifications.
     * @param resident l'adresse courriel du résident dont les notifications doivent être mises à jour.
     * @return
     */
    public boolean markNotificationsAsViewed(String resident) {
        File tempFile = new File("src/main/resources/notifications_temp.csv");
        boolean updated = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(NOTIFICATIONS_FILE_PATH));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            String header = reader.readLine();
            writer.write(header);
            writer.newLine();

            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (fields.length >= 3 && fields[1].equalsIgnoreCase(resident) && fields[2].equalsIgnoreCase("false")) {
                    fields[2] = "true"; // Mark as viewed
                    updated = true;
                }
                writer.write(formatCsvLine(fields));
                writer.newLine();
            }
            updated=true;
        } catch (IOException e) {
            System.err.println("Erreur lors de la mise à jour des notifications.");
            e.printStackTrace();
            return false;
        }

        if (updated) {
            if (!tempFile.renameTo(new File(NOTIFICATIONS_FILE_PATH))) {
                System.err.println("Erreur lors du remplacement du fichier notifications.csv.");
                return false;
            }
        } else {
            tempFile.delete();
        }

        return updated;
    }
    /**
     * Écrit une nouvelle notification dans le fichier CSV des notifications.
     * @param id l'identifiant unique de la notification.
     * @param resident l'adresse courriel du résident pour lequel la notification est destinée.
     * @param description la description du contenu de la notification.
     */
    public void writeNotificationToCsv(int id, String resident, String description) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(NOTIFICATIONS_FILE_PATH, true))) {
            String csvLine = formatCsvLine(String.valueOf(id), resident, "false", description); // Ensure 'false' is written for vue
            writer.write(csvLine);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Erreur lors de l'écriture dans le fichier notifications.csv.");
            e.printStackTrace();
        }
    }
    /**
     * Génère le prochain identifiant unique pour une nouvelle notification.
     * @return le prochain identifiant de notification à utiliser. Retourne 1 si le fichier est vide
     *         ou en cas d'erreur.
     */
    public int getNextNotificationId() {
        String notificationsFilePath = NOTIFICATIONS_FILE_PATH;
        try (BufferedReader reader = new BufferedReader(new FileReader(notificationsFilePath))) {
            String lastLine = null, line;
            while ((line = reader.readLine()) != null) {
                lastLine = line;
            }
            if (lastLine != null) {
                String[] fields = lastLine.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                return Integer.parseInt(fields[0]) + 1; // Increment the last ID
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
        return 1; // Default to ID 1 if the file is empty
    }

    /**
     * Génère le prochain identifiant unique pour un nouveau projet.
     * @return le prochain identifiant de projet à utiliser. Retourne 1 si le fichier est vide
     *         ou en cas d'erreur.
     */
    private int getNextProjectId() {
        try (BufferedReader reader = new BufferedReader(new FileReader(PROJETS_FILE_PATH))) {
            String lastLine = null, line;
            while ((line = reader.readLine()) != null) {
                lastLine = line;
            }
            if (lastLine != null) {
                String[] fields = lastLine.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                return Integer.parseInt(fields[0]) + 1; // Increment the last ID
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
        return 1; // Default to ID 1 if the file is empty
    }
    /**
     * Met à jour le statut d'un projet spécifique dans le fichier CSV des projets.
     *
     * @param projectId l'identifiant unique du projet à mettre à jour.
     * @param newStatus le nouveau statut du projet (par exemple, "en cours", "terminé").
     * @return vrai si succes
     */
    public boolean updateProjetStatus(int projectId, String newStatus) {
        File tempFile = new File("src/main/resources/projets_temp.csv");
        boolean updated = false;
    
        try (BufferedReader reader = new BufferedReader(new FileReader(PROJETS_FILE_PATH));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
    
            String header = reader.readLine();
            writer.write(header);
            writer.newLine();
    
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (fields.length > 0 && Integer.parseInt(fields[0]) == projectId) {
                    fields[fields.length - 2] = escapeCsv(newStatus); // Update the "statut" field
                    updated = true;
                }
                writer.write(formatCsvLine(fields));
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la mise à jour du statut dans projets.csv.");
            e.printStackTrace();
            return false;
        }
    
        if (updated) {
            if (!tempFile.renameTo(new File(PROJETS_FILE_PATH))) {
                System.err.println("Erreur lors du remplacement du fichier projets.csv.");
                return false;
            }
        } else {
            tempFile.delete();
        }
    
        return updated;
    }

    /**
     * Met à jour les préférences horaires d'un utilisateur résident dans le fichier CSV.
     * @param courriel l'adresse courriel de l'utilisateur dont les préférences doivent être mises à jour.
     * @param preferences les nouvelles préférences horaires au format "Jour=HH:MM->HH:MM;".
     * @return vrai si succes
     */
    public boolean updateUserPreferences(String courriel, String preferences) {
        File tempFile = new File("src/main/resources/residents_temp.csv");
        boolean updated = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(RESIDENTS_FILE_PATH));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            String header = reader.readLine();
            writer.write(header);
            writer.newLine();

            String[] headers = header.split(",");
            int horaireIndex = -1;

            // Find the index for the "horaire" field
            for (int i = 0; i < headers.length; i++) {
                if ("horaire".equalsIgnoreCase(headers[i].trim())) {
                    horaireIndex = i;
                    break;
                }
            }

            if (horaireIndex == -1) {
                throw new IllegalStateException("Header 'horaire' not found in CSV file.");
            }

            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

                // If this is the user, update their horaire
                if (fields.length > 1 && fields[1].trim().equalsIgnoreCase(courriel.trim())) {
                    if (fields.length <= horaireIndex) {
                        fields = Arrays.copyOf(fields, horaireIndex + 1);
                        fields[horaireIndex] = ""; // Initialize missing fields with an empty string
                    }
                    fields[horaireIndex] = escapeCsv(preferences);
                    updated = true;
                }

                writer.write(formatCsvLine(fields));
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la mise à jour des préférences.");
            e.printStackTrace();
            return false;
        }

        if (updated) {
            if (!tempFile.renameTo(new File(RESIDENTS_FILE_PATH))) {
                System.err.println("Erreur lors du remplacement du fichier residents.csv.");
                return false;
            }
        } else {
            tempFile.delete();
        }

        return updated;
    }

    /**
     * Vérifie si une adresse courriel est déjà utilisée dans les fichiers CSV des résidents ou des intervenants.
     * @param courriel l'adresse courriel à vérifier.
     * @return vrai is existe
     */
    public boolean isCourrielTaken(String courriel) {
        String[] paths = {RESIDENTS_FILE_PATH, INTERVENANTS_FILE_PATH};
        for (String path : paths) {
            if (isCourrielPresentInCsv(courriel, path)) {
                return true;
            }
        }
        return false;
    }
    /**
     * Vérifie si une adresse courriel est présente dans un fichier CSV spécifique.
     *
     * @param courriel l'adresse courriel à rechercher.
     * @param filePath le chemin du fichier CSV dans lequel effectuer la recherche.
     * @return vrai si existe
     */
    private boolean isCourrielPresentInCsv(String courriel, String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            reader.readLine(); // Read and skip the header
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (fields.length > 1 && fields[1].trim().equalsIgnoreCase(courriel.trim())) {
                    return true;
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la lecture du fichier CSV: " + filePath);
            e.printStackTrace();
        }
        return false;
    }
    /**
     * Lit tous les projets depuis le fichier CSV des projets.
     *
     * @return un tableau JSON contenant toutes les données des projets présentes dans le fichier CSV.
     */
    public JSONArray readProjetsFromCsv() {
        return readCsvFile(PROJETS_FILE_PATH);
    }

    /**
     * Lit tous les résidents depuis le fichier CSV des résidents.
     *
     * @return un tableau JSON contenant toutes les données des résidents présentes dans le fichier CSV.
     */
    public JSONArray readResidentFromCSV() {
        return readCsvFile(RESIDENTS_FILE_PATH);
    }
    /**
     * Lit tous les intervenants depuis le fichier CSV des intervenants.
     *
     * @return un tableau JSON contenant toutes les données des intervenants présentes dans le fichier CSV.
     */
    public JSONArray readIntervenantFromCSV() {
        return readCsvFile(INTERVENANTS_FILE_PATH);
    }
    /**
     * Lit tous les arrondissements depuis le fichier CSV des codes postaux.
     *
     * @return un tableau JSON contenant toutes les données des arrondissements présentes dans le fichier CSV.
     */
    public JSONArray readArrondissementFromCSV() {
        return readCsvFile("src/main/resources/codesPostaux.csv");
    }
    /**
     * Lit un fichier CSV et convertit chaque ligne en un objet JSON
     * @param filePath le chemin du fichier CSV à lire.
     * @return un tableau contenant les données
     */
    private JSONArray readCsvFile(String filePath) {
        JSONArray jsonArray = new JSONArray();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String[] headers = reader.readLine().split(",");
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                JSONObject jsonObject = new JSONObject();
                for (int i = 0; i < headers.length; i++) {
                    jsonObject.put(headers[i], i < fields.length ? fields[i].replaceAll("^\"|\"$", "") : "");
                }
                jsonArray.put(jsonObject);
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la lecture du fichier CSV.");
            e.printStackTrace();
        }
        return jsonArray;
    }
    /**
     * Formate les champs fournis en une seule ligne CSV.
     *
     * @param fields les champs à inclure dans la ligne CSV.
     * @return une ligne CSV formatée.
     */
    private String formatCsvLine(String... fields) {
        StringBuilder csvLine = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            csvLine.append(escapeCsv(fields[i]));
            if (i < fields.length - 1) {
                csvLine.append(",");
            }
        }
        return csvLine.toString();
    }
    /**
     * Échappe un champ pour qu'il respecte les règles du format CSV.
     * @param field le champ à échapper.
     * @return le champ correctement échappé pour le format CSV.
     */
    private String escapeCsv(String field) {
        if (field.contains(",") || field.contains("\"")) {
            field = field.replace("\"", "\"\""); // Escape double quotes
            return "\"" + field + "\""; // Surround with double quotes
        }
        return field;
    }
}