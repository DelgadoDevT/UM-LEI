import numpy as np
import numpy.random as npr

#funcao objetivo
def objectivo(v):
    x, y = v
    return -20.0 * np.exp(-0.2 * np.sqrt(0.5 * (x ** 2 + y ** 2))) - np.exp(
        0.5 * (np.cos(2 * np.pi * x) + np.cos(2 * np.pi * y))) + np.e + 20


# verifica se um ponto esta dentro dos limites
def dentro_limites(ponto, limites):
    # percorre todas as dimensões do ponto
    for i in range(len(limites)):
        # verifica se está fora dos limites para esta dimensão
        if ponto[i] < limites[i, 0] or ponto[i] > limites[i, 1]:
            return False
    return True


# algortimo hill clibing
def hillclimbing(objectivo, limites,numero_iteracoes, tamanho_passo, ponto_inicial):
    # guarda o ponto inicial
    ponto = ponto_inicial
    # avalia o ponto
    ponto_avaliacao = objectivo(ponto)
    #ciclo para o número de iterações
    for i in range(numero_iteracoes):
        candidato = None
        while candidato is None or not dentro_limites(candidato, limites):
            candidato = ponto + npr.randn(len(limites)) * tamanho_passo
        # avaliacao o ponto candidato
        candidato_avalicao = objectivo(candidato)
        #  verificar se avaliacao do candidato e melhor que a do ponto atual
        if candidato_avalicao <= ponto_avaliacao:
            # guarda o novo ponto
            ponto, ponto_avaliacao = candidato, candidato_avalicao
    return [ponto, ponto_avaliacao]


# algoritmo hill clibing random restart
def random_restarts(objectivo, limites, numero_iteracoes, tamanho_passo, numero_restarts):
    melhor, melhor_avalicao = None, 1e+10
    # Para cada restart
    for i in range(numero_restarts):
        # gera o ponto inicial para a busca
        ponto_inicial = None
        while ponto_inicial is None or not dentro_limites(ponto_inicial, limites):
            ponto_inicial = limites[:, 0] + npr.rand(len(limites)) * (limites[:, 1] - limites[:, 0])
        # algortimo hill climbing para aquele ponto
        ponto, ponto_avalicao = hillclimbing(objectivo, limites, numero_iteracoes, tamanho_passo, ponto_inicial)
        # verifica se e melhor
        if ponto_avalicao < melhor_avalicao:
            melhor, melhor_avalicao = ponto, ponto_avalicao
            print('Restart:  %d, melhor: f(%s) = %.5f' % (i, melhor, melhor_avalicao))
    return [melhor, melhor_avalicao]


# definimos uma seed por questãoes e reprodução de código , de forma a obter os mesmos resultados
npr.seed(1)
# Os limites serão uma matriz 2D com uma dimensão para cada variável de entrada que define o mínimo e o máximo para a variável.
limites = np.asarray([[-5.0, 5.0], [-5.0, 5.0]])
# numero de iteracoes
numero_iteracoes = 500
# maximo tamanho do passo
tamanho_passo = 0.1
# total de restarts
n_restarts = 20
best, score = random_restarts(objectivo, limites, numero_iteracoes, tamanho_passo, n_restarts)
print('Done!')
print('f(%s) = %f' % (best, score))
