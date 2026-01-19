/**
 * Restaurant and Employee Management Subsystem.
 * <p>
 * This subsystem handles all operations related to restaurants, employees (funcionários),
 * workstations (postos), and performance indicators (indicadores de desempenho).
 * It manages the organizational structure and operational monitoring of the restaurant chain.
 * </p>
 *
 * <h2>Main Components</h2>
 * <ul>
 *   <li>{@link dss.cadeiaRestaurantesLN.subsistemaRestaurantes.IGestRestaurantes} - Restaurant management interface</li>
 *   <li>{@link dss.cadeiaRestaurantesLN.subsistemaRestaurantes.RestaurantesFacade} - Facade for restaurant operations</li>
 * </ul>
 *
 * <h2>Domain Entities</h2>
 * <ul>
 *   <li>{@link dss.cadeiaRestaurantesLN.subsistemaRestaurantes.Restaurante} - Restaurant entity with location, staff, and stock</li>
 *   <li>{@link dss.cadeiaRestaurantesLN.subsistemaRestaurantes.Funcionario} - Employee entity with role and assignments</li>
 *   <li>{@link dss.cadeiaRestaurantesLN.subsistemaRestaurantes.Posto} - Workstation entity (e.g., kitchen, counter)</li>
 *   <li>{@link dss.cadeiaRestaurantesLN.subsistemaRestaurantes.IndicadorDesempenho} - Performance metrics entity</li>
 * </ul>
 *
 * <h2>Key Features</h2>
 * <ul>
 *   <li><b>Restaurant Management:</b> Create, update, and query restaurants</li>
 *   <li><b>Employee Management:</b> Track staff, roles, and assignments</li>
 *   <li><b>Workstation Management:</b> Organize work areas and task distribution</li>
 *   <li><b>Performance Monitoring:</b> Track KPIs like revenue, orders, and preparation time</li>
 *   <li><b>Inventory Control:</b> Manage ingredient stock levels per restaurant</li>
 * </ul>
 *
 * <h2>Business Rules</h2>
 * <ul>
 *   <li>Each restaurant has unique identification and location</li>
 *   <li>Employees are assigned to specific workstations</li>
 *   <li>Performance indicators are tracked daily per restaurant</li>
 *   <li>Stock levels are monitored for ingredient availability</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * // Get facade instance
 * IGestRestaurantes gestRest = new RestaurantesFacade();
 *
 * // Get all restaurants
 * List<Restaurante> restaurantes = gestRest.getRestaurantes();
 *
 * // Get performance indicators for a specific restaurant
 * List<IndicadorDesempenho> indicadores =
 *     gestRest.getIndicadoresDesempenho(restauranteId, startDate, endDate);
 * }</pre>
 *
 * @since 1.0
 * @version 1.0-SNAPSHOT
 * @author Group 8 - DSS Course 2025/26
 */
package dss.cadeiaRestaurantesLN.subsistemaRestaurantes;

