module Teste1617 where

-- ** 1 **

type MSet a = [(a,Int)]

-- a

cardMSet :: MSet a -> Int
cardMSet [] = 0
cardMSet ((l,ls):lr) = ls + cardMSet lr

-- b

moda :: MSet a -> [a]
moda [] = []
moda m = modaH1 m (modaH2 m)

modaH1 :: MSet a -> Int -> [a]
modaH1 [] _ = []
modaH1 ((e, f):r) mf
  | f == mf = replicate f e ++ modaH1 r mf
  | otherwise = modaH1 r mf

modaH2 :: MSet a -> Int
modaH2 [] = 0
modaH2 ((_, f):r) = max f (modaH2 r)

-- c

converterMSet :: MSet a -> [a]
converterMSet [] = []
converterMSet ((l,ls):lr) = replicate ls l ++ converterMSet lr

-- d

addNcopies :: Eq a => MSet a -> a -> Int -> MSet a 
addNcopies [] e v = [(e,v)]
addNcopies ((l,ls):lr) e v
   | e == l = ((l,ls+v):lr)
   | otherwise = (l,ls) : addNcopies lr e v

-- ** 2 **

data SReais = AA Double Double | FF Double Double
            | AF Double Double | FA Double Double
            | Uniao SReais SReais

u1 :: SReais
u1 = Uniao (Uniao (AA 4.2 5.5) (AF 3.1 7.0)) (FF (-12.3) 30.0)

-- a 

instance Show SReais where
    show = pp

pp :: SReais -> String
pp (AA v1 v2) = "]" ++ show v1 ++ "," ++ show v2 ++ "["
pp (FF v1 v2) = "[" ++ show v1 ++ "," ++ show v2 ++ "]"
pp (AF v1 v2) = "]" ++ show v1 ++ "," ++ show v2 ++ "]"
pp (FA v1 v2) = "[" ++ show v1 ++ "," ++ show v2 ++ "["
pp (Uniao u1 u2) = "(" ++ pp u1 ++ " U " ++ pp u2 ++ ")"

-- b

pertence :: Double -> SReais -> Bool 
pertence e (AA v1 v2) = e >= v1 && e <= v2 
pertence e (FF v1 v2) = e > v1 && e < v2
pertence e (AF v1 v2) = e >= v1 && e < v2 
pertence e (FA v1 v2) = e > v1 && e <= v2 
pertence e (Uniao u1 u2) = pertence e u1 || pertence e u2

-- c

tira :: Double -> SReais -> SReais
tira e (AA v1 v2)
    | e > v1 && e < v2 = Uniao (AA v1 e) (AA e v2) 
    | otherwise = AA v1 v2
tira e (FF v1 v2)
    | e > v1 && e < v2 = Uniao (FA v1 e) (AF e v2) 
    | e == v1 = AF v1 v2
    | e == v2 = FA v1 v2
    | otherwise = FF v1 v2
tira e (AF v1 v2)
    | e > v1 && e < v2 = Uniao (AA v1 e) (AF e v2) 
    | e == v2 = AA v1 v2
    | otherwise = AF v1 v2
tira e (FA v1 v2)
    | e > v1 && e < v2 = Uniao (FA v1 e) (AA e v2) 
    | e == v1 = AA v1 v2
    | otherwise = FA v1 v2
tira e (Uniao u1 u2) = Uniao (tira e u1) (tira e u2)

 -- ** 3 **

data RTree a = R a [RTree a]

 -- a

percorre :: [Int] -> RTree a -> Maybe [a]
percorre [] (R a _) = Just [a]
percorre (c:cs) (R a s) = case lookup' c s of
  Just st -> (a:) <$> percorre cs st
  Nothing -> Nothing

lookup' :: Int -> [RTree a] -> Maybe (RTree a)
lookup' 1 (x:_) = Just x
lookup' n (_:xs) | n > 1 = lookup' (n - 1) xs
lookup' _ [] = Nothing

-- b

procura :: Eq a => a -> RTree a -> Maybe [Int]
procura e (R a s) =
    if e == a
    then Just []
    else case lookupeIns e 1 s of
           Just p  -> Just p 
           Nothing -> Nothing 

lookupeIns :: Eq a => a -> Int -> [RTree a] -> Maybe [Int]
lookupeIns _ _ [] = Nothing
lookupeIns e i (st : s) =
  case procura e st of
    Just p  -> Just (i : p)
    Nothing -> lookupeIns e (i + 1) s