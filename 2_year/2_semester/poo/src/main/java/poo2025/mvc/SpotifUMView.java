/*
 * Copyright (c) 2025. João Delgado, Nelson Mendes, Simão Mendes
 *
 * License: MIT
 *
 * Permission is granted to use, copy, modify, and distribute this work,
 * provided that the copyright notice and this license are included in all copies.
 */

package poo2025.mvc;

import poo2025.common.Menu;

import java.util.Objects;

/**
 * The SpotifUMView class represents the view component in the MVC architecture of the SpotifUM application.
 * This class is responsible for handling all user interface interactions and displaying information to the user.
 * It manages the presentation of menus, user authentication, and various operational views for both regular and
 * administrative users.
 *
 * <p>The view communicates with the application logic through a {@link SpotifUMController} instance,
 * following the MVC pattern. It provides different menu interfaces based on user roles and handles
 * all user input and output operations.</p>
 */
public class SpotifUMView {
    /** The controller instance that handles business logic for the view */
    private SpotifUMController controller;

    /**
     * Constructs a new SpotifUMView with a default controller.
     * Creates and initializes a new SpotifUMController instance.
     */
    public SpotifUMView() {
        this.controller = new SpotifUMController();
    }

    /**
     * Constructs a SpotifUMView with a specified controller.
     *
     * @param controller the SpotifUMController instance to use
     */
    public SpotifUMView(SpotifUMController controller) {
        this.controller = controller;
    }

    /**
     * Copy constructor that creates a new SpotifUMView as a copy of another one.
     *
     * @param s the SpotifUMView to copy
     */
    public SpotifUMView(SpotifUMView s) {
        this.controller = s.getController();
    }

    /**
     * Gets the controller associated with this view.
     *
     * @return the SpotifUMController instance
     */
    public SpotifUMController getController() {
        return this.controller;
    }

    /**
     * Sets the controller for this view.
     *
     * @param controller the new SpotifUMController instance
     */
    public void setController(SpotifUMController controller) {
        this.controller = controller;
    }

    /**
     * Starts the application's main interface.
     * Displays the welcome message and initiates the authentication menu.
     */
    public void run() {
        this.printOnTerminal("====================================", true);
        this.printOnTerminal(Menu.applyColorAndStyle("      BEM-VINDO AO SPOTIFUM       ", Menu.ColorStyle.CYAN, Menu.ColorStyle.BOLD), true);
        this.printOnTerminal("====================================", true);

        this.runAuthenticationMenu();
        this.printOnTerminal(Menu.applyColorAndStyle("\nThank you for using SpotifUM. Goodbye!", Menu.ColorStyle.CYAN, Menu.ColorStyle.BOLD), true);
    }

    /**
     * Prints a message to the terminal with optional line break.
     *
     * @param s the string to print
     * @param enter true to add a line break, false otherwise
     */
    public void printOnTerminal(String s, boolean enter) {
        if (s != null) {
            if (enter)
                System.out.println(s);
            else
                System.out.print(s);
        }
    }

    /**
     * Manages the authentication process including login and new user registration.
     * Handles different user roles (admin, regular user) and their respective menu flows.
     */
    private void runAuthenticationMenu() {
        boolean isLoggedIn = false;
        while (!isLoggedIn) {
            this.printOnTerminal(Menu.printAuthenticationMenu(), false);
            int option = Menu.runMenu(0, 2);

            if (option == 1) {
                Integer id = Menu.getIntWithMessage("Enter your user ID: ");
                String password = Menu.getStringWithMessage("Enter your password: ");
                boolean login = this.controller.login(id, password);

                if (login && id == -1) {
                    this.runAdminMenu();
                    isLoggedIn = true;
                } else if (login) {
                    this.runUserMenu(id);
                    isLoggedIn = true;
                } else {
                    this.printOnTerminal("Incorrect user ID or password, please try again", true);
                }
            } else if (option == 2) {
                this.runNewUserMenu();
                isLoggedIn = true;
            } else {
                this.printOnTerminal("Authentication menu closing", true);
                break;
            }
        }
        if (this.controller.spotifUMAutoSaveState()) this.controller.saveState();
    }

    /**
     * Manages the administrator menu interface and its operations.
     * Provides access to entity operations, queries, and system settings.
     *
     */
    private void runAdminMenu() {
        int option;
        do  {
            this.printOnTerminal(Menu.printAdminMenu(), false);
            option = Menu.runMenu(0, 6);
            switch (option) {
                case 1 -> this.runEntityOperationsMenu();
                case 2 -> this.runQueryMenu();
                case 3 -> this.printOnTerminal(controller.historyView(), true);
                case 4 -> this.controller.loadState();
                case 5 -> this.controller.saveState();
                case 6 -> {
                    boolean updatedAutosave = !this.controller.spotifUMAutoSaveState();
                    this.controller.setSpotifUMAutoSaveState(updatedAutosave);
                    this.printOnTerminal("AutoSave is now " + (updatedAutosave ? "enabled" : "disabled") + ".", true);
                }
                case 0 -> this.printOnTerminal("Exiting...", true);
                default -> this.printOnTerminal("Invalid option.", true);
            }
        } while (option != 0);
        this.controller.logout();
        this.runAuthenticationMenu();
    }

    /**
     * Manages the query menu interface for administrators.
     * Allows execution of various system queries and displays their results.
     */
    private void runQueryMenu() {
        int queryNumber;
        do {
            this.printOnTerminal(Menu.printQueryMenu(), false);
            queryNumber = Menu.runMenu(0, 7);
            this.printOnTerminal(this.controller.runQuery(queryNumber), true);
        } while(queryNumber != 0);
    }

    /**
     * Manages the entity operations menu interface.
     * Handles creation, modification, and deletion of system entities.
     */
    private void runEntityOperationsMenu() {
        int operation;
        do {
            operation = this.selectOperation();
            if (operation == 0) continue;
            int type = selectEntityType();
            if (type == 0) continue;
            this.printOnTerminal(this.controller.entityOperations(operation, type), true);
        } while (operation != 0);
    }

    private int selectOperation() {
        this.printOnTerminal(Menu.printEntityOperationsMenu(), false);
        return Menu.runMenu(0, 4);
    }

    private int selectEntityType() {
        this.printOnTerminal(Menu.printEntityMenu(), false);
        return Menu.runMenu(0, 4);
    }

    /**
     * Manages the new user registration process.
     * Creates a new user account and logs them in.
     */
    private void runNewUserMenu() {
        String password = Menu.getStringWithMessage("Enter user password: ");
        Integer id = this.controller.addUserGeneric(password);
        this.printOnTerminal("Your user id is: " + id + ". Please remember it.", true);
        this.controller.login(id, password);
        this.runUserMenu(id);
    }

    /**
     * Manages the regular user menu interface and its operations.
     * Provides access to user-specific features like playlists and music playback.
     *
     * @param userId the ID of the authenticated user
     */
    private void runUserMenu(Integer userId) {
        int option;
        do {
            this.printOnTerminal(Menu.printUserMenu(), false);
            option = Menu.runMenu(0, 14);
            this.printOnTerminal(this.runUser(userId, option), true);
        } while (option != 0);
        this.controller.logout();
        this.runAuthenticationMenu();
    }

    /**
     * Processes user menu options and executes corresponding actions.
     *
     * @param userId the ID of the authenticated user
     * @param option the selected menu option
     * @return a string containing the result of the operation
     */
    private String runUser(Integer userId, int option) {
        return switch (option) {
            case 1 -> controller.handleUserProfile(userId);
            case 2 -> controller.viewUserCollection(userId, "playlists");
            case 3 -> controller.viewUserCollection(userId, "albums");
            case 4 -> controller.listMusics();
            case 5 -> controller.listPublicPlaylists();
            case 6 -> controller.listAlbums();
            case 7 -> controller.handleMusicPlayback(userId);
            case 8 -> controller.handlePlaylistCreation(userId);
            case 9 -> controller.handleAddMusicsToPlaylist(userId);
            case 10 -> controller.handleAddPublicPlaylistToLibrary(userId);
            case 11 -> controller.handleAddAlbumToLibrary(userId);
            case 12 -> controller.handleRecommendedPlaylist(userId);
            case 13 -> controller.getUserHistory(userId);
            case 0 -> "Exiting...";
            default -> "Invalid option.";
        };
    }

    /**
     * Creates and returns a deep copy of this SpotifUMView.
     *
     * @return a new SpotifUMView instance that is a copy of this one
     */
    @Override
    public SpotifUMView clone() {
        return new SpotifUMView(this);
    }

    /**
     * Compares this SpotifUMView with another object for equality.
     *
     * @param o the object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SpotifUMView that = (SpotifUMView) o;
        return Objects.equals(this.controller, that.getController());
    }

    /**
     * Generates a hash code for this SpotifUMView.
     *
     * @return a hash code value for this object
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(this.controller);
    }

    /**
     * Returns a string representation of this SpotifUMView.
     *
     * @return a string containing the class name and controller information
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("poo2025.mvc.SpotifUMView{");
        sb.append("controller=").append(this.controller);
        sb.append('}');
        return sb.toString();
    }
}