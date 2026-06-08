package pt.uminho.taki.ln.vendas;

import pt.uminho.taki.ln.lojas.Produto;
import java.util.List;

/**
 * Interface que define o contrato para a gestão e cálculo de promoções.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public interface IPromocaoService {
    /**
     * Calcula o desconto mais favorável (maior) aplicável a um determinado produto no instante atual,
     * baseando-se nas datas das promoções em vigor.
     * @param produto O produto a avaliar.
     * @return O valor percentual de desconto a aplicar, 0.0 caso não existam promoções.
     */
    double calcularMelhorDesconto(Produto produto);

    /**
     * Retorna a lista de promoções correntemente ativas (válidas neste datetime e no estado "Ativa").
     * @return A lista de promoções ativas.
     */
    List<Promocao> getPromocoesAtivas();
    
    /**
     * Adiciona uma promoção ao motor de campanhas (para fins de mock/memória se o DAO não for injetado de imediato, ou para propagação lógica).
     * @param promocao A promoção a adicionar.
     */
    void adicionarPromocao(Promocao promocao);

    /**
     * Cancela logicamente uma promoção ativa/pendente.
     * @param idPromocao identificador da promoção
     * @param motivo motivo operacional da anulação
     */
    void cancelarPromocao(String idPromocao, String motivo);
}
