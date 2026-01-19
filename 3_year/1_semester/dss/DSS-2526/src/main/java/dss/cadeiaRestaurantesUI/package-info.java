/**
 * Presentation Layer (User Interface) for the Restaurant Chain Management System.
 * <p>
 * This package contains the user interface components following the MVC
 * (Model-View-Controller) pattern. It handles user interactions and displays
 * information retrieved from the business logic layer.
 * </p>
 *
 * <h2>Main Components</h2>
 * <ul>
 *   <li>{@link dss.cadeiaRestaurantesUI.CadeiaRestaurantesView} - Main view (console-based UI)</li>
 *   <li>{@link dss.cadeiaRestaurantesUI.CadeiaRestaurantesController} - Main controller</li>
 * </ul>
 *
 * <h2>MVC Architecture</h2>
 * <ul>
 *   <li><b>Model:</b> Business logic layer ({@link dss.cadeiaRestaurantesLN})</li>
 *   <li><b>View:</b> {@link dss.cadeiaRestaurantesUI.CadeiaRestaurantesView} - Handles user I/O</li>
 *   <li><b>Controller:</b> {@link dss.cadeiaRestaurantesUI.CadeiaRestaurantesController} - Coordinates view and model</li>
 * </ul>
 *
 * <h2>Key Features</h2>
 * <ul>
 *   <li><b>Interactive Menu System:</b> Console-based navigation menus</li>
 *   <li><b>Restaurant Operations:</b> View and manage restaurants</li>
 *   <li><b>Order Management:</b> Create and track orders</li>
 *   <li><b>Menu Browsing:</b> View available products and menus</li>
 *   <li><b>Performance Reporting:</b> Display performance indicators</li>
 *   <li><b>Employee Functions:</b> Access role-specific features</li>
 * </ul>
 *
 * <h2>User Interface Flow</h2>
 * <ol>
 *   <li>Main menu displays available options</li>
 *   <li>User selects an operation</li>
 *   <li>View requests necessary input from user</li>
 *   <li>Controller processes input and invokes business logic</li>
 *   <li>Results are displayed back to user</li>
 *   <li>Returns to menu for next operation</li>
 * </ol>
 *
 * <h2>Design Patterns</h2>
 * <ul>
 *   <li><b>MVC Pattern</b> - Separation of concerns (view/controller/model)</li>
 *   <li><b>Command Pattern</b> - Menu options as commands</li>
 *   <li><b>Facade Pattern</b> - Controller simplifies access to business logic</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * // Typically started from Application.main()
 * ICadeiaRestaurantesLN model = new CadeiaRestaurantesLNFacade();
 * CadeiaRestaurantesController controller = new CadeiaRestaurantesController(model);
 * CadeiaRestaurantesView view = new CadeiaRestaurantesView(controller);
 *
 * view.run(); // Start the interactive console UI
 * }</pre>
 *
 * @since 1.0
 * @version 1.0-SNAPSHOT
 * @author Group 8 - DSS Course 2025/26
 */
package dss.cadeiaRestaurantesUI;

