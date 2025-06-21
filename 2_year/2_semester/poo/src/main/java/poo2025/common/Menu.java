/*
 * Copyright (c) 2025. João Delgado, Nelson Mendes, Simão Mendes
 *
 * License: MIT
 *
 * Permission is granted to use, copy, modify, and distribute this work,
 * provided that the copyright notice and this license are included in all copies.
 */

package poo2025.common;

import java.util.Scanner;

/**
 * The Menu class provides utility methods for creating and managing console-based user interfaces
 * in the SpotifUM application. It handles menu generation, user input processing, and terminal output
 * styling with ANSI color codes.
 *
 * <p>This class includes functionality for:</p>
 * <ul>
 *   <li>Creating styled menu interfaces with borders and color-coded options</li>
 *   <li>Processing and validating user input</li>
 *   <li>Applying color and text styling to terminal output</li>
 *   <li>Managing different types of user menus (admin, user, authentication, etc.)</li>
 * </ul>
 *
 * <p>The class is designed as a utility class and cannot be instantiated.</p>
 */
public class Menu {
    /** Scanner instance for reading user input from the console */
    private static final Scanner scanner = new Scanner(System.in);

    /**
     * Private constructor to prevent instantiation of this utility class.
     *
     * @throws UnsupportedOperationException always, as this is a utility class
     */
    public Menu() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Utility Class");
    }

    /**
     * Enumeration of ANSI color and style codes for terminal output formatting.
     * Provides various text colors and styles that can be applied to console output.
     */
    public enum ColorStyle {
        /** Reset all colors and styles */
        RESET("\033[0m"),
        /** Red text color */
        RED("\033[31m"),
        /** Green text color */
        GREEN("\033[32m"),
        /** Yellow text color */
        YELLOW("\033[33m"),
        /** Blue text color */
        BLUE("\033[34m"),
        /** Magenta text color */
        MAGENTA("\033[35m"),
        /** Cyan text color */
        CYAN("\033[36m"),
        /** White text color */
        WHITE("\033[37m"),
        /** Bold text style */
        BOLD("\033[1m"),
        /** Underlined text style */
        UNDERLINE("\033[4m");

        private final String code;

        /**
         * Constructs a ColorStyle enum value with the specified ANSI code.
         *
         * @param code the ANSI escape code for the color or style
         */
        ColorStyle(String code) {
            this.code = code;
        }

        /**
         * Gets the ANSI escape code for this color or style.
         *
         * @return the ANSI escape code as a string
         */
        public String getCode() {
            return this.code;
        }
    }

    /**
     * Applies color and style to a text string using ANSI escape codes.
     *
     * @param text the text to style
     * @param color the color to apply (may be null)
     * @param style the style to apply (may be null)
     * @return the styled text string
     * @throws IllegalArgumentException if the text parameter is null
     */
    public static String applyColorAndStyle(String text, ColorStyle color, ColorStyle style) {
        if (text == null)
            throw new IllegalArgumentException("Text cannot be null");

        String colorCode = (color != null) ? color.getCode() : "";
        String styleCode = (style != null) ? style.getCode() : "";

        return styleCode + colorCode + text + ColorStyle.RESET.getCode();
    }

    /**
     * Runs a menu interface and validates user input within a specified range.
     *
     * @param min the minimum valid option number
     * @param max the maximum valid option number
     * @return the validated user input
     */
    public static int runMenu(int min, int max) {
        int option = -1;

        while (option < min || option > max) {
            option = getIntWithMessage(null);
            if (option < min || option > max) {
                printOnTerminal(applyColorAndStyle("⚠ Invalid option, please choose a valid option (" + min + "-" + max + ").",
                        ColorStyle.RED, ColorStyle.BOLD));
            }
        }

        return option;
    }

    /**
     * Creates a styled border line for menu formatting.
     *
     * @param length the length of the border
     * @param symbol the symbol to use for the border
     * @param color the color to apply to the border
     * @return the formatted border string
     */
    private static String createBorder(int length, String symbol, ColorStyle color) {
        return applyColorAndStyle(new String(new char[length]).replace("\0", symbol), color, ColorStyle.BOLD);
    }

    /**
     * Builds a formatted menu with a title, options, and border.
     *
     * @param title the menu title
     * @param options array of menu options
     * @return the formatted menu string
     */
    private static String buildMenu(String title, String[] options) {
        StringBuilder sb = new StringBuilder();
        int maxLength = title.length() + 10;

        for (String option : options) {
            if (option.length() + 8 > maxLength) {
                maxLength = option.length() + 8;
            }
        }

        String border = createBorder(maxLength, "═", ColorStyle.BLUE);

        sb.append("\n").append(border).append("\n");
        sb.append(applyColorAndStyle("  " + title + "  ", ColorStyle.CYAN, ColorStyle.BOLD)).append("\n");
        sb.append(border).append("\n");

        for (String option : options) {
            if (option.startsWith("0.")) {
                sb.append(applyColorAndStyle(option, ColorStyle.RED, ColorStyle.BOLD)).append("\n");
            } else {
                String[] parts = option.split("\\.", 2);
                sb.append(applyColorAndStyle(parts[0] + ".", ColorStyle.YELLOW, ColorStyle.BOLD))
                        .append(applyColorAndStyle(parts[1], ColorStyle.WHITE, ColorStyle.BOLD))
                        .append("\n");
            }
        }

        sb.append(border).append("\n");
        sb.append(applyColorAndStyle("Choose an option: ", ColorStyle.GREEN, ColorStyle.BOLD));

        return sb.toString();
    }

    /**
     * Generates the admin menu interface.
     *
     * @return the formatted admin menu string
     */
    public static String printAdminMenu() {
        String[] mainMenuOptions = {
                "1. Entity Operations",
                "2. Run Queries",
                "3. History",
                "4. Load State",
                "5. Save State",
                "6. AutoSave True/False",
                "0. Exit"
        };
        return buildMenu("SpotifUM Admin Menu", mainMenuOptions);
    }

    /**
     * Generates the entity operations menu interface.
     *
     * @return the formatted entity operations menu string
     */
    public static String printEntityOperationsMenu() {
        String[] entityOperations = {
                "1. Add Entity",
                "2. Modify Entity",
                "3. Remove Entity",
                "4. List Entities",
                "0. Return"
        };
        return buildMenu("Entity Operation Menu", entityOperations);
    }

    /**
     * Generates the entity selection menu interface.
     *
     * @return the formatted entity selection menu string
     */
    public static String printEntityMenu() {
        String[] entityType = {
                "1. User",
                "2. Music",
                "3. Playlist",
                "4. Album",
                "0. Return"
        };
        return buildMenu("Entity Menu", entityType);
    }

    /**
     * Generates the query menu interface.
     *
     * @return the formatted query menu string
     */
    public static String printQueryMenu() {
        String[] queryOptions = {
                "1. Most Played Music",
                "2. Most Listened Artist",
                "3. Top User by Listening Time",
                "4. Top User by Points",
                "5. Top Music genre",
                "6. Public Playlists Count",
                "7. User with Most Playlists",
                "0. Return"
        };
        return buildMenu("Query Menu", queryOptions);
    }

    /**
     * Generates the authentication menu interface.
     *
     * @return the formatted authentication menu string
     */
    public static String printAuthenticationMenu() {
        String[] authenticationOptions = {
                "1. Login",
                "2. Register",
                "0. Quit"
        };
        return buildMenu("Login Menu", authenticationOptions);
    }

    /**
     * Generates the user menu interface.
     *
     * @return the formatted user menu string
     */
    public static String printUserMenu() {
        String[] userOptions = {
                "1. View Profile",
                "2. View My Playlists Library",
                "3. View My Albums Library",
                "4. View Musics",
                "5. View Public Playlists",
                "6. View Albums",
                "7. Play Music",
                "8. Create Playlist",
                "9. Add Music to Playlist",
                "10. Add Public Playlist to Library",
                "11. Add Album to Library",
                "12. Generate Recommended Playlist",
                "13. View my History",
                "0. Logout"
        };
        return buildMenu("User Menu", userOptions);
    }

    /**
     * Gets an integer input from the user with an optional prompt message.
     *
     * @param prompt the message to display before getting input (may be null)
     * @return the validated integer input
     */
    public static int getIntWithMessage(String prompt) {
        if (prompt != null) {
            System.out.print(applyColorAndStyle(prompt, ColorStyle.GREEN, null));
        }

        while (!scanner.hasNextInt()) {
            System.out.print(applyColorAndStyle("Invalid input. Please enter a valid integer: ",
                    ColorStyle.RED, null));
            scanner.next();
        }
        int value = scanner.nextInt();
        scanner.nextLine();
        return value;
    }

    /**
     * Prints a message to the terminal with formatting preserved.
     *
     * @param s the string to print (may be null)
     */
    public static void printOnTerminal(String s) {
        if (s != null) {
            System.out.println(s);
        }
    }

    /**
     * Gets a string input from the user with an optional prompt message.
     *
     * @param prompt the message to display before getting input (may be null)
     * @return the user's input string
     */
    public static String getStringWithMessage(String prompt) {
        if (prompt != null) {
            System.out.print(applyColorAndStyle(prompt, ColorStyle.GREEN, null));
        }
        return scanner.nextLine();
    }

    /**
     * Gets a yes/no response from the user.
     *
     * @param message the prompt message to display
     * @return the user's response ("yes" or "no")
     */
    public static String addYesNoInput(String message) {
        String response;
        do {
            response = Menu.getStringWithMessage(applyColorAndStyle(message + " (yes/no): ",
                    ColorStyle.YELLOW, null));
        } while (!response.equalsIgnoreCase("yes") && !response.equalsIgnoreCase("no"));
        return response;
    }

    /**
     * Gets the user's plan type selection (Base or Top).
     *
     * @return the selected plan type
     */
    public static String getPlanTypeInput() {
        String response;
        do {
            response = Menu.getStringWithMessage(applyColorAndStyle("Choose plan type (Base/Top): ",
                    ColorStyle.YELLOW, null));
        } while (!response.equalsIgnoreCase("base") && !response.equalsIgnoreCase("top"));
        return response;
    }

    /**
     * Gets the user's playback source selection.
     *
     * @param options the valid playback source options
     * @return the selected playback source
     */
    public static String getPlaybackSource(String options) {
        String playFrom;
        do {
            playFrom = Menu.getStringWithMessage(applyColorAndStyle("Play from (" + options + "): ",
                    ColorStyle.YELLOW, null));
        } while (!isValidPlaybackSource(playFrom, options));
        return playFrom;
    }

    /**
     * Validates if a playback source is valid according to the provided options.
     *
     * @param source the source to validate
     * @param validOptions string containing valid options
     * @return true if the source is valid, false otherwise
     */
    private static boolean isValidPlaybackSource(String source, String validOptions) {
        return validOptions.toLowerCase().contains(source.toLowerCase());
    }
}