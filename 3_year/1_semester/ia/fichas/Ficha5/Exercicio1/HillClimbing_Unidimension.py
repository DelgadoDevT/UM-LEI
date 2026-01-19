import numpy as np
import numpy.random as npr
import math

def objectivo(x):
    return math.sin(x[0])

# algortimo hill clibing
def hillclimbing(objectivo, limites, numero_iteracoes, tamanho_passo):
    # gerar o ponto inicial
    ponto_atual = limites[:, 0] + npr.rand(len(limites)) * (limites[:, 1] - limites[:, 0])
    # avaliar o ponto inicial
    ponto_atual_avalicao = objectivo(ponto_atual)
    resultados = list()
    resultados.append(ponto_atual)
    # ciclo para o número de iterações
    for i in range(numero_iteracoes):
        candidato = ponto_atual + npr.randn(len(limites)) * tamanho_passo
        # avaliar o ponto candidato
        candidato_avaliacao = objectivo(candidato)
        # verificar se avaliacao do candidato e melhor que a do ponto atual
        if candidato_avaliacao > ponto_atual_avalicao:
            # guardar o novo ponto
            ponto_atual, ponto_atual_avalicao = candidato, candidato_avaliacao
            # guardaro o ponnto na lista de resultados
            resultados.append(ponto_atual)
            print('Iteracacao: >%d,  f(%s) = %.5f' % (i, ponto_atual, ponto_atual_avalicao))
    return [ponto_atual, ponto_atual_avalicao, resultados]


# definimos uma seed por questãoes e reprodução de código , de forma a obter os mesmos resultados
npr.seed(1)
# Os limites serão uma matriz 2D com uma dimensão para cada variável de entrada que define o mínimo e o máximo para a variável.
limites = np.asarray([[-3.0, 2.0]])
# numero de iteracoes
numero_iteracoes = 500
# maximo tamanho do passo
tamanho_passo = 0.1
# aplica o algortimo Hill Climbing
melhor, avalicao, resultados = hillclimbing(objectivo, limites, numero_iteracoes, tamanho_passo)
print('Fim!')
print('f(%s) = %f' % (melhor, avalicao))