module Ficha8 where

import Data.Char

-- ** 1 **

data Frac = F Integer Integer

f1 = F (-33) (-51)
f2 = F 50 (-5)
f3 = F 10000 10

-- a

normaliza :: Frac -> Frac
normaliza (F x y) 
   | y < 0 = normaliza (F (-x) (-y))
   | otherwise = F (x `div` z) (y `div` z)
      where 
        z = mdc x y 

mdc :: Integer -> Integer -> Integer
mdc x 0 = x
mdc 0 y = y
mdc x y = mdc y (x `mod` y)

-- b

instance Eq Frac where 
    (==) = fracCompare

fracCompare :: Frac -> Frac -> Bool
fracCompare x y = x1 == x2 && y1 == y2
   where 
    F x1 y1 = normaliza x
    F x2 y2 = normaliza y

-- c

instance Ord Frac where
    (<=) = fracOrd

fracOrd :: Frac -> Frac -> Bool
fracOrd x y = x1 * y2 <= x2 * y1
   where 
    F x1 y1 = normaliza x
    F x2 y2 = normaliza y

-- d

instance Show Frac where
    show = ppFrac

ppFrac :: Frac -> String
ppFrac f = show x1 ++ "/" ++ show y1
   where 
    F x1 y1 = normaliza f

-- e

instance Num Frac where
    (+) = somaF
    (*) = multF
    abs = moduF
    signum = signF
    fromInteger = frIntF
    negate = menosF

somaF :: Frac -> Frac -> Frac
somaF (F x1 y1) (F x2 y2) = normaliza (F (x1 * y2 + x2 * y1) (y1 * y2))

multF :: Frac -> Frac -> Frac
multF (F x1 y1) (F x2 y2) = normaliza (F (x1 * x2) (y1 * y2))

moduF :: Frac -> Frac
moduF f = F (abs x1) y1 
   where 
    F x1 y1 = normaliza f

signF :: Frac -> Frac
signF f = F (signum x1) y1
   where 
    F x1 y1 = normaliza f

frIntF :: Integer -> Frac
frIntF f = F f 1 

menosF :: Frac -> Frac
menosF (F x1 y1) = normaliza (F (-x1) y1)

-- f

funF :: Frac -> [Frac] -> [Frac]
funF f fs = filter (>2*f) fs

-- ** 2 **

data Exp a = Const a
           | Simetrico (Exp a)
           | Mais (Exp a) (Exp a)
           | Menos (Exp a) (Exp a)
           | Mult (Exp a) (Exp a)

-- e = 3 + 4 * 5
e :: Exp Int
e = (Const 3) `Mais` ((Const 4) `Mult` (Const 5))

-- e' = 2 + 5 * 7
e' :: Exp Int
e' = (Const 2) `Mais` ((Const 5) `Mult` (Const 7))

-- a

pp :: Show a => Exp a -> String
pp (Const v) = show v
pp (Simetrico e) = "( -" ++ pp e ++ ")"
pp (Mais e1 e2) = "(" ++ pp e1 ++ "+" ++ pp e2 ++ ")"
pp (Menos e1 e2) = "(" ++ pp e1 ++ "-" ++ pp e2 ++ ")"
pp (Mult e1 e2) = "(" ++ pp e1 ++ "*" ++ pp e2 ++ ")"

instance Show a => Show (Exp a) where
     show e = pp e

-- b

calcula :: Num a => Exp a -> a
calcula (Const v) = v
calcula (Simetrico e) = - calcula e
calcula (Mais e1 e2) = calcula e1 + calcula e2
calcula (Menos e1 e2) = calcula e1 - calcula e2
calcula (Mult e1 e2) = calcula e1 * calcula e2

iguais :: (Num a, Eq a) => Exp a -> Exp a -> Bool
iguais e1 e2 = calcula e1 == calcula e2

instance (Num a, Eq a) => Eq (Exp a) where
    (==) = iguais

-- c

adicao :: (Num a) => Exp a -> Exp a -> Exp a
adicao e1 e2 = Const $ calcula e1 + calcula e2

multiplicacao :: (Num a) => Exp a -> Exp a -> Exp a
multiplicacao e1 e2 = Const $ calcula e1 * calcula e2

modulo :: (Num a) => Exp a -> Exp a
modulo e1 = Const $ abs (calcula e1) 

signumExp :: (Num a) => Exp a -> Exp a
signumExp e1 = Const $ signum (calcula e1)

fromIntegerExp :: (Num a) => Integer -> Exp a
fromIntegerExp e1 = Const $ fromInteger e1

subtracao :: (Num a) => Exp a -> Exp a -> Exp a
subtracao e1 e2 = Const $ calcula e1 - calcula e2

instance (Num a) => Num (Exp a) where
    (+) = adicao
    (-) = subtracao
    (*) = multiplicacao
    abs = modulo
    signum = signumExp
    fromInteger = fromIntegerExp

-- ** 3 ** 

data Movimento = Credito Float | Debito Float
data Data = D Int Int Int deriving (Eq)
data Extracto = Ext Float [(Data, String, Movimento)]

-- a

dataA :: Data
dataA = D 2023 12 28

instance Ord Data where
    (<=) = dataCompare

dataCompare :: Data -> Data -> Bool
dataCompare (D a1 m1 d1) (D a2 m2 d2) 
    | a1 < a2 = True
    | a1 == a2 && m1 < m2 = True
    | a1 == a2 && m1 == m2 && d1 <= d2 = True
    | otherwise = False 

-- b

instance Show Data where
    show = dd 

dd :: Data -> String
dd (D x y z) = show x ++ "/" ++ show y ++ "/" ++ show z

-- c 

ordena :: Extracto -> Extracto
ordena (Ext i l) = Ext i (insertionSort l)

insertionSort :: [(Data, String, Movimento)] -> [(Data, String, Movimento)]
insertionSort [] = []
insertionSort (l:ls) = insert l (insertionSort ls)

insert :: (Data, String, Movimento) -> [(Data, String, Movimento)] -> [(Data, String, Movimento)]
insert x [] = [x]
insert x@(d1, _, _) (y@(d2, _, _) : ys)
    | d1 <= d2   = x : y : ys
    | otherwise  = y : insert x ys

-- d

shopping :: Extracto
shopping = (Ext 300 [(D 2010 4 5, "Deposito",Credito 2000),
                     (D 2010 8 10,"Compra",  Debito 37.5),
                     (D 2010 9 1, "LEV",     Debito 90),
                     (D 2010 1 7, "Juros",   Credito 100),
                     (D 2010 1 22,"Anuidade",Debito 8)
                    ]
            )

instance Show Extracto where
    show = ee

ee :: Extracto -> String
ee (Ext s l) = "Saldo anterior: " ++ show s ++
               "\n----------------------------------" ++
               "\nData      Descricao Credito Debito" ++ 
               "\n----------------------------------\n" ++
               concatMap bill (insertionSort l) ++
               "----------------------------------\n" ++
               "Saldo actual: " ++ show (saldoA s l)

saldoA :: Float -> [(Data, String, Movimento)] -> Float
saldoA sa l = foldl (\sn (_,_,money) -> case money of
                                          Debito cash  -> sn - cash
                                          Credito cash -> sn + cash
                    ) sa l

bill :: (Data, String, Movimento) -> String
bill (d,s,m) =
    show d ++ " " ++ showDesc s ++ " " ++ showC m ++ " " ++ showD m ++ "\n"

showDesc :: String -> String
showDesc x = map toUpper x

showC :: Movimento -> String
showC (Credito money) = show money
showC _ = "     "

showD :: Movimento -> String
showD (Debito money) = show money
showD _ = "     "