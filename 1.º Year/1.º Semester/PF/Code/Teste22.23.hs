module Teste2223 where

import Data.List

-- ** 1 **

unlines' :: [String] -> String
unlines' [] = ""
unlines' [l] = l
unlines' (l:ls) = l ++ "\n" ++ unlines' ls

-- ** 2 **

type Mat = [[Int]]

-- a

stringToMat :: String -> Mat
stringToMat s = map stringToVector (lines s)

stringToVector :: String -> [Int]
stringToVector l = read ("[" ++ l ++ "]")

-- b

transposta :: String -> String
transposta l = unlines (map (intercalate "," . map show) (transpose (stringToMat l)))

-- ** 3 **

data Lista a = Esq a (Lista a) | Dir (Lista a) a | Nula

-- a

semUltimo :: Lista a -> Lista a
semUltimo (Esq a Nula) = Nula
semUltimo (Esq a s) = Esq a (semUltimo s)
semUltimo (Dir s a) = s

-- b

l1 = Esq 1 (Dir (Dir (Esq 9 Nula) 3) 4)

instance Show a => Show (Lista a) where
    show = pp

pp :: Show a => Lista a -> String
pp Nula = "[]"
pp l = "[" ++ pp' l ++ "]"
  where
    pp' (Esq a Nula) = show a
    pp' (Esq a s) = show a ++ "," ++ pp' s
    pp' (Dir Nula a) = show a
    pp' (Dir l' a') = pp' l' ++ "," ++ show a'

-- ** 4 **

data BTree a = Empty | Node a (BTree a) (BTree a) deriving Show

a1 = (Node 'a' (Node 'b' Empty Empty) (Node 'c' Empty Empty))

-- a

numera :: BTree a -> BTree (a,Int)
numera a = snd $ numeraAux 1 a

numeraAux :: Int -> BTree a -> (Int,BTree (a,Int))
numeraAux n Empty = (n, Empty)
numeraAux n (Node r e d) =
  let (n1, e') = numeraAux n e
      (n2, r') = (n1 + 1, (r, n1))
      (n3, d') = numeraAux n2 d
  in (n3, Node r' e' d')

-- b

unInorder :: [a] -> [BTree a]
unInorder [] = [Empty]
unInorder (l:ls) = concatMap (\(e, d) -> [Node l f r | f <- unInorder e, r <- unInorder d]) (sList ls)

sList :: [a] -> [([a], [a])]
sList [] = [([], [])]
sList l'@(l:ls) = ([], l') : [(l:e, d) | (e, d) <- sList ls]