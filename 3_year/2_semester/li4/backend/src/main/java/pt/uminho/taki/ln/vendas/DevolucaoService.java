package pt.uminho.taki.ln.vendas;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

/**
 * Serviço para a gestão de devoluções.
 * 
 * @author TakiLN Team
 * @since 1.0
 */
public class DevolucaoService implements IDevolucaoService {

    private final List<IDevolucaoObserver> observers;
    private final pt.uminho.taki.dao.DevolucaoDAO devolucaoDAO;
    private final pt.uminho.taki.dao.VendaDAO vendaDAO;

    /**
     * Constrói uma instância de DevolucaoService.
     * 
     * @param devolucaoDAO o DAO de devolução
     */
    public DevolucaoService(pt.uminho.taki.dao.DevolucaoDAO devolucaoDAO) {
        this(devolucaoDAO, null);
    }

    /**
     * Constrói uma instância de DevolucaoService.
     * 
     * @param devolucaoDAO o DAO de devolução
     * @param vendaDAO o DAO de venda
     */
    public DevolucaoService(pt.uminho.taki.dao.DevolucaoDAO devolucaoDAO, pt.uminho.taki.dao.VendaDAO vendaDAO) {
        this.observers = new ArrayList<>();
        this.devolucaoDAO = devolucaoDAO;
        this.vendaDAO = vendaDAO;
    }

    /**
     * Adiciona um observador.
     * 
     * @param observer o observador
     */
    @Override
    public void adicionarObserver(IDevolucaoObserver observer) {
        if (observer != null && !this.observers.contains(observer)) {
            this.observers.add(observer);
        }
    }

    /**
     * Remove um observador.
     * 
     * @param observer o observador
     */
    @Override
    public void removerObserver(IDevolucaoObserver observer) {
        this.observers.remove(observer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Devolucao> listarDevolucoes() {
        return this.devolucaoDAO != null ? new ArrayList<>(this.devolucaoDAO.findAll()) : new ArrayList<>();
    }

    /**
     * Processa uma devolução.
     * 
     * @param vendaOriginal a venda original
     * @param linhasADevolver as linhas a devolver
     * @return a devolução processada
     * @throws PrazoDevolucaoExcedidoException se o prazo de devolução for excedido
     */
    @Override
    public Devolucao processarDevolucao(Venda vendaOriginal, List<LinhaVenda> linhasADevolver) throws PrazoDevolucaoExcedidoException {
        return processarDevolucao(vendaOriginal, linhasADevolver, null);
    }

    /**
     * Processa uma devolução.
     * 
     * @param vendaOriginal a venda original
     * @param linhasADevolver as linhas a devolver
     * @param metodoReembolsoAlternativo o método de reembolso alternativo
     * @return a devolução processada
     * @throws PrazoDevolucaoExcedidoException se o prazo de devolução for excedido
     */
    @Override
    public Devolucao processarDevolucao(Venda vendaOriginal, List<LinhaVenda> linhasADevolver, String metodoReembolsoAlternativo) throws PrazoDevolucaoExcedidoException {
        if (vendaOriginal == null) {
            throw new IllegalArgumentException("A Venda não pode ser nula.");
        }
        
        if (vendaOriginal.getDataHora() != null && vendaOriginal.getDataHora().isBefore(LocalDateTime.now().minusDays(30))) {
            throw new PrazoDevolucaoExcedidoException("O prazo legal de devolução de 30 dias foi excedido.");
        }

        if (linhasADevolver == null || linhasADevolver.isEmpty()) {
            throw new IllegalArgumentException("Tem de fornecer artigos para devolver.");
        }

        // Validar se as quantidades não excedem o disponível
        if (devolucaoDAO != null) {
            Map<String, Double> jaDevolvidoMap = devolucaoDAO.getQuantidadesDevolvidas();
            for (LinhaVenda lDev : linhasADevolver) {
                double jaDevolvido = jaDevolvidoMap.getOrDefault(lDev.getIdLinhaVenda(), 0.0);
                double original = vendaOriginal.getLinhas().stream()
                        .filter(lv -> lv.getIdLinhaVenda().equals(lDev.getIdLinhaVenda()))
                        .mapToDouble(LinhaVenda::getQuantidade)
                        .findFirst()
                        .orElse(0.0);
                
                if (jaDevolvido + lDev.getQuantidade() > original + 0.0001) {
                    throw new IllegalArgumentException("A quantidade a devolver (" + lDev.getQuantidade() + 
                        ") excede a quantidade disponível (" + (original - jaDevolvido) + ") para o artigo.");
                }
            }
        }

        double valorEstorno = linhasADevolver.stream()
                .mapToDouble(LinhaVenda::getTotalFinal)
                .sum();

        String metodoReembolso = "Original";
        if (vendaDAO != null) {
            metodoReembolso = vendaDAO.getMetodoPagamento(vendaOriginal.getIdVenda()).orElse("Original");
        }
        if (metodoReembolsoAlternativo != null && !metodoReembolsoAlternativo.isBlank()) {
            metodoReembolso = normalizarMetodoReembolso(metodoReembolsoAlternativo);
        }

        Devolucao devolucao = new Devolucao(
                UUID.randomUUID().toString(),
                LocalDateTime.now(),
                valorEstorno,
                metodoReembolso,
                null,
                vendaOriginal.getIdVenda(),
                vendaOriginal.getIdFuncionario()
        );

        if (devolucaoDAO != null) {
            devolucaoDAO.save(devolucao, linhasADevolver);
        }

        for (IDevolucaoObserver o : observers) {
            o.onDevolucaoConcluida(devolucao, linhasADevolver);
        }

        return devolucao;
    }

    /**
     * Processa uma devolução através do número da fatura.
     * 
     * @param numeroFatura o número da fatura
     * @param linhasADevolver as linhas a devolver
     * @return a devolução processada
     * @throws PrazoDevolucaoExcedidoException se o prazo de devolução for excedido
     */
    @Override
    public Devolucao processarDevolucaoPorNumeroFatura(String numeroFatura, List<LinhaVenda> linhasADevolver) throws PrazoDevolucaoExcedidoException {
        if (vendaDAO == null) {
            throw new IllegalStateException("A pesquisa por número de fatura requer o VendaDAO.");
        }
        Venda vendaOriginal = vendaDAO.findByNumeroFatura(numeroFatura)
                .orElseThrow(() -> new IllegalArgumentException("Não existe venda para a fatura indicada."));
        return processarDevolucao(vendaOriginal, linhasADevolver);
    }

    /**
     * Normaliza a designação do método de reembolso para os valores padrão do sistema.
     * 
     * @param metodo a designação textual do método a normalizar
     * @return a designação normalizada (ex.: "Numerário", "Cartão" ou "Original")
     * @throws IllegalArgumentException se o método fornecido não for reconhecido pelo sistema
     */
    private String normalizarMetodoReembolso(String metodo) {
        String valor = metodo.trim();
        if ("Numerário".equalsIgnoreCase(valor) || "Numerario".equalsIgnoreCase(valor)) return "Numerário";
        if ("Cartão".equalsIgnoreCase(valor) || "Cartao".equalsIgnoreCase(valor)) return "Cartão";
        if ("Original".equalsIgnoreCase(valor)) return "Original";
        throw new IllegalArgumentException("Método de reembolso inválido. Utilize Original, Numerário ou Cartão.");
    }
}
