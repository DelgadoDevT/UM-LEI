module Exame1819 where

import Data.List 

-- ** 1 **

-- a

isSorted :: (Ord a) => [a] -> Bool 
isSorted [] = True
isSorted [l] = True
isSorted (l:ls:lr)
   | l <= ls = isSorted (ls:lr)
   | otherwise = False 

-- b

inits' :: [a] -> [[a]] 
inits' [] = [[]]
inits' l = inits' (init l) ++ [l]

-- ** 2 **

maximumMB :: (Ord a) => [Maybe a] -> Maybe a
maximumMB [] = Nothing
maximumMB [l] = l
maximumMB (l:ls:lr) = maximumMB ((max l ls) : lr) 

-- ** 3 **

data LTree a = Tip a | Fork (LTree a) (LTree a)

l1 :: LTree Int
l1 = Fork (Fork (Tip 7) (Tip 1)) (Tip 2)

-- a

listaLT :: LTree a -> [a]
listaLT (Tip v) = [v]
listaLT (Fork l r) = listaLT l ++ listaLT r

-- b

instance Show a => Show (LTree a) where
    show t = pp t 0

pp :: Show a => LTree a -> Int -> String
pp (Tip v) n = replicate n '.' ++ show v ++ "\n"
pp (Fork l r) n = pp l (n+1) ++ pp r (n+1)

-- ** 4 **

maxSumInit :: (Num a, Ord a) => [a] -> a
maxSumInit l = maximum [sum m | m <- inits l]

maxSumInit' :: (Num a, Ord a) => [a] -> a
maxSumInit' l = maxSumH l 0

maxSumH :: (Num a, Ord a) => [a] -> a -> a
maxSumH [] n = n
maxSumH (l:ls) n = maxSumH ls (l + n)

-- ** 5 **

type RelP a = [(a,a)]
type RelL a = [(a,[a])]
type RelF a = ([a], a->[a])

rp = [(1,3),(1,4),(2,1),(2,4),(2,5),(3,7),(4,7),(5,7),(6,5),(7,6)] :: RelP Int

rl = [(1,[3,4]),(2,[1,4,5]),(3,[7]),(4,[7]),(5,[7]),(6,[5]),(7,[6])] :: RelL Int 

rf :: RelF Int
rf = ([1,2,3,4,5,6,7], f)
  where
    f 1 = [3,4]
    f 2 = [1,4,5]
    f 3 = [7]
    f 4 = [7]
    f 5 = [7]
    f 6 = [5]
    f 7 = [6]

-- a

convPL :: (Eq a) => RelP a -> RelL a
convPL [] = []
convPL ((l, ls):lr) = (l, ls : [z | (a, z) <- lr, a == l]) : convPL [(a, b) | (a, b) <- lr, a /= l]

-- b 

criaRelPint :: Int -> IO (RelP Int)
criaRelPint n = do
  putStrLn "Digite os pares de inteiros (cada par separado por espaço):"
  i <- getLine
  let p = map (\[x, y] -> (x, y)) $ map (map read . words) (lines i)
  return $ take n p
    
-- c

-- i

convFP :: (Eq a) => RelF a -> RelP a
convFP (e, f) = [(x, y) | x <- e, y <- f x]

-- ii

convPF :: (Eq a) => RelP a -> RelF a
convPF relP = (e, f)
  where
    e = nub $ concatMap (\(x, y) -> [x, y]) relP
    f x = nub $ concatMap (\(a, b) -> if a == x then [b] else []) relP