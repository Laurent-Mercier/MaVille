package ca.umontreal.menu;

import ca.umontreal.user.*;
import java.util.Scanner;

public class MenuExit extends Menu {
    public MenuExit() {
        super("exit");
    }

    public void showMenu(Scanner scanner, User user) {
    }

    public Menu updateMenu(int choix, User user, Scanner scanner) {
        return this;
    }
}
