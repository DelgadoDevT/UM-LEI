% Filho (i - v)
filho("João", "José").
filho("José", "Manuel").
filho("Carlos", "José").
filho("Filipe", "Paulo").
filho("Maria", "Paulo").

% Sexo (viii - xi)
sexo("João", masculino).
sexo("José", masculino).
sexo("Maria", feminino).
sexo("Joana", feminino).

% xii
pai(P,F):- filho(F,P).

% Avo (vi - vii)
avo("António", "Nádia").
avo("Ana", "Nuno").
% xiii
avo(A,N):- filho(N,X), filho(X,A).

% xiv
neto(N,A):- avo(A,N).

% xv
descendente(X,Y):- filho(X,Y).
descendente(X,Y):- filho(X,P), descendente(P,Y).

% xvi
descendenteG(X,Y,1):- filho(X,Y).
descendenteG(X,Y,G):- filho(X,P), descendenteG(P,Y,G1), G is G1 + 1.

% xvii
avo_por_grau(A,N):- descendenteG(N,A,2).

% xviii
bisavo(X,Y):- descendenteG(Y,X,3).

% xix
trisavo(X,Y):- descendenteG(Y,X,4).

% xx
tetraneto(X,Y):- trisavo(Y,X).