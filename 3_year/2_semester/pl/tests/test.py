import os
import sys

project_root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
sys.path.append(project_root)

from src.lexer import tokenize
from src.parser import parse
from src.semantic import SemanticAnalyzer
from src.optimizer import optimize_ast
from src.codegen import CodeGenerator

def test_compiler():
    base_dir = os.path.dirname(os.path.abspath(__file__))
    pasta_testes = base_dir
    pasta_esperados = os.path.join(pasta_testes, "expected_outputs")

    ficheiros = sorted([f for f in os.listdir(pasta_testes) if f.endswith('.f')])
    
    if not ficheiros:
        print("Aviso: Nenhum ficheiro .f encontrado na pasta tests.")
        return

    print(f"A iniciar testes para {len(ficheiros)} ficheiros...")

    aprovados = 0
    for nome_f in ficheiros:
        print(f"\n--- Testing {nome_f} ---")
        caminho_f = os.path.join(pasta_testes, nome_f)
        
        try:
            with open(caminho_f, "r", encoding="utf-8") as f:
                data = f.read()

            # Lexer
            tokens = tokenize(data)
            print("  -> LEXER SUCESSO")
            
            # Parsing
            ast_tree = parse(data)
            print("  -> PARSER SUCESSO")
            
            # Semântica
            analyzer = SemanticAnalyzer()
            analyzer.analyze(ast_tree)
            print("  -> SEMÂNTICA SUCESSO")
            
            # Otimização
            optimized_ast, _ = optimize_ast(ast_tree)
            print("  -> OTIMIZAÇÃO SUCESSO")
            
            # Codegen
            generator = CodeGenerator(analyzer.symtab)
            vm_code = generator.generate(optimized_ast)
            
            caminho_vm_esperado = os.path.join(pasta_esperados, nome_f.replace(".f", ".vm"))
            if not os.path.exists(caminho_vm_esperado):
                print(f"  -> AVISO: Ficheiro esperado não encontrado em {caminho_vm_esperado}")
                continue
                
            with open(caminho_vm_esperado, "r", encoding="utf-8") as f_vm_esp:
                vm_code_esperado = f_vm_esp.read()
                
            if vm_code.strip() == vm_code_esperado.strip():
                print("  -> CODEGEN SUCESSO")
                aprovados += 1
            else:
                print(f"  -> FALHA: O código gerado não corresponde ao esperado em {caminho_vm_esperado}")
            
        except Exception as e:
            print(f"  -> ERRO: {e}")

    print(f"\nTestes Concluídos: {aprovados}/{len(ficheiros)} ficheiros com sucesso.")

if __name__ == "__main__":
    test_compiler()
