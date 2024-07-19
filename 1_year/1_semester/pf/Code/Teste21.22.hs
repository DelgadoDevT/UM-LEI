module Teste2122 where

import System.Random

-- ** 1 **

zip' :: [a] -> [b] -> [(a,b)]
zip' [] _ = []
zip' _ [] = []
zip' (l:ls) (x:xs) = (l,x) : zip' ls xs 

-- ** 2 **

preCrescente :: Ord a => [a] -> [a]
preCrescente [] = []
preCrescente [l] = [l]
preCrescente (l:lr) 
   | l < (head lr) = l : preCrescente lr 
   | otherwise = [l]

-- ** 3 **

amplitude :: [Int] -> Int
amplitude [] = 0
amplitude (x:xs) = let (a,b) = foldl (\(minV, maxV) e -> (min minV e, max maxV e)) (x, x) xs
                   in b - a

-- ** 4 **

type Mat a = [[a]]

soma :: Num a => Mat a -> Mat a -> Mat a
soma m1 m2 = zipWith (zipWith (+)) m1 m2

-- ** 5 **

type Nome = String
type Telefone = Integer
data Agenda = Vazia | Nodo (Nome,[Telefone]) Agenda Agenda

a1 :: Agenda
a1 = Nodo ("Abílio", [123456789, 987654321])
         (Nodo ("Salvador", [123498765, 284123813]) Vazia Vazia)
         (Nodo ("Jacinto", [123987123, 142837319]) Vazia Vazia)

instance Show Agenda where
    show = pp

pp :: Agenda -> String
pp Vazia = ""
pp (Nodo (n,nu) l r) = show l ++ n ++ ": " ++ showNu nu ++ "\n" ++ show r
   where
    showNu :: [Telefone] -> String
    showNu [] = ""
    showNu (n:ns) = show n ++ concatMap (\f -> '/' : show f) ns 

-- ** 6 **

randomSel :: Show a => Int -> [a] -> IO [a]
randomSel _ [] = return []
randomSel 0 _ = return []
randomSel n l = do
    rn <- randomRIO (1, length l)
    re <- randomSel (n - 1) (take (rn - 1) l ++ drop rn l)
    return (l !! (rn - 1) : re)

-- ** 7 **
organiza :: Eq a => [a] -> [(a, [Int])]
organiza l = foldl organizaH [] (zip l [0..])
  where
    organizaH a (y, i) =
      case lookup y a of
        Just ind -> (y, i : ind) : filter (\(x, _) -> x /= y) a
        Nothing  -> (y, [i]) : a

-- ** 8 **
func :: [[Int]] -> [Int]
func l = funcH l []
  where 
    funcH :: [[Int]] -> [Int] -> [Int]
    funcH [] r = r
    funcH (l:ls) r = if sum l > 10 then funcH ls (r ++ l) else funcH ls r 

-- ** 9 **
data RTree a = R a [RTree a] deriving Show
type Dictionary = [ RTree (Char, Maybe String) ]

d1 :: Dictionary
d1 =
  [ R ('c', Nothing)
    [ R ('a', Nothing)
      [ R ('r', Nothing)
        [ R ('a', Just "...")
          [ R ('s', Just "...") [] ]
        , R ('o', Just "...") []
        , R ('r', Nothing)
          [ R ('o', Just "...") [] ]
        ]
      ]
    ]
  ] 

insere :: String -> String -> Dictionary -> Dictionary
insere [x] inf d = insereH x inf d
insere (h:t) inf [] = [ R (h,Nothing) (insere t inf [])]
insere (h:t) inf (R (a,b) l:d)
    | h == a = R (a,b) (insere t inf l) : d
    | otherwise = R (a,b) l : insere (h:t) inf d

insereH :: Char -> String -> Dictionary -> Dictionary
insereH x inf [] = [ R (x,Just inf) [] ]
insereH x inf (R (a,b) l:t) 
    | x == a = R (a,Just inf) l : t
    | otherwise = R (a,b) l : insereH x inf t