module Teste1920 where

import Data.List (nub)

-- ** 1 **

-- a

intersect :: Eq a => [a] -> [a] -> [a]
intersect _ [] = []
intersect [] _ = []
intersect (x:xs) l 
  | x `elem` l = x : intersect xs l
  | otherwise = intersect xs l

-- b

tails :: [a] -> [[a]]
tails l = tails' l (length l)

tails' :: [a] -> Int -> [[a]]
tails' l 0 = [l]
tails' l n = l : tails' (tail l) (n - 1)

-- ** 2 **

type ConjInt = [Intervalo]
type Intervalo = (Int, Int)

-- a

elems :: ConjInt -> [Int]
elems [] = []
elems ((c1,c2):cs) = [c1 .. c2] ++ elems cs

-- b

geraconj :: [Int] -> ConjInt
geraconj [] = []
geraconj (x:xs) = geraconjH x x xs

geraconjH :: Int -> Int -> [Int] -> ConjInt
geraconjH i f [] = [(i, f)]
geraconjH i f (y:ys)
  | y == succ f = geraconjH i y ys
  | otherwise = (i,f) : geraconjH y y ys

-- ** 3 **

data Contacto = Casa Integer
              | Trab Integer
              | Tlm Integer
              | Email String
    deriving (Show)

type Nome = String
type Agenda = [(Nome, [Contacto])]

-- a

acresEmail :: Nome -> String -> Agenda -> Agenda
acresEmail n s [] = [(n, [Email s])]
acresEmail n s ((no,co):re) 
  | n == no = (no, (Email s) : co) : re
  | otherwise = (no,co) : acresEmail n s re

-- b

verEmails :: Nome -> Agenda -> Maybe [String]
verEmails n [] = Nothing
verEmails n a@((no,co):re)
  | n == no = Just [em | Email em <- co]
  | otherwise = verEmails n re

-- c

ce :: [Contacto]
ce = [Casa 123123123, Tlm 456456456, Email "john@email.com", Trab 789789789, Email "work@iol.pt"]

ag :: Agenda
ag = [("Jacinto",[Casa 123123123, Tlm 456456456, Email "jacinto@email.com"]),("Xavier",[Trab 789789789, Email "work@iol.pt"])]

consulta :: [Contacto] -> ([Integer], [String])
consulta c = foldr (\x (n,e) -> case x of 
    Casa num -> (num : n, e) 
    Trab num -> (num : n, e)
    Tlm  num -> (num : n, e)
    Email ne -> (n, ne : e) 
  ) ([], []) c

-- d

consultaIO :: Agenda -> IO ()
consultaIO a = do
               n <- getLine
               let cr = helper n a
               putStrLn (show cr)
        where
           helper :: Nome -> Agenda -> [Contacto]
           helper _ [] = []
           helper n ((no,c):re) 
            | n == no = c
            | otherwise = helper n re

-- ** 4 **

data RTree a = R a [RTree a] deriving (Show, Eq)

rt :: RTree Int
rt = (R 1 [R 2 [], 
           R 3 [R 4 [R 5 [], R 6 []]], 
           R 7 []
          ]
     )

-- a

paths :: RTree a -> [[a]]
paths (R l []) = [[l]]
paths (R l s) = map (l:) (concatMap paths s)

-- b

unpaths :: Eq a => [[a]] -> RTree a
unpaths l = R n [unpaths (filter (not . null) [t | t <- br, head t == y]) | y <- hs]
  where
    n = head $ head l
    br = filter (not . null) [tail x | x <- l]
    hs = nub [head x | x <- br]





