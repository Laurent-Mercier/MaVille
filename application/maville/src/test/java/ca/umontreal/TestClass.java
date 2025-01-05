package ca.umontreal;
import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Method;
import java.net.http.HttpResponse;

import java.util.Scanner;
import ca.umontreal.menu.*;
import ca.umontreal.user.*;
import ca.umontreal.restApi.RestApi;
import static org.junit.Assert.*;

public class TestClass {


    // Fonctionnalité: Soumettre une requete de travail
    // verifie que l'on peut soumettre
    @Test 
    public void testChoixMenuSoumettre() throws Exception {
        RestApi restApi = new RestApi();
        MenuSoumettreRequete soumettreRequete = new MenuSoumettreRequete();
        Method updateMenu = MenuSoumettreRequete.class.getDeclaredMethod("updateMenu", int.class, User.class, Scanner.class);
        updateMenu.setAccessible(true);
        Menu result = (Menu) updateMenu.invoke(soumettreRequete, 3, new User("usager"), new Scanner(System.in));
        assertEquals("principal",result.getType());
        restApi.stop();
    }
    // Fonctionnalité: se connecter/deconnecter
    // Verifie que l'option 1 amene le menu de connection
    @Test
    public void testMenuConnexionSeConnecter() throws Exception {
        // Arrange
        MenuConnexion menuConnexion = new MenuConnexion();
        Method updateMenu = MenuConnexion.class.getDeclaredMethod("updateMenu", int.class, User.class, Scanner.class);
        updateMenu.setAccessible(true);

        // Act
        Menu result = (Menu) updateMenu.invoke(menuConnexion, 1, new User("testUser"), new Scanner(System.in));

        // Assert
        assertEquals("seConnecter", result.getType());
    }
    // Verifie si on peut sortir du menu de connexion
    @Test
    public void testMenuConnexionExit() throws Exception {
        // Arrange
        MenuConnexion menuConnexion = new MenuConnexion();
        Method updateMenu = MenuConnexion.class.getDeclaredMethod("updateMenu", int.class, User.class, Scanner.class);
        updateMenu.setAccessible(true);

        // Act
        Menu result = (Menu) updateMenu.invoke(menuConnexion, 3, new User("testUser"), new Scanner(System.in));

        // Assert
        assertEquals("exit", result.getType());
    }

    // Verifie si menu de connection gere bien les mauvais input
    @Test
    public void testMenuConnexionInvalidOption() throws Exception {
        // Arrange
        MenuConnexion menuConnexion = new MenuConnexion();
        Method updateMenu = MenuConnexion.class.getDeclaredMethod("updateMenu", int.class, User.class, Scanner.class);
        updateMenu.setAccessible(true);

        // Act
        Menu result = (Menu) updateMenu.invoke(menuConnexion, 4, new User("testUser"), new Scanner(System.in));

        // Assert
        assertEquals("connexion", result.getType());
    }





}

