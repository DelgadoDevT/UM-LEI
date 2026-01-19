package sd.client;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class UserInterface {

    // ANSI color codes for console output
    public static final String RESET = "\033[0m";
    public static final String BOLD = "\033[1m";
    public static final String RED = "\033[0;31m";
    public static final String GREEN = "\033[0;32m";
    public static final String YELLOW = "\033[0;33m";
    public static final String PURPLE = "\033[0;35m";
    public static final String CYAN = "\033[0;36m";
    public static final String WHITE = "\033[0;37m";
    public static final String GREEN_BOLD = "\033[1;32m";
    public static final String RED_BOLD = "\033[1;31m";
    public static final String YELLOW_BOLD = "\033[1;33m";
    public static final String ROYAL_BLUE = "\033[38;5;33m";
    public static final String ROYAL_BLUE_BOLD = "\033[1;38;5;33m";

    /**
     * Main sales client console application
     * Usage: java UserInterface [ip] [port]
     * Default: localhost:12345
     */
    public static void main(String[] args) {
        String ip = (args.length > 0) ? args[0] : "localhost";
        int port = (args.length > 1) ? Integer.parseInt(args[1]) : 12345;

        SalesClient client = new SalesClient();
        Scanner scanner = new Scanner(System.in);
        boolean loggedIn = false;

        try {
            printBanner();
            System.out.println(YELLOW + " ⚡ Connecting to server..." + RESET);
            client.connect(ip, port);
            System.out.println(GREEN_BOLD + " ✔ Connected successfully!" + RESET);

            boolean running = true;

            while (running) {
                printMenu();
                System.out.print(BOLD + "> Choose an option: " + RESET);

                String choice = scanner.nextLine().trim();

                switch (choice) {
                    case "1":
                        // Register new user
                        printHeader("📝 Register New User");
                        System.out.print(PURPLE + "   👤 New Username: " + RESET);
                        String rUser = scanner.nextLine().trim();
                        System.out.print(PURPLE + "   🔑 New Password: " + RESET);
                        String rPass = scanner.nextLine().trim();

                        try {
                            boolean regOk = client.register(rUser, rPass);
                            if (regOk) printSuccess("Registered successfully! You can now login.");
                            else printError("User already exists!");
                        } catch (IOException | InterruptedException e) {
                            printError("Error: " + e.getMessage());
                        }
                        break;

                    case "2":
                        // User login
                        printHeader("🔓 Login");
                        System.out.print(PURPLE + "   👤 Username: " + RESET);
                        String lUser = scanner.nextLine().trim();
                        System.out.print(PURPLE + "   🔑 Password: " + RESET);
                        String lPass = scanner.nextLine().trim();

                        try {
                            boolean logOk = client.login(lUser, lPass);
                            if (logOk) {
                                printSuccess("Login successful! Session started.");
                                loggedIn = true;
                            } else {
                                printError("Wrong credentials.");
                            }
                        } catch (IOException | InterruptedException e ) {
                            printError("Error: " + e.getMessage());
                        }
                        break;

                    case "3":
                        // Record sale for current day
                        if (!loggedIn) {
                            printError("You need to login first!");
                            break;
                        }
                        printHeader("🛒 Record Sale (Current Day)");
                        System.out.print(CYAN + "   📦 Product: " + RESET);
                        String prod = scanner.nextLine().trim();
                        System.out.print(CYAN + "   🔢 Quantity: " + RESET);
                        String qtdStr = scanner.nextLine().trim();
                        System.out.print(CYAN + "   💰 Unit Price: " + RESET);
                        String priceStr = scanner.nextLine().trim();

                        try {
                            int qtd = Integer.parseInt(qtdStr);
                            double price = Double.parseDouble(priceStr);
                            client.addEvent(prod, qtd, price);
                            printSuccess("Sale recorded in the system.");
                        } catch (NumberFormatException e) {
                            printError("Invalid quantity or price.");
                        } catch (IOException | InterruptedException e) {
                            printError("Error: " + e.getMessage());
                        }
                        break;

                    case "4":
                        // Advance to next day
                        if (!loggedIn) {
                            printError("You need to login first!");
                            break;
                        }
                        printHeader("🌅 Advance Time");
                        System.out.println(YELLOW + "   ⏳ Processing end of day..." + RESET);
                        try {
                            client.startNewDay();
                            System.out.println(GREEN + "   ✔ Day advanced!" + RESET + " Sales archived.");
                        } catch (IOException | InterruptedException e) {
                            printError("Error: " + e.getMessage());
                        }
                        break;

                    case "5":
                        // Query statistical aggregations
                        if (!loggedIn) {
                            printError("You need to login first!");
                            break;
                        }
                        printHeader("📊 Query Aggregations");
                        System.out.println(CYAN + "   1." + RESET + " Total Quantity");
                        System.out.println(CYAN + "   2." + RESET + " Total Volume (€)");
                        System.out.println(CYAN + "   3." + RESET + " Average Price");
                        System.out.println(CYAN + "   4." + RESET + " Maximum Price");
                        System.out.print(BOLD + "   > Type: " + RESET);
                        String typeStr = scanner.nextLine().trim();
                        System.out.print(CYAN + "   📦 Product: " + RESET);
                        String agProd = scanner.nextLine().trim();
                        System.out.print(CYAN + "   📅 Previous days (Ex: 1 = yesterday): " + RESET);
                        String daysStr = scanner.nextLine().trim();

                        try {
                            int type = Integer.parseInt(typeStr);
                            int days = Integer.parseInt(daysStr);

                            double result = 0;
                            String unit = "";
                            switch (type) {
                                case 1: result = client.getQuantity(agProd, days); unit = "units"; break;
                                case 2: result = client.getVolume(agProd, days); unit = "€"; break;
                                case 3: result = client.getAverage(agProd, days); unit = "€ (avg)"; break;
                                case 4: result = client.getMax(agProd, days); unit = "€ (max)"; break;
                                default: printError("Invalid type."); break;
                            }

                            if (type >= 1 && type <= 4) {
                                if (result < 0) {
                                    printError("Error getting data (check if days are valid).");
                                } else {
                                    String formattedResult;
                                    if (type == 1) {
                                        formattedResult = String.format("%.0f", result);
                                    } else {
                                        formattedResult = String.format("%.2f", result);
                                    }
                                    System.out.println("\n   🔍 Result: " + YELLOW_BOLD + formattedResult + " " + unit + RESET);
                                }
                            }
                        } catch (NumberFormatException e) {
                            printError("Invalid type or days.");
                        } catch (IOException | InterruptedException e) {
                            printError("Error: " + e.getMessage());
                        }
                        break;

                    case "6":
                        // Filter events in compact format
                        if (!loggedIn) {
                            printError("You need to login first!");
                            break;
                        }
                        printHeader("🔍 Filter Events (Compact Format)");
                        System.out.print(CYAN + "   Previous days (d): " + RESET);
                        String daysBackStr = scanner.nextLine().trim();

                        try {
                            int daysBack = Integer.parseInt(daysBackStr);
                            System.out.println(CYAN + "   Products (comma separated): " + RESET);
                            System.out.print(CYAN + "   > " + RESET);
                            String productsInput = scanner.nextLine().trim();

                            if (productsInput.isEmpty()) {
                                printError("You must specify products!");
                                break;
                            }

                            String[] productArray = productsInput.split(",");
                            Set<String> products = new HashSet<>();
                            for (String p : productArray) {
                                products.add(p.trim());
                            }

                            List<String> filteredEvents = client.filterEvents(daysBack, products);
                            if (filteredEvents.isEmpty()) {
                                printWarning("No events found.");
                            } else {
                                System.out.println(GREEN_BOLD + "   Found " + filteredEvents.size() + " events:" + RESET);
                                for (String event : filteredEvents) {
                                    System.out.println("      • " + event);
                                }
                            }
                        } catch (NumberFormatException e) {
                            printError("Invalid days.");
                        } catch (IOException | InterruptedException e) {
                            printError("Error: " + e.getMessage());
                        }
                        break;

                    case "7":
                        // Subscribe to simultaneous sales (blocking)
                        if (!loggedIn) {
                            printError("You need to login first!");
                            break;
                        }
                        printHeader("🔔 Subscribe to Simultaneous Sales");
                        System.out.println(YELLOW + "   ⚠  BLOCKING: Waits until event occurs." + RESET);
                        System.out.print(CYAN + "   📦 Product 1: " + RESET);
                        String p1 = scanner.nextLine().trim();
                        System.out.print(CYAN + "   📦 Product 2: " + RESET);
                        String p2 = scanner.nextLine().trim();

                        System.out.println(YELLOW + "   ⏳ Waiting..." + RESET);
                        try {
                            boolean simResult = client.subscribeSimultaneous(p1, p2);
                            if (simResult) printSuccess("BOTH products were sold!");
                            else printWarning("Day ended without success.");
                        } catch (IOException | InterruptedException e) {
                            printError("Error: " + e.getMessage());
                        }
                        break;

                    case "8":
                        // Subscribe to consecutive sales (blocking)
                        if (!loggedIn) {
                            printError("You need to login first!");
                            break;
                        }
                        printHeader("📈 Subscribe to Consecutive Sales");
                        System.out.println(YELLOW + "   ⚠  BLOCKING: Waits until event occurs." + RESET);
                        System.out.print(CYAN + "   📦 Product: " + RESET);
                        String cProd = scanner.nextLine().trim();
                        System.out.print(CYAN + "   🔢 Quantity (N): " + RESET);
                        String nStr = scanner.nextLine().trim();

                        try {
                            int n = Integer.parseInt(nStr);
                            System.out.println(YELLOW + "   ⏳ Waiting for " + n + " sales of " + cProd + "..." + RESET);
                            String conResult = client.subscribeConsecutive(cProd, n);
                            if (conResult != null && !conResult.equals("null")) {
                                printSuccess(cProd + " sold " + n + " consecutive times!");
                            } else {
                                printWarning("Objective not reached.");
                            }
                        } catch (NumberFormatException e) {
                            printError("Invalid quantity.");
                        } catch (IOException | InterruptedException e) {
                            printError("Error: " + e.getMessage());
                        }
                        break;

                    case "0":
                        // Exit application
                        running = false;
                        System.out.println(YELLOW + "\n 👋 Exiting... Goodbye!" + RESET);
                        break;

                    default:
                        printError("Invalid option.");
                }

                if(running && !choice.equals("7") && !choice.equals("8")) {
                    System.out.println(ROYAL_BLUE + "\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" + RESET);
                }
            }
            client.close();

        } catch (IOException e) {
            printError("Server offline or unreachable.");
        }
    }

    // --- UI Helper Methods ---

    /** Display application banner */
    private static void printBanner() {
        System.out.println();
        System.out.println(ROYAL_BLUE_BOLD + "  ╔═══════════════════════════════════════╗");
        System.out.println(ROYAL_BLUE_BOLD + "  ║                                       ║");
        System.out.println(ROYAL_BLUE_BOLD + "  ║    _____ ____        ____ _           ║");
        System.out.println(ROYAL_BLUE_BOLD + "  ║   / ____|  _ \\      / ___| |          ║");
        System.out.println(ROYAL_BLUE_BOLD + "  ║   \\___ \\| | | |____| |   | |          ║");
        System.out.println(ROYAL_BLUE_BOLD + "  ║    ___) | |_| |____| |___| |___       ║");
        System.out.println(ROYAL_BLUE_BOLD + "  ║   |____/|____/      \\____|_____|      ║");
        System.out.println(ROYAL_BLUE_BOLD + "  ║                                       ║");
        System.out.println(ROYAL_BLUE_BOLD + "  ║        " + CYAN + "Sales System v1.0" + ROYAL_BLUE_BOLD + "              ║");
        System.out.println(ROYAL_BLUE_BOLD + "  ║                                       ║");
        System.out.println(ROYAL_BLUE_BOLD + "  ╚═══════════════════════════════════════╝" + RESET);
        System.out.println();
    }

    /** Display main menu options */
    private static void printMenu() {
        System.out.println(ROYAL_BLUE_BOLD + "═══════════════════════════════════════════");
        System.out.println(ROYAL_BLUE_BOLD + "  " + CYAN + "🔐 AUTHENTICATION" + ROYAL_BLUE_BOLD);
        System.out.println(ROYAL_BLUE_BOLD + "     " + WHITE + "1." + RESET + " Register");
        System.out.println(ROYAL_BLUE_BOLD + "     " + WHITE + "2." + RESET + " Login");
        System.out.println(ROYAL_BLUE_BOLD + "═══════════════════════════════════════════");
        System.out.println(ROYAL_BLUE_BOLD + "  " + CYAN + "📝 EVENTS" + ROYAL_BLUE_BOLD);
        System.out.println(ROYAL_BLUE_BOLD + "     " + WHITE + "3." + RESET + " Record Sale (Today)");
        System.out.println(ROYAL_BLUE_BOLD + "     " + WHITE + "4." + RESET + " New Day (Advance Time)");
        System.out.println(ROYAL_BLUE_BOLD + "═══════════════════════════════════════════");
        System.out.println(ROYAL_BLUE_BOLD + "  " + CYAN + "📊 STATISTICS" + ROYAL_BLUE_BOLD);
        System.out.println(ROYAL_BLUE_BOLD + "     " + WHITE + "5." + RESET + " Query Aggregations");
        System.out.println(ROYAL_BLUE_BOLD + "     " + WHITE + "6." + RESET + " Filter Events (Compact)");
        System.out.println(ROYAL_BLUE_BOLD + "═══════════════════════════════════════════");
        System.out.println(ROYAL_BLUE_BOLD + "  " + CYAN + "🔔 NOTIFICATIONS" + ROYAL_BLUE_BOLD);
        System.out.println(ROYAL_BLUE_BOLD + "     " + WHITE + "7." + RESET + " Simultaneous Sales");
        System.out.println(ROYAL_BLUE_BOLD + "     " + WHITE + "8." + RESET + " Consecutive Sales");
        System.out.println(ROYAL_BLUE_BOLD + "═══════════════════════════════════════════");
        System.out.println(ROYAL_BLUE_BOLD + "     " + RED + "0." + RESET + " EXIT");
        System.out.println(ROYAL_BLUE_BOLD + "═══════════════════════════════════════════" + RESET);
    }

    /** Display section header */
    private static void printHeader(String text) {
        System.out.println("\n" + ROYAL_BLUE_BOLD + "═══════════════════════════════════════════");
        System.out.println(ROYAL_BLUE_BOLD + " " + CYAN + text + ROYAL_BLUE_BOLD);
        System.out.println(ROYAL_BLUE_BOLD + "═══════════════════════════════════════════" + RESET);
    }

    /** Display success message */
    private static void printSuccess(String msg) {
        System.out.println(GREEN_BOLD + " ✔ SUCCESS: " + RESET + GREEN + msg + RESET);
    }

    /** Display error message */
    private static void printError(String msg) {
        System.out.println(RED_BOLD + " ✖ ERROR: " + RESET + RED + msg + RESET);
    }

    /** Display warning message */
    private static void printWarning(String msg) {
        System.out.println(YELLOW_BOLD + " ⚠ WARNING: " + RESET + YELLOW + msg + RESET);
    }
}