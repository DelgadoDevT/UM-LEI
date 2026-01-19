/**
 * Business Logic Layer for the Restaurant Chain Management System.
 * <p>
 * This package contains the core business logic and domain model of the application.
 * It coordinates operations between the presentation layer (UI) and data access layer (DL),
 * implementing business rules and orchestrating workflows across subsystems.
 * </p>
 *
 * <h2>Main Components</h2>
 * <ul>
 *   <li>{@link dss.cadeiaRestaurantesLN.ICadeiaRestaurantesLN} - Main business logic interface</li>
 *   <li>{@link dss.cadeiaRestaurantesLN.CadeiaRestaurantesLNFacade} - Primary facade implementation</li>
 * </ul>
 *
 * <h2>Subsystems</h2>
 * <p>The business logic is organized into specialized subsystems:</p>
 * <ul>
 *   <li><b>{@link dss.cadeiaRestaurantesLN.subsistemaRestaurantes}</b> - Restaurant and employee management</li>
 *   <li><b>{@link dss.cadeiaRestaurantesLN.subsistemaMenuPedidos}</b> - Menu, product, and order management</li>
 * </ul>
 *
 * <h2>Design Patterns</h2>
 * <ul>
 *   <li><b>Facade Pattern</b> - CadeiaRestaurantesLNFacade provides simplified interface to subsystems</li>
 *   <li><b>Subsystem Pattern</b> - Modular organization of related business logic</li>
 *   <li><b>Interface Segregation</b> - Clear contracts via ICadeiaRestaurantesLN interface</li>
 * </ul>
 *
 * <h2>Key Responsibilities</h2>
 * <ul>
 *   <li>Business rule validation and enforcement</li>
 *   <li>Transaction coordination across multiple entities</li>
 *   <li>Data transformation between UI and DL layers</li>
 *   <li>Complex query orchestration</li>
 *   <li>Domain model management</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * // Create facade instance
 * ICadeiaRestaurantesLN facade = new CadeiaRestaurantesLNFacade();
 *
 * // Get available restaurants
 * List<String> restaurantes = facade.getNomesRestaurantesPublico();
 *
 * // Register new order
 * Pedido pedido = new Pedido(...);
 * facade.registarPedido(pedido);
 * }</pre>
 *
 * @since 1.0
 * @version 1.0-SNAPSHOT
 * @author Group 8 - DSS Course 2025/26
 */
package dss.cadeiaRestaurantesLN;

