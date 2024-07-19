module Exame2223 where

-- ** 1 **

type MSet a = [(a,Int)]

-- a

converteMSet :: MSet a -> [a]
converteMSet [] = []
converteMSet ((c,n):r) = replicate n c ++ converteMSet r

-- b

removeMSet :: Eq a => a -> MSet a -> MSet a
removeMSet _ [] = []    
removeMSet e ((c,n):r)
    | e == c && n > 0 = (c,n-1) : r
    | e == c = r
    | otherwise = (c,n) : removeMSet e r

-- c

uniaoMSet :: Eq a => MSet a -> MSet a -> MSet a
uniaoMSet [] c = c
uniaoMSet c [] = c
uniaoMSet ((x1, n1):ms1) ms2 =
  (x1, n1 + sum [n | (y, n) <- ms2, y == x1]) : uniaoMSet ms1 (filter (\(y, _) -> y /= x1) ms2)

-- ** 2 **

type Posicao = (Int,Int)
data Movimento = Norte | Sul | Este | Oeste
data Caminho = C Posicao [Movimento]

instance Eq Caminho where
    (==) = com 

com :: Caminho -> Caminho -> Bool
com (C p1 m1) (C p2 m2) = p1 == p2 && length m1 == length m2 && fPos p1 m1 == fPos p2 m2  

fPos :: Posicao -> [Movimento] -> Posicao
fPos pf [] = pf 
fPos (x,y) (m:ms) = case m of
    Norte -> fPos (x,y+1) ms
    Sul   -> fPos (x,y-1) ms
    Este  -> fPos (x+1,y) ms
    Oeste -> fPos (x-1,y) ms

-- ** 3 **

func' :: [[Int]] -> [Int]
func' [] = []
func' (l:ls)
  | sum l > 10 = l ++ func' ls 
  | otherwise = func' ls

-- ** 4 **

data Prop = Var String | Not Prop | And Prop Prop | Or Prop Prop deriving Show

p1 :: Prop
p1 = Not (Or (And (Not (Var "A")) (Var "B")) (Var "C"))

-- a

eval :: [(String, Bool)] -> Prop -> Bool
eval e (Var x) = lookupVar x e
eval e (Not p) = not (eval e p)
eval e (And p1 p2) = eval e p1 && eval e p2
eval e (Or p1 p2) = eval e p1 || eval e p2

lookupVar :: String -> [(String, Bool)] -> Bool
lookupVar x e = case lookup x e of
  Just val -> val
  Nothing  -> error $ "The logical value of the variable " ++ x ++ " was not found"

-- b

nnf :: Prop -> Prop
nnf (Var s) = Var s
nnf (Not p) = case p of
              Var p     -> Not (Var p)
              Not p     -> nnf p
              And p1 p2 -> Or (nnf (Not p1)) (nnf (Not p2))
              Or p1 p2  -> And (nnf (Not p1)) (nnf (Not p2))
nnf (And a b) = And (nnf a) (nnf b)
nnf (Or a b) = Or (nnf a) (nnf b)