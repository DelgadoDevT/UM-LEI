module Exame1920 where

import System.Random

-- ** 1 **

-- a 

inits :: [a] -> [[a]]
inits [] = [[]]
inits l = inits (init l) ++ [l]

-- b

isPrefixOf :: Eq a => [a] -> [a] -> Bool
isPrefixOf [] _ = True
isPrefixOf _ [] = False
isPrefixOf (x:xs) (y:ys)
  | x == y = isPrefixOf xs ys
  | otherwise = False

-- ** 2 **

data BTree a = Empty | Node a (BTree a) (BTree a) deriving Show

at :: BTree Int
at =
  Node 1
    (Node 2
      (Node 4 Empty Empty)
      (Node 5 Empty Empty))
    (Node 3
      Empty
      (Node 6 Empty Empty))

-- a 

folhas :: BTree a -> Int 
folhas Empty = 0
folhas (Node _ Empty Empty) = 1
folhas (Node d l r) = folhas l + folhas r

-- b 

path :: [Bool] -> BTree a -> [a]
path [] (Node a _ _) = [a]
path _ Empty = []
path (True:cs) (Node d _ r) = d : path cs r
path (False:cs) (Node d l _) = d : path cs l

-- ** 3 **

type Polinomio = [Coeficiente]
type Coeficiente = Float

p1 :: Polinomio
p1 = [0, 0, 0, -5, 0, 2]

-- a

valor :: Polinomio -> Float -> Float
valor x v = sum (zipWith (\a b -> b * v ^ a) [0 ..] x)

-- b

deriv :: Polinomio -> Polinomio
deriv l = tail (zipWith (*) [0 ..] l)

-- c 

soma :: Polinomio -> Polinomio -> Polinomio
soma p1 p2 = zipWith (*) p1 p2

-- ** 4 **

type Mat a = [[a]]

ex = [[1,4,3,2,5], [6,7,8,9,0], [3,5,4,9,1]]

-- a

quebraLinha :: [Int] -> [a] -> [[a]]
quebraLinha [] _ = []
quebraLinha (a:as) l = take a l : quebraLinha as (drop a l)

-- b

fragmenta :: [Int] -> [Int] -> Mat a -> [Mat a]
fragmenta [] _ _ = []
fragmenta (a:as) c l = quebraLinhas c (take a l) ++ fragmenta as c (drop a l)
    where 
        quebraLinhas :: [Int] -> Mat a -> [Mat a]
        quebraLinhas [] _ = []
        quebraLinhas (a:as) l = map (take a) l : quebraLinhas as (map (drop a) l)

-- c

geraMat :: (Int, Int) -> (Int, Int) -> IO (Mat Int)
geraMat (x, y) (a, b) = do
  let gL = sequence [randomRIO (a, b) | _ <- [1..y]]
  m <- sequence [gL | _ <- [1..x]]
  return m