module Teste2021 where

import Data.List (sortBy)

-- ** 1 **

(\\) :: Eq a => [a] -> [a] -> [a]
(\\) [] _ = []
(\\) l [] = l
(\\) (l:ls) (n:ns)
  | l == n = (\\) ls ns
  | otherwise = l : (\\) ls (n:ns)

-- ** 2 **

type MSet a = [(a,Int)]

-- a

removeMSet :: Eq a => a -> [(a,Int)] -> [(a,Int)]
removeMSet e [] = []
removeMSet e ((a,b):c)
  | e == a = removeMSet e c
  | otherwise = (a,b) : removeMSet e c 

-- b

calcula :: MSet a -> ([a],Int)
calcula l = foldr (\(x,y) (e, t) -> (x : e, y + t)) ([],0) l

-- ** 3 **

partes :: String -> Char -> [String]
partes "" _ = []
partes s c = case span (/= c) s of
              (p,"") -> [p]
              ("",r) -> partes (tail r) c
              (p,r)  -> p : partes (tail r) c

-- ** 4 **

data BTree a = Empty | Node a (BTree a) (BTree a)

a1 :: BTree Int
a1 = Node 5 (Node 3 Empty Empty) 
            (Node 7 Empty (Node 9 Empty Empty))

-- a 

remove :: Ord a => a -> BTree a -> BTree a
remove _ Empty = Empty
remove e (Node p l r)
   | e < p = Node p (remove e l) r
   | e > p = Node p l (remove e r)
   | otherwise = case (l,r) of
                  (Empty,r) -> r
                  (l,Empty) -> l
                  (l,r)     -> Node minV l (remove minV r)
                                where 
                                    minV = fMin r
                                    fMin :: BTree a -> a
                                    fMin (Node p Empty _) = p
                                    fMin (Node _ l _)     = fMin l
                                    fMin Empty            = error "The tree has no minimum element" 

-- b

instance Show a => Show (BTree a) where
    show = pp
    
pp :: Show a => BTree a -> String
pp Empty = "*"
pp (Node p l r) = "(" ++ pp l ++ " <-" ++ show p ++ "-> " ++ pp r ++ ")"

-- ** 5 **

sortOn :: Ord b => (a -> b) -> [a] -> [a]
sortOn f l = sortBy (\x y -> compare (f x) (f y)) l

-- ** 6 **

data FileSystem = File Nome | Dir Nome [FileSystem] deriving Show
type Nome = String

fs1 = Dir "usr" [Dir "xxx" [File "abc.txt", File "readme", Dir "PF" [File "exemplo.hs"]],
                 Dir "yyy" [], Dir "zzz" [Dir "tmp" [], File "teste.c"] ]

-- a

fichs :: FileSystem -> [Nome]
fichs (File n) = [n]
fichs (Dir _ f) = concatMap fichs f 

-- b

dirFiles :: FileSystem -> [Nome] -> Maybe [Nome]
dirFiles _ [] = Nothing
dirFiles (File _) (_:_) = Nothing
dirFiles (Dir n sd) [target]
  | n == target = Just (listFiles sd)
  | otherwise = Nothing
dirFiles (Dir n sd) (current:r)
  | n /= current = Nothing
  | otherwise = Just $ concat (mapMaybe (`dirFiles` r) sd)

listFiles :: [FileSystem] -> [Nome]
listFiles [] = []
listFiles (Dir _ _ : r) = listFiles r
listFiles (File fn : r) = fn : listFiles r

mapMaybe :: (a -> Maybe b) -> [a] -> [b]
mapMaybe _ [] = []
mapMaybe f (x:xs) =
  case f x of
    Just r -> r : mapMaybe f xs
    Nothing -> mapMaybe f xs

-- c

listaFich :: FileSystem -> IO ()
listaFich fs = do
    putStr "> "
    path <- getLine
    case dirFiles fs (partes path '/') of 
        Just files -> print files
        Nothing -> putStrLn "Its not a directory"