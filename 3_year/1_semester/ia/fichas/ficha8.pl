%%%%%% Exercício 1 %%%%%%%

% Base de Conhecimento

% aluno(id,nome,sexo)
aluno(1,joao,m).
aluno(2,antonio,m).
aluno(3,carlos,m).
aluno(4,luisa,f).
aluno(5,maria,f).
aluno(6,isabel,f).

% curso(cod,sigla)
curso(1,lei).
curso(2,miei).
curso(3,lcc).

% disciplina(cod,sigla,ano,curso)
disciplina(1,ed,2,1).
disciplina(2,ia,3,1).
disciplina(3,fp,1,2).

% inscrito(aluno,disciplina)
inscrito(1,1).
inscrito(1,2).
inscrito(5,3).
inscrito(5,5).
inscrito(2,5).

% nota(aluno,disciplina,nota)
nota(1,1,15).
nota(1,2,16).
nota(1,5,20).
nota(2,5,10).
nota(3,5,8).

% copia
copia(1,2).
copia(2,3).
copia(3,4).

% i
naoInscritoDisciplina(I):- 
    aluno(I,_,_), \+ inscrito(I,_).

% ii
naoInscritoDisciplinaExistente(I, D):- 
    aluno(I,_,_), \+ inscrito(I,D), disciplina(D,_,_,_).

% iii
somaL([],0).
somaL([H|T],R):- somaL(T,R1), R is R1 + H.

sizeL([],0).
sizeL([_|T],R):- sizeL(T,R1), R is R1 + 1.

avgAluno(A,N):- 
    findall(Nota, nota(A,_,Nota), L),
    L \== [],
    sizeL(L, Len),
    somaL(L, Soma),
    N is Soma / Len.

% iv
mediaGlobal(MG) :-
    findall(N, avgAluno(_, N), ListaNotas),
    somaL(ListaNotas, Soma),
    sizeL(ListaNotas, Len),
    MG is Soma / Len.

acimaMediaGlobal(AC):-
    mediaGlobal(MG),
    findall(I, (aluno(I, _, _), avgAluno(I, N), N > MG), AC).

% v
alunosCopiaram(N):- 
    findall(Nome, (aluno(I,Nome,_), copia(I,_)), L),
    list_to_set(L, N).

% vi
copia_D_ou_Ind(I, X) :- copia(I, X).
copia_D_ou_Ind(I, X) :- 
    copia(I, H), 
    copia_D_ou_Ind(H, X).

alunosCopiaramDI(ID, LN) :- 
    findall(Nome, (copia_D_ou_Ind(I, ID), aluno(I, Nome, _)), LS),
    list_to_set(LS, LN).

% vii
mapToNome([],[]).
mapToNome([H|T],N):-
    mapToNome(T,N1),
    aluno(H,Name,_),
    append([Name],N1,N).

%%%%%% Exercício 2 %%%%%%%

% Base de Conhecimento

%biblioteca(id, nome, localidade)
biblioteca(1, uminhogeral, braga).
biblioteca(2, luciocracveiro, braga).
biblioteca(3, municipal, porto).
biblioteca(4, publica, viana).
biblioteca(5, ajuda, lisboa).
biblioteca(6, cidade, coimbra).

%livros( id, nome, biblioteca)
livros(1, gameofthrones, 1). 
livros(2, codigodavinci, 2).
livros(3, setimoselo, 1).
livros(4, fireblood, 4).
livros(5, harrypotter, 6).
livros(6, senhoradosneis, 7).
livros(7, oalgoritmomestre, 9).

%leitores(id, nome, genero)
leitores(1, pedro, m).
leitores(2, joao, m).
leitores(3, lucia, f).
leitores(4, sofia, f).
leitores(5, patricia, f).
leitores(6, diana, f).

%requisicoes(id_requisicao,id_leitor, id_livro, data(A,M,D)
requisicoes(1,2,3,data(2022,5,17)).
requisicoes(2,1,2,data(2022,7,10)).
requisicoes(3,1,3,data(2021,11,2)).
requisicoes(4,1,4,data(2022,2,1)).
requisicoes(5,5,3,data(2022,4,23)).
requisicoes(6,4,2,data(2021,3,9)).
requisicoes(7,4,1,data(2022,5,5)).
requisicoes(8,2,6,data(2021,7,18)).
requisicoes(9,5,7,data(2022,4,12)).

%devolucoes(id_requisicao, data(A,M, D))
devolucoes(2, data(2022, 7,26)).
devolucoes(4, data(2022,2,4)).
devolucoes(5, data(2022, 6, 13)).
devolucoes(1, data(2022, 5, 23)).
devolucoes(6, data(2022, 4, 9)).

% i
countF(C):- 
    findall(I, (leitores(I,_,S), S == f), I), 
    sizeL(I,C).

% ii
requisitados(L):-
    findall(I, (
        requisicoes(_,_,I,_), 
        livros(I,_,B), 
        \+ biblioteca(B,_,_)), 
        L).

% iii
livrosLeitoresRequisicoesBraga(L):-
    findall(IDLi-IDLe, (
        requisicoes(_,IDLi,IDLe,_), 
        livros(IDLi,_,IDB), 
        biblioteca(IDB,_,braga)), 
        L).

% iv
livrosSemRequisicao(L):-
    findall(IDL, (
        livros(IDL,_,_),
        \+ requisicoes(_,_,IDL,_)),
    L).

% v
livrosRequisitados2022(L):-
    findall(IDL-data(2022, M, D), requisicoes(_,_,IDL,data(2022,M,D)), L).

% vi
leitoresRequisicoesVerao(L):-
    findall(IDL, (
        requisicoes(_,IDL,_,data(_,M,_)),
        M > 6,
        M < 10),
    L).

% vii
apos15Dias(data(A1, M1, D1), data(A2, M2, D2)):- 
    N is (A2 - A1) * 365 + (M2 - M1) * 30 + (D2 - D1), 
    N > 15.

requisicoesAposLimite(L):-
    findall(IDL, (
        requisicoes(IDR,IDL,_,D1),
        devolucoes(IDR,D2),
        apos15Dias(D1,D2)),
    LS),
    list_to_set(LS, L).

