{-|
Module      : Task3
Description : Moves characters in the game
Copyright   : Joao Pedro Delgado Teixeira <a106836@alunos.uminho.pt>
              Ricardo Miguel da Silva Morais <a106935@alunos.uminho.pt>

Module for the realisation of Task3 of LI1 in 2023/24.
-}
module Task3 where

import LI12324

import Task1

import Task4

{-|

The __movimetna__ function must animate all the characters, i.e. calculate their new positions and their consequences.
To do this, the function has to test a __non-exaustive__ list of events and conditions. To do this, we use auxiliary functions whose purpose is to check conditions and make changes to the game, such as:

* @perderVida@: function that receives the list of enemies on a map and returns this same list with the lives removed from the supposed enemies.
* @gamaIni@: function that filters out enemies with 1 life and suppresses those with 0 lives.
* @menosAl@: function that updates the game map matrix when a trapdoor is stepped on.
* @fallVoid@: function that changes the character's speed if they are not on a platform.
* @danoDuro@: function that removes a life from the player if they come into contact with an enemy.
* @recColec@: function that updates the game when a collectible is acquired, and depending on the collectible makes changes to either the player's points or the player's character properties.
* @noPassPar@: function that slows the player down horizontally and/or vertically, depending on their collision with the edges of the map or with platform blocks.

== Properties:
=== Main function "movimenta":
prop> movimenta :: Semente -> Tempo -> Jogo -> Jogo
prop> movimenta s t j@(Jogo m@(Mapa i1 i2 i3) i c p) =
prop>     Jogo mr (movInimigo t (randInimigos m s (gamaIni (perderVida i p)))) co (movJogador t (danoDuro i (noPassPar m (fallVoid m jo))))
prop>     where
prop>       mr = (Mapa i1 i2 (menosAl m p))
prop>       ri = movInimigo t (randInimigos m s i)
prop>       (co,jo) = recColec j

=== Auxiliary function "perderVida":
prop> perderVida :: [Personagem] -> Personagem -> [Personagem]
prop> perderVida [] _ = []
prop> perderVida ((Personagem i1 i2 i3 i4 i5 i6 i7 v i9 (d,id2)):ls) p
prop>  | acH (hitPersona (Personagem i1 i2 i3 i4 i5 i6 i7 v i9 (d,id2))) == hitDano p && (danAtivo p == True) = ((Personagem i1 i2 i3 i4 i5 i6 i7 (v - 1) i9 (d,id2))) : perderVida ls p
prop>  | otherwise = (Personagem i1 i2 i3 i4 i5 i6 i7 v i9 (d,id2)) : perderVida ls p

==== Auxiliary function "hitDano":
prop> hitDano :: Personagem -> Hitbox
prop> hitDano (Personagem i1 i2 (x, y) o (g, c) i3 i4 i5 i6 i7)
prop>     | o == Este   = (somPar (fst (acH (hitPersona p))) (1,0), somPar (snd (acH (hitPersona p))) (1,0))
prop>     | o == Oeste  = (somPar (fst (acH (hitPersona p))) (-1,0), somPar (snd (acH (hitPersona p))) (-1,0))
prop>     | otherwise   = acH (hitPersona (Personagem i1 i2 (x, y) o (g, c) i3 i4 i5 i6 i7))
prop>     where
prop>         p = (Personagem i1 i2 (x, y) o (g, c) i3 i4 i5 i6 i7)

==== Auxiliary function "danAtivo":
prop> danAtivo :: Personagem -> Bool
prop> danAtivo (Personagem _ _ _ _ _ _ _ _ _ (d,_))
prop>   = if d == True
prop>     then True
prop>     else False

=== Auxiliary function "gamaIni":
prop> gamaIni :: [Personagem] -> [Personagem]
prop> gamaIni i = filter (\(Personagem _ _ _ _ _ _ _ v _ _) -> v > 0) i

=== Auxiliary function "menosAl":
prop> menosAl :: Mapa -> Personagem -> [[Bloco]]
prop> menosAl mp@(Mapa _ _ m) p@(Personagem _ e (x, y) _ _ _ _ _ _ _)
prop>         | e == Jogador && elem (fst (hitPersona (acP p))) (findBlocA m) = substituiMatriz m (yf,xf) Vazio
prop>         | otherwise = m
prop>       where (xf,yf) = fst (hitPersona (acP p))

==== Auxiliary function "substituiMatriz":
prop> substituiMatriz :: [[Bloco]] -> Posicao -> Bloco -> [[Bloco]]
prop> substituiMatriz [] _ _ = []
prop> substituiMatriz (l:t) (0,c) v = (substitui l c v) : t
prop> substituiMatriz (l:t) (linha, coluna) v = l : substituiMatriz t (linha - 1, coluna) v

==== Auxiliary function "substitui":
prop> substitui :: [Bloco] -> Double -> Bloco -> [Bloco]
prop> substitui [] _ _ = []
prop> substitui (x:t) 0 y = y:t
prop> substitui (x:t) p y = x : substitui t (p-1) y

==== Auxiliary function "findBlocA":
prop> findBlocA :: [[Bloco]] -> [Posicao] 
prop> findBlocA [] = []
prop> findBlocA m =
prop>   concatMap (\(l, bs) -> [(fromIntegral c, fromIntegral l) | (b, c) <- zip bs [0..], b == Alcapao]) $ zip [0..] m

=== Auxiliary function "fallVoid":
prop> fallVoid :: Mapa -> Personagem -> Personagem
prop> fallVoid (Mapa _ _ b) p@(Personagem (vx,vy) _ (x, y) _ _ i6 _ _ _ _)
prop>     | fst (hitPersona (acP p)) `elem` findBlocP b = p
prop>     | i6 = p
prop>     | otherwise = p { velocidade = (vx, vy + 10) }

=== Auxiliary function "danoDuro":
prop> danoDuro :: [Personagem] -> Personagem -> Personagem
prop> danoDuro [] p = p
prop> danoDuro l p@(Personagem _ _ _ _ _ _ _ x _ _) | any (==True) (colisoesDano l p) = p { vida = (x - 1) }
prop>                                               | otherwise = p

==== Auxiliary function "colisoesDano":
prop> colisoesDano :: [Personagem] -> Personagem -> [Bool]
prop> colisoesDano [] p = [False]
prop> colisoesDano (h:ht) p = colisoesPersonagens h p : colisoesDano ht p

=== Auxiliary function "recColec":
prop> recColec :: Jogo -> Jogo
prop> recColec jogo@(Jogo m@(Mapa m1 m2 m3) i [] p) = ([],p)
prop> recColec (Jogo m i (colec@(c,pov):r) p@(Personagem _ _ pos _ _ _ _ _ moeda dano))
prop>    | colPersona (acH (hitColec pov)) (acH (hitPersona p)) && c == Martelo = (r, p { aplicaDano = (True,10) } )
prop>    | colPersona (acH (hitColec pov)) (acH (hitPersona p)) && c == Moeda   = (r, p { pontos = (moeda + 1)} )
prop>    | otherwise = insColec colec (recColec (Jogo m i r p))

==== Auxiliary function "insColec":
prop> insColec :: (Colecionavel, Posicao) -> ([(Colecionavel, Posicao)],Personagem) -> ([(Colecionavel, Posicao)],Personagem)
prop> insColec c (r,p) = (c:r, p)

==== Auxiliary function "hitColec":
prop> hitColec :: Posicao -> Hitbox 
prop> hitColec (x,y)
prop>      = ((xe1,yd1),(xe2,yd2))
prop>      where
prop>         xe1 = x - (1 / 2)
prop>         yd1 = y + (1 / 2)
prop>         xe2 = x + (1 / 2)
prop>         yd2 = y - (1 / 2)

=== Auxiliary function "noPassPar":
prop> noPassPar :: Mapa -> Personagem -> Personagem
prop> noPassPar mp@(Mapa _ _ m) p@(Personagem (vx, vy) _ (x, y) _ (g, c) _ _ _ _ _)
prop>     |  t1 && t2 = p {velocidade = (0,0)}
prop>     |  t1 = p {velocidade = (0,vy)}
prop>     |  t2 = p {velocidade = (vx,0)}
prop>     |  otherwise = p
prop>   where
prop>     t1 = colPar m p
prop>     t2 = any (`elem` [(x2,y2)]) (findBlocP m)
prop>      where
prop>       (x2,y2) = fst (hitPersona p)

==== Auxiliary function "movJogador":
prop> movJogador :: Personagem -> Personagem
prop> movJogador t (Personagem (vx,vy) i2 (x, y) i4 i5 i6 i7 i8 i9 i10) =
prop>         (Personagem (0,0) i2 (x + (vx * t), y + (vy * t)) i4 i5 i6 i7 i8 i9 i10)

==== Auxiliary function "movInimigo":
prop> movInimigo :: [Personagem] -> [Personagem]
prop> movInimigo t i = map (movJogador t) i

==== Auxiliary function "randInimigos":
prop> randInimigos m s [] = []
prop> randInimigos m s [] = []
prop> randInimigos m s (i:is) = randInimigosAux m i (last l) : randInimigos m s is
prop>         where
prop>           l = geraAleatorios s (length (i:is))

===== Auxiliary function "randInimigosAux"
prop> randInimigosAux :: Mapa -> Personagem -> Int -> Personagem
prop> randInimigosAux m@(Mapa m1 m2 m3) i@(Personagem (v1,v2) i2 i3 i4 i5 i6 i7 i8 i9 i10) n =
prop>   case n `mod` 4 of
prop>     0 -> aD m3 i
prop>     1 -> aE m3 i
prop>     2 -> aS m3 i
prop>     3 -> aC m3 i
prop>     _ -> i
prop>     where
prop>       aD :: [[Bloco]] -> Personagem -> Personagem
prop>       aD m3 i = if movDireita m3 i then (Personagem (v1-1,v2) i2 i3 Oeste i5 i6 i7 i8 i9 i10) else (Personagem (v1+1,v2) i2 i3 Este i5 i6 i7 i8 i9 i10)
prop>       aE :: [[Bloco]] -> Personagem -> Personagem
prop>       aE m3 i = if movEsquerda m3 i then (Personagem (v1+1,v2) i2 i3 Este i5 i6 i7 i8 i9 i10) else (Personagem (v1-1,v2) i2 i3 Oeste i5 i6 i7 i8 i9 i10)
prop>       aS :: [[Bloco]] -> Personagem -> Personagem
prop>       aS m3 i = if subEscada m3 i then (Personagem (v1,v2-1) i2 i3 i4 i5 i6 i7 i8 i9 i10) else i
prop>       aC :: [[Bloco]] -> Personagem -> Personagem
prop>       aC m3 i = if subEscada m3 i then (Personagem (v1,v2+1) i2 i3 i4 i5 i6 i7 i8 i9 i10) else i

== Examples of use:
>>> movimenta (fromIntegral 1) 2 (Jogo (Mapa ((2.5, 1.5), Este) (5, 5) [[Plataforma, Plataforma, Plataforma, Plataforma, Plataforma], [Vazio, Vazio, Vazio, Vazio, Vazio], [Vazio, Alcapao, Vazio, Vazio, Vazio], [Vazio, Vazio, Vazio, Vazio, Vazio], [Plataforma, Plataforma, Plataforma, Plataforma, Plataforma]]) ([(Personagem (1, 0) Fantasma (2.5, 1.5) Oeste (1, 1) False True 1 0 (False, 0))]) ([(Moeda, (1.5,1.5)), (Martelo,(4.5,4.5))]) (Personagem (0, 0) Jogador (1.5, 1.5) Este (1, 1) False True 3 0 (True, 5)))
Jogo Mapa ((2.5, 1.5), Este) (5, 5) [[Plataforma, Plataforma, Plataforma, Plataforma, Plataforma], [Vazio, Vazio, Vazio, Vazio, Vazio], [Vazio, Vazio, Vazio, Vazio, Vazio], [Vazio, Vazio, Vazio, Vazio, Vazio], [Plataforma, Plataforma, Plataforma, Plataforma, Plataforma]] [] [(Martelo,(4.5,4.5))] Personagem (0, 10) Jogador (1.5, 1.5) Este (1, 1) False True 2 1 (True, 5)

>>> movimenta (fromIntegral 1) 2 (Jogo (Mapa ((2.5, 1.5), Este) (5, 5) [[Plataforma, Plataforma, Plataforma, Plataforma, Plataforma], [Vazio, Vazio, Vazio, Vazio, Vazio], [Vazio, Plataforma, Vazio, Vazio, Vazio], [Vazio, Vazio, Vazio, Vazio, Vazio], [Plataforma, Plataforma, Plataforma, Plataforma, Plataforma]]) ([(Personagem (1, 0) Fantasma (3.5, 3.5) Oeste (1, 1) False True 1 0 (False, 0))]) ([(Martelo,(4.5,4.5))]) (Personagem (0, 0) Jogador (1.5, 1.5) Este (1, 1) False True 3 0 (False, 0)))
Jogo Mapa ((2.5, 1.5), Este) (5, 5) [[Plataforma, Plataforma, Plataforma, Plataforma, Plataforma], [Vazio, Vazio, Vazio, Vazio, Vazio], [Vazio, Plataforma, Vazio, Vazio, Vazio], [Vazio, Vazio, Vazio, Vazio, Vazio], [Plataforma, Plataforma, Plataforma, Plataforma, Plataforma]] [(Personagem (1, 0) Fantasma (3.5, 3.5) Oeste (1, 1) False True 1 0 (False, 0))] [(Martelo,(4.5,4.5))] Personagem (0, 0) Jogador (1.5, 1.5) Este (1, 1) False True 3 0 (False, 0)

-}

{-|
Function that must animate all the characters, i.e. calculate their new positions and their consequences.
-}

movimenta :: Semente -- ^ Game seed
          -> Tempo -- ^ Game time
          -> Jogo -- ^ Game
          -> Jogo -- ^ Updated game

movimenta s t j@(Jogo m@(Mapa i1 i2 i3) i c p) =
    Jogo mr (movInimigo t (randInimigos m s (gamaIni (perderVida i p)))) co (movJogador t (danoDuro i (noPassPar m (fallVoid m jo))))
    where
      mr = (Mapa i1 i2 (menosAl m p))
      ri = movInimigo t (randInimigos m s i)
      (co,jo) = recColec j

{-|
Function that changes the position of the player's character according to speed
-}

movJogador :: Tempo -- ^ Game time
           -> Personagem -- ^ Player character
           -> Personagem -- ^ Updated player character
movJogador t (Personagem (vx,vy) i2 (x, y) i4 i5 i6 i7 i8 i9 i10) =
        (Personagem (0,0) i2 (x + (vx * t), y + (vy * t)) i4 i5 i6 i7 i8 i9 i10)

{-|
Function that changes the position of enemies according to their speed
-}

movInimigo :: Tempo -- ^ Game time
           -> [Personagem] -- ^ Enemies list
           -> [Personagem] -- ^ Updated enemies list
movInimigo t i = map (movJogador t) i

{-|
Function that generates random movement to a list of enemies
-}

randInimigos :: Mapa -- ^ Game map
             -> Semente -- ^ Game seed
             -> [Personagem] -- ^ Enemies list
             -> [Personagem] -- ^ Updated enemies list
randInimigos m s [] = []
randInimigos m s (i:is) = randInimigosAux m i (last l) : randInimigos m s is
        where
          l = geraAleatorios s (length (i:is))

{-|
Function that assists "randEnemies"
-}

randInimigosAux :: Mapa -- ^ Game map
                -> Personagem -- ^ Game character
                -> Int -- ^ Randomly generated 18-digit number
                -> Personagem -- ^ Updated character
randInimigosAux m@(Mapa m1 m2 m3) i@(Personagem (v1,v2) i2 i3 i4 i5 i6 i7 i8 i9 i10) n =
  case n `mod` 4 of
    0 -> aD m3 i
    1 -> aE m3 i
    2 -> aS m3 i
    3 -> aC m3 i
    _ -> i
    where
      aD :: [[Bloco]] -> Personagem -> Personagem
      aD m3 i = if movDireita m3 i then (Personagem (v1-1,v2) i2 i3 Oeste i5 i6 i7 i8 i9 i10) else (Personagem (v1+1,v2) i2 i3 Este i5 i6 i7 i8 i9 i10)
      aE :: [[Bloco]] -> Personagem -> Personagem
      aE m3 i = if movEsquerda m3 i then (Personagem (v1+1,v2) i2 i3 Este i5 i6 i7 i8 i9 i10) else (Personagem (v1-1,v2) i2 i3 Oeste i5 i6 i7 i8 i9 i10)
      aS :: [[Bloco]] -> Personagem -> Personagem
      aS m3 i = if subEscada m3 i then (Personagem (v1,v2-1) i2 i3 i4 i5 i6 i7 i8 i9 i10) else i
      aC :: [[Bloco]] -> Personagem -> Personagem
      aC m3 i = if subEscada m3 i then (Personagem (v1,v2+1) i2 i3 i4 i5 i6 i7 i8 i9 i10) else i

{-|
Function that receives the list of enemies on a map and returns this same list with the lives taken from the supposed enemies.
-}

perderVida :: [Personagem] -- ^ Enemies list
           -> Personagem -- ^ Player character
           -> [Personagem] -- ^ Updated enemies list
perderVida [] _ = []
perderVida ((Personagem i1 i2 i3 i4 i5 i6 i7 v i9 (d,id2)):ls) p
 | acH (hitPersona (Personagem i1 i2 i3 i4 i5 i6 i7 v i9 (d,id2))) == hitDano p && (danAtivo p == True) = ((Personagem i1 i2 i3 i4 i5 i6 i7 (v - 1) i9 (d,id2))) : perderVida ls p
 | otherwise = (Personagem i1 i2 i3 i4 i5 i6 i7 v i9 (d,id2)) : perderVida ls p

{-|
Function that calculates the damage hitbox of the Player's character.
-}

hitDano :: Personagem -- ^ Player character
        -> Hitbox -- ^ Player damage hitbox
hitDano (Personagem i1 i2 (x, y) o (g, c) i3 i4 i5 i6 i7)
    | o == Este   = (somPar (fst (acH (hitPersona p))) (1,0), somPar (snd (acH (hitPersona p))) (1,0))
    | o == Oeste  = (somPar (fst (acH (hitPersona p))) (-1,0), somPar (snd (acH (hitPersona p))) (-1,0))
    | otherwise   = acH (hitPersona (Personagem i1 i2 (x, y) o (g, c) i3 i4 i5 i6 i7))
    where
        p = (Personagem i1 i2 (x, y) o (g, c) i3 i4 i5 i6 i7)

somPar :: Posicao -> Posicao -> Posicao
somPar (x,y) (a,b) = (x+a,y+b)

{-|
function that checks if the "aplicaDano" property on the Player character is _False_
-}

danAtivo :: Personagem -- ^ Player character
         -> Bool -- ^ Logical value of the "aplicaDano" property being active
danAtivo (Personagem _ _ _ _ _ _ _ _ _ (d,_))
  = if d == True
    then True
    else False

{-|
Function that filters out enemies with 1 life and suppresses those with 0 lives.
-}

gamaIni :: [Personagem] -- ^ Enemies list
        -> [Personagem] -- ^ List of enemies with 1 (one) life
gamaIni i = filter (\(Personagem _ _ _ _ _ _ _ v _ _) -> v > 0) i

{-|
Function that changes the character's speed if he is not on a platform.
-}

fallVoid :: Mapa -- ^ Game map 
         -> Personagem -- ^ Player character 
         -> Personagem -- ^ Player character with updated speed.
fallVoid (Mapa _ _ b) p@(Personagem (vx,vy) _ (x, y) _ _ i6 _ _ _ _)
    | fst (hitPersona (acP p)) `elem` (findBlocP b) = p
    | i6 = p
    | otherwise = p { velocidade = (vx, vy + 10) }

{-|
Function that removes a life from the player if they come into contact with an enemy.
-}

danoDuro :: [Personagem] -- ^ Enemies list
         -> Personagem -- ^ Player character 
         -> Personagem -- ^ Player character with updated life.
danoDuro [] p = p
danoDuro l p@(Personagem _ _ _ _ _ _ _ x _ _) | any (==True) (colisoesDano l p) = p { vida = (x - 1) }
                                              | otherwise = p

{-|
Function that checks if any enemies are colliding with the player.
-}

colisoesDano :: [Personagem] -- ^ Enemies list
             -> Personagem -- ^ Player character 
             -> [Bool] -- ^ List of the truth values of enemies in contact with the player
colisoesDano [] p = [False]
colisoesDano (h:ht) p = colisoesPersonagens h p : colisoesDano ht p

{-|
Function that updates the game when a collectible is acquired, and depending on the collectible makes changes to either the player's points or the player's character properties.
-}

recColec :: Jogo -- ^ Game
         -> ([(Colecionavel, Posicao)],Personagem) -- ^ Updated collectibles
recColec jogo@(Jogo m@(Mapa m1 m2 m3) i [] p) = ([],p)
recColec (Jogo m i (colec@(c,pov):r) p@(Personagem _ _ pos _ _ _ _ _ moeda dano))
   | colPersona (acH (hitColec pov)) (acH (hitPersona p)) && c == Martelo = (r, p { aplicaDano = (True,10) } )
   | colPersona (acH (hitColec pov)) (acH (hitPersona p)) && c == Moeda   = (r, p { pontos = (moeda + 1)} )
   | otherwise = insColec colec (recColec (Jogo m i r p))

insColec :: (Colecionavel, Posicao) -- ^ Collectible and its previously processed position
         -> ([(Colecionavel, Posicao)],Personagem) -- ^ Remaining collectibles and character
         -> ([(Colecionavel, Posicao)],Personagem) -- ^ Complete (updated) list of collectibles and characters
insColec c (r,p) = (c:r, p)

{-|
Function that calculates the hitbox of a collectible.
-}

hitColec :: Posicao -- ^ Position on the map of a collectible
         -> Hitbox -- ^ Collectible hitbox
hitColec (x,y)
     = ((xe1,yd1),(xe2,yd2))
     where
        xe1 = x - (1 / 2)
        yd1 = y + (1 / 2)
        xe2 = x + (1 / 2)
        yd2 = y - (1 / 2)

{-|
Function that updates the game map matrix when a trapdoor is stepped on.
-}

menosAl :: Mapa -- ^ Game map 
        -> Personagem -- ^ Player character
        -> [[Bloco]] -- ^ Map matrix with updated trapdoors
menosAl mp@(Mapa _ _ m) p@(Personagem _ e (x, y) _ _ _ _ _ _ _)
        | e == Jogador && elem (fst (hitPersona (acP p))) (findBlocA m) = substituiMatriz m (yf,xf) Vazio
        | otherwise = m
      where (xf,yf) = fst (hitPersona (acP p))

{-|
Function that replaces a block in an array with another block.
-}

substituiMatriz :: [[Bloco]] -- ^ Map Matrix
                -> Posicao -- ^ Position of a block on the map
                -> Bloco -- ^ Block
                -> [[Bloco]] -- ^ Matrix with updated block
substituiMatriz [] _ _ = []
substituiMatriz (l:t) (0,c) v = (substitui l c v) : t
substituiMatriz (l:t) (linha, coluna) v = l : substituiMatriz t (linha - 1, coluna) v

{-|
Function that replaces a block in a list of blocks with another block.
-}

substitui :: [Bloco] -- ^ List of blocks
          -> Double -- ^ Position on the list
          -> Bloco -- ^ Block
          -> [Bloco] -- ^ Updated list
substitui [] _ _ = []
substitui (x:t) 0 y = y:t
substitui (x:t) p y = x : substitui t (p-1) y

{-|
Function that calculates the list of positions of all the trapdoor blocks in a map matrix.
-}

findBlocA :: [[Bloco]] -- ^ Map definition matrix
          -> [Posicao] -- ^ List of coordinates for the top left corner of the trapdoor block
findBlocA [] = []
findBlocA m =
  concatMap (\(l, bs) -> [(fromIntegral c, fromIntegral l) | (b, c) <- zip bs [0..], b == Alcapao]) $ zip [0..] m

{-|
Function that slows the player down horizontally and/or vertically, depending on their collision with the edges of the map or platform blocks.
-}

noPassPar :: Mapa -- ^ Game map
          -> Personagem -- ^ Player character 
          -> Personagem -- ^ Player character with updated speed.
noPassPar mp@(Mapa _ _ m) p@(Personagem (vx, vy) _ (x, y) _ (g, c) _ _ _ _ _)
    |  t1 && t2 = p {velocidade = (0,0)}
    |  t1 = p {velocidade = (0,vy)}
    |  t2 = p {velocidade = (vx,0)}
    |  otherwise = p
  where
    t1 = colPar m p
    t2 = any (`elem` [(x2,y2)]) (findBlocP m)
     where
      (x2,y2) = fst (hitPersona p)