package pt.uminho.taki.api.shared.erros;

import io.javalin.Javalin;
import io.javalin.http.ConflictResponse;
import io.javalin.http.Context;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.HttpResponseException;
import io.javalin.http.NotFoundResponse;
import io.javalin.http.UnauthorizedResponse;
import io.javalin.http.UnprocessableContentResponse;
import pt.uminho.taki.api.shared.seguranca.TokenJwtInvalidoException;
import pt.uminho.taki.ln.fornecimentos.exceptions.CamposObrigatoriosEmFaltaException;
import pt.uminho.taki.ln.fornecimentos.exceptions.FornecedorExistenteException;
import pt.uminho.taki.ln.inventario.exceptions.ArtigoNaoEncontradoException;
import pt.uminho.taki.ln.inventario.exceptions.DataInvalidaException;
import pt.uminho.taki.ln.inventario.exceptions.FuncionarioInexistenteException;
import pt.uminho.taki.ln.inventario.exceptions.LojaInexistenteException;
import pt.uminho.taki.ln.inventario.exceptions.MotivoObrigatorioException;
import pt.uminho.taki.ln.inventario.exceptions.QuantidadeInvalidaException;
import pt.uminho.taki.ln.inventario.exceptions.StockInsuficienteException;
import pt.uminho.taki.ln.lojas.exceptions.ContaBloqueadaException;
import pt.uminho.taki.ln.lojas.exceptions.CredenciaisInvalidasException;
import pt.uminho.taki.ln.lojas.exceptions.EmailJaExisteException;
import pt.uminho.taki.ln.lojas.exceptions.FuncionarioNaoEncontradoException;
import pt.uminho.taki.ln.lojas.exceptions.PasswordFracaException;
import pt.uminho.taki.ln.lojas.exceptions.PerfilDuplicadoException;
import pt.uminho.taki.ln.lojas.exceptions.ProdutoExistenteException;
import pt.uminho.taki.ln.lojas.exceptions.ProdutoInexistenteException;
import pt.uminho.taki.ln.lojas.exceptions.CategoriaInvalidaException;
import pt.uminho.taki.ln.fornecimentos.exceptions.FornecedorInativoException;
import pt.uminho.taki.ln.vendas.MetodoPagamentoIndisponivelException;
import pt.uminho.taki.ln.vendas.PrazoDevolucaoExcedidoException;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Tratador global de exceções da API.
 * 
 * @author TakiLN Team
 * @since 1.0
 */
public final class TratadorGlobalExcecoesApi {

    /**
     * Construtor privado para evitar a instanciação de uma classe utilitária.
     */
    private TratadorGlobalExcecoesApi() {
    }

    /**
     * Regista o tratador global de exceções na aplicação Javalin.
     * 
     * @param app a aplicação Javalin
     */
    public static void registar(Javalin app) {
        Objects.requireNonNull(app, "app");
        app.exception(Exception.class, TratadorGlobalExcecoesApi::tratarExcecao);
        app.error(404, ctx -> responder(ctx, 404, "recurso_nao_encontrado", "Recurso não encontrado.", "Endpoint não encontrado."));
    }

    /**
     * Efetua o tratamento centralizado de uma exceção.
     * 
     * @param excecao a exceção capturada
     * @param ctx o contexto do Javalin
     */
    private static void tratarExcecao(Throwable excecao, Context ctx) {
        int status = resolverStatus(excecao);
        String code = codePorStatus(status);
        Object details = extrairDetalhes(excecao);
        // Para erros de negócio (status < 500) com mensagem específica, propaga-a;
        // para 500 mantém-se a genérica para não expor erros internos.
        String message = (status < 500 && excecao.getMessage() != null && !excecao.getMessage().isBlank())
                ? excecao.getMessage()
                : messagePorStatus(status);
        responder(ctx, status, code, message, details);
    }

    /**
     * Resolve o código de estado HTTP adequado para uma determinada exceção.
     * 
     * @param excecao a exceção a analisar
     * @return o código de estado HTTP correspondente
     */
    private static int resolverStatus(Throwable excecao) {
        if (excecao instanceof UnauthorizedResponse
                || excecao instanceof CredenciaisInvalidasException
                || excecao instanceof TokenJwtInvalidoException) {
            return 401;
        }
        if (excecao instanceof ForbiddenResponse
                || excecao instanceof ContaBloqueadaException
                || excecao instanceof SecurityException) {
            return 403;
        }
        if (excecao instanceof NotFoundResponse
                || excecao instanceof NoSuchElementException
                || excecao instanceof ProdutoInexistenteException
                || excecao instanceof FuncionarioNaoEncontradoException
                || excecao instanceof ArtigoNaoEncontradoException
                || excecao instanceof LojaInexistenteException
                || excecao instanceof FuncionarioInexistenteException) {
            return 404;
        }
        if (excecao instanceof ConflictResponse
                || excecao instanceof PerfilDuplicadoException
                || excecao instanceof EmailJaExisteException
                || excecao instanceof ProdutoExistenteException
                || excecao instanceof FornecedorExistenteException
                || excecao instanceof FornecedorInativoException
                || excecao instanceof MetodoPagamentoIndisponivelException
                || excecao instanceof PrazoDevolucaoExcedidoException
                || excecao instanceof StockInsuficienteException
                || excecao instanceof IllegalStateException) {
            return 409;
        }
        if (excecao instanceof UnprocessableContentResponse
                || excecao instanceof IllegalArgumentException
                || excecao instanceof CamposObrigatoriosEmFaltaException
                || excecao instanceof QuantidadeInvalidaException
                || excecao instanceof MotivoObrigatorioException
                || excecao instanceof DataInvalidaException
                || excecao instanceof PasswordFracaException
                || excecao instanceof CategoriaInvalidaException) {
            return 422;
        }
        if (excecao instanceof HttpResponseException httpResponseException) {
            return switch (httpResponseException.getStatus()) {
                case 400, 401, 403, 404, 409, 422 -> httpResponseException.getStatus();
                default -> 500;
            };
        }
        return 500;
    }

    /**
     * Extrai detalhes relevantes de uma exceção para inclusão na resposta.
     * 
     * @param excecao a exceção a analisar
     * @return um objeto com os detalhes da exceção
     */
    private static Object extrairDetalhes(Throwable excecao) {
        if (excecao instanceof HttpResponseException httpResponseException
                && httpResponseException.getDetails() != null
                && !httpResponseException.getDetails().isEmpty()) {
            return httpResponseException.getDetails();
        }
        if (excecao.getMessage() != null && !excecao.getMessage().isBlank()) {
            return excecao.getMessage();
        }
        Throwable causa = excecao.getCause();
        if (causa != null && causa.getMessage() != null && !causa.getMessage().isBlank()) {
            return causa.getMessage();
        }
        return excecao.getClass().getSimpleName();
    }

    /**
     * Retorna um identificador textual curto baseado no estado HTTP.
     * 
     * @param status o código de estado HTTP
     * @return o código de erro em formato string
     */
    public static String codePorStatus(int status) {
        return switch (status) {
            case 400 -> "pedido_invalido";
            case 401 -> "nao_autorizado";
            case 403 -> "acesso_proibido";
            case 404 -> "recurso_nao_encontrado";
            case 409 -> "conflito";
            case 422 -> "entidade_nao_processavel";
            default -> "erro_interno";
        };
    }

    /**
     * Retorna uma mensagem amigável para o utilizador baseada no estado HTTP.
     * 
     * @param status o código de estado HTTP
     * @return a mensagem de erro descritiva
     */
    private static String messagePorStatus(int status) {
        return switch (status) {
            case 400 -> "O pedido contém dados inválidos ou mal formatados.";
            case 401 -> "Autenticação inválida ou em falta.";
            case 403 -> "Sem permissões para executar esta operação.";
            case 404 -> "O recurso pedido não existe.";
            case 409 -> "A operação entra em conflito com o estado atual.";
            case 422 -> "O pedido contém dados inválidos.";
            default -> "Erro interno ao processar o pedido.";
        };
    }

    /**
     * Constrói e envia a resposta de erro padronizada.
     * 
     * @param ctx o contexto do Javalin
     * @param status o código de estado HTTP
     * @param code o identificador interno do erro
     * @param message a mensagem descritiva
     * @param details os detalhes técnicos ou contextuais
     */
    public static void responder(Context ctx, int status, String code, String message, Object details) {
        ErroApiResponse erro = new ErroApiResponse(code, message, details, Instant.now().toString());
        ctx.status(status).json(erro);
    }
}
