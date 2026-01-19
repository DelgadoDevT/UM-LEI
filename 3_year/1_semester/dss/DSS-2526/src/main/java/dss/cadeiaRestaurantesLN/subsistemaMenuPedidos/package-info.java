/**
 * Menu and Order Management Subsystem.
 * <p>
 * This subsystem handles all operations related to menus, products, ingredients,
 * orders (pedidos), and payments. It manages the complete order lifecycle from
 * menu composition to order fulfillment and payment processing.
 * </p>
 *
 * <h2>Main Components</h2>
 * <ul>
 *   <li>{@link dss.cadeiaRestaurantesLN.subsistemaMenuPedidos.IGestMenuPedidos} - Menu and order management interface</li>
 *   <li>{@link dss.cadeiaRestaurantesLN.subsistemaMenuPedidos.MenuPedidosFacade} - Facade for menu/order operations</li>
 * </ul>
 *
 * <h2>Domain Entities</h2>
 *
 * <h3>Menu &amp; Products</h3>
 * <ul>
 *   <li>{@link dss.cadeiaRestaurantesLN.subsistemaMenuPedidos.Menu} - Menu containing products</li>
 *   <li>{@link dss.cadeiaRestaurantesLN.subsistemaMenuPedidos.Produto} - Individual product/dish</li>
 *   <li>{@link dss.cadeiaRestaurantesLN.subsistemaMenuPedidos.Ingrediente} - Ingredient with allergens</li>
 *   <li>{@link dss.cadeiaRestaurantesLN.subsistemaMenuPedidos.ArtigoVenda} - Abstract sellable item</li>
 *   <li>{@link dss.cadeiaRestaurantesLN.subsistemaMenuPedidos.MenuPack} - Product bundle/pack</li>
 *   <li>{@link dss.cadeiaRestaurantesLN.subsistemaMenuPedidos.MenuCategoria} - Product categorized in menu</li>
 * </ul>
 *
 * <h3>Orders &amp; Payments</h3>
 * <ul>
 *   <li>{@link dss.cadeiaRestaurantesLN.subsistemaMenuPedidos.Pedido} - Order entity</li>
 *   <li>{@link dss.cadeiaRestaurantesLN.subsistemaMenuPedidos.LinhaDePedido} - Order line item</li>
 *   <li>{@link dss.cadeiaRestaurantesLN.subsistemaMenuPedidos.Pagamento} - Payment processing</li>
 *   <li>{@link dss.cadeiaRestaurantesLN.subsistemaMenuPedidos.Cartao_Bancario} - Card payment</li>
 *   <li>{@link dss.cadeiaRestaurantesLN.subsistemaMenuPedidos.Dinheiro} - Cash payment</li>
 * </ul>
 *
 * <h3>Enumerations</h3>
 * <ul>
 *   <li>{@link dss.cadeiaRestaurantesLN.subsistemaMenuPedidos.Estado} - Order state (PENDING, PREPARING, READY, COMPLETED)</li>
 *   <li>{@link dss.cadeiaRestaurantesLN.subsistemaMenuPedidos.Tamanho} - Product size (SMALL, MEDIUM, LARGE)</li>
 * </ul>
 *
 * <h2>Key Features</h2>
 * <ul>
 *   <li><b>Menu Management:</b> Create and organize menus with products and categories</li>
 *   <li><b>Product Composition:</b> Define products with ingredients and allergen tracking</li>
 *   <li><b>Order Processing:</b> Handle complete order lifecycle with state transitions</li>
 *   <li><b>Payment Processing:</b> Support multiple payment methods (card, cash)</li>
 *   <li><b>Customization:</b> Allow product customization with notes per order line</li>
 *   <li><b>Pricing:</b> Calculate order totals with bundle discounts</li>
 * </ul>
 *
 * <h2>Order Lifecycle</h2>
 * <ol>
 *   <li><b>PENDING:</b> Order received, awaiting confirmation</li>
 *   <li><b>PREPARING:</b> Order being prepared in kitchen</li>
 *   <li><b>READY:</b> Order ready for pickup/delivery</li>
 *   <li><b>COMPLETED:</b> Order fulfilled and delivered</li>
 * </ol>
 *
 * <h2>Business Rules</h2>
 * <ul>
 *   <li>Products must specify ingredients for allergen tracking</li>
 *   <li>Menu packs can offer discounted pricing vs individual items</li>
 *   <li>Each order line can have custom notes (e.g., "no onions")</li>
 *   <li>Orders track consumption type (dine-in, takeout, delivery)</li>
 *   <li>Payment must be processed before order completion</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * // Get facade instance
 * IGestMenuPedidos gestMenu = new MenuPedidosFacade();
 *
 * // Get available products
 * List<Produto> produtos = gestMenu.getProdutos();
 *
 * // Create and register an order
 * Pedido pedido = new Pedido();
 * pedido.setIdRestaurante(1);
 * pedido.setTipoConsumo("TAKE_AWAY");
 *
 * LinhaDePedido linha = new LinhaDePedido();
 * linha.setArtigo(produto);
 * linha.setQuantidade(2);
 * pedido.addLinha(linha);
 *
 * gestMenu.registarPedido(pedido);
 *
 * // Process payment
 * Pagamento pagamento = new Cartao_Bancario(pedido.getValorTotal());
 * gestMenu.processarPagamento(pedido.getId(), pagamento);
 * }</pre>
 *
 * @since 1.0
 * @version 1.0-SNAPSHOT
 * @author Group 8 - DSS Course 2025/26
 */
package dss.cadeiaRestaurantesLN.subsistemaMenuPedidos;

