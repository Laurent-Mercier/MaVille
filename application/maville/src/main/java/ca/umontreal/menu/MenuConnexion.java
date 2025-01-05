package ca.umontreal.menu;

import ca.umontreal.user.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class MenuConnexion extends Menu {

    public MenuConnexion() {
        super("connexion");
        String [] options = {"Se connecter (1)", "Créer un compte (2)", 
        "Fermer l'application (3)"};
        this.setElements(new ArrayList<String>(Arrays.asList(options)));
    }
    /**
     * Affiche le menu à l'utilisateur avec les options disponibles pour se connecter,
     * créer un compte.
     */

    public void showMenu(Scanner scanner, User user) {
        System.out.println("\nMaVille\n\n");
        System.out.println("\nConnectez-vous ou créez un compte! " +
        "Entrez le numéro entre parenthèses pour choisir une option:\n");
        for (int i=0; i<this.getElements().size(); i++) {
            System.out.println(this.getElements().get(i));
        }
        System.out.println("");
    }
    /**
     * Affiche le menu principal à l'utilisateur avec les options disponibles.
     */
    public Menu updateMenu(int choix, User user, Scanner scanner) {
        switch (choix) {
            case 1:
                Menu seConnecter = new MenuSeConnecter();
                return seConnecter;
            case 2:
                Menu creerUnCompte = new MenuCreerUnCompte();
                return creerUnCompte;
            case 3:
                Menu exit = new MenuExit();
                return exit;
            default:
                System.out.println("\nEntrez un nombre de 1 à 3 pour choisir" + 
                " une option:\n");
                return this;
        }
    }
}
