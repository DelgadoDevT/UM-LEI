package pt.uminho.taki.ln.fatura;

import java.security.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.Base64;
import pt.uminho.taki.ln.vendas.IVendaObserver;
import pt.uminho.taki.ln.vendas.Venda;
import pt.uminho.taki.ln.vendas.LinhaVenda;
import pt.uminho.taki.dao.FaturaDAO;
import pt.uminho.taki.dao.VendaDAO;
import io.javalin.http.NotFoundResponse;
import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPCell;

/**
 * Implementação do serviço de faturas com lógica de integridade SAF-T (PT).
 * Garante o encadeamento de hashes entre faturas consecutivas para evitar a manipulação de dados.
 *
 * @author TakiLN Team
 * @since 1.0
 */
public class FaturaService implements IFaturaService, IVendaObserver {
    private final FaturaDAO faturaDAO;
    private final VendaDAO vendaDAO;
    /** Chave privada RSA utilizada para a assinatura digital das faturas (Material criptográfico sensível). */
    private PrivateKey privateKey;

    /**
     * Construtor por omissão que utiliza a implementação padrão de FaturaDAO.
     */
    public FaturaService() {
        this(new FaturaDAO(), new VendaDAO());
    }

    /**
     * Construtor com injeção de dependência do FaturaDAO.
     *
     * @param faturaDAO o DAO de faturas
     */
    public FaturaService(FaturaDAO faturaDAO) {
        this(faturaDAO, new VendaDAO());
    }

    /**
     * Construtor com injeção de dependências do FaturaDAO e VendaDAO.
     *
     * @param faturaDAO o DAO de faturas
     * @param vendaDAO o DAO de vendas
     */
    public FaturaService(FaturaDAO faturaDAO, VendaDAO vendaDAO) {
        this.faturaDAO = Objects.requireNonNull(faturaDAO, "faturaDAO");
        this.vendaDAO = Objects.requireNonNull(vendaDAO, "vendaDAO");
        // Iniciar com uma chave de teste (em produção carregada de ficheiro/ambiente)
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair kp = kpg.generateKeyPair();
            this.privateKey = kp.getPrivate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Fatura emitirFatura(String idVenda, String nifCliente) throws Exception {
        // 1. Obter a última fatura para encadeamento
        Fatura ultima = getUltimaFatura();

        // 2. Criar nova fatura
        int proximoNumero = faturaDAO.count() + 1; 
        String numFatura = "FT 2024/" + proximoNumero;
        
        // Regra de Integridade: Data não pode ser anterior à última
        LocalDateTime agora = LocalDateTime.now();
        if (ultima != null && agora.isBefore(ultima.getDataEmissao())) {
            throw new IllegalStateException("Data da fatura não pode ser anterior à última fatura emitida.");
        }

        // Validação básica do NIF para cumprir restrição da BD (9 dígitos ou null)
        String nifValidado = (nifCliente != null && nifCliente.matches("^[0-9]{9}$")) ? nifCliente : null;

        Fatura nova = new Fatura(numFatura, nifValidado, idVenda);
        nova.setDataEmissao(agora);

        // 3. Gerar Hash (Assinatura RSA)
        String dataParaAssinar = construirPayloadAssinatura(nova, ultima != null ? ultima.getHash() : "");
        nova.setHash(gerarAssinatura(dataParaAssinar));
        
        return nova;
    }

    /**
     * Gera a assinatura RSA-SHA1 para o payload de integridade da fatura.
     * 
     * @param texto o payload a assinar
     * @return a assinatura codificada em Base64
     * @throws Exception se ocorrer um erro na geração da assinatura
     */
    private String gerarAssinatura(String texto) throws Exception {
        Signature privateSignature = Signature.getInstance("SHA1withRSA");
        privateSignature.initSign(privateKey);
        privateSignature.update(texto.getBytes("UTF-8"));
        byte[] s = privateSignature.sign();
        return Base64.getEncoder().encodeToString(s);
    }

    /**
     * Obtém a última fatura emitida para permitir o encadeamento de hashes (requisito SAF-T).
     * 
     * @return a última fatura ou null se for a primeira
     */
    private Fatura getUltimaFatura() {
        return faturaDAO.findAll().stream()
                .sorted(Comparator.comparing(Fatura::getDataEmissao).reversed())
                .findFirst()
                .orElse(null);
    }

    /**
     * Reage à conclusão de uma venda para proceder à emissão automática da respetiva fatura.
     * Este método assegura a integridade dos dados seguindo as normas SAF-T (PT), ao invocar
     * a lógica de emissão que gera uma assinatura digital RSA-SHA1. A assinatura é construída
     * com base no encadeamento da hash da fatura anterior, garantindo a imutabilidade e a 
     * ordem cronológica dos documentos. A fatura resultante é então associada à venda.
     *
     * @param venda a entidade de venda concluída que despoleta a emissão
     */
    @Override
    public void onVendaConcluida(Venda venda) {
        if (venda == null || venda.getIdVenda() == null) return;
        try {
            // Se o NIF vier vazio ou for "Consumidor Final", emitirFatura tratará como null
            Fatura f = this.emitirFatura(venda.getIdVenda(), null);
            venda.setFatura(f);
        } catch (Exception e) {
            System.err.println("Erro ao emitir fatura automática para a venda: " + venda.getIdVenda());
            e.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Fatura getFatura(String numFatura) {
        return faturaDAO.findByNumeroFatura(numFatura).orElse(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Fatura> getFaturas() {
        return new ArrayList<>(faturaDAO.findAll());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Fatura emitirSegundaVia(String numFatura) {
        Fatura original = getFatura(numFatura);
        if (original == null) {
            throw new IllegalArgumentException("Fatura inexistente para segunda via.");
        }
        return original.clone();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String exportarFaturaJson(String numFatura) {
        Fatura f = emitirSegundaVia(numFatura);
        return "{"
                + "\"idFatura\":\"" + escapeJson(f.getIdFatura()) + "\","
                + "\"numFatura\":\"" + escapeJson(f.getNumFatura()) + "\","
                + "\"dataEmissao\":\"" + f.getDataEmissao() + "\","
                + "\"nifCliente\":\"" + escapeJson(f.getNifCliente()) + "\","
                + "\"idVenda\":\"" + escapeJson(f.getIdVenda()) + "\","
                + "\"hash\":\"" + escapeJson(f.getHash()) + "\","
                + "\"hashControl\":\"" + escapeJson(f.getHashControl()) + "\""
                + "}";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String exportarFaturaCsv(String numFatura) {
        Fatura f = emitirSegundaVia(numFatura);
        StringBuilder sb = new StringBuilder();
        sb.append("id_fatura,num_fatura,data_emissao,nif_cliente,id_venda,hash,hash_control\n");
        sb.append(csv(f.getIdFatura())).append(',')
          .append(csv(f.getNumFatura())).append(',')
          .append(csv(f.getDataEmissao().toString())).append(',')
          .append(csv(f.getNifCliente())).append(',')
          .append(csv(f.getIdVenda())).append(',')
          .append(csv(f.getHash())).append(',')
          .append(csv(f.getHashControl())).append('\n');
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String exportarSaftPt(LocalDate dataInicio, LocalDate dataFim) {
        if (dataInicio == null || dataFim == null || dataFim.isBefore(dataInicio)) {
            throw new IllegalArgumentException("Período SAF-T inválido.");
        }

        List<Fatura> noPeriodo = getFaturas().stream()
                .filter(f -> {
                    LocalDate data = f.getDataEmissao().toLocalDate();
                    return !data.isBefore(dataInicio) && !data.isAfter(dataFim);
                })
                .sorted(Comparator.comparing(Fatura::getDataEmissao))
                .toList();

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<AuditFile xmlns=\"urn:OECD:StandardAuditFile-Tax:PT_1.04_01\">\n");
        sb.append("  <Header>\n");
        sb.append("    <AuditFileVersion>1.04_01</AuditFileVersion>\n");
        sb.append("    <CompanyID>TAKI-RETAIL</CompanyID>\n");
        sb.append("    <TaxRegistrationNumber>500000000</TaxRegistrationNumber>\n");
        sb.append("    <TaxAccountingBasis>F</TaxAccountingBasis>\n");
        sb.append("    <CompanyName>Taki Retail Franchise</CompanyName>\n");
        sb.append("    <BusinessName>Taki</BusinessName>\n");
        sb.append("    <TaxYear>").append(dataInicio.getYear()).append("</TaxYear>\n");
        sb.append("    <StartDate>").append(dataInicio).append("</StartDate>\n");
        sb.append("    <EndDate>").append(dataFim).append("</EndDate>\n");
        sb.append("    <CurrencyCode>EUR</CurrencyCode>\n");
        sb.append("    <DateCreated>").append(LocalDate.now()).append("</DateCreated>\n");
        sb.append("    <TaxEntity>Global</TaxEntity>\n");
        sb.append("    <ProductCompanyID>Taki-Soft-LI4</ProductCompanyID>\n");
        sb.append("    <SoftwareCertificateNumber>1234/AT</SoftwareCertificateNumber>\n");
        sb.append("  </Header>\n");
        
        sb.append("  <SourceDocuments>\n");
        sb.append("    <SalesInvoices>\n");
        sb.append("      <NumberOfEntries>").append(noPeriodo.size()).append("</NumberOfEntries>\n");
        
        double totalCredit = noPeriodo.stream()
                .map(f -> vendaDAO.findById(f.getIdVenda()).orElse(null))
                .filter(Objects::nonNull)
                .mapToDouble(Venda::getTotal)
                .sum();
        sb.append("      <TotalDebit>0.00</TotalDebit>\n");
        sb.append("      <TotalCredit>").append(String.format(Locale.ROOT, "%.2f", totalCredit)).append("</TotalCredit>\n");

        for (Fatura f : noPeriodo) {
            sb.append("      <Invoice>\n");
            sb.append("        <InvoiceNo>").append(escapeXml(f.getNumFatura())).append("</InvoiceNo>\n");
            sb.append("        <InvoiceStatus>N</InvoiceStatus>\n");
            sb.append("        <Hash>").append(escapeXml(f.getHash())).append("</Hash>\n");
            sb.append("        <InvoiceDate>").append(f.getDataEmissao().toLocalDate()).append("</InvoiceDate>\n");
            sb.append("        <InvoiceType>FT</InvoiceType>\n");
            sb.append("        <SystemEntryDate>").append(f.getDataEmissao()).append("</SystemEntryDate>\n");
            sb.append("        <CustomerTaxID>").append(escapeXml(f.getNifCliente() == null ? "999999990" : f.getNifCliente())).append("</CustomerTaxID>\n");
            sb.append("      </Invoice>\n");
        }

        sb.append("    </SalesInvoices>\n");
        sb.append("  </SourceDocuments>\n");
        sb.append("</AuditFile>");
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] gerarFaturaPDF(String numFatura) {
        Fatura f = getFatura(numFatura);
        if (f == null) {
            throw new NotFoundResponse("Fatura não encontrada: " + numFatura);
        }

        Document document = new Document(PageSize.A6);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font fontNegrito = new Font(Font.HELVETICA, 12, Font.BOLD);
            Font fontNormal = new Font(Font.HELVETICA, 10, Font.NORMAL);
            Font fontTabelaCabecalho = new Font(Font.HELVETICA, 8, Font.BOLD);
            Font fontTabelaCelula = new Font(Font.HELVETICA, 8, Font.NORMAL);

            Paragraph pTitulo = new Paragraph("TAKI RETAIL", fontNegrito);
            pTitulo.setAlignment(Element.ALIGN_CENTER);
            document.add(pTitulo);

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Documento: " + f.getNumFatura(), fontNormal));
            document.add(new Paragraph("Data: " + f.getDataEmissao().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), fontNormal));
            document.add(new Paragraph("NIF Cliente: " + (f.getNifCliente() != null ? f.getNifCliente() : "Consumidor Final"), fontNormal));

            document.add(new Paragraph(" "));

            Venda venda = f.getIdVenda() == null ? null : vendaDAO.findById(f.getIdVenda()).orElse(null);
            List<LinhaVenda> linhas = venda != null && venda.getLinhas() != null ? venda.getLinhas() : List.of();

            PdfPTable tabela = new PdfPTable(4);
            tabela.setWidthPercentage(100);
            tabela.setWidths(new float[]{4, 1, 2, 2});

            for (String cabecalho : new String[]{"Produto", "Qtd", "Preço", "Total"}) {
                PdfPCell celula = new PdfPCell(new Phrase(cabecalho, fontTabelaCabecalho));
                celula.setHorizontalAlignment(Element.ALIGN_CENTER);
                celula.setPadding(3);
                tabela.addCell(celula);
            }

            for (LinhaVenda linha : linhas) {
                String nomeProduto = linha.getProduto() != null && linha.getProduto().getNome() != null
                        ? linha.getProduto().getNome()
                        : "—";
                double qtdLinha = linha.getQuantidade();
                double totalLinha = linha.getTotalFinal();
                double precoUnitarioComIva = qtdLinha > 0 ? totalLinha / qtdLinha : 0.0;

                tabela.addCell(new PdfPCell(new Phrase(nomeProduto, fontTabelaCelula)));
                PdfPCell qtd = new PdfPCell(new Phrase(String.valueOf((int) qtdLinha), fontTabelaCelula));
                qtd.setHorizontalAlignment(Element.ALIGN_RIGHT);
                tabela.addCell(qtd);
                PdfPCell preco = new PdfPCell(new Phrase(String.format(Locale.ROOT, "%.2f€", precoUnitarioComIva), fontTabelaCelula));
                preco.setHorizontalAlignment(Element.ALIGN_RIGHT);
                tabela.addCell(preco);
                PdfPCell total = new PdfPCell(new Phrase(String.format(Locale.ROOT, "%.2f€", totalLinha), fontTabelaCelula));
                total.setHorizontalAlignment(Element.ALIGN_RIGHT);
                tabela.addCell(total);
            }
            document.add(tabela);

            if (venda != null) {
                java.util.Map<String, Double> ivaPorTaxa = new java.util.TreeMap<>();
                double subtotalSemIva = 0.0;
                double totalIva = 0.0;
                for (LinhaVenda l : linhas) {
                    double taxa = 0.0;
                    if (l.getProduto() != null && l.getProduto().getTaxaIva() != null) {
                        taxa = l.getProduto().getTaxaIva().getValor();
                    }
                    double lineTotal = l.getTotalFinal();
                    double lineSub = taxa > 0 ? lineTotal / (1 + taxa) : lineTotal;
                    double lineIva = lineTotal - lineSub;
                    subtotalSemIva += lineSub;
                    totalIva += lineIva;
                    if (taxa > 0) {
                        String key = String.format(Locale.ROOT, "%.0f%%", taxa * 100);
                        ivaPorTaxa.merge(key, lineIva, Double::sum);
                    }
                }

                Paragraph pSub = new Paragraph(String.format(Locale.ROOT, "Subtotal: %.2f€", subtotalSemIva), fontNormal);
                pSub.setAlignment(Element.ALIGN_RIGHT);
                document.add(pSub);

                if (!ivaPorTaxa.isEmpty()) {
                    for (java.util.Map.Entry<String, Double> e : ivaPorTaxa.entrySet()) {
                        Paragraph pIva = new Paragraph(String.format(Locale.ROOT, "IVA %s: %.2f€", e.getKey(), e.getValue()), fontNormal);
                        pIva.setAlignment(Element.ALIGN_RIGHT);
                        document.add(pIva);
                    }
                } else if (totalIva > 0) {
                    Paragraph pIva = new Paragraph(String.format(Locale.ROOT, "IVA: %.2f€", totalIva), fontNormal);
                    pIva.setAlignment(Element.ALIGN_RIGHT);
                    document.add(pIva);
                }

                Paragraph pTotal = new Paragraph(String.format(Locale.ROOT, "Total: %.2f€", venda.getTotal()), fontNegrito);
                pTotal.setAlignment(Element.ALIGN_RIGHT);
                document.add(pTotal);
            }

            document.add(new Paragraph(" "));

            Paragraph pFooter = new Paragraph("Obrigado pela sua preferência!", fontNormal);
            pFooter.setAlignment(Element.ALIGN_CENTER);
            document.add(pFooter);

            document.close(); // Close the document here
            return out.toByteArray(); // Return the byte array if successful

        } catch (DocumentException e) {
            System.err.println("Erro ao gerar PDF da fatura " + numFatura + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Falha interna ao gerar PDF da fatura. Detalhes: " + e.getMessage(), e);
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validarIntegridade() {
        String hashAnterior = "";
        for (Fatura f : getFaturas()) {
            if (f.getHash() == null || f.getHash().isBlank()) {
                return false;
            }
            String payload = construirPayloadAssinatura(f, hashAnterior);
            try {
                String hashEsperado = gerarAssinatura(payload);
                if (!hashEsperado.equals(f.getHash())) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
            hashAnterior = f.getHash();
        }
        return true;
    }

    /**
     * Constrói a string de dados que será assinada, incluindo a hash da fatura anterior.
     * 
     * @param fatura a fatura atual
     * @param hashAnterior a hash da fatura imediatamente anterior
     * @return o payload formatado para assinatura
     */
    private String construirPayloadAssinatura(Fatura fatura, String hashAnterior) {
        return String.format("%s;%s;%s;%s",
                fatura.getDataEmissao().toLocalDate().toString(),
                fatura.getDataEmissao().toString(),
                fatura.getNumFatura(),
                hashAnterior != null ? hashAnterior : "");
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

    /**
     * Formata um valor para inclusão num ficheiro CSV, adicionando aspas e escapando aspas internas.
     * 
     * @param valor o valor a formatar
     * @return o valor formatado para CSV
     */
    private String csv(String valor) {
        if (valor == null) return "";
        String normalizado = valor.replace("\"", "\"\"");
        return "\"" + normalizado + "\"";
    }

    /**
     * Escapa caracteres especiais para formato XML (SAF-T).
     * 
     * @param valor o valor a escapar
     * @return o valor escapado para XML
     */
    private String escapeXml(String valor) {
        if (valor == null) return "";
        return valor
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
