# Problema dos baldes
# dois baldes  a-> 4 litros e b -> 3 litros
# começar do estado (0,0)  e chegar a (2,0)
# estados possiveis-> tuplos com qualquer combinaçao (x,y) pertencendo  x e y {0,1,2,3,4}

# ações
# encher totalmente um balde
# esvaziar totalmente um balde
# despejar um balde no outro ate que o ultimo fique cheio
# despejar um balde no outro ate que o primeiro fique vazio


# Utilizar a classe grafo para resolver o problema
# Necessário modelar o problema como um grafo


from nodo import Node
from Graph import Grafo
from queue import Queue

class Balde():


    # start deve ser a capacidade dos dois baldes no inicio ex (0,0) "estado inicial"
    # goal deve ser o objectivo ex (2,0).  " estado final"
    # os estados são representados por "(x,y)" como string, em que x e y representam
    # as quantidades de agua nos jarros

    def __init__(self, start="(0,0)", goal="(2,0)", cap1=4, cap2=3):
        self.g=Grafo(directed=True)
        self.start=start
        self.goal=goal
        self.balde1=cap1   # capacidade do balde 1
        self.balde2=cap2   # capacidade do balde 2


    # Partindo do estado inicial, utilizando as ações possíveis como transições
    # construir o grafo
    def cria_grafo(self):
        self.start = (int(self.start[1]), int(self.start[3]))

        queue = [self.start]
        visited = [self.start]

        while len(queue) > 0:
            cur = queue.pop();
            for node in self.expande(cur):
                self.g.add_edge(cur, node, 1)

                if node not in visited:
                    queue.append(node)
                    visited.append(node)

    # Dado um estado, expande para outros mediante as açoes possiveis
    def expande(self,estado):
        cap1, cap2 = estado
        ans = []

        # Encher um balde
        if cap1 < self.balde1:
            ans.append(self.enche1(estado))
        if cap2 < self.balde2:
            ans.append(self.enche2(estado))

        # Esvaziar um balde
        if cap1 > 0:
            ans.append(self.esvazia1(estado))
        if cap2 > 0:
            ans.append(self.esvazia2(estado))

        # Transferir de um balde para outro
        if cap1 > 0 and cap2 < self.balde2:
            ans.append(self.despeja12(estado))
        if cap2 > 0 and cap1 < self.balde1:
            ans.append(self.despeja21(estado))

        return ans


    # Devolve o estado resultante de esvaziar o primeiro balde
    def esvazia1(self, nodo):
        return 0, nodo[1]

    # Devolve o estado resultante de esvaziar o segundo balde
    def esvazia2(self, nodo):
        return nodo[0], 0

    # Devolve o estado resultante de encher totalmente o primeiro balde da torneira
    def enche1(self, nodo):
        return self.balde1, nodo[1]

    # Devolve o estado resultante de encher totalmente o segundo balde da torneira
    def enche2(self, nodo):
        return nodo[0], self.balde2

    # Devolve o estado resultante de despejar o balde 1 no balde 2
    def despeja12(self, nodo):
        cap1, cap2 = nodo
        remain_cap2 = self.balde2 - cap2
        qtd_move = min(cap1, remain_cap2)

        return cap1 - qtd_move, cap2 + qtd_move

    # Devolve o estado resultante de despejar o balde 2 no balde 1
    def despeja21(self, nodo):
        cap1, cap2 = nodo
        remain_cap1 = self.balde1 - cap1
        qtd_move = min(cap2, remain_cap1)

        return cap1 + qtd_move, cap2 - qtd_move

    # Encontra a solução utilizando DFS (recorre à classe grafo e node implementada antes
    def solucaoDFS(self,start,goal):
        start = (int(start[1]), int(start[3]))
        goal = (int(goal[1]), int(goal[3]))
        res=self.g.procura_DFS(start,goal,path=[], visited=set())
        return (res)

    # Encontra a solução utilizando BFS (recorre à classe grafo e node implementada antes
    def solucaoBFS(self,start,goal):
        start = (int(start[1]), int(start[3]))
        goal = (int(goal[1]), int(goal[3]))
        return self.g.procura_BFS(start,goal)

    ##################################################################################
    # Dados dois estados e1 e e2, devolve a ação que origina a transição de e1 para e2
    ##################################################################################
    def mostraA(self,e1,e2):
        e1_x, e1_y = e1
        e2_x, e2_y = e2

        if e1_x> 0 and e2_x==0 and e1_y==e2_y:
            # Despejar balde1
            return "Despejar balde 1"
        elif e1_y> 0 and e2_y==0 and e1_x==e2_x:
            # Despejar balde2
            return "Despejar balde2"
        elif e1_y==e2_y and e1_x<e2_x:
            # Encher balde 1
            return "Encher balde 1"
        elif e1_x==e2_x and e1_y < e2_y:
            # Encher balde 2
            return "Encher balde 2"
        elif e1_x > e2_x and e2_y > e2_x:
             # Despejar balde 1 no balde 2
            return "Despejar balde 1 no balde 2"
        elif e2_x > e1_x and e1_x < e2_x:
             # Despejar balde 2 no balde 1
            return "Despejar balde 2 no balde 1"

    ########################################################
    # Imprimir sequência de ações para um caminho encontrado
    ########################################################
    def imprimeA(self,caminho):
        lista_acoes=[]

        i=0
        while i+1 < len(caminho):
            lista_acoes.append(self.mostraA(caminho[i], caminho[i+1]))
            i=i+1
        return lista_acoes
