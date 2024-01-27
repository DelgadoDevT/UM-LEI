{-|
Module      : Task4
Description : Updates character speeds in the game
Copyright   : Joao Pedro Delgado Teixeira <a106836@alunos.uminho.pt>
              Ricardo Miguel da Silva Morais <a106935@alunos.uminho.pt>

Module for the realisation of Task4 of LI1 in 2023/24.
-}
module Task4 where

import Data.Maybe

import LI12324

import Task1

import Task2

{-|

The __atualiza__ function must validate the new directions and speeds of the characters (enemies and player) according to the actions given.
To define it, we need to use the following auxiliary functions:

* @atualizaIni@: Function that updates the enemies according to the list of actions given
* @atualizaPer@: Function that updates the player character according to the given action

To define these two auxiliary functions, we use other functions that will be called by them, such as:

* @atualizaAcao@: Function that, given an action, updates a character's data.
* @subEscada@: Function that checks if a character is in the position of a Ladder type block.
* @movDireita@: Function that checks if a character cannot move to the right
* @movEsquerda@: Function that checks if a character cannot move to the left
* @parOp@: Function that checks if a character can be stationary

== Properties:
=== Main function "atualiza":
prop> atualiza :: [Maybe Acao] -> Maybe Acao -> Jogo -> Jogo
prop> atualiza ap aj j@(Jogo m i c p) =
prop>   let 
prop>     ni = atualizaIni m ap i 
prop>     nj = atualizaPer m aj p
prop>   in 
prop>    (Jogo m ni c nj)

==== Auxiliary function  "atualizaIni":
prop> atualizaIni :: Mapa -> [Maybe Acao] -> [Personagem] -> [Personagem]
prop> atualizaIni _ [] i = i
prop> atualizaIni m (ac:acs) i =
prop>   case ac of
prop>     Just ac  -> atualizaIni m acs (map (atualizaAcao m ac) i)
prop>     Nothing  -> 

==== Auxiliary function  "atualizaPer":
prop> atualizaPer :: Mapa -> Maybe Acao -> Personagem -> Personagem
prop> atualizaPer m mac p =
prop>     case mac of 
prop>         Just ac -> atualizaAcao m ac p
prop>         Nothing -> p

==== Auxiliary function "atualizaAcao":
prop> atualizaAcao :: Mapa -> Acao -> Personagem -> Personagem
prop> atualizaAcao m@(Mapa m1 m2 m3) ac p@(Personagem (v1,v2) i2 i3 i4 i5 i6 i7 i8 i9 i10) =
prop>     case ac of
prop>         Subir         -> if subEscada m3 pm then Personagem (v1,v2-1) i2 i3 i4 i5 i6 i7 i8 i9 i10 else p
prop>         Descer        -> if subEscada m3 pm then Personagem (v1,v2+1) i2 i3 i4 i5 i6 i7 i8 i9 i10 else p
prop>         AndarDireita  -> if movDireita m3 pm then Personagem (v1-1,v2) i2 i3 Oeste i5 i6 i7 i8 i9 i10 else Personagem (v1+1,v2) i2 i3 Este i5 i6 i7 i8 i9 i10
prop>         AndarEsquerda -> if movEsquerda m3 pm then Personagem (v1+1,v2) i2 i3 Este i5 i6 i7 i8 i9 i10 else Personagem (v1-1,v2) i2 i3 Oeste i5 i6 i7 i8 i9 i10
prop>        Saltar        -> if not i6 then Personagem (v1,v2-1) i2 i3 i4 i5 i6 i7 i8 i9 i10 else p
prop>         Parar         -> if parOp m3 pm then Personagem (0,0) i2 i3 i4 i5 i6 i7 i8 i9 i10 else p
prop>     where 
prop>       pm = acP p 

==== Auxiliary function "subEscada":
prop> subEscada :: [[Bloco]] -> Personagem -> Bool
prop> subEscada b p@(Personagem i1 i2 i3 i4 i5 s i7 i8 i9 i10)
prop>    | s = True
prop>    | any (== hitPersona p) (hitBlocos (findBlocE b)) = True
prop>    | otherwise = False

==== Auxiliary function "movDireita":
prop> movDireita :: [[Bloco]] -> Personagem -> Bool
prop> movDireita b p@(Personagem i1 i2 (x, y) d i5 i6 r i8 i9 i10)
prop>   | r && (`elem` findBlocP b) (snd (hitPersona p)) = True
prop>   | r && (`elem` findBlocV b) (fst (fst (hitPersona p)) + 1, snd (fst (hitPersona p))) = True
prop>   | otherwise = False

==== Auxiliary function "movEsquerda":
prop> movEsquerda :: [[Bloco]] -> Personagem -> Bool
prop> movEsquerda b p@(Personagem i1 i2 (x, y) d i5 i6 r i8 i9 i10)
prop>   | r && (`elem` findBlocP b) (fst (snd (hitPersona p)) - 2, snd (snd (hitPersona p))) = True
prop>   | r && (`elem` findBlocV b) (fst (fst (hitPersona p)) - 1, snd (fst (hitPersona p))) = True
prop>   | otherwise = False

==== Auxiliary function "parOp":
prop> parOp :: [[Bloco]] -> Personagem -> Bool
prop> parOp b p@(Personagem i1 i2 (x, y) d i5 i6 r i8 i9 i10)
prop>   | (`elem` findBlocP b) (fst (hitPersona p)) = True
prop>   | otherwise = False

== Examples of use:
>>> atualiza ([Just AndarDireita, Just AndarDireita]) ((Just Subir)) (Jogo (Mapa ((1.5, 1.5), Este) (5, 5) [[Escada, Vazio, Vazio], [Vazio, Vazio, Plataforma], [Plataforma, Plataforma, Plataforma]]) ([(Personagem (0, 0) Fantasma (1.5, 1.5) Este (1, 1) False True 1 0 (False, 0)),(Personagem (0, 0) Fantasma (0.5, 1.5) Oeste (1, 1) False False 1 0 (False, 0))]) ([(Moeda,(1.5,1.5)),(Martelo,(1.5,1.5))]) (Personagem (5, 0) Jogador (0.5, 0.5) Oeste (1, 1) True True 2 0 (True, 5)))
Jogo (Mapa ((1.5, 1.5), Este) (5, 5) [[Escada, Vazio, Vazio], [Vazio, Vazio, Plataforma], [Plataforma, Plataforma, Plataforma]]) ([(Personagem (-2, 0) Fantasma (1.5, 1.5) Oeste (1, 1) False True 1 0 (False, 0)),(Personagem (2, 0) Fantasma (0.5, 1.5) Este (1, 1) False False 1 0 (False, 0))]) ([(Moeda,(1.5,1.5)),(Martelo,(1.5,1.5))]) (Personagem (5, -1) Jogador (0.5, 0.5) Oeste (1, 1) True True 2 0 (True, 5))

>>> atualiza ([Nothing]) (Nothing) Jogo (Mapa ((2.5, 1.5), Este) (5, 5) [[Plataforma, Plataforma, Plataforma, Plataforma, Plataforma], [Vazio, Vazio, Vazio, Vazio, Vazio], [Vazio, Alcapao, Vazio, Vazio, Vazio], [Vazio, Vazio, Vazio, Vazio, Vazio], [Plataforma, Plataforma, Plataforma, Plataforma, Plataforma]]) ([(Personagem (1, 0) Fantasma (3.0, 1.5) Oeste (1, 1) False True 1 0 (False, 0)),(Personagem (1, 0) Fantasma (0.5, 1.5) Oeste (1, 1) False True 1 0 (False, 0)),(Personagem (1, 0) Fantasma (2.5, 2.5) Oeste (1, 1) False True 0 0 (False, 0))]) ([(Martelo,(4.5,4.5))]) (Personagem (0, 0) Jogador (0.5, 1.5) Oeste (1, 1) False True 2 0 (True, 5))
Jogo (Mapa ((2.5, 1.5), Este) (5, 5) [[Plataforma, Plataforma, Plataforma, Plataforma, Plataforma], [Vazio, Vazio, Vazio, Vazio, Vazio], [Vazio, Alcapao, Vazio, Vazio, Vazio], [Vazio, Vazio, Vazio, Vazio, Vazio], [Plataforma, Plataforma, Plataforma, Plataforma, Plataforma]]) ([(Personagem (1, 0) Fantasma (3.0, 1.5) Oeste (1, 1) False True 1 0 (False, 0)),(Personagem (1, 0) Fantasma (0.5, 1.5) Oeste (1, 1) False True 1 0 (False, 0)),(Personagem (1, 0) Fantasma (2.5, 2.5) Oeste (1, 1) False True 0 0 (False, 0))]) ([(Martelo,(4.5,4.5))]) (Personagem (0, 0) Jogador (0.5, 1.5) Oeste (1, 1) False True 2 0 (True, 5))

-}

{- |
Function that validates the new directions and speeds of the characters (enemies and player) according to the actions taken.
-}

atualiza :: [Maybe Acao] -- ^ Actions to apply to enemies
         -> Maybe Acao -- ^ Action to be applied to the player
         -> Jogo -- ^ Game
         -> Jogo -- ^ Updated Game
atualiza ap aj j@(Jogo m i c p) =
  let
    ni = atualizaIni m ap i
    nj = atualizaPer m aj p
  in
   Jogo m ni c nj

{- |
Function that updates enemies according to the list of actions given.
-}

atualizaIni :: Mapa -- ^ Game map 
            -> [Maybe Acao] -- ^ List of actions
            -> [Personagem] -- ^ Enemies List
            -> [Personagem] -- ^ Updated enemies list
atualizaIni _ [] i = i
atualizaIni m (ac:acs) i =
  case ac of
    Just ac  -> atualizaIni m acs (map (atualizaAcao m ac) i)
    Nothing  -> i

{- |
Function that updates the player's character according to the action taken.
-}

atualizaPer :: Mapa -- ^ Game map
            -> Maybe Acao -- ^ Action to be applied
            -> Personagem -- ^ Player character 
            -> Personagem -- ^ Player character updated after action
atualizaPer m mac p =
    case mac of
        Just ac -> atualizaAcao m ac p
        Nothing -> p

{- |
Function that, given an action, updates a character's data.
-}

atualizaAcao :: Mapa -- ^ Game map 
             -> Acao -- ^ Action to be applied
             -> Personagem -- ^ Character 
             -> Personagem -- ^ Updated character
atualizaAcao m@(Mapa m1 m2 m3) ac p@(Personagem (v1,v2) i2 i3 i4 i5 i6 i7 i8 i9 i10) =
    case ac of
        Subir         -> if subEscada m3 pm then Personagem (v1,v2-1) i2 i3 i4 i5 i6 i7 i8 i9 i10 else p
        Descer        -> if subEscada m3 pm then Personagem (v1,v2+1) i2 i3 i4 i5 i6 i7 i8 i9 i10 else p
        AndarDireita  -> if movDireita m3 pm then Personagem (v1-1,v2) i2 i3 Oeste i5 i6 i7 i8 i9 i10 else Personagem (v1+1,v2) i2 i3 Este i5 i6 i7 i8 i9 i10
        AndarEsquerda -> if movEsquerda m3 pm then Personagem (v1+1,v2) i2 i3 Este i5 i6 i7 i8 i9 i10 else Personagem (v1-1,v2) i2 i3 Oeste i5 i6 i7 i8 i9 i10
        Saltar        -> if not i6 then Personagem (v1,v2-1) i2 i3 i4 i5 i6 i7 i8 i9 i10 else p
        Parar         -> if parOp m3 pm then Personagem (0,0) i2 i3 i4 i5 i6 i7 i8 i9 i10 else p
    where 
      pm = acP p 

{- |
Function that checks whether a character is in the position of a Ladder type block.
-}

subEscada :: [[Bloco]] -- ^ Map definition matrix
          -> Personagem -- ^ Character
          -> Bool -- ^ Truth value of the character being in the position of a Ladder block
subEscada b p@(Personagem i1 i2 i3 i4 i5 s i7 i8 i9 i10)
   | s = True
   | any (== hitPersona p) (hitBlocos (findBlocE b)) = True
   | otherwise = False

{- |
Function that checks if a character cannot move to the right.
-}

movDireita :: [[Bloco]] -- ^ Map definition matrix
          -> Personagem -- ^ Character
          -> Bool -- ^ True value of the character not being able to move to the right
movDireita b p@(Personagem i1 i2 (x, y) d i5 i6 r i8 i9 i10)
  | r && (`elem` findBlocP b) (snd (hitPersona p)) = True
  | r && (`elem` findBlocV b) (fst (fst (hitPersona p)) + 1, snd (fst (hitPersona p))) = True
  | otherwise = False

{- |
Function that checks if a character cannot move to the left.
-}

movEsquerda :: [[Bloco]] -- ^ Map definition matrix
            -> Personagem -- ^ Character
            -> Bool -- ^ rue value of the character not being able to move to the left
movEsquerda b p@(Personagem i1 i2 (x, y) d i5 i6 r i8 i9 i10)
  | r && (`elem` findBlocP b) (fst (snd (hitPersona p)) - 2, snd (snd (hitPersona p))) = True
  | r && (`elem` findBlocV b) (fst (fst (hitPersona p)) - 1, snd (fst (hitPersona p))) = True
  | otherwise = False

{- |
Function that checks whether a character can be stationary.
-}

parOp :: [[Bloco]] -- ^ Map definition matrix
      -> Personagem -- ^ Character
      -> Bool -- ^ Truth value of the character being able to stand still 
parOp b p@(Personagem i1 i2 (x, y) d i5 i6 r i8 i9 i10)
  | (`elem` findBlocP b) (fst (hitPersona p)) = True
  | otherwise = False