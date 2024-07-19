module Ficha4 where

import Data.Char

import Data.List

-- ** 1 **

digitAlpha :: String -> (String,String)
digitAlpha l = ([c | c <- l, isAlpha c], [c | c <- l, isDigit c])

-- ** 2 **

nzp :: [Int] -> (Int, Int, Int)
nzp = foldl contar (0, 0, 0)
  where
    contar (n, z, p) x
      | x < 0     = (n + 1, z, p)
      | x == 0    = (n, z + 1, p)
      | otherwise = (n, z, p + 1)

-- ** 3 **

divMod' :: Integral a => a -> a -> (a, a)
divMod' _ 0 = error "Divisão por zero"
divMod' x y = divModAux x y 0
  where
    divModAux a b count
      | a < b     = (count, a)
      | otherwise = divModAux (a - b) b (count + 1)

-- ** 4 **

fromDigits :: [Int] -> Int
fromDigits [] = 0
fromDigits (h:t) = h*10^(length t) + fromDigits t

fromDigits' :: [Int] -> Int
fromDigits' l = fromDigits'' l 0
  where
    fromDigits'' :: [Int] -> Int -> Int
    fromDigits'' [] ac     = ac
    fromDigits'' (digit:ds) ac = fromDigits'' ds (ac * 10 + digit)

-- ** 5 ** 

maxSumInit :: (Num a, Ord a) => [a] -> a
maxSumInit l = maximum [sum m | m <- inits l]

maxSumInit' :: (Num a, Ord a) => [a] -> a
maxSumInit' l = maxSumInit'' l 0
    where
        maxSumInit'' :: (Num a, Ord a) => [a] -> a -> a
        maxSumInit'' [] ac = ac
        maxSumInit'' (l:ls) ac  =
            if l < ac 
            then ac
            else maxSumInit'' ls (ac + l)

-- ** 6 **

fib :: Int -> Int
fib 0 = 0
fib 1 = 1
fib n = fib (n-1) + fib (n-2)

fib' :: Int -> Int
fib' l = fibAux l 0 1
   where
    fibAux :: Int -> Int -> Int -> Int
    fibAux 0 a b = a
    fibAux n a b = fibAux (n-1) b (a + b)

-- ** 7 ** 
intToStr :: Integer -> String
intToStr n = intToStrAux (show n) ""
  where
    intToStrAux :: String -> String -> String
    intToStrAux "" m = m
    intToStrAux (n:ns) m = intToStrAux ns (m ++ [n])

-- ** 8 **

-- a

ae = [x | x <- [1..20], mod x 2 == 0, mod x 3 == 0]
ar = [6, 12, 18]
ae' = [x | x <- [1..20], mod x 6 == 0]

-- b

be = [x | x <- [y | y <- [1..20], mod y 2 == 0], mod x 3 == 0]
br = [6, 12 ,18]
be' = [x | x <- [1..20], mod x 6 == 0]

-- c

ce = [(x,y) | x <- [0..20], y <- [0..20], x+y == 30]
cr = [[(10,20),(11,19),(12,18),(13,17),(14,16),(15,15),(16,14),(17,13),(18,12),(19,11),(20,10)]]
ce' = [(x,y) | x <- [10..20], y <- reverse [10..20], x+y == 30]

-- d

de = [sum [y | y <- [1..x], odd y] | x <- [1..10]]
dr = [1, 1, 4, 4, 9, 9, 16, 16, 25, 25]
de' = [ x^2 | x <- [1..5], y <- [1..2]]

-- ** 9 ** 

-- a

cra = [1,2,4,8,16,32,64,128,256,512,1024]
ca = [2 ^ x| x <- [1..10]]

-- b

crb = [(1,5),(2,4),(3,3),(4,2),(5,1)]
cb = [(x,y) | x <- [1..5], y <- [1..5], x + y == 6]

-- c

crc = [[1],[1,2],[1,2,3],[1,2,3,4],[1,2,3,4,5]]
cc = [[1..x] | x <- [1..5]]

-- d 

crd  = [[1],[1,1],[1,1,1],[1,1,1,1],[1,1,1,1,1]]
cd = [[1 | _ <- [1..n]] | n <- [1..5]]

-- e 

cre = [1,2,6,24,120,720]
cf = [product [y | y <- [1..x]] | x <- [1..6]]