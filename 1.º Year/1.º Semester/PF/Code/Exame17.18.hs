module Exame1718 where

import Data.List

-- ** 1 **

(!!!) :: [a] -> Int -> a 
(!!!) (l:ls) n
   | n == 0 = l 
   | otherwise = (!!!) ls (n-1)

-- ** 2 **

data Movimento = Norte | Sul | Este | Oeste deriving Show

posicao :: (Int,Int) -> [Movimento] -> (Int,Int)
posicao p [] = p 
posicao (x,y) (m:ms) = case m of 
                    Norte -> posicao (x,y+1) ms 
                    Sul   -> posicao (x,y-1) ms 
                    Este  -> posicao (x+1,y) ms 
                    Oeste -> posicao (x-1,y) ms

-- ** 3 **

any' :: (a -> Bool) -> [a] -> Bool
any' f l = or (map f l)

-- ** 4 **

type Mat a = [[a]]

m1 :: Mat Int
m1 = [[1,2,3], [0,4,5], [0,0,6]]

triSup :: (Num a, Eq a) => Mat a -> Bool
triSup m = all checkL [ (i, j) | i <- [1 .. nR - 1], j <- [0 .. i - 1] ]
  where
    nR = length m
    checkL :: (Int, Int) -> Bool
    checkL (i, j) = m !! i !! j == 0

-- ** 5 **

movimenta :: IO (Int,Int)
movimenta = do 
            putStrLn "Insira a direção do movimento desejado (N/S/E/O):"
            mov <- getChar
            let npos = posicao (0,0) [traduz mov] 
            putStrLn $ "Nova posição: "
            return npos

traduz :: Char -> Movimento 
traduz 'N' = Norte 
traduz 'S' = Sul 
traduz 'E' = Este
traduz 'O' = Oeste
traduz _   = error "O caractere introduzido não é valido"

-- ** 6 **

data Imagem = Quadrado Int
            | Mover (Int,Int) Imagem
            | Juntar [Imagem] deriving Show

i1 :: Imagem
i1 = Mover (5,5)
      (Juntar [Mover (0,1) (Quadrado 5),
              Quadrado 4,
              Mover (4,3) (Quadrado 2)])

i2 :: Imagem
i2 = Juntar [Mover (5,5) (Quadrado 4), 
            Mover (5,6) (Quadrado 5), 
            Mover (9,8) (Quadrado 2)]

-- a

vazia :: Imagem -> Bool
vazia (Quadrado _) = False
vazia (Mover _ i) = vazia i 
vazia (Juntar l) = all (==True) (map vazia l)

-- b

maior :: Imagem -> Maybe Int 
maior i 
  | vazia i = Nothing 
  | otherwise = case i of 
                 Quadrado p -> Just p  
                 Mover _ p  -> maior p 
                 Juntar l   -> maximum (map maior l) 

-- c

instance Eq Imagem where
    x == y = aux (mPQua x) (mPQua y)

aux :: Imagem -> Imagem -> Bool
aux (Mover v i) (Mover v2 i2) = v2 == v && i == i2
aux (Quadrado x) (Quadrado x2) = x == x2
aux (Juntar l) (Juntar l2) = length l == length l2 && all (\x -> x `elem` l) l2
aux _ _ = False

mPQua :: Imagem -> Imagem
mPQua (Quadrado a) = Quadrado a
mPQua (Mover (a, b) (Quadrado x)) = Mover (a, b) (Quadrado x)
mPQua (Mover (a, b) (Juntar i)) = Juntar (map (Mover (a, b)) i)
mPQua (Mover (a, b) (Mover (x, y) i)) = Mover (a + x, b + y) (mPQua i)
mPQua (Juntar l) = Juntar (map mPQua l)