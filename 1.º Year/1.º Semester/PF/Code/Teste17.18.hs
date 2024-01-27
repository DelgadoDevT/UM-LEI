module Teste1718 where

import Data.List

import System.Random

-- ** 1 **

insert' :: Ord a => a -> [a] -> [a]
insert' e [] = [e]
insert' e (l:ls) 
  | e > l = l : insert' e ls 
  | otherwise = e:l:ls

-- ** 2 **

catMaybes :: [Maybe a] -> [a]
catMaybes [] = []
catMaybes (c:cs) = case c of 
                    Just e  -> e : catMaybes cs 
                    Nothing -> catMaybes cs

-- ** 3 **

data Exp a = Const a
           | Var String
           | Mais (Exp a) (Exp a)
           | Mult (Exp a) (Exp a)

e1 :: Exp Int 
e1 = (Mais (Var "x") (Mult (Const 3) (Const 4)))

instance Show a => Show (Exp a) where 
    show e = pp e

pp :: Show a => Exp a -> String
pp (Const v) = show v
pp (Var v) = v
pp (Mais v1 v2) = "(" ++ pp v1 ++ " + " ++ pp v2 ++ ")"
pp (Mult v1 v2) = "(" ++ pp v1 ++ " * " ++ pp v2 ++ ")"

-- ** 4 **

sortOn' :: Ord b => (a -> b) -> [a] -> [a]
sortOn' _ [] = []
sortOn' f (x:xs) = insertBy' f x (sortOn' f xs)

insertBy' :: Ord b => (a -> b) -> a -> [a] -> [a]
insertBy' _ e [] = [e]
insertBy' f e (y:ys)
  | f e <= f y = e:y:ys
  | otherwise = y : insertBy' f e ys

-- ** 5 **

-- a

amplitude :: [Int] -> Int 
amplitude l = if null l then 0 else maximum l - minimum l

-- b 

parte :: [Int] -> ([Int], [Int])
parte l = parteH (sort l) ([], [])
  where
    parteH [] (l1, l2) = (reverse l1, reverse l2)
    parteH (x:xs) (l1, l2) = parteH xs (x:l2, l1)

-- ** 6 **

data Imagem = Quadrado Int
            | Mover (Int,Int) Imagem
            | Juntar [Imagem] 
            deriving Show

ex :: Imagem
ex = Mover (5,5) (Juntar [Mover (0,1) (Quadrado 5),
                          Quadrado 4,
                          Mover (4,3) (Quadrado 2)])

-- a

conta :: Imagem -> Int
conta (Quadrado _) = 1
conta (Mover _ i) = conta i 
conta (Juntar l) = sum $ map conta l

-- b

apaga :: Imagem -> IO Imagem
apaga i = do 
          qap <- randomRIO (1, r)
          putStrLn "Apagando quadrado aleatório"
          return (remove qap i) 
          where 
            r = conta i 

remove :: Int -> Imagem -> Imagem
remove _ (Quadrado _) = Juntar []
remove n (Mover p i) = Mover p (remove n i)
remove n (Juntar l) = Juntar (removeH n l)

removeH :: Int -> [a] -> [a]
removeH _ [] = []
removeH 0 (x:xs) = xs
removeH n (x:xs) = x : removeH (n - 1) xs 