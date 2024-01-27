module Teste2324 where

-- ** 1 **

alterna :: Num a => Int -> a -> [a]
alterna 0 _ = []
alterna n v = v : alterna (n-1) (-v)

-- ** 2 **

data Turma = Empty | Node (Integer, String) Turma Turma

a1 :: Turma
a1 = Node (12,"Abilio") (Node (11,"Jacinto") Empty Empty) (Node (13,"Maria") Empty Empty)

-- a

instance Show Turma where
    show = pp

pp :: Turma -> String
pp Empty = "" 
pp (Node d l r) = pp l ++ ordena d ++ pp r 
   where 
    ordena :: (Integer, String) -> String
    ordena (n1,n2) = "(" ++ show n1 ++ ": " ++ n2 ++ ")"

-- b

limites :: Turma -> (Integer,Integer)
limites (Node f Empty Empty) = (fst f, fst f)
limites (Node f l Empty) = (fst $ limites l, fst f)
limites (Node f Empty r) = (fst f, snd $ limites r)
limites (Node f l r) = (fst $ limites l,snd $ limites r)

-- ** 3 **

type TabAbrev = [(Palavra,Abreviatura)]
type Palavra = String
type Abreviatura = String

-- a

difMaior :: TabAbrev -> (Palavra, Int)
difMaior [] = error "Tabela sem elementos"
difMaior [(l, ls)] = (l, length l - length ls)
difMaior ((l, ls) : (r, rs) : lr)
  | dl > dr = (l, dl)
  | otherwise = (w, dr)
  where
    dl = length l - length ls
    (w, dr) = difMaior ((r, rs) : lr)

-- b

subst :: [String] -> TabAbrev -> [String]
subst [] _ = []
subst [t] [] = [t] 
subst (t:ts) tab@((l, ls):lr)
  | t == ls = l : subst ts tab
  | otherwise = subst [t] lr ++ subst ts tab

-- ** 4 **

data LTree a = Tip a | Fork (LTree a) (LTree a) deriving Show

dumpLT :: LTree a -> [(a,Int)]
dumpLT (Tip x) = [(x,1)]
dumpLT (Fork e d) = map f (dumpLT e ++ dumpLT d)
    where f :: (a,Int) -> (a,Int)
          f (x,y) = (x,y+1)

a2 :: LTree Char
a2 = (Fork (Tip 'a') (Fork (Tip 'b') (Tip 'c')))

-- a

dumpLT' :: LTree a -> [(a,Int)]
dumpLT' (Tip x) = [(x,1)]
dumpLT' (Fork l r) = dumpLTH l 2 ++ dumpLTH r 2 

dumpLTH :: LTree a -> Int -> [(a,Int)]
dumpLTH (Tip x) n = [(x,n)]
dumpLTH (Fork l r) n = dumpLTH l (n+1) ++ dumpLTH r (n+1)

-- b

unDumpLT :: [(a,Int)] -> LTree a
unDumpLT [(a,_)] = Tip a 
unDumpLT (l:lr) = Fork (unDumpLT [l]) (unDumpLT lr )  