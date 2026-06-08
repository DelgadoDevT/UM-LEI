package pt.uminho.taki.ln.inventario;

import pt.uminho.taki.dao.InventarioDAO;
import pt.uminho.taki.ln.inventario.exceptions.ArtigoNaoEncontradoException;
import pt.uminho.taki.ln.inventario.exceptions.StockInsuficienteException;
import pt.uminho.taki.ln.vendas.Venda;
import pt.uminho.taki.ln.vendas.LinhaVenda;
import pt.uminho.taki.ln.vendas.IVendaObserver;
import pt.uminho.taki.ln.vendas.IDevolucaoObserver;
import pt.uminho.taki.ln.vendas.Devolucao;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Implementação do serviço de gestão de inventário e stock local.
 * Este serviço é responsável por orquestrar os movimentos de stock, definir limites
 * de segurança e exportar dados de inventário para formatos de intercâmbio.
 * 
 * @author TakiLN Team
 * @since 1.0
 */
public class InventarioService implements IInventarioService, IVendaObserver, IDevolucaoObserver {
    /** Objeto de acesso a dados para persistência e consulta do estado do inventário. */
    private final InventarioDAO inventarioDAO;

    /**
     * Construtor para injeção de dependências.
     * @param inventarioDAO o DAO para estado do stock
     */
    public InventarioService(InventarioDAO inventarioDAO) {
        this.inventarioDAO = inventarioDAO;
    }

    @Override
    public void registarMovimentoManual(MovimentoInventario movimento) throws StockInsuficienteException, ArtigoNaoEncontradoException {
        // Consulta o estado atual do inventario
        Optional<Inventario> inventarioOpt = this.inventarioDAO.findById(movimento.getIdInventario());
        
        if (inventarioOpt.isEmpty()) {
            throw new ArtigoNaoEncontradoException("Registo de inventario para o produto " + movimento.getIdInventario() + " nao foi encontrado.");
        }

        Inventario inventario = inventarioOpt.get();
        double quantidadeAnterior = inventario.getQuantidade();
        double novaQuantidade = quantidadeAnterior;

        // Regras de negocio para movimentos manuais
        if (movimento.getTipo() == TipoMovimento.ENTRADA) {
            novaQuantidade += movimento.getQuantidade();
        } else {
            // Saida ou Quebra: Verifica stock disponivel
            if (inventario.getQuantidade() < movimento.getQuantidade()) {
                throw new StockInsuficienteException("Stock insuficiente para o movimento de " + movimento.getTipo().name() + 
                                                   " (Atual: " + inventario.getQuantidade() + ", Requerido: " + movimento.getQuantidade() + ").");
            }
            novaQuantidade -= movimento.getQuantidade();
        }

        // Atualiza o inventario
        inventario.setQuantidade(novaQuantidade);
        this.inventarioDAO.save(inventario.getId(), inventario);

        if (movimento.getDataRegisto() == null) {
            movimento.setDataRegisto(LocalDateTime.now());
        }
        this.inventarioDAO.addMovimento(inventario.getIdProduto(), movimento);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void definirLimiteSeguranca(String idInventario, double novoLimite) throws ArtigoNaoEncontradoException {
        Optional<Inventario> inventarioOpt = this.inventarioDAO.findById(idInventario);

        if (inventarioOpt.isEmpty()) {
            throw new ArtigoNaoEncontradoException("Inventario com ID " + idInventario + " nao foi encontrado no sistema.");
        }

        Inventario inventario = inventarioOpt.get();
        
        // Persiste as alteracoes diretamente sem calculos, conforme pedido pelo utilizador
        inventario.setQuantidadeMinima(novoLimite);
        this.inventarioDAO.save(inventario.getId(), inventario);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onVendaConcluida(Venda venda) {
        // O stock é abatido automaticamente pelo trigger trg_venda_abater_stock no Postgres
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDevolucaoConcluida(Devolucao devolucao, java.util.List<LinhaVenda> linhasDevolvidas) {
        // O stock é reposto automaticamente pelo trigger trg_devolucao_repor_stock no Postgres
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processarEntradaEncomenda(int idLoja, String idProduto, double quantidade) {
        Optional<Inventario> invOpt = this.inventarioDAO.findAll().stream()
                .filter(i -> i.getIdLoja() == idLoja && i.getIdProduto().equals(idProduto))
                .findFirst();

        if (invOpt.isPresent()) {
            Inventario inv = invOpt.get();
            inv.setQuantidade(inv.getQuantidade() + quantidade);
            this.inventarioDAO.save(inv.getId(), inv);

            MovimentoInventario m = new MovimentoInventario();
            m.setId(java.util.UUID.randomUUID().toString());
            m.setTipo(TipoMovimento.ENTRADA);
            m.setQuantidade(quantidade);
            m.setDataRegisto(LocalDateTime.now());
            m.setMotivo("Entrada por Encomenda");
            m.setIdInventario(inv.getId());
            // idFuncionario fica null quando o movimento é gerado pelo sistema (FK nula é permitida)
            m.setIdFuncionario(null);
            this.inventarioDAO.addMovimento(idProduto, m);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String exportarStockCsv() {
        List<Inventario> registos = carregarInventarioOrdenado();
        StringBuilder sb = new StringBuilder("id_inventario,id_loja,id_produto,quantidade,quantidade_minima\n");
        for (Inventario inv : registos) {
            sb.append(inv.getId()).append(',')
              .append(inv.getIdLoja()).append(',')
              .append(inv.getIdProduto()).append(',')
              .append(inv.getQuantidade()).append(',')
              .append(inv.getQuantidadeMinima()).append('\n');
        }
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String exportarStockJson() {
        List<Inventario> registos = carregarInventarioOrdenado();
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < registos.size(); i++) {
            Inventario inv = registos.get(i);
            if (i > 0) sb.append(',');
            sb.append('{')
              .append("\"idInventario\":\"").append(escapeJson(inv.getId())).append("\",")
              .append("\"idLoja\":").append(inv.getIdLoja()).append(',')
              .append("\"idProduto\":\"").append(escapeJson(inv.getIdProduto())).append("\",")
              .append("\"quantidade\":").append(inv.getQuantidade()).append(',')
              .append("\"quantidadeMinima\":").append(inv.getQuantidadeMinima())
              .append('}');
        }
        sb.append(']');
        return sb.toString();
    }

    /**
     * Carrega o inventário da base de dados ordenado por loja e produto.
     * 
     * @return lista ordenada de inventário
     */
    private List<Inventario> carregarInventarioOrdenado() {
        List<Inventario> registos = new ArrayList<>(this.inventarioDAO.findAll());
        registos.sort(Comparator
                .comparingInt(Inventario::getIdLoja)
                .thenComparing(Inventario::getIdProduto));
        return registos;
    }

    /**
     * Escapa caracteres especiais para formato JSON.
     * 
     * @param valor o valor a escapar
     * @return o valor escapado
     */
    private String escapeJson(String valor) {
        if (valor == null) return "";
        return valor.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
