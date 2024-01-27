{-|
Module      : Task1
Description : Check collisions
Copyright   : Joao Pedro Delgado Teixeira <a106836@alunos.uminho.pt>
              Ricardo Miguel da Silva Morais <a106935@alunos.uminho.pt>

Module for the realisation of Task1 of LI1 in 2023/24.
-}

module Task1 where

import LI12324

{-|

The __colisoesParede__ function must test whether a character is colliding with any of the map's boundaries (sides or top) or with any platform blocks.
To obtain this result, we use auxiliary functions that perform the various operations required, such as:

* @acC@: function that centres the positions of the game entities
* @acP@: function that centres the game entities
* @acH@: function that centres the hitboxes of game entities
* @colPar@: function that tests whether a character collides with the map boundaries
* @colBloco@: function that tests whether a character collides with a platform block
* @hitPersona@: function that calculates a character's hitbox, defined by a pair of positions, these being the bottom left and top right, respectively
* @hitBlocos@: function that calculates the hitbox of a list of platforms
* @findBlocP@: function that receives the map definition matrix and returns a list with the coordinates of the top left corner of each plataform block

== Properties:
=== Main function "colisoesParede":
prop> colisoesParede :: Mapa -> Personagem -> Bool
prop> colisoesParede (Mapa _ _ m) p
prop>     | colPar m (acP p) || colBloco (hitBlocos $ findBlocP m) (hitPersona (acP p)) = True
prop>     | otherwise = False

=== Auxiliary function "acC":
prop> acC :: Posicao -> Posicao
prop> acC (x, y) = (ar x, ar y)
prop>   where
prop>     ar :: Double -> Double
prop>     ar n
prop>       | dP == 0.5 = n
prop>       | dP >= 0.9 = fromIntegral (ceiling n) - 0.5
prop>       | otherwise = fromIntegral (floor n) + 0.5
prop>       where
prop>         dP = n - fromIntegral (floor n)

=== Auxiliary function "acP":
prop> acP :: Personagem -> Personagem
prop> acP (Personagem i1 i2 i3 i4 i5 i6 i7 i8 i9 i10) = Personagem i1 i2 (acC i3) i4 i5 i6 i7 i8 i9 i10

=== Auxiliary function "acH":
prop> acH :: Hitbox -> Hitbox 
prop> acH ((a,b),(c,d)) = (acC (a,b), acC (c,d))

=== Auxiliary function "colPar":
prop> colPar :: [[Bloco]] -> Personagem -> Bool
prop> colPar m (Personagem _ _ (x, y) _ (g, c) _ _ _ _ _) 
prop> | x - (g / 2) <= 0 || x + (g / 2) >= th || y - (c / 2) <= 0 || y + (c / 2) >= tv = True
prop> | otherwise = False
prop> where
prop> th = fromIntegral $ length $ head m
prop> tv = fromIntegral $ length m

=== Auxiliary function "colBloco":
prop> colBloco :: [Hitbox] -> Hitbox -> Bool
prop> colBloco [] _ = False
prop> colBloco (((xi1, yi1), (xi2, yi2)):xs) ((xe1, yd1), (xe2, yd2)) =
prop> elem True [x >= min xe1 xe2 && x <= max xe1 xe2 && y >= min yd1 yd2 && y <= max yd1 yd2 | (x, y) <- v] || colBloco xs ((xe1, yd1), (xe2, yd2))
prop> where
prop> v = [(x, y) | x <- [xi1, xi2], y <- [yi1, yi2]]

=== Auxiliary function "findBlocP":
prop> findBlocP :: [[Bloco]] -> [Posicao]
prop> findBlocP [] = [] 
prop> findBlocP m =
prop> concatMap (\(l, bs) -> [(fromIntegral c, fromIntegral l) | (b, c) <- zip bs [0..], b == Plataforma]) $ zip [0..] m

=== Auxiliary function "hitBlocos":
prop> hitBlocos :: [Posicao] -> [Hitbox]
prop> hitBlocos = map (\(x, y) -> ((x, y + 1), (x + 1, y)))

=== Auxiliary function "hitPersona":
prop> hitPersona :: Personagem -> Hitbox
prop> hitPersona (Personagem _ _ (x,y) _ (g,c) _ _ _ _ _) 
prop> = ((xe1,yd1),(xe2,yd2))
prop> where 
prop> xe1 = x - (g / 2)
prop> yd1 = y + (c / 2)
prop> xe2 = x + (g / 2)
prop> yd2 = y - (c / 2)

== Examples of use: 
>>> colisoesParede (Mapa ((0, 0), Este) (5, 5) [[Plataforma, Plataforma, Plataforma], [Plataforma, Vazio, Plataforma], [Plataforma, Plataforma, Plataforma]]) (Personagem (1, 0) Jogador (1, 1) Este (1, 1) False False 3 0 (False, 0))
True

>>> colisoesParede (Mapa ((0, 0), Este) (5, 5) [[Vazio, Vazio, Vazio], [Vazio, Vazio, Vazio], [Vazio, Vazio, Vazio]]) (Personagem (1, 0) Jogador (1, 1) Este (1, 1) False False 3 0 (False, 0))
False
-}

colisoesParede :: Mapa -- ^ Game Map
                  -> Personagem -- ^ Details of the character in questiona
                  -> Bool -- ^ Logical value of the collision between map boundaries or platforms and the character
colisoesParede (Mapa _ _ m) p
    | colPar m (acP p) || colBloco (hitBlocos $ findBlocP m) (hitPersona (acP p)) = True
    | otherwise = False

{-|
Function that centres the positions of the game entities
-}

acC :: Posicao -- ^ Entity position
    -> Posicao -- ^ Centred position of the entity
acC (x, y) = (ar x, ar y)
  where
    ar :: Double -> Double
    ar n
      | dP == 0.5 = n
      | dP >= 0.9 = fromIntegral (ceiling n) - 0.5
      | otherwise = fromIntegral (floor n) + 0.5
      where
        dP = n - fromIntegral (floor n)

{-|
Function that centres the game entities
-}

acP :: Personagem -- ^ Entity
    -> Personagem -- ^ Centred position of the entity
acP (Personagem i1 i2 i3 i4 i5 i6 i7 i8 i9 i10) = Personagem i1 i2 (acC i3) i4 i5 i6 i7 i8 i9 i10

{-|
Function that centres the hitboxes of game entities
-}

acH :: Hitbox -- ^ Entity hitbox
    -> Hitbox -- ^ Centred entity hitbox
acH ((a,b),(c,d)) = (acC (a,b), acC (c,d))

{-|
Function that tests whether a character collides with the map boundaries
-}

colPar :: [[Bloco]] -- ^ Matrix that defines the map
          -> Personagem -- ^ Character in question
          -> Bool -- ^ Logical value of the character's collision with the map boundaries
colPar m (Personagem _ _ (x, y) _ (g, c) _ _ _ _ _) 
   | x - (g / 2) <= 0 || x + (g / 2) >= th || y - (c / 2) <= 0 || y + (c / 2) >= tv = True
   | otherwise = False
  where
    th = fromIntegral $ length $ head m
    tv = fromIntegral $ length m

{-|
Function that tests whether a character collides with a platform block
-}

colBloco :: [Hitbox] -- ^ List of hitboxes for each platform block
         -> Hitbox -- ^ Character hitbox
         -> Bool -- ^ Logical value of the platform blocks colliding with the character
colBloco [] _ = False
colBloco (((xi1, yi1), (xi2, yi2)):xs) ((xe1, yd1), (xe2, yd2)) =
    elem True [x >= min xe1 xe2 && x <= max xe1 xe2 && y >= min yd1 yd2 && y <= max yd1 yd2 | (x, y) <- v] || colBloco xs ((xe1, yd1), (xe2, yd2))
  where
    v = [(x, y) | x <- [xi1, xi2], y <- [yi1, yi2]]

{-|
Function that receives the map definition matrix and returns a list with the coordinates of the top left corner of each plataform block
-}

findBlocP :: [[Bloco]] -- ^ Map definition matrix
         -> [Posicao] -- ^ List of coordinates in the top left corner of the block
findBlocP [] = [] 
findBlocP m =
  concatMap (\(l, bs) -> [(fromIntegral c, fromIntegral l) | (b, c) <- zip bs [0..], b == Plataforma]) $ zip [0..] m

{-|
Function that calculates the hitbox of a list of platforms
-}

hitBlocos :: [Posicao] -- ^ List of platform positions
          -> [Hitbox] -- ^ List of platform block hitboxes
hitBlocos = map (\(x, y) -> ((x, y + 1), (x + 1, y)))

{-|
Function that calculates a character's hitbox, defined by a pair of positions, these being the bottom left and top right, respectively.
-}

hitPersona :: Personagem -- ^ Character in question
           -> Hitbox -- ^ Hitbox of the character in question
hitPersona (Personagem _ _ (x,y) _ (g,c) _ _ _ _ _) 
     = ((xe1,yd1),(xe2,yd2))
     where 
        xe1 = x - (g / 2)
        yd1 = y + (c / 2)
        xe2 = x + (g / 2)
        yd2 = y - (c / 2)

{-|

The __colisoesPersonagens__ function should test whether two characters collide, i.e. whether the hitbox of the characters in question intersects in any way.
To define it, as well as using the auxiliary function __hitPersona__ mentioned above, we also use:

* @colPersona@: Function that checks if two hitboxes intersect

== Properties:
=== Main function "colisoesPersonagens":
prop> colisoesPersonagens :: Personagem -> Personagem -> Bool
prop> colisoesPersonagens p1 p2 = colPersona (hitPersona p1) (hitPersona p2) 

=== Auxiliary function "colPersona"
prop> colPersona :: Hitbox -> Hitbox -> Bool
prop> colPersona ((xi1, yi1), (xi2, yi2)) ((xe1, yd1), (xe2, yd2)) =
prop> elem True [x >= min xe1 xe2 && x <= max xe1 xe2 && y >= min yd1 yd2 && y <= max yd1 yd2 | (x, y) <- v] 
prop> where
prop> v = [(x, y) | x <- [xi1, xi2], y <- [yi1, yi2]]

== Examples of use:
>>> colisoesPersonagens (Personagem (1, 0) Jogador (1.5, 1.5) Este (1.5, 1.5) False False 3 0 (False, 0)) (Personagem (1, 0) Jogador (0.5, 0.5) Este (1, 1) False False 3 0 (False, 0))
True

>>> colisoesPersonagens (Personagem (1, 0) Jogador (0.5, 0.5) Este (1, 1) False False 3 0 (False, 0)) (Personagem (1, 0) Jogador (2.5, 2.5) Este (1.5, 1.5) False False 3 0 (False, 0))
False

-}

colisoesPersonagens :: Personagem -- ^ Entity 1
                       -> Personagem -- ^ Entity 2
                       -> Bool -- ^ Logical value of the collision between the two entities
colisoesPersonagens p1 p2 = colPersona (hitPersona p1) (hitPersona p2) 

{-|
Function that checks if two hitboxes intersect
-}

colPersona :: Hitbox -- ^ Character's hitbox
              -> Hitbox -- ^ Character's hitbox
              -> Bool -- ^ Logical value of the collision between two entities
colPersona ((xi1, yi1), (xi2, yi2)) ((xe1, yd1), (xe2, yd2)) =
    elem True [x >= min xe1 xe2 && x <= max xe1 xe2 && y >= min yd1 yd2 && y <= max yd1 yd2 | (x, y) <- v] 
  where
    v = [(x, y) | x <- [xi1, xi2], y <- [yi1, yi2]]