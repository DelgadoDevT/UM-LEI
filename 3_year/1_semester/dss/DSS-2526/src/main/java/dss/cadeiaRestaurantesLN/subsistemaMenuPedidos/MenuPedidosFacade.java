package dss.cadeiaRestaurantesLN.subsistemaMenuPedidos;

import dss.cadeiaRestaurantesDL.PedidoDAO;
import dss.cadeiaRestaurantesDL.ProdutoDAO;
import dss.cadeiaRestaurantesDL.MenuDAO;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Facade for the Menu and Orders Management subsystem.
 * Handles order lifecycle (creation, preparation, delivery), product catalog, and ingredient management.
 * Implements IGestMenuPedidos interface.
 */
public class MenuPedidosFacade implements IGestMenuPedidos {
    private PedidoDAO pedidoDAO;
    private MenuDAO menuDAO;
    private ProdutoDAO produtoDAO;

    /**
     * Default constructor. Initializes DAOs with singleton instances.
     */
    public MenuPedidosFacade() {
        this.pedidoDAO = PedidoDAO.getInstance();
        this.menuDAO = MenuDAO.getInstance();
        this.produtoDAO = ProdutoDAO.getInstance();
    }

    /**
     * Retrieves the sales catalog containing all available products and menu packs.
     * @return List of sellable items
     */
    // CU1
    public List<ArtigoVenda> getCatalogoVenda() {
        List<ArtigoVenda> catalogo = new ArrayList<>();
        catalogo.addAll(this.produtoDAO.values());

        for (Menu m : this.menuDAO.values()) {
            if (m instanceof MenuPack) catalogo.add((MenuPack) m);
        }
        return catalogo;
    }


    // --- Lógica para "Confecionar Pedido" / Gerir Preparação (CU3) ---
    @Override
    public List<String> consultarFilaPedidos(int idRestaurante) {
        List<Pedido> pedidos = this.pedidoDAO.getPedidosPorRestaurante(idRestaurante);
        List<String> infoPedidos = new ArrayList<>();

        String BOLD = "\u001B[1m";
        String RESET = "\u001B[0m";
        String CYAN = "\u001B[36m";
        String YELLOW = "\u001B[33m";

        for (Pedido p : pedidos) {
            if (p.getEstado() == Estado.REGISTADO || p.getEstado() == Estado.EM_PREPARACAO) {
                StringBuilder sb = new StringBuilder();
                String estadoStr = (p.getEstado() == Estado.EM_PREPARACAO) ? YELLOW + "[EM PREPARAÇÃO]" + RESET : "[REGISTADO]";

                // Cabeçalho da Lista
                sb.append(String.format("   %s🍳 PEDIDO #%-3d%s %-12s %s\n",
                        BOLD, p.getId(), RESET, "(" + p.getTipoConsumo() + ")", estadoStr));

                // Itens com Emojis corretos
                for (LinhaDePedido lp : p.getLinhas()) {
                    String icon = getEmojiArtigo(lp.getArtigo());
                    sb.append(String.format("      %2dx %s %s\n", lp.getQuantidade(), icon, lp.getArtigo().getNome()));

                    // Mostrar ingredientes personalizados (se for Produto)
                    if (lp.getArtigo() instanceof Produto) {
                        Produto produto = (Produto) lp.getArtigo();
                        String personalizacao = getPersonalizacaoIngredientes(produto);
                        if (!personalizacao.isEmpty()) {
                            sb.append(String.format("          %s🔧 %s%s\n", YELLOW, personalizacao, RESET));
                        }
                    }

                    if (lp.getNota() != null && !lp.getNota().isEmpty()) {
                        sb.append(String.format("          %s↳ Obs: %s%s\n", CYAN, lp.getNota(), RESET));
                    }
                }
                sb.append(YELLOW + "   ............................................" + RESET);
                infoPedidos.add(sb.toString());
            }
        }
        return infoPedidos;
    }

    @Override
    public void iniciarPreparacaoPedido(int idPedido) {
        Pedido p = this.pedidoDAO.get(idPedido);
        if (p == null) {
            throw new IllegalArgumentException("Pedido não encontrado: " + idPedido);
        }

        // Validação: Só pode iniciar preparação se o pedido estiver REGISTADO
        if (p.getEstado() != Estado.REGISTADO) {
            throw new IllegalStateException(
                "Não é possível iniciar preparação. Estado atual: " + p.getEstado() +
                ". Apenas pedidos no estado REGISTADO podem ser iniciados."
            );
        }

        p.setEstado(Estado.EM_PREPARACAO);
        this.pedidoDAO.put(p.getId(), p);
    }

    @Override
    public void concluirPreparacaoPedido(int idPedido) {
        Pedido p = this.pedidoDAO.get(idPedido);
        if (p == null) {
            throw new IllegalArgumentException("Pedido não encontrado: " + idPedido);
        }

        // Validação: Só pode concluir se o pedido estiver EM_PREPARACAO
        if (p.getEstado() != Estado.EM_PREPARACAO) {
            throw new IllegalStateException(
                "Não é possível marcar como pronto. Estado atual: " + p.getEstado() +
                ". Apenas pedidos no estado EM_PREPARACAO podem ser marcados como prontos."
            );
        }

        p.setEstado(Estado.PRONTO);
        this.pedidoDAO.put(p.getId(), p);
    }

    @Override
    public void cancelarPedido(int idPedido, String motivo) {
        Pedido p = this.pedidoDAO.get(idPedido);
        if (p == null) {
            throw new IllegalArgumentException("Pedido não encontrado: " + idPedido);
        }

        // Validação: Não pode cancelar se já estiver PRONTO ou ENTREGUE
        if (p.getEstado() == Estado.PRONTO || p.getEstado() == Estado.ENTREGUE) {
            throw new IllegalStateException(
                "Não é possível cancelar pedido. Estado atual: " + p.getEstado() +
                ". Pedidos nos estados PRONTO ou ENTREGUE não podem ser cancelados."
            );
        }

        // Validação: Não pode cancelar se já estiver CANCELADO
        if (p.getEstado() == Estado.CANCELADO) {
            throw new IllegalStateException("Pedido já está cancelado.");
        }

        p.setEstado(Estado.CANCELADO);
        this.pedidoDAO.put(p.getId(), p);
    }

    // --- Lógica para "Entregar Pedido" (CU6) ---

    @Override
    public List<String> consultarPedidosProntos(int idRestaurante) {
        List<Pedido> pedidos = this.pedidoDAO.getPedidosPorRestaurante(idRestaurante);
        List<String> infoPedidos = new ArrayList<>();

        String BOLD = "\u001B[1m";
        String RESET = "\u001B[0m";
        String GREEN = "\u001B[32m";

        for (Pedido p : pedidos) {
            if (p.getEstado() == Estado.PRONTO) {
                StringBuilder sb = new StringBuilder();
                sb.append(String.format("   %s⚡ PEDIDO #%-3d%s (%s) %sTotal: %.2f€%s\n",
                        BOLD, p.getId(), RESET, p.getTipoConsumo(),
                        GREEN, p.getValorTotal(), RESET));

                List<String> itensResumo = new ArrayList<>();
                for(LinhaDePedido lp : p.getLinhas()) {
                    // Apenas nome e quantidade para o resumo rápido
                    itensResumo.add(lp.getQuantidade() + "x " + lp.getArtigo().getNome());
                }
                sb.append("      📦 " + String.join(", ", itensResumo));

                infoPedidos.add(sb.toString());
            }
        }
        return infoPedidos;
    }

    @Override
    public void entregarPedido(int idPedido) {
        Pedido p = this.pedidoDAO.get(idPedido);
        if (p == null) {
            throw new IllegalArgumentException("Pedido não encontrado: " + idPedido);
        }

        // Validação: Só pode entregar se o pedido estiver PRONTO
        if (p.getEstado() != Estado.PRONTO) {
            throw new IllegalStateException(
                "Não é possível entregar pedido. Estado atual: " + p.getEstado() +
                ". Apenas pedidos no estado PRONTO podem ser entregues."
            );
        }

        p.setEstado(Estado.ENTREGUE);
        this.pedidoDAO.put(p.getId(), p);
    }

    @Override
    public void criarPedido(int idRestaurante, List<LinhaDePedido> linhas, Tipo tipoConsumo, Pagamento pagamento) {
        Pedido p = new Pedido();
        p.setId(0); // Auto-increment
        p.setIdRestaurante(idRestaurante); // Define o restaurante escolhido pelo cliente
        p.setEstado(Estado.REGISTADO);
        p.setTipoConsumo(tipoConsumo);
        p.setLinhas(linhas);

        // Calcula o total
        double total = linhas.stream().mapToDouble(l -> l.getPreco() * l.getQuantidade()).sum();
        p.setValorTotal(total);

        p.setPagamento(pagamento);
        if (pagamento != null) {
            pagamento.setIdPedido(p.getId()); // O DAO tratará de ligar os IDs corretamente
        }

        // Grava na BD
        PedidoDAO.getInstance().put(p.getId(), p);
    }

    private String getEmojiArtigo(ArtigoVenda a) {
        if (a instanceof MenuPack) return "📦";

        String nome = a.getNome().toLowerCase();
        if (nome.contains("hambúrguer") || nome.contains("burger")) return "🍔";
        if (nome.contains("batata")) return "🍟";
        if (nome.contains("salada")) return "🥗";
        if (nome.contains("bitoque") || nome.contains("prato")) return "🍛";
        if (nome.contains("frango")) return "🍗";
        if (nome.contains("sundae") || nome.contains("gelado")) return "🍦";
        if (nome.contains("refrigerante") || nome.contains("cola") || nome.contains("sumo")) return "🥤";
        if (nome.contains("água")) return "💧";
        if (nome.contains("vinho") || nome.contains("cerveja")) return "🍺";
        if (nome.contains("café")) return "☕";
        return "🍽 ";
    }

    /**
     * Detecta se um produto tem ingredientes personalizados (removidos ou adicionados)
     * e retorna uma string descritiva para mostrar ao cozinheiro.
     *
     * IMPORTANTE: Trata ingredientes duplicados (ex: 2x Bacon) corretamente
     */
    private String getPersonalizacaoIngredientes(Produto produto) {
        // Busca o produto original do catálogo/DAO para comparar
        Produto produtoOriginal = produtoDAO.get(produto.getId());

        if (produtoOriginal == null) {
            return ""; // Produto não encontrado no catálogo
        }

        Set<Ingrediente> ingredientesOriginais = produtoOriginal.getIngredientes();
        Set<Ingrediente> ingredientesAtuais = produto.getIngredientes();

        // Usar mapas para contar quantidades de cada ingrediente
        // Chave: nome do ingrediente (lowercase + trim), Valor: quantidade
        java.util.Map<String, Integer> contagemOriginais = new java.util.HashMap<>();
        for (Ingrediente ing : ingredientesOriginais) {
            String nomeNorm = ing.getNome().toLowerCase().trim();
            contagemOriginais.put(nomeNorm, contagemOriginais.getOrDefault(nomeNorm, 0) + 1);
        }

        java.util.Map<String, Integer> contagemAtuais = new java.util.HashMap<>();
        java.util.Map<String, String> nomeOriginalMap = new java.util.HashMap<>(); // Manter nome original com capitalização
        for (Ingrediente ing : ingredientesAtuais) {
            String nomeNorm = ing.getNome().toLowerCase().trim();
            contagemAtuais.put(nomeNorm, contagemAtuais.getOrDefault(nomeNorm, 0) + 1);
            nomeOriginalMap.put(nomeNorm, ing.getNome()); // Guardar o nome com capitalização original
        }

        // Detectar ingredientes REMOVIDOS
        List<String> removidos = new ArrayList<>();
        for (Ingrediente orig : ingredientesOriginais) {
            String nomeNorm = orig.getNome().toLowerCase().trim();
            int qtdOriginal = contagemOriginais.getOrDefault(nomeNorm, 0);
            int qtdAtual = contagemAtuais.getOrDefault(nomeNorm, 0);

            // Se tinha no original mas não tem agora, foi removido
            if (qtdAtual == 0 && qtdOriginal > 0) {
                if (!removidos.contains(orig.getNome())) {
                    removidos.add(orig.getNome());
                }
            }
        }

        // Detectar ingredientes ADICIONADOS (extras e quantidades extras)
        List<String> adicionados = new ArrayList<>();
        for (String nomeNorm : contagemAtuais.keySet()) {
            int qtdOriginal = contagemOriginais.getOrDefault(nomeNorm, 0);
            int qtdAtual = contagemAtuais.get(nomeNorm);
            int diferenca = qtdAtual - qtdOriginal;

            // Se tem mais agora do que no original, é extra
            if (diferenca > 0) {
                String nomeExibir = nomeOriginalMap.get(nomeNorm);
                if (diferenca == 1) {
                    adicionados.add(nomeExibir);
                } else {
                    // Se tem múltiplos extras do mesmo ingrediente, mostrar a quantidade
                    adicionados.add(diferenca + "x " + nomeExibir);
                }
            }
        }

        // Formatar a string de personalização
        StringBuilder sb = new StringBuilder();

        if (!removidos.isEmpty()) {
            sb.append("SEM ");
            sb.append(String.join(", ", removidos));
        }

        if (!adicionados.isEmpty()) {
            if (sb.length() > 0) sb.append(" | ");
            sb.append("EXTRA ");
            sb.append(String.join(", ", adicionados));
        }

        return sb.toString();
    }

    public List<Ingrediente> getIngredientesDisponiveis() {
        return new ArrayList<>(dss.cadeiaRestaurantesDL.IngredienteDAO.getInstance().values());
    }
}
