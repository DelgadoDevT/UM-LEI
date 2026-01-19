/**
 * Data Access Layer for the Restaurant Chain Management System.
 * <p>
 * This package contains all DAO (Data Access Object) implementations that handle
 * database operations using MariaDB JDBC driver. Each DAO class provides CRUD
 * (Create, Read, Update, Delete) operations for its corresponding entity.
 * </p>
 *
 * <h2>Key Components</h2>
 * <ul>
 *   <li>{@link dss.cadeiaRestaurantesDL.DAOConfig} - Database connection configuration and credentials</li>
 *   <li>{@link dss.cadeiaRestaurantesDL.RestauranteDAO} - Restaurant entity data access</li>
 *   <li>{@link dss.cadeiaRestaurantesDL.FuncionarioDAO} - Employee entity data access</li>
 *   <li>{@link dss.cadeiaRestaurantesDL.MenuDAO} - Menu entity data access</li>
 *   <li>{@link dss.cadeiaRestaurantesDL.ProdutoDAO} - Product entity data access</li>
 *   <li>{@link dss.cadeiaRestaurantesDL.IngredienteDAO} - Ingredient entity data access</li>
 *   <li>{@link dss.cadeiaRestaurantesDL.PedidoDAO} - Order entity data access</li>
 *   <li>{@link dss.cadeiaRestaurantesDL.IndicadorDAO} - Performance indicator data access</li>
 * </ul>
 *
 * <h2>Design Patterns</h2>
 * <p>All DAO classes implement the following patterns:</p>
 * <ul>
 *   <li><b>DAO Pattern</b> - Abstracts and encapsulates all access to the data source</li>
 *   <li><b>Singleton Pattern</b> - Each DAO maintains a single instance via getInstance()</li>
 *   <li><b>Map Interface</b> - All DAOs implement java.util.Map for uniform access patterns</li>
 * </ul>
 *
 * <h2>Database</h2>
 * <ul>
 *   <li><b>DBMS:</b> MariaDB 10.x+</li>
 *   <li><b>Driver:</b> MariaDB Connector/J 3.5.6</li>
 *   <li><b>Database:</b> cadeiaRestaurantes</li>
 *   <li><b>Connection:</b> localhost:3306</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * // Get DAO instance
 * RestauranteDAO restauranteDAO = RestauranteDAO.getInstance();
 *
 * // Retrieve a restaurant
 * Restaurante rest = restauranteDAO.get(1);
 *
 * // Add a new restaurant
 * Restaurante novoRest = new Restaurante("Restaurante Central", "Lisboa");
 * restauranteDAO.put(novoRest.getId(), novoRest);
 * }</pre>
 *
 * @since 1.0
 * @version 1.0-SNAPSHOT
 * @author Group 8 - DSS Course 2025/26
 */
package dss.cadeiaRestaurantesDL;

