{-|
Module      : Task2
Description : Validate game
Copyright   : Joao Pedro Delgado Teixeira <a106836@alunos.uminho.pt>
              Ricardo Miguel da Silva Morais <a106935@alunos.uminho.pt>

Module for the realisation of Task2 of LI1 in 2023/24.
-}
module Task2 where

import LI12324

import Task1

{- |

In this task, we aim to define the __valida__ function, a function that checks whether a given game violates the following restrictions:

1. The map has a "chao", i.e. a platform that prevents the player or another character from falling off the map.
2. All the enemies have the __ressalta__ property at @True@, while the player has it at @False@.
3. A player's starting position cannot collide with another character's starting position. Note that the starting positions of enemies can collide with each other.
4. Minimum number of enemies: 2 (two).
5. __Fantasma__ enemies have exactly 1 (one) life.
6. Ladders cannot start/end at trapdoors, and at least one of their ends must be of the __Plataforma__ type.
7. Trap doors cannot be wider than the player.
8. There can be no characters or collectibles "inside" platforms or trapdoors, i.e. the block (in the map matrix) corresponding to the position of a character or object must be __Vazio__.

To be able to define this function, we can use __auxiliary__ functions that check the logical value of each of these properties, such as:

* @chao@ function, which checks whether a map has a "floor", i.e. a platform that prevents the player from falling off the map.
* @ressalto@ function, which checks that all enemies have the "ressalta" property @True@ and the player @False@.
* @choquePs@ function, which checks that the player's starting position does not coincide with another character's starting position.
* @minInimigos@ function, which checks that the minimum number of enemies on the map is 2 (two).
* @vidaFan@ function, which checks that all enemies of the @Fanstasma@ type have exactly 1 (one) life.
* @checkBloc@ function, which checks that all the ladders on the map don't start or end in trapdoors and that at least one of their ends is of type @Platforma@.
* @alcLargo@ function, which checks if a player is smaller than a map block.
* @colEntidades@ function, which checks if a character or collectible is "inside" an @empty@ map block.

In the case of the __choquePs__ function, we use:

* the @colp@ function, which removes a character's position.

In the case of the __vidaFan__ function, we use an auxiliary function __vidaFanAux__, which checks that all ghost-type enemies have exactly 1 (one) life. The main function only checks that all cases are of type @True@.

In the case of the __checkBloc__ function, we've used auxiliary functions already defined previously, such as @findBloc@, adapted to detect the __Escada__ type (findBlocE, with the help of __findBlocEAux__ to be able to use it by giving a @Mapa@ as an argument), and created new ones such as:

* function __extEscada__, which receives the matrix that defines the map and the list of ladder block positions on the map (calculated by findblocE), and returns a list of pairs that correspond to the block above and below the ladders, respectively. 
* function __checkBLocAux__ function, which receives a list of pairs of blocks above and below the stairs and returns the list of logical values for whether the stairs have at least one platform block and no trapdoor blocks.

In the case of the @colEntidades@ function, we have used auxiliary functions already defined previously, such as @FindBloc@, adapted to detect the __Vazio__ type (findBlocV), and created new ones such as:

* function __colEntidadesAux__, which calculates the list of positions of all entities, simultaneously.
* __centra__ function, which calculates the coordinates of the centre of a block.

== Properties:
=== Main function "valida":
prop> valida :: Jogo -> Bool
prop> valida (Jogo m i c p)
prop>    = (all (==True) [f1,f2,f3,f4,f5,f6,f7,f8])
prop>    where
prop>     f1 = chao m
prop>     f2 = ressalto i p
prop>     f3 = not (choquePs m i)
prop>     f4 = minInimigos i
prop>     f5 = vidaFan (vidaFanAux i)
prop>     f6 = checkBloc m (extEscada m (findBlocEAux m))
prop>     f7 = alcLargo p
prop>     f8 = colEntidades i p c m

=== Auxiliary function "chao":
prop> chao :: Mapa -> Bool
prop> chao (Mapa _ _ m) = not (notElem Plataforma (last m))

=== Auxiliary function "ressalto":
prop> ressalto :: [Personagem] -> Personagem -> Bool
prop> ressalto li (Personagem _ _ _ _ _ _ c2 _ _ _)
prop>     = all (== True) (map colc li) && c2 == False 
prop>    where
prop>        colc :: Personagem -> Bool
prop>        colp (Personagem _ _ p2 _ _ _ _ _ _ _) = p2

=== Auxiliary function "choquePs":
prop> choquePs :: Mapa -> [Personagem] -> Bool
prop> choquePs (Mapa (p1,_) _ _) li
prop>    = any (== (acC p1)) (map colp li)
prop>     where
prop>         colp :: Personagem -> Posicao 
prop>         colp (Personagem _ _ p2 _ _ _ _ _ _ _) = acC p2

=== Auxiliary function "minInimigos":
prop> minInimigos :: [Personagem] -> Bool
prop> minInimigos i = length i >= 2

=== Auxiliary function "vidaFan":
prop> vidaFan :: [Bool] -> [Bool]
prop> vidaFan l = all (True==) l

==== Auxiliary function "vidaFanAux":
prop> vidaFanAux [] = []
prop> vidaFanAux ((Personagem _ t _ _ _ _ _ v _ _):ps)
prop>    | t == MacacoMalvado || t == Jogador = True : vidaFanAux ps
prop>    | t == Fantasma && v == 1 = True : vidaFanAux ps
prop>    | otherwise = False : vidaFanAux ps

=== Auxiliary function "checkBloc":
prop> checkBloc :: Mapa -> [(Bloco, Bloco)] -> Bool
prop> checkBloc (Mapa _ _ m) l = if findBlocE m == [] then True else if not (null (findBlocE m)) && length l >= 1 then all (==True) (checkBlocAux l) else False

==== Auxiliary function "checkBlocAux":
prop> checkBlocAux :: [(Bloco,Bloco)] -> [Bool]
prop> checkBlocAux [] = [True]
prop> checkBlocAux ((x,y):r) 
prop>   | x == Plataforma && (y == Plataforma || y == Vazio || y == Escada) = True : checkBlocAux r
prop>   | (x == Plataforma || x == Vazio || x == Escada) && y == Plataforma = True : checkBlocAux r
prop>   | otherwise = False : checkBlocAux r

==== Auxiliary function "extEscada":
prop> extEscada :: Mapa -> [Posicao] -> [(Bloco, Bloco)]
prop> extEscada _ [] = []
prop> extEscada (Mapa i1 i2 m) ((l, c):re)
prop>     | l > 0 && l < fromIntegral (length m) - 1 && c >= 0 && c < fromIntegral (length (head m)) =
prop>         let lAn = floor (l - 1)
prop>             lAt = floor l
prop>             lS = floor (l + 1)
prop>             cA = floor c
prop>             cima = m !! lAn !! cA
prop>             baixo = m !! lS !! cA
prop>             ext = (cima, baixo)
prop>             r = extEscada (Mapa i1 i2 m) re
prop>         in if cima /= Alcapao && baixo /= Alcapao && (cima == Plataforma || baixo == Plataforma)
prop>             then ext : r
prop>             else r
prop>     | otherwise = extEscada (Mapa i1 i2 m) re

=== Auxiliary function "alcLargo":
prop> alcLargo :: Personagem -> Bool
prop> alcLargo (Personagem _ _ _ _ (g,c) _ _ _ _ _) = g <= 1 && c <= 1

=== Auxiliary function "colEntidades": --
prop> colEntidades :: [Personagem] -> Personagem -> [(Colecionavel, Posicao)] -> Mapa -> Bool
prop> colEntidades ps p cs (Mapa i1 i2 m) = all (`elem` (centra ((findBlocV m) ++ (findBlocE m)))) (colEntidadesAux ps p cs)

==== Auxiliary function "colEntidadesAux":
prop> colEntidadesAux :: [Personagem] -> Personagem -> [(Colecionavel, Posicao)] -> [Posicao]
prop> colEntidadesAux ps (Personagem _ _ (x2, y2) _ _ _ _ _ _ _) cs =
prop>    map (acC . (\(Personagem _ _ (x1, y1) _ _ _ _ _ _ _)-> (x1, y1))) ps ++ [acC (x2, y2)] ++ map snd cs

==== Auxiliary function "centra":
prop> centra :: [Posicao] -> [Posicao]
prop> centra [] = []
prop> centra m = map (\(l, ls) -> (l + 0.5, ls + 0.5)) m

== Examples of use:
>>> valida (Jogo Mapa ((1.5,1.5),Este) (1.5,1.5) [[Vazio, Vazio, Vazio], [Vazio, Vazio, Vazio], [Plataforma, Plataforma, Plataforma]] [(Personagem (1, 0) Fantasma (2.5, 1.5) Este (1, 1) False True 1 0 (False, 0)),(Personagem (1, 0) Fantasma (2.5, 1.5) Este (1, 1) False True 1 0 (False, 0))] [(Moeda, (1.5,1.5))] Personagem (1, 0) Jogador (0.5, 0.5) Este (1, 1) False False 3 0 (False, 0))
True

>>> valida (Jogo Mapa ((1.5,1.5),Este) (1.5,1.5) [[Vazio, Alcapao, Vazio], [Vazio, Escada, Vazio], [Vazio, Vazio, Plataforma]] [(Personagem (1, 0) Fantasma (1.5, 1.5) Este (1, 1) False False 2 0 (False, 0))] [(Martelo,(2.5,2.5))] Personagem (1, 0) Jogador (2.5, 2.5) Este (2, 1) False True 3 0 (False, 0))
False

-}

valida :: Jogo -- ^ Game Definition
       -> Bool -- ^ Logical value of checking all the requested conditions
valida (Jogo m i c p)
   = (all (==True) [f1,f2,f3,f4,f5,f6,f7,f8])
   where
    f1 = chao m
    f2 = ressalto i p
    f3 = not (choquePs m i)
    f4 = minInimigos i
    f5 = vidaFan (vidaFanAux i)
    f6 = checkBloc m (extEscada m (findBlocEAux m))
    f7 = alcLargo p
    f8 = colEntidades i p c m

{- |
Function, which checks whether a map has a "floor", i.e. a platform that prevents the player from falling off the map.
-}

chao :: Mapa -- ^ Game map
     -> Bool -- ^ Logical value of the Map floor being made entirely of "Plataforma" blocks
chao (Mapa _ _ m) = all (== Plataforma) (last m)

{- |
Function, which checks that all enemies have the "ressalta" property @True@ and the player @False@.
-}

ressalto :: [Personagem] -- ^ Enemies List
         -> Personagem -- ^ Player character 
         -> Bool -- ^ Logical value of the "ressalta" property to be __True__ on enemies and __False__ on the Player
ressalto li (Personagem _ _ _ _ _ _ c2 _ _ _)
    = all (== True) (map colc li) && c2 == False
    where
        colc :: Personagem -> Bool
        colc (Personagem _ _ _ _ _ _ c1 _ _ _) = c1

{- |
Function that checks that the player's starting position does not coincide with the starting position of another character.
-}

choquePs :: Mapa -- ^ Game map
         -> [Personagem] -- ^ Enemies list
         -> Bool -- ^ Logical value of the enemies' spawning place being different from the Player's spawn
choquePs (Mapa (p1,_) _ _) li
   = any (== (acC p1)) (map colp li)
    where
        colp :: Personagem -- ^ Entity
             -> Posicao -- ^ Entity Position
        colp (Personagem _ _ p2 _ _ _ _ _ _ _) = acC p2

{- |
Function, which checks that the minimum number of enemies on the map is 2 (two).
-}

minInimigos :: [Personagem] -- ^ Enemies list
            -> Bool -- ^ Logical value of the minimum number of enemies on the map being more or less than two
minInimigos i = length i >= 2

{- |
Function that checks if all the cases studied in the __vidaFanAux__ function are true.
-}

vidaFan :: [Bool] -- ^ List of logical values calculated in "vidaFan"
        -> Bool -- ^ Logic value of all list 
vidaFan l = all (True==) l

{- |
Function that checks if all enemies of type @Fantasma@ have exactly 1 (one) life.
-}

vidaFanAux :: [Personagem] -- ^ Enemies list on map
           -> [Bool] -- ^ List of logical values if the life of the "Fantasma" type is equal to 1
vidaFanAux [] = []
vidaFanAux ((Personagem _ t _ _ _ _ _ v _ _):ps)
   | t == MacacoMalvado || t == Jogador = True : vidaFanAux ps
   | t == Fantasma && v == 1 = True : vidaFanAux ps
   | otherwise = False : vidaFanAux ps

{- |
Function that checks that all the stairs on the map do not start or end in trapdoors, and that at least one of their ends is of type @Plataforma@.
-}

checkBloc :: Mapa -- ^ Game map
          -> [(Bloco, Bloco)] -- ^ List of pairs that correspond to the block above and below the ladder, respectively.
          -> Bool -- Logical value of the two necessary conditions being met
checkBloc (Mapa _ _ m) l = if findBlocE m == [] then True else if not (null (findBlocE m)) && length l >= 1 then all (==True) (checkBlocAux l) else False

{-|
Function that receives a list of pairs of blocks above and below the stairs and returns the list of logical values of whether the stairs have at least one platform block and no trapdoor blocks.
-}

checkBlocAux :: [(Bloco,Bloco)] -- ^ List of pairs that correspond to the block above and below the ladder, respectively.
             -> [Bool] -- ^ List of logical values.
checkBlocAux [] = [True]
checkBlocAux ((x,y):r)
   | x == Plataforma && (y == Plataforma || y == Vazio || y == Escada) = True : checkBlocAux r
   | (x == Plataforma || x == Vazio || x == Escada) && y == Plataforma = True : checkBlocAux r
   | otherwise = False : checkBlocAux r

{- |
Function that receives the matrix that defines the map and the list of ladder block positions on the map (calculated by findblocE), and returns a list of pairs that correspond to the block above and below the ladders, respectively.
-}

extEscada :: Mapa -- ^ Game map 
          -> [Posicao] -- ^ List of ladder block positions 
          -> [(Bloco, Bloco)] -- ^ List of pairs that correspond to the block above and below the ladder, respectively.
extEscada _ [] = []
extEscada (Mapa i1 i2 m) ((l, c):re)
    | l > 0 && l < fromIntegral (length m) - 1 && c >= 0 && c < fromIntegral (length (head m)) =
        let lAn = floor (l - 1)
            lAt = floor l
            lS = floor (l + 1)
            cA = floor c
            cima = m !! lAn !! cA
            baixo = m !! lS !! cA
            ext = (cima, baixo)
            r = extEscada (Mapa i1 i2 m) re
        in if cima /= Alcapao && baixo /= Alcapao && (cima == Plataforma || baixo == Plataforma)
            then ext : r
            else r
    | otherwise = extEscada (Mapa i1 i2 m) re

findBlocEAux :: Mapa -- ^ Game map
             -> [Posicao] -- ^ List of coordinates in the top left corner of the block
findBlocEAux (Mapa _ _ m) = findBlocE m

findBlocE :: [[Bloco]] -- ^ Map definition matrix
         -> [Posicao] -- ^ List of coordinates in the top left corner of the block
findBlocE [] = []
findBlocE m =
  concatMap (\(l, bs) -> [(fromIntegral c, fromIntegral l) | (b, c) <- zip bs [0..], b == Escada]) $ zip [0..] m

{- |
Function that checks if a player is smaller than a Trapdoor block on the map.
-}

alcLargo :: Personagem -- ^ Player character 
         -> Bool -- ^ Logical value of the Player Character being smaller than a Trapdoor block on the map
alcLargo (Personagem _ _ _ _ (g,c) _ _ _ _ _) = g <= 1 && c <= 1

{- |
Function that checks if any character or collectible is "inside" an Empty block on the map.
-}

colEntidades :: [Personagem] -- ^ Enemies List 
             -> Personagem -- ^ Player character
             -> [(Colecionavel, Posicao)] -- ^ Collectibles list
             -> Mapa -- ^ Map definition matrix
             -> Bool -- ^ Logical value of a character or collectible being "inside" an empty map block
colEntidades ps p cs (Mapa i1 i2 m) = all (`elem` (centra ((findBlocV m) ++ (findBlocE m)))) (colEntidadesAux ps p cs)

findBlocV :: [[Bloco]] -- ^ Map definition matrix
         -> [Posicao] -- ^ List of coordinates in the top left corner of the block
findBlocV [] = []
findBlocV m =
  concatMap (\(l, bs) -> [(fromIntegral c, fromIntegral l) | (b, c) <- zip bs [0..], b == Vazio]) $ zip [0..] m

{- |
Function that calculates the coordinates of the centre points of a block.
-}


centra :: [Posicao] -- ^ List of coordinates in the top left corner of the block
       -> [Posicao] -- ^ List of coordinates in the center of the block
centra [] = []
centra m = map (\(l, ls) -> (l + 0.5, ls + 0.5)) m

{- |
Function that calculates the list of positions of all entities simultaneously.
-}

colEntidadesAux :: [Personagem] -- ^ Enemies list
                -> Personagem -- ^ Player character 
                -> [(Colecionavel, Posicao)] -- ^ Collectibles list
                -> [Posicao] -- ^ List of positions of all entities on the map
colEntidadesAux ps (Personagem _ _ (x2, y2) _ _ _ _ _ _ _) cs =
   map (acC . (\(Personagem _ _ (x1, y1) _ _ _ _ _ _ _)-> (x1, y1))) ps ++ [acC (x2, y2)] ++ map snd cs