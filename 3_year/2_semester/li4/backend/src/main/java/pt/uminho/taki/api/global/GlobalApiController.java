package pt.uminho.taki.api.global;

import io.javalin.http.Context;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.NotFoundResponse;
import pt.uminho.taki.api.NodeRole;
import pt.uminho.taki.api.NodeRoleConfig;
import pt.uminho.taki.dao.DevolucaoDAO;
import pt.uminho.taki.dao.EncomendaDAO;
import pt.uminho.taki.dao.FornecedorDAO;
import pt.uminho.taki.dao.FuncionarioDAO;
import pt.uminho.taki.dao.InventarioDAO;
import pt.uminho.taki.dao.PromocaoDAO;
import pt.uminho.taki.dao.ProdutoDAO;
import pt.uminho.taki.dao.VendaDAO;
import pt.uminho.taki.api.global.dto.CategoriaRequestDto;
import pt.uminho.taki.api.global.dto.CategoriaResponseDto;
import pt.uminho.taki.api.global.dto.FornecedorRequestDto;
import pt.uminho.taki.api.global.dto.FuncionarioRequestDto;
import pt.uminho.taki.api.global.dto.MensagemResponseDto;
import pt.uminho.taki.api.global.dto.ProdutoRequestDto;
import pt.uminho.taki.api.global.dto.ProdutoResponseDto;
import pt.uminho.taki.api.global.dto.ValorMetricaResponseDto;
import pt.uminho.taki.ln.ITakiLNGlobal;
import pt.uminho.taki.api.shared.erros.TratadorGlobalExcecoesApi;
import pt.uminho.taki.ln.estatisticas.DatasInvalidasException;
import pt.uminho.taki.ln.estatisticas.RelatorioInventarioDTO;
import pt.uminho.taki.ln.estatisticas.RelatorioVendasDTO;
import pt.uminho.taki.ln.lojas.Funcionario;
import pt.uminho.taki.ln.lojas.EstadoConta;
import pt.uminho.taki.ln.lojas.ISubSistemaLojas;
import pt.uminho.taki.ln.lojas.Loja;
import pt.uminho.taki.ln.lojas.PerfilAcesso;
import pt.uminho.taki.ln.lojas.Permissao;
import pt.uminho.taki.ln.lojas.Produto;
import pt.uminho.taki.ln.vendas.Promocao;
import pt.uminho.taki.ln.fatura.FaturaService;
import pt.uminho.taki.ln.report.RelatorioService;
import pt.uminho.taki.ln.inventario.Inventario;
import pt.uminho.taki.ln.vendas.Venda;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class GlobalApiController {

    private final ITakiLNGlobal takiLNGlobal;
    private final ISubSistemaLojas subSistemaLojas;
    private final ProdutoDAO produtoDAO;
    private final VendaDAO vendaDAO;
    private final EncomendaDAO encomendaDAO;
    private final FornecedorDAO fornecedorDAO;
    private final InventarioDAO inventarioDAO;
    private final FuncionarioDAO funcionarioDAO;
    private final PromocaoDAO promocaoDAO;
    private final DevolucaoDAO devolucaoDAO;
    private final pt.uminho.taki.dao.StatisticsDAO statisticsDAO;
    private final FaturaService faturaService;
    private final RelatorioService relatorioService;

    public GlobalApiController(
            ITakiLNGlobal takiLNGlobal,
            ISubSistemaLojas subSistemaLojas,
            ProdutoDAO produtoDAO,
            VendaDAO vendaDAO,
            EncomendaDAO encomendaDAO,
            FornecedorDAO fornecedorDAO,
            InventarioDAO inventarioDAO,
            FuncionarioDAO funcionarioDAO,
            PromocaoDAO promocaoDAO,
            DevolucaoDAO devolucaoDAO,
            pt.uminho.taki.dao.StatisticsDAO statisticsDAO,
            FaturaService faturaService,
            RelatorioService relatorioService
    ) {
        this.takiLNGlobal = Objects.requireNonNull(takiLNGlobal, "takiLNGlobal");
        this.subSistemaLojas = Objects.requireNonNull(subSistemaLojas, "subSistemaLojas");
        this.produtoDAO = Objects.requireNonNull(produtoDAO, "produtoDAO");
        this.vendaDAO = Objects.requireNonNull(vendaDAO, "vendaDAO");
        this.encomendaDAO = Objects.requireNonNull(encomendaDAO, "encomendaDAO");
        this.fornecedorDAO = Objects.requireNonNull(fornecedorDAO, "fornecedorDAO");
        this.inventarioDAO = Objects.requireNonNull(inventarioDAO, "inventarioDAO");
        this.funcionarioDAO = Objects.requireNonNull(funcionarioDAO, "funcionarioDAO");
        this.promocaoDAO = Objects.requireNonNull(promocaoDAO, "promocaoDAO");
        this.devolucaoDAO = Objects.requireNonNull(devolucaoDAO, "devolucaoDAO");
        this.statisticsDAO = Objects.requireNonNull(statisticsDAO, "statisticsDAO");
        this.faturaService = Objects.requireNonNull(faturaService, "faturaService");
        this.relatorioService = Objects.requireNonNull(relatorioService, "relatorioService");
    }

    // --- Lojas ---
    public void listarLojas(Context ctx) {
        List<Loja> lojas = this.takiLNGlobal.listarLojas();
        ctx.json(lojas);
    }

    public void buscarLoja(Context ctx) {
        int idLoja = lerInteiroObrigatorio(ctx, "idLoja");
        Loja loja = this.takiLNGlobal.buscarLoja(idLoja)
                .orElseThrow(() -> new NotFoundResponse("Loja com id " + idLoja + " não encontrada."));
        ctx.json(loja);
    }

    public void registarLoja(Context ctx) {
        Loja loja = ctx.bodyAsClass(Loja.class);
        Loja criada = this.takiLNGlobal.registarLoja(loja);
        ctx.status(201).json(criada);
    }

    public void atualizarLoja(Context ctx) {
        int idLoja = lerInteiroObrigatorio(ctx, "idLoja");
        Loja loja = ctx.bodyAsClass(Loja.class);
        loja.setIdLoja(idLoja);
        Loja atualizada = this.takiLNGlobal.atualizarLoja(loja);
        ctx.json(atualizada);
    }

    public void removerLoja(Context ctx) {
        int idLoja = lerInteiroObrigatorio(ctx, "idLoja");
        this.takiLNGlobal.removerLoja(idLoja);
        ctx.json(MensagemResponseDto.sucesso("loja_removida", "Loja removida com sucesso."));
    }

    // --- Autenticação ---
    private static final String VARIAVEL_CHAVE_PRIVADA_JWT = "TAKI_JWT_PRIVATE_KEY";
    private static final long DURACAO_TOKEN_SEGUNDOS = 8 * 60 * 60;

    public void autenticar(Context ctx) {
        pt.uminho.taki.api.local.LocalApiDtos.PedidoAutenticacao pedido = ctx.bodyValidator(pt.uminho.taki.api.local.LocalApiDtos.PedidoAutenticacao.class).get();
        String email = campoObrigatorio(pedido.getEmail(), "email");
        String password = campoObrigatorio(pedido.getPassword(), "password");

        Funcionario funcionario = this.subSistemaLojas.autenticar(email, password);
        if (funcionario == null) {
            throw new io.javalin.http.UnauthorizedResponse("Utilizador ou palavra-passe incorretos.");
        }
        if (NodeRoleConfig.resolverRoleRuntime() == NodeRole.CENTRAL && !temPerfilGestaoCentral(funcionario)) {
            throw new ForbiddenResponse("Acesso negado: permissões insuficientes para o modo central");
        }
        String token = gerarTokenJwt(funcionario);

        ctx.cookie(new io.javalin.http.Cookie(
            "auth_token",
            token,
            "/",
            (int) DURACAO_TOKEN_SEGUNDOS,
            deveUsarCookieSeguro(System.getenv("APP_MODE")),
            1,
            true
        ));

        java.util.Map<String, Object> resposta = new java.util.LinkedHashMap<>();
        resposta.put("token", token);
        resposta.put("funcionario", mapearFuncionario(funcionario));
        ctx.status(200).json(resposta);
    }

    public void logout(Context ctx) {
        ctx.removeCookie("auth_token", "/");
        ctx.status(200).json(MensagemResponseDto.sucesso("logout_sucesso", "Logout efetuado com sucesso."));
    }

    public void verificarPassword(Context ctx) {
        pt.uminho.taki.api.local.LocalApiDtos.PedidoVerificacaoPassword pedido = ctx.bodyValidator(pt.uminho.taki.api.local.LocalApiDtos.PedidoVerificacaoPassword.class).get();
        String password = pedido.getPassword();

        pt.uminho.taki.api.shared.seguranca.ContextoAutenticacao contexto = pt.uminho.taki.api.shared.seguranca.MiddlewareSegurancaApi.obterContexto(ctx).orElse(null);
        boolean valida = false;

        if (contexto != null && password != null && !password.isBlank()) {
            try {
                valida = this.subSistemaLojas.autenticar(contexto.getEmail(), password) != null;
            } catch (Exception e) {
                valida = false;
            }
        }

        java.util.Map<String, Object> resposta = new java.util.LinkedHashMap<>();
        resposta.put("valid", valida);
        ctx.status(200).json(resposta);
    }

    public void obterSessaoAtual(Context ctx) {
        pt.uminho.taki.api.shared.seguranca.ContextoAutenticacao contexto = pt.uminho.taki.api.shared.seguranca.MiddlewareSegurancaApi.obterContexto(ctx)
                .orElseThrow(() -> new io.javalin.http.UnauthorizedResponse("Autenticação obrigatória."));

        java.util.Optional<Funcionario> funcionario = this.subSistemaLojas.buscarFuncionarioPorId(contexto.getSubject());

        java.util.Map<String, Object> dados = new java.util.LinkedHashMap<>();
        dados.put("subject", contexto.getSubject());
        dados.put("roles", contexto.getRoles());
        dados.put("scopes", contexto.getScopes());
        funcionario.ifPresent(value -> dados.put("funcionario", mapearFuncionario(value)));

        ctx.status(200).json(dados);
    }

    private String gerarTokenJwt(Funcionario funcionario) {
        String privateKey = System.getenv(VARIAVEL_CHAVE_PRIVADA_JWT);
        if (privateKey == null || privateKey.isBlank()) {
            throw new RuntimeException("Chave Privada JWT não configurada em " + VARIAVEL_CHAVE_PRIVADA_JWT + ".");
        }

        java.util.Set<String> roles = resolverRoles(funcionario.getIdPerfilAcesso());
        java.util.Set<String> scopes = new java.util.LinkedHashSet<>();
        long exp = Instant.now().plusSeconds(DURACAO_TOKEN_SEGUNDOS).getEpochSecond();

        String payload = construirPayloadJwt(funcionario, roles, scopes, exp);
        return pt.uminho.taki.api.shared.seguranca.JwtAuthUtils.gerarTokenRSA(payload, privateKey);
    }

    static boolean deveUsarCookieSeguro(String appMode) {
        return "PROD".equalsIgnoreCase(appMode);
    }

    private String construirPayloadJwt(Funcionario funcionario, java.util.Set<String> roles, java.util.Set<String> scopes, long exp) {
        StringBuilder payload = new StringBuilder("{");
        payload.append("\"sub\":\"").append(escaparJson(funcionario.getId())).append("\",");
        payload.append("\"nome\":\"").append(escaparJson(funcionario.getNome())).append("\",");
        payload.append("\"email\":\"").append(escaparJson(funcionario.getEmail())).append("\",");
        payload.append("\"idLoja\":").append(funcionario.getIdLoja()).append(",");
        payload.append("\"roles\":").append(serializarArrayJson(roles)).append(",");
        payload.append("\"scopes\":").append(serializarArrayJson(scopes)).append(",");
        payload.append("\"exp\":").append(exp);
        payload.append("}");
        return payload.toString();
    }

    private java.util.Set<String> resolverRoles(String perfil) {
        java.util.Set<String> roles = new java.util.LinkedHashSet<>();
        String roleBase = (perfil == null || perfil.isBlank() ? "OPERADOR" : perfil.trim()).toUpperCase(java.util.Locale.ROOT);
        roles.add(roleBase);
        String semAcentos = java.text.Normalizer.normalize(roleBase, java.text.Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        roles.add(semAcentos);
        if (semAcentos.contains("PROPRIETARIO")) {
            roles.add("ADMIN");
            roles.add("PROPRIETARIO DA CADEIA");
        }
        return roles;
    }

    private static String serializarArrayJson(java.util.Set<String> valores) {
        return valores.stream()
                .map(GlobalApiController::escaparJson)
                .map(valor -> "\"" + valor + "\"")
                .collect(Collectors.joining(",", "[", "]"));
    }

    private static String escaparJson(String valor) {
        if (valor == null) return "";
        return valor.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private java.util.Map<String, Object> mapearFuncionario(Funcionario funcionario) {
        java.util.Map<String, Object> resultado = new java.util.LinkedHashMap<>();
        resultado.put("id", funcionario.getId());
        resultado.put("nome", funcionario.getNome());
        resultado.put("email", funcionario.getEmail());
        String idPerfil = funcionario.getIdPerfilAcesso();
        resultado.put("idPerfilAcesso", idPerfil);
        String nomePerfil = this.subSistemaLojas.listarPerfis().stream()
                .filter(p -> idPerfil != null && p.getId().equalsIgnoreCase(idPerfil))
                .map(PerfilAcesso::getNome)
                .findFirst()
                .orElse(idPerfil);
        resultado.put("role", nomePerfil);
        resultado.put("idLoja", funcionario.getIdLoja());
        resultado.put("estadoConta", funcionario.getEstadoConta() != null ? funcionario.getEstadoConta().name() : null);
        return resultado;
    }

    private static String campoObrigatorio(String valor, String nomeCampo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Campo obrigatório em falta: " + nomeCampo + ".");
        }
        return valor.trim();
    }

    /**
     * Indica se o funcionário possui um perfil de gestão central (ADMIN ou GESTOR_CENTRAL).
     */
    private static boolean temPerfilGestaoCentral(Funcionario funcionario) {
        if (funcionario == null) return false;
        String perfil = funcionario.getIdPerfilAcesso();
        if (perfil == null) return false;
        String chave = perfil.trim().toUpperCase(java.util.Locale.ROOT);
        return chave.equals("ADMIN") || chave.equals("GESTOR_CENTRAL");
    }

    // --- Categorias ---
    public void listarCategorias(Context ctx) {
        List<CategoriaResponseDto> categorias = this.takiLNGlobal.listarCategorias().stream()
                .map(CategoriaResponseDto::from)
                .collect(Collectors.toList());
        ctx.json(categorias);
    }

    public void adicionarCategoria(Context ctx) {
        CategoriaRequestDto pedido = ctx.bodyValidator(CategoriaRequestDto.class).get();
        this.takiLNGlobal.adicionarCategoria(pedido.paraDominio());
        ctx.status(201).json(MensagemResponseDto.sucesso("categoria_adicionada", "Categoria adicionada com sucesso."));
    }

    // --- Produtos ---
    public void adicionarProduto(Context ctx) {
        ProdutoRequestDto pedido = ctx.bodyValidator(ProdutoRequestDto.class).get();
        this.takiLNGlobal.adicionarProduto(pedido.paraDominio());
        ctx.status(201).json(MensagemResponseDto.sucesso("produto_adicionado", "Produto adicionado com sucesso."));
    }

    public void pesquisarProdutoPorCodigoBarras(Context ctx) {
        String codigoBarras = ctx.pathParam("codigoBarras");
        Produto produto = this.takiLNGlobal.pesquisarPorCodigoBarras(codigoBarras);
        if (produto == null) {
            throw new NotFoundResponse("Produto não encontrado para o código de barras indicado.");
        }
        ctx.json(ProdutoResponseDto.from(produto));
    }

    public void inativarProduto(Context ctx) {
        String idProduto = ctx.pathParam("idProduto");
        this.takiLNGlobal.inativarProduto(idProduto);
        ctx.json(MensagemResponseDto.sucesso("produto_inativado", "Produto inativado com sucesso."));
    }

    /**
     * Rejeita operações de escrita em entidades que devem ser geridas localmente.
     * 
     * @param ctx o contexto do pedido
     */
    public void rejeitarAlteracaoGlobal(Context ctx) {
        TratadorGlobalExcecoesApi.responder(ctx, 501, TratadorGlobalExcecoesApi.codePorStatus(501), "Esta operação só pode ser realizada localmente na respetiva loja.", null);
    }

    // --- Funcionarios ---
    public void registarFuncionario(Context ctx) {
        FuncionarioRequestDto pedido = ctx.bodyValidator(FuncionarioRequestDto.class).get();
        this.takiLNGlobal.registarFuncionario(pedido.paraDominio());
        ctx.status(201).json(MensagemResponseDto.sucesso("funcionario_registado", "Funcionário registado com sucesso."));
    }

    public void bloquearContaFuncionario(Context ctx) {
        String idFuncionario = ctx.pathParam("idFuncionario");
        this.takiLNGlobal.bloquearConta(idFuncionario);
        ctx.json(MensagemResponseDto.sucesso("conta_bloqueada", "Conta bloqueada com sucesso."));
    }

    // --- Fornecedores ---
    public void adicionarFornecedor(Context ctx) {
        FornecedorRequestDto pedido = ctx.bodyValidator(FornecedorRequestDto.class).get();
        this.takiLNGlobal.adicionarFornecedor(pedido.paraDominio());
        ctx.status(201).json(MensagemResponseDto.sucesso("fornecedor_adicionado", "Fornecedor adicionado com sucesso."));
    }

    public void inativarFornecedor(Context ctx) {
        String idFornecedor = ctx.pathParam("idFornecedor");
        this.takiLNGlobal.inativarFornecedor(idFornecedor);
        ctx.json(MensagemResponseDto.sucesso("fornecedor_inativado", "Fornecedor inativado com sucesso."));
    }

    // --- Estatisticas ---
    public void calcularVolumeVendas(Context ctx) {
        LocalDateTime inicio = lerDataHoraObrigatoria(ctx, "inicio");
        LocalDateTime fim = lerDataHoraObrigatoria(ctx, "fim");
        double volume = executarOperacaoDatas(() -> this.takiLNGlobal.calcularVolumeVendas(inicio, fim));
        ctx.json(new ValorMetricaResponseDto("volumeVendas", volume));
    }

    public void calcularTicketMedio(Context ctx) {
        LocalDateTime inicio = lerDataHoraObrigatoria(ctx, "inicio");
        LocalDateTime fim = lerDataHoraObrigatoria(ctx, "fim");
        double ticketMedio = executarOperacaoDatas(() -> this.takiLNGlobal.calcularTicketMedio(inicio, fim));
        ctx.json(new ValorMetricaResponseDto("ticketMedio", ticketMedio));
    }

    public void gerarRelatorioVendas(Context ctx) {
        LocalDateTime inicio = lerDataHoraObrigatoria(ctx, "inicio");
        LocalDateTime fim = lerDataHoraObrigatoria(ctx, "fim");
        Integer idLoja = lerInteiroOpcional(ctx, "idLoja");
        String categoria = lerTextoOpcional(ctx, "categoria");
        RelatorioVendasDTO relatorio = executarOperacaoDatas(() ->
                this.takiLNGlobal.gerarRelatorioVendas(inicio, fim, idLoja, categoria)
        );
        ctx.json(relatorio);
    }

    public void gerarRelatorioInventario(Context ctx) {
        Integer idLoja = lerInteiroOpcional(ctx, "idLoja");
        RelatorioInventarioDTO relatorio = this.takiLNGlobal.gerarRelatorioInventario(idLoja);
        ctx.json(relatorio);
    }

    public void gerarDashboardKpis(Context ctx) {
        Integer idLoja = lerInteiroOpcional(ctx, "idLoja");
        ctx.json(this.takiLNGlobal.gerarDashboardKPIs(idLoja));
    }

    // --- View ---
    public void atualizarView(Context ctx) {
        this.takiLNGlobal.atualizarView();
        ctx.status(202).json(MensagemResponseDto.sucesso("view_atualizada", "Atualização da view global iniciada."));
    }

    // --- Compatibilidade com frontend global ---
    public void listarProdutos(Context ctx) {
        Integer idLoja = lerInteiroOpcional(ctx, "idLoja");
        int lojaFiltro = (idLoja == null || idLoja <= 0) ? 0 : idLoja;
        ctx.json(this.produtoDAO.listarProdutosComInventario(lojaFiltro));
    }

    public void listarVendas(Context ctx) {
        Integer idLoja = lerInteiroOpcional(ctx, "idLoja");
        int lojaFiltro = (idLoja == null || idLoja <= 0) ? 0 : idLoja;

        Map<String, String> nomesFuncionarios = this.funcionarioDAO.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(
                        pt.uminho.taki.ln.lojas.Funcionario::getId,
                        pt.uminho.taki.ln.lojas.Funcionario::getNome,
                        (a, b) -> a));

        List<Map<String, Object>> vendas = this.vendaDAO.findAll().stream()
                .filter(venda -> lojaFiltro == 0 || venda.getIdLoja() == lojaFiltro)
                .map(venda -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("idVenda", venda.getIdVenda());
                    map.put("numFatura", venda.getFatura() != null ? venda.getFatura().getNumFatura() : "");
                    map.put("dataHora", venda.getDataHora() != null ? venda.getDataHora().toString() : null);
                    map.put("subtotal", venda.getSubtotal());
                    map.put("imposto", venda.getImposto());
                    map.put("total", venda.getTotal());
                    map.put("estado", venda.getEstado());
                    map.put("idLoja", venda.getIdLoja());
                    map.put("idFuncionario", venda.getIdFuncionario());
                    map.put("nomeFuncionario", nomesFuncionarios.getOrDefault(venda.getIdFuncionario(), venda.getIdFuncionario()));
                    map.put("metodoPagamento", this.vendaDAO.getMetodoPagamento(venda.getIdVenda()).orElse(null));
                    map.put("linhas", venda.getLinhas() == null ? List.of()
                            : venda.getLinhas().stream().map(linha -> {
                                Map<String, Object> ml = new LinkedHashMap<>();
                                ml.put("idLinhaVenda", linha.getIdLinhaVenda());
                                ml.put("quantidade", linha.getQuantidade());
                                ml.put("subtotal", linha.getSubtotal());
                                ml.put("totalImposto", linha.getTotalImposto());
                                ml.put("totalFinal", linha.getTotalFinal());
                                if (linha.getProduto() != null) {
                                    Map<String, Object> mp = new LinkedHashMap<>();
                                    mp.put("idProduto", linha.getProduto().getIdProduto());
                                    mp.put("nome", linha.getProduto().getNome());
                                    mp.put("precoVenda", linha.getProduto().getPrecoVenda());
                                    mp.put("taxaIva", linha.getProduto().getTaxaIva() != null ? linha.getProduto().getTaxaIva().toString() : null);
                                    ml.put("produto", mp);
                                    ml.put("idProduto", linha.getProduto().getIdProduto());
                                    ml.put("nome", linha.getProduto().getNome());
                                    ml.put("preco", linha.getProduto().getPrecoVenda());
                                }
                                return ml;
                            }).collect(Collectors.toList()));
                    return map;
                })
                .collect(Collectors.toList());
        ctx.json(vendas);
    }

    public void listarDevolucoes(Context ctx) {
        Integer idLoja = lerInteiroOpcional(ctx, "idLoja");
        int lojaFiltro = (idLoja == null || idLoja <= 0) ? 0 : idLoja;

        Map<String, Integer> idLojaPorVenda = lojaFiltro == 0
                ? Map.of()
                : this.vendaDAO.findAll().stream()
                        .collect(Collectors.toMap(pt.uminho.taki.ln.vendas.Venda::getIdVenda,
                                pt.uminho.taki.ln.vendas.Venda::getIdLoja, (a, b) -> a));

        List<Map<String, Object>> devolucoes = this.devolucaoDAO.findAll().stream()
                .filter(d -> lojaFiltro == 0 || (idLojaPorVenda.getOrDefault(d.getIdVenda(), -1) == lojaFiltro))
                .map(d -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("idDevolucao", d.getIdDevolucao());
                    map.put("idVenda", d.getIdVenda());
                    map.put("dataHora", d.getDataHora() != null ? d.getDataHora().toString() : null);
                    map.put("valor", d.getValor());
                    map.put("metodoReembolso", d.getMetodoReembolso());
                    map.put("numNotaCredito", d.getNumNotaCredito());
                    map.put("idFuncionario", d.getIdFuncionario());
                    return map;
                })
                .collect(Collectors.toList());
        ctx.json(devolucoes);
    }

    public void listarEncomendas(Context ctx) {
        Integer idLoja = lerInteiroOpcional(ctx, "idLoja");
        int lojaFiltro = (idLoja == null || idLoja <= 0) ? 0 : idLoja;

        Map<String, String> fornecedoresMap = this.fornecedorDAO.findAll().stream()
                .collect(Collectors.toMap(pt.uminho.taki.ln.fornecimentos.Fornecedor::getIdFornecedor,
                        pt.uminho.taki.ln.fornecimentos.Fornecedor::getNome, (a, b) -> a));

        List<Map<String, Object>> encomendas = this.encomendaDAO.findAll().stream()
                .filter(enc -> lojaFiltro == 0 || String.valueOf(lojaFiltro).equals(enc.getIdLoja()))
                .map(enc -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("idEncomenda", enc.getIdEncomenda());
                    map.put("idFornecedor", enc.getIdFornecedor());
                    map.put("nomeFornecedor", fornecedoresMap.getOrDefault(enc.getIdFornecedor(), enc.getIdFornecedor()));
                    map.put("idLoja", enc.getIdLoja());
                    map.put("dataCriacao", enc.getDataCriacao() != null ? enc.getDataCriacao().toString() : null);
                    map.put("dataEntrega", enc.getDataEntrega() != null ? enc.getDataEntrega().toString() : null);
                    map.put("estado", enc.getEstadoAtual() != null ? enc.getEstadoAtual().getDesignacao() : null);
                    map.put("total", enc.getValorTotal());
                    map.put("linhas", enc.getLinhas().stream().map(linha -> {
                        Map<String, Object> ml = new LinkedHashMap<>();
                        ml.put("idLinhaEncomenda", linha.getIdLinhaEncomenda());
                        ml.put("idEncomenda", linha.getIdEncomenda());
                        ml.put("idProduto", linha.getIdProduto());
                        ml.put("quantidade", linha.getQuantidade());
                        ml.put("precoCusto", linha.getPrecoCustoAplicado());
                        ml.put("subTotal", linha.getSubTotal());
                        return ml;
                    }).collect(Collectors.toList()));
                    return map;
                })
                .collect(Collectors.toList());
        ctx.json(encomendas);
    }

    public void listarFornecedores(Context ctx) {
        ctx.json(this.fornecedorDAO.findAll());
    }

    public void listarFuncionarios(Context ctx) {
        Integer idLoja = lerInteiroOpcional(ctx, "idLoja");
        int lojaFiltro = (idLoja == null || idLoja <= 0) ? 0 : idLoja;
        List<Map<String, Object>> funcionarios = this.funcionarioDAO.findAll().stream()
                .filter(f -> f.getEstadoConta() != EstadoConta.INATIVO)
                .filter(f -> lojaFiltro == 0 || f.getIdLoja() == lojaFiltro)
                .map(this::mapearFuncionario)
                .collect(Collectors.toList());
        ctx.json(funcionarios);
    }

    public void listarFuncionariosNaoSuportado(Context ctx) {
        listarFuncionarios(ctx);
    }

    public void registarFuncionarioCompat(Context ctx) {
        Funcionario funcionario = ctx.bodyAsClass(Funcionario.class);
        this.takiLNGlobal.registarFuncionario(funcionario);
        ctx.status(201).json(mapearFuncionario(funcionario));
    }

    public void atualizarFuncionarioCompat(Context ctx) {
        String idFuncionario = campoObrigatorio(ctx.pathParam("idFuncionario"), "idFuncionario");
        Funcionario funcionario = ctx.bodyAsClass(Funcionario.class);
        funcionario.setId(idFuncionario);
        verificarPodeAtribuirPerfil(ctx, idFuncionario, funcionario.getIdPerfilAcesso());
        this.subSistemaLojas.atualizarFuncionario(funcionario);
        ctx.status(200).json(mapearFuncionario(funcionario));
    }

    public void bloquearFuncionarioCompat(Context ctx) {
        String idFuncionario = campoObrigatorio(ctx.pathParam("idFuncionario"), "idFuncionario");
        verificarPodeModificarFuncionario(ctx, idFuncionario);
        this.subSistemaLojas.bloquearConta(idFuncionario);
        ctx.status(200).json(MensagemResponseDto.sucesso("conta_bloqueada", "Conta bloqueada com sucesso."));
    }

    public void desbloquearFuncionarioCompat(Context ctx) {
        String idFuncionario = campoObrigatorio(ctx.pathParam("idFuncionario"), "idFuncionario");
        verificarPodeModificarFuncionario(ctx, idFuncionario);
        Funcionario funcionario = this.subSistemaLojas.buscarFuncionarioPorId(idFuncionario)
                .map(Funcionario::clone)
                .orElseThrow(() -> new NotFoundResponse("Funcionário não encontrado: " + idFuncionario));
        funcionario.setEstadoConta(EstadoConta.ATIVO);
        this.subSistemaLojas.atualizarFuncionario(funcionario);
        ctx.status(200).json(MensagemResponseDto.sucesso("conta_desbloqueada", "Conta desbloqueada com sucesso."));
    }

    public void removerFuncionarioCompat(Context ctx) {
        String idFuncionario = campoObrigatorio(ctx.pathParam("idFuncionario"), "idFuncionario");
        verificarPodeModificarFuncionario(ctx, idFuncionario);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = Optional.ofNullable(ctx.bodyAsClass(Map.class)).orElseGet(Map::of);
        String passwordAdministrador = body.get("passwordAdministrador") == null ? null : String.valueOf(body.get("passwordAdministrador"));
        String idAdministrador = obterAdministradorSessao(ctx);
        this.subSistemaLojas.removerContaLogicamente(idFuncionario, idAdministrador, passwordAdministrador);
        ctx.status(200).json(MensagemResponseDto.sucesso("conta_inativada", "Conta inativada com sucesso."));
    }

    public void atribuirPerfilFuncionarioCompat(Context ctx) {
        String idFuncionario = campoObrigatorio(ctx.pathParam("idFuncionario"), "idFuncionario");
        @SuppressWarnings("unchecked")
        Map<String, Object> body = Optional.ofNullable(ctx.bodyAsClass(Map.class)).orElseGet(Map::of);
        String nomePerfil = campoObrigatorio(body.get("nomePerfil") == null ? null : String.valueOf(body.get("nomePerfil")), "nomePerfil");
        verificarPodeAtribuirPerfil(ctx, idFuncionario, nomePerfil);
        String idAdministrador = obterAdministradorSessao(ctx);
        this.subSistemaLojas.atribuirPerfil(idFuncionario, nomePerfil, idAdministrador);
        ctx.status(200).json(MensagemResponseDto.sucesso("perfil_atribuido", "Perfil atribuído com sucesso."));
    }

    public void obterKpiGerais(Context ctx) {
        Integer idLoja = lerInteiroOpcional(ctx, "idLoja");
        int lojaFiltro = (idLoja == null || idLoja <= 0) ? 0 : idLoja;

        Map<String, Object> kpis = this.statisticsDAO.getKpiComparativo(lojaFiltro);
        kpis.put("valorStock", this.statisticsDAO.getValorizacaoEstoque(lojaFiltro));
        
        ctx.json(kpis);
    }

    public void obterVendasMensais(Context ctx) {
        Integer idLoja = lerInteiroOpcional(ctx, "idLoja");
        int lojaFiltro = (idLoja == null || idLoja <= 0) ? 0 : idLoja;
        int meses = lerInteiroOpcional(ctx, "meses") == null ? 3 : Math.max(1, lerInteiroOpcional(ctx, "meses"));

        Map<YearMonth, List<pt.uminho.taki.ln.vendas.Venda>> agrupadas = this.vendaDAO.findAll().stream()
                .filter(v -> lojaFiltro == 0 || v.getIdLoja() == lojaFiltro)
                .filter(v -> v.getDataHora() != null)
                .collect(Collectors.groupingBy(v -> YearMonth.from(v.getDataHora())));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");
        List<YearMonth> mesesOrdenados = new ArrayList<>(agrupadas.keySet());
        mesesOrdenados.sort(Comparator.naturalOrder());
        if (mesesOrdenados.size() > meses) {
            mesesOrdenados = mesesOrdenados.subList(mesesOrdenados.size() - meses, mesesOrdenados.size());
        }

        List<Map<String, Object>> resposta = mesesOrdenados.stream().map(mes -> {
            List<pt.uminho.taki.ln.vendas.Venda> vendasMes = agrupadas.getOrDefault(mes, List.of());
            Map<String, Object> linha = new LinkedHashMap<>();
            linha.put("mes", mes.format(formatter));
            linha.put("totalVendas", vendasMes.stream().mapToDouble(pt.uminho.taki.ln.vendas.Venda::getTotal).sum());
            linha.put("totalTransacoes", vendasMes.size());
            return linha;
        }).collect(Collectors.toList());

        ctx.json(resposta);
    }

    public void obterVendasPorHora(Context ctx) {
        Integer idLoja = lerInteiroOpcional(ctx, "idLoja");
        int lojaFiltro = (idLoja == null || idLoja <= 0) ? 0 : idLoja;

        Map<Integer, Long> contagemPorHora = this.vendaDAO.findAll().stream()
                .filter(v -> lojaFiltro == 0 || v.getIdLoja() == lojaFiltro)
                .filter(v -> v.getDataHora() != null)
                .collect(Collectors.groupingBy(v -> v.getDataHora().getHour(), Collectors.counting()));

        List<Map<String, Object>> resposta = contagemPorHora.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    Map<String, Object> linha = new LinkedHashMap<>();
                    linha.put("hora", String.format("%02d:00", entry.getKey()));
                    linha.put("totalClientes", entry.getValue());
                    return linha;
                })
                .collect(Collectors.toList());
        ctx.json(resposta);
    }

    public void obterVendasPorCategoria(Context ctx) {
        Integer idLoja = lerInteiroOpcional(ctx, "idLoja");
        int lojaFiltro = (idLoja == null || idLoja <= 0) ? 0 : idLoja;
        Map<String, String> categoriaDesignacaoPorId = this.takiLNGlobal.listarCategorias().stream()
                .collect(Collectors.toMap(c -> c.getIdCategoria(), c -> c.getDesignacao(), (a, b) -> a));

        Map<String, String> categoriaPorProduto = new LinkedHashMap<>();
        this.produtoDAO.findAll().forEach(produto -> {
            String idCategoria = this.produtoDAO.getCategorias(produto.getIdProduto()).stream().findFirst().orElse(null);
            String categoria = idCategoria == null
                    ? "Sem categoria"
                    : categoriaDesignacaoPorId.getOrDefault(idCategoria, idCategoria);
            categoriaPorProduto.put(produto.getIdProduto(), categoria);
        });

        Map<String, Double> totalPorCategoria = new LinkedHashMap<>();
        this.vendaDAO.findAll().stream()
                .filter(v -> lojaFiltro == 0 || v.getIdLoja() == lojaFiltro)
                .forEach(v -> {
                    if (v.getLinhas() == null) {
                        return;
                    }
                    v.getLinhas().forEach(linha -> {
                        String idProduto = linha.getProduto() != null ? linha.getProduto().getIdProduto() : null;
                        String categoria = categoriaPorProduto.getOrDefault(idProduto, "Sem categoria");
                        totalPorCategoria.merge(categoria, linha.getSubtotal(), Double::sum);
                    });
                });

        List<Map<String, Object>> resposta = totalPorCategoria.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> linha = new LinkedHashMap<>();
                    linha.put("categoria", entry.getKey());
                    linha.put("totalFaturado", entry.getValue());
                    return linha;
                })
                .sorted((a, b) -> Double.compare((Double) b.get("totalFaturado"), (Double) a.get("totalFaturado")))
                .collect(Collectors.toList());

        ctx.json(resposta);
    }

    public void listarPerfis(Context ctx) {
        List<Map<String, Object>> perfis = this.subSistemaLojas.listarPerfis().stream()
                .map(this::mapearPerfil)
                .collect(Collectors.toList());
        ctx.json(perfis);
    }

    public void registarPerfil(Context ctx) {
        @SuppressWarnings("unchecked")
        Map<String, Object> body = Optional.ofNullable(ctx.bodyAsClass(Map.class)).orElseGet(Map::of);
        String id = campoObrigatorio(body.get("id") == null ? null : String.valueOf(body.get("id")), "id");
        String nome = campoObrigatorio(body.get("nome") == null ? null : String.valueOf(body.get("nome")), "nome");
        List<Permissao> permissoes = resolverPermissoes(extrairListaTexto(body.get("permissoes")));
        PerfilAcesso perfil = new PerfilAcesso(id, nome, permissoes);
        this.subSistemaLojas.registarPerfil(perfil);
        ctx.status(201).json(mapearPerfil(perfil));
    }

    public void editarPerfil(Context ctx) {
        String nomePerfil = campoObrigatorio(ctx.pathParam("nomePerfil"), "nomePerfil");
        @SuppressWarnings("unchecked")
        Map<String, Object> body = Optional.ofNullable(ctx.bodyAsClass(Map.class)).orElseGet(Map::of);
        List<Permissao> permissoes = resolverPermissoes(extrairListaTexto(body.get("permissoes")));
        this.subSistemaLojas.editarPerfil(nomePerfil, permissoes);
        ctx.status(200).json(MensagemResponseDto.sucesso("perfil_atualizado", "Perfil atualizado com sucesso."));
    }

    public void listarPromocoesAtivasCompat(Context ctx) {
        Integer idLoja = lerInteiroOpcional(ctx, "idLoja");
        int lojaFiltro = (idLoja == null || idLoja <= 0) ? 0 : idLoja;
        LocalDateTime agora = LocalDateTime.now();
        List<Map<String, Object>> promocoes = this.promocaoDAO.findAll().stream()
                .filter(p -> lojaFiltro == 0 || (p.getIdLoja() != null && p.getIdLoja() == lojaFiltro))
                .filter(p -> p.getDataInicio() != null && p.getDataFim() != null)
                .filter(p -> !agora.isBefore(p.getDataInicio()) && !agora.isAfter(p.getDataFim()))
                .filter(p -> p.getEstado() == null || "Ativa".equalsIgnoreCase(p.getEstado()))
                .map(this::mapearPromocao)
                .collect(Collectors.toList());
        ctx.json(promocoes);
    }

    public void adicionarPromocaoCompat(Context ctx) {
        Promocao promocao = ctx.bodyAsClass(Promocao.class);
        if (promocao.getIdPromocao() == null || promocao.getIdPromocao().isBlank()) {
            promocao.setIdPromocao(UUID.randomUUID().toString());
        }
        if (promocao.getEstado() == null || promocao.getEstado().isBlank()) {
            promocao.setEstado("Ativa");
        }

        this.promocaoDAO.save(promocao.getIdPromocao(), promocao);
        if (promocao.getProdutos() != null) {
            promocao.getProdutos().forEach(idProduto -> this.promocaoDAO.addProduto(promocao.getIdPromocao(), idProduto));
        }
        if (promocao.getCategorias() != null) {
            promocao.getCategorias().forEach(idCategoria -> this.promocaoDAO.addCategoria(promocao.getIdPromocao(), idCategoria));
        }
        ctx.status(201).json(mapearPromocao(promocao));
    }

    public void cancelarPromocaoCompat(Context ctx) {
        String idPromocao = campoObrigatorio(ctx.pathParam("idPromocao"), "idPromocao");
        Promocao promocao = this.promocaoDAO.findById(idPromocao)
                .orElseThrow(() -> new NotFoundResponse("Promoção não encontrada: " + idPromocao));
        promocao.setEstado("Cancelada");
        promocao.setDataFim(LocalDateTime.now());
        this.promocaoDAO.save(idPromocao, promocao);
        ctx.status(200).json(MensagemResponseDto.sucesso("promocao_cancelada", "Promoção cancelada com sucesso."));
    }

    public void sincronizacaoExportacaoNaoSuportada(Context ctx) {
        String payload = ctx.body();
        this.takiLNGlobal.processarSincronizacaoExportacao(payload);
        ctx.status(200).json(MensagemResponseDto.sucesso("sincronizacao_recebida", "Payload recebido pelo nó central."));
    }

    public void sincronizacaoImportacaoNaoSuportada(Context ctx) {
        String resultado = this.takiLNGlobal.processarSincronizacaoImportacao();
        ctx.status(200).result(resultado).contentType("application/json");
    }


    // --- Utilitários privados ---
    private int lerInteiroObrigatorio(Context ctx, String nomeParam) {
        String valor = ctx.pathParam(nomeParam);
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Path param '" + nomeParam + "' deve ser um inteiro válido.", e);
        }
    }

    private LocalDateTime lerDataHoraObrigatoria(Context ctx, String nomeParametro) {
        String valor = ctx.queryParam(nomeParametro);
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Parâmetro obrigatório em falta: " + nomeParametro);
        }
        try {
            return LocalDateTime.parse(valor);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Parâmetro '" + nomeParametro + "' inválido. Use o formato ISO-8601 (ex.: 2025-01-10T14:30:00).",
                    e
            );
        }
    }

    private Integer lerInteiroOpcional(Context ctx, String nomeParametro) {
        String valor = ctx.queryParam(nomeParametro);
        if (valor == null || valor.isBlank()) return null;
        if ("all".equalsIgnoreCase(valor.trim())) return null;
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Parâmetro '" + nomeParametro + "' deve ser um inteiro válido.", e);
        }
    }

    private String lerTextoOpcional(Context ctx, String nomeParametro) {
        String valor = ctx.queryParam(nomeParametro);
        if (valor == null || valor.isBlank()) return null;
        return valor.trim();
    }

    private String obterAdministradorSessao(Context ctx) {
        return pt.uminho.taki.api.shared.seguranca.MiddlewareSegurancaApi.obterContexto(ctx)
                .map(pt.uminho.taki.api.shared.seguranca.ContextoAutenticacao::getSubject)
                .orElseThrow(() -> new io.javalin.http.UnauthorizedResponse("Autenticação obrigatória para operação administrativa."));
    }

    private int nivelHierarquia(String perfil) {
        if (perfil == null) return 0;
        String p = java.text.Normalizer.normalize(perfil.trim().toUpperCase(java.util.Locale.ROOT), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        if (p.contains("PROPRIET") || p.contains("GESTOR_CENTRAL")) return 4;
        if (p.contains("ADMIN")) return 3;
        if (p.contains("GERENTE") || p.contains("GESTOR_LOJA")) return 2;
        if (p.contains("OPERADOR")) return 1;
        return 0;
    }

    private void verificarPodeModificarFuncionario(Context ctx, String idAlvo) {
        String idAdmin = obterAdministradorSessao(ctx);
        if (idAlvo != null && idAlvo.equals(idAdmin)) {
            throw new ForbiddenResponse("Não pode modificar a sua própria conta.");
        }
        Funcionario admin = this.funcionarioDAO.findById(idAdmin)
                .orElseThrow(() -> new io.javalin.http.UnauthorizedResponse("Utilizador autenticado não encontrado."));
        Funcionario alvo = this.funcionarioDAO.findById(idAlvo)
                .orElseThrow(() -> new NotFoundResponse("Funcionário não encontrado: " + idAlvo));
        int nivelAdmin = nivelHierarquia(admin.getIdPerfilAcesso());
        int nivelAlvo = nivelHierarquia(alvo.getIdPerfilAcesso());
        if (nivelAlvo >= nivelAdmin) {
            throw new ForbiddenResponse("Não tem permissão para modificar contas do mesmo nível ou superior.");
        }
    }

    private void verificarPodeAtribuirPerfil(Context ctx, String idAlvo, String novoPerfil) {
        verificarPodeModificarFuncionario(ctx, idAlvo);
        if (novoPerfil == null || novoPerfil.isBlank()) return;
        String idAdmin = obterAdministradorSessao(ctx);
        Funcionario admin = this.funcionarioDAO.findById(idAdmin)
                .orElseThrow(() -> new io.javalin.http.UnauthorizedResponse("Utilizador autenticado não encontrado."));
        int nivelAdmin = nivelHierarquia(admin.getIdPerfilAcesso());
        int nivelNovo = nivelHierarquia(novoPerfil);
        if (nivelNovo >= nivelAdmin) {
            throw new ForbiddenResponse("Não pode atribuir um perfil de nível igual ou superior ao seu.");
        }
    }

    private List<Permissao> resolverPermissoes(List<String> permissoes) {
        if (permissoes == null || permissoes.isEmpty()) {
            throw new IllegalArgumentException("É obrigatório indicar pelo menos uma permissão.");
        }
        List<Permissao> resultado = new ArrayList<>();
        for (String permissao : permissoes) {
            if (permissao == null || permissao.isBlank()) {
                continue;
            }
            resultado.add(Permissao.valueOf(permissao.trim().toUpperCase(java.util.Locale.ROOT)));
        }
        if (resultado.isEmpty()) {
            throw new IllegalArgumentException("É obrigatório indicar pelo menos uma permissão válida.");
        }
        return resultado;
    }

    private List<String> extrairListaTexto(Object origem) {
        if (!(origem instanceof List<?> lista)) {
            return List.of();
        }
        return lista.stream().filter(Objects::nonNull).map(String::valueOf).collect(Collectors.toList());
    }

    private Map<String, Object> mapearPerfil(PerfilAcesso perfil) {
        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("id", perfil.getId());
        resultado.put("nome", perfil.getNome());
        resultado.put("permissoes", perfil.getPermissoes().stream().map(Enum::name).collect(Collectors.toList()));
        return resultado;
    }

    private Map<String, Object> mapearPromocao(Promocao promocao) {
        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("idPromocao", promocao.getIdPromocao());
        resultado.put("designacao", promocao.getDesignacao());
        resultado.put("desconto", promocao.getDesconto());
        resultado.put("dataInicio", promocao.getDataInicio() != null ? promocao.getDataInicio().toString() : null);
        resultado.put("dataFim", promocao.getDataFim() != null ? promocao.getDataFim().toString() : null);
        resultado.put("estado", promocao.getEstado());
        resultado.put("idLoja", promocao.getIdLoja());
        resultado.put("produtos", promocao.getProdutos());
        resultado.put("categorias", promocao.getCategorias());
        return resultado;
    }

    private <T> T executarOperacaoDatas(OperacaoDatas<T> operacao) {
        try {
            return operacao.executar();
        } catch (DatasInvalidasException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    @FunctionalInterface
    private interface OperacaoDatas<T> {
        T executar() throws DatasInvalidasException;
    }

    // --- Faturas / SAF-T ---

    public void gerarSegundaViaPDF(Context ctx) {
        try {
            String idFatura = ctx.pathParam("idFatura");
            byte[] pdf = this.faturaService.gerarFaturaPDF(idFatura);
            ctx.contentType("application/pdf")
               .header("Content-Disposition", "inline; filename=Fatura_" + idFatura + ".pdf")
               .result(new java.io.ByteArrayInputStream(pdf));
        } catch (Exception e) {
            TratadorGlobalExcecoesApi.responder(ctx, 500, TratadorGlobalExcecoesApi.codePorStatus(500), "Falha ao gerar PDF da fatura.", "Falha ao gerar PDF da fatura.");
        }
    }

    public void gerarSaft(Context ctx) {
        try {
            int ano = Integer.parseInt(ctx.queryParamAsClass("ano", String.class).getOrDefault(String.valueOf(java.time.LocalDate.now().getYear())));
            int mes = Integer.parseInt(ctx.queryParamAsClass("mes", String.class).getOrDefault(String.valueOf(java.time.LocalDate.now().getMonthValue())));
            java.time.LocalDate inicio = java.time.LocalDate.of(ano, mes, 1);
            java.time.LocalDate fim = inicio.withDayOfMonth(inicio.lengthOfMonth());
            String xml = this.faturaService.exportarSaftPt(inicio, fim);
            ctx.contentType("application/xml; charset=utf-8")
               .header("Content-Disposition", "attachment; filename=SAFT_" + ano + "_" + mes + ".xml")
               .result(xml);
        } catch (Exception e) {
            TratadorGlobalExcecoesApi.responder(ctx, 500, TratadorGlobalExcecoesApi.codePorStatus(500), "Erro ao gerar SAF-T: " + e.getMessage(), e.getMessage());
        }
    }

    // --- Relatórios PDF ---

    public void exportarRelatorioVendasPDF(Context ctx) {
        try {
            String inicioStr = ctx.queryParam("startDate");
            String fimStr = ctx.queryParam("endDate");
            Integer idLoja = lerInteiroOpcional(ctx, "idLoja");
            int lojaFiltro = (idLoja == null || idLoja <= 0) ? 0 : idLoja;

            List<Venda> vendas = new ArrayList<>(this.vendaDAO.findAll()).stream()
                    .filter(v -> lojaFiltro == 0 || v.getIdLoja() == lojaFiltro)
                    .collect(Collectors.toList());

            if (inicioStr != null && fimStr != null) {
                LocalDateTime inicio = java.time.LocalDate.parse(inicioStr).atStartOfDay();
                LocalDateTime fim = java.time.LocalDate.parse(fimStr).atTime(23, 59, 59);
                vendas = vendas.stream()
                        .filter(v -> v.getDataHora() != null && !v.getDataHora().isBefore(inicio) && !v.getDataHora().isAfter(fim))
                        .collect(Collectors.toList());
            }

            byte[] pdf = this.relatorioService.gerarRelatorioVendasPDF(vendas, "Relatório de Vendas");
            ctx.contentType("application/pdf")
               .header("Content-Disposition", "attachment; filename=relatorio_vendas.pdf")
               .result(new java.io.ByteArrayInputStream(pdf));
        } catch (Exception e) {
            TratadorGlobalExcecoesApi.responder(ctx, 500, TratadorGlobalExcecoesApi.codePorStatus(500), "Erro ao exportar PDF de vendas.", "Erro ao exportar PDF de vendas.");
        }
    }

    public void exportarRelatorioInventarioPDF(Context ctx) {
        try {
            Integer idLoja = lerInteiroOpcional(ctx, "idLoja");
            int lojaFiltro = (idLoja == null || idLoja <= 0) ? 0 : idLoja;

            List<Inventario> itens = new ArrayList<>(this.inventarioDAO.findAll()).stream()
                    .filter(i -> lojaFiltro == 0 || i.getIdLoja() == lojaFiltro)
                    .collect(Collectors.toList());
            Map<String, Produto> mapaProds = this.produtoDAO.findAll().stream()
                    .collect(Collectors.toMap(Produto::getIdProduto, p -> p, (a, b) -> a));

            byte[] pdf = this.relatorioService.gerarRelatorioInventarioPDF(itens, mapaProds);
            ctx.contentType("application/pdf")
               .header("Content-Disposition", "attachment; filename=relatorio_inventario.pdf")
               .result(new java.io.ByteArrayInputStream(pdf));
        } catch (Exception e) {
            TratadorGlobalExcecoesApi.responder(ctx, 500, TratadorGlobalExcecoesApi.codePorStatus(500), "Erro ao exportar PDF de inventário.", "Erro ao exportar PDF de inventário.");
        }
    }
}
