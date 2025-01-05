package ca.umontreal.menu;

import ca.umontreal.user.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Scanner;
import java.util.StringJoiner;
import java.util.regex.Pattern;

public class MenuCreerUnCompte extends Menu {

    public MenuCreerUnCompte() {
        super("creerUnCompte");
    }

    /**
     * Affiche le menu
     * @param scanner le scanner pour la saisie
     * @param user utilisateur
     */
    public void showMenu(Scanner scanner, User user) {
        System.out.println("\nCréer un compte\n\n");

        // Select user type
        String typeUsager = selectUserType(scanner);

        // Collect user details
        String nomComplet = collectFullName(scanner);
        String dateNaissance = typeUsager.equals("resident") ? collectDateOfBirth(scanner) : "";
        String courriel = collectEmail(scanner);
        String password = collectPassword(scanner);
        String telephone = typeUsager.equals("resident") ? collectPhone(scanner) : "";
        String adresse = "";
        String arrondissement = "";
        String horaire = "";

        if (typeUsager.equals("resident")) {
            String[] addressDetails = collectAddress(scanner);
            adresse = addressDetails[0];
            arrondissement = addressDetails[1];
        }

        String typeInter = "";
        String idVille = "";

        if (typeUsager.equals("intervenant")) {
            typeInter = selectIntervenantType(scanner);
            idVille = collectCityId(scanner);
        }

        // Register user
        createAccountApiCall(typeUsager, nomComplet, dateNaissance, courriel, password, telephone, adresse,
                arrondissement, horaire, typeInter, idVille);

        System.out.println("\nVotre compte a été créé avec succès! Vous pouvez maintenant vous connecter à MaVille.\n");
    }

    /**
     * Permet à l'utilisateur de sélectionner son type d'inscription : résident ou intervenant.
     *
     * @param scanner le scanner pour capturer la saisie
     * @return l'utilisateur
     */
    private String selectUserType(Scanner scanner) {
        System.out.println("\nVous inscrivez-vous en tant que résident (1) ou en tant qu'intervenant (2)?\n");
        while (true) {
            try {
                int choix = Integer.parseInt(scanner.nextLine());
                if (choix == 1 || choix == 2) {
                    return choix == 1 ? "resident" : "intervenant";
                } else {
                    System.out.println("\nOption invalide. Veuillez entrer 1 pour résident ou 2 pour intervenant.\n");
                }
            } catch (NumberFormatException e) {
                System.out.println("\nVeuillez entrer un numéro valide.\n");
            }
        }
    }

    /**
     * Collecte et valide le nom de l'utilisateur
     * @param scanner le scanner pour capturer la saisie
     * @return nom de l'utilisateur
     */
    private String collectFullName(Scanner scanner) {
        System.out.println("\nEntrez votre nom complet:\n");
        while (true) {
            String nomComplet = scanner.nextLine();
            if (nomComplet.matches("[A-Za-zÀ-ÿ'\\- ]{2,50}")) {
                return nomComplet;
            } else {
                System.out.println("\nFormat du nom incorrect. Veuillez réessayer.\n");
            }
        }
    }

    /**
     * Collecte et valide la date de naissance de l'utilisateur
     *  @param scanner le scanner pour capturer la saisie
     * @return la date formattée
     */
    private String collectDateOfBirth(Scanner scanner) {
        System.out.println("\nEntrez votre date de naissance (jj/mm/aaaa):\n");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
        LocalDate minimumDateOfBirth = LocalDate.now().minusYears(16);

        while (true) {
            try {
                LocalDate date = LocalDate.parse(scanner.nextLine(), formatter);
                if (date.isAfter(minimumDateOfBirth)) {
                    System.out.println("\nVous devez avoir au moins 16 ans pour créer un compte.\n");
                } else if (date.getYear() <= 1900) {
                    System.out.println("\nAnnée invalide. Veuillez entrer une année à partir de 1901.\n");
                } else {
                    return date.format(formatter);
                }
            } catch (DateTimeParseException e) {
                System.out.println("\nDate invalide. Veuillez réessayer.\n");
            }
        }
    }
    /**
     * Collecte et valide l'adresse courriel de l'utilisateur.
     * @param scanner le scanner pour capturer la saisie
     * @return  l'adresse courriel
     */
    private String collectEmail(Scanner scanner) {
        System.out.println("\nEntrez votre adresse courriel:\n");
        while (true) {
            String courriel = scanner.nextLine();
            if (Pattern.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", courriel)) {
                if (verifCourrielApiCall(courriel)) {
                    return courriel;
                } else {
                    System.out.println("\nLe courriel est déjà utilisé. Veuillez en choisir un autre.\n");
                }
            } else {
                System.out.println("\nFormat d'adresse courriel invalide. Veuillez réessayer.\n");
            }
        }
    }

    /**
     * Collecte et valide le mot de passe de l'utilisateur
     * @param scanner le scanner pour capturer la saisie
     * @return le mot de passe
     */
    private String collectPassword(Scanner scanner) {
        System.out.println("\nEntrez votre mot de passe (au moins 8 caractères, dont une majuscule et un chiffre):\n");
        while (true) {
            String password = scanner.nextLine();
            if (isValidPassword(password)) {
                return password;
            } else {
                System.out.println("\nLe mot de passe ne respecte pas les critères. Veuillez réessayer.\n");
            }
        }
    }

    /**
     * Collecte et valide le numero de l'utilisateur
     * @param scanner le scanner pour capturer la saisie
     * @return le numero de l'utilisateur
     */
    private String collectPhone(Scanner scanner) {
        System.out.println("\nVoulez-vous ajouter votre numéro de téléphone ? Entrez 1 pour oui, et 2 pour non:\n");
        while (true) {
            try {
                int choix = Integer.parseInt(scanner.nextLine());
                if (choix == 1) {
                    System.out.println("\nEntrez votre numéro de téléphone (xxx-xxx-xxxx):\n");
                    while (true) {
                        String telephone = scanner.nextLine();
                        if (Pattern.matches("\\d{3}-\\d{3}-\\d{4}", telephone)) {
                            return telephone;
                        } else {
                            System.out.println("\nFormat de téléphone invalide. Réessayez.\n");
                        }
                    }
                } else if (choix == 2) {
                    return "";
                } else {
                    System.out.println("\nOption invalide. Entrez 1 pour oui ou 2 pour non.\n");
                }
            } catch (NumberFormatException e) {
                System.out.println("\nVeuillez entrer un numéro valide.\n");
            }
        }
    }

    /**
     * Collecte et valide l'adresse
     * @param scanner le scanner pour capturer la saisie
     * @return l'adresse
     */
    private String[] collectAddress(Scanner scanner) {
        System.out.println("\nEntrez votre adresse résidentielle à Montréal (format: numéro rue, code postal):\n");
        while (true) {
            String adresse = scanner.nextLine();
            String[] parts = adresse.split(",");
            if (parts.length > 1 && parts[1].trim().length() >= 6) {
                String codePostal = parts[1].trim().substring(0, 3).toUpperCase();
                String arrondissement = verifCodePostalApiCall(codePostal);
                if (!arrondissement.isEmpty()) {
                    return new String[]{adresse, arrondissement};
                } else {
                    System.out.println("\nLe code postal n'est pas un code postal de Montréal. Réessayez.\n");
                }
            } else {
                System.out.println("\nFormat d'adresse invalide. Réessayez.\n");
            }
        }
    }

    /**
     * Collecte et valide le type de l'intervenant
     * @param scanner pour la saisie
     * @return le type de l'intervenant
     */
    private String selectIntervenantType(Scanner scanner) {
        System.out.println("\nQuel type d'intervenant êtes-vous?\n1. Entreprise publique\n2. Entrepreneur privé\n3. Particulier\n");
        while (true) {
            try {
                int choix = Integer.parseInt(scanner.nextLine());
                switch (choix) {
                    case 1:
                        return "entreprisePublique";
                    case 2:
                        return "entrepreneurPrive";
                    case 3:
                        return "particulier";
                    default:
                        System.out.println("\nOption invalide. Entrez un nombre entre 1 et 3.\n");
                }
            } catch (NumberFormatException e) {
                System.out.println("\nVeuillez entrer un numéro valide.\n");
            }
        }
    }

    /**
     * Collecte le id de la ville de l'intervenantt
     * @param scanner poyr la saisie
     * @return le id de la ville
     */
    private String collectCityId(Scanner scanner) {
        System.out.println("\nEntrez votre identifiant de la ville (8 chiffres):\n");
        while (true) {
            String idVille = scanner.nextLine();
            if (Pattern.matches("\\d{8}", idVille)) {
                return idVille;
            } else {
                System.out.println("\nIdentifiant invalide. Veuillez entrer un identifiant à 8 chiffres.\n");
            }
        }
    }

    /**
     * Vérifie si un mot de passe est valide selon les critères définis.
     * @param password mot de passe a verifier
     * @return validité du mot de passe
     */
    private boolean isValidPassword(String password) {
        return password.length() >= 8 &&
                Pattern.compile("[A-Z]").matcher(password).find() &&
                Pattern.compile("[0-9]").matcher(password).find();
    }

    private boolean verifCourrielApiCall(String courriel) {
        String url = "http://localhost:7070/isCourrielTaken";
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("courriel=" + URLEncoder.encode(courriel, StandardCharsets.UTF_8)))
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Vérifie si un code postal correspond à un arrondissement avec une API.
     * @param codePostal code postal a valider.
     * @return arrondissement
     */
    private String verifCodePostalApiCall(String codePostal) {
        String url = "http://localhost:7070/verifArrondissement";
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONArray arr = new JSONArray(response.body());
            for (int i = 0; i < arr.length(); i++) {
                JSONObject jsonObject = arr.getJSONObject(i);
                if (jsonObject.has("codePostal") && jsonObject.getString("codePostal").equals(codePostal)) {
                    return jsonObject.getString("arrondissement");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return "";
    }

    /**
     * Http request qui créer un compte
     * @param typeUsager le type de l'usager
     * @param nomComplet le nom de l'usager
     * @param dateNaissance date de naissance
     * @param courriel courriel
     * @param password mot de passe
     * @param telephone telephone
     * @param adresse adresse
     * @param arrondissement arondissement
     * @param horaire horaire
     * @param typeInter type de l'intervenant
     * @param idVille id de ville de l'interveant
     */
    private void createAccountApiCall(String typeUsager, String nomComplet, String dateNaissance,
                                       String courriel, String password, String telephone,
                                       String adresse, String arrondissement, String horaire, String typeInter, String idVille) {
        String url = "http://localhost:7070/creer-compte";
        HttpClient client = HttpClient.newHttpClient();
        StringJoiner formParams = new StringJoiner("&");
        formParams.add("typeUsager=" + URLEncoder.encode(typeUsager, StandardCharsets.UTF_8));
        formParams.add("nomComplet=" + URLEncoder.encode(nomComplet, StandardCharsets.UTF_8));
        formParams.add("dateNaissance=" + URLEncoder.encode(dateNaissance, StandardCharsets.UTF_8));
        formParams.add("courriel=" + URLEncoder.encode(courriel, StandardCharsets.UTF_8));
        formParams.add("password=" + URLEncoder.encode(password, StandardCharsets.UTF_8));
        formParams.add("telephone=" + URLEncoder.encode(telephone, StandardCharsets.UTF_8));
        formParams.add("adresse=" + URLEncoder.encode(adresse, StandardCharsets.UTF_8));
        formParams.add("arrondissement=" + URLEncoder.encode(arrondissement, StandardCharsets.UTF_8));
        formParams.add("horaire=" + URLEncoder.encode(horaire, StandardCharsets.UTF_8));
        formParams.add("typeInter=" + URLEncoder.encode(typeInter, StandardCharsets.UTF_8));
        formParams.add("idVille=" + URLEncoder.encode(idVille, StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formParams.toString()))
                .build();

        try {
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Menu updateMenu(int choix, User user, Scanner scanner) {
        return new MenuConnexion();
    }
}