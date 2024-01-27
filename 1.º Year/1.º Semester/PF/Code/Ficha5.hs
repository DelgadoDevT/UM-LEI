module Ficha5 where

import Data.List

-- ** 1 ** 

-- a

any' :: (a -> Bool) -> [a] -> Bool
any' a [] = False 
any' a (l:ls) = a l || any' a ls 

-- b 

zipWith' :: (a -> b -> c) -> [a] -> [b] -> [c]
zipWith' f xs ys = map (uncurry f) $ zip xs ys

-- c

takeWhile' :: (a -> Bool) -> [a] -> [a]
takeWhile' a [] = []
takeWhile' a (l:ls) 
  | a l = l : takeWhile' a ls 
  | otherwise = []

-- d

dropWhile' :: (a -> Bool) -> [a] -> [a]
dropWhile' a [] = []
dropWhile' a (l:ls)
  | a l = dropWhile' a ls 
  | otherwise = (l:ls)

-- e

span' :: (a -> Bool) -> [a] -> ([a], [a])
span' c l =
  let (y, z) = foldr (\x (a1, a2) -> if c x then (x:a1, a2) else ([], x:a2)) ([], []) l
  in  (y, z)

-- f

deleteBy' :: (a -> a -> Bool) -> a -> [a] -> [a]
deleteBy' a g (l:ls)
   | a g l = ls 
   | otherwise = l : deleteBy' a g ls

-- g

sortOn' :: Ord b => (a -> b) -> [a] -> [a]
sortOn' _ [] = []
sortOn' f (x:xs) = insertBy' (\a b -> compare (f a) (f b)) x (sortOn' f xs)

insertBy' :: (a -> a -> Ordering) -> a -> [a] -> [a]
insertBy' _ x [] = [x]
insertBy' cmp x (y:ys)
  | cmp x y == GT = y : insertBy' cmp x ys  
  | otherwise = x : y : ys 

-- ** 2 **  

type Polinomio = [Monomio]
type Monomio   = (Float,Int)

-- a 

selgrau :: Int -> Polinomio -> Polinomio
selgrau n p = filter (\a -> snd a == n) p

-- b

conta :: Int -> Polinomio -> Int
conta g p = foldr (\(_, e) acc -> if e == g then acc + 1 else acc) 0 p

-- c

grau :: Polinomio -> Int
grau p = maximum $ map snd p

-- d

deriv :: Polinomio -> Polinomio
deriv p = map (\(a, n) -> (a * fromIntegral n, n - 1)) (filter (\(_, n) -> n > 0) p)

-- e

calcula :: Float -> Polinomio -> Float
calcula v p = sum $ map (\(a,n) -> a * (v ^ n)) p 

-- f

simp :: Polinomio -> Polinomio 
simp p = filter (\(_,n) -> n > 0) p

-- g

mult :: Monomio -> Polinomio -> Polinomio
mult (m1,m2) p = map (\(x,y) -> (m1*x,m2+y)) p

-- h

ordena :: Polinomio -> Polinomio 
ordena p = sortBy (\(_, b1) (_, b2) -> compare b1 b2) p

-- i 

normaliza :: Polinomio -> Polinomio
normaliza p = map (foldl (\(x,y) (a,b) -> (x+a,b)) (0,0)) $ groupBy (\f1 f2 -> snd f1 == snd f2) $ ordena p

-- j

soma :: Polinomio -> Polinomio -> Polinomio
soma p1 p2 = normaliza (p1 ++ p2)

-- k 

produto :: Polinomio -> Polinomio -> Polinomio
produto p1 p2 = foldl (\acc m -> soma (mult m p2) acc) [] p1  

-- l

equiv :: Polinomio -> Polinomio -> Bool
equiv p1 p2 = ordena (normaliza p1) == ordena (normaliza p2)

-- ** 3 ** 

type Mat a = [[a]]

mat1 = [[1,2,3], [4,5,6], [7,8,9]]

-- a

dimOK :: Mat a -> Bool
dimOK l = all (== length l) (map length l)

-- b

dimMat :: Mat a -> (Int,Int)
dimMat [] = (0,0)
dimMat mat = (length (head mat), length mat)

-- c

addMat :: Num a => Mat a -> Mat a -> Mat a 
addMat mat1 mat2 
   | dimMat mat1 == dimMat mat2 = zipWith (zipWith (+)) mat1 mat2
   | otherwise = error "As matrizes não tem o mesmo tamanho" 

-- d

transpose' :: Mat a -> Mat a
transpose' mat = foldr (zipWith (:)) (repeat []) mat

-- e

multMat :: Num a => Mat a -> Mat a -> Mat a
multMat mat1 mat2 = [[sum $ zipWith (*) r c | c <- transpose mat2] | r <- mat1]

-- f

zipWMat :: (a -> b -> c) -> Mat a -> Mat b -> Mat c 
zipWMat mat = zipWith (zipWith mat)

-- g

triSup :: (Eq a, Num a) => Mat a -> Bool
triSup mat = and [all (== 0) linha | (linha, i) <- zip mat [0..], (elemento, j) <- zip linha [0..], i > j]

-- h

rotateLeft :: Mat a -> Mat a
rotateLeft mat = reverse (transpose mat)