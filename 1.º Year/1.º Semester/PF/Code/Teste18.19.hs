module Teste1819 where

import Data.Char

import Data.List 

import System.Random 

-- ** 1 **

-- a

elemIndices' :: Eq a => a -> [a] -> [Int]
elemIndices' e l = elemH e l 0

elemH :: Eq a => a -> [a] -> Int -> [Int]
elemH _ [] _ = []
elemH e (l:ls) v 
   | e == l = v : elemH e ls (v+1)
   | otherwise = elemH e ls (v+1)

-- b

isSubsequenceOf' :: Eq a => [a] -> [a] -> Bool
isSubsequenceOf' [] _ = True
isSubsequenceOf' _ [] = False 
isSubsequenceOf' (l:ls) (r:rs)
  | l == r = isSubsequenceOf' ls rs 
  | otherwise = isSubsequenceOf' (l:ls) rs 

-- ** 2 **

data BTree a = Empty | Node a (BTree a) (BTree a) deriving Show

b1 :: BTree (Int,Int)
b1 = Node (10,11)
      (Node (5,6)
        (Node (2,3) Empty Empty)
        (Node (7,8) Empty Empty))
      (Node (15,16)
        (Node (12,13) Empty Empty)
        (Node (20,21) Empty Empty))

b2 :: BTree Int
b2 = Node 10
      (Node 5
        (Node 2 Empty Empty)
        (Node 7 Empty Empty))
      (Node 15
        (Node 12 Empty Empty)
        (Node 20 Empty Empty))

-- a

lookupAP :: Ord a => a -> BTree (a,b) -> Maybe b 
lookupAP _ Empty = Nothing 
lookupAP e (Node f l r)
  | e == fst f = Just (snd f)
  | e < fst f = lookupAP e l 
  | otherwise = lookupAP e r

-- b

zipWithBT :: (a -> b -> c) -> BTree a -> BTree b -> BTree c
zipWithBT _ Empty _ = Empty 
zipWithBT _ _ Empty = Empty
zipWithBT m (Node f l r) (Node g h j) = Node (m f g) (zipWithBT m l h) (zipWithBT m r j)

-- ** 3 **

digitAlpha :: String -> (String,String)
digitAlpha s = digitH s ("","")

digitH :: String -> (String,String) -> (String,String)
digitH "" s = s
digitH (s:ss) (l,r)
  | isDigit s = digitH ss (l ++ [s],r)
  | otherwise = digitH ss (l,r ++ [s])

-- ** 4 **

data Seq a = Nil | Cons a (Seq a) | App (Seq a) (Seq a)

s1 :: Seq Int
s1 = App (Cons 1 Nil) (App (Cons 7 (Cons 5 Nil)) (Cons 3 Nil))

-- a

firstSeq :: Seq a -> a
firstSeq s = head $ seqL s

seqL :: Seq a -> [a]
seqL (Nil) = []
seqL (Cons a s) = a : seqL s
seqL (App s1 s2) = (seqL s1) ++ (seqL s2)

-- b

dropSeq :: Int -> Seq a -> Seq a 
dropSeq 0 s = s
dropSeq v (Cons a s) = dropSeq (v-1) s 
dropSeq v (App s1 s2) = App (dropSeq (v-1) s1) (dropSeq (v-1) s2)

-- c

instance Show a => Show (Seq a) where
    show = pp

pp :: Show a => Seq a -> String
pp seq = "<<" ++ intercalate "," (map show (seqL seq)) ++ ">>"

-- ** 5 **

type Mat a = [[a]]

m1 :: Mat Int 
m1 = [[6,7,2], [1,5,9], [8,3,4]]

-- a

getElem :: Mat a -> IO a 
getElem m = do 
            la <- randomRIO (0, length m - 1)
            ca <- randomRIO (0, length (head m) - 1)
            putStrLn "Escolhendo elemento aleatório na matriz"
            putStrLn "O elemento é aleatório escolhido é:"
            return $ pickElem la ca m 

pickElem :: Int -> Int -> Mat a -> a
pickElem _ _ [] = error "A matriz não tem elementos"
pickElem l c m = (m !! l) !! c

-- b

magic :: Mat Int -> Bool
magic m = comp (map sum m) && comp (map sum (transpose m)) && sum (diag m) == sum (rDiag m)
  where
    comp xs = all (== head xs) xs
    transpose ([]:_) = []
    transpose m = map head m : transpose (map tail m)
    diag m = [m !! i !! i | i <- [0..length m - 1]]
    rDiag m = [m !! i !! (length m - 1 - i) | i <- [0..length m - 1]]