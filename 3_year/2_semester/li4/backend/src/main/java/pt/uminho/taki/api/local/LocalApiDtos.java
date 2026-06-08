package pt.uminho.taki.api.local;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;
import java.util.List;

/**
 * Classe contentora para todos os Objetos de Transferência de Dados (DTOs) utilizados pela API local para encapsular os payloads de pedido e resposta.
 * 
 * @author TakiLN Team
 * @since 1.0
 */
public final class LocalApiDtos {

    private LocalApiDtos() {
    }

    /** DTO que contém as credenciais necessárias para a autenticação de um utilizador.
     * 
     * @author TakiLN Team
     * @since 1.0
     */
    public static class PedidoAutenticacao {
        /** Endereço de email do utilizador para autenticação. */
        @NotBlank(message = "O email não pode ser vazio")
        @Email(message = "O formato do email é inválido")
        private String email;
        /** Palavra-passe do utilizador para autenticação. */
        @NotBlank(message = "A password não pode ser vazia")
        private String password;

        /**
         * Construtor para PedidoAutenticacao.
         */
        public PedidoAutenticacao() {}
        /**
         * Obtenção do correio eletrónico (email).
         * @return o correio eletrónico (email)
         */
        public String getEmail() { return email; }
        /**
         * Definição do correio eletrónico (email).
         * @param email o correio eletrónico (email)
         */
        public void setEmail(String email) { this.email = email; }
        /**
         * Obtenção da palavra-passe.
         * @return a palavra-passe
         */
        public String getPassword() { return password; }
        /**
         * Definição da palavra-passe.
         * @param password a palavra-passe
         */
        public void setPassword(String password) { this.password = password; }
    }

    /** DTO que transporta a palavra-passe a ser verificada para operações sensíveis.
     * 
     * @author TakiLN Team
     * @since 1.0
     */
    public static class PedidoVerificacaoPassword {
        /** Palavra-passe a ser verificada. */
        @NotBlank(message = "A password não pode ser vazia")
        private String password;

        /**
         * Construtor para PedidoVerificacaoPassword.
         */
        public PedidoVerificacaoPassword() {}
        /**
         * Obtenção da palavra-passe.
         * @return a palavra-passe
         */
        public String getPassword() { return password; }
        /**
         * Definição da palavra-passe.
         * @param password a palavra-passe
         */
        public void setPassword(String password) { this.password = password; }
    }

    /** DTO que encapsula a informação pessoal e de acesso necessária para o registo de um novo funcionário.
     * 
     * @author TakiLN Team
     * @since 1.0
     */
    public static class PedidoRegistoFuncionario {
        /** Identificador único do funcionário. */
        @NotBlank(message = "O id não pode ser vazio")
        private String id;
        /** Nome completo do funcionário. */
        @NotBlank(message = "O nome não pode ser vazio")
        private String nome;
        /** Endereço de email para acesso ao sistema. */
        @NotBlank(message = "O email não pode ser vazio")
        @Email(message = "O formato do email é inválido")
        private String email;
        /** Palavra-passe inicial do funcionário. */
        @NotBlank(message = "A password não pode ser vazia")
        private String password;
        /** Identificador do perfil de acesso a atribuir. */
        @NotBlank(message = "O perfil de acesso não pode ser vazio")
        private String idPerfilAcesso;
        /** Identificador da loja à qual o funcionário pertence. */
        @NotNull(message = "O id da loja não pode ser nulo")
        private Integer idLoja;

        /**
         * Construtor para PedidoRegistoFuncionario.
         */
        public PedidoRegistoFuncionario() {}
        /**
         * Obtenção do identificador (ID).
         * @return o identificador (ID)
         */
        public String getId() { return id; }
        /**
         * Definição do identificador (ID).
         * @param id o identificador (ID)
         */
        public void setId(String id) { this.id = id; }
        /**
         * Obtenção do nome.
         * @return o nome
         */
        public String getNome() { return nome; }
        /**
         * Definição do nome.
         * @param nome o nome
         */
        public void setNome(String nome) { this.nome = nome; }
        /**
         * Obtenção do correio eletrónico (email).
         * @return o correio eletrónico (email)
         */
        public String getEmail() { return email; }
        /**
         * Definição do correio eletrónico (email).
         * @param email o correio eletrónico (email)
         */
        public void setEmail(String email) { this.email = email; }
        /**
         * Obtenção da palavra-passe.
         * @return a palavra-passe
         */
        public String getPassword() { return password; }
        /**
         * Definição da palavra-passe.
         * @param password a palavra-passe
         */
        public void setPassword(String password) { this.password = password; }
        /**
         * Obtenção do identificador do perfil de acesso.
         * @return o identificador do perfil de acesso
         */
        public String getIdPerfilAcesso() { return idPerfilAcesso; }
        /**
         * Definição do identificador do perfil de acesso.
         * @param idPerfilAcesso o identificador do perfil de acesso
         */
        public void setIdPerfilAcesso(String idPerfilAcesso) { this.idPerfilAcesso = idPerfilAcesso; }
        /**
         * Obtenção do identificador da loja.
         * @return o identificador da loja
         */
        public Integer getIdLoja() { return idLoja; }
        /**
         * Definição do identificador da loja.
         * @param idLoja o identificador da loja
         */
        public void setIdLoja(Integer idLoja) { this.idLoja = idLoja; }

        /**
         * Conversão para objeto de domínio.
         * @return o objeto de domínio
         */
        public pt.uminho.taki.ln.lojas.Funcionario paraDominio() {
            return new pt.uminho.taki.ln.lojas.Funcionario(id, nome, email, password, idPerfilAcesso, idLoja);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    /** DTO que contém os detalhes do produto, que inclui preços e impostos, necessários para adicionar um novo produto ao catálogo.
     * 
     * @author TakiLN Team
     * @since 1.0
     */
    public static class PedidoAdicionarProduto {
        /** Identificador único do produto. */
        @NotBlank(message = "O ID do produto não pode ser vazio")
        private String idProduto;
        /** Código de barras único do produto. */
        @NotBlank(message = "O código de barras não pode ser vazio")
        private String codigoBarras;
        /** Nome comercial do produto. */
        @NotBlank(message = "O nome não pode ser vazio")
        private String nome;
        /** Descrição detalhada das características do produto. */
        @NotBlank(message = "A descrição não pode ser vazia")
        private String descricao;
        /** Preço de custo unitário (base para margem). */
        @NotNull(message = "O preço de custo não pode ser nulo")
        @Positive(message = "O preço de custo deve ser positivo")
        @Digits(integer = 12, fraction = 2, message = "O preço de custo deve ter no máximo 2 casas decimais")
        private Double precoCusto;
        /** Preço de venda ao público (PVP). */
        @NotNull(message = "O preço de venda não pode ser nulo")
        @Positive(message = "O preço de venda deve ser positivo")
        @Digits(integer = 12, fraction = 2, message = "O preço de venda deve ter no máximo 2 casas decimais")
        private Double precoVenda;
        /** Escalão de IVA aplicável (ex: NORMAL_23). */
        @NotBlank(message = "A taxa de IVA não pode ser vazia")
        private String taxaIva;
        
        /** Unidade de medida do produto (ex: unidade, kg). */
        private String unidadeMedida;
        /** Estado de comercialização do produto (ex: Ativo, Inativo). */
        private String estado;
        /** Quantidade inicial em stock. */
        private Double stock;
        /** Quantidade mínima de segurança em stock. */
        private Double minStock;

        /**
         * Construtor para PedidoAdicionarProduto.
         */
        public PedidoAdicionarProduto() {}
        /**
         * Obtenção do identificador do produto.
         * @return o identificador do produto
         */
        public String getIdProduto() { return idProduto; }
        /**
         * Definição do identificador do produto.
         * @param idProduto o identificador do produto
         */
        public void setIdProduto(String idProduto) { this.idProduto = idProduto; }
        /**
         * Obtenção do código de barras.
         * @return o código de barras
         */
        public String getCodigoBarras() { return codigoBarras; }
        /**
         * Definição do código de barras.
         * @param codigoBarras o código de barras
         */
        public void setCodigoBarras(String codigoBarras) { this.codigoBarras = codigoBarras; }
        /**
         * Obtenção do nome.
         * @return o nome
         */
        public String getNome() { return nome; }
        /**
         * Definição do nome.
         * @param nome o nome
         */
        public void setNome(String nome) { this.nome = nome; }
        /**
         * Obtenção da descrição.
         * @return a descrição
         */
        public String getDescricao() { return descricao; }
        /**
         * Definição da descrição.
         * @param descricao a descrição
         */
        public void setDescricao(String descricao) { this.descricao = descricao; }
        /**
         * Obtenção do preço de custo.
         * @return o preço de custo
         */
        public Double getPrecoCusto() { return precoCusto; }
        /**
         * Definição do preço de custo.
         * @param precoCusto o preço de custo
         */
        public void setPrecoCusto(Double precoCusto) { this.precoCusto = precoCusto; }
        /**
         * Obtenção do preço de venda.
         * @return o preço de venda
         */
        public Double getPrecoVenda() { return precoVenda; }
        /**
         * Definição do preço de venda.
         * @param precoVenda o preço de venda
         */
        public void setPrecoVenda(Double precoVenda) { this.precoVenda = precoVenda; }
        /**
         * Obtenção da taxa de IVA.
         * @return a taxa de IVA
         */
        public String getTaxaIva() { return taxaIva; }
        /**
         * Definição da taxa de IVA.
         * @param taxaIva a taxa de IVA
         */
        public void setTaxaIva(String taxaIva) { this.taxaIva = taxaIva; }
        /**
         * Obtenção da unidade de medida.
         * @return a unidade de medida
         */
        public String getUnidadeMedida() { return unidadeMedida; }
        /**
         * Definição da unidade de medida.
         * @param unidadeMedida a unidade de medida
         */
        public void setUnidadeMedida(String unidadeMedida) { this.unidadeMedida = unidadeMedida; }
        /**
         * Obtenção do estado.
         * @return o estado
         */
        public String getEstado() { return estado; }
        /**
         * Definição do estado.
         * @param estado o estado
         */
        public void setEstado(String estado) { this.estado = estado; }
        /**
         * Obtenção da quantidade em stock.
         * @return a quantidade em stock
         */
        public Double getStock() { return stock; }
        /**
         * Definição da quantidade em stock.
         * @param stock a quantidade em stock
         */
        public void setStock(Double stock) { this.stock = stock; }
        /**
         * Obtenção da quantidade mínima em stock.
         * @return a quantidade mínima em stock
         */
        public Double getMinStock() { return minStock; }
        /**
         * Definição da quantidade mínima em stock.
         * @param minStock a quantidade mínima em stock
         */
        public void setMinStock(Double minStock) { this.minStock = minStock; }

        /**
         * Conversão para objeto de domínio.
         * @return o objeto de domínio
         */
        public pt.uminho.taki.ln.lojas.Produto paraDominio() {
            return new pt.uminho.taki.ln.lojas.Produto(
                idProduto, codigoBarras, nome, descricao, precoCusto, precoVenda,
                pt.uminho.taki.ln.lojas.TaxaIva.NORMAL_23, // Valor por defeito, atualizado pelo controller
                unidadeMedida != null ? unidadeMedida : "unidade",
                estado != null ? estado : "Ativo"
            );
        }
    }

    /** DTO que contém a designação e a estrutura hierárquica para a criação de uma nova categoria de produto.
     * 
     * @author TakiLN Team
     * @since 1.0
     */
    public static class PedidoAdicionarCategoria {
        /** Identificador único da categoria. */
        @NotBlank(message = "O ID da categoria não pode ser vazio")
        private String idCategoria;
        /** Designação ou nome da categoria. */
        @NotBlank(message = "A designação não pode ser vazia")
        private String designacao;
        /** Identificador da categoria pai (opcional para categorias de topo). */
        private String idCategoriaPai;

        /**
         * Construtor para PedidoAdicionarCategoria.
         */
        public PedidoAdicionarCategoria() {}
        /**
         * Obtenção do identificador da categoria.
         * @return o identificador da categoria
         */
        public String getIdCategoria() { return idCategoria; }
        /**
         * Definição do identificador da categoria.
         * @param idCategoria o identificador da categoria
         */
        public void setIdCategoria(String idCategoria) { this.idCategoria = idCategoria; }
        /**
         * Obtenção da designação.
         * @return a designação
         */
        public String getDesignacao() { return designacao; }
        /**
         * Definição da designação.
         * @param designacao a designação
         */
        public void setDesignacao(String designacao) { this.designacao = designacao; }
        /**
         * Obtenção do identificador da categoria pai.
         * @return o identificador da categoria pai
         */
        public String getIdCategoriaPai() { return idCategoriaPai; }
        /**
         * Definição do identificador da categoria pai.
         * @param idCategoriaPai o identificador da categoria pai
         */
        public void setIdCategoriaPai(String idCategoriaPai) { this.idCategoriaPai = idCategoriaPai; }

        /**
         * Conversão para objeto de domínio.
         * @return o objeto de domínio
         */
        public pt.uminho.taki.ln.lojas.Categoria paraDominio() {
            return new pt.uminho.taki.ln.lojas.Categoria(idCategoria, designacao, idCategoriaPai != null ? idCategoriaPai : "");
        }
    }

    /** DTO que especifica os detalhes de um movimento de inventário, tais como alterações de quantidade e justificação.
     * 
     * @author TakiLN Team
     * @since 1.0
     */
    public static class PedidoRegistoMovimentoInventario {
        /** Identificador único do movimento. */
        @NotBlank(message = "O id não pode ser vazio")
        private String id;
        /** Tipo de movimento (ENTRADA, SAIDA, QUEBRA). */
        @NotBlank(message = "O tipo não pode ser vazio")
        private String tipo;
        /** Quantidade envolvida no movimento. */
        @NotNull(message = "A quantidade não pode ser nula")
        @Positive(message = "A quantidade deve ser positiva")
        private Double quantidade;
        /** Data e hora do registo do movimento (formato ISO-8601). */
        @NotBlank(message = "A data não pode ser vazia")
        private String dataRegisto;
        /** Justificação ou motivo do movimento. */
        @NotBlank(message = "O motivo não pode ser vazio")
        private String motivo;
        /** Identificador do registo de inventário afetado. */
        @NotBlank(message = "O id de inventário não pode ser vazio")
        private String idInventario;
        /** Identificador do funcionário que efetuou o registo. */
        @NotBlank(message = "O id do funcionário não pode ser vazio")
        private String idFuncionario;

        /**
         * Construtor para PedidoRegistoMovimentoInventario.
         */
        public PedidoRegistoMovimentoInventario() {}
        /**
         * Obtenção do identificador (ID).
         * @return o identificador (ID)
         */
        public String getId() { return id; }
        /**
         * Definição do identificador (ID).
         * @param id o identificador (ID)
         */
        public void setId(String id) { this.id = id; }
        /**
         * Obtenção do tipo.
         * @return o tipo
         */
        public String getTipo() { return tipo; }
        /**
         * Definição do tipo.
         * @param tipo o tipo
         */
        public void setTipo(String tipo) { this.tipo = tipo; }
        /**
         * Obtenção da quantidade.
         * @return a quantidade
         */
        public Double getQuantidade() { return quantidade; }
        /**
         * Definição da quantidade.
         * @param quantidade a quantidade
         */
        public void setQuantidade(Double quantidade) { this.quantidade = quantidade; }
        /**
         * Obtenção da data de registo.
         * @return a data de registo
         */
        public String getDataRegisto() { return dataRegisto; }
        /**
         * Definição da data de registo.
         * @param dataRegisto a data de registo
         */
        public void setDataRegisto(String dataRegisto) { this.dataRegisto = dataRegisto; }
        /**
         * Obtenção do motivo.
         * @return o motivo
         */
        public String getMotivo() { return motivo; }
        /**
         * Definição do motivo.
         * @param motivo o motivo
         */
        public void setMotivo(String motivo) { this.motivo = motivo; }
        /**
         * Obtenção do identificador de inventário.
         * @return o identificador de inventário
         */
        public String getIdInventario() { return idInventario; }
        /**
         * Definição do identificador de inventário.
         * @param idInventario o identificador de inventário
         */
        public void setIdInventario(String idInventario) { this.idInventario = idInventario; }
        /**
         * Obtenção do identificador do funcionário.
         * @return o identificador do funcionário
         */
        public String getIdFuncionario() { return idFuncionario; }
        /**
         * Definição do identificador do funcionário.
         * @param idFuncionario o identificador do funcionário
         */
        public void setIdFuncionario(String idFuncionario) { this.idFuncionario = idFuncionario; }

        /**
         * Conversão para objeto de domínio.
         * @return o objeto de domínio
         */
        public pt.uminho.taki.ln.inventario.MovimentoInventario paraDominio() {
            pt.uminho.taki.ln.inventario.MovimentoInventario m = new pt.uminho.taki.ln.inventario.MovimentoInventario();
            m.setId(id != null ? id : java.util.UUID.randomUUID().toString());
            m.setQuantidade(quantidade);
            m.setMotivo(motivo);
            m.setIdInventario(idInventario);
            m.setIdFuncionario(idFuncionario);
            return m;
        }
    }

    /** DTO que mapeia um produto a um fornecedor com códigos de referência e custos específicos.
     * 
     * @author TakiLN Team
     * @since 1.0
     */
    public static class PedidoAssociarProdutoFornecedor {
        /** Identificador do produto a associar. */
        @NotBlank(message = "O idProduto não pode ser vazio")
        private String idProduto;
        /** Identificador do fornecedor a associar. */
        @NotBlank(message = "O idFornecedor não pode ser vazio")
        private String idFornecedor;
        /** Preço de custo negociado com este fornecedor. */
        @NotNull(message = "O precoCusto não pode ser nulo")
        @PositiveOrZero(message = "O precoCusto não pode ser negativo")
        @Digits(integer = 12, fraction = 2, message = "O precoCusto deve ter no máximo 2 casas decimais")
        private Double precoCusto;

        /**
         * Construtor para PedidoAssociarProdutoFornecedor.
         */
        public PedidoAssociarProdutoFornecedor() {}
        /**
         * Obtenção do identificador do produto.
         * @return o identificador do produto
         */
        public String getIdProduto() { return idProduto; }
        /**
         * Definição do identificador do produto.
         * @param idProduto o identificador do produto
         */
        public void setIdProduto(String idProduto) { this.idProduto = idProduto; }
        /**
         * Obtenção do identificador do fornecedor.
         * @return o identificador do fornecedor
         */
        public String getIdFornecedor() { return idFornecedor; }
        /**
         * Definição do identificador do fornecedor.
         * @param idFornecedor o identificador do fornecedor
         */
        public void setIdFornecedor(String idFornecedor) { this.idFornecedor = idFornecedor; }
        /**
         * Obtenção do preço de custo.
         * @return o preço de custo
         */
        public Double getPrecoCusto() { return precoCusto; }
        /**
         * Definição do preço de custo.
         * @param precoCusto o preço de custo
         */
        public void setPrecoCusto(Double precoCusto) { this.precoCusto = precoCusto; }
    }

    /** DTO utilizado para iniciar uma nova venda, que a associa à loja e ao funcionário que trata da transação.
     * 
     * @author TakiLN Team
     * @since 1.0
     */
    public static class PedidoIniciarVenda {
        /** Identificador da loja onde a venda ocorre. */
        @NotNull(message = "A loja não pode ser nula")
        private Integer idLoja;
        /** Identificador do funcionário que realiza a venda. */
        @NotBlank(message = "O funcionario não pode ser vazio")
        private String idFuncionario;

        /**
         * Construtor para PedidoIniciarVenda.
         */
        public PedidoIniciarVenda() {}
        /**
         * Obtenção do identificador da loja.
         * @return o identificador da loja
         */
        public Integer getIdLoja() { return idLoja; }
        /**
         * Definição do identificador da loja.
         * @param idLoja o identificador da loja
         */
        public void setIdLoja(Integer idLoja) { this.idLoja = idLoja; }
        /**
         * Obtenção do identificador do funcionário.
         * @return o identificador do funcionário
         */
        public String getIdFuncionario() { return idFuncionario; }
        /**
         * Definição do identificador do funcionário.
         * @param idFuncionario o identificador do funcionário
         */
        public void setIdFuncionario(String idFuncionario) { this.idFuncionario = idFuncionario; }
    }

    /** DTO que representa uma linha de venda, que especifica o produto, a quantidade e os preços aplicados.
     * 
     * @author TakiLN Team
     * @since 1.0
     */
    public static class PedidoAdicionarLinhaVenda {
        /** Identificador do produto a adicionar à venda. */
        @NotBlank(message = "O id do produto não pode ser vazio")
        private String idProduto;
        /** Quantidade do produto a vender. */
        @NotNull(message = "A quantidade não pode ser nula")
        @Positive(message = "A quantidade deve ser positiva")
        private Integer quantidade;

        /**
         * Construtor para PedidoAdicionarLinhaVenda.
         */
        public PedidoAdicionarLinhaVenda() {}
        /**
         * Obtenção do identificador do produto.
         * @return o identificador do produto
         */
        public String getIdProduto() { return idProduto; }
        /**
         * Definição do identificador do produto.
         * @param idProduto o identificador do produto
         */
        public void setIdProduto(String idProduto) { this.idProduto = idProduto; }
        /**
         * Obtenção da quantidade.
         * @return a quantidade
         */
        public Integer getQuantidade() { return quantidade; }
        /**
         * Definição da quantidade.
         * @param quantidade a quantidade
         */
        public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }
    }

    /** DTO que resume os detalhes finais de uma venda, que inclui o método de pagamento e os valores totais, para efeitos de registo.
     * 
     * @author TakiLN Team
     * @since 1.0
     */
    public static class PedidoRegistoVenda {
        /** Método de pagamento utilizado (ex: Numerário, Cartão). */
        @NotBlank(message = "O metodoPagamento não pode ser vazio")
        private String metodoPagamento;
        /** Valor total entregue pelo cliente. */
        @NotNull(message = "O valorEntregue não pode ser nulo")
        @PositiveOrZero(message = "O valorEntregue não pode ser negativo")
        @Digits(integer = 12, fraction = 2, message = "O valorEntregue deve ter no máximo 2 casas decimais")
        private Double valorEntregue;

        /**
         * Construtor para PedidoRegistoVenda.
         */
        public PedidoRegistoVenda() {}
        /**
         * Obtenção do método de pagamento.
         * @return o método de pagamento
         */
        public String getMetodoPagamento() { return metodoPagamento; }
        /**
         * Definição do método de pagamento.
         * @param metodoPagamento o método de pagamento
         */
        public void setMetodoPagamento(String metodoPagamento) { this.metodoPagamento = metodoPagamento; }
        /**
         * Obtenção do valor entregue.
         * @return o valor entregue
         */
        public Double getValorEntregue() { return valorEntregue; }
        /**
         * Definição do valor entregue.
         * @param valorEntregue o valor entregue
         */
        public void setValorEntregue(Double valorEntregue) { this.valorEntregue = valorEntregue; }
    }

    /** DTO que detalha uma operação de devolução, que contém a lista de itens a serem devolvidos e a justificação.
     * 
     * @author TakiLN Team
     * @since 1.0
     */
    public static class PedidoProcessarDevolucao {
        /** Lista de itens da venda a devolver. */
        @NotEmpty(message = "A lista de linhas não pode ser vazia")
        private List<ItemDevolucao> linhas;

        /**
         * Construtor para PedidoProcessarDevolucao.
         */
        public PedidoProcessarDevolucao() {}
        /**
         * Obtenção das linhas.
         * @return as linhas
         */
        public List<ItemDevolucao> getLinhas() { return linhas; }
        /**
         * Definição das linhas.
         * @param linhas as linhas
         */
        public void setLinhas(List<ItemDevolucao> linhas) { this.linhas = linhas; }
    }

    /** DTO que representa um único item dentro de uma operação de devolução, que inclui o identificador do produto e a quantidade.
     * 
     * @author TakiLN Team
     * @since 1.0
     */
    public static class ItemDevolucao {
        /** Identificador da linha de venda original. */
        @NotBlank(message = "idLinhaVenda não pode ser vazio")
        private String idLinhaVenda;
        /** Quantidade a devolver desta linha. */
        @NotNull(message = "A quantidade não pode ser nula")
        @Positive(message = "A quantidade deve ser positiva")
        private Integer quantidade;

        /**
         * Construtor para ItemDevolucao.
         */
        public ItemDevolucao() {}
        /**
         * Obtenção do identificador da linha de venda.
         * @return o identificador da linha de venda
         */
        public String getIdLinhaVenda() { return idLinhaVenda; }
        /**
         * Definição do identificador da linha de venda.
         * @param idLinhaVenda o identificador da linha de venda
         */
        public void setIdLinhaVenda(String idLinhaVenda) { this.idLinhaVenda = idLinhaVenda; }
        /**
         * Obtenção da quantidade.
         * @return a quantidade
         */
        public Integer getQuantidade() { return quantidade; }
        /**
         * Definição da quantidade.
         * @param quantidade a quantidade
         */
        public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    /** DTO que transporta a informação atualizada para modificar os detalhes e o preço de um produto existente.
     * 
     * @author TakiLN Team
     * @since 1.0
     */
    public static class PedidoEditarProduto {
        /** Novo código de barras do produto. */
        @NotBlank(message = "O código de barras não pode ser vazio")
        private String codigoBarras;
        /** Novo nome comercial. */
        @NotBlank(message = "O nome não pode ser vazio")
        private String nome;
        /** Nova descrição do produto. */
        @NotBlank(message = "A descrição não pode ser vazia")
        private String descricao;
        /** Novo preço de custo unitário. */
        @NotNull(message = "O preço de custo não pode ser nulo")
        @Positive(message = "O preço de custo deve ser positivo")
        @Digits(integer = 12, fraction = 2, message = "O preço de custo deve ter no máximo 2 casas decimais")
        private Double precoCusto;
        /** Novo preço de venda ao público. */
        @NotNull(message = "O preço de venda não pode ser nulo")
        @Positive(message = "O preço de venda deve ser positivo")
        @Digits(integer = 12, fraction = 2, message = "O preço de venda deve ter no máximo 2 casas decimais")
        private Double precoVenda;
        /** Nova taxa de IVA aplicável. */
        @NotBlank(message = "A taxa de IVA não pode ser vazia")
        private String taxaIva;
        /** Nova unidade de medida. */
        private String unidadeMedida;
        /** Novo estado do produto (Ativo/Inativo). */
        private String estado;
        /** Ajuste da quantidade em stock. */
        private Double stock;
        /** Novo limite de segurança em stock. */
        private Double minStock;

        /**
         * Obtenção do código de barras.
         * @return o código de barras
         */
        public String getCodigoBarras() { return codigoBarras; }
        /**
         * Definição do código de barras.
         * @param codigoBarras o código de barras
         */
        public void setCodigoBarras(String codigoBarras) { this.codigoBarras = codigoBarras; }
        /**
         * Obtenção do nome.
         * @return o nome
         */
        public String getNome() { return nome; }
        /**
         * Definição do nome.
         * @param nome o nome
         */
        public void setNome(String nome) { this.nome = nome; }
        /**
         * Obtenção da descrição.
         * @return a descrição
         */
        public String getDescricao() { return descricao; }
        /**
         * Definição da descrição.
         * @param descricao a descrição
         */
        public void setDescricao(String descricao) { this.descricao = descricao; }
        /**
         * Obtenção do preço de custo.
         * @return o preço de custo
         */
        public Double getPrecoCusto() { return precoCusto; }
        /**
         * Definição do preço de custo.
         * @param precoCusto o preço de custo
         */
        public void setPrecoCusto(Double precoCusto) { this.precoCusto = precoCusto; }
        /**
         * Obtenção do preço de venda.
         * @return o preço de venda
         */
        public Double getPrecoVenda() { return precoVenda; }
        /**
         * Definição do preço de venda.
         * @param precoVenda o preço de venda
         */
        public void setPrecoVenda(Double precoVenda) { this.precoVenda = precoVenda; }
        /**
         * Obtenção da taxa de IVA.
         * @return a taxa de IVA
         */
        public String getTaxaIva() { return taxaIva; }
        /**
         * Definição da taxa de IVA.
         * @param taxaIva a taxa de IVA
         */
        public void setTaxaIva(String taxaIva) { this.taxaIva = taxaIva; }
        /**
         * Obtenção da unidade de medida.
         * @return a unidade de medida
         */
        public String getUnidadeMedida() { return unidadeMedida; }
        /**
         * Definição da unidade de medida.
         * @param unidadeMedida a unidade de medida
         */
        public void setUnidadeMedida(String unidadeMedida) { this.unidadeMedida = unidadeMedida; }
        /**
         * Obtenção do estado.
         * @return o estado
         */
        public String getEstado() { return estado; }
        /**
         * Definição do estado.
         * @param estado o estado
         */
        public void setEstado(String estado) { this.estado = estado; }
        /**
         * Obtenção do stock.
         * @return o stock
         */
        public Double getStock() { return stock; }
        /**
         * Definição do stock.
         * @param stock o stock
         */
        public void setStock(Double stock) { this.stock = stock; }
        /**
         * Obtenção do stock mínimo.
         * @return o stock mínimo
         */
        public Double getMinStock() { return minStock; }
        /**
         * Definição do stock mínimo.
         * @param minStock o stock mínimo
         */
        public void setMinStock(Double minStock) { this.minStock = minStock; }
    }

    /** DTO que contém a nova designação ou o mapeamento de categoria pai para uma categoria existente.
     * 
     * @author TakiLN Team
     * @since 1.0
     */
    public static class PedidoEditarCategoria {
        /** Nova designação da categoria. */
        @NotBlank(message = "A designação não pode ser vazia")
        private String designacao;
        /** Novo identificador da categoria pai. */
        private String idCategoriaPai;

        /**
         * Obtenção da designação.
         * @return a designação
         */
        public String getDesignacao() { return designacao; }
        /**
         * Definição da designação.
         * @param designacao a designação
         */
        public void setDesignacao(String designacao) { this.designacao = designacao; }
        /**
         * Obtenção do identificador da categoria pai.
         * @return o identificador da categoria pai
         */
        public String getIdCategoriaPai() { return idCategoriaPai; }
        /**
         * Definição do identificador da categoria pai.
         * @param idCategoriaPai o identificador da categoria pai
         */
        public void setIdCategoriaPai(String idCategoriaPai) { this.idCategoriaPai = idCategoriaPai; }
    }

    /** DTO que encapsula informação do fornecedor, tal como detalhes de contacto e identificação fiscal para efeitos de registo ou atualização.
     * 
     * @author TakiLN Team
     * @since 1.0
     */
    public static class PedidoFornecedor {
        /** Identificador único do fornecedor. */
        @NotBlank(message = "ID do fornecedor obrigatório")
        private String idFornecedor;
        /** Nome ou razão social do fornecedor. */
        @NotBlank(message = "Nome obrigatório")
        private String nome;
        /** Número de Identificação Fiscal (NIF) do fornecedor. */
        @NotBlank(message = "NIF obrigatório")
        private String nif;
        /** Contacto telefónico. */
        @NotBlank(message = "Telefone obrigatório")
        private String telefone;
        /** Endereço de correio eletrónico. */
        @NotBlank(message = "Email obrigatório")
        @Email(message = "Email inválido")
        private String email;
        /** Estado do fornecedor (Ativo/Inativo). */
        private String estado;

        /**
         * Obtenção do identificador do fornecedor.
         * @return o identificador do fornecedor
         */
        public String getIdFornecedor() { return idFornecedor; }
        /**
         * Definição do identificador do fornecedor.
         * @param idFornecedor o identificador do fornecedor
         */
        public void setIdFornecedor(String idFornecedor) { this.idFornecedor = idFornecedor; }
        /**
         * Obtenção do nome.
         * @return o nome
         */
        public String getNome() { return nome; }
        /**
         * Definição do nome.
         * @param nome o nome
         */
        public void setNome(String nome) { this.nome = nome; }
        /**
         * Obtenção do NIF.
         * @return o NIF
         */
        public String getNif() { return nif; }
        /**
         * Definição do NIF.
         * @param nif o NIF
         */
        public void setNif(String nif) { this.nif = nif; }
        /**
         * Obtenção do telefone.
         * @return o telefone
         */
        public String getTelefone() { return telefone; }
        /**
         * Definição do telefone.
         * @param telefone o telefone
         */
        public void setTelefone(String telefone) { this.telefone = telefone; }
        /**
         * Obtenção do correio eletrónico.
         * @return o correio eletrónico
         */
        public String getEmail() { return email; }
        /**
         * Definição do correio eletrónico.
         * @param email o correio eletrónico
         */
        public void setEmail(String email) { this.email = email; }
        /**
         * Obtenção do estado.
         * @return o estado
         */
        public String getEstado() { return estado; }
        /**
         * Definição do estado.
         * @param estado o estado
         */
        public void setEstado(String estado) { this.estado = estado; }

        /**
         * Conversão para objeto de domínio.
         * @return o objeto de domínio
         */
        public pt.uminho.taki.ln.fornecimentos.Fornecedor paraDominio() {
            return new pt.uminho.taki.ln.fornecimentos.Fornecedor(idFornecedor, nome, nif, telefone, email, estado != null ? estado : "Ativo");
        }
    }

    /** DTO que define uma encomenda dirigida a um fornecedor, que inclui datas de entrega e itens específicos.
     * 
     * @author TakiLN Team
     * @since 1.0
     */
    public static class PedidoCriarEncomenda {
        /** Identificador único da nova encomenda. */
        @NotBlank(message = "idEncomenda obrigatório")
        private String idEncomenda = java.util.UUID.randomUUID().toString();
        /** Identificador do fornecedor destinatário. */
        @NotBlank(message = "idFornecedor obrigatório")
        private String idFornecedor = "AUTO_RESOLVE";
        /** Identificador da loja que solicita a encomenda. */
        @NotNull(message = "idLoja obrigatório")
        private Integer idLoja;
        /** Lista de itens a encomendar. */
        @NotEmpty(message = "A encomenda deve ter pelo menos uma linha")
        private List<ItemEncomenda> linhas;

        /**
         * Obtenção do identificador da encomenda.
         * @return o identificador da encomenda
         */
        public String getIdEncomenda() { return idEncomenda; }
        /**
         * Definição do identificador da encomenda.
         * @param idEncomenda o identificador da encomenda
         */
        public void setIdEncomenda(String idEncomenda) { 
            if (idEncomenda != null && !idEncomenda.trim().isEmpty()) {
                this.idEncomenda = idEncomenda; 
            }
        }
        /**
         * Obtenção do identificador do fornecedor.
         * @return o identificador do fornecedor
         */
        public String getIdFornecedor() { return idFornecedor; }
        /**
         * Definição do identificador do fornecedor.
         * @param idFornecedor o identificador do fornecedor
         */
        public void setIdFornecedor(String idFornecedor) { 
            if (idFornecedor != null && !idFornecedor.trim().isEmpty()) {
                this.idFornecedor = idFornecedor; 
            }
        }
        /**
         * Obtenção do identificador da loja.
         * @return o identificador da loja
         */
        public Integer getIdLoja() { return idLoja; }
        /**
         * Definição do identificador da loja.
         * @param idLoja o identificador da loja
         */
        public void setIdLoja(Integer idLoja) { this.idLoja = idLoja; }
        /**
         * Obtenção das linhas.
         * @return as linhas
         */
        public List<ItemEncomenda> getLinhas() { return linhas; }
        /**
         * Definição das linhas.
         * @param linhas as linhas
         */
        public void setLinhas(List<ItemEncomenda> linhas) { this.linhas = linhas; }

        /**
         * Resolução das linhas.
         * @return as linhas resolvidas
         */
        public java.util.List<pt.uminho.taki.ln.fornecimentos.LinhaEncomenda> resolverLinhas() {
            return (linhas == null) ? java.util.Collections.emptyList() : 
                linhas.stream().map(i -> new pt.uminho.taki.ln.fornecimentos.LinhaEncomenda(
                    idEncomenda, i.idProduto, i.quantidade, i.precoCusto
                )).collect(java.util.stream.Collectors.toList());
        }
    }

    /** DTO que detalha uma única linha de produto dentro de uma encomenda, que inclui a quantidade e o custo acordado.
     * 
     * @author TakiLN Team
     * @since 1.0
     */
    public static class ItemEncomenda {
        /** Identificador do produto a encomendar. */
        @NotBlank(message = "idProduto obrigatório")
        private String idProduto;
        /** Quantidade solicitada. */
        @NotNull(message = "Quantidade obrigatória")
        @Positive(message = "Quantidade deve ser positiva")
        private Double quantidade;
        /** Preço de custo unitário estimado. */
        @NotNull(message = "Preço de custo obrigatório")
        @Positive(message = "Preço de custo deve ser positivo")
        @Digits(integer = 12, fraction = 2, message = "Preço de custo deve ter no máximo 2 casas decimais")
        private Double precoCusto = 1.0;

        /**
         * Obtenção do identificador do produto.
         * @return o identificador do produto
         */
        public String getIdProduto() { return idProduto; }
        /**
         * Definição do identificador do produto.
         * @param idProduto o identificador do produto
         */
        public void setIdProduto(String idProduto) { this.idProduto = idProduto; }
        /**
         * Obtenção da quantidade.
         * @return a quantidade
         */
        public Double getQuantidade() { return quantidade; }
        /**
         * Definição da quantidade.
         * @param quantidade a quantidade
         */
        public void setQuantidade(Double quantidade) { this.quantidade = quantidade; }
        /**
         * Obtenção do preço de custo.
         * @return o preço de custo
         */
        public Double getPrecoCusto() { return precoCusto; }
        /**
         * Definição do preço de custo.
         * @param precoCusto o preço de custo
         */
        public void setPrecoCusto(Double precoCusto) { 
            if (precoCusto != null && precoCusto > 0) {
                this.precoCusto = precoCusto; 
            }
        }
    }

    /** DTO que especifica a percentagem e o período de validade para uma nova promoção aplicada a produtos ou categorias.
     * 
     * @author TakiLN Team
     * @since 1.0
     */
    public static class PedidoAdicionarPromocao {
        /** Identificador único da promoção. */
        @NotBlank(message = "idPromocao obrigatório")
        private String idPromocao;
        /** Nome ou descrição da campanha promocional. */
        @NotBlank(message = "Designação obrigatória")
        private String designacao;
        /** Percentagem de desconto a aplicar. */
        @NotNull(message = "Desconto obrigatório")
        @Positive(message = "Desconto deve ser positivo")
        private Double desconto;
        /** Data e hora de início da validade. */
        @NotBlank(message = "Data de início obrigatória")
        private String dataInicio;
        /** Data e hora de fim da validade. */
        @NotBlank(message = "Data de fim obrigatória")
        private String dataFim;
        /** Identificador da loja onde se aplica (opcional). */
        private Integer idLoja;
        /** Lista de identificadores de produtos abrangidos. */
        private List<String> produtos;
        /** Lista de identificadores de categorias abrangidas. */
        private List<String> categorias;

        /**
         * Obtenção do identificador da promoção.
         * @return o identificador da promoção
         */
        public String getIdPromocao() { return idPromocao; }
        /**
         * Definição do identificador da promoção.
         * @param idPromocao o identificador da promoção
         */
        public void setIdPromocao(String idPromocao) { this.idPromocao = idPromocao; }
        /**
         * Obtenção da designação.
         * @return a designação
         */
        public String getDesignacao() { return designacao; }
        /**
         * Definição da designação.
         * @param designacao a designação
         */
        public void setDesignacao(String designacao) { this.designacao = designacao; }
        /**
         * Obtenção do desconto.
         * @return o desconto
         */
        public Double getDesconto() { return desconto; }
        /**
         * Definição do desconto.
         * @param desconto o desconto
         */
        public void setDesconto(Double desconto) { this.desconto = desconto; }
        /**
         * Obtenção da data de início.
         * @return a data de início
         */
        public String getDataInicio() { return dataInicio; }
        /**
         * Definição da data de início.
         * @param dataInicio a data de início
         */
        public void setDataInicio(String dataInicio) { this.dataInicio = dataInicio; }
        /**
         * Obtenção da data de fim.
         * @return a data de fim
         */
        public String getDataFim() { return dataFim; }
        /**
         * Definição da data de fim.
         * @param dataFim a data de fim
         */
        public void setDataFim(String dataFim) { this.dataFim = dataFim; }
        /**
         * Obtenção do identificador da loja.
         * @return o identificador da loja
         */
        public Integer getIdLoja() { return idLoja; }
        /**
         * Definição do identificador da loja.
         * @param idLoja o identificador da loja
         */
        public void setIdLoja(Integer idLoja) { this.idLoja = idLoja; }
        /**
         * Obtenção das permissões.
         * @return as permissões
         */
        public List<String> getProdutos() { return produtos; }
        /**
         * Definição das permissões.
         * @param produtos as permissões
         */
        public void setProdutos(List<String> produtos) { this.produtos = produtos; }
        /**
         * Obtenção das permissões.
         * @return as permissões
         */
        public List<String> getCategorias() { return categorias; }
        /**
         * Definição das permissões.
         * @param categorias as permissões
         */
        public void setCategorias(List<String> categorias) { this.categorias = categorias; }

        /**
         * Conversão para objeto de domínio.
         * @param parser o analisador (parser)
         * @return o objeto de domínio
         */
        public pt.uminho.taki.ln.vendas.Promocao paraDominio(java.util.function.Function<String, java.time.LocalDateTime> parser) {
            pt.uminho.taki.ln.vendas.Promocao p = new pt.uminho.taki.ln.vendas.Promocao(
                idPromocao, designacao, desconto, 
                parser.apply(dataInicio), parser.apply(dataFim), 
                "Ativa", idLoja != null ? idLoja : 1
            );
            if (produtos != null) p.setProdutos(new java.util.HashSet<>(produtos));
            if (categorias != null) p.setCategorias(new java.util.HashSet<>(categorias));
            return p;
        }
    }

    /** DTO utilizado para solicitar o cancelamento ou a terminação antecipada de uma promoção ativa.
     * 
     * @author TakiLN Team
     * @since 1.0
     */
    public static class PedidoCancelarPromocao {
        /** Motivo ou justificação para o cancelamento da promoção. */
        @NotBlank(message = "Motivo obrigatório")
        private String motivo;

        /**
         * Obtenção do motivo.
         * @return o motivo
         */
        public String getMotivo() { return motivo; }
        /**
         * Definição do motivo.
         * @param motivo o motivo
         */
        public void setMotivo(String motivo) { this.motivo = motivo; }
    }

    /** DTO que contém informação de contacto e de acesso atualizada para a modificação de um registo de funcionário existente.
     * 
     * @author TakiLN Team
     * @since 1.0
     */
    public static class PedidoAtualizarFuncionario {
        /** Novo nome completo do funcionário. */
        @NotBlank(message = "Nome obrigatório")
        private String nome;
        /** Novo endereço de email. */
        @NotBlank(message = "Email obrigatório")
        @Email(message = "Email inválido")
        private String email;
        /** Nova palavra-passe (opcional, apenas se for para alterar). */
        private String password;
        /** Novo identificador do perfil de acesso. */
        @NotBlank(message = "Perfil obrigatório")
        private String idPerfilAcesso;
        /** Novo identificador da loja. */
        @NotNull(message = "Loja obrigatória")
        private Integer idLoja;

        /**
         * Obtenção do nome.
         * @return o nome
         */
        public String getNome() { return nome; }
        /**
         * Definição do nome.
         * @param nome o nome
         */
        public void setNome(String nome) { this.nome = nome; }
        /**
         * Obtenção do correio eletrónico.
         * @return o correio eletrónico
         */
        public String getEmail() { return email; }
        /**
         * Definição do correio eletrónico.
         * @param email o correio eletrónico
         */
        public void setEmail(String email) { this.email = email; }
        /**
         * Obtenção da palavra-passe.
         * @return a palavra-passe
         */
        public String getPassword() { return password; }
        /**
         * Definição da palavra-passe.
         * @param password a palavra-passe
         */
        public void setPassword(String password) { this.password = password; }
        /**
         * Obtenção do identificador do perfil de acesso.
         * @return o identificador do perfil de acesso
         */
        public String getIdPerfilAcesso() { return idPerfilAcesso; }
        /**
         * Definição do identificador do perfil de acesso.
         * @param idPerfilAcesso o identificador do perfil de acesso
         */
        public void setIdPerfilAcesso(String idPerfilAcesso) { this.idPerfilAcesso = idPerfilAcesso; }
        /**
         * Obtenção do identificador da loja.
         * @return o identificador da loja
         */
        public Integer getIdLoja() { return idLoja; }
        /**
         * Definição do identificador da loja.
         * @param idLoja o identificador da loja
         */
        public void setIdLoja(Integer idLoja) { this.idLoja = idLoja; }

        /**
         * Conversão para objeto de domínio.
         * @param id o identificador
         * @return o objeto de domínio
         */
        public pt.uminho.taki.ln.lojas.Funcionario paraDominio(String id) {
            pt.uminho.taki.ln.lojas.Funcionario f = new pt.uminho.taki.ln.lojas.Funcionario(id, nome, email, password, idPerfilAcesso, idLoja);
            return f;
        }
    }

    /** DTO que transporta as credenciais de administrador necessárias para autorizar ações restritas ou de alto risco.
     * 
     * @author TakiLN Team
     * @since 1.0
     */
    public static class PedidoConfirmacaoAdmin {
        /** Palavra-passe do administrador para validação da operação. */
        @NotBlank(message = "Password do administrador obrigatória")
        private String passwordAdministrador;

        /**
         * Obtenção da palavra-passe do administrador.
         * @return a palavra-passe do administrador
         */
        public String getPasswordAdministrador() { return passwordAdministrador; }
        /**
         * Definição da palavra-passe do administrador.
         * @param passwordAdministrador a palavra-passe do administrador
         */
        public void setPasswordAdministrador(String passwordAdministrador) { this.passwordAdministrador = passwordAdministrador; }
    }

    /** DTO que define a atribuição de um perfil de acesso específico a um funcionário.
     * 
     * @author TakiLN Team
     * @since 1.0
     */
    public static class PedidoAtribuirPerfil {
        /** Nome do perfil de acesso a ser atribuído. */
        @NotBlank(message = "Perfil obrigatório")
        private String nomePerfil;

        /**
         * Obtenção do nome do perfil.
         * @return o nome do perfil
         */
        public String getNomePerfil() { return nomePerfil; }
        /**
         * Definição do nome do perfil.
         * @param nomePerfil o nome do perfil
         */
        public void setNomePerfil(String nomePerfil) { this.nomePerfil = nomePerfil; }
    }

    /** DTO que especifica a designação e as permissões exatas necessárias para criar um novo perfil de acesso.
     * 
     * @author TakiLN Team
     * @since 1.0
     */
    public static class PedidoRegistarPerfil {
        /** Identificador único do novo perfil. */
        @NotBlank(message = "ID do perfil obrigatório")
        private String id;
        /** Nome legível do perfil. */
        @NotBlank(message = "Nome do perfil obrigatório")
        private String nome;
        /** Lista de permissões associadas ao perfil. */
        @NotEmpty(message = "Permissões obrigatórias")
        private List<String> permissoes;

        /**
         * Obtenção do identificador (ID).
         * @return o identificador (ID)
         */
        public String getId() { return id; }
        /**
         * Definição do identificador (ID).
         * @param id o identificador (ID)
         */
        public void setId(String id) { this.id = id; }
        /**
         * Obtenção do nome.
         * @return o nome
         */
        public String getNome() { return nome; }
        /**
         * Definição do nome.
         * @param nome o nome
         */
        public void setNome(String nome) { this.nome = nome; }
        /**
         * Obtenção das permissões.
         * @return as permissões
         */
        public List<String> getPermissoes() { return permissoes; }
        /**
         * Definição das permissões.
         * @param permissoes as permissões
         */
        public void setPermissoes(List<String> permissoes) { this.permissoes = permissoes; }

        /**
         * Conversão para objeto de domínio.
         * @param resolverPermissoes o resolvedor de permissões
         * @return o objeto de domínio
         */
        public pt.uminho.taki.ln.lojas.PerfilAcesso paraDominio(java.util.function.Function<List<String>, List<pt.uminho.taki.ln.lojas.Permissao>> resolverPermissoes) {
            return new pt.uminho.taki.ln.lojas.PerfilAcesso(id, nome, resolverPermissoes.apply(permissoes));
        }
    }

    /** DTO que encapsula as permissões atualizadas para modificar um perfil de acesso existente.
     * 
     * @author TakiLN Team
     * @since 1.0
     */
    public static class PedidoEditarPerfil {
        /** Nova lista exaustiva de permissões do perfil. */
        @NotEmpty(message = "Permissões obrigatórias")
        private List<String> permissoes;

        /**
         * Obtenção das permissões.
         * @return as permissões
         */
        public List<String> getPermissoes() { return permissoes; }
        /**
         * Definição das permissões.
         * @param permissoes as permissões
         */
        public void setPermissoes(List<String> permissoes) { this.permissoes = permissoes; }
    }
}
