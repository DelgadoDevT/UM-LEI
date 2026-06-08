package pt.uminho.taki.api.local;

import io.javalin.http.Context;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.NotFoundResponse;
import io.javalin.http.UnauthorizedResponse;
import pt.uminho.taki.api.NodeRole;
import pt.uminho.taki.api.NodeRoleConfig;
import pt.uminho.taki.api.shared.erros.ErroApiResponse;
import pt.uminho.taki.api.shared.seguranca.ContextoAutenticacao;
import pt.uminho.taki.api.shared.seguranca.JwtAuthUtils;
import pt.uminho.taki.api.shared.seguranca.MiddlewareSegurancaApi;
import pt.uminho.taki.ln.ITakiLNLocal;
import pt.uminho.taki.ln.fornecimentos.Encomenda;
import pt.uminho.taki.ln.fornecimentos.Fornecedor;
import pt.uminho.taki.ln.fornecimentos.LinhaEncomenda;
import pt.uminho.taki.ln.inventario.Inventario;
import pt.uminho.taki.ln.inventario.MovimentoInventario;
import pt.uminho.taki.ln.inventario.TipoMovimento;
import pt.uminho.taki.ln.lojas.Categoria;
import pt.uminho.taki.ln.lojas.EstadoConta;
import pt.uminho.taki.ln.lojas.Funcionario;
import pt.uminho.taki.ln.lojas.PerfilAcesso;
import pt.uminho.taki.ln.lojas.Permissao;
import pt.uminho.taki.ln.lojas.Produto;
import pt.uminho.taki.ln.lojas.TaxaIva;
import pt.uminho.taki.ln.vendas.LinhaVenda;
import pt.uminho.taki.ln.vendas.Promocao;
import pt.uminho.taki.ln.vendas.Venda;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import pt.uminho.taki.api.shared.erros.TratadorGlobalExcecoesApi;

/**
 * Controlador de API REST responsável pela gestão das operações locais de loja, com inclusão de vendas, inventário e autenticação local.
 * 
 * @author TakiLN Team
 * @since 1.0
 */
public class LocalApiController {

    /** Nome da variável de ambiente que contém a chave privada RSA para assinatura de tokens JWT. */
    private static final String VARIAVEL_CHAVE_PRIVADA_JWT = "TAKI_JWT_PRIVATE_KEY";
    /** Duração da validade do token de autenticação em segundos (8 horas). */
    private static final long DURACAO_TOKEN_SEGUNDOS = 8 * 60 * 60;

    /** Interface de fachada para as operações da lógica de negócio local. */
    private final ITakiLNLocal takiLNLocal;
    /** Serviço de relatórios. */
    private final pt.uminho.taki.ln.report.RelatorioService relatorioService;
    /** Mapa volátil para gestão de vendas que se encontram em processo de registo. */
    private final ConcurrentMap<String, Venda> vendasEmCurso;
    private final pt.uminho.taki.dao.StatisticsDAO statisticsDAO;
    private final pt.uminho.taki.dao.DevolucaoDAO devolucaoDAO;
    private final pt.uminho.taki.dao.VendaDAO vendaDAO = new pt.uminho.taki.dao.VendaDAO();

    /**
     * Construtor para o controlador LocalApiController.
     * 
     * @param takiLNLocal a instância da lógica de negócio local (ITakiLNLocal)
     * @param statisticsDAO
     * @param devolucaoDAO
     * @param relatorioService
     * @throws NullPointerException se a instância de takiLNLocal for nula
     */
    public LocalApiController(ITakiLNLocal takiLNLocal, pt.uminho.taki.dao.StatisticsDAO statisticsDAO, pt.uminho.taki.dao.DevolucaoDAO devolucaoDAO, pt.uminho.taki.ln.report.RelatorioService relatorioService) {
        this.takiLNLocal = Objects.requireNonNull(takiLNLocal, "takiLNLocal");
        this.relatorioService = Objects.requireNonNull(relatorioService, "relatorioService");
        this.vendasEmCurso = new ConcurrentHashMap<>();
        this.statisticsDAO = Objects.requireNonNull(statisticsDAO, "statisticsDAO");
        this.devolucaoDAO = Objects.requireNonNull(devolucaoDAO, "devolucaoDAO");
    }

    /**
     * Efetua a autenticação de um funcionário no sistema.
     * 
     * @param ctx o contexto do pedido HTTP (Javalin Context)
     * @throws UnauthorizedResponse se as credenciais de acesso forem inválidas
     * @throws IllegalArgumentException se faltarem campos obrigatórios no pedido
     */
    public void autenticar(Context ctx) {
        LocalApiDtos.PedidoAutenticacao pedido = ctx.bodyValidator(LocalApiDtos.PedidoAutenticacao.class).get();
        String email = campoObrigatorio(pedido.getEmail(), "email");
        String password = campoObrigatorio(pedido.getPassword(), "password");

        Funcionario funcionario = this.takiLNLocal.autenticar(email, password);
        if (funcionario == null) {
            throw new UnauthorizedResponse("Utilizador ou palavra-passe incorretos.");
        }
        if (NodeRoleConfig.resolverRoleRuntime() == NodeRole.CENTRAL && !temPerfilGestaoCentral(funcionario)) {
            throw new ForbiddenResponse("Acesso negado: permissões insuficientes para o modo central");
        }
        String token = gerarTokenJwt(funcionario);

        // Set HttpOnly Secure Cookie
        ctx.cookie(new io.javalin.http.Cookie(
            "auth_token", 
            token, 
            "/", 
            (int) DURACAO_TOKEN_SEGUNDOS, 
            "PROD".equalsIgnoreCase(System.getenv("APP_MODE")), 
            1, 
            true
        ));

        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("token", token); // Retido temporariamente para compatibilidade até o Frontend ser atualizado no passo seguinte, mas o cookie já está a ser enviado
        resposta.put("funcionario", mapearFuncionario(funcionario));
        responderComDados(ctx, 200, resposta);
    }

    /**
     * Termina a sessão do utilizador atual.
     * 
     * @param ctx o contexto do pedido HTTP (Javalin Context)
     */
    public void logout(Context ctx) {
        ctx.removeCookie("auth_token", "/");
        responderComSucesso(ctx, 200, "Logout efetuado com sucesso.");
    }

    /**
     * Verifica se a palavra-passe fornecida corresponde à do utilizador autenticado.
     * 
     * @param ctx o contexto do pedido HTTP (Javalin Context)
     */
    public void verificarPassword(Context ctx) {
        LocalApiDtos.PedidoVerificacaoPassword pedido = ctx.bodyValidator(LocalApiDtos.PedidoVerificacaoPassword.class).get();
        String password = pedido.getPassword();
        
        ContextoAutenticacao contexto = MiddlewareSegurancaApi.obterContexto(ctx).orElse(null);
        boolean valida = false;

        if (contexto != null && password != null && !password.isBlank()) {
            try {
                valida = this.takiLNLocal.autenticar(contexto.getEmail(), password) != null;
            } catch (Exception e) {
                valida = false;
            }
        }

        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("valid", valida);
        responderComDados(ctx, 200, resposta);
    }

    /**
     * Obtém os detalhes da sessão do utilizador autenticado.
     * 
     * @param ctx o contexto do pedido HTTP (Javalin Context)
     * @throws UnauthorizedResponse se o utilizador não estiver autenticado
     */
    public void obterSessaoAtual(Context ctx) {
        ContextoAutenticacao contexto = MiddlewareSegurancaApi.obterContexto(ctx)
                .orElseThrow(() -> new UnauthorizedResponse("Autenticação obrigatória."));

        Optional<Funcionario> funcionario = this.takiLNLocal.buscarFuncionarioPorId(contexto.getSubject());

        Map<String, Object> dados = new LinkedHashMap<>();
        dados.put("subject", contexto.getSubject());
        dados.put("roles", contexto.getRoles());
        dados.put("scopes", contexto.getScopes());
        funcionario.ifPresent(value -> dados.put("funcionario", mapearFuncionario(value)));

        responderComDados(ctx, 200, dados);
    }

    /**
     * Lista todos os produtos disponíveis, com possibilidade de filtragem por loja.
     * 
     * @param ctx o contexto do pedido HTTP (Javalin Context)
     */
    public void listarProdutos(Context ctx) {
        String idLojaParam = ctx.queryParam("idLoja");
        // Se idLoja=0, idLoja=all ou nulo -> visão global (null no filtro)
        Integer idLojaFiltro = (idLojaParam != null && !idLojaParam.equals("all") && !idLojaParam.equals("0")) ? Integer.parseInt(idLojaParam) : null;

        Map<String, Inventario> inventarioPorProduto = this.takiLNLocal.listarInventario().stream()
                .filter(i -> idLojaFiltro == null || i.getIdLoja() == idLojaFiltro)
                .collect(Collectors.toMap(Inventario::getIdProduto, i -> i, (a, b) -> a));

        Map<String, String> categoriasMap = this.takiLNLocal.listarCategorias().stream()
                .collect(Collectors.toMap(Categoria::getIdCategoria, Categoria::getDesignacao, (a, b) -> a));

        List<Map<String, Object>> produtos = this.takiLNLocal.listarProdutos(idLojaFiltro).stream()
                .map(produto -> {
                    Map<String, Object> map = mapearProduto(produto, inventarioPorProduto.get(produto.getIdProduto()));
                    String idCat = (String) map.get("idCategoria");
                    if (idCat != null && !idCat.isEmpty()) {
                        map.put("categoriaDesignacao", categoriasMap.getOrDefault(idCat, "Categoria descontinuada"));
                    }
                    return map;
                })
                .toList();
        responderComDados(ctx, 200, produtos);
    }

    /**
     * Verifica se o utilizador possui permissões de escrita global.
     *
     * @param ctx o contexto do pedido HTTP (Javalin Context)
     * @throws io.javalin.http.ForbiddenResponse se o acesso for negado a utilizadores com perfil de proprietário ou administrador global
     */
    private void verificarEscritaGlobal(Context ctx) {
        ContextoAutenticacao auth = MiddlewareSegurancaApi.obterContexto(ctx).orElse(null);
        if (auth != null && auth.getRoles() != null) {
            boolean isGlobal = auth.getRoles().stream()
                    .anyMatch(r -> r.toUpperCase().contains("PROPRIET") || r.toUpperCase().contains("ADMIN"));
            if (isGlobal) {
                throw new io.javalin.http.ForbiddenResponse("Utilizadores Globais têm acesso apenas de leitura.");
            }
        }
    }

    /**
     * Adiciona um novo produto ao sistema.
     * 
     * @param ctx o contexto do pedido HTTP (Javalin Context)
     * @throws Exception se ocorrer um erro durante a persistência do produto ou do inventário inicial
     * @throws io.javalin.http.ForbiddenResponse se o utilizador for global
     */
    public void adicionarProduto(Context ctx) throws Exception {
        verificarEscritaGlobal(ctx);
        LocalApiDtos.PedidoAdicionarProduto pedido = ctx.bodyValidator(LocalApiDtos.PedidoAdicionarProduto.class).get();
        Produto produto = new Produto(
                campoObrigatorio(pedido.getIdProduto(), "idProduto"),
                campoObrigatorio(pedido.getCodigoBarras(), "codigoBarras"),
                campoObrigatorio(pedido.getNome(), "nome"),
                campoObrigatorio(pedido.getDescricao(), "descricao"),
                decimalObrigatorioPositivo(pedido.getPrecoCusto(), "precoCusto"),
                decimalObrigatorioPositivo(pedido.getPrecoVenda(), "precoVenda"),
                resolverTaxaIva(pedido.getTaxaIva()),
                valorOuOmissao(pedido.getUnidadeMedida(), "unidade"),
                valorOuOmissao(pedido.getEstado(), "Ativo")
        );
        
        ContextoAutenticacao auth = MiddlewareSegurancaApi.obterContexto(ctx).orElse(null);
        String idFunc = auth != null ? auth.getSubject() : "SISTEMA";

        this.takiLNLocal.adicionarProduto(produto);
        
        // Se houver stock, atualizar/criar inventário
        if (pedido.getStock() != null || pedido.getMinStock() != null) {
            this.takiLNLocal.atualizarArtigoComStock(produto, pedido.getStock(), pedido.getMinStock(), idFunc);
        }

        responderComDados(ctx, 201, mapearProduto(produto));
    }

    /**
     * Edita os detalhes de um produto existente.
     * 
     * @param ctx o contexto do pedido HTTP (Javalin Context)
     * @throws Exception se ocorrer um erro durante a atualização dos dados
     * @throws io.javalin.http.ForbiddenResponse se o utilizador for global
     * @throws IllegalArgumentException se faltarem campos obrigatórios
     */
    public void editarProduto(Context ctx) throws Exception {
        verificarEscritaGlobal(ctx);
        String idProduto = campoObrigatorio(ctx.pathParam("idProduto"), "idProduto");
        LocalApiDtos.PedidoEditarProduto pedido = ctx.bodyValidator(LocalApiDtos.PedidoEditarProduto.class).get();

        Produto produto = new Produto(
                idProduto,
                campoObrigatorio(pedido.getCodigoBarras(), "codigoBarras"),
                campoObrigatorio(pedido.getNome(), "nome"),
                campoObrigatorio(pedido.getDescricao(), "descricao"),
                decimalObrigatorioPositivo(pedido.getPrecoCusto(), "precoCusto"),
                decimalObrigatorioPositivo(pedido.getPrecoVenda(), "precoVenda"),
                resolverTaxaIva(pedido.getTaxaIva()),
                valorOuOmissao(pedido.getUnidadeMedida(), "unidade"),
                valorOuOmissao(pedido.getEstado(), "Ativo")
        );
        
        this.takiLNLocal.atualizarArtigoComStock(produto, pedido.getStock(), pedido.getMinStock(), obterIdFuncionarioAutenticado(ctx));
        responderComDados(ctx, 200, mapearProduto(produto));
    }

    /**
     * Obtém o identificador do funcionário autenticado a partir do contexto da sessão.
     *
     * @param ctx o contexto do pedido HTTP (Javalin Context)
     * @return o identificador do funcionário autenticado ou uma string vazia se não autenticado
     */
    private String obterIdFuncionarioAutenticado(Context ctx) {
        return MiddlewareSegurancaApi.obterContexto(ctx)
                .map(ContextoAutenticacao::getSubject)
                .orElse("");
    }

    /**
     * Inativa logicamente um produto no sistema.
     * 
     * @param ctx o contexto do pedido HTTP (Javalin Context)
     * @throws Exception se ocorrer um erro durante a inativação
     * @throws IllegalArgumentException se o identificador do produto não for fornecido
     */
    public void inativarProduto(Context ctx) throws Exception {
        String idProduto = campoObrigatorio(ctx.pathParam("idProduto"), "idProduto");
        this.takiLNLocal.inativarProduto(idProduto);
        responderComSucesso(ctx, 200, "Produto inativado com sucesso.");
    }

    /**
     * Lista todas as categorias de produtos registadas.
     * 
     * @param ctx o contexto do pedido HTTP (Javalin Context)
     */
    public void listarCategorias(Context ctx) {
        List<Map<String, Object>> categorias = this.takiLNLocal.listarCategorias().stream()
                .map(this::mapearCategoria)
                .toList();
        responderComDados(ctx, 200, categorias);
    }

    /**
     * Regista uma nova categoria de produtos.
     * 
     * @param ctx o contexto do pedido HTTP (Javalin Context)
     * @throws Exception se ocorrer um erro ao adicionar a categoria
     */
    public void adicionarCategoria(Context ctx) throws Exception {
        LocalApiDtos.PedidoAdicionarCategoria pedido = ctx.bodyValidator(LocalApiDtos.PedidoAdicionarCategoria.class).get();
        Categoria categoria = pedido.paraDominio();

        this.takiLNLocal.adicionarCategoria(categoria);
        responderComDados(ctx, 201, mapearCategoria(categoria));
    }

    /**
     * Atualiza os dados de uma categoria de produtos existente.
     * 
     * @param ctx o contexto do pedido HTTP (Javalin Context)
     * @throws Exception se ocorrer um erro durante a edição
     * @throws IllegalArgumentException se faltarem campos obrigatórios
     */
    public void editarCategoria(Context ctx) throws Exception {
        String idCategoria = campoObrigatorio(ctx.pathParam("idCategoria"), "idCategoria");
        LocalApiDtos.PedidoEditarCategoria pedido = ctx.bodyValidator(LocalApiDtos.PedidoEditarCategoria.class).get();

        Categoria categoria = new Categoria(
                idCategoria,
                campoObrigatorio(pedido.getDesignacao(), "designacao"),
                valorOuOmissao(pedido.getIdCategoriaPai(), "")
        );
        this.takiLNLocal.editarCategoria(categoria);
        responderComDados(ctx, 200, mapearCategoria(categoria));
    }

    /**
     * Inativa uma categoria de produtos existente.
     * 
     * @param ctx o contexto do pedido HTTP (Javalin Context)
     * @throws Exception se ocorrer um erro ao inativar a categoria
     * @throws IllegalArgumentException se o identificador da categoria não for fornecido
     */
    public void inativarCategoria(Context ctx) throws Exception {
        String idCategoria = campoObrigatorio(ctx.pathParam("idCategoria"), "idCategoria");
        this.takiLNLocal.inativarCategoria(idCategoria);
        responderComSucesso(ctx, 200, "Categoria inativada com sucesso.");
    }

    /**
     * Lista os funcionários do sistema, permitindo filtrar por loja.
     * 
     * @param ctx o contexto do pedido HTTP (Javalin Context)
     */
    public void listarFuncionarios(Context ctx) {
        String idLojaParam = ctx.queryParam("idLoja");
        Integer idLojaFiltro = (idLojaParam != null && !idLojaParam.equals("all") && !idLojaParam.equals("0")) ? Integer.parseInt(idLojaParam) : null;

        List<Map<String, Object>> dados = this.takiLNLocal.listarFuncionarios().stream()
                .filter(f -> f.getEstadoConta() != EstadoConta.INATIVO)
                .filter(f -> idLojaFiltro == null || f.getIdLoja() == idLojaFiltro)
                .map(this::mapearFuncionario)
                .toList();
        responderComDados(ctx, 200, dados);
    }


    /**
     * Efetua o registo de um novo funcionário.
     * 
     * @param ctx o contexto do pedido HTTP (Javalin Context)
     * @throws io.javalin.http.ForbiddenResponse se o utilizador for global
     */
    public void registarFuncionario(Context ctx) {
        verificarEscritaGlobal(ctx);
        LocalApiDtos.PedidoRegistoFuncionario pedido = ctx.bodyValidator(LocalApiDtos.PedidoRegistoFuncionario.class).get();
        Funcionario funcionario = pedido.paraDominio();
        this.takiLNLocal.registarFuncionario(funcionario);
        responderComDados(ctx, 201, mapearFuncionario(funcionario));
    }

    /**
     * Atualiza os dados de um funcionário existente.
     * 
     * @param ctx o contexto do pedido HTTP (Javalin Context)
     * @throws Exception se ocorrer um erro ao atualizar os dados
     * @throws io.javalin.http.ForbiddenResponse se o utilizador for global
     * @throws IllegalArgumentException se o identificador do funcionário não for fornecido
     */
    public void atualizarFuncionario(Context ctx) throws Exception {
        verificarEscritaGlobal(ctx);
        String idFuncionario = campoObrigatorio(ctx.pathParam("idFuncionario"), "idFuncionario");
        LocalApiDtos.PedidoAtualizarFuncionario pedido = ctx.bodyValidator(LocalApiDtos.PedidoAtualizarFuncionario.class).get();

        Funcionario funcionario = pedido.paraDominio(idFuncionario);
        verificarPodeAtribuirPerfil(ctx, idFuncionario, funcionario.getIdPerfilAcesso());
        this.takiLNLocal.atualizarFuncionario(funcionario);
        responderComDados(ctx, 200, mapearFuncionario(funcionario));
    }

    /**
     * Bloqueia a conta de um funcionário.
     * 
     * @param ctx o contexto do pedido HTTP (Javalin Context)
     * @throws Exception se ocorrer um erro durante o bloqueio
     * @throws io.javalin.http.ForbiddenResponse se o utilizador for global
     * @throws UnauthorizedResponse se o utilizador administrador não estiver autenticado
     */
    public void bloquearFuncionario(Context ctx) throws Exception {
        verificarEscritaGlobal(ctx);
        String idFuncionario = campoObrigatorio(ctx.pathParam("idFuncionario"), "idFuncionario");
        verificarPodeModificarFuncionario(ctx, idFuncionario);
        // Removemos a obrigatoriedade de password para bloqueio simples,
        // confiando no token de Administrador/Gerente do middleware
        String idAdministrador = obterAdministradorSessao(ctx);
        
        // Chamada à LN sem password (passamos null ou string vazia se a interface permitir, 
        // ou usamos o método que não pede password se existir)
        // Como a TakiLNLocal.bloquearConta pede password, vamos passar uma flag ou usar null
        this.takiLNLocal.bloquearConta(idFuncionario, idAdministrador, null);
        responderComSucesso(ctx, 200, "Conta bloqueada com sucesso.");
    }

    /**
     * Desbloqueia a conta de um funcionário anteriormente bloqueada.
     * 
     * @param ctx o contexto do pedido HTTP (Javalin Context)
     * @throws Exception se ocorrer um erro ao desbloquear
     * @throws io.javalin.http.ForbiddenResponse se o utilizador for global
     * @throws NotFoundResponse se o funcionário não for encontrado
     */
    public void desbloquearFuncionario(Context ctx) throws Exception {
        verificarEscritaGlobal(ctx);
        String idFuncionario = campoObrigatorio(ctx.pathParam("idFuncionario"), "idFuncionario");
        verificarPodeModificarFuncionario(ctx, idFuncionario);
        // Para desbloquear também removemos a barreira de password repetitiva
        
        Funcionario funcionario = this.takiLNLocal.buscarFuncionarioPorId(idFuncionario)
                .orElseThrow(() -> new NotFoundResponse("Funcionário não encontrado: " + idFuncionario))
                .clone();

        funcionario.setEstadoConta(EstadoConta.ATIVO);
        this.takiLNLocal.atualizarFuncionario(funcionario);
        responderComSucesso(ctx, 200, "Conta desbloqueada com sucesso.");
    }

    /**
     * Remove logicamente um funcionário do sistema.
     * 
     * @param ctx o contexto do pedido HTTP (Javalin Context)
     * @throws Exception se ocorrer um erro ao remover a conta
     * @throws io.javalin.http.ForbiddenResponse se o utilizador for global
     * @throws UnauthorizedResponse se a autenticação de administrador falhar
     */
    public void removerFuncionario(Context ctx) throws Exception {
        verificarEscritaGlobal(ctx);
        String idFuncionario = campoObrigatorio(ctx.pathParam("idFuncionario"), "idFuncionario");
        verificarPodeModificarFuncionario(ctx, idFuncionario);
        LocalApiDtos.PedidoConfirmacaoAdmin pedido = ctx.bodyValidator(LocalApiDtos.PedidoConfirmacaoAdmin.class).get();
        String idAdministrador = obterAdministradorSessao(ctx);
        this.takiLNLocal.removerContaLogicamente(idFuncionario, idAdministrador, pedido.getPasswordAdministrador());
        responderComSucesso(ctx, 200, "Conta inativada com sucesso.");
    }

    /**
     * Atribui um novo perfil de acesso a um funcionário.
     * 
     * @param ctx o contexto do pedido HTTP (Javalin Context)
     * @throws Exception se ocorrer um erro na atribuição do perfil
     * @throws io.javalin.http.ForbiddenResponse se o utilizador for global
     * @throws IllegalArgumentException se o nome do perfil não for fornecido
     */
    public void atribuirPerfilFuncionario(Context ctx) throws Exception {
        verificarEscritaGlobal(ctx);
        String idFuncionario = campoObrigatorio(ctx.pathParam("idFuncionario"), "idFuncionario");
        LocalApiDtos.PedidoAtribuirPerfil pedido = ctx.bodyValidator(LocalApiDtos.PedidoAtribuirPerfil.class).get();
        verificarPodeAtribuirPerfil(ctx, idFuncionario, pedido.getNomePerfil());
        String idAdministrador = obterAdministradorSessao(ctx);
        this.takiLNLocal.atribuirPerfil(idFuncionario, campoObrigatorio(pedido.getNomePerfil(), "nomePerfil"), idAdministrador);
        responderComSucesso(ctx, 200, "Perfil atribuído com sucesso.");
    }

    /**
     * Regista um novo movimento manual de inventário (ex: quebra, ajuste).
     * 
     * @param ctx o contexto do pedido HTTP (Javalin Context)
     * @throws Exception se ocorrer um erro ao registar o movimento
     * @throws io.javalin.http.ForbiddenResponse se o utilizador for global
     * @throws IllegalArgumentException se os dados do movimento forem inválidos
     */
    public void registarMovimentoInventario(Context ctx) throws Exception {
        verificarEscritaGlobal(ctx);
        LocalApiDtos.PedidoRegistoMovimentoInventario pedido = ctx.bodyValidator(LocalApiDtos.PedidoRegistoMovimentoInventario.class).get();
        MovimentoInventario movimento = pedido.paraDominio();
        movimento.setTipo(resolverTipoMovimento(pedido.getTipo()));
        movimento.setDataRegisto(resolverDataRegisto(pedido.getDataRegisto()));

        this.takiLNLocal.registarMovimentoManual(movimento);
        responderComDados(ctx, 201, mapearMovimentoInventario(movimento));
    }

    /**
     * Lista o inventário atual, com opção de filtragem por loja.
     * 
     * @param ctx o contexto do pedido HTTP (Javalin Context)
     */
    public void listarInventario(Context ctx) {
        String idLojaParam = ctx.queryParam("idLoja");
        Integer idLojaFiltro = (idLojaParam != null && !idLojaParam.equals("all") && !idLojaParam.equals("0")) ? Integer.parseInt(idLojaParam) : null;

        List<Map<String, Object>> inventario = this.takiLNLocal.listarInventario().stream()
                .filter(i -> idLojaFiltro == null || i.getIdLoja() == idLojaFiltro)
                .map(this::mapearInventario)
                .toList();
        responderComDados(ctx, 200, inventario);
    }

    /**
     * Lista todos os fornecedores registados.
     * 
     * @param ctx o contexto do pedido HTTP (Javalin Context)
     */
    public void listarFornecedores(Context ctx) {
        List<Map<String, Object>> fornecedores = this.takiLNLocal.listarFornecedores().stream()
                .map(this::mapearFornecedor)
                .toList();
        responderComDados(ctx, 200, fornecedores);
    }

    /**
     * Adiciona um novo fornecedor ao sistema.
     * 
     * @param ctx o contexto do pedido HTTP (Javalin Context)
     * @throws Exception se ocorrer um erro ao adicionar o fornecedor
     * @throws io.javalin.http.ForbiddenResponse se o utilizador for global
     */
    public void adicionarFornecedor(Context ctx) throws Exception {
        verificarEscritaGlobal(ctx);
        LocalApiDtos.PedidoFornecedor pedido = ctx.bodyValidator(LocalApiDtos.PedidoFornecedor.class).get();
        Fornecedor fornecedor = pedido.paraDominio();
        this.takiLNLocal.adicionarFornecedor(fornecedor);
        responderComDados(ctx, 201, mapearFornecedor(fornecedor));
    }

    /**
     * Edita os dados de um fornecedor existente.
     * 
     * @param ctx o contexto do pedido HTTP (Javalin Context)
     * @throws Exception se ocorrer um erro durante a atualização
     * @throws io.javalin.http.ForbiddenResponse se o utilizador for global
     * @throws IllegalArgumentException se o identificador do fornecedor não for fornecido
     */
    public void editarFornecedor(Context ctx) throws Exception {
        verificarEscritaGlobal(ctx);
        String idFornecedor = campoObrigatorio(ctx.pathParam("idFornecedor"), "idFornecedor");
        LocalApiDtos.PedidoFornecedor pedido = ctx.bodyValidator(LocalApiDtos.PedidoFornecedor.class).get();
        Fornecedor fornecedor = pedido.paraDominio();
        fornecedor.setIdFornecedor(idFornecedor);

        this.takiLNLocal.editarFornecedor(fornecedor);
        responderComDados(ctx, 200, mapearFornecedor(fornecedor));
    }

    /**
     * Inativa logicamente um fornecedor.
     * 
     * @param ctx o contexto do pedido HTTP (Javalin Context)
     * @throws Exception se ocorrer um erro ao inativar o fornecedor
     * @throws IllegalArgumentException se o identificador do fornecedor não for fornecido
     */
    public void inativarFornecedor(Context ctx) throws Exception {
        String idFornecedor = campoObrigatorio(ctx.pathParam("idFornecedor"), "idFornecedor");
        this.takiLNLocal.inativarFornecedor(idFornecedor);
        responderComSucesso(ctx, 200, "Fornecedor inativado com sucesso.");
    }

    /**
     * Estabelece uma relação entre um produto e um fornecedor, indicando o preço de custo acordado.
     * 
     * @param ctx o contexto do pedido HTTP (Javalin Context)
     * @throws Exception se ocorrer um erro ao criar a associação
     * @throws IllegalArgumentException se os campos obrigatórios ou o preço forem inválidos
     */
    public void associarProdutoAFornecedor(Context ctx) throws Exception {
        LocalApiDtos.PedidoAssociarProdutoFornecedor pedido = ctx.bodyValidator(LocalApiDtos.PedidoAssociarProdutoFornecedor.class).get();
        String idProduto = campoObrigatorio(pedido.getIdProduto(), "idProduto");
        String idFornecedor = campoObrigatorio(pedido.getIdFornecedor(), "idFornecedor");
        double precoCusto = decimalObrigatorioPositivo(pedido.getPrecoCusto(), "precoCusto");

        this.takiLNLocal.associarProdutoAFornecedor(idProduto, idFornecedor, precoCusto);

        Map<String, Object> dados = new LinkedHashMap<>();
        dados.put("idProduto", idProduto);
        dados.put("idFornecedor", idFornecedor);
        dados.put("precoCusto", precoCusto);
        responderComDados(ctx, 200, dados);
    }

    /**
     * Lista as encomendas efetuadas, com opção de filtragem por loja.
     * 
     * @param ctx o contexto do pedido HTTP (Javalin Context)
     */
    public void listarEncomendas(Context ctx) {
        String idLojaParam = ctx.queryParam("idLoja");
        Integer idLojaFiltro = (idLojaParam != null && !idLojaParam.equals("all") && !idLojaParam.equals("0")) ? Integer.parseInt(idLojaParam) : null;

        Map<String, String> fornecedoresMap = this.takiLNLocal.listarFornecedores().stream()
                .collect(Collectors.toMap(Fornecedor::getIdFornecedor, Fornecedor::getNome, (a, b) -> a));

        List<Map<String, Object>> encomendas = this.takiLNLocal.listarEncomendas().stream()
                .filter(e -> idLojaFiltro == null || String.valueOf(idLojaFiltro).equals(e.getIdLoja()))
                .map(enc -> {
                    Map<String, Object> map = mapearEncomenda(enc);
                    map.put("nomeFornecedor", fornecedoresMap.getOrDefault(enc.getIdFornecedor(), enc.getIdFornecedor()));
                    return map;
                })
                .toList();
        responderComDados(ctx, 200, encomendas);
    }

    /**
     * Cria uma nova encomenda a um fornecedor para uma determinada loja.
     * 
     * @param ctx o contexto do pedido HTTP (Javalin Context)
     * @throws Exception se ocorrer um erro ao criar a encomenda ou associar produtos automaticamente
     * @throws io.javalin.http.ForbiddenResponse se o utilizador for global
     * @throws IllegalArgumentException se o identificador da encomenda ou fornecedor forem inválidos
     */
    public void criarEncomenda(Context ctx) throws Exception {
        verificarEscritaGlobal(ctx);
        LocalApiDtos.PedidoCriarEncomenda pedido = ctx.bodyValidator(LocalApiDtos.PedidoCriarEncomenda.class).get();

        String idFornecedor = pedido.getIdFornecedor();
        if (pedido.getLinhas() != null && !pedido.getLinhas().isEmpty()) {
            String idProduto = pedido.getLinhas().get(0).getIdProduto();
            List<String> fornecedores = this.takiLNLocal.listarFornecedoresDeProduto(idProduto);
            
            if (fornecedores != null && !fornecedores.isEmpty()) {
                if (idFornecedor == null || !fornecedores.contains(idFornecedor) || "AUTO_RESOLVE".equals(idFornecedor)) {
                    idFornecedor = fornecedores.get(0);
                }
            } else {
                // Product has no associated suppliers. Auto-associate it to proceed.
                if (idFornecedor == null || "AUTO_RESOLVE".equals(idFornecedor)) {
                    List<pt.uminho.taki.ln.fornecimentos.Fornecedor> todos = this.takiLNLocal.listarFornecedores();
                    if (!todos.isEmpty()) {
                        idFornecedor = todos.get(0).getIdFornecedor();
                    }
                }
                if (idFornecedor != null && !"AUTO_RESOLVE".equals(idFornecedor)) {
                    double precoCusto = pedido.getLinhas().get(0).getPrecoCusto();
                    if (precoCusto <= 0) precoCusto = 1.0;
                    this.takiLNLocal.associarProdutoAFornecedor(idProduto, idFornecedor, precoCusto);
                }
            }
        }

        this.takiLNLocal.criarEncomenda(
                campoObrigatorio(pedido.getIdEncomenda(), "idEncomenda"),
                campoObrigatorio(idFornecedor, "idFornecedor"),
                String.valueOf(pedido.getIdLoja()),
                pedido.resolverLinhas()
        );
        responderComSucesso(ctx, 201, "Encomenda criada com sucesso.");
    }

    /**
     * Processa a transição de estado de uma encomenda (ex: de Pendente para Entregue).
     * 
     * @param ctx o contexto do pedido HTTP (Javalin Context)
     * @throws io.javalin.http.ForbiddenResponse se o utilizador for global
     * @throws IllegalArgumentException se o identificador da encomenda não for fornecido
     */
    public void processarTransicaoEstadoEncomenda(Context ctx) {
        verificarEscritaGlobal(ctx);
        String idEncomenda = campoObrigatorio(ctx.pathParam("idEncomenda"), "idEncomenda");
        this.takiLNLocal.processarTransicaoEstado(idEncomenda);

        Encomenda atualizada = this.takiLNLocal.listarEncomendas().stream()
                .filter(e -> idEncomenda.equals(e.getIdEncomenda()))
                .findFirst()
                .orElse(null);

        Map<String, Object> dados = new LinkedHashMap<>();
        dados.put("idEncomenda", idEncomenda);
        dados.put("estadoProcessado", true);
        if (atualizada != null && atualizada.getEstadoAtual() != null) {
            dados.put("estado", atualizada.getEstadoAtual().getDesignacao());
        }
        responderComDados(ctx, 200, dados);
    }

    /**
     * Inicia um novo processo de venda em loja.
     * 
     * @param ctx o contexto do pedido HTTP (Javalin Context)
     * @throws io.javalin.http.ForbiddenResponse se o utilizador for global
     * @throws IllegalArgumentException se faltarem dados essenciais para iniciar a venda
     */
    public void iniciarVenda(Context ctx) {
        verificarEscritaGlobal(ctx);
        LocalApiDtos.PedidoIniciarVenda pedido = ctx.bodyValidator(LocalApiDtos.PedidoIniciarVenda.class).get();

        String idFuncionario = resolverIdFuncionarioVenda(ctx, pedido.getIdFuncionario());
        int idLoja = resolverIdLojaVenda(pedido.getIdLoja(), idFuncionario);

        Venda venda = this.takiLNLocal.iniciarVenda(idLoja, idFuncionario);
        this.vendasEmCurso.put(venda.getIdVenda(), venda);

        responderComDados(ctx, 201, mapearVenda(venda));
    }

    /**
     * Adiciona uma linha (produto e quantidade) a uma venda que se encontre em curso.
     * 
     * @param ctx o contexto do pedido HTTP (Javalin Context)
     * @throws io.javalin.http.ForbiddenResponse se o utilizador for global
     * @throws NotFoundResponse se a venda ou o produto não forem encontrados
     * @throws IllegalArgumentException se os campos obrigatórios ou a quantidade forem inválidos
     */
    public void adicionarLinhaVenda(Context ctx) {
        verificarEscritaGlobal(ctx);
        String idVenda = campoObrigatorio(ctx.pathParam("idVenda"), "idVenda");
        LocalApiDtos.PedidoAdicionarLinhaVenda pedido = ctx.bodyValidator(LocalApiDtos.PedidoAdicionarLinhaVenda.class).get();

        Venda venda = obterVendaEmCurso(idVenda);
        Produto produto = encontrarProdutoPorId(campoObrigatorio(pedido.getIdProduto(), "idProduto"));
        int quantidade = inteiroObrigatorioPositivo(pedido.getQuantidade(), "quantidade");

        this.takiLNLocal.adicionarLinhaVenda(venda, produto, quantidade);
        responderComDados(ctx, 200, mapearVenda(venda));
    }

    /**
     * Finaliza e regista uma venda, processando o pagamento e atualizando o stock.
     * 
     * @param ctx o contexto do pedido HTTP (Javalin Context)
     * @throws Exception se ocorrer um erro durante a finalização da venda
     * @throws io.javalin.http.ForbiddenResponse se o utilizador for global
     * @throws IllegalArgumentException se os dados de pagamento forem inválidos ou se houver rutura de stock
     */
    public void registarVenda(Context ctx) throws Exception {
        verificarEscritaGlobal(ctx);
        String idVenda = campoObrigatorio(ctx.pathParam("idVenda"), "idVenda");
        LocalApiDtos.PedidoRegistoVenda pedido = ctx.bodyValidator(LocalApiDtos.PedidoRegistoVenda.class).get();
        Venda venda = obterVendaEmCurso(idVenda);

        String metodoPagamento = campoObrigatorio(pedido.getMetodoPagamento(), "metodoPagamento");
        try {
            if (pedido.getValorEntregue() != null) {
                this.takiLNLocal.registarVenda(venda, metodoPagamento, pedido.getValorEntregue());
            } else {
                this.takiLNLocal.registarVenda(venda, metodoPagamento);
            }
        } catch (IllegalStateException e) {
            // Se for erro de stock (propagado do DAO/Trigger), transformamos em BadRequest legível
            if (e.getMessage().contains("stock") || e.getMessage().contains("Stock")) {
                TratadorGlobalExcecoesApi.responder(ctx, 400, TratadorGlobalExcecoesApi.codePorStatus(400), e.getMessage(), e.getMessage());
                return;
            }
            throw e;
        }

        this.vendasEmCurso.remove(idVenda);
        responderComDados(ctx, 200, mapearVenda(venda));
    }

    /**
     * Lista o histórico de vendas concluídas, permitindo filtrar por loja.
     * 
     * @param ctx o contexto do pedido HTTP (Javalin Context)
     */
    public void listarVendas(Context ctx) {
        String idLojaParam = ctx.queryParam("idLoja");
        Integer idLojaFiltro = (idLojaParam != null && !idLojaParam.equals("all") && !idLojaParam.equals("0")) ? Integer.parseInt(idLojaParam) : null;

        List<Map<String, Object>> vendas = this.takiLNLocal.listarVendas().stream()
                .filter(v -> idLojaFiltro == null || v.getIdLoja() == idLojaFiltro)
                .map(this::mapearVenda)
                .toList();
        responderComDados(ctx, 200, vendas);
    }

    /**
     * Obtém o mapeamento de quantidades já devolvidas por cada linha de venda.
     * 
     * @param ctx o contexto do pedido HTTP (Javalin Context)
     */
    public void obterLinhasDevolvidas(Context ctx) {
        // Acesso direto ao DAO para simplicidade neste caso específico de auditoria de linhas
        responderComDados(ctx, 200, this.devolucaoDAO.getQuantidadesDevolvidas());
    }

    /**
     * Lista o histórico de devoluções concluídas.
     * 
     * @param ctx o contexto do pedido HTTP (Javalin Context)
     */
    public void listarDevolucoes(Context ctx) {
        List<pt.uminho.taki.ln.vendas.Devolucao> devolucoes = this.takiLNLocal.listarDevolucoes();
        List<Map<String, Object>> devolucoesMapeadas = devolucoes.stream()
                .map(d -> {
                    Map<String, Object> map = new java.util.LinkedHashMap<>();
                    map.put("idDevolucao", d.getIdDevolucao());
                    map.put("idVenda", d.getIdVenda());
                    map.put("dataHora", d.getDataHora() != null ? d.getDataHora().toString() : null);
                    map.put("valor", d.getValor());
                    map.put("metodoReembolso", d.getMetodoReembolso());
                    map.put("numNotaCredito", d.getNumNotaCredito());
                    map.put("idFuncionario", d.getIdFuncionario());
                    return map;
                })
                .toList();
        responderComDados(ctx, 200, devolucoesMapeadas);
    }

    /**
     * Processa a devolução de produtos de uma venda concluída.
     * 
     * @param ctx o contexto do pedido HTTP (Javalin Context)
     * @throws Exception se ocorrer um erro ao processar a devolução
     * @throws NotFoundResponse se a venda ou as linhas de venda não forem encontradas
     * @throws IllegalArgumentException se a quantidade a devolver for inválida
     */
    public void processarDevolucao(Context ctx) throws Exception {
        String idVenda = campoObrigatorio(ctx.pathParam("idVenda"), "idVenda");
        LocalApiDtos.PedidoProcessarDevolucao pedido = ctx.bodyValidator(LocalApiDtos.PedidoProcessarDevolucao.class).get();

        Venda vendaOriginal = encontrarVendaConcluidaPorId(idVenda);
        List<LinhaVenda> linhasDevolucao = construirLinhasDevolucao(vendaOriginal, pedido.getLinhas());
        this.takiLNLocal.processarDevolucao(vendaOriginal, linhasDevolucao);

        Map<String, Object> dados = new LinkedHashMap<>();
        dados.put("idVendaOriginal", idVenda);
        dados.put("linhasDevolvidas", linhasDevolucao.stream().map(this::mapearLinhaVenda).toList());
        responderComDados(ctx, 200, dados);
    }

    /**
     * Lista as promoções que se encontram atualmente ativas.
     * 
     * @param ctx o contexto do pedido HTTP (Javalin Context)
     */
    public void listarPromocoesAtivas(Context ctx) {
        List<Map<String, Object>> promocoes = this.takiLNLocal.listarPromocoesAtivas().stream()
                .map(this::mapearPromocao)
                .toList();
        responderComDados(ctx, 200, promocoes);
    }

    /**
     * Adiciona uma nova campanha promocional ao sistema.
     * 
     * @param ctx o contexto do pedido HTTP (Javalin Context)
     * @throws Exception se ocorrer um erro ao adicionar a promoção
     * @throws io.javalin.http.ForbiddenResponse se o utilizador for global
     * @throws IllegalArgumentException se os dados da promoção forem inválidos
     */
    public void adicionarPromocao(Context ctx) throws Exception {
        verificarEscritaGlobal(ctx);
        LocalApiDtos.PedidoAdicionarPromocao pedido = ctx.bodyValidator(LocalApiDtos.PedidoAdicionarPromocao.class).get();
        Promocao promocao = pedido.paraDominio(dt -> resolverDataRegisto(dt));

        this.takiLNLocal.adicionarPromocao(promocao);
        responderComDados(ctx, 201, mapearPromocao(promocao));
    }

    /**
     * Cancela antecipadamente uma promoção que se encontre ativa.
     * 
     * @param ctx o contexto do pedido HTTP (Javalin Context)
     * @throws io.javalin.http.ForbiddenResponse se o utilizador for global
     * @throws IllegalArgumentException se o identificador ou o motivo do cancelamento não forem fornecidos
     */
    public void cancelarPromocao(Context ctx) {
        verificarEscritaGlobal(ctx);
        String idPromocao = campoObrigatorio(ctx.pathParam("idPromocao"), "idPromocao");
        LocalApiDtos.PedidoCancelarPromocao pedido = ctx.bodyValidator(LocalApiDtos.PedidoCancelarPromocao.class).get();
        this.takiLNLocal.cancelarPromocao(idPromocao, campoObrigatorio(pedido.getMotivo(), "motivo"));
        responderComSucesso(ctx, 200, "Promoção cancelada com sucesso.");
    }

    /**
     * Lista os perfis de acesso e respetivas permissões configurados.
     * 
     * @param ctx o contexto do pedido HTTP (Javalin Context)
     */
    public void listarPerfis(Context ctx) {
        List<Map<String, Object>> perfis = this.takiLNLocal.listarPerfis().stream()
                .map(this::mapearPerfil)
                .toList();
        responderComDados(ctx, 200, perfis);
    }

    /**
     * Regista um novo perfil de acesso no sistema.
     * 
     * @param ctx o contexto do pedido HTTP (Javalin Context)
     * @throws Exception se ocorrer um erro ao registar o perfil
     * @throws IllegalArgumentException se as permissões fornecidas forem inválidas
     */
    public void registarPerfil(Context ctx) throws Exception {
        LocalApiDtos.PedidoRegistarPerfil pedido = ctx.bodyValidator(LocalApiDtos.PedidoRegistarPerfil.class).get();
        PerfilAcesso perfil = pedido.paraDominio(this::resolverPermissoes);

        this.takiLNLocal.registarPerfil(perfil);
        responderComDados(ctx, 201, mapearPerfil(perfil));
    }

    /**
     * Atualiza as permissões associadas a um perfil de acesso existente.
     * 
     * @param ctx o contexto do pedido HTTP (Javalin Context)
     * @throws IllegalArgumentException se o nome do perfil ou as permissões forem inválidos
     */
    public void editarPerfil(Context ctx) {
        String nomePerfil = campoObrigatorio(ctx.pathParam("nomePerfil"), "nomePerfil");
        LocalApiDtos.PedidoEditarPerfil pedido = ctx.bodyValidator(LocalApiDtos.PedidoEditarPerfil.class).get();

        this.takiLNLocal.editarPerfil(nomePerfil, resolverPermissoes(pedido.getPermissoes()));
        responderComSucesso(ctx, 200, "Perfil atualizado com sucesso.");
    }

    /**
     * Aciona o processo de sincronização manual de dados com o servidor central.
     * 
     * @param ctx o contexto do pedido HTTP (Javalin Context)
     * @throws Exception se ocorrer uma falha crítica durante a sincronização
     */
    public void sincronizarDados(Context ctx) throws Exception {
        try {
            this.takiLNLocal.sincronizarDados();
            Map<String, Object> dados = new LinkedHashMap<>();
            dados.put("sincronizacaoIniciada", true);
            dados.put("mensagem", "Sincronização concluída com sucesso (Modo Mock)");
            responderComDados(ctx, 200, dados);
        } catch (Exception e) {
            TratadorGlobalExcecoesApi.responder(ctx, 500, TratadorGlobalExcecoesApi.codePorStatus(500), "Erro ao processar sincronização: " + e.getMessage(), e.getMessage());
        }
    }

    /**
     * Consulta o estado atual da ligação e disponibilidade do servidor central.
     * 
     * @param ctx o contexto do pedido HTTP (Javalin Context)
     */
    public void consultarEstadoSincronizacao(Context ctx) {
        Map<String, Object> dados = new LinkedHashMap<>();
        dados.put("centralDisponivel", this.takiLNLocal.verificarDisponibilidadeCentral());
        dados.put("disponibilidadePercentagem", this.takiLNLocal.obterDisponibilidadeCentral());
        responderComDados(ctx, 200, dados);
    }

    /**
     * Obtém os indicadores-chave de desempenho (KPIs) gerais.
     *
     * @param ctx o contexto do pedido HTTP (Javalin Context)
     */
    public void obterKpiGerais(Context ctx) {
        int idLoja = Integer.parseInt(ctx.queryParamAsClass("idLoja", String.class).getOrDefault("0"));
        Map<String, Object> kpis = this.statisticsDAO.getKpiComparativo(idLoja);
        kpis.put("valorStock", this.statisticsDAO.getValorizacaoEstoque(idLoja));
        responderComDados(ctx, 200, kpis);
    }
    
    /**
     * Retorna o papel do nó atual ("local" ou "central") com base na variável de ambiente APP_MODE.
     * @param ctx o contexto do pedido HTTP (Javalin Context)
     */
    public void getNodeRole(Context ctx) {
        NodeRole role = NodeRoleConfig.resolverRoleRuntime();
        Map<String, String> response = new LinkedHashMap<>();
        response.put("nodeRole", role.name().toLowerCase(Locale.ROOT));
        responderComDados(ctx, 200, response);
    }

    /**
     * Indica se o funcionário possui um perfil de gestão central (ADMIN ou GESTOR_CENTRAL).
     */
    static boolean temPerfilGestaoCentral(Funcionario funcionario) {
        if (funcionario == null) return false;
        String perfil = funcionario.getIdPerfilAcesso();
        if (perfil == null) return false;
        String chave = perfil.trim().toUpperCase(Locale.ROOT);
        return chave.equals("ADMIN") || chave.equals("GESTOR_CENTRAL");
    }
    /**
     * Obtém o volume de vendas agregadas por mês para um determinado período.
     * 
     * @param ctx o contexto do pedido HTTP (Javalin Context)
     */
    public void obterVendasMensais(Context ctx) {
        int idLoja = Integer.parseInt(ctx.queryParamAsClass("idLoja", String.class).getOrDefault("0"));
        int meses = Integer.parseInt(ctx.queryParamAsClass("meses", String.class).getOrDefault("3"));
        responderComDados(ctx, 200, this.takiLNLocal.obterVendasMensais(idLoja, meses));
    }

    /**
     * Obtém a distribuição de vendas por hora do dia.
     * 
     * @param ctx o contexto do pedido HTTP (Javalin Context)
     */
    public void obterVendasPorHora(Context ctx) {
        int idLoja = Integer.parseInt(ctx.queryParamAsClass("idLoja", String.class).getOrDefault("0"));
        responderComDados(ctx, 200, this.takiLNLocal.obterVendasPorHora(idLoja));
    }

    /**
     * Obtém a distribuição do volume de vendas por categoria de produto.
     * 
     * @param ctx o contexto do pedido HTTP (Javalin Context)
     */
    public void obterVendasPorCategoria(Context ctx) {
        int idLoja = Integer.parseInt(ctx.queryParamAsClass("idLoja", String.class).getOrDefault("0"));
        responderComDados(ctx, 200, this.takiLNLocal.obterVendasPorCategoria(idLoja));
    }

    /**
     * Lista todos os produtos que são fornecidos por um determinado fornecedor.
     * 
     * @param ctx o contexto do pedido HTTP (Javalin Context)
     * @throws IllegalArgumentException se o identificador do fornecedor não for fornecido
     */
    public void consultarProdutosDoFornecedor(Context ctx) {
        String idFornecedor = campoObrigatorio(ctx.pathParam("idFornecedor"), "idFornecedor");
        List<Map<String, Object>> produtos = this.takiLNLocal.consultarProdutosDoFornecedor(idFornecedor).stream()
                .map(this::mapearProdutoFornecedor)
                .toList();
        responderComDados(ctx, 200, produtos);
    }

    /**
     * Efetua o mapeamento de um objeto do domínio ProdutoFornecedor para uma estrutura de resposta (mapa).
     *
     * @param pf o objeto de domínio representando a relação produto-fornecedor
     * @return um mapa contendo as propriedades serializáveis do objeto
     */
    private Map<String, Object> mapearProdutoFornecedor(pt.uminho.taki.ln.fornecimentos.ProdutoFornecedor pf) {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("idProduto", pf.getIdProduto());
        res.put("idFornecedor", pf.getIdFornecedor());
        res.put("precoCusto", pf.getPrecoCusto());
        return res;
    }

    /**
     * Gera e disponibiliza para download o ficheiro XML do SAF-T (PT) para um mês e ano específicos.
     * 
     * @param ctx o contexto do pedido HTTP (Javalin Context)
     */
    public void gerarSaft(Context ctx) {
        try {
            int ano = Integer.parseInt(ctx.queryParamAsClass("ano", String.class).getOrDefault(String.valueOf(java.time.LocalDate.now().getYear())));
            int mes = Integer.parseInt(ctx.queryParamAsClass("mes", String.class).getOrDefault(String.valueOf(java.time.LocalDate.now().getMonthValue())));
            
            String xml = this.takiLNLocal.gerarSaftMensal(ano, mes);

            ctx.contentType("application/xml; charset=utf-8")
               .header("Content-Disposition", "attachment; filename=SAFT_" + ano + "_" + mes + ".xml")
               .result(xml);
        } catch (Exception e) {
            TratadorGlobalExcecoesApi.responder(ctx, 500, TratadorGlobalExcecoesApi.codePorStatus(500), "Erro ao gerar SAF-T: " + e.getMessage(), e.getMessage());
        }
    }

    /**
     * Gera e disponibiliza a representação em PDF (segunda via) de uma fatura.
     *
     * @param ctx o contexto do pedido HTTP (Javalin Context)
     */
    public void gerarSegundaViaPDF(Context ctx) {
        try {
            String idFatura = ctx.pathParam("idFatura");
            byte[] pdf = this.takiLNLocal.gerarSegundaViaPDF(idFatura);

            ctx.contentType("application/pdf")
               .header("Content-Disposition", "inline; filename=Fatura_" + idFatura + ".pdf")
               .result(new java.io.ByteArrayInputStream(pdf));
        } catch (Exception e) {
            TratadorGlobalExcecoesApi.responder(ctx, 500, TratadorGlobalExcecoesApi.codePorStatus(500), "Falha ao gerar PDF da fatura.", "Falha ao gerar PDF da fatura.");
        }
    }

    /**
     * Exporta o relatório de vendas em formato PDF para um intervalo de datas.
     * 
     * @param ctx o contexto do pedido
     */
    public void exportarRelatorioVendasPDF(Context ctx) {
        try {
            String inicioStr = ctx.queryParam("startDate");
            String fimStr = ctx.queryParam("endDate");

            List<Venda> vendas = this.takiLNLocal.listarVendas();
            if (inicioStr != null && fimStr != null) {
                LocalDateTime inicio = LocalDate.parse(inicioStr).atStartOfDay();
                LocalDateTime fim = LocalDate.parse(fimStr).atTime(23, 59, 59);
                vendas = vendas.stream()
                        .filter(v -> !v.getDataHora().isBefore(inicio) && !v.getDataHora().isAfter(fim))
                        .toList();
            }

            byte[] pdf = this.relatorioService.gerarRelatorioVendasPDF(vendas, "Relatório de Vendas");

            ctx.contentType("application/pdf")
               .header("Content-Disposition", "attachment; filename=relatorio_vendas.pdf")
               .result(new java.io.ByteArrayInputStream(pdf));
        } catch (Exception e) {
            TratadorGlobalExcecoesApi.responder(ctx, 500, TratadorGlobalExcecoesApi.codePorStatus(500), "Erro ao exportar PDF de vendas.", "Erro ao exportar PDF de vendas.");
        }
    }

    /**
     * Exporta o relatório completo de inventário em formato PDF.
     * 
     * @param ctx o contexto do pedido
     */
    public void exportarRelatorioInventarioPDF(Context ctx) {
        try {
            List<Inventario> itens = this.takiLNLocal.listarInventario();
            List<Produto> prods = this.takiLNLocal.listarProdutos();
            Map<String, Produto> mapaProds = prods.stream().collect(Collectors.toMap(Produto::getIdProduto, p -> p));

            byte[] pdf = this.relatorioService.gerarRelatorioInventarioPDF(itens, mapaProds);

            ctx.contentType("application/pdf")
               .header("Content-Disposition", "attachment; filename=relatorio_inventario.pdf")
               .result(new java.io.ByteArrayInputStream(pdf));
        } catch (Exception e) {
            TratadorGlobalExcecoesApi.responder(ctx, 500, TratadorGlobalExcecoesApi.codePorStatus(500), "Erro ao exportar PDF de inventário.", "Erro ao exportar PDF de inventário.");
        }
    }
    /**
     * Envia uma resposta padrão para funcionalidades que ainda não foram implementadas na lógica de negócio local.
     * 
     * @param ctx o contexto do pedido HTTP (Javalin Context)
     * @param operacao o nome da operação solicitada
     * @param detalhe informações adicionais sobre o estado da funcionalidade
     */
    public void responderNaoImplementado(Context ctx, String operacao, String detalhe) {
        Map<String, Object> detalhes = new LinkedHashMap<>();
        detalhes.put("operacao", operacao);
        detalhes.put("detalhe", detalhe);

        ErroApiResponse erro = new ErroApiResponse(
                "nao_implementado",
                "Operação planeada mas ainda não suportada pela LN local.",
                detalhes,
                Instant.now().toString()
        );
        ctx.status(501).json(erro);
    }

    /**
     * Resolve e valida o identificador do funcionário que deve ser associado a uma nova venda.
     *
     * @param ctx o contexto do pedido HTTP (Javalin Context)
     * @param idFuncionarioPedido o identificador opcional fornecido no corpo do pedido
     * @return o identificador do funcionário resolvido
     * @throws IllegalArgumentException se o identificador não puder ser determinado a partir do pedido ou da sessão
     */
    private String resolverIdFuncionarioVenda(Context ctx, String idFuncionarioPedido) {
        if (idFuncionarioPedido != null && !idFuncionarioPedido.isBlank()) {
            return idFuncionarioPedido.trim();
        }
        return MiddlewareSegurancaApi.obterContexto(ctx)
                .map(ContextoAutenticacao::getSubject)
                .orElseThrow(() -> new IllegalArgumentException("idFuncionario é obrigatório para iniciar venda."));
    }

    /**
     * Resolve e valida o identificador da loja onde a venda está a ser efetuada.
     *
     * @param idLojaPedido o identificador opcional fornecido no pedido
     * @param idFuncionario o identificador do funcionário responsável pela venda
     * @return o identificador da loja resolvido
     * @throws IllegalArgumentException se a loja não puder ser determinada para o funcionário em causa
     */
    private int resolverIdLojaVenda(Integer idLojaPedido, String idFuncionario) {
        if (idLojaPedido != null && idLojaPedido > 0) {
            return idLojaPedido;
        }
        return this.takiLNLocal.listarFuncionarios().stream()
                .filter(f -> idFuncionario.equals(f.getId()))
                .findFirst()
                .map(Funcionario::getIdLoja)
                .filter(id -> id > 0)
                .orElseThrow(() -> new IllegalArgumentException("idLoja é obrigatório para iniciar venda."));
    }

    /**
     * Procura e devolve uma venda que se encontre atualmente em memória (em curso).
     *
     * @param idVenda o identificador único da venda
     * @return o objeto Venda correspondente
     * @throws NotFoundResponse se a venda não for encontrada na memória volátil do controlador
     */
    private Venda obterVendaEmCurso(String idVenda) {
        Venda venda = this.vendasEmCurso.get(idVenda);
        if (venda == null) {
            throw new NotFoundResponse("Venda em curso não encontrada: " + idVenda);
        }
        return venda;
    }

    /**
     * Procura e devolve uma venda que já tenha sido concluída e persistida.
     *
     * @param idVenda o identificador único da venda
     * @return o objeto Venda correspondente
     * @throws NotFoundResponse se a venda não constar no histórico de vendas concluídas
     */
    private Venda encontrarVendaConcluidaPorId(String idVenda) {
        return this.takiLNLocal.listarVendas().stream()
                .filter(v -> idVenda.equals(v.getIdVenda()))
                .findFirst()
                .orElseThrow(() -> new NotFoundResponse("Venda não encontrada: " + idVenda));
    }

    /**
     * Procura um produto no catálogo através do seu identificador.
     *
     * @param idProduto o identificador único do produto
     * @return o objeto Produto correspondente
     * @throws NotFoundResponse se o produto não for encontrado no sistema
     */
    private Produto encontrarProdutoPorId(String idProduto) {
        return this.takiLNLocal.listarProdutos().stream()
                .filter(p -> idProduto.equals(p.getIdProduto()))
                .findFirst()
                .orElseThrow(() -> new NotFoundResponse("Produto não encontrado: " + idProduto));
    }

    /**
     * Constrói a lista de objetos LinhaVenda destinados a uma devolução, validando as quantidades face à venda original.
     *
     * @param vendaOriginal a venda concluída da qual se pretende devolver produtos
     * @param itensPedido a lista de itens e quantidades solicitados para devolução
     * @return a lista de linhas de venda preparadas para o processo de devolução
     * @throws IllegalArgumentException se o pedido for vazio ou se as quantidades excederem o originalmente vendido
     * @throws NotFoundResponse se uma linha de venda especificada não existir na venda original
     */
    private List<LinhaVenda> construirLinhasDevolucao(Venda vendaOriginal, List<LocalApiDtos.ItemDevolucao> itensPedido) {
        if (itensPedido == null || itensPedido.isEmpty()) {
            throw new IllegalArgumentException("É obrigatório indicar as linhas a devolver.");
        }

        Map<String, LinhaVenda> linhasOriginais = vendaOriginal.getLinhas().stream()
                .collect(Collectors.toMap(LinhaVenda::getIdLinhaVenda, linha -> linha));

        List<LinhaVenda> linhasDevolucao = new ArrayList<>();
        for (LocalApiDtos.ItemDevolucao item : itensPedido) {
            String idLinhaVenda = campoObrigatorio(item.getIdLinhaVenda(), "idLinhaVenda");
            LinhaVenda linhaOriginal = linhasOriginais.get(idLinhaVenda);
            if (linhaOriginal == null) {
                throw new NotFoundResponse("Linha de venda não encontrada: " + idLinhaVenda);
            }

            int quantidade = item.getQuantidade() == null ? linhaOriginal.getQuantidade() : item.getQuantidade();
            if (quantidade <= 0 || quantidade > linhaOriginal.getQuantidade()) {
                throw new IllegalArgumentException("Quantidade inválida para a linha " + idLinhaVenda + ".");
            }

            LinhaVenda linhaDevolucao = linhaOriginal.clone();
            linhaDevolucao.setQuantidade(quantidade);
            linhasDevolucao.add(linhaDevolucao);
        }
        return linhasDevolucao;
    }

    /**
     * Gera um token de autenticação JWT assinado por RSA para um funcionário.
     *
     * @param funcionario o funcionário titular do token
     * @return o token JWT serializado em formato JWS
     * @throws RuntimeException se a chave privada RSA não estiver configurada no ambiente
     */
    private String gerarTokenJwt(Funcionario funcionario) {
        String privateKey = System.getenv(VARIAVEL_CHAVE_PRIVADA_JWT);
        if (privateKey == null || privateKey.isBlank()) {
            throw new RuntimeException("Chave Privada JWT não configurada em " + VARIAVEL_CHAVE_PRIVADA_JWT + ".");
        }

        Set<String> roles = resolverRoles(funcionario.getIdPerfilAcesso());
        Set<String> scopes = resolverScopes(roles);
        long exp = Instant.now().plusSeconds(DURACAO_TOKEN_SEGUNDOS).getEpochSecond();

        String payload = construirPayloadJwt(funcionario, roles, scopes, exp);
        return JwtAuthUtils.gerarTokenRSA(payload, privateKey);
    }

    /**
     * Constrói manualmente o corpo JSON do payload para o token JWT.
     *
     * @param funcionario o funcionário cujos dados serão incluídos nas claims
     * @param roles o conjunto de papéis normalizados
     * @param scopes o conjunto de âmbitos (permissões) concedidos
     * @param exp a data/hora de expiração do token (Unix timestamp)
     * @return a representação JSON do payload do token
     */
    private String construirPayloadJwt(Funcionario funcionario, Set<String> roles, Set<String> scopes, long exp) {
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

    /**
     * Resolve e normaliza o conjunto de papéis (roles) para as claims do JWT com base no perfil.
     *
     * @param perfil o nome do perfil de acesso do funcionário
     * @return um conjunto de strings representando os papéis normalizados e expandidos
     */
    private Set<String> resolverRoles(String perfil) {
        Set<String> roles = new LinkedHashSet<>();
        String roleBase = valorOuOmissao(perfil, "OPERADOR").toUpperCase(Locale.ROOT);
        roles.add(roleBase);

        String semAcentos = removerAcentos(roleBase);
        roles.add(semAcentos);

        if (semAcentos.contains("OPERADOR")) {
            roles.add("OPERADOR");
        }
        if (semAcentos.contains("GERENTE")) {
            roles.add("GERENTE");
        }
        if (semAcentos.contains("ADMIN")) {
            roles.add("ADMIN");
        }
        if (semAcentos.contains("PROPRIETARIO")) {
            roles.add("ADMIN");
            roles.add("PROPRIETARIO DA CADEIA");
            roles.add("PROPRIETÁRIO DA CADEIA");
        }

        return roles;
    }

    /**
     * Resolve os âmbitos (scopes) de permissão com base nos papéis normalizados do utilizador.
     *
     * @param roles o conjunto de papéis atribuídos ao utilizador
     * @return o conjunto de âmbitos de acesso (ex: lojas:write, vendas:read) correspondentes
     */
    private Set<String> resolverScopes(Set<String> roles) {
        Set<String> scopes = new LinkedHashSet<>();
        if (roles.contains("ADMIN")) {
            scopes.add("lojas:write");
            scopes.add("produtos:write");
            scopes.add("inventario:write");
            scopes.add("fornecimentos:write");
            scopes.add("vendas:write");
            scopes.add("sync:write");
        }
        if (roles.contains("GERENTE")) {
            scopes.add("produtos:read");
            scopes.add("produtos:write");
            scopes.add("inventario:write");
            scopes.add("fornecimentos:write");
            scopes.add("vendas:read");
            scopes.add("vendas:write");
            scopes.add("sync:read");
        }
        if (roles.contains("OPERADOR")) {
            scopes.add("vendas:read");
            scopes.add("vendas:write");
            scopes.add("promocoes:read");
        }
        if (scopes.isEmpty()) {
            scopes.add("vendas:read");
        }
        return scopes;
    }

    /**
     * Obtém o identificador do funcionário administrador a partir do contexto de segurança da sessão.
     *
     * @param ctx o contexto do pedido HTTP (Javalin Context)
     * @return o identificador do administrador autenticado
     * @throws UnauthorizedResponse se não for possível determinar o administrador a partir da sessão
     */
    private String obterAdministradorSessao(Context ctx) {
        return MiddlewareSegurancaApi.obterContexto(ctx)
                .map(ContextoAutenticacao::getSubject)
                .orElseThrow(() -> new UnauthorizedResponse("Autenticação obrigatória para operação administrativa."));
    }

    /**
     * Devolve o nível hierárquico associado a um perfil de acesso (1=Operador, 4=Proprietário).
     */
    private int nivelHierarquia(String perfil) {
        if (perfil == null) return 0;
        String p = removerAcentos(perfil.trim().toUpperCase(Locale.ROOT));
        if (p.contains("PROPRIET") || p.contains("GESTOR_CENTRAL")) return 4;
        if (p.contains("ADMIN")) return 3;
        if (p.contains("GERENTE") || p.contains("GESTOR_LOJA")) return 2;
        if (p.contains("OPERADOR")) return 1;
        return 0;
    }

    /**
     * Garante que o utilizador autenticado pode modificar o funcionário alvo:
     * - não pode ser ele próprio;
     * - o alvo tem de ter nível hierárquico estritamente inferior.
     */
    private void verificarPodeModificarFuncionario(Context ctx, String idAlvo) {
        String idAdmin = obterAdministradorSessao(ctx);
        if (idAlvo != null && idAlvo.equals(idAdmin)) {
            throw new ForbiddenResponse("Não pode modificar a sua própria conta.");
        }
        Funcionario admin = this.takiLNLocal.buscarFuncionarioPorId(idAdmin)
                .orElseThrow(() -> new UnauthorizedResponse("Utilizador autenticado não encontrado."));
        Funcionario alvo = this.takiLNLocal.buscarFuncionarioPorId(idAlvo)
                .orElseThrow(() -> new NotFoundResponse("Funcionário não encontrado: " + idAlvo));
        int nivelAdmin = nivelHierarquia(admin.getIdPerfilAcesso());
        int nivelAlvo = nivelHierarquia(alvo.getIdPerfilAcesso());
        if (nivelAlvo >= nivelAdmin) {
            throw new ForbiddenResponse("Não tem permissão para modificar contas do mesmo nível ou superior.");
        }
    }

    /**
     * Variante que recebe também o perfil novo a aplicar (para edição/atribuição de perfil),
     * impedindo escalada para nível >= ao do próprio admin.
     */
    private void verificarPodeAtribuirPerfil(Context ctx, String idAlvo, String novoPerfil) {
        verificarPodeModificarFuncionario(ctx, idAlvo);
        if (novoPerfil == null || novoPerfil.isBlank()) return;
        String idAdmin = obterAdministradorSessao(ctx);
        Funcionario admin = this.takiLNLocal.buscarFuncionarioPorId(idAdmin).orElseThrow();
        int nivelAdmin = nivelHierarquia(admin.getIdPerfilAcesso());
        int nivelNovo = nivelHierarquia(novoPerfil);
        if (nivelNovo >= nivelAdmin) {
            throw new ForbiddenResponse("Não pode atribuir um perfil de nível igual ou superior ao seu.");
        }
    }

    /**
     * Converte uma lista de representações textuais de permissões em objetos do domínio Permissao.
     *
     * @param permissoes a lista de strings com os nomes das permissões
     * @return a lista de enums Permissao correspondente
     * @throws IllegalArgumentException se a lista estiver vazia ou contiver permissões desconhecidas
     */
    private List<Permissao> resolverPermissoes(List<String> permissoes) {
        if (permissoes == null || permissoes.isEmpty()) {
            throw new IllegalArgumentException("É obrigatório indicar pelo menos uma permissão.");
        }
        List<Permissao> resultado = new ArrayList<>();
        for (String permissao : permissoes) {
            if (permissao == null || permissao.isBlank()) {
                continue;
            }
            try {
                resultado.add(Permissao.valueOf(permissao.trim().toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Permissão inválida: " + permissao);
            }
        }
        if (resultado.isEmpty()) {
            throw new IllegalArgumentException("É obrigatório indicar pelo menos uma permissão válida.");
        }
        return resultado;
    }

    /**
     * Serializa um conjunto de strings para uma representação de array JSON.
     *
     * @param valores o conjunto de strings a serializar
     * @return a representação textual do array JSON (ex: ["valor1","valor2"])
     */
    private static String serializarArrayJson(Set<String> valores) {
        return valores.stream()
                .map(LocalApiController::escaparJson)
                .map(valor -> "\"" + valor + "\"")
                .collect(Collectors.joining(",", "[", "]"));
    }

    /**
     * Escapa caracteres especiais de uma string para garantir que esta seja válida num campo JSON.
     *
     * @param valor a string original
     * @return a string escapada (com aspas e barras invertidas protegidas)
     */
    private static String escaparJson(String valor) {
        if (valor == null) {
            return "";
        }
        return valor
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    /**
     * Remove acentos e caracteres especiais de normalização de uma string.
     *
     * @param valor o texto original com acentos
     * @return o texto normalizado apenas com caracteres base
     */
    private static String removerAcentos(String valor) {
        String normalizado = Normalizer.normalize(valor, Normalizer.Form.NFD);
        return normalizado.replaceAll("\\p{M}", "");
    }

    /**
     * Valida a presença obrigatória de uma string, devolvendo-a sem espaços em branco nas extremidades.
     *
     * @param valor o valor a validar
     * @param nomeCampo o nome do campo para efeitos de mensagem de erro
     * @return o valor validado e processado
     * @throws IllegalArgumentException se o valor for nulo ou consistir apenas em espaços em branco
     */
    private static String campoObrigatorio(String valor, String nomeCampo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Campo obrigatório em falta: " + nomeCampo + ".");
        }
        return valor.trim();
    }

    /**
     * Valida a presença obrigatória de um valor inteiro e garante que este é estritamente positivo.
     *
     * @param valor o valor a validar
     * @param nomeCampo o nome do campo para efeitos de mensagem de erro
     * @return o valor inteiro validado
     * @throws IllegalArgumentException se o valor for nulo ou inferior ou igual a zero
     */
    private static int inteiroObrigatorioPositivo(Integer valor, String nomeCampo) {
        if (valor == null || valor <= 0) {
            throw new IllegalArgumentException("Campo obrigatório inválido: " + nomeCampo + ".");
        }
        return valor;
    }

    /**
     * Valida a presença obrigatória de um valor decimal e garante que este é estritamente positivo.
     *
     * @param valor o valor a validar
     * @param nomeCampo o nome do campo para efeitos de mensagem de erro
     * @return o valor decimal validado
     * @throws IllegalArgumentException se o valor for nulo ou inferior ou igual a zero
     */
    private static double decimalObrigatorioPositivo(Double valor, String nomeCampo) {
        if (valor == null || valor <= 0) {
            throw new IllegalArgumentException("Campo obrigatório inválido: " + nomeCampo + ".");
        }
        BigDecimal decimal = BigDecimal.valueOf(valor).stripTrailingZeros();
        if (decimal.scale() > 2) {
            throw new IllegalArgumentException("Campo inválido: " + nomeCampo + " deve ter no máximo 2 casas decimais.");
        }
        return valor;
    }

    /**
     * Devolve o valor fornecido ou um valor por omissão caso o original esteja vazio ou nulo.
     *
     * @param valor o valor a processar
     * @param omissao o valor a devolver caso o original seja inválido
     * @return o valor resolvido
     */
    private static String valorOuOmissao(String valor, String omissao) {
        if (valor == null || valor.isBlank()) {
            return omissao;
        }
        return valor.trim();
    }

    /**
     * Resolve a representação textual de uma taxa de IVA para o respetivo objeto do domínio TaxaIva.
     *
     * @param taxaIva o nome da taxa de IVA (ex: NORMAL_23)
     * @return o enum TaxaIva correspondente ou NORMAL_23 por defeito
     */
    private static TaxaIva resolverTaxaIva(String taxaIva) {
        if (taxaIva == null || taxaIva.isBlank()) {
            return TaxaIva.NORMAL_23;
        }
        String normalizado = taxaIva.trim().toUpperCase(Locale.ROOT);
        return TaxaIva.valueOf(normalizado);
    }

    /**
     * Resolve a representação textual de um tipo de movimento para o respetivo objeto TipoMovimento.
     *
     * @param tipo o nome do tipo de movimento (ENTRADA, SAIDA, QUEBRA)
     * @return o enum TipoMovimento correspondente
     * @throws IllegalArgumentException se o tipo for nulo, vazio ou não for reconhecido
     */
    private static TipoMovimento resolverTipoMovimento(String tipo) {
        if (tipo == null || tipo.isBlank()) {
            throw new IllegalArgumentException("Campo obrigatório em falta: tipo.");
        }
        try {
            return TipoMovimento.valueOf(tipo.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Tipo de movimento inválido. Utilize ENTRADA, SAIDA ou QUEBRA.");
        }
    }

    /**
     * Resolve uma representação textual de data e hora para um objeto LocalDateTime, suportando formatos ISO-8601.
     *
     * @param dataRegisto a string contendo a data e hora
     * @return o objeto LocalDateTime correspondente ou o instante atual por defeito
     * @throws IllegalArgumentException se o formato da data for inválido e impossível de interpretar
     */
    private static LocalDateTime resolverDataRegisto(String dataRegisto) {
        if (dataRegisto == null || dataRegisto.isBlank()) {
            return LocalDateTime.now();
        }
        try {
            // Suporta formatos ISO-8601 com offset (ex: .toISOString())
            if (dataRegisto.endsWith("Z") || dataRegisto.contains("+")) {
                return java.time.OffsetDateTime.parse(dataRegisto.trim()).toLocalDateTime();
            }
            return LocalDateTime.parse(dataRegisto.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException("Formato de dataRegisto inválido. Utilize ISO-8601.");
        }
    }

    /**
     * Efetua o mapeamento de um funcionário do domínio para uma estrutura de resposta enriquecida com o nome do perfil.
     *
     * @param funcionario o objeto de domínio do funcionário
     * @return um mapa contendo os dados do funcionário prontos para serialização JSON
     */
    private Map<String, Object> mapearFuncionario(Funcionario funcionario) {
        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("id", funcionario.getId());
        resultado.put("nome", funcionario.getNome());
        resultado.put("email", funcionario.getEmail());
        
        String idPerfil = funcionario.getIdPerfilAcesso();
        resultado.put("idPerfilAcesso", idPerfil);
        
        // Obter nome legível do perfil
        String nomePerfil = this.takiLNLocal.listarPerfis().stream()
                .filter(p -> p.getId().equalsIgnoreCase(idPerfil))
                .map(PerfilAcesso::getNome)
                .findFirst()
                .orElse(idPerfil);
        resultado.put("role", nomePerfil);

        resultado.put("idLoja", funcionario.getIdLoja());
        resultado.put("estadoConta", funcionario.getEstadoConta() != null ? funcionario.getEstadoConta().name() : null);
        return resultado;
    }

    /**
     * Efetua o mapeamento de um produto para uma estrutura de resposta sem informações de inventário.
     *
     * @param produto o objeto de domínio do produto
     * @return um mapa contendo os dados base do produto
     */
    private Map<String, Object> mapearProduto(Produto produto) {
        return mapearProduto(produto, null);
    }

    /**
     * Efetua o mapeamento de um produto e respetivo inventário para uma estrutura de resposta detalhada.
     *
     * @param produto o objeto de domínio do produto
     * @param inventario o objeto de domínio do inventário (pode ser nulo)
     * @return um mapa contendo os dados fundidos de produto e stock
     */
    private Map<String, Object> mapearProduto(Produto produto, Inventario inventario) {
        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("idProduto", produto.getIdProduto());
        resultado.put("codigoBarras", produto.getCodigoBarras());
        resultado.put("nome", produto.getNome());
        resultado.put("descricao", produto.getDescricao());
        resultado.put("precoCusto", produto.getPrecoCusto());
        resultado.put("precoVenda", produto.getPrecoVenda());
        resultado.put("taxaIva", produto.getTaxaIva() != null ? produto.getTaxaIva().name() : null);
        resultado.put("taxaIvaValor", produto.getTaxaIvaValor());
        resultado.put("unidadeMedida", produto.getUnidadeMedida());
        resultado.put("estado", produto.getEstado());

        if (inventario != null) {
            resultado.put("stockAtual", inventario.getQuantidade());
            resultado.put("stockMinimo", inventario.getQuantidadeMinima());
            resultado.put("idInventario", inventario.getId());
        }

        List<String> categorias = this.takiLNLocal.listarCategoriasDeProduto(produto.getIdProduto());
        if (!categorias.isEmpty()) {
            resultado.put("idCategoria", categorias.get(0));
        } else {
            resultado.put("idCategoria", "");
        }

        List<String> fornecedoresIds = this.takiLNLocal.listarFornecedoresDeProduto(produto.getIdProduto());
        if (!fornecedoresIds.isEmpty()) {
            String idFornecedor = fornecedoresIds.get(0);
            resultado.put("supplierId", idFornecedor);
            // Look up supplier name
            String nomeFornecedor = this.takiLNLocal.listarFornecedores().stream()
                    .filter(f -> f.getIdFornecedor().equals(idFornecedor))
                    .map(Fornecedor::getNome)
                    .findFirst()
                    .orElse("Geral");
            resultado.put("supplier", nomeFornecedor);
        } else {
            resultado.put("supplier", "Geral");
            resultado.put("supplierId", "");
        }

        if (inventario != null) {
            resultado.put("stockAtual", inventario.getQuantidade());
            resultado.put("stockMinimo", inventario.getQuantidadeMinima());
            resultado.put("idLojaStock", inventario.getIdLoja());
            resultado.put("idInventario", inventario.getId());
        }
        return resultado;
    }

    /**
     * Efetua o mapeamento de uma categoria para uma estrutura de resposta.
     *
     * @param categoria o objeto de domínio da categoria
     * @return um mapa contendo os dados da categoria
     */
    private Map<String, Object> mapearCategoria(Categoria categoria) {
        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("idCategoria", categoria.getIdCategoria());
        resultado.put("designacao", categoria.getDesignacao());
        resultado.put("idCategoriaPai", categoria.getIdCategoriaPai());
        return resultado;
    }

    /**
     * Efetua o mapeamento de um movimento de inventário para uma estrutura de resposta.
     *
     * @param movimento o objeto de domínio do movimento
     * @return um mapa contendo os dados do movimento
     */
    private Map<String, Object> mapearMovimentoInventario(MovimentoInventario movimento) {
        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("id", movimento.getId());
        resultado.put("tipo", movimento.getTipo() != null ? movimento.getTipo().name() : null);
        resultado.put("quantidade", movimento.getQuantidade());
        resultado.put("dataRegisto", movimento.getDataRegisto() != null ? movimento.getDataRegisto().toString() : null);
        resultado.put("motivo", movimento.getMotivo());
        resultado.put("idInventario", movimento.getIdInventario());
        resultado.put("idFuncionario", movimento.getIdFuncionario());
        return resultado;
    }

    /**
     * Efetua o mapeamento de um registo de inventário para uma estrutura de resposta.
     *
     * @param inventario o objeto de domínio do inventário
     * @return um mapa contendo os dados de stock
     */
    private Map<String, Object> mapearInventario(Inventario inventario) {
        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("idInventario", inventario.getId());
        resultado.put("idProduto", inventario.getIdProduto());
        resultado.put("idLoja", inventario.getIdLoja());
        resultado.put("quantidade", inventario.getQuantidade());
        resultado.put("quantidadeMinima", inventario.getQuantidadeMinima());
        return resultado;
    }

    /**
     * Efetua o mapeamento de um fornecedor para uma estrutura de resposta.
     *
     * @param fornecedor o objeto de domínio do fornecedor
     * @return um mapa contendo os dados do fornecedor
     */
    private Map<String, Object> mapearFornecedor(Fornecedor fornecedor) {
        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("idFornecedor", fornecedor.getIdFornecedor());
        resultado.put("nome", fornecedor.getNome());
        resultado.put("nif", fornecedor.getNif());
        resultado.put("telefone", fornecedor.getTelefone());
        resultado.put("email", fornecedor.getEmail());
        resultado.put("estado", fornecedor.getEstado());
        return resultado;
    }

    /**
     * Efetua o mapeamento de uma encomenda e das suas linhas para uma estrutura de resposta detalhada.
     *
     * @param encomenda o objeto de domínio da encomenda
     * @return um mapa contendo todos os dados da encomenda e respetivas linhas
     */
    private Map<String, Object> mapearEncomenda(Encomenda encomenda) {
        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("idEncomenda", encomenda.getIdEncomenda());
        resultado.put("idFornecedor", encomenda.getIdFornecedor());
        resultado.put("idLoja", encomenda.getIdLoja());
        resultado.put("dataCriacao", encomenda.getDataCriacao() != null ? encomenda.getDataCriacao().toString() : null);
        resultado.put("dataEntrega", encomenda.getDataEntrega() != null ? encomenda.getDataEntrega().toString() : null);
        resultado.put("estado", encomenda.getEstadoAtual() != null ? encomenda.getEstadoAtual().getDesignacao() : null);
        resultado.put("total", encomenda.getValorTotal());
        resultado.put("linhas", encomenda.getLinhas().stream().map(this::mapearLinhaEncomenda).toList());
        return resultado;
    }

    /**
     * Efetua o mapeamento de uma linha de encomenda para uma estrutura de resposta, resolvendo o nome do produto se possível.
     *
     * @param linha o objeto de domínio da linha de encomenda
     * @return um mapa contendo os dados da linha de encomenda
     */
    private Map<String, Object> mapearLinhaEncomenda(LinhaEncomenda linha) {
        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("idLinhaEncomenda", linha.getIdLinhaEncomenda());
        resultado.put("idEncomenda", linha.getIdEncomenda());
        
        String nomeProduto = linha.getIdProduto();
        try {
            Produto p = encontrarProdutoPorId(linha.getIdProduto());
            if (p != null && p.getNome() != null && !p.getNome().isBlank()) {
                nomeProduto = p.getNome();
            }
        } catch (Exception e) {
            // Fallback to ID if product is not found or deleted
        }
        
        resultado.put("idProduto", nomeProduto);
        resultado.put("idProdutoOriginal", linha.getIdProduto());
        resultado.put("quantidade", linha.getQuantidade());
        resultado.put("precoCusto", linha.getPrecoCustoAplicado());
        resultado.put("subTotal", linha.getSubTotal());
        return resultado;
    }

    /**
     * Efetua o mapeamento de uma venda para uma estrutura de resposta detalhada, incluindo fatura e linhas detalhadas.
     *
     * @param venda o objeto de domínio da venda
     * @return um mapa contendo os dados consolidados da venda
     */
    private Map<String, Object> mapearVenda(Venda venda) {
        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("idVenda", venda.getIdVenda());
        resultado.put("numFatura", venda.getFatura() != null ? venda.getFatura().getNumFatura() : "");
        resultado.put("dataHora", venda.getDataHora() != null ? venda.getDataHora().toString() : null);
        resultado.put("subtotal", venda.getSubtotal());
        resultado.put("imposto", venda.getImposto());
        resultado.put("total", venda.getTotal());
        resultado.put("estado", venda.getEstado());
        resultado.put("idLoja", venda.getIdLoja());
        resultado.put("idFuncionario", venda.getIdFuncionario());
        resultado.put("nomeFuncionario", resolverNomeFuncionario(venda.getIdFuncionario()));
        resultado.put("metodoPagamento", this.vendaDAO.getMetodoPagamento(venda.getIdVenda()).orElse(null));
        resultado.put(
                "linhas",
                venda.getLinhas() == null
                        ? List.of()
                        : venda.getLinhas().stream().map(this::mapearLinhaVenda).toList()
        );
        return resultado;
    }

    private String resolverNomeFuncionario(String idFuncionario) {
        if (idFuncionario == null || idFuncionario.isBlank()) {
            return "";
        }
        return this.takiLNLocal.listarFuncionarios().stream()
                .filter(f -> idFuncionario.equals(f.getId()))
                .map(Funcionario::getNome)
                .findFirst()
                .orElse(idFuncionario);
    }

    /**
     * Efetua o mapeamento de uma linha de venda para uma estrutura de resposta, integrando os dados do produto.
     *
     * @param linha o objeto de domínio da linha de venda
     * @return um mapa contendo os dados da linha e do respetivo produto
     */
    private Map<String, Object> mapearLinhaVenda(LinhaVenda linha) {
        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("idLinhaVenda", linha.getIdLinhaVenda());
        resultado.put("quantidade", linha.getQuantidade());
        resultado.put("desconto", linha.getDesconto());
        resultado.put("subtotal", linha.getSubtotal());
        resultado.put("totalImposto", linha.getTotalImposto());
        resultado.put("totalFinal", linha.getTotalFinal());
        resultado.put("produto", linha.getProduto() != null ? mapearProduto(linha.getProduto()) : null);
        return resultado;
    }

    /**
     * Efetua o mapeamento de uma promoção para uma estrutura de resposta.
     *
     * @param promocao o objeto de domínio da promoção
     * @return um mapa contendo os dados da promoção e o seu âmbito (produtos/categorias)
     */
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

    /**
     * Efetua o mapeamento de um perfil de acesso para uma estrutura de resposta.
     *
     * @param perfil o objeto de domínio do perfil
     * @return um mapa contendo os dados do perfil e a lista de permissões associadas
     */
    private Map<String, Object> mapearPerfil(PerfilAcesso perfil) {
        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("id", perfil.getId());
        resultado.put("nome", perfil.getNome());
        resultado.put("permissoes", perfil.getPermissoes().stream().map(Enum::name).toList());
        return resultado;
    }

    /**
     * Envia uma resposta JSON de sucesso padrão para o cliente.
     *
     * @param ctx o contexto do pedido HTTP (Javalin Context)
     * @param status o código de estado HTTP (ex: 200, 201)
     * @param mensagem a mensagem descritiva do sucesso
     */
    private static void responderComSucesso(Context ctx, int status, String mensagem) {
        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("message", mensagem);
        ctx.status(status).json(resposta);
    }



    /**
     * Envia uma resposta JSON contendo um objeto de dados para o cliente.
     *
     * @param ctx o contexto do pedido HTTP (Javalin Context)
     * @param status o código de estado HTTP
     * @param data o objeto de dados a ser enviado sob a chave "data"
     */
    private static void responderComDados(Context ctx, int status, Object data) {
        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("data", data);
        ctx.status(status).json(resposta);
    }
}
