package ca.umontreal.menu;

import ca.umontreal.user.*;
import java.util.ArrayList;
import java.util.Scanner;

public abstract class Menu {

    private User user;
    private ArrayList<String> elements;
    private String type;
    public Menu(String menuType) {
        this.elements= new ArrayList<String>(0);
        this.type = menuType;
        this.user = new User("usager");
    }
    
    public ArrayList<String> getElements() {
        return this.elements;
    }
    public String getType() {
        return this.type;
    }

    public User getUser() {
        return this.user;
    }

    public void setElements(ArrayList<String> elements) {
        this.elements = elements;
    }

    public void setType(String type) {
        this.type=type;
    }

    public void setUser(User user) {
        this.user=user;
    }

    public abstract void showMenu(Scanner scanner, User user);

    public abstract Menu updateMenu(int choix, User user, Scanner scanner);
}
