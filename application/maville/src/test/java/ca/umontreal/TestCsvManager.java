package ca.umontreal;

import ca.umontreal.restApi.csvManager.CsvManager;
import org.json.JSONArray;
import org.junit.Test;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;


public class TestCsvManager {

    // Fonctionnalité: Créer un compte
    // Ce test permet de verifier que la création de résident au csv se fait correctement
    @Test
    public void testWriteUserToCSVSuccessResident() throws IOException {

        CsvManager csvManager = new CsvManager();
        // Arrange
        Path tempFile = Files.createTempFile("residents", ".csv");
        tempFile.toFile().deleteOnExit();
        csvManager.setResidentsFilePath(tempFile.toString());

        String expectedData = "guy bizz,guy.bizz@example.com,123,01/01/2000,123567890,123 pole nord,Ville-Marie,temp";

        String typeUsager = "resident";
        String nomComplet = "guy bizz";
        String dateNaissance = "01/01/2000";
        String courriel = "guy.bizz@example.com";
        String password = "123";
        String telephone = "123567890";
        String adresse = "123 pole nord";
        String arrondissement = "Ville-Marie";
        String horaire = "temp";

        // Act
        csvManager.writeUserToCSV(typeUsager, nomComplet, dateNaissance, courriel, password, telephone, adresse, arrondissement, horaire, null, null);

        // Assert
        String writtenData = Files.readString(tempFile).trim(); // Read after writing
        assertEquals(expectedData, writtenData.split("\n")[0]); // Compare only the first line
    }

    // Ce test permet de verifier que la création d'intervenant au csv se fait correctement
    @Test
    public void testWriteUserToCSVSuccessIntervenant() throws IOException {

        CsvManager csvManager = new CsvManager();
        // Arrange
        Path tempFile = Files.createTempFile("intervenants", ".csv");
        tempFile.toFile().deleteOnExit();
        csvManager.setIntervenantsFilePath(tempFile.toString());

        String typeUsager = "intervenant";
        String nomComplet = "Marie Curie";
        String dateNaissance = null; // Not applicable for intervenants
        String courriel = "marie.curie@example.com";
        String password = "456";
        String telephone = null; // Not applicable for intervenants
        String adresse = null; // Not applicable for intervenants
        String arrondissement = "breakingTest"; // Not applicable for intervenants
        String horaire = null; // Not applicable for intervenants
        String typeInter = "Consultant";
        String idVille = "1234";

        // Act
        csvManager.writeUserToCSV(typeUsager, nomComplet, dateNaissance, courriel, password, telephone, adresse, arrondissement, horaire, typeInter, idVille);

        // Assert
        String writtenData = Files.readString(tempFile);
        assertEquals("Marie Curie,marie.curie@example.com,456,Consultant,1234\n", writtenData);
    }
    // Ce test verifie que le formatting est correct avec des fields missing
    @Test
    public void testWriteUserToCsvMissingFields() throws IOException {

        CsvManager csvManager = new CsvManager();
        // Arrange
        Path tempFile = Files.createTempFile("residents", ".csv");
        tempFile.toFile().deleteOnExit();
        csvManager.setResidentsFilePath(tempFile.toString());

        String typeUsager = "resident";
        String nomComplet = "Jean Dupont";
        String dateNaissance = "15/07/1985";
        String courriel = "jean.dupont@example.com";
        String password = "789";
        String telephone = ""; // Missing
        String adresse = ""; // Missing
        String arrondissement = "Ahuntsic-Cartierville";
        String horaire = ""; // Missing

        // Act
        csvManager.writeUserToCSV(typeUsager, nomComplet, dateNaissance, courriel, password, telephone, adresse, arrondissement, horaire, null, null);

        // Assert
        String writtenData = Files.readString(tempFile);
        assertEquals("Jean Dupont,jean.dupont@example.com,789,15/07/1985,,,Ahuntsic-Cartierville,\n", writtenData);
    }

    // Fonctionnalité: Soumettre une requete de travail
    // test qui vérifie que la requête est bel et bien ajoutée au dossier des requetes
    @Test
    public void testWriteRequestToCsvSuccess() throws IOException {
        CsvManager csvManager = new CsvManager();

        // Arrange
        Path tempFile = Files.createTempFile("requetes", ".csv");
        tempFile.toFile().deleteOnExit();
        csvManager.setRequetesFilePath(tempFile.toString());

        String titreDuTravail = "Réparation de la route chez grand maman";
        String description = "Réparer les fissures sur la route.";
        String typeDeTravaux = "Infrastructure";
        String dateDebut = "01/01/2025";
        String quartier = "Ville-Marie";
        String resident = "Guy Bizz";
        String intervenant = "";
        int requeteId = 1;

        // Act
        csvManager.writeRequestToCsv(titreDuTravail, description, typeDeTravaux, dateDebut, quartier, resident, intervenant, requeteId);

        // Assert
        String writtenData = Files.readString(tempFile);
        String expectedLine = "1,Réparation de la route chez grand maman,Réparer les fissures sur la route.,Infrastructure,01/01/2025,Ville-Marie,Guy Bizz,";
        assertEquals(expectedLine, writtenData.trim().split("\n")[0]);
    }
    // Test qui voit si plusieurs soumissionn de requetes fonctionnent
    @Test
    public void testWriteMultipleRequestsToCsv() throws IOException {
        CsvManager csvManager = new CsvManager();

        // Arrange
        Path tempFile = Files.createTempFile("requetes", ".csv");
        tempFile.toFile().deleteOnExit();
        csvManager.setRequetesFilePath(tempFile.toString());

        //  1
        String titreDuTravail1 = "Réparation des trottoirs";
        String description1 = "Réparer les fissures sur les trottoirs.";
        String typeDeTravaux1 = "Maintenance";
        String dateDebut1 = "10/03/2025";
        String quartier1 = "Plateau-Mont-Royal";
        String resident1 = "Jean Dupont";
        String intervenant1 = "Intervenant2";
        int requeteId1 = 3;

        //  2
        String titreDuTravail2 = "Installation de lampadaires";
        String description2 = "Installer des lampadaires sur les nouvelles routes.";
        String typeDeTravaux2 = "Éclairage";
        String dateDebut2 = "01/04/2025";
        String quartier2 = "Rosemont";
        String resident2 = "Anne Martin";
        String intervenant2 = "";
        int requeteId2 = 4;

        // Act
        csvManager.writeRequestToCsv(titreDuTravail1, description1, typeDeTravaux1, dateDebut1, quartier1, resident1, intervenant1, requeteId1);
        csvManager.writeRequestToCsv(titreDuTravail2, description2, typeDeTravaux2, dateDebut2, quartier2, resident2, intervenant2, requeteId2);

        // Assert
        String writtenData = Files.readString(tempFile);
        String[] lines = writtenData.trim().split("\n");
        String expectedLine1 = "3,Réparation des trottoirs,Réparer les fissures sur les trottoirs.,Maintenance,10/03/2025,Plateau-Mont-Royal,Jean Dupont,Intervenant2";
        String expectedLine2 = "4,Installation de lampadaires,Installer des lampadaires sur les nouvelles routes.,Éclairage,01/04/2025,Rosemont,Anne Martin,";
        assertEquals(expectedLine1, lines[0]);
        assertEquals(expectedLine2, lines[1]);
    }

    //Fonctionnalité: Soumettre/Soustraire sa candidature
    // Ce test permet de mettre a jour une candidature
    @Test
    public void testWriteCandidatureToCsvExisting() throws IOException {
        CsvManager csvManager = new CsvManager();

        // Arrange
        Path tempFile = Files.createTempFile("candidatures", ".csv");
        tempFile.toFile().deleteOnExit();
        csvManager.setCandidaturesFilePath(tempFile.toString());

        String initialData = "requeteId,intervenant,accepted\n" +
                "1,jho.doe@example.com,false\n";
        Files.writeString(tempFile, initialData);

        String requeteId = "1";
        String intervenant = "jho.doe@example.com";
        boolean accepted = true;

        // Act
        csvManager.writeCandidatureToCsv(requeteId, intervenant, accepted);

        // Assert
        String writtenData = Files.readString(tempFile);
        String expectedData = "requeteId,intervenant,accepted\n" +
                "1,jho.doe@example.com,true\n";
        assertEquals(expectedData.trim(), writtenData.trim());
    }
    // Ce test permet d'ajouter le candidat s'il n'existe pas deja
    @Test
    public void testWriteCandidatureToCsv() throws IOException {
        CsvManager csvManager = new CsvManager();

        // Arrange
        Path tempFile = Files.createTempFile("candidatures", ".csv");
        tempFile.toFile().deleteOnExit();
        csvManager.setCandidaturesFilePath(tempFile.toString());

        String initialData = "requeteId,intervenant,accepted\n" +
                "1,jho.doe@example.com,false\n";
        Files.writeString(tempFile, initialData);

        String requeteId = "2";
        String intervenant = "jess.doe@example.com";
        boolean accepted = true;

        // Act
        csvManager.writeCandidatureToCsv(requeteId, intervenant, accepted);

        // Assert
        String writtenData = Files.readString(tempFile);
        String expectedData = "requeteId,intervenant,accepted\n" +
                "1,jho.doe@example.com,false\n" +
                "2,jess.doe@example.com,true\n";
        assertEquals(expectedData.trim(), writtenData.trim());
    }
    // Ce test permet de verifier qu'on enleve une candidature
    @Test
    public void testRemoveCandidatureFromCsv() throws IOException {
        CsvManager csvManager = new CsvManager();

        // Arrange
        Path tempFile = Files.createTempFile("candidatures", ".csv");
        tempFile.toFile().deleteOnExit();
        csvManager.setCandidaturesFilePath(tempFile.toString());

        String initialData = "requeteId,intervenant,accepted\n" +
                "1,jho.doe@example.com,false\n" +
                "2,jess.doe@example.com,true\n";
        Files.writeString(tempFile, initialData);

        String requeteId = "1";
        String intervenant = "jho.doe@example.com";

        // Act
        boolean result = csvManager.removeCandidatureFromCsv(requeteId, intervenant);

        // Assert
        String writtenData = Files.readString(tempFile);
        String expectedData = "requeteId,intervenant,accepted\n" +
                "2,jess.doe@example.com,true\n";
        assertTrue(result);
        assertEquals(expectedData.trim(), writtenData.trim());
    }

    //Fonctionnalite: Modifier ses préférences horaires
    //Ce test verifie que la modification de preferences au csv
    @Test
    public void testUpdateUserPreferencesSuccess() throws IOException {
        CsvManager csvManager = new CsvManager();

        // Arrange
        Path tempFile = Files.createTempFile("residents", ".csv");
        tempFile.toFile().deleteOnExit();
        Files.writeString(tempFile, "name,courriel,password,horaire\njho Doe,jho.doe@example.com,1234,\n");
        csvManager.setResidentsFilePath(tempFile.toString());

        String courriel = "jho.doe@example.com";
        String newPreferences = "Monday=08:00->16:00";

        // Act
        boolean result = csvManager.updateUserPreferences(courriel, newPreferences);

        // Assert
        String updatedData = Files.readString(tempFile);
        assertTrue(result);
        assertEquals("jho Doe,jho.doe@example.com,1234,Monday=08:00->16:00", updatedData.split("\n")[1]);
    }
    // Ce test verifie ce qui arrive lorsque utilisateur non trouve
    @Test
    public void testUpdateUserPreferencesUserNotFound() throws IOException {
        CsvManager csvManager = new CsvManager();

        // Arrange
        Path tempFile = Files.createTempFile("residents", ".csv");
        tempFile.toFile().deleteOnExit();
        Files.writeString(tempFile, "name,courriel,password,horaire\nJane Doe,jane.doe@example.com,5678,\n");
        csvManager.setResidentsFilePath(tempFile.toString());

        String courriel = "jho.doe@example.com";
        String newPreferences = "Monday=08:00->16:00";

        // Act
        boolean result = csvManager.updateUserPreferences(courriel, newPreferences);

        // Assert
        String unchangedData = Files.readString(tempFile);
        assertFalse(result);
        assertEquals("name,courriel,password,horaire\nJane Doe,jane.doe@example.com,5678,", unchangedData.trim());
    }
    // Ce test verifie qu'une erreur est lancée si le header n'est pass dans le file
    @Test
    public void testUpdateUserPreferencesMissingHoraireColumn() throws IOException {
        CsvManager csvManager = new CsvManager();

        // Arrange
        Path tempFile = Files.createTempFile("residents", ".csv");
        tempFile.toFile().deleteOnExit();
        Files.writeString(tempFile, "name,courriel,password\njho Doe,jho.doe@example.com,1234\n");
        csvManager.setResidentsFilePath(tempFile.toString());

        String courriel = "jho.doe@example.com";
        String newPreferences = "Monday=08:00->16:00";

        // Act
        //Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            csvManager.updateUserPreferences(courriel, newPreferences);
        });
        // Assert
        assertEquals("Header 'horaire' not found in CSV file.", exception.getMessage());
    }

    // Fonctionnalité: Voir ses notifications
    // test si les notifications peuvent être lue de manière adéquate
    @Test
    public void testReadNotificationsFromCsv() throws IOException {
        CsvManager csvManager = new CsvManager();

        // Arrange
        Path tempFile = Files.createTempFile("notifications", ".csv");
        tempFile.toFile().deleteOnExit();
        csvManager.setNotificationsFilePath(tempFile.toString());


        String testData = "id,resident,vue,description\n" +
                "1,jho.doe@example.com,false,Test notification 1\n" +
                "2,jess.doe@example.com,true,Test notification 2\n";
        Files.writeString(tempFile, testData);

        // Act
        JSONArray notifications = csvManager.readNotificationsFromCsv();

        // Assert
        assertEquals(2, notifications.length());
        assertEquals("jho.doe@example.com", notifications.getJSONObject(0).getString("resident"));
        assertEquals("false", notifications.getJSONObject(0).getString("vue"));
        assertEquals("Test notification 1", notifications.getJSONObject(0).getString("description"));
    }
    // Test si les notifications sont marqués comme vue
    @Test
    public void testMarkNotificationsAsViewed() throws IOException {
        CsvManager csvManager = new CsvManager();

        // Arrange
        Path tempFile = Files.createTempFile("notifications", ".csv");
        tempFile.toFile().deleteOnExit();
        csvManager.setNotificationsFilePath(tempFile.toString());

        String testData = "id,resident,vue,description\n" +
                "1,jho.doe@example.com,false,Test notification 1\n" +
                "2,jess.doe@example.com,false,Test notification 2\n";
        Files.writeString(tempFile, testData);

        // Act
        boolean updated = csvManager.markNotificationsAsViewed("jho.doe@example.com");

        // Assert
        assertTrue(updated);
        String updatedData = Files.readString(tempFile);
        assertTrue(updatedData.contains("1,jho.doe@example.com,true,Test notification 1"));
        assertTrue(updatedData.contains("2,jess.doe@example.com,false,Test notification 2"));
    }
    // test que la generation de id est correcte
    @Test
    public void testGetNextNotificationId() throws IOException {
        CsvManager csvManager = new CsvManager();

        // Arrange
        Path tempFile = Files.createTempFile("notifications", ".csv");
        tempFile.toFile().deleteOnExit();
        csvManager.setNotificationsFilePath(tempFile.toString());
        csvManager.setNotificationsFilePath(tempFile.toString());

        String testData = "id,resident,vue,description\n" +
                "1,jho.doe@example.com,false,Notification 1\n" +
                "2,jess.doe@example.com,true,Notification 2\n";
        Files.writeString(tempFile, testData);

        // Act
        int nextId = csvManager.getNextNotificationId();

        // Assert
        assertEquals(3, nextId);
    }

}
