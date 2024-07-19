module Exame2122 where

import System.Random

-- ** 1 **

replicate' :: Int -> a -> [a]
replicate' 0 _ = []
replicate' n e = e : replicate' (n-1) e

-- ** 2 **

intersect :: Eq a => [a] -> [a] -> [a] 
intersect [] _ = []
intersect _ [] = []
intersect (x:xs) y
   | x `elem` y = x : intersect xs y
   | otherwise = intersect xs y

-- ** 3 **

data LTree a = Tip a | Fork (LTree a) (LTree a) deriving Show
data FTree a b = Leaf a | No b (FTree a b) (FTree a b) deriving Show

av :: LTree Int
av = Fork (Fork (Tip 5)
                (Fork (Tip 6)
                      (Tip 4)))
          (Fork (Fork (Tip 3)
                      (Tip 7))
                (Tip 5))

conv :: LTree Int -> FTree Int Int
conv (Tip a) = Leaf a
conv t@(Fork l r) = No (som t) (conv l) (conv r)
   where
    som :: LTree Int -> Int
    som (Tip a) = a
    som (Fork l r) = som l + som r 

-- ** 4 **

type Mat a = [[a]]

m1 :: Mat Int
m1 = [[1,2,3], [0,4,5], [0,0,6]]

triSup :: (Eq a, Num a) => Mat a -> Bool
triSup m = all triSupH (zip [0..] m)
  where
    triSupH :: (Eq a, Num a) => (Int, [a]) -> Bool
    triSupH (_, []) = True
    triSupH (l, r) = all (== 0) (take l r)

-- ** 5 **

data SReais = AA Double Double | FF Double Double
            | AF Double Double | FA Double Double 
            | Uniao SReais SReais

sr :: SReais 
sr = Uniao (Uniao (AA 4.2 5.5) (AF 3.1 7.0)) (FF (-12.3) 30.0)

-- a

instance Show SReais where
    show = pp

pp :: SReais -> String
pp (AA x y) = "]" ++ show x ++ "," ++ show y ++ "["
pp (FF x y) = "]" ++ show x ++ "," ++ show y ++ "["
pp (AF x y) = "]" ++ show x ++ "," ++ show y ++ "]"
pp (FA x y) = "[" ++ show x ++ "," ++ show y ++ "["
pp (Uniao a b) = "(" ++ pp a ++ "U" ++ pp b ++ ")"

-- b

tira :: Double -> SReais -> SReais
tira v (AA x y)
    | v > x && v < y = Uniao (AA x v) (AA v y) 
    | otherwise = AA x y
tira v (FF x y)
    | v > x && v < y = Uniao (FA x v) (AF v y) 
    | v == x = AF x y
    | v == y = FA x y
    | otherwise = FF x y
tira v (AF x y)
    | v > x && v < y = Uniao (AA x v) (AF v y) 
    | v == y = AA x y
    | otherwise = AF x y
tira v (FA x y)
    | v > x && v < y = Uniao (FA x v) (AA v y) 
    | v == x = AA x y
    | otherwise = FA x y
tira v (Uniao x y) = Uniao (tira v x) (tira v y)

-- ** 6 **

func :: Float -> [(Float,Float)] -> [Float]
func _ [] = []
func x ((a,b):c)
  | x == a = b : func x c
  | otherwise = func x c

-- ** 7 **

subseqSum :: [Int] -> Int -> Bool
subseqSum l k = subseqSumH l 0
  where
    subseqSumH :: [Int] -> Int -> Bool
    subseqSumH [] c = k == c
    subseqSumH (l:ls) c = subseqSumH ls (c + l) || subseqSumH ls c

-- ** 8 **

jogo :: Int -> (Int, Int) -> IO ()
jogo n (a,b) = do 
               l <- geraLista n (a,b)
               putStrLn "Enter a number:"
               f <- getLine
               let m = read f :: Int
               if subseqSum l m
               then putStrLn "The property is verified"
               else putStrLn "The property is not verified"
               putStrLn ("Generated list: " ++ show l)

geraLista :: Int -> (Int, Int) -> IO [Int]
geraLista 0 _ = return []
geraLista n (a, b) = do
  l <- randomRIO (a, b)
  ls <- geraLista (n - 1) (a, b)
  return (l : ls)