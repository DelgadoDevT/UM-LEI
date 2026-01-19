% Parte I

% i
soma3(A,B,C,R):- R is A + B + C.

% ii 
somaC([],0):- !.
somaC([_|T],R):- somaC(T, R1), R is R1 + 1.

% iii
maxV(X,Y,Y):- X < Y.
maxV(X,Y,X):- X >= Y.

% iv
maxA([],0):- !.
maxA([H|T],M):- maxA(T, M1), (H > M1 -> M = H; M = M1).

% v
somaT([],0):- !.
somaT([H|T],R):- somaT(T, R1), R is R1 + H.

avg([],0):- !.
avg(L,R):- somaT(L,A), somaC(L,B), B > 0, R is A / B.

% vi
ord(L, L_Ord) :- swap(L, L_Aux), !, ord(L_Aux, L_Ord).
ord(L, L).

swap([X, Y | T], [Y, X | T]):- X > Y.
swap([H | T], [H | T1]):- swap(T, T1).

% vii
pares([], []).
pares([H|T],L):- pares(T,L1), (H mod 2 =:= 0 -> L = [H|L1]; L = L1).

% Parte II

% viii
pertence([X|_], X).
pertence([_|T], X) :- pertence(T, X).

% ix
comprimento([],0).
comprimento([_|T],R):- comprimento(T, R1), R is R1 + 1.

% x
diferentes([],0).
diferentes([H|T],D):- pertence(T,H), diferentes(T,D).
diferentes([H|T],D):- not(pertence(T,H)), diferentes(T,D1), D is D1 + 1.

% xi
apaga1([],[],_).
apaga1([H|T],T,H):- !.
apaga1([H|T],L,E):- H \== E, apaga1(T,L1,E), append([H],L1,L).

% xii
apagaT([],[],_).
apagaT([H|T],L,H):- apagaT(T,L,H).
apagaT([H|T],L,E):- H \== E, apagaT(T,L1,E), append([H],L1,L).

% xiii
adicionar(X,L,_):- pertence(L,X), !.
adicionar(X,L,R):- append(L,[X],R).

% xiv
concatenar([],L2,L2).
concatenar(L1,[],L1).
concatenar(L1,L2,R):- append(L1,L2,R).

% xv
inverter([],[]).
inverter([H|T],R):- inverter(T,R1), append(R1,[H],R).

% xvi
prefixo([], _).
prefixo([H|T], [H|XS]) :- prefixo(T, XS).

sublista([], _).
sublista([H|T], [H|XS]) :- prefixo(T, XS).
sublista(S, [_|XS]) :- sublista(S, XS).