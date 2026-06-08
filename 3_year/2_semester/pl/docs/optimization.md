# Fase de Otimização (Pessoa B)

## Introdução

A fase de otimização transforma a AST **depois da análise semântica** e **antes da geração de código**.  
Está implementada em `src/optimizer.py` e é invocada no pipeline principal através de `optimize_ast(...)`.

## Passes implementadas

### 1. Constant Folding

**Descrição:** avalia em compilação expressões cujos operandos já são literais.

**Deteta e transforma:**
- Aritmética: `+`, `-`, `*`, `/`, `**`
- Lógica: `AND`, `OR`, `NOT`, `EQV`, `NEQV`
- Relacional: `EQ`, `NE`, `LT`, `LE`, `GT`, `GE`
- Unário: `+`, `-`, `NOT`

**Exemplos (Fortran 77):**
- Antes: `A = 3 + 4 * 2`  
  Depois: `A = 11`
- Antes: `B = 2 ** 3`  
  Depois: `B = 8`
- Antes: `C = .TRUE. .AND. .FALSE.`  
  Depois: `C = .FALSE.`
- Antes: `D = .NOT. .FALSE.`  
  Depois: `D = .TRUE.`
- Antes: `E = 3 .GT. 2`  
  Depois: `E = .TRUE.`

**Implementação em `src/optimizer.py`:**
- `ASTOptimizer._simplify_expr`
- `_fold_unary`
- `_fold_binary`
- `_normalize_operator`

### 2. Algebraic Simplification

**Descrição:** aplica identidades algébricas e elementos absorventes para reduzir expressões sem alterar semântica.

**Deteta e transforma:**
- `x + 0 -> x`
- `0 + x -> x`
- `x - 0 -> x`
- `x * 1 -> x`
- `1 * x -> x`
- `x * 0 -> 0`
- `0 * x -> 0`
- `x / 1 -> x`
- Identidades booleanas com `.TRUE.` / `.FALSE.` para `AND` e `OR`

**Exemplos (Fortran 77):**
- Antes: `S = X + 0`  
  Depois: `S = X`
- Antes: `P = 1 * X`  
  Depois: `P = X`
- Antes: `Z = X * 0`  
  Depois: `Z = 0`
- Antes: `Q = FLAG .AND. .TRUE.`  
  Depois: `Q = FLAG`
- Antes: `R = FLAG .OR. .FALSE.`  
  Depois: `R = FLAG`

**Implementação em `src/optimizer.py`:**
- `ASTOptimizer._simplify_expr`
- `_algebraic_simplify`
- `_is_zero`, `_is_one`, `_logical_is`

### 3. Dead Code Elimination

**Descrição:** remove ramos e instruções estaticamente inalcançáveis.

**Deteta e transforma:**
- `IF(.FALSE.) THEN ... ENDIF` -> remove bloco
- `IF(.TRUE.) THEN ... ELSE ... ENDIF` -> mantém só ramo `THEN`
- Instruções após `RETURN` -> removidas
- Instruções após `STOP` (`("stop",)`) -> removidas
- Aplicação recursiva em blocos aninhados (`IF`/`DO`)

**Exemplos (Fortran 77):**
- Antes:
  ```fortran
        IF (.FALSE.) THEN
          A = 1
        ENDIF
  ```
  Depois: *(bloco removido)*
- Antes:
  ```fortran
        IF (.TRUE.) THEN
          B = 2
        ELSE
          B = 3
        ENDIF
  ```
  Depois:
  ```fortran
        B = 2
  ```
- Antes:
  ```fortran
        RETURN
        X = 10
  ```
  Depois:
  ```fortran
        RETURN
  ```

**Implementação em `src/optimizer.py`:**
- `ASTOptimizer._optimize_statement_list`
- `ASTOptimizer._optimize_statement`
- `_is_terminator`
- `_logical_literal_value`

### 4. Jump Optimization

**Descrição:** simplifica controlo de fluxo com `GOTO` sem alterar labels estruturais de `DO`.

**Deteta e transforma:**
- Colapso transitivo de cadeias `GOTO` (`GOTO L1`, `L1: GOTO L2` -> `GOTO L2`)
- Remoção de `GOTO` redundante para o próximo statement rotulado (fall-through)
- Preservação de labels de `DoNode.label` (nunca eliminadas/reescritas)

**Exemplos (Fortran 77):**
- Antes:
  ```fortran
        GOTO 10
   10   GOTO 20
   20   CONTINUE
  ```
  Depois:
  ```fortran
        GOTO 20
   10   GOTO 20
   20   CONTINUE
  ```
  A label `10` é preservada porque pode ser alvo de outros `GOTO` não visíveis neste excerto, e eliminá-la poderia quebrar a correção do fluxo de controlo.
- Antes:
  ```fortran
        GOTO 30
   30   CONTINUE
  ```
  Depois:
  ```fortran
   30   CONTINUE
  ```

**Implementação em `src/optimizer.py`:**
- `ASTOptimizer._jump_optimize_scope`
- `ASTOptimizer._jump_optimize_statement_list`
- `ASTOptimizer._resolve_goto_chain`
- `ASTOptimizer._collect_do_labels`
- `_get_goto_target`, `_set_goto_target`, `_next_labeled_statement`

## O que não é otimizado (intencionalmente)

- **Sem propagação segura de constantes:** foi removida para evitar complexidade adicional e riscos com semântica por referência do Fortran 77 em `CALL`/funções.
- **Sem IR intermédio:** a fase mantém-se diretamente sobre AST para não acoplar o projeto a uma IR antes de `codegen` estar finalizado.

## Testes

O ficheiro de testes da otimização é `tests/test_optimizer.py`.

Grupos cobertos:
- folding constante (aritmético, lógico, relacional, unário),
- simplificação algébrica (todas as regras principais),
- eliminação de código morto (`IF` constante, código após `RETURN`/`STOP`, incluindo casos aninhados),
- otimização de saltos (`GOTO` em cadeia e fall-through),
- regressão de preservação de labels de `DO`,
- suporte a formas AST mistas (nós, tuplos, dicionários),
- regressão no-op (sem otimizações aplicáveis).
