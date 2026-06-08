package pt.uminho.taki.api.global.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes unitários para garantir a correta aplicação das validações JSR-380 (Bean Validation)
 * nos DTOs da API Global.
 * 
 * @author TakiLN Team
 */
class GlobalDtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    // --- FuncionarioRequestDto ---

    @Test
    void funcionarioRequestDto_objetoValido_zeroViolacoes() {
        FuncionarioRequestDto dto = new FuncionarioRequestDto();
        dto.setNome("João");
        dto.setEmail("joao@taki.pt");
        dto.setIdPerfilAcesso("GERENTE");
        dto.setIdLoja(1);

        Set<ConstraintViolation<FuncionarioRequestDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Um objeto válido não deve gerar violações");
    }

    @Test
    void funcionarioRequestDto_nomeEmBranco_violaNome() {
        FuncionarioRequestDto dto = new FuncionarioRequestDto();
        dto.setNome(""); // blank
        dto.setEmail("joao@taki.pt");
        dto.setIdPerfilAcesso("GERENTE");
        dto.setIdLoja(1);

        Set<ConstraintViolation<FuncionarioRequestDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        boolean violouNome = violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("nome"));
        assertTrue(violouNome, "Deve reportar violação no campo nome");
    }

    @Test
    void funcionarioRequestDto_emailMalformado_violaEmail() {
        FuncionarioRequestDto dto = new FuncionarioRequestDto();
        dto.setNome("João");
        dto.setEmail("notanemail");
        dto.setIdPerfilAcesso("GERENTE");
        dto.setIdLoja(1);

        Set<ConstraintViolation<FuncionarioRequestDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        boolean violouEmail = violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email"));
        assertTrue(violouEmail, "Deve reportar violação no campo email");
    }

    @Test
    void funcionarioRequestDto_emailNulo_violaEmail() {
        FuncionarioRequestDto dto = new FuncionarioRequestDto();
        dto.setNome("João");
        dto.setEmail(null);
        dto.setIdPerfilAcesso("GERENTE");
        dto.setIdLoja(1);

        Set<ConstraintViolation<FuncionarioRequestDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        boolean violouEmail = violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email"));
        assertTrue(violouEmail, "Deve reportar violação no campo email");
    }

    // --- ProdutoRequestDto ---

    @Test
    void produtoRequestDto_objetoValido_zeroViolacoes() {
        ProdutoRequestDto dto = new ProdutoRequestDto();
        dto.setCodigoBarras("123456789");
        dto.setNome("Produto Teste");
        dto.setDescricao("Descricao Teste");
        dto.setPrecoCusto(10.0);
        dto.setPrecoVenda(20.0);
        dto.setTaxaIva("NORMAL_23");
        dto.setUnidadeMedida("UN");

        Set<ConstraintViolation<ProdutoRequestDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Um objeto válido não deve gerar violações");
    }

    @Test
    void produtoRequestDto_nomeEmBranco_violaNome() {
        ProdutoRequestDto dto = new ProdutoRequestDto();
        dto.setCodigoBarras("123456789");
        dto.setNome(""); // blank
        dto.setDescricao("Descricao Teste");
        dto.setPrecoCusto(10.0);
        dto.setPrecoVenda(20.0);
        dto.setTaxaIva("NORMAL_23");
        dto.setUnidadeMedida("UN");

        Set<ConstraintViolation<ProdutoRequestDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        boolean violouNome = violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("nome"));
        assertTrue(violouNome, "Deve reportar violação no campo nome");
    }

    @Test
    void produtoRequestDto_precoNegativo_violaPreco() {
        ProdutoRequestDto dto = new ProdutoRequestDto();
        dto.setCodigoBarras("123456789");
        dto.setNome("Produto Teste");
        dto.setDescricao("Descricao Teste");
        dto.setPrecoCusto(-5.0); // negative
        dto.setPrecoVenda(-10.0); // negative
        dto.setTaxaIva("NORMAL_23");
        dto.setUnidadeMedida("UN");

        Set<ConstraintViolation<ProdutoRequestDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        boolean violouPrecoCusto = violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("precoCusto"));
        boolean violouPrecoVenda = violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("precoVenda"));
        assertTrue(violouPrecoCusto || violouPrecoVenda, "Deve reportar violação no precoCusto ou precoVenda");
    }

    // --- FornecedorRequestDto ---

    @Test
    void fornecedorRequestDto_objetoValido_zeroViolacoes() {
        FornecedorRequestDto dto = new FornecedorRequestDto();
        dto.setNome("Fornecedor Teste");
        dto.setNif("123456789");
        dto.setTelefone("910000000");
        dto.setEmail("fornecedor@teste.pt");

        Set<ConstraintViolation<FornecedorRequestDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Um objeto válido não deve gerar violações");
    }

    @Test
    void fornecedorRequestDto_nomeEmBranco_violaNome() {
        FornecedorRequestDto dto = new FornecedorRequestDto();
        dto.setNome(""); // blank
        dto.setNif("123456789");
        dto.setTelefone("910000000");
        dto.setEmail("fornecedor@teste.pt");

        Set<ConstraintViolation<FornecedorRequestDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        boolean violouNome = violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("nome"));
        assertTrue(violouNome, "Deve reportar violação no campo nome");
    }

    // --- CategoriaRequestDto ---

    @Test
    void categoriaRequestDto_objetoValido_zeroViolacoes() {
        CategoriaRequestDto dto = new CategoriaRequestDto();
        dto.setDesignacao("Categoria Teste"); // field is designacao, mapped to "nome" in requirements

        Set<ConstraintViolation<CategoriaRequestDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Um objeto válido não deve gerar violações");
    }

    @Test
    void categoriaRequestDto_nomeEmBranco_violaNome() {
        CategoriaRequestDto dto = new CategoriaRequestDto();
        dto.setDesignacao(""); // blank

        Set<ConstraintViolation<CategoriaRequestDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        boolean violouDesignacao = violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("designacao"));
        assertTrue(violouDesignacao, "Deve reportar violação no campo designacao (nome)");
    }
}
