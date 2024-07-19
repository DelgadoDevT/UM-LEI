module Ficha6 where

-- ** 1 ** 

data BTree a = Empty
             | Node a (BTree a) (BTree a)
             deriving Show

exampleTree :: BTree Int
exampleTree =
  Node 1
    (Node 2
      (Node 4 Empty Empty)
      (Node 5 Empty Empty))
    (Node 3
      Empty
      (Node 6 Empty Empty))

exampleTree2 :: BTree Int
exampleTree2 =
    Node 4
      (Node 3
        (Node 1 Empty Empty)
        (Node 2 Empty Empty))
      (Node 5
        (Node 6 Empty Empty)
        (Node 7 Empty Empty))  

-- a 

altura :: BTree a -> Int
altura Empty          = 0
altura (Node _ e d)   = 1 + max (altura e) (altura d)

-- b 

contaNodos :: BTree a -> Int
contaNodos Empty = 0
contaNodos (Node d l r) = 1 + contaNodos l + contaNodos r

-- c

folhas :: BTree a -> Int
folhas Empty = 0
folhas (Node d Empty Empty) = 1
folhas (Node d l r) = folhas l + folhas r

-- d 

prune :: Int -> BTree a -> BTree a
prune _ Empty = Empty
prune p (Node r e d) 
   | p <= 0 = Empty
   | otherwise = Node r (prune (p - 1) e) (prune (p - 1) d)

-- e

path :: [Bool] -> BTree a -> [a]
path _ Empty = []
path [] (Node d _ _) = [d]
path (True:re) (Node d _ r)  = d :path re r 
path (False:re) (Node d l _) = d :path re l

-- f

mirror :: BTree a -> BTree a
mirror Empty = Empty
mirror (Node r e d) = Node r (mirror d) (mirror e)

-- g

zipWithBT :: (a -> b -> c) -> BTree a -> BTree b -> BTree c 
zipWithBT f (Node d l r) (Node de le re) = Node (f d de) (zipWithBT f l le) (zipWithBT f r re) 
zipWithBT _ _ _ = Empty

-- h

unzipBT :: BTree (a,b,c) -> (BTree a,BTree b,BTree c)
unzipBT Empty = (Empty, Empty, Empty)
unzipBT (Node (a,b,c) l r) = 
  let 
     (al1,al2,al3) = unzipBT l
     (ar1,ar2,ar3) = unzipBT r
  in (Node a al1 ar1, Node b al2 ar2, Node c al3 ar3)

-- ** 2 ** 

-- a

minimo :: Ord a => BTree a -> a
minimo (Node r Empty _) = r
minimo (Node _ l _)  = minimo l 

-- b 

semMinimo :: Ord a => BTree a -> BTree a
semMinimo (Node r Empty _) = Empty
semMinimo (Node r (Node r' Empty Empty) d) = Node r Empty d
semMinimo (Node r e d) = (Node r (semMinimo e) d)

-- c

minSmin :: Ord a => BTree a -> (a,BTree a)
minSmin (Node p Empty r) = (p,r)
minSmin (Node p l r) = (x,Node p y r)
    where (x,y) = minSmin l

-- d

remove :: Ord a => a -> BTree a -> BTree a
remove _ Empty = Empty
remove rem (Node p l r)
   | rem > p = Node p l (remove rem r)
   | rem < p = Node p (remove rem l) r
   | otherwise = case r of  
                  Empty -> l
                  _ -> let (mr, nr) = minSmin r
                       in Node mr l nr

-- ** 3 ** 

type Aluno = (Numero,Nome,Regime,Classificacao)
type Numero = Int
type Nome = String
data Regime = ORD | TE | MEL deriving (Show, Eq)
data Classificacao = Aprov Int
                   | Rep
                   | Faltou
                   deriving (Show, Eq)
type Turma = BTree Aluno

-- a

inscNum :: Numero -> Turma -> Bool
inscNum _ Empty = False
inscNum n (Node (i1, _, _, _) e d)
    | n == i1   = True
    | n < i1    = inscNum n e
    | otherwise = inscNum n d

-- b

inscNome :: Nome -> Turma -> Bool
inscNome n Empty = False
inscNome n (Node (_,i2,_,_) e d) = n == i2 || inscNome n e || inscNome n d

-- c 

trabEst :: Turma -> [(Numero,Nome)]
trabEst Empty = []
trabEst (Node i@(i1, i2, i3, _) e d)
   | i3 == TE = [(i1,i2)] ++ trabEst e ++ trabEst d
   | otherwise = trabEst e ++ trabEst d

-- d

nota :: Numero -> Turma -> Maybe Classificacao
nota n (Node (nu,_,_,cl) l r)
   | n == nu = Just cl
   | n < nu = nota n l
   | otherwise = nota n r
nota _ _ = Nothing

-- e

percFaltas :: Turma -> Float
percFaltas t = (no / na) * 100
    where 
      na = fromIntegral $ contaNodos t
      no = nFaltas t 

nFaltas :: Turma -> Float
nFaltas Empty = 0
nFaltas (Node (_,_,_,cl) l r) = case cl of
                                Faltou -> 1 + nFaltas l + nFaltas r
                                _ -> nFaltas l + nFaltas r

-- f

mediaAprov :: Turma -> Float
mediaAprov Empty = 0
mediaAprov (Node (_, _, _, cl) e d)
    | isAprov cl = nota + (mediaAprov e * nae + mediaAprov d * nad) / totalAlunos
    | otherwise = (mediaAprov e * nae + mediaAprov d * nad) / totalAlunos
  where
    nota = case cl of
      Aprov n -> fromIntegral n
      _       -> 0
    nae = fromIntegral $ nAlunos e
    nad = fromIntegral $ nAlunos d
    totalAlunos = nae + nad + 1

nAlunos :: Turma -> Int
nAlunos Empty = 0
nAlunos (Node _ e d) = 1 + nAlunos e + nAlunos d

isAprov :: Classificacao -> Bool
isAprov (Aprov _) = True
isAprov _         = False

-- g

aprovAv :: Turma -> Float
aprovAv turma = fromIntegral ap / fromIntegral av
  where
    (ap, av) = aprovAvH turma (0, 0)

aprovAvH :: Turma -> (Int, Int) -> (Int, Int)
aprovAvH Empty (ap, av) = (ap, av)
aprovAvH (Node (_,_,_,cn) l r) (ap, av) =
  case cn of
    Aprov _ -> aprovAvH r (ap + 1, av + 1)
    Rep -> aprovAvH r (ap, av + 1)
    Faltou -> aprovAvH r (ap, av)

unzipBT' :: BTree (a,b,c) -> (BTree a, BTree b, BTree c)
unzipBT' (Empty) = (Empty,Empty,Empty)
unzipBT' (Node (fa,fb,fc) l r) = ((Node fa al1 ar1),(Node fb al2 ar2),(Node fc al3 ar3))
      where 
       (al1,al2,al3) = unzipBT l
       (ar1,ar2,ar3) = unzipBT r