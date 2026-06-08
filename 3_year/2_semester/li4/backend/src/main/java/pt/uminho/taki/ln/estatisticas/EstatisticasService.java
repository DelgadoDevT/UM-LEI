package pt.uminho.taki.ln.estatisticas;

import pt.uminho.taki.dao.InventarioDAO;
import pt.uminho.taki.dao.VendaDAO;
import pt.uminho.taki.ln.inventario.Inventario;
import pt.uminho.taki.ln.vendas.Venda;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementação do serviço de Estatísticas e KPIs (RF16, RF17, RF18).
 * Opera sobre os DAOs de Vendas e Inventário locais.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class EstatisticasService implements IEstatisticasService {

    private final VendaDAO vendaDAO;
    private final InventarioDAO inventarioDAO;
    private final pt.uminho.taki.dao.ProdutoDAO produtoDAO;

    /**
     * Construtor para o EstatisticasService.
     *
     * @param vendaDAO o DAO de vendas
     * @param inventarioDAO o DAO de inventário
     * @param produtoDAO o DAO de produtos
     */
    public EstatisticasService(VendaDAO vendaDAO, InventarioDAO inventarioDAO, pt.uminho.taki.dao.ProdutoDAO produtoDAO) {
        this.vendaDAO = vendaDAO;
        this.inventarioDAO = inventarioDAO;
        this.produtoDAO = produtoDAO;
    }

    // -------------------------------------------------------------------------
    // RF16 — Relatório de Vendas
    // -------------------------------------------------------------------------

    /**
     * Calcula o volume total de vendas num determinado intervalo temporal.
     *
     * @param inicio a data de início do intervalo
     * @param fim a data de fim do intervalo
     * @return o volume total de vendas (subtotal)
     * @throws DatasInvalidasException se o intervalo de datas for inválido
     */
    @Override
    public double calcularVolumeVendas(LocalDateTime inicio, LocalDateTime fim) throws DatasInvalidasException {
        validarIntervalo(inicio, fim);
        return filtrarVendasPorPeriodo(inicio, fim).stream()
                .mapToDouble(Venda::getSubtotal)
                .sum();
    }

    /**
     * Gera um relatório detalhado de vendas com base em vários filtros.
     *
     * @param inicio a data de início do intervalo
     * @param fim a data de fim do intervalo
     * @param idLoja o identificador da loja (opcional)
     * @param categoria a categoria de produtos (opcional)
     * @return um DTO contendo o relatório de vendas
     * @throws DatasInvalidasException se o intervalo de datas for inválido
     */
    @Override
    public RelatorioVendasDTO gerarRelatorioVendas(
            LocalDateTime inicio, LocalDateTime fim, Integer idLoja, String categoria)
            throws DatasInvalidasException {

        validarIntervalo(inicio, fim);

        List<Venda> vendas = filtrarVendasPorPeriodo(inicio, fim);

        if (idLoja != null) {
            vendas = vendas.stream()
                    .filter(v -> v.getIdLoja() == idLoja)
                    .collect(Collectors.toList());
        }

        boolean aplicarFiltroCategoria = categoria != null && !categoria.isBlank();
        double volumeTotal = 0.0;
        int quantidadeArtigos = 0;
        int vendasComLinhasValidas = 0;

        for (Venda venda : vendas) {
            if (venda.getLinhas() == null || venda.getLinhas().isEmpty()) {
                continue;
            }
            double subtotalVendaFiltrada = 0.0;
            int qtdVendaFiltrada = 0;
            for (pt.uminho.taki.ln.vendas.LinhaVenda linha : venda.getLinhas()) {
                if (linha.getProduto() == null) {
                    continue;
                }
                if (aplicarFiltroCategoria) {
                    java.util.Set<String> categoriasProduto = produtoDAO.getCategorias(linha.getProduto().getIdProduto());
                    if (!categoriasProduto.contains(categoria)) {
                        continue;
                    }
                }
                subtotalVendaFiltrada += linha.getSubtotal();
                qtdVendaFiltrada += linha.getQuantidade();
            }
            if (subtotalVendaFiltrada > 0 || qtdVendaFiltrada > 0) {
                vendasComLinhasValidas++;
            }
            volumeTotal += subtotalVendaFiltrada;
            quantidadeArtigos += qtdVendaFiltrada;
        }

        double ticketMedio = vendasComLinhasValidas == 0 ? 0.0 : volumeTotal / vendasComLinhasValidas;

        return new RelatorioVendasDTO(volumeTotal, quantidadeArtigos, ticketMedio);
    }

    // -------------------------------------------------------------------------
    // RF17 — Relatório de Inventário
    // -------------------------------------------------------------------------

    /**
     * Gera um relatório do estado atual do inventário para uma loja específica.
     *
     * @param idLoja o identificador da loja (opcional)
     * @return um DTO contendo o relatório de inventário
     */
    @Override
    public RelatorioInventarioDTO gerarRelatorioInventario(Integer idLoja) {
        Collection<Inventario> todos = inventarioDAO.findAll();

        List<Inventario> inventarios = todos.stream()
                .filter(i -> idLoja == null || i.getIdLoja() == idLoja)
                .collect(Collectors.toList());

        // Valorização: quantidade * preço de custo. O preço de custo é guardado
        // no próprio inventário; o DAO devolve tudo o que está disponível localmente.
        double valorizacao = inventarios.stream()
                .mapToDouble(i -> {
                    return produtoDAO.findById(i.getIdProduto())
                            .map(p -> i.getQuantidade() * p.getPrecoCusto())
                            .orElse(0.0);
                })
                .sum();

        List<String> emRutura = inventarios.stream()
                .filter(i -> i.getQuantidade() <= i.getQuantidadeMinima())
                .map(Inventario::getIdProduto)
                .collect(Collectors.toList());

        return new RelatorioInventarioDTO(valorizacao, emRutura);
    }

    // -------------------------------------------------------------------------
    // RF18 — Dashboard de KPIs
    // -------------------------------------------------------------------------

    /**
     * Calcula o valor do ticket médio das vendas num determinado intervalo temporal.
     *
     * @param inicio a data de início do intervalo
     * @param fim a data de fim do intervalo
     * @return o valor do ticket médio
     * @throws DatasInvalidasException se o intervalo de datas for inválido
     */
    @Override
    public double calcularTicketMedio(LocalDateTime inicio, LocalDateTime fim) throws DatasInvalidasException {
        validarIntervalo(inicio, fim);
        List<Venda> vendas = filtrarVendasPorPeriodo(inicio, fim);
        if (vendas.isEmpty()) return 0.0;
        double total = vendas.stream().mapToDouble(Venda::getSubtotal).sum();
        return total / vendas.size();
    }

    /**
     * Gera o conjunto de KPIs para o dashboard de uma loja específica.
     *
     * @param idLoja o identificador da loja (opcional)
     * @return um DTO contendo os KPIs do dashboard
     */
    @Override
    public DashboardKPIsDTO gerarDashboardKPIs(Integer idLoja) {
        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        LocalDateTime fimDia = inicioDia.plusDays(1).minusNanos(1);

        List<Venda> vendasDia = filtrarVendasPorPeriodo(inicioDia, fimDia);
        if (idLoja != null) {
            vendasDia = vendasDia.stream()
                    .filter(v -> v.getIdLoja() == idLoja)
                    .collect(Collectors.toList());
        }

        double volumeDia = vendasDia.stream().mapToDouble(Venda::getSubtotal).sum();
        double ticketMedio = vendasDia.isEmpty() ? 0.0 : volumeDia / vendasDia.size();

        RelatorioInventarioDTO relInv = gerarRelatorioInventario(idLoja);
        int emRutura = relInv.getProdutosEmRutura().size();

        return new DashboardKPIsDTO(volumeDia, volumeDia, emRutura, ticketMedio);
    }

    // -------------------------------------------------------------------------
    // Auxiliares internos
    // -------------------------------------------------------------------------

    /**
     * Valida se um intervalo temporal é coerente (início antes do fim).
     *
     * @param inicio a data de início
     * @param fim a data de fim
     * @throws DatasInvalidasException se o intervalo for nulo ou incoerente
     */
    private void validarIntervalo(LocalDateTime inicio, LocalDateTime fim) throws DatasInvalidasException {
        if (inicio == null || fim == null || inicio.isAfter(fim)) {
            throw new DatasInvalidasException(
                    "O intervalo temporal é inválido: a data de início não pode ser posterior à data de fim.");
        }
    }

    /**
     * Filtra a lista global de vendas para um determinado período.
     *
     * @param inicio a data de início
     * @param fim a data de fim
     * @return a lista de vendas filtrada
     */
    private List<Venda> filtrarVendasPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return vendaDAO.findAll().stream()
                .filter(v -> v.getDataHora() != null
                        && !v.getDataHora().isBefore(inicio)
                        && !v.getDataHora().isAfter(fim))
                .collect(Collectors.toList());
    }
}
