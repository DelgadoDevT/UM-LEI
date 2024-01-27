{-|
Module      : Alterations
Description : Changes and additions to improve the game
Copyright   : Joao Pedro Delgado Teixeira <a106836@alunos.uminho.pt>
              Ricardo Miguel da Silva Morais <a106935@alunos.uminho.pt>

Module for the realisation of Alterations of LI1 in 2023/24.
-}
module Alterations where

import LI12324
import Task1
import Task2
import Task3
import Task4
import Data.Maybe (fromJust)
import Data.List (nub, foldl1')
import System.Random

data Tecpress = Tecpress { pressao   :: Bool,
                           acao      :: Maybe Acao,
                           presse    :: Bool,
                           pressd    :: Bool,
                           hsalt     :: Bool}
                           deriving (Eq, Show)

type Paridade = (Bool,Bool)

type MinSeg = (Int,Int)

{-|
This document presents specific functions for the proper functioning of the graphics part of the game, including new functions for the definition in Gloss and changes to functions already defined in previous tasks, but with specificity for some cases of the graphics part.
for some cases in the graphics area.

* @aoPos@: Efficient function for obtaining a player's position by providing a player.
* @redondo@: Function that rounds the values of a position to the integer value
* @virgulado@: Function that rounds the values of a position in integers to a position in Doubles
* @otnEscala@: Function that goes from coordinates within the matrix to the actual map
* @ntoEscala@: Function that goes from the coordinates of the top left corner of the map to the coordinates of the centre of the matrix
* @estrelinhas@: Function that calculates the player's rating in a stage, based on the time it took them to complete it
* @segtoMin@: Function that converts the time from seconds to minutes and seconds
* @parI@: Function that checks whether the rounded down value of the time is even or odd
* @dist@: Function that calculates the distance between two positions
* @pmP@: Function that receives a position and a list of positions, and returns the position from the list closest to the singular position entered
* @findBlocPC@: Function that receives a list of block lists and returns the centre coordinates in the Platform block matrix
* @findBlocPV@: Function that receives a list of block lists and returns the centre coordinates in the Empty block matrix
* @findBlocPE@: Function that receives a list of block lists and returns the centre coordinates in the Ladder block matrix
* @findBlocPA@: Function that receives a list of block lists and returns the centre coordinates in the Trapdoor block matrix.
* @findBlocBC@: Function that receives a list of blocks and returns the centre coordinates in the Barrier block matrix
* @collat@: Function that checks whether the player can move left or right, and prevents movement if it is not valid
* @esFix@: Function that helps the player get off the ladder, minimising a small bug that occurred in normal situations.
* @esFixHelper@: Function that helps the player get off the ladder, minimising a small bug when this occurs in normal situations.
* @movCon@: Function responsible for allowing a continuous click on a key to move the player continuously
* @jumpNAssist@: Function responsible for preventing the player from jumping without support
* @vidaJog@: Function that checks if the player has less than 3 lives left
* @remEspada@: Function that removes the damage applied to the player after 10 seconds
* @collatI@: Function that applies the colLat function to the list of enemies
* @subEscadaI@: Function that checks if a character is on a ladder block
* @esFixI@: Function that applies the esFix' function to the list of enemies
* @fallVoidIH@: Function that applies the gravity effect to an enemy
* @esFix'@: Function that helps the enemy get off the ladder, minimising a small bug when this occurred in normal situations 
* @newMov@: Function that calculates the enemy's next move
* @colLatB@: Function that checks whether an enemy can move left or right
* @fallVoidI@: Function that applies the fallVoidIH function to the entire list of enemies

== Properties:
=== Function "oPos":
prop> oPos :: Personagem -> Posicao
prop> oPos (Personagem i1 i2 (x, y) i3 i4 i5 i6 i7 i8 i9) = (x, y)

=== Function "redondo":
prop> redondo :: Posicao -> (Int,Int)
prop> redondo (x,y) = (round x, round y)

=== Function "virgulado":
prop> virgulado :: (Int,Int) -> Posicao
prop> virgulado (x,y) = (fromIntegral x, fromIntegral y)

=== Function "otnEscala":
prop> otnEscala :: Posicao -> Posicao
prop> otnEscala (x, y) = (32 * x - 400, 496 - 32 * y)

=== Function "ntoEscala":
prop> ntoEscala :: Posicao -> Posicao
prop> ntoEscala (x, y) = ((x + 400) / 32, ((-y + 496) / 32))

=== Function "estrelinhas":
prop> estrelinhas :: MinSeg -> Int 
prop> estrelinhas (min,seg) = if min == 0 && 0 <= seg && seg < 60 
prop>                         then 3 
prop>                         else if 1 <= min && min <= 2 && 0 <= seg && seg < 60 
prop>                              then 2
prop>                              else 1

=== Function "segToMin":
prop> segToMin :: Tempo -> MinSeg
prop> segToMin t = (round t `div` 60 , round t `mod` 60)

=== Function "parI":
prop> parI :: Tempo -> Paridade
prop> parI t = if odd (ceiling t) then (True,False) else (False,True)

=== Function "dist":
prop> dist :: Posicao -> Posicao -> Double
prop> dist (x1, y1) (x2, y2) = sqrt ((x2 - x1)^2 + (y2 - y1)^2)

=== Function "pmP":
prop> pmP :: Posicao -> [Posicao] -> Posicao
prop> pmP _ [] = error "Lista vazia"
prop> pmP p0 p = foldl1' (\p1 p2 -> if dist p0 p1 < dist p0 p2 then p1 else p2) p

=== Function "findBlocPC":
prop> findBlocPC :: [[Bloco]] -> [Posicao]
prop> findBlocPC [] = []
prop> findBlocPC m =
prop>   concatMap (\(l, bs) -> [(fromIntegral c + 0.5, fromIntegral l + 0.5) | (b, c) <- zip bs [0..], b == Plataforma]) $ zip [0..] m

=== Function "findBlocPV":
prop> findBlocPV :: [[Bloco]] -> [Posicao]
prop> findBlocPV [] = []
prop> findBlocPV m =
prop>   concatMap (\(l, bs) -> [(fromIntegral c + 0.5, fromIntegral l + 0.5) | (b, c) <- zip bs [0..], b == Vazio]) $ zip [0..] m

=== Function "findBlocPE":
prop> findBlocPE :: [[Bloco]] -> [Posicao]
prop> findBlocPE [] = []
prop> findBlocPE m =
prop>   concatMap (\(l, bs) -> [(fromIntegral c + 0.5, fromIntegral l + 0.5) | (b, c) <- zip bs [0..], b == Escada]) $ zip [0..] m

=== Function "findBlocPA":
prop> findBlocPA :: [[Bloco]] -> [Posicao]
prop> findBlocPA [] = []
prop> findBlocPA m =
prop>   concatMap (\(l, bs) -> [(fromIntegral c + 0.5, fromIntegral l + 0.5) | (b, c) <- zip bs [0..], b == Alcapao]) $ zip [0..] m

=== Function "findBlocBC":
prop> findBlocBC :: [[Bloco]] -> [Posicao]
prop> findBlocBC [] = []
prop> findBlocBC m =
prop>   concatMap (\(l, bs) -> [(fromIntegral c + 0.5, fromIntegral l + 0.5) | (b, c) <- zip bs [0..], b == Barreira]) $ zip [0..] m

=== Function "colLat":
prop> colLat :: Mapa -> Personagem -> Personagem
prop> colLat (Mapa _ _ m3) j@(Personagem (vx, vy) _ (x, y) _ _ _ _ _ _ _) =
prop>     if colD
prop>     then j { velocidade = (min 0 vx, vy) }
prop>     else if colE
prop>         then j { velocidade = (max 0 vx, vy) }
prop>         else j
prop>   where
prop>     colE = any (==lJ) (concatMap (\(x, y) -> [(i, j) | i <- [x], j <- [y .. y + 31]]) (map (\(x,y) -> (x+32,y-16)) (map otnEscala  lP)))
prop>     colD = any (==(x,y)) (concatMap (\(x, y) -> [(i, j) | i <- [x], j <- [y .. y + 31]]) (map (\(x,y) -> (x-32,y-16)) (map otnEscala  lP)))
prop>     lP = findBlocPC m3
prop>     lJ = (x,y)

=== Function "esFix":
prop> esFix :: [[Bloco]] -> Personagem -> Personagem
prop> esFix m p@(Personagem (vx,vy) i2 i3 i4 i5 i6 i7 i8 i9 i10)
prop>   | any (`elem` [i3]) l = (Personagem (vx,vy + 32) i2 i3 i4 i5 i6 i7 i8 i9 i10)
prop>   | otherwise = p
prop>   where
prop>     l = concatMap (\(x, y) -> [(i, j) | i <- [x .. x + 31], j <- [y]]) (map (\(x,y) -> (x-16,y)) (esFixHelper m))

=== Function "esFixHelper":
prop> esFixHelper :: [[Bloco]] -> [Posicao]
prop> esFixHelper m =
prop>   let l = findBlocPC m
prop>       s = map (\(x, y) -> (x, y - 1)) (findBlocPE m)
prop>   in map otnEscala (nub [(x, y) | (x, y) <- l, (x, y) `elem` s])


=== Function "movCon":
prop> movCon :: Jogo -> Tecpress -> Jogo
prop> movCon j@(Jogo m i c jog@(Personagem (vx, vy) _ _ _ _ _ _ _ _ _)) tecpress
prop>     | pressao tecpress && (jumpNAssist m jog || (acao tecpress == Just Subir) || (acao tecpress == Just Descer)) =
prop>        atualiza' [] (acao tecpress) j
prop>     | acao tecpress == Just Saltar && not (presse tecpress || pressd tecpress) =
prop>         atualiza' [] (acao tecpress) j
prop>     | acao tecpress == Just Saltar && pressd tecpress =
prop>        atualiza' [] (Just Saltar) (j { jogador = jog { velocidade = (vx+8, vy) } })
prop>     | acao tecpress == Just Saltar && presse tecpress =
prop>         atualiza' [] (Just Saltar) (j { jogador = jog { velocidade = (vx-8, vy) } })
prop>     | acao tecpress == Just Parar =
prop>        atualiza' [] (acao tecpress) j
prop>     | otherwise = j

=== Function "jumpNAssist":
prop> jumpNAssist :: Mapa -> Personagem  -> Bool
prop> jumpNAssist (Mapa _ _ b) p@(Personagem (vx, vy) _ (x,y) _ _ _ _ _ _ _)
prop>     | any (==p') b' = True
prop>     | otherwise = False
prop>      where
prop>         p' = virgulado $ redondo (oPos p)
prop>         b' = concatMap (\(x, y) -> [(i, j) | i <- [x .. x + 45], j <- [y]]) (map (\(x,y) -> (x-24,y+32)) (map otnEscala (findBlocPC b ++ findBlocPA b)))

=== Function "vidaJog":
prop> vidaJog :: Jogo -> Bool
prop> vidaJog (Jogo m i r (Personagem i1 i2 i3 i4 i5 i6 i7 v i9 i10)) = v < 3

=== Function "remEspada":
prop> remEspada :: Personagem -> Personagem
prop> remEspada p@(Personagem i1 i2 i3 i4 i5 i6 i7 i8 i9 (f,n))
prop>   | n > 0 = (Personagem i1 i2 i3 i4 i5 i6 i7 i8 i9 (f,n - 1/30))
prop>   | otherwise = (Personagem i1 i2 i3 i4 i5 i6 i7 i8 i9 (False, 0))

=== Function "colLatI":
prop> colLatI :: Mapa -> [Personagem] -> [Personagem]
prop> colLatI m i = map (colLat m) i

=== Function "subEscadaI":
prop> subEscadaI :: [[Bloco]] -> Personagem -> Bool 
prop> subEscadaI b p@(Personagem i1 i2 i3 i4 i5 i6 i7 i8 i9 i10)
prop>    | any (`elem` [i3]) b' = True
prop>    | otherwise = False
prop>    where
prop>     b' = concatMap (\(x, y) -> [(i, j) | i <- [x], j <- [y .. y + 32]]) (map (\(x,y) -> (x,y-32)) (map otnEscala (findBlocPE b)))

=== Function "esFixI":
prop> esFixI :: [[Bloco]] -> [Personagem] -> [Personagem]
prop> esFixI m i = map (esFix' m) i

=== Function "fallVoidIH":
prop> fallVoidIH :: Mapa -> Personagem -> Personagem
prop> fallVoidIH (Mapa _ _ b) p@(Personagem (vx, vy) i2 _ _ _ i6 _ _ _ _)
prop>   | subEscadaI b p = p
prop>   | not (any (==p') b') && i2 == Fantasma = p { velocidade = (vx, vy - 8) }
prop>   | otherwise = p
prop>     where
prop>         p' = virgulado $ redondo (oPos p)
prop>         b' = concatMap (\(x, y) -> [(i, j) | i <- [x .. x + 45], j <- [y]]) (map (\(x,y) -> (x-24,y+32)) (map otnEscala (findBlocPC b ++ findBlocPA b)))

=== Function "esFix":
prop> esFix' :: [[Bloco]] -> Personagem -> Personagem
prop> esFix' m p@(Personagem (vx,vy) i2 i3@(x,y) i4 i5 i6 i7 i8 i9 i10)
prop>   | any (`elem` [i3]) l = (Personagem (vx,vy) i2 (x,y+64) i4 i5 i6 i7 i8 i9 i10)
prop>   | otherwise = p
prop>   where
prop>     l = concatMap (\(x, y) -> [(i, j) | i <- [x .. x + 31], j <- [y]]) (map (\(x,y) -> (x-16,y-32)) (esFixHelper m))

=== Function "newMov":
prop> newMov :: Mapa -> Personagem -> Personagem
prop> newMov m@(Mapa m1 m2 m3) i@(Personagem (v1,v2) i2 i3 i4 i5 i6 i7 i8 i9 i10)
prop> rop>      | subEscadaI m3 i = Personagem (v1,v2+4) i2 i3 i4 i5 i6 i7 i8 i9 i10
prop>      | colLatB m i == (True,False) && i7 = Personagem (v1-4,v2) i2 i3 Oeste i5 i6 i7 i8 i9 i10
prop>      | colLatB m i == (True,True)  && i7 = Personagem (v1+4,v2) i2 i3 Este i5 i6 i7 i8 i9 i10
prop>      | otherwise = if i4 == Este then Personagem (v1+4,v2) i2 i3 Este i5 i6 i7 i8 i9 i10 else Personagem (v1-4,v2) i2 i3 Oeste i5 i6 i7 i8 i9 i10

=== Function "colLatB":
prop> colLatB :: Mapa -> Personagem -> (Bool,Bool)
prop> colLatB (Mapa _ _ m3) j@(Personagem (vx, vy) _ (x, y) _ _ _ _ _ _ _)
prop>    | colD = (True,False)
prop>    | colE = (True,True)
prop>    | otherwise = (False, False)
prop>   where
prop>     colE = any (==lJ) (concatMap (\(x, y) -> [(i, j) | i <- [x], j <- [y .. y + 31]]) (map (\(x,y) -> (x+32,y-16)) (map otnEscala (lP ++ lB))))
prop>     colD = any (==(x,y)) (concatMap (\(x, y) -> [(i, j) | i <- [x], j <- [y .. y + 31]]) (map (\(x,y) -> (x-32,y-16)) (map otnEscala (lP ++ lB))))
prop>     lP = findBlocPC m3
prop>     lB = findBlocBC m3
prop>     lJ = (x,y)

=== Function "fallVoidI":
prop> fallVoidI :: Mapa -> [Personagem] -> [Personagem] -- a
prop> fallVoidI m i = map (fallVoidIH m) i

-}

alterations :: a -> a
alterations a = undefined

{-| 
Efficient function for obtaining a player's position by supplying a player
-}
oPos :: Personagem -- ^ Player character
     -> Posicao -- ^ Player position
oPos (Personagem i1 i2 (x, y) i3 i4 i5 i6 i7 i8 i9) = (x, y)

{-| 
Function that rounds the values of a position to the integer value
-}
redondo :: Posicao -- ^ Position in Double
        -> (Int,Int) -- ^ Rounded position in Int
redondo (x,y) = (round x, round y)

{-| 
Function that rounds values from a position in int to a position in Doubles
-}
virgulado :: (Int,Int) -- ^ Rounded position in Int
          -> Posicao -- ^ Position in Double
virgulado (x,y) = (fromIntegral x, fromIntegral y)

{-| 
Function that passes from coordinates within the matrix to the actual map
-}
otnEscala :: Posicao -- ^ Position in the matrix
          -> Posicao -- ^ Position on the map
otnEscala (x, y) = (32 * x - 400, 496 - 32 * y)

{-| 
Function that goes from the coordinates of the top left corner of the map to the coordinates of the centre of the matrix
-}
ntoEscala :: Posicao -- ^ Position in the matrix
          -> Posicao -- ^ Position on the map
ntoEscala (x, y) = ((x + 400) / 32, ((-y + 496) / 32))

{-| 
Function that calculates the player's rating in a stage, based on the time it took to complete it
-}
estrelinhas :: MinSeg -- ^ Time the player spent in the phase
            -> Int -- ^ Player rating from 1 to 3
estrelinhas (min,seg) = if min == 0 && 0 <= seg && seg < 60 
                        then 3 
                        else if 1 <= min && min <= 2 && 0 <= seg && seg < 60 
                             then 2
                             else 1

{-| 
Function that converts time from seconds to minutes and seconds
-}
segToMin :: Tempo -- ^ Time in seconds
         -> MinSeg -- ^ Time in minutes and seconds
segToMin t = (round t `div` 60 , round t `mod` 60)

{-| 
Function that checks whether the rounded value under the time is even or odd
-}
parI :: Tempo -- ^ Time in seconds
     -> Paridade -- ^ Truth value in the form of a different type of data
parI t = if odd (ceiling t) then (True,False) else (False,True)

{-| 
Function that calculates the distance between two positions
-}
dist :: Posicao -- ^ First position
     -> Posicao -- ^ Second position
     -> Double -- ^ Distance between the two positions
dist (x1, y1) (x2, y2) = sqrt ((x2 - x1)^2 + (y2 - y1)^2)

{-| 
Function that receives a position and a list of positions, and returns the list position closest to the singular position entered
-}
pmP :: Posicao -- ^ Starting position
    -> [Posicao] -- ^ List of positions
    -> Posicao -- ^ List position closest to starting position
pmP _ [] = error "Lista vazia"
pmP p0 p = foldl1' (\p1 p2 -> if dist p0 p1 < dist p0 p2 then p1 else p2) p

{-| 
Function that receives a list of block lists and returns the centre coordinates in the Platform block matrix
-}
findBlocPC :: [[Bloco]] -- ^ Map blocks
           -> [Posicao] -- ^ List of Platform block positions
findBlocPC [] = []
findBlocPC m =
  concatMap (\(l, bs) -> [(fromIntegral c + 0.5, fromIntegral l + 0.5) | (b, c) <- zip bs [0..], b == Plataforma]) $ zip [0..] m

{-| 
Function that receives a list of block lists and returns the centre coordinates in the Empty block matrix
-}
findBlocPV :: [[Bloco]] -- ^ Map blocks
           -> [Posicao] -- ^ List of Empty block positions
findBlocPV [] = []
findBlocPV m =
  concatMap (\(l, bs) -> [(fromIntegral c + 0.5, fromIntegral l + 0.5) | (b, c) <- zip bs [0..], b == Vazio]) $ zip [0..] m

{-| 
Function that receives a list of block lists and returns the centre coordinates in the Ladder block matrix
-}
findBlocPE :: [[Bloco]] -- ^ Map blocks
           -> [Posicao] -- ^ List of Ladder block positions
findBlocPE [] = []
findBlocPE m =
  concatMap (\(l, bs) -> [(fromIntegral c + 0.5, fromIntegral l + 0.5) | (b, c) <- zip bs [0..], b == Escada]) $ zip [0..] m

{-| 
Function that receives a list of block lists and returns the centre coordinates in the Trapdoor block matrix
-}
findBlocPA :: [[Bloco]] -- ^ Map blocks
           -> [Posicao] -- ^ List of Trapdoor block positions
findBlocPA [] = []
findBlocPA m =
  concatMap (\(l, bs) -> [(fromIntegral c + 0.5, fromIntegral l + 0.5) | (b, c) <- zip bs [0..], b == Alcapao]) $ zip [0..] m

{-| 
Function that receives a list of block lists and returns the centre coordinates in the Barrier block matrix
-}
findBlocBC :: [[Bloco]] -- ^ Map blocks
           -> [Posicao] -- ^ List of Barrier block positions
findBlocBC [] = []
findBlocBC m =
  concatMap (\(l, bs) -> [(fromIntegral c + 0.5, fromIntegral l + 0.5) | (b, c) <- zip bs [0..], b == Barreira]) $ zip [0..] m

{-| 
Function that checks whether the player can move left or right, and prevents movement if it is not valid
-}
colLat :: Mapa -- ^ Game map 
       -> Personagem -- ^ Player character
       -> Personagem -- ^ Updated player character
colLat (Mapa _ _ m3) j@(Personagem (vx, vy) _ (x, y) _ _ _ _ _ _ _) =
    if colD
    then j { velocidade = (min 0 vx, vy) }
    else if colE
        then j { velocidade = (max 0 vx, vy) }
        else j
  where
    colE = any (==lJ) (concatMap (\(x, y) -> [(i, j) | i <- [x], j <- [y .. y + 31]]) (map (\(x,y) -> (x+32,y-16)) (map otnEscala  lP)))
    colD = any (==(x,y)) (concatMap (\(x, y) -> [(i, j) | i <- [x], j <- [y .. y + 31]]) (map (\(x,y) -> (x-32,y-16)) (map otnEscala  lP)))
    lP = findBlocPC m3
    lJ = (x,y)

{-| 
Function that helps the player get off the ladder, minimising a small bug when this occurred in normal situations
-}
esFixHelper :: [[Bloco]] -- ^ List of map blocks
            -> [Posicao] -- ^ List of platform block positions on top of the last vertical ladder
esFixHelper m =
  let l = findBlocPC m
      s = map (\(x, y) -> (x, y - 1)) (findBlocPE m)
  in map otnEscala (nub [(x, y) | (x, y) <- l, (x, y) `elem` s])

esFix :: [[Bloco]] -- ^ List of map blocks
         -> Personagem -- ^ Player character
         -> Personagem -- ^ Player character after correction
esFix m p@(Personagem (vx,vy) i2 i3 i4 i5 i6 i7 i8 i9 i10)
  | any (`elem` [i3]) l = (Personagem (vx,vy + 32) i2 i3 i4 i5 i6 i7 i8 i9 i10)
  | otherwise = p
  where
    l = concatMap (\(x, y) -> [(i, j) | i <- [x .. x + 31], j <- [y]]) (map (\(x,y) -> (x-16,y)) (esFixHelper m))

{-| 
Function responsible for allowing a continuous click on a key to move the player continuously
-}
movCon :: Jogo -- ^ Game in question
       -> Tecpress -- ^ Pressing certain buttons
       -> Jogo -- ^ Updated game
movCon j@(Jogo m i c jog@(Personagem (vx, vy) _ _ _ _ _ _ _ _ _)) tecpress
    | pressao tecpress && (jumpNAssist m jog || (acao tecpress == Just Subir) || (acao tecpress == Just Descer)) =
        atualiza' [] (acao tecpress) j
    | acao tecpress == Just Saltar && not (presse tecpress || pressd tecpress) =
        atualiza' [] (acao tecpress) j
    | acao tecpress == Just Saltar && pressd tecpress =
        atualiza' [] (Just Saltar) (j { jogador = jog { velocidade = (vx+8, vy) } })
    | acao tecpress == Just Saltar && presse tecpress =
        atualiza' [] (Just Saltar) (j { jogador = jog { velocidade = (vx-8, vy) } })
    | acao tecpress == Just Parar =
        atualiza' [] (acao tecpress) j
    | otherwise = j

{-| 
Function responsible for preventing the player from jumping without support
-}
jumpNAssist :: Mapa   -- ^ Game map
            -> Personagem -- ^ Player character
            -> Bool -- ^ Truth value for the jump
jumpNAssist (Mapa _ _ b) p@(Personagem (vx, vy) _ (x,y) _ _ _ _ _ _ _)
    | any (==p') b' = True
    | otherwise = False
     where
        p' = virgulado $ redondo (oPos p)
        b' = concatMap (\(x, y) -> [(i, j) | i <- [x .. x + 45], j <- [y]]) (map (\(x,y) -> (x-24,y+32)) (map otnEscala (findBlocPC b ++ findBlocPA b)))

{-| 
Function that checks if the player has less than 3 lives
-}
vidaJog :: Jogo -- ^ Game in question
        -> Bool -- ^ Truth value for the determined
vidaJog (Jogo m i r (Personagem i1 i2 i3 i4 i5 i6 i7 v i9 i10)) = v < 3

{-| 
Function that removes damage from the player after 10 seconds
-}
remEspada :: Personagem -- ^ Player character
          -> Personagem -- ^ Updated player character
remEspada p@(Personagem i1 i2 i3 i4 i5 i6 i7 i8 i9 (f,n))
  | n > 0 = (Personagem i1 i2 i3 i4 i5 i6 i7 i8 i9 (f,n - 1/30))
  | otherwise = (Personagem i1 i2 i3 i4 i5 i6 i7 i8 i9 (False, 0))

{-| 
Function that applies the colLat function to the list of enemies
-}
colLatI :: Mapa -- ^ Game map
        -> [Personagem] -- ^ Enemies list
        -> [Personagem] -- ^ Updated enemies list
colLatI m i = map (colLat m) i

{-| 
Function that checks if a character is on a ladder block
-}
subEscadaI :: [[Bloco]] -- ^ Map defenition matriz
           -> Personagem -- ^ Character
           -> Bool -- ^ Truth value of the character being in the position of a Ladder block
subEscadaI b p@(Personagem i1 i2 i3 i4 i5 i6 i7 i8 i9 i10)
   | any (`elem` [i3]) b' = True
   | otherwise = False
   where
    b' = concatMap (\(x, y) -> [(i, j) | i <- [x], j <- [y .. y + 32]]) (map (\(x,y) -> (x,y-32)) (map otnEscala (findBlocPE b)))

{-| 
Function that applies the esFix' function to the enemies list
-}
esFixI :: [[Bloco]] -- ^ List of map block lists
       -> [Personagem] -- ^ Enemies list
       -> [Personagem] -- ^ Updated enemies list
esFixI m i = map (esFix' m) i

{-| 
Function that applies the effect of gravity to an enemy
-}
fallVoidIH :: Mapa -- ^ Game map
           -> Personagem -- ^ Enemy in question
           -> Personagem -- ^ Updated enemy
fallVoidIH (Mapa _ _ b) p@(Personagem (vx, vy) i2 _ _ _ i6 _ _ _ _)
  | subEscadaI b p = p
  | not (any (==p') b') && i2 == Fantasma = p { velocidade = (vx, vy - 8) }
  | otherwise = p
    where
        p' = virgulado $ redondo (oPos p)
        b' = concatMap (\(x, y) -> [(i, j) | i <- [x .. x + 45], j <- [y]]) (map (\(x,y) -> (x-24,y+32)) (map otnEscala (findBlocPC b ++ findBlocPA b)))

{-| 
Function that helps the enemy get off the ladder, minimising a small bug when this occurred in normal situations
-}
esFix' :: [[Bloco]] -- ^ List of map block lists
       -> Personagem -- ^ Enemy in question
       -> Personagem -- ^ Updated enemy
esFix' m p@(Personagem (vx,vy) i2 i3@(x,y) i4 i5 i6 i7 i8 i9 i10)
  | any (`elem` [i3]) l = (Personagem (vx,vy) i2 (x,y+64) i4 i5 i6 i7 i8 i9 i10)
  | otherwise = p
  where
    l = concatMap (\(x, y) -> [(i, j) | i <- [x .. x + 31], j <- [y]]) (map (\(x,y) -> (x-16,y-32)) (esFixHelper m))

{-| 
Function that calculates the enemy's next move
-}
newMov :: Mapa -- ^ Game map
       -> Personagem -- ^ Enemy in question
       -> Personagem -- ^ Updated enemy
newMov m@(Mapa m1 m2 m3) i@(Personagem (v1,v2) i2 i3 i4 i5 i6 i7 i8 i9 i10)
     | subEscadaI m3 i = Personagem (v1,v2+4) i2 i3 i4 i5 i6 i7 i8 i9 i10
     | colLatB m i == (True,False) && i7 = Personagem (v1-4,v2) i2 i3 Oeste i5 i6 i7 i8 i9 i10
     | colLatB m i == (True,True)  && i7 = Personagem (v1+4,v2) i2 i3 Este i5 i6 i7 i8 i9 i10
     | otherwise = if i4 == Este then Personagem (v1+4,v2) i2 i3 Este i5 i6 i7 i8 i9 i10 else Personagem (v1-4,v2) i2 i3 Oeste i5 i6 i7 i8 i9 i10

{-| 
Function that checks whether an enemy can move left or right
-}
colLatB :: Mapa -- ^ Game map
        -> Personagem -- ^ Enemy in question
        -> (Bool,Bool) -- ^ Truth value for the determined
colLatB (Mapa _ _ m3) j@(Personagem (vx, vy) _ (x, y) _ _ _ _ _ _ _)
   | colD = (True,False)
   | colE = (True,True)
   | otherwise = (False, False)
  where
    colE = any (==lJ) (concatMap (\(x, y) -> [(i, j) | i <- [x], j <- [y .. y + 31]]) (map (\(x,y) -> (x+32,y-16)) (map otnEscala (lP ++ lB))))
    colD = any (==(x,y)) (concatMap (\(x, y) -> [(i, j) | i <- [x], j <- [y .. y + 31]]) (map (\(x,y) -> (x-32,y-16)) (map otnEscala (lP ++ lB))))
    lP = findBlocPC m3
    lB = findBlocBC m3 
    lJ = (x,y)

{-| 
Function that applies the fallVoidIH function to the entire list of enemies
-}
fallVoidI :: Mapa -- ^ Game map 
          -> [Personagem] -- ^ Enemies list
          -> [Personagem] -- ^ Updated enemies list
fallVoidI m i = map (fallVoidIH m) i

{-|
The remaining functions are changes to some of the functions defined in the previous tasks. 
These have been altered for specific game purposes, so we haven't done any haddock documentation on them
-}

valida' :: Jogo -> Bool
valida' (Jogo m@(Mapa m1 m2 m3) i c p)
   = (all (==True) [f1,f2,f3,f4,f5,f6,f7,f8])
   where
    f1 = chao m
    f2 = ressalto i p
    f3 = not (choquePs' m i)
    f4 = minInimigos i
    f5 = vidaFan (vidaFanAux i)
    f6 = all (==True) (checkBlocAux (extEscada' m (findBlocE m3)))
    f7 = alcLargo' p
    f8 = colEntidades' i p c m

choquePs' :: Mapa -> [Personagem] -> Bool 
choquePs' (Mapa (p1,_) _ _) li
   = any (== fixp) (map colp' li)
    where
        fixp= ntoEscala p1
        colp' (Personagem _ _ p2 _ _ _ _ _ _ _) = ntoEscala p2

alcLargo' :: Personagem -> Bool 
alcLargo' (Personagem _ _ _ _ (g,_) _ _ _ _ _) = g <= 64

colEntidades' :: [Personagem] -> Personagem -> [(Colecionavel, Posicao)] -> Mapa -> Bool 
colEntidades' ps p cs (Mapa i1 i2 m) = all (`elem` ((findBlocPV m) ++ (findBlocPE m))) (map acC (colEntidadesAux' ps p cs))

colEntidadesAux' :: [Personagem] -> Personagem -> [(Colecionavel, Posicao)] -> [Posicao] 
colEntidadesAux' ps (Personagem _ _ (x2, y2) _ _ _ _ _ _ _) cs =
    (map ntoEscala (map (\(Personagem _ _ (x1, y1) _ _ _ _ _ _ _)-> (x1, y1)) ps ++ [(x2, y2)] ++ map snd cs))

movimenta' :: Semente -> Tempo -> Jogo -> Jogo 
movimenta' s t j@(Jogo m@(Mapa i1 i2 i3) i c p) =
    recColec' $ Jogo mr (colLatI m (esFixI i3 (fallVoidI m (gamaIni (perderVida' ri p))))) c (remEspada (movJogador' (danoDuro' i (esFix i3 (colLat m (fallVoid' m (subEscada' i3 p)))))))
    where
      mr = (Mapa i1 i2 (menosAl' m p))
      ri = movInimigo' (randInimigos' m s i)

movJogador' :: Personagem -> Personagem 
movJogador' (Personagem (vx,vy) i2 (x, y) i4 i5 i6 i7 i8 i9 i10) =
        (Personagem (0,0) i2 (x + vx, y + vy) i4 i5 i6 i7 i8 i9 i10)

movInimigo' :: [Personagem] -> [Personagem]
movInimigo' i = map movJogador' i

menosAl' :: Mapa -> Personagem -> [[Bloco]] 
menosAl' mp@(Mapa _ _ m) p@(Personagem i1 e (x, y) i2 i3 i4 i5 i6 i7 i8)
        | e == Jogador && any (==(x, y)) pc' = substituiMatriz m (yf-0.5,xf-0.5) Vazio
        | otherwise = m
      where
        (xf,yf) = pmP (ntoEscala (x,y)) (findBlocPA m)
        pc' = concatMap (\(x, y) -> [(i, j) | i <- [x .. x + 31], j <- [y]]) (map (\(x,y) -> (x - 16,y + 32)) (map otnEscala (findBlocPA m)))

fallVoid' :: Mapa -> Personagem -> Personagem
fallVoid' (Mapa _ _ b) p@(Personagem (vx, vy) _ _ _ _ i6 _ _ _ _)
  | i6 == True = p
  | not (any (==p') b') = p { velocidade = (vx, vy - 8) }
  | otherwise = p
    where
        p' = virgulado $ redondo (oPos p)
        b' = concatMap (\(x, y) -> [(i, j) | i <- [x .. x + 45], j <- [y]]) (map (\(x,y) -> (x-24,y+32)) (map otnEscala (findBlocPC b)))

extEscada' :: Mapa -> [Posicao] -> [(Bloco, Bloco)]
extEscada' _ [] = []
extEscada' (Mapa i1 i2 m) ((l, c):re)
    | l > 0 && l < fromIntegral (length m) - 1 && c >= 0 && c < fromIntegral (length (head m)) =
        let lAn = floor (l - 1)
            lAt = floor l
            lS = floor (l + 1)
            cA = floor c

            cima = m !! lAn !! cA
            baixo = m !! lS !! cA

            ext = (cima, baixo)
            r = extEscada' (Mapa i1 i2 m) re
        in if cima /= Alcapao && baixo /= Alcapao && (cima == Plataforma || baixo == Plataforma)
            then ext : r
            else r
    | otherwise = extEscada' (Mapa i1 i2 m) re

recColec' :: Jogo -> Jogo
recColec' jogo@(Jogo m@(Mapa m1 m2 m3) i [] p) = jogo
recColec' (Jogo m i ((colec@(c, pov)):r) p@(Personagem _ _ pos _ _ _ _ _ moeda dano))
   | interColec pos pov && c == Martelo = recColec' (Jogo m i r p { aplicaDano = (True, 10) })
   | interColec pos pov && c == Moeda   = recColec' (Jogo m i r p { pontos = (moeda + 100) })
   | otherwise = insColec' colec (recColec' (Jogo m i r p))

insColec' :: (Colecionavel, Posicao) -> Jogo -> Jogo 
insColec' c (Jogo m i r p) = (Jogo m i (c:r) p)

recColecE :: Jogo -> Bool
recColecE jogo@(Jogo m i [] p) = False
recColecE (Jogo m i (colec@(c, pov):r) p@(Personagem _ _ pos _ _ _ _ _ moeda dano))
   | interColec pos pov && c == Estrela = True
   | otherwise = recColecE (Jogo m i r p)

interColec :: Posicao -> Posicao -> Bool
interColec pj pc = any (`elem` [pj]) pc'
  where
    pc' = concatMap (\(x, y) -> [(i, j) | i <- [x .. x + 31], j <- [y]]) (map (\(x, y) -> (x - 16, y)) [pc])

randInimigos' :: Mapa -> Semente -> [Personagem] -> [Personagem] 
randInimigos' m s [] = []
randInimigos' m s (i@(Personagem i1 i2 i3 i4 i5 i6 i7 i8 i9 i10):is)
  | i2 == Fantasma = newMov m i : randInimigos' m s is
  | otherwise = i : randInimigos' m s is

subEscadaB :: [[Bloco]] -> Personagem -> Bool 
subEscadaB b p@(Personagem i1 i2 i3 i4 i5 i6 i7 i8 i9 i10)
   | any (`elem` [i3]) b' = True
   | otherwise = False
   where
    b' = concatMap (\(x, y) -> [(i, j) | i <- [x .. x + 31], j <- [y .. y + 32]]) (map (\(x,y) -> (x-16,y)) (map otnEscala (findBlocPE b)))

perderVida' :: [Personagem] -> Personagem -> [Personagem]
perderVida' [] _ = []
perderVida' (i@(Personagem i1 i2 i3@(x,y) i4 i5 i6 i7 v i9 (d,id2)):ls) p@(Personagem a1 a2 (a,b) a4 a5 a6 a7 a8 a9 a10)
 | any (`elem` hDI) (hitDano' p) && (danAtivo p == True) && i2 == Fantasma = ((Personagem i1 i2 i3 i4 i5 i6 i7 (v - 1) i9 (d,id2))) : perderVida' ls p
 | otherwise = (Personagem i1 i2 i3 i4 i5 i6 i7 v i9 (d,id2)) : perderVida' ls p
  where
    hDI = concatMap (\(x, y) -> [(i, j) | i <- [x .. x + 15 ], j <- [y .. y + 15]]) (map (\(x,y) -> (x-8,y-8)) [(x,y)])

hitDano' :: Personagem -> [Posicao]
hitDano' (Personagem i1 i2 (x, y) o (g, c) i3 i4 i5 i6 i7)
    | o == Este   = concatMap (\(x, y) -> [(i, j) | i <- [x .. x + 15 ], j <- [y .. y + 15]]) (map (\(x,y) -> (x+24,y-8)) [(x,y)])
    | o == Oeste  = concatMap (\(x, y) -> [(i, j) | i <- [x .. x + 15 ], j <- [y .. y + 15]]) (map (\(x,y) -> (x-40,y-8)) [(x,y)])
    | otherwise   = [(x,y)]

danoDuro' :: [Personagem] -> Personagem -> Personagem 
danoDuro' [] p = p
danoDuro' l p@(Personagem _ _ _ _ _ _ _ x _ _) | any (==True) (colisoesDano' l p) = p { vida = (x - 1) }
                                               | otherwise = p

colisoesDano' :: [Personagem] -> Personagem -> [Bool] 
colisoesDano' [] p = [False]
colisoesDano' (h:ht) p = colisoesPersonagens' h p : colisoesDano' ht p

colisoesPersonagens' :: Personagem -> Personagem -> Bool
colisoesPersonagens' (Personagem i1 i2 (x,y) i4 i5 i6 i7 v i9 (d,id2)) (Personagem a1 a2 (a,b) a4 a5 a6 a7 a8 a9 a10)
    | any (`elem` hI) hJ = True
    | otherwise = False
    where
      hI = concatMap (\(x, y) -> [(i, j) | i <- [x .. x + 15], j <- [y .. y + 15]]) (map (\(x,y) -> (x-8,y-8)) [(x,y)])
      hJ = concatMap (\(x, y) -> [(i, j) | i <- [x .. x + 15], j <- [y .. y + 15]]) (map (\(x,y) -> (x-8,y-8)) [(a,b)])

atualiza' :: [Maybe Acao] -> Maybe Acao -> Jogo -> Jogo 
atualiza' ap aj j@(Jogo m i c p) =
  let
    ni = atualizaIni m ap i
    nj = atualizaPer' m aj p
  in
   (Jogo m ni c nj)

atualizaPer' :: Mapa -> Maybe Acao -> Personagem -> Personagem 
atualizaPer' m mac p =
    case mac of
        Just ac -> atualizaAcao' m ac p
        Nothing -> p

atualizaAcao' :: Mapa -> Acao -> Personagem -> Personagem
atualizaAcao' m@(Mapa m1 m2 m3) ac p@(Personagem (v1,v2) i2 i3 i4 i5 i6 i7 i8 i9 i10) =
    case ac of
        Subir         -> if i6 then (Personagem (v1,v2+8) i2 i3 i4 i5 i6 i7 i8 i9 i10) else p
        Descer        -> if i6 then (Personagem (v1,v2-8) i2 i3 i4 i5 i6 i7 i8 i9 i10) else p
        AndarDireita  -> if jumpNAssist m p then (Personagem (v1+8,v2) i2 i3 Este i5 i6 i7 i8 i9 i10) else p
        AndarEsquerda -> if jumpNAssist m p then (Personagem (v1-8,v2) i2 i3 Oeste i5 i6 i7 i8 i9 i10) else p
        Saltar        -> if i6 == False && jumpNAssist m p then (Personagem (v1,v2+64) i2 i3 i4 i5 i6 i7 i8 i9 i10) else p
        Parar         -> if jumpNAssist m p then (Personagem (0,0) i2 i3 i4 i5 i6 i7 i8 i9 i10)  else p

subEscada' :: [[Bloco]] -> Personagem -> Personagem 
subEscada' b p@(Personagem i1 i2 i3 i4 i5 i6 i7 i8 i9 i10)
   | any (`elem` [i3]) b' = p { emEscada = True }
   | otherwise = p { emEscada = False}
   where
    b' = concatMap (\(x, y) -> [(i, j) | i <- [x .. x + 31], j <- [y .. y + 32]]) (map (\(x,y) -> (x-16,y)) (map otnEscala (findBlocPE b)))

subVazio :: [[Bloco]] -> Personagem -> Bool
subVazio b p@(Personagem i1 i2 i3 i4 i5 i6 i7 i8 i9 i10)
   | any (`elem` [i3]) b' = True
   | otherwise = False
   where
    b' = concatMap (\(x, y) -> [(i, j) | i <- [x .. x + 31], j <- [y .. y + 32]]) (map (\(x,y) -> (x-16,y)) (map otnEscala (findBlocPE b)))

parOp' :: [[Bloco]] -> Personagem -> Bool
parOp' b p@(Personagem i1 i2 i3 d i5 i6 r i8 i9 i10)
  | (any (`elem` [(ntoEscala i3)]) (findBlocPC b) == True) = True
  | otherwise = False