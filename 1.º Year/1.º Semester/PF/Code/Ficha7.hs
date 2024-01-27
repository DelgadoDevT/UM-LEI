module Ficha7 where

-- ** 1 ** 

data ExpInt = Const Int
            | Simetrico ExpInt
            | Mais ExpInt ExpInt
            | Menos ExpInt ExpInt
            | Mult ExpInt ExpInt
        
-- a 

calcula :: ExpInt -> Int
calcula (Const n) = n
calcula (Simetrico n) = - calcula n
calcula (Mais n1 n2) = calcula n1 + calcula n2
calcula (Menos n1 n2) = calcula n1 - calcula n2
calcula (Mult n1 n2) = calcula n1 * calcula n2

-- b

infixa :: ExpInt -> String
infixa (Const n) = show n
infixa (Simetrico exp) = "(-(" ++ infixa exp ++ "))"
infixa (Mais n1 n2) = '(':infixa n1 ++ " + " ++ infixa n2 ++ ")"
infixa (Menos n1 n2) = '(':infixa n1 ++ " - " ++ infixa n2 ++ ")"
infixa (Mult n1 n2) = '(':infixa n1 ++ " * " ++ infixa n2 ++ ")"

-- c

posfixa :: ExpInt -> String
posfixa (Const n) = show n
posfixa (Simetrico exp) = posfixa exp ++ " - "
posfixa (Mais n1 n2) = posfixa n1 ++ " " ++ posfixa n2 ++ " + " 
posfixa (Menos n1 n2) = posfixa n1 ++ " " ++ posfixa n2 ++ " - " 
posfixa (Mult n1 n2) = posfixa n1 ++ " " ++ posfixa n2 ++ " * " 

-- ** 2 ** 


data RTree a = R a [RTree a] deriving Show

-- a

soma :: Num a => RTree a -> a
soma (R l []) = l
soma (R l ls) = l + sum (map soma ls)

-- b

altura :: RTree a -> Int
altura (R l []) = 1
altura (R l ls) = 1 + maximum (map altura ls)

-- c

prune :: Int -> RTree a -> RTree a
prune 0 (R l ls) = R l []
prune n (R l ls) = R l (map (prune (n - 1)) ls)

-- d

mirror :: RTree a -> RTree a
mirror (R l ls) = R l (map mirror (reverse ls))

-- e

postorder :: RTree a -> [a]
postorder (R l ls) = foldr (\(R x ls) acc -> postorder (R x ls) ++ acc) [l] ls

-- ** 3 **

data BTree a = Empty 
             | Node a (BTree a) (BTree a)
             deriving Show

data LTree a = Tip a 
             | Fork (LTree a) (LTree a)
             deriving Show

-- a 

ltSum :: Num a => LTree a -> a
ltSum (Tip n) = n
ltSum (Fork n1 n2) = ltSum n1 + ltSum n2

-- b

listaLT :: LTree a -> [a]
listaLT (Tip n) = [n]
listaLT (Fork n1 n2) = listaLT n1 ++ listaLT n2

-- c

itHeight :: LTree a -> Int
itHeight (Tip n) = 0
itHeight (Fork n1 n2) = 1 + max (itHeight n1) (itHeight n2)

-- ** 4 **  

data FTree a b = Leaf b 
               | No a (FTree a b) (FTree a b)
               deriving Show

-- a

splitFTree :: FTree a b -> (BTree a, LTree b)
splitFTree (Leaf n) = (Empty, Tip n)
splitFTree (No a av rv) = (Node a av1 rv1, Fork av2 rv2)
    where
        (av1,av2) = splitFTree av
        (rv1,rv2) = splitFTree rv

-- b

joinTrees :: BTree a -> LTree b -> Maybe (FTree a b)
joinTrees (Empty) (Tip n) = Just (Leaf n)
joinTrees (Node e l r) (Fork a b) =
    case (joinTrees l a, joinTrees r b) of 
        (Just x, Just y) -> Just (No e x y)
        _                -> Nothing
joinTrees _ _ = Nothing