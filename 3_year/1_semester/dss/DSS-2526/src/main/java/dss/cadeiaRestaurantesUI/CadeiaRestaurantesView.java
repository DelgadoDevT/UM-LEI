package dss.cadeiaRestaurantesUI;

import dss.cadeiaRestaurantesLN.subsistemaMenuPedidos.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * View layer for the Restaurant Chain Management System.
 * Provides a console-based user interface for customers and employees.
 * Handles menu display, user input, and output formatting with color-coded messages.
 */
public class CadeiaRestaurantesView {
    private final CadeiaRestaurantesController controller;
    private final Scanner scanner;

    // ANSI color codes for console output formatting
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[94m";
    private static final String PURPLE = "\u001B[35m";
    private static final String CYAN = "\u001B[36m";
    private static final String BOLD = "\u001B[1m";

    /**
     * Constructor with controller.
     * @param controller UI controller
     */
    public CadeiaRestaurantesView(CadeiaRestaurantesController controller) {
        this.controller = controller;
        this.scanner = new Scanner(System.in);
    }

    /**
     * Main loop for the application.
     * Continuously displays the main menu until user exits.
     */
    public void run() {
        while (true) {
            menuInicial();
        }
    }

    private void menuInicial() {
        System.out.println("\n" + BOLD + CYAN + "╔══════════════════════════════════════════╗" + RESET);
        System.out.println(BOLD + CYAN + "║" + RESET + "      🍽  SISTEMA DSS RESTAURANTES        " + BOLD + CYAN + " ║" + RESET);
        System.out.println(BOLD + CYAN + "╚══════════════════════════════════════════╝" + RESET);

        System.out.println("   1. 🍔 Fazer Pedido (Cliente)");
        System.out.println("   2. 🔐 Área de Funcionário");
        System.out.println("   3. 🚪 Sair");

        System.out.print(CYAN + "\n👉 Opção: " + RESET);

        String opcao = scanner.nextLine().trim();

        switch (opcao) {
            case "1" -> criarPedido();
            case "2" -> {
                menuAutenticacao();
                if (controller.isAutenticado()) {
                    menuFuncionario();
                }
            }
            case "3" -> {
                System.out.println(YELLOW + "A encerrar sistema... Até logo! 👋" + RESET);
                System.exit(0);
            }
            default -> System.out.println(RED + "Opção inválida" + RESET);
        }
    }

    private void menuAutenticacao() {
        while (true) {
            System.out.println("\n" + BOLD + PURPLE + "╔══════════════════════════════════════════╗" + RESET);
            System.out.println(BOLD + PURPLE + "║" + RESET + "       🔐  AUTENTICAÇÃO DE SISTEMA        " + BOLD + PURPLE + "║" + RESET);
            System.out.println(BOLD + PURPLE + "╚══════════════════════════════════════════╝" + RESET);
            System.out.print(CYAN + "👉 ID de Funcionário (-1 para voltar): " + RESET);

            try {
                String input = scanner.nextLine().trim();
                if (input.equals("-1")) return;

                int id = Integer.parseInt(input);

                if (controller.autenticarFuncionario(id)) {
                    System.out.println(GREEN + "✔ Login efetuado com sucesso." + RESET);
                    break;
                } else {
                    System.out.println(RED + "❌ Acesso negado. ID desconhecido." + RESET);
                }
            } catch (NumberFormatException e) {
                System.out.println(RED + "⚠ O ID deve ser numérico." + RESET);
            }
        }
    }

    private void menuFuncionario() {
        // Apenas para direcionar, não precisa de visual,
        // mas garante que o logout mostra mensagem bonita.
        while (controller.isAutenticado()) {
            if (controller.isGestor()) {
                menuGestor();
            } else if (controller.isCozinheiro()) {
                menuCozinheiro();
            } else if (controller.isAtendente()) {
                menuAtendente();
            } else {
                controller.logout();
                return;
            }
        }
        System.out.println(YELLOW + "🔒 Sessão terminada." + RESET);
    }

    private void menuGestor() {
        System.out.println("\n" + BOLD + PURPLE + "╔══════════════════════════════════════════╗" + RESET);
        System.out.println(BOLD + PURPLE + "║" + RESET + "       💼  ADMINISTRAÇÃO & GESTÃO         " + BOLD + PURPLE + "║" + RESET);
        System.out.println(BOLD + PURPLE + "╚══════════════════════════════════════════╝" + RESET);

        System.out.println("   1. 📢 Enviar Mensagem Global");
        System.out.println("   2. 📊 Consultar Indicadores");
        System.out.println("   3. 📨 Ver Mensagens Recebidas");
        System.out.println("   4. 👤 O Meu Perfil");
        System.out.println("   5. 🔓 Logout");

        System.out.print(CYAN + "\n👉 Opção: " + RESET);

        String opcao = scanner.nextLine().trim();

        switch (opcao) {
            case "1" -> enviarMensagem();
            case "2" -> consultarIndicadores();
            case "3" -> verMensagens();
            case "4" -> verPerfil();
            case "5" -> controller.logout();
            default -> System.out.println(RED + "Opção inválida" + RESET);
        }
    }

    private void menuCozinheiro() {
        System.out.println(BOLD + GREEN + "\n=== Menu Cozinheiro ===" + RESET);
        System.out.println("1. Alterar estado pedido");
        System.out.println("2. Listar pedidos em preparação");
        System.out.println("3. Ver mensagens");
        System.out.println("4. Ver perfil");
        System.out.println("5. Logout");
        System.out.print("Opção: ");

        String opcao = scanner.nextLine().trim();

        switch (opcao) {
            case "1" -> alterarEstadoPedido();
            case "2" -> listarPedidosCozinha();
            case "3" -> verMensagens();
            case "4" -> verPerfil();
            case "5" -> controller.logout();
            default -> System.out.println("Opção inválida");
        }
    }

    private void menuAtendente() {
        System.out.println(BOLD + YELLOW + "\n=== Menu Atendente ===" + RESET);
        System.out.println("1. Marcar pedido concluído");
        System.out.println("2. Listar pedidos prontos");
        System.out.println("3. Ver mensagens");
        System.out.println("4. Ver perfil");
        System.out.println("5. Logout");
        System.out.print("Opção: ");

        String opcao = scanner.nextLine().trim();

        switch (opcao) {
            case "1" -> marcarPedidoConcluido();
            case "2" -> listarPedidosProntos();
            case "3" -> verMensagens();
            case "4" -> verPerfil();
            case "5" -> controller.logout();
            default -> System.out.println("Opção inválida");
        }
    }

    private void criarPedido() {
        System.out.println(BOLD + GREEN + "\n=== 🍔 NOVO PEDIDO 🍔 ===" + RESET);

        try {
            // 1. SELEÇÃO DO RESTAURANTE
            System.out.println("\n📍 Selecione o restaurante:");
            List<String> restaurantes = controller.getRestaurantesPublico();

            if (restaurantes.isEmpty()) {
                System.out.println(RED + "⚠ Indisponível." + RESET);
                return;
            }

            for (String r : restaurantes) {
                System.out.println("   " + r.replace("Restaurante:", "🏢"));
            }

            System.out.print(CYAN + "👉 ID do restaurante: " + RESET);
            String idStr = scanner.nextLine().trim();
            if(idStr.isEmpty()) return;
            int idRestaurante = Integer.parseInt(idStr);

            // 2. SELEÇÃO DE ARTIGOS
            List<LinhaDePedido> linhasPedido = new ArrayList<>();
            List<ArtigoVenda> catalogo = controller.getCatalogoVenda();

            boolean adicionarMais = true;
            while (adicionarMais) {
                System.out.println("\n" + BOLD + "📜 CATÁLOGO" + RESET);

                for (int i = 0; i < catalogo.size(); i++) {
                    ArtigoVenda a = catalogo.get(i);
                    String icon = getEmojiArtigo(a);

                    System.out.printf("   %2d. %s %-25s " + GREEN + "%6.2f€" + RESET + "\n",
                            (i + 1), icon, a.getNome(), a.getPreco());
                }
                System.out.println("    0. ✅ Finalizar seleção");

                System.out.print(CYAN + "👉 Escolha o nº do artigo: " + RESET);
                String escolhaStr = scanner.nextLine().trim();
                if (escolhaStr.isEmpty()) continue;
                int escolha = Integer.parseInt(escolhaStr);

                if (escolha == 0) break;
                if (escolha < 0 || escolha > catalogo.size()) {
                    System.out.println(RED + "Opção inválida." + RESET);
                    continue;
                }

                ArtigoVenda selecionado = catalogo.get(escolha - 1);

                System.out.print("   🔢 Quantidade: ");
                int qtd = Integer.parseInt(scanner.nextLine().trim());

                LinhaDePedido linha = new LinhaDePedido(0, 0, qtd, selecionado.getPreco(), selecionado, "");

                // Ingredientes
                if (selecionado instanceof Produto) {
                    System.out.print("   📝 Personalizar? (s/n): ");
                    if (scanner.nextLine().trim().equalsIgnoreCase("s")) {
                        ajustarIngredientesNaView(linha);
                    }
                }

                System.out.print("   💬 Nota (Enter para vazio): ");
                linha.setNota(scanner.nextLine().trim());

                linhasPedido.add(linha);
                System.out.println(GREEN + "   ✔ Item adicionado!" + RESET);

                System.out.print(CYAN + "\n👉 Adicionar mais itens? (s/n) [S]: " + RESET);
                String mais = scanner.nextLine().trim();
                adicionarMais = mais.isEmpty() || mais.equalsIgnoreCase("s");
            }

            if (linhasPedido.isEmpty()) return;

            // 3. FINALIZAÇÃO
            System.out.println("\n🍽 Tipo de Consumo:");
            System.out.println("   1. Local 🪑");
            System.out.println("   2. Take-away 👜");
            System.out.print(CYAN + "👉 Opção: " + RESET);
            Tipo tipoStr = Tipo.valueOf(scanner.nextLine().trim().equals("2") ? "TAKE_AWAY" : "LOCAL");

            double totalProv = linhasPedido.stream().mapToDouble(l -> l.getPreco() * l.getQuantidade()).sum();
            System.out.println("\n" + BOLD + "💰 TOTAL A PAGAR: " + GREEN + String.format("%.2f€", totalProv) + RESET);

            System.out.println("\n💳 Pagamento:");
            System.out.println("   1. Dinheiro 💵");
            System.out.println("   2. MBWay 📱");
            System.out.println("   3. Cartão 💳");
            System.out.print(CYAN + "👉 Método: " + RESET);

            String met = scanner.nextLine().trim();
            Pagamento pag;
            switch (met) {
                case "2" -> {
                    // LÓGICA DO MBWAY (PEDIR CONTACTO)
                    System.out.print("   📱 Nº de Telemóvel: ");
                    String telemovel = scanner.nextLine().trim();
                    while(telemovel.length() < 9) { // Validação básica
                        System.out.print(RED + "   Número inválido. Tente novamente: " + RESET);
                        telemovel = scanner.nextLine().trim();
                    }
                    pag = new MBWay(totalProv);
                }
                case "3" -> pag = new Cartao_Bancario(totalProv);
                default -> pag = new Dinheiro(totalProv);
            }

            controller.criarPedido(idRestaurante, linhasPedido, tipoStr, pag);

            System.out.println(BOLD + GREEN + "\n✅ Pedido registado com sucesso!" + RESET);
            System.out.println("   O seu número será chamado em breve.\n");

            System.out.println("(Pressione Enter para voltar ao menu)");
            scanner.nextLine();

        } catch (Exception e) {
            System.out.println(RED + "❌ Erro ao criar pedido: " + e.getMessage() + RESET);
        }
    }

    private void ajustarIngredientesNaView(LinhaDePedido linha) {
        Produto p = (Produto) linha.getArtigo();

        Set<String> ingredientesOriginais = new HashSet<>();
        for (Ingrediente ing : p.getIngredientes()) {
            ingredientesOriginais.add(ing.getNome());
        }

        List<Ingrediente> atuais = new ArrayList<>(p.getIngredientes());
        boolean continuarPersonalizacao = true;

        while (continuarPersonalizacao) {
            System.out.println("\n" + BOLD + YELLOW + "   🥗 INGREDIENTES ATUAIS:" + RESET);
            System.out.println(CYAN + "   ───────────────────────" + RESET);

            if (atuais.isEmpty()) {
                System.out.println("   " + YELLOW + "(Sem ingredientes)" + RESET);
            } else {
                for (int i = 0; i < atuais.size(); i++) {
                    Ingrediente ing = atuais.get(i);
                    String emoji = getEmojiIngrediente(ing.getNome());
                    System.out.printf("   %2d. %s %-20s " + GREEN + "%5.2f€" + RESET + "\n",
                        (i + 1), emoji, ing.getNome(), ing.getPreco());
                }
            }

            System.out.println("\n" + BOLD + "🛠 PERSONALIZAR:" + RESET);
            System.out.println("   1. ➖ Retirar ingrediente");
            System.out.println("   2. ➕ Adicionar extra");
            System.out.println("   0. ✅ Concluir personalização");
            System.out.print(CYAN + "👉 Opção: " + RESET);
            String op = scanner.nextLine().trim();

            if (op.equals("0")) {
                break;
            } else if (op.equals("1")) {
                System.out.print(CYAN + "   📝 Nome do ingrediente a retirar: " + RESET);
                String nome = scanner.nextLine().trim();
                boolean removido = atuais.removeIf(i -> i.getNome().equalsIgnoreCase(nome));
                if (removido) {
                    System.out.println(GREEN + "   ✔ Ingrediente removido!" + RESET);
                } else {
                    System.out.println(RED + "   ⚠ Ingrediente não encontrado." + RESET);
                }
            } else if (op.equals("2")) {
                // Buscar ingredientes disponíveis do sistema
                List<Ingrediente> ingredientesDisponiveis = controller.getIngredientesDisponiveis();

                if (ingredientesDisponiveis.isEmpty()) {
                    System.out.println(RED + "   ⚠ Nenhum ingrediente disponível no momento." + RESET);
                    continue; // Volta ao menu de personalização
                }

                System.out.println("\n" + BOLD + YELLOW + "   🛒 INGREDIENTES DISPONÍVEIS:" + RESET);
                System.out.println(CYAN + "   ───────────────────────" + RESET);

                for (int i = 0; i < ingredientesDisponiveis.size(); i++) {
                    Ingrediente ing = ingredientesDisponiveis.get(i);
                    String emoji = getEmojiIngrediente(ing.getNome());
                    System.out.printf("   %2d. %s %-20s " + GREEN + "%5.2f€" + RESET + "\n",
                        (i + 1), emoji, ing.getNome(), ing.getPreco());
                }

                System.out.print(CYAN + "\n   📝 Escolha o nº do ingrediente (0 para cancelar): " + RESET);
                String escolhaStr = scanner.nextLine().trim();

                if (escolhaStr.equals("0")) {
                    continue; // Volta ao menu de personalização
                }

                try {
                    int escolha = Integer.parseInt(escolhaStr);
                    if (escolha > 0 && escolha <= ingredientesDisponiveis.size()) {
                        Ingrediente extra = ingredientesDisponiveis.get(escolha - 1);
                        atuais.add(extra);
                        System.out.println(GREEN + "   ✔ Extra adicionado: " + extra.getNome() + " (+" + String.format("%.2f€", extra.getPreco()) + ")" + RESET);
                    } else {
                        System.out.println(RED + "   ⚠ Opção inválida." + RESET);
                    }
                } catch (NumberFormatException e) {
                    System.out.println(RED + "   ⚠ Deve inserir um número." + RESET);
                }
            } else {
                System.out.println(RED + "   ⚠ Opção inválida." + RESET);
            }

            // Perguntar se quer fazer mais alterações (se não escolheu sair)
            if (!op.equals("0")) {
                System.out.print(CYAN + "\n   👉 Fazer mais alterações? (s/n) [S]: " + RESET);
                String maisAlteracoes = scanner.nextLine().trim();
                continuarPersonalizacao = maisAlteracoes.isEmpty() || maisAlteracoes.equalsIgnoreCase("s");
            }
        }

        // IMPORTANTE: Aplicar as mudanças ao produto
        p.setIngredientes(new HashSet<>(atuais));

        // Recalcular preço: somar apenas ingredientes ADICIONADOS (extras)
        double precoExtra = 0;
        for (Ingrediente i : atuais) {
            if (!ingredientesOriginais.contains(i.getNome())) {
                precoExtra += i.getPreco();
            }
        }

        linha.setPreco(p.getPreco() + precoExtra);

        if (precoExtra > 0) {
            System.out.println(YELLOW + "   💰 Preço atualizado: " + String.format("%.2f€", p.getPreco()) +
                             " + " + String.format("%.2f€", precoExtra) + " (extras) = " +
                             GREEN + String.format("%.2f€", linha.getPreco()) + RESET);
        }
    }

    private void alterarEstadoPedido() {
        System.out.println(BOLD + YELLOW + "\n=== 🍳 GESTÃO DE COZINHA ===" + RESET);
        try {
            List<String> pedidos = controller.getPedidosCozinha();

            if (pedidos.isEmpty()) {
                System.out.println(GREEN + "✅ Tudo limpo! Não há pedidos pendentes." + RESET);
                return;
            }

            System.out.println("\n📋 FILA DE PREPARAÇÃO:");
            for (String s : pedidos) {
                System.out.println(s);
            }

            System.out.print(CYAN + "\n👉 ID do Pedido para alterar (-1 para voltar): " + RESET);
            String input = scanner.nextLine().trim();
            if (input.equals("-1")) return;

            int idPedido = Integer.parseInt(input);

            System.out.println("\n🛠 AÇÃO:");
            System.out.println("   1. 🔥 Iniciar Preparação");
            System.out.println("   2. 🔔 Marcar como Pronto");
            System.out.println("   3. ❌ Cancelar Pedido");
            System.out.print(CYAN + "👉 Opção: " + RESET);

            String opcaoEstado = scanner.nextLine().trim();

            switch (opcaoEstado) {
                case "1" -> {
                    controller.iniciarPreparacaoPedido(idPedido);
                    System.out.println(GREEN + "✔ Pedido #" + idPedido + " a ser preparado!" + RESET);
                }
                case "2" -> {
                    controller.concluirPreparacaoPedido(idPedido);
                    System.out.println(GREEN + "✔ Pedido #" + idPedido + " está PRONTO para entrega!" + RESET);
                }
                case "3" -> {
                    System.out.print("Motivo: ");
                    String motivo = scanner.nextLine().trim();
                    controller.cancelarPedido(idPedido, motivo.isEmpty() ? "Cancelado na cozinha" : motivo);
                    System.out.println(RED + "⚠ Pedido #" + idPedido + " cancelado." + RESET);
                }
                default -> System.out.println(RED + "Opção inválida" + RESET);
            }

            // Pausa rápida para ler a mensagem
            Thread.sleep(800);

        } catch (NumberFormatException e) {
            System.out.println(RED + "Erro: O ID deve ser um número." + RESET);
        } catch (Exception e) {
            System.out.println(RED + "Erro: " + e.getMessage() + RESET);
        }
    }

    private void marcarPedidoConcluido() {
        System.out.println(BOLD + BLUE + "\n=== 🛎 BALCÃO DE ENTREGA ===" + RESET);
        try {
            List<String> pedidos = controller.getPedidosParaEntrega();

            if (pedidos.isEmpty()) {
                System.out.println("📭 Não há pedidos prontos para entregar.");
                return;
            }

            System.out.println("\n📦 PRONTOS PARA ENTREGA:");
            for (String s : pedidos) {
                System.out.println(s);
            }

            System.out.print(CYAN + "\n👉 ID do Pedido entregue (-1 para voltar): " + RESET);
            String input = scanner.nextLine().trim();
            if (input.equals("-1")) return;

            int idPedido = Integer.parseInt(input);

            controller.registarEntrega(idPedido);
            System.out.println(GREEN + "✔ Sucesso! Pedido #" + idPedido + " entregue ao cliente." + RESET);
            Thread.sleep(800);

        } catch (NumberFormatException e) {
            System.out.println(RED + "Erro: ID inválido." + RESET);
        } catch (Exception e) {
            System.out.println(RED + "Erro: " + e.getMessage() + RESET);
        }
    }

    private void listarPedidosCozinha() {
        System.out.println(BOLD + GREEN + "\n=== Lista de Pedidos em Preparação ===" + RESET);
        try {
            List<String> pedidos = controller.getPedidosCozinha();

            if (pedidos.isEmpty()) {
                System.out.println("Não há pedidos em preparação no momento.");
                return;
            }

            System.out.println("Total de pedidos: " + pedidos.size());
            System.out.println();
            for (String s : pedidos) {
                System.out.println(s);
            }

            System.out.println("\nPressione ENTER para continuar...");
            scanner.nextLine();

        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void listarPedidosProntos() {
        System.out.println(BOLD + YELLOW + "\n=== Lista de Pedidos Prontos ===" + RESET);
        try {
            List<String> pedidos = controller.getPedidosParaEntrega();

            if (pedidos.isEmpty()) {
                System.out.println("Não há pedidos prontos para entrega no momento.");
                return;
            }

            System.out.println("Total de pedidos: " + pedidos.size());
            System.out.println();
            for (String s : pedidos) {
                System.out.println(s);
            }

            System.out.println("\nPressione ENTER para continuar...");
            scanner.nextLine();

        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void verMensagens() {
        try {
            List<String> mensagens = controller.getMensagensPosto();

            String titulo = "📨 MENSAGENS";
            String cor = RESET;
            if (controller.isGestor()) { cor = PURPLE; }
            else if (controller.isCozinheiro()) { cor = YELLOW; titulo = "📨 MENSAGENS DA COZINHA"; }
            else if (controller.isAtendente()) { cor = BLUE; titulo = "📨 MENSAGENS DO BALCÃO"; }

            System.out.println("\n" + BOLD + cor + "   " + titulo + RESET);

            if (mensagens.isEmpty()) {
                System.out.println("   (Caixa de entrada vazia)");
            } else {
                for (String msg : mensagens) {
                    System.out.println("   📩 " + msg);
                }
            }

            System.out.println("\n(Enter para continuar)");
            scanner.nextLine();

        } catch (Exception e) {
            System.out.println(RED + "Erro: " + e.getMessage() + RESET);
        }
    }

    private void enviarMensagem() {
        System.out.print("Mensagem: ");
        String mensagem = scanner.nextLine();
        if (mensagem.trim().isEmpty()) {
            System.out.println("Mensagem não pode estar vazia");
            return;
        }

        List<Integer> restaurantes = null;

        if (controller.isGestorGeral()) {
            List<String> todosRestaurantes = controller.getRestaurantesComNomes();
            System.out.println("Restaurantes disponíveis:");
            for (String restauranteInfo : todosRestaurantes) {
                System.out.println("  " + restauranteInfo);
            }

            System.out.print("IDs restaurantes (separados por vírgula, -1=todos): ");
            String restaurantesStr = scanner.nextLine().trim();

            if (restaurantesStr.equals("-1")) {
                restaurantes = new ArrayList<>();
                for (String restauranteInfo : todosRestaurantes) {
                    String[] parts = restauranteInfo.split(":");
                    if (parts.length == 2) {
                        restaurantes.add(Integer.parseInt(parts[1]));
                    }
                }
            } else if (!restaurantesStr.isEmpty()) {
                restaurantes = new ArrayList<>();
                String[] idsArray = restaurantesStr.split(",");
                for (String idStr : idsArray) {
                    try {
                        int id = Integer.parseInt(idStr.trim());
                        boolean encontrado = false;
                        for (String restauranteInfo : todosRestaurantes) {
                            String[] parts = restauranteInfo.split(":");
                            if (parts.length == 2 && Integer.parseInt(parts[1]) == id) {
                                encontrado = true;
                                break;
                            }
                        }
                        if (encontrado) {
                            restaurantes.add(id);
                        } else {
                            System.out.println("Restaurante inválido: " + idStr);
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("ID inválido ignorado: " + idStr);
                    }
                }
            }

            if (restaurantes == null || restaurantes.isEmpty()) {
                System.out.println("Nenhum restaurante selecionado");
                return;
            }
        }

        Set<String> tiposPostoValidos = controller.getTiposPostoValidosParaGestor();
        if (tiposPostoValidos.isEmpty()) {
            System.out.println("Não tem permissão para enviar mensagens para nenhum posto");
            return;
        }

        System.out.println("Tipos de posto disponíveis: " + String.join(", ", tiposPostoValidos));
        System.out.print("Tipos de posto (separados por vírgula): ");
        String postosInput = scanner.nextLine().trim();

        if (postosInput.isEmpty()) {
            System.out.println("Deve especificar pelo menos um tipo de posto");
            return;
        }

        Set<String> tiposPostoSelecionados = new HashSet<>();
        String[] postosArray = postosInput.split(",");
        boolean postosInvalidos = false;

        for (String p : postosArray) {
            String tipoPosto = p.trim().toUpperCase();
            if (tiposPostoValidos.contains(tipoPosto)) {
                tiposPostoSelecionados.add(tipoPosto);
            } else {
                System.out.println("Tipo de posto inválido: " + p);
                postosInvalidos = true;
            }
        }

        if (postosInvalidos && tiposPostoSelecionados.isEmpty()) {
            System.out.println("Nenhum tipo de posto válido selecionado");
            return;
        }

        try {
            controller.enviarMensagemParaRestaurantes(mensagem, new ArrayList<>(tiposPostoSelecionados), restaurantes);
            System.out.println("Mensagem enviada com sucesso");
        } catch (Exception e) {
            System.out.println("Erro ao enviar mensagem: " + e.getMessage());
        }
    }

    private void consultarIndicadores() {
        System.out.println("\n" + BOLD + PURPLE + "╔══════════════════════════════════════════╗" + RESET);
        System.out.println(BOLD + PURPLE + "║" + RESET + "      📊  RELATÓRIOS DE DESEMPENHO        " + BOLD + PURPLE + "║" + RESET);
        System.out.println(BOLD + PURPLE + "╚══════════════════════════════════════════╝" + RESET);

        try {
            // 1. Inputs de Data
            System.out.print(CYAN + "\n📅 Data Início (YYYY-MM-DD): " + RESET);
            String inicioStr = scanner.nextLine().trim();
            if (inicioStr.equals("-1")) return;
            LocalDate inicio = LocalDate.parse(inicioStr);

            System.out.print(CYAN + "📅 Data Fim (YYYY-MM-DD):    " + RESET);
            String fimStr = scanner.nextLine().trim();
            LocalDate fim = LocalDate.parse(fimStr);

            if (fim.isBefore(inicio)) {
                System.out.println(RED + "⚠ A data de fim deve ser posterior à data de início." + RESET);
                return;
            }

            // 2. Lógica de Seleção de Restaurantes
            List<Integer> restaurantesSelecionados = new ArrayList<>();

            // Se for o Gestor Geral, permitimos escolher quais restaurantes consultar
            if (controller.isGestorGeral()) {
                System.out.println("\n" + BOLD + "🏢 Restaurantes disponíveis:" + RESET);
                List<String> todosRestaurantes = controller.getRestaurantesComNomes();

                if (todosRestaurantes.isEmpty()) {
                    System.out.println(RED + "⚠ Nenhum restaurante encontrado no sistema." + RESET);
                    return;
                }

                // Mostrar a lista
                for (String r : todosRestaurantes) {
                    System.out.println("   " + r.replace("Restaurante:", "🔹"));
                }

                System.out.println(CYAN + "\n👉 IDs dos restaurantes (separados por vírgula)");
                System.out.print("   (Enter ou -1 para selecionar TODOS): " + RESET);
                String restaurantesStr = scanner.nextLine().trim();

                // Lógica para selecionar TODOS
                if (restaurantesStr.isEmpty() || restaurantesStr.equals("-1")) {
                    for (String r : todosRestaurantes) {
                        try {
                            if(r.contains(":")) {
                                String idPart = r.substring(r.lastIndexOf(":") + 1).trim();
                                restaurantesSelecionados.add(Integer.parseInt(idPart));
                            }
                        } catch (Exception e) {
                            System.err.println("Erro ao parsear restaurante: " + r);
                        }
                    }
                    System.out.println(GREEN + "✔ Todos os restaurantes selecionados." + RESET);
                } else {
                    // Lógica para selecionar ESPECÍFICOS
                    String[] idsArray = restaurantesStr.split(",");
                    for (String idStr : idsArray) {
                        try {
                            restaurantesSelecionados.add(Integer.parseInt(idStr.trim()));
                        } catch (NumberFormatException e) {
                            System.out.println(RED + "⚠ ID inválido ignorado: " + idStr + RESET);
                        }
                    }
                }

                if (restaurantesSelecionados.isEmpty()) {
                    System.out.println(RED + "⚠ Nenhum restaurante válido selecionado." + RESET);
                    return;
                }
            }

            System.out.println(YELLOW + "\n⏳ A processar dados..." + RESET);
            List<String> indicadores = controller.consultarIndicadoresDesempenho(inicio, fim, restaurantesSelecionados);

            System.out.println("\n" + BOLD + GREEN + "╔══════════════════════════════════════════╗" + RESET);
            System.out.println(BOLD + GREEN + "║" + RESET + "  📈 RESULTADOS (" + inicio + " a " + fim + ")" + BOLD + GREEN + " ║" + RESET);
            System.out.println(BOLD + GREEN + "╚══════════════════════════════════════════╝" + RESET);

            if (indicadores == null || indicadores.isEmpty()) {
                System.out.println("\n" + YELLOW + "   ⚠ Sem dados registados para este período." + RESET);
                System.out.println("   " + CYAN + "💡 Dica: Verifique se há pedidos finalizados" + RESET);
                System.out.println("   " + CYAN + "   nas datas selecionadas." + RESET);
            } else {
                for (String indicador : indicadores) {
                    // Formatação visual da string crua que vem do backend
                    String bonita = indicador
                            .replace("Restaurante", BOLD + "🏢 Restaurante" + RESET)
                            .replace("Faturação:", GREEN + "💰 Faturação:" + RESET)
                            .replace("Pedidos:", BLUE + "🧾 Pedidos:" + RESET)
                            .replace("Tempo Médio:", YELLOW + "⏱ Tempo Médio:" + RESET);

                    System.out.println("\n   " + bonita);
                    System.out.println(CYAN + "   ────────────────────────────────" + RESET);
                }
                System.out.println("\n" + GREEN + "✓ Relatório gerado com sucesso!" + RESET);
            }

            System.out.println("\n" + CYAN + "(Enter para voltar)" + RESET);
            scanner.nextLine();

        } catch (DateTimeParseException e) {
            System.out.println(RED + "❌ Data inválida. Use o formato YYYY-MM-DD (ex: 2024-01-15)." + RESET);
        } catch (Exception e) {
            System.out.println(RED + "❌ Erro ao consultar indicadores: " + e.getMessage() + RESET);
            e.printStackTrace();
        }
    }

    private void verPerfil() {
        try {
            List<String> perfil = controller.getPerfilFuncionario();

            System.out.println("\n" + BOLD + CYAN + "╔══════════════════════════════════════════╗" + RESET);
            System.out.println(BOLD + CYAN + "║" + RESET + "         👤  FICHA DE FUNCIONÁRIO         " + BOLD + CYAN + "║" + RESET);
            System.out.println(BOLD + CYAN + "╚══════════════════════════════════════════╝" + RESET);

            for (String info : perfil) {
                String[] parts = info.split(":", 2);
                if(parts.length > 1) {
                    String campo = parts[0].trim();
                    String valor = parts[1].trim();
                    String emoji = getEmojiCampoPerfil(campo);
                    System.out.println("   " + emoji + " " + BOLD + campo + ":" + RESET + " " + valor);
                } else {
                    System.out.println("   " + info);
                }
            }

            System.out.println("\n" + CYAN + "(Enter para voltar)" + RESET);
            scanner.nextLine();
        } catch (Exception e) {
            System.out.println(RED + "Erro: " + e.getMessage() + RESET);
        }
    }

    private String getEmojiArtigo(ArtigoVenda a) {
        if (a instanceof MenuPack) return "📦";

        String nome = a.getNome().toLowerCase();

        if (nome.contains("hambúrguer") || nome.contains("burger") || nome.contains("cheeseburger")) return "🍔";
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

    private String getEmojiIngrediente(String nome) {
        String nomeLower = nome.toLowerCase();

        if (nomeLower.contains("alface")) return "🥬";
        if (nomeLower.contains("tomate")) return "🍅";
        if (nomeLower.contains("cebola")) return "🧅";
        if (nomeLower.contains("pepino")) return "🥒";
        if (nomeLower.contains("cenoura")) return "🥕";
        if (nomeLower.contains("milho")) return "🌽";

        if (nomeLower.contains("queijo")) return "🧀";
        if (nomeLower.contains("bacon")) return "🥓";
        if (nomeLower.contains("ovo")) return "🥚";
        if (nomeLower.contains("carne")) return "🥩";
        if (nomeLower.contains("frango")) return "🍗";
        if (nomeLower.contains("peixe")) return "🐟";

        if (nomeLower.contains("molho") || nomeLower.contains("ketchup") || nomeLower.contains("maionese")) return "🥫";
        if (nomeLower.contains("mostarda")) return "🌭";
        if (nomeLower.contains("azeite") || nomeLower.contains("óleo")) return "🫒";

        if (nomeLower.contains("pão")) return "🍞";
        if (nomeLower.contains("massa")) return "🍝";

        if (nomeLower.contains("arroz")) return "🍚";
        if (nomeLower.contains("batata")) return "🥔";
        if (nomeLower.contains("cogumelo")) return "🍄";
        if (nomeLower.contains("abacate")) return "🥑";

        return "🔸"; // Default para ingredientes genéricos
    }

    private String getEmojiCampoPerfil(String campo) {
        String campoLower = campo.toLowerCase();

        if (campoLower.contains("id")) return "🆔";
        if (campoLower.contains("nome")) return "👤";
        if (campoLower.contains("cargo") || campoLower.contains("tipo") || campoLower.contains("posto")) return "💼";
        if (campoLower.contains("restaurante")) return "🏢";
        if (campoLower.contains("email") || campoLower.contains("e-mail")) return "📧";
        if (campoLower.contains("telefone") || campoLower.contains("telemóvel") || campoLower.contains("contacto")) return "📱";
        if (campoLower.contains("salário") || campoLower.contains("salario")) return "💰";
        if (campoLower.contains("data")) return "📅";
        if (campoLower.contains("horário") || campoLower.contains("turno")) return "⏰";

        return "📋"; // Default
    }


}

