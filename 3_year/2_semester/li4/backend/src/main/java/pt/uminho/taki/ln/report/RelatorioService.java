package pt.uminho.taki.ln.report;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import pt.uminho.taki.ln.inventario.Inventario;
import pt.uminho.taki.ln.lojas.Produto;
import pt.uminho.taki.dao.VendaDAO;
import pt.uminho.taki.dao.FuncionarioDAO;
import pt.uminho.taki.ln.lojas.Funcionario;
import pt.uminho.taki.ln.vendas.Venda;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Serviço responsável pela geração de relatórios em formato PDF.
 * 
 * @author TakiLN Team
 * @since 1.0
 */
public class RelatorioService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final VendaDAO vendaDAO;
    private final FuncionarioDAO funcionarioDAO;

    public RelatorioService() {
        this.vendaDAO = new VendaDAO();
        this.funcionarioDAO = new FuncionarioDAO();
    }

    /**
     * Gera um relatório de vendas em PDF.
     * 
     * @param vendas a lista de vendas a incluir no relatório
     * @param titulo o título do relatório
     * @return o PDF gerado em array de bytes
     */
    public byte[] gerarRelatorioVendasPDF(List<Venda> vendas, String titulo) {
        Document document = new Document(PageSize.A4.rotate());
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Título
            Font fontTitulo = new Font(Font.HELVETICA, 18, Font.BOLD);
            Paragraph pTitulo = new Paragraph(titulo, fontTitulo);
            pTitulo.setAlignment(Element.ALIGN_CENTER);
            pTitulo.setSpacingAfter(20);
            document.add(pTitulo);

            // Tabela
            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2, 3, 2, 2, 2, 2, 2});

            // Cabeçalho
            addTableHeader(table, "ID Venda", "Data/Hora", "Subtotal", "IVA", "Total", "Método", "Operador");

            // Mapa idFuncionario -> nome para resolver operadores
            java.util.Map<String, String> nomesOperadores = new java.util.HashMap<>();
            try {
                for (Funcionario f : this.funcionarioDAO.findAll()) {
                    if (f != null && f.getId() != null) {
                        nomesOperadores.put(f.getId(), f.getNome() != null ? f.getNome() : f.getId());
                    }
                }
            } catch (Exception ignored) {
                // se falhar, cai no fallback abreviado do id
            }

            // Dados
            double totalSubtotalGeral = 0;
            double totalIvaGeral = 0;
            double totalGeral = 0;
            java.util.Map<String, Double> ivaPorTaxa = new java.util.TreeMap<>();
            for (Venda v : vendas) {
                double total = v.getTotal();
                double subtotal = v.getSubtotal();
                double iva = v.getImposto();
                if (subtotal == 0 && iva == 0 && v.getLinhas() != null) {
                    for (pt.uminho.taki.ln.vendas.LinhaVenda l : v.getLinhas()) {
                        double taxa = (l.getProduto() != null && l.getProduto().getTaxaIva() != null)
                                ? l.getProduto().getTaxaIva().getValor() : 0.0;
                        double lineTotal = l.getTotalFinal();
                        double lineSub = taxa > 0 ? lineTotal / (1 + taxa) : lineTotal;
                        subtotal += lineSub;
                        iva += (lineTotal - lineSub);
                        if (taxa > 0) {
                            String key = String.format("%.0f%%", taxa * 100);
                            ivaPorTaxa.merge(key, lineTotal - lineSub, Double::sum);
                        }
                    }
                } else if (v.getLinhas() != null) {
                    for (pt.uminho.taki.ln.vendas.LinhaVenda l : v.getLinhas()) {
                        double taxa = (l.getProduto() != null && l.getProduto().getTaxaIva() != null)
                                ? l.getProduto().getTaxaIva().getValor() : 0.0;
                        if (taxa > 0) {
                            double lineTotal = l.getTotalFinal();
                            double lineIva = lineTotal - (lineTotal / (1 + taxa));
                            String key = String.format("%.0f%%", taxa * 100);
                            ivaPorTaxa.merge(key, lineIva, Double::sum);
                        }
                    }
                }

                table.addCell(abreviar(v.getIdVenda(), 8));
                table.addCell(v.getDataHora() != null ? v.getDataHora().format(DATE_FORMATTER) : "");
                table.addCell(String.format("%.2f€", subtotal));
                table.addCell(String.format("%.2f€", iva));
                table.addCell(String.format("%.2f€", total));
                table.addCell(this.vendaDAO.getMetodoPagamento(v.getIdVenda()).orElse("N/A"));
                String nomeOperador = nomesOperadores.getOrDefault(v.getIdFuncionario(), abreviar(v.getIdFuncionario(), 8));
                table.addCell(nomeOperador);

                totalSubtotalGeral += subtotal;
                totalIvaGeral += iva;
                totalGeral += total;
            }

            document.add(table);

            // Resumo
            Font fontResumoNormal = new Font(Font.HELVETICA, 11, Font.NORMAL);
            Font fontResumoBold = new Font(Font.HELVETICA, 12, Font.BOLD);

            Paragraph pSub = new Paragraph(String.format("\nSubtotal (sem IVA): %.2f€", totalSubtotalGeral), fontResumoNormal);
            pSub.setAlignment(Element.ALIGN_RIGHT);
            document.add(pSub);

            if (!ivaPorTaxa.isEmpty()) {
                for (java.util.Map.Entry<String, Double> e : ivaPorTaxa.entrySet()) {
                    Paragraph pIva = new Paragraph(String.format("IVA %s: %.2f€", e.getKey(), e.getValue()), fontResumoNormal);
                    pIva.setAlignment(Element.ALIGN_RIGHT);
                    document.add(pIva);
                }
            } else if (totalIvaGeral > 0) {
                Paragraph pIva = new Paragraph(String.format("IVA Total: %.2f€", totalIvaGeral), fontResumoNormal);
                pIva.setAlignment(Element.ALIGN_RIGHT);
                document.add(pIva);
            }

            Paragraph resumo = new Paragraph(String.format("Total Faturado no Período: %.2f€", totalGeral), fontResumoBold);
            resumo.setAlignment(Element.ALIGN_RIGHT);
            document.add(resumo);

            document.close();
            return out.toByteArray();

        } catch (DocumentException e) {
            System.err.println("Erro ao gerar relatório de vendas: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Falha interna ao gerar Relatório de Vendas PDF. Detalhes: " + e.getMessage(), e);
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }
    }

    /**
     * Gera um relatório de inventário em PDF.
     * 
     * @param itens a lista de itens de inventário
     * @param produtos o mapa de produtos para obter nomes
     * @return o PDF gerado em array de bytes
     */
    public byte[] gerarRelatorioInventarioPDF(List<Inventario> itens, Map<String, Produto> produtos) {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Título
            Font fontTitulo = new Font(Font.HELVETICA, 18, Font.BOLD);
            Paragraph pTitulo = new Paragraph("Relatório de Inventário Completo", fontTitulo);
            pTitulo.setAlignment(Element.ALIGN_CENTER);
            pTitulo.setSpacingAfter(20);
            document.add(pTitulo);

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{4, 2, 2, 2, 2});

            // Cabeçalho
            addTableHeader(table, "Produto", "Loja", "Stock", "Mínimo", "Estado");

            for (Inventario i : itens) {
                Produto p = produtos.get(i.getIdProduto());
                String nomeProd = (p != null) ? p.getNome() : i.getIdProduto();
                
                table.addCell(nomeProd);
                table.addCell(String.valueOf(i.getIdLoja()));
                table.addCell(String.valueOf(i.getQuantidade()));
                table.addCell(String.valueOf(i.getQuantidadeMinima()));
                
                PdfPCell cellEstado = new PdfPCell();
                if (i.getQuantidade() <= i.getQuantidadeMinima()) {
                    cellEstado.setPhrase(new Phrase("CRÍTICO", new Font(Font.HELVETICA, 10, Font.BOLD, Color.RED)));
                } else {
                    cellEstado.setPhrase(new Phrase("OK", new Font(Font.HELVETICA, 10, Font.NORMAL, Color.GREEN.darker())));
                }
                table.addCell(cellEstado);
            }

            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (DocumentException e) {
            System.err.println("Erro ao gerar relatório de inventário: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Falha interna ao gerar Relatório de Inventário PDF. Detalhes: " + e.getMessage(), e);
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }
    }

    private static String abreviar(String valor, int max) {
        if (valor == null) return "";
        if (valor.length() <= max) return valor;
        return valor.substring(0, max) + "...";
    }

    private void addTableHeader(PdfPTable table, String... headers) {
        Font fontHeader = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, fontHeader));
            cell.setBackgroundColor(Color.DARK_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            table.addCell(cell);
        }
    }
}