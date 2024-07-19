module Ficha2 where

import Data.Char

-- ** 1 **

-- a

-- O valor de funA [2,3,5,1] é 39

-- b

-- O valor de funB [8,5,12] é [8,12]

-- c

-- O valor de funC [1,2,3,4,5] é [5]

-- d

-- O valor de funD "otrec" é "certo"

-- ** 2 **

-- a

dobros :: [Float] -> [Float]
dobros [] = []
dobros (l:ls) = (l * 2) : dobros ls

-- b

numOcorre :: Char -> String -> Int
numOcorre _ "" = 0
numOcorre c (l:ls) 
   | c == l = 1 + numOcorre c ls
   | otherwise = numOcorre c ls

-- c

positivos :: [Int] -> Bool
positivos [] = True 
positivos (l:ls)
  | l <= 0 = False
  | otherwise = positivos ls

-- d

soPos :: [Int] -> [Int]
soPos [] = []
soPos (l:ls)
  | l < 0 = soPos ls 
  | otherwise = l : soPos ls

-- e

somaNeg :: [Int] -> Int
somaNeg [] = 0
somaNeg (l:ls) 
  | l < 0 = l +  somaNeg ls
  | otherwise = somaNeg ls

-- f

tresUlt :: [a] -> [a]
tresUlt [] = []
tresUlt li@(l:ls)
  | length ls < 3 = li
  | otherwise = tresUlt ls 

-- g

segundos :: [(a,b)] -> [b]
segundos [] = []
segundos ((a,b):r) = b : segundos r 

-- h 

nosPrimeiros :: (Eq a) => a -> [(a,b)] -> Bool
nosPrimeiros _ [] = False
nosPrimeiros c ((l,ls):r)
   | c == l = True 
   | otherwise = nosPrimeiros c r 

-- i

sumTriplos :: (Num a, Num b, Num c) => [(a,b,c)] -> (a,b,c)
sumTriplos [] = (0,0,0) 
sumTriplos [(a, b, c)] = (a, b, c)
sumTriplos ((a,b,c):(a2,b2,c2):ls) = sumTriplos ((a + a2, b + b2, c + c2) : ls)

-- ** 3 **

-- a

soDigitos :: [Char] -> [Char]
soDigitos [] = []
soDigitos (l:ls)
  | isDigit l = l : soDigitos ls
  | otherwise = soDigitos ls

-- b

minusculas :: [Char] -> Int 
minusculas [] = 0 
minusculas (l:ls)
  | isLower l = 1 + minusculas ls 
  | otherwise = minusculas ls

-- c

nums :: String -> [Int]
nums [] = []
nums (l:ls) 
    | isDigit l = digitToInt l : nums ls
    | otherwise = nums ls

-- ** 4 **

type Polinomio = [Monomio]
type Monomio = (Float,Int)

p1 :: Polinomio
p1 = [(2,3), (3,4), (5,3), (4,5)]

-- a 

conta :: Int -> Polinomio -> Int 
conta _ [] = 0
conta g (l:ls)
   | g == snd l = 1 + conta g ls 
   | otherwise = conta g ls 

-- b

grau :: Polinomio -> Int 
grau [] = 0
grau (l:ls) 
  | snd l > grau ls = snd l
  | otherwise = grau ls  

-- c

selgrau :: Int -> Polinomio -> Polinomio 
selgrau _ [] = []
selgrau g (l:ls) 
  | g == snd l = l : selgrau g ls 
  | otherwise = selgrau g ls 

--d

deriv :: Polinomio -> Polinomio
deriv [] = []
deriv (l:ls) =
    (fst l * (fromIntegral (snd l)), snd l - 1) : deriv ls

-- e  

calcula :: Float -> Polinomio -> Float
calcula _ [] = 0
calcula v (l:ls) = fst l * (v ^ snd l) + calcula v ls

--f

simp :: Polinomio -> Polinomio
simp [] = []
simp (l:ls)
  | snd l == 0 = simp ls
  | otherwise = l : simp ls

-- g 

mult :: Monomio -> Polinomio -> Polinomio 
mult _ [] = []
mult (x,y) ((p1,p2):r) = (x * p1, y + p2) : mult (x,y) r

-- h

normaliza :: Polinomio -> Polinomio
normaliza [] = []
normaliza (l:ls) = normalizaH l (normaliza ls)

normalizaH :: Monomio -> Polinomio -> Polinomio
normalizaH p [] = [p]
normalizaH m@(x,y) (p@(p1,p2):r)
   | y == p2 = (x + p1, y) : r 
   | otherwise = p : normalizaH m r

-- i 

soma :: Polinomio -> Polinomio -> Polinomio
soma p [] = p
soma [] p = p 
soma (l:ls) p = somaH l (soma ls p)

somaH :: Monomio -> Polinomio -> Polinomio
somaH p [] = [p]
somaH m@(x,y) (p@(p1,p2):r)
   | y == p2 = (x + p1, y) : r
   | otherwise = p : somaH m r

-- j 

produto :: Polinomio -> Polinomio -> Polinomio
produto [] _ = []
produto (l:ls) p = mult l p ++ produto ls p

--k

ordena :: Polinomio -> Polinomio
ordena [] = []
ordena (x:xs) = insereOrdenado x (ordena xs)

insereOrdenado :: Monomio -> Polinomio -> Polinomio
insereOrdenado monomio [] = [monomio]
insereOrdenado monomio@(c, e) (m@(c', e'):resto)
    | e <= e'   = monomio : m : resto
    | otherwise = m : insereOrdenado monomio resto

-- l

equiv :: Polinomio -> Polinomio -> Bool
equiv c1 c2 = ordena (normaliza c1) == ordena (normaliza c2)