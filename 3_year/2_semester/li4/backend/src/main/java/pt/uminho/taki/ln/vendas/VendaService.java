package pt.uminho.taki.ln.vendas;

import pt.uminho.taki.dao.VendaDAO;
import pt.uminho.taki.ln.lojas.Produto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implementação do serviço para gestão de operações de venda.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class VendaService implements IVendaService {

    private static final long LIMITE_TRANSACAO_MS = 10_000;
    private static final String METODO_NUMERARIO = "Numerário";
    private static final String METODO_CARTAO = "Cartão";
    private static final String METODO_MBWAY = "MBWay";
    private final IPromocaoService promocaoService;
    private final VendaDAO vendaDAO;
    private final ITpaGateway tpaGateway;
    private final List<IVendaObserver> observers;

    /**
     * Constrói uma nova instância de VendaService com o serviço de promoções e o DAO de vendas fornecidos.
     * Utiliza um gateway TPA por omissão.
     *
     * @param promocaoService o serviço de promoções
     * @param vendaDAO        o DAO de vendas
     */
    public VendaService(IPromocaoService promocaoService, VendaDAO vendaDAO) {
        this(promocaoService, vendaDAO, new DefaultTpaGateway());
    }

    /**
     * Constrói uma nova instância de VendaService com o serviço de promoções, o DAO de vendas e o gateway TPA fornecidos.
     *
     * @param promocaoService o serviço de promoções
     * @param vendaDAO        o DAO de vendas
     * @param tpaGateway      o gateway TPA
     */
    public VendaService(IPromocaoService promocaoService, VendaDAO vendaDAO, ITpaGateway tpaGateway) {
        this.promocaoService = promocaoService;
        this.vendaDAO = vendaDAO;
        this.tpaGateway = tpaGateway;
        this.observers = new ArrayList<>();
    }

    /**
     * Adiciona um observador de vendas.
     *
     * @param observer o observador a adicionar
     */
    public void adicionarObserver(IVendaObserver observer) {
        if (observer != null && !this.observers.contains(observer)) {
            this.observers.add(observer);
        }
    }

    /**
     * Remove um observador de vendas.
     *
     * @param observer o observador a remover
     */
    public void removerObserver(IVendaObserver observer) {
        this.observers.remove(observer);
    }

    /**
     * Inicia uma nova venda.
     *
     * @param idLoja        o identificador da loja
     * @param idFuncionario o identificador do funcionário
     * @return a venda recém-criada
     */
    @Override
    public Venda iniciarVenda(int idLoja, String idFuncionario) {
        Venda venda = new Venda();
        venda.setIdVenda(UUID.randomUUID().toString());
        venda.setIdLoja(idLoja);
        venda.setIdFuncionario(idFuncionario);
        venda.setEstado("Pendente");
        venda.setDataHora(LocalDateTime.now());
        return venda;
    }

    /**
     * Adiciona um item de linha a uma venda.
     *
     * @param venda      a venda à qual adicionar a linha
     * @param produto    o produto a ser vendido
     * @param quantidade a quantidade do produto
     */
    @Override
    public void adicionarLinha(Venda venda, Produto produto, int quantidade) {
        if (venda == null || produto == null || quantidade <= 0) return;
        
        double desconto = promocaoService.calcularMelhorDesconto(produto);
        LinhaVenda linha = new LinhaVenda(produto, quantidade, desconto);
        venda.adicionarLinhaVenda(linha);
    }

    /**
     * Processa uma venda com um determinado método de pagamento.
     *
     * @param venda           a venda a processar
     * @param metodoPagamento o método de pagamento utilizado
     * @throws MetodoPagamentoIndisponivelException se o método de pagamento não se encontrar disponível ou for inválido.
     */
    @Override
    public void processarVenda(Venda venda, String metodoPagamento) throws MetodoPagamentoIndisponivelException {
        processarVenda(venda, metodoPagamento, venda != null ? venda.getTotal() : 0.0);
    }

    /**
     * Processa uma venda com um determinado método de pagamento e um valor entregue pelo cliente.
     *
     * @param venda           a venda a processar
     * @param metodoPagamento o método de pagamento utilizado
     * @param valorEntregue   o valor entregue pelo cliente
     * @throws MetodoPagamentoIndisponivelException se o método de pagamento não se encontrar disponível ou for inválido.
     */
    @Override
    public void processarVenda(Venda venda, String metodoPagamento, double valorEntregue) throws MetodoPagamentoIndisponivelException {
        long inicioProcessamento = System.currentTimeMillis();
        if (venda == null) {
            throw new MetodoPagamentoIndisponivelException("Venda inválida.");
        }
        if (venda.getLinhas() == null || venda.getLinhas().isEmpty()) {
            throw new MetodoPagamentoIndisponivelException("A venda não contém artigos.");
        }

        String metodoNormalizado;
        MetodoPagamentoTipo tipoMetodo = normalizarMetodoPagamento(metodoPagamento);
        double totalArredondado = arredondarMoeda(venda.getTotal());
        double valorEntregueArredondado = arredondarMoeda(valorEntregue);
        if (tipoMetodo == MetodoPagamentoTipo.NUMERARIO) {
            metodoNormalizado = METODO_NUMERARIO;
            if (valorEntregueArredondado < totalArredondado) {
                throw new MetodoPagamentoIndisponivelException("Valor entregue insuficiente para pagamento em numerário.");
            }
            valorEntregue = valorEntregueArredondado;
        } else if (tipoMetodo == MetodoPagamentoTipo.CARTAO) {
            metodoNormalizado = METODO_CARTAO;
            if (tpaGateway == null || !tpaGateway.autorizarPagamentoCartao(venda.getIdVenda(), totalArredondado)) {
                throw new MetodoPagamentoIndisponivelException("Pagamento por cartão não autorizado pelo TPA.");
            }
            valorEntregue = totalArredondado;
        } else {
            // MBWay — fluxo simulado para efeitos de demonstração: o pedido considera-se
            // confirmado pela aplicação MBWay, sem troco e com valor exactamente igual ao total.
            metodoNormalizado = METODO_MBWAY;
            valorEntregue = totalArredondado;
        }

        double troco = METODO_NUMERARIO.equals(metodoNormalizado)
                ? arredondarMoeda(valorEntregue - totalArredondado)
                : 0.0;

        if (venda.getLinhas() != null) {
            for (IVendaObserver observer : observers) {
                observer.onVendaConcluida(venda);
            }
        }
        
        venda.setEstado("Concluída");
        if (vendaDAO != null) {
            vendaDAO.saveComPagamento(venda.getIdVenda(), venda, metodoNormalizado, valorEntregue, troco);
        }

        long duracao = System.currentTimeMillis() - inicioProcessamento;
        if (duracao > LIMITE_TRANSACAO_MS) {
            throw new MetodoPagamentoIndisponivelException("Tempo máximo de processamento da venda excedido (10 segundos).");
        }
    }

    /**
     * Normaliza a cadeia de caracteres do método de pagamento.
     *
     * @param metodoPagamento a cadeia de caracteres do método de pagamento
     * @return o tipo de método de pagamento normalizado
     * @throws MetodoPagamentoIndisponivelException se o método de pagamento não se encontrar disponível ou for inválido.
     */
    private MetodoPagamentoTipo normalizarMetodoPagamento(String metodoPagamento) throws MetodoPagamentoIndisponivelException {
        if (metodoPagamento == null || metodoPagamento.trim().isEmpty()) {
            throw new MetodoPagamentoIndisponivelException("Método de pagamento indisponível ou inválido.");
        }

        String metodo = metodoPagamento.trim();
        if ("Numerário".equalsIgnoreCase(metodo)
                || "Numerario".equalsIgnoreCase(metodo)
                || "Dinheiro".equalsIgnoreCase(metodo)) {
            return MetodoPagamentoTipo.NUMERARIO;
        }
        if ("Cartão".equalsIgnoreCase(metodo)
                || "Cartao".equalsIgnoreCase(metodo)
                || "Multibanco".equalsIgnoreCase(metodo)) {
            return MetodoPagamentoTipo.CARTAO;
        }
        if ("MBWay".equalsIgnoreCase(metodo)
                || "MB Way".equalsIgnoreCase(metodo)
                || "mbway".equalsIgnoreCase(metodo)) {
            return MetodoPagamentoTipo.MBWAY;
        }
        throw new MetodoPagamentoIndisponivelException("Método de pagamento não suportado. Utilize Dinheiro, Cartão, Multibanco ou MBWay.");
    }

    /**
     * Enumeração para os tipos de métodos de pagamento.
     *
     * @author TakiLN Team
     * @since 1.0
     */
    private enum MetodoPagamentoTipo {
        /** Pagamento em numerário. */
        NUMERARIO,
        /** Pagamento por cartão. */
        CARTAO,
        /** Pagamento via MBWay. */
        MBWAY
    }

    private static double arredondarMoeda(double valor) {
        return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * Lista todas as vendas.
     *
     * @return uma lista de vendas
     */
    @Override
    public List<Venda> listarVendas() {
        if (vendaDAO == null) return new ArrayList<>();
        return new ArrayList<>(this.vendaDAO.findAll());
    }
}
