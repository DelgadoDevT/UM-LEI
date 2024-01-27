module Exame1617 where

-- ** 1 ** 

-- a

unlines' :: [String] -> String
unlines' [] = ""
unlines' (l:ls) = l ++ (if null ls then "" else "\n") ++ unlines' ls

-- b

(\\) :: (Eq a) => [a] -> [a] -> [a]
(\\) l [] = l
(\\) [] _ = []
(\\) (l:ls) (r:rs)
   | l == r = (\\) ls rs 
   | otherwise = l : (\\) ls (r:rs)

-- ** 2 **

data Seq a = Nil | Inicio a (Seq a) | Fim (Seq a) a deriving Show

s1 :: Seq Int
s1 = Inicio 2 (Inicio 4 (Fim Nil 8))

-- a

primeiro :: Seq a -> a
primeiro (Inicio e _) = e 
primeiro (Fim Nil e) = e
primeiro (Fim l e) = primeiro l

-- b

semUltimo :: Seq a -> Seq a 
semUltimo (Fim Nil e) = Nil 
semUltimo (Inicio e Nil) = Nil
semUltimo (Inicio e l) = Inicio e (semUltimo l)
semUltimo (Fim l e) = Fim (semUltimo l) e

-- ** 3 **

data BTree a = Empty | Node a (BTree a) (BTree a) deriving Show

b1 :: BTree Int
b1 = (Node 5 (Node 4 (Empty) (Node 2 Empty Empty)) (Empty))

-- a

prune :: Int -> BTree a -> BTree a 
prune 0 Empty = Empty
prune n (Node f l r)  
  | n > 0 = Node f (prune (n-1) l) (prune (n-1) r)
  | otherwise = Empty

-- b

semMinimo :: (Ord a) => BTree a -> BTree a 
semMinimo (Node f Empty r) = Empty
semMinimo (Node f (Node f' Empty Empty) r) = Node f Empty r
semMinimo (Node f l r) = Node f (semMinimo l) r  

-- ** 4 **

type Tabuleiro = [String]

exemplo :: Tabuleiro
exemplo = ["..R.",
           "R...",
           "...R",
           ".R.."]

-- a

posicoes :: Tabuleiro -> [(Int, Int)]
posicoes t = [(c, l) | (l, c) <- zip [0..] t, (c, 'R') <- zip [0..] c]

-- b

valido :: Tabuleiro -> Bool
valido t = not (at || ats || atd)
  where
    pr = posicoes t
    at :: Bool
    at = any (\pos -> length (filter (\p -> snd p == snd pos) pr) > 1) pr
    ats :: Bool
    ats = any (\pos -> length (filter (\p -> fst p == fst pos) pr) > 1) pr
    atd :: Bool
    atd = any (\pos -> length (filter (\p -> abs (fst p - fst pos) == abs (snd p - snd pos)) pr) > 1) pr

-- c

bemFormado :: Int -> Tabuleiro -> Bool
bemFormado n t = length t == n && all (\l -> length l == n && nRai 1 l) t

nRai :: Int -> String -> Bool
nRai n l = length (filter (== 'R') l) == n