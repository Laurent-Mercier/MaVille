package ca.umontreal.menu;

import ca.umontreal.user.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class MenuPrincipal extends Menu {
    public MenuPrincipal() {
        super("principal");
    }

    /**
     * Affiche le menu principal pour l'utilisateur en fonction de son type (résident ou intervenant).
     * @param scanner
     * @param user
     */
    public void showMenu(Scanner scanner, User user) {
        System.out.println("\nBienvenue dans MaVille! Entrez le numéro " + 
        "entre parenthèses pour choisir une option:\n\n");
        String [] options;
        if (user.getUserType().equals("resident")) {
            options = new String[] {"Consulter les travaux (1)", 
            "Notifications personnalisées (2)",
            "Requêtes de travail (3)", 
            "Consulter les entraves routières (4)",
            "Précisez vos préférences sur l'horaire des chantiers (5)", "Fermer (6)"};
        } else {
            options = new String[] {"Requêtes de travaux (1)", 
            "Projets (2)","Fermer (3)"};
        }
        for (int i=0; i<options.length; i++) {
            System.out.println(options[i]);
        }
        System.out.println("");
    }

    /**
     * Met à jour le menu en fonction du choix de l'utilisateur et de son type (résident ou intervenant) et
     * redirige l'utilisateur vers le menu approprié selon l'option sélectionnée.
     * @param choix
     * @param user
     * @param scanner
     * @return un menu
     */
    public Menu updateMenu(int choix, User user, Scanner scanner) {
        
        if (user.getUserType().equals("resident")) {
            switch (choix) {
                case 1:
                    Menu consulterTravaux = new MenuConsulterTravaux();
                    return consulterTravaux;
                case 2:
                    Menu notif = new MenuNotification();
                    return notif;
                case 3:
                    Menu soumettreRequete = new MenuSoumettreRequete();
                    return soumettreRequete;
                case 4:
                    Menu consulEntraves = new MenuConsulterEntraves();
                    return consulEntraves;
                case 5:
                    Menu prefTravaux = new MenuPreferencesTravaux();
                    return prefTravaux;
                case 6:
                    Menu connexion = new MenuConnexion();
                    return connexion;
                default:
                    System.out.println("\nEntrez un nombre de 1 à 6 pour " +
                    "choisir une option:\n");
                    return this;
            }
        } else {
            switch (choix) {
                case 1:
                    Menu consulRequetes = new MenuConsulterRequetes();
                    return consulRequetes;
                case 2:
                    Menu soumettreProjet = new MenuSoumettreProjet();
                    return soumettreProjet;
                case 3:
                    Menu connexion = new MenuConnexion();
                    return connexion;
                default:
                    System.out.println("\nEntrez un nombre de 1 à 3 pour " + 
                    "choisir une option:\n");
                    return this;
            }
        }
    }
}