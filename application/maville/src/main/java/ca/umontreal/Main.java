package ca.umontreal;
import ca.umontreal.restApi.RestApi;
import ca.umontreal.menu.*;
import ca.umontreal.user.*;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {

        RestApi restApi = new RestApi();
        restApi.start();

        Menu connexion = new MenuConnexion();
        List<Menu> menuList = new ArrayList<>(Arrays.asList(connexion));
        Scanner userInput=new Scanner(System.in);
        User user = new User("usager");
        while (!menuList.get(0).getType().equals("exit")) {
            menuList.get(0).showMenu(userInput, user);
            int choix;
            String optionTextuelle;
            if (!(menuList.get(0).getType().equals("creerUnCompte")||menuList.get(0).getType().equals("seConnecter"))) {
                while (true) {
                    optionTextuelle = userInput.nextLine();
                    try {
                        choix=Integer.parseInt(optionTextuelle);
                        break;
                    } catch (Exception e) {
                        System.out.println("\nVeuillez entrer un numéro.\n");
                    }
                }
            } else {
                choix = 0;
            }
            
            if ((menuList.get(0).getType().equals("seConnecter")||menuList.get(0).getType().equals("creerUnCompte"))&&user.getUserType().equals("usager")) {
                Menu retourConnexion = new MenuConnexion();
                menuList.add(retourConnexion);
            } else {
                menuList.add(menuList.get(0).updateMenu(choix, 
                user, userInput));
            }
            menuList.set(0, menuList.get(1));
            menuList.remove(1);
        }
        userInput.close();
        restApi.stop();
        System.exit(0);
    }
}