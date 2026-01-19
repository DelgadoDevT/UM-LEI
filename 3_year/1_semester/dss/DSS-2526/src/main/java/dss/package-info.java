/**
 * Root package for the Restaurant Chain Management System (DSS Project).
 * <p>
 * This is the main package containing the complete Restaurant Chain Management System,
 * developed as part of the Software Systems Development (DSS) course project.
 * The system manages multiple restaurants, menus, orders, employees, and performance indicators.
 * </p>
 *
 * <h2>Architecture Overview</h2>
 * <p>The system follows a 3-layer architecture pattern:</p>
 * <ul>
 *   <li><b>{@link dss.cadeiaRestaurantesUI}</b> - Presentation Layer: User interface components</li>
 *   <li><b>{@link dss.cadeiaRestaurantesLN}</b> - Business Logic Layer: Core business logic and rules</li>
 *   <li><b>{@link dss.cadeiaRestaurantesDL}</b> - Data Access Layer: Database operations and persistence</li>
 * </ul>
 *
 * <h2>Main Components</h2>
 * <ul>
 *   <li>{@link dss.Application} - Application entry point</li>
 *   <li>{@link dss.cadeiaRestaurantesLN.CadeiaRestaurantesLNFacade} - Main business logic facade</li>
 *   <li>{@link dss.cadeiaRestaurantesUI.CadeiaRestaurantesController} - Main controller</li>
 *   <li>{@link dss.cadeiaRestaurantesUI.CadeiaRestaurantesView} - Main view</li>
 * </ul>
 *
 * <h2>Design Patterns Used</h2>
 * <ul>
 *   <li><b>Facade Pattern</b> - Simplifies access to complex subsystems</li>
 *   <li><b>DAO Pattern</b> - Abstracts database access operations</li>
 *   <li><b>Singleton Pattern</b> - Ensures single instance of DAO objects</li>
 *   <li><b>MVC Pattern</b> - Separates presentation, logic, and data concerns</li>
 * </ul>
 *
 * @since 1.0
 * @version 1.0-SNAPSHOT
 * @author Group 8 - DSS Course 2025/26
 */
package dss;

