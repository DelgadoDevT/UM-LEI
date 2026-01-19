import numpy as np
import numpy.random as npr


# funcao objetivo
def objectivo(v):
    x, y = v
    return -20.0 * np.exp(-0.2 * np.sqrt(0.5 * (x ** 2 + y ** 2))) - np.exp(
        0.5 * (np.cos(2 * np.pi * x) + np.cos(2 * np.pi * y))) + np.e + 20


# algoritmo simulated annealing
def simulated_annealing(objectivo, limites, numero_iteracoes, tamanho_passo, temperatura_inicial):
    # gera o nosso ponto inicial como um ponto aleatório dentro dos limites do problema
    melhor = limites[:, 0] + npr.rand(len(limites)) * (limites[:, 1] - limites[:, 0])
    # avalia o ponto inicial usando a função objetivo
    melhor_avaliacao = objectivo(melhor)
    # guardamos a solução atual de forma a poder comparar com as próximas solucoes
    atual, atua_avaliacao = melhor_avaliacao, melhor_avaliacao
    #definos uma lista de scores
    resultados = list()
    # ciclo para correr o algortimos em cada uma das iterações
    for i in range(numero_iteracoes):
        # gera uma nova solução a partir da solução atual, multiplancado pelo tamanho do passo
        candidato = atual + npr.randn(len(limites)) * tamanho_passo
        # avaliamos a nova solução
        candidato_avaliacao = objectivo(candidato)
        # verificar se a avaliação do novo ponto e que o melhor ponto
        # caso seja substituir o melhor ponto pelo novo ponto.
        if candidato_avaliacao < melhor_avaliacao:
            melhor, melhor_avaliacao = candidato, candidato_avaliacao
            # guarda na lista de scores
            resultados.append(melhor_avaliacao)
            print('> Iteracao: %d, f(%s) = %.5f' % (i, melhor, melhor_avaliacao))
        #calcula a diferença entre a avaliação do candidato e do ponto atual
        diferenca = candidato_avaliacao - atua_avaliacao
        # calcular a tempaatura para a respetiva iteracao tendo em conta a temperatura inicial
        t = temperatura_inicial / float(i + 1)
        # calcular a probabilidade de aceitação.
        probabilidade_aceitacao = np.exp(-diferenca / t)
        # aceitamos o novo ponto como a solução atual se ele tiver uma melhor avaliação da função objetivo (a diferença é negativa)
        # ou se a função objetivo for pior, mas decidirmos aceitá-la probabilisticamente.
        if diferenca < 0 or npr.rand() < probabilidade_aceitacao:
            # guardar o novo ponto atual
            atual, atua_avaliacao = candidato, candidato_avaliacao
            print('Iteracacao: >%d,  f(%s) = %.5f' % (i, atual, atua_avaliacao))
    return [melhor, melhor_avaliacao, resultados]


# definimos uma seed por questãoes e reprodução de código , de forma a obter os mesmos resultados
npr.seed(1)
# Os limites serão uma matriz 2D com uma dimensão para cada variável de entrada que define o mínimo e o máximo para a variável.
limites = np.asarray([[-5.0, 5.0], [-5.0, 5.0]])
numero_iteracoes = 500
# maximo tamanho do passo
tamanho_passo = 0.1
temperatura_inicial = 10

melhor, resultado, resultados = simulated_annealing(objectivo, limites, numero_iteracoes, tamanho_passo, temperatura_inicial)
print('Fim!')
print('f(%s) = %f' % (melhor, resultado))
