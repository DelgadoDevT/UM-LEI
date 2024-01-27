module Ficha1 where

import Data.Char

-- ** 1 **

-- a

perimetro :: Double -> Double
perimetro r = 2 * pi * r

type Ponto = (Double,Double)

-- b

dist :: Ponto -> Ponto -> Double
dist (x1,y1) (x2,y2) = sqrt ((x2-x1)^2 + (y2-y1)^2)

-- c

primUlt :: [String] -> (String,String)
primUlt l = (head l, last l)

-- d

multiplo :: Int -> Int -> Bool
multiplo m n = if mod n m == 0 then True else False 

-- e 

truncaImpar :: [String] -> [String]
truncaImpar l = if odd (length l) == True then tail l else l

-- f

max2 :: Double -> Double -> Double
max2 a b = if a > b then a else b 

-- g

max3 :: Double -> Double -> Double -> Double
max3 a b c = max (max a b) c

-- ** 2 **

-- a 

nRaizes :: Double -> Double -> Double -> Int
nRaizes a b c = length (raizes a b c)

-- b 

raizes :: Double -> Double -> Double -> [Double]
raizes a b c
  | d < 0 = []
  | otherwise = [(-b + sqrt d) / (2*a), (-b - sqrt d) / (2*a)]
  where
    d = b^2 - 4*a*c

-- ** 3 ** 

type Hora = (Int,Int)

-- a

horaValida :: Hora -> Bool
horaValida (h,m) = h >= 0 && h <= 24 && m >= 0 && m < 60

-- b

compHora :: Hora -> Hora -> Bool
compHora (h1,m1) (h2,m2) 
  | h1 > h2 = True
  | m1 > m2 = True
  | otherwise = False

-- c

hToMin :: Hora -> Int
hToMin (h,m) = h * 60 + m

-- d
    
minToH :: Int -> Hora
minToH m = ((div m 60),(mod m 60))

-- e 

diffHoras :: Hora -> Hora -> Int
diffHoras h1 h2 = abs (hToMin h2 - hToMin h1)

-- f

addMin :: Hora -> Int -> Hora 
addMin t a = minToH (hToMin t + a)

-- ** 4 **

data HoraH = H Int Int deriving (Show,Eq)

-- a 

horaValidaH :: HoraH -> Bool
horaValidaH (H h m) = h >= 0 && h <= 24 && m >= 0 && m < 60

-- b

compHoraH :: HoraH -> HoraH -> Bool
compHoraH (H h1 m1) (H h2 m2)
  | h1 > h2 = True
  | m1 > m2 = True
  | otherwise = False

-- c

hToMinH :: HoraH -> Int
hToMinH (H h m) = h * 60 + m

-- d 

minToHH :: Int -> HoraH
minToHH m = (H (div m 60) (mod m 60))

-- e 

diffHorasH :: HoraH -> HoraH -> Int
diffHorasH h1 h2 = abs (hToMinH h2 - hToMinH h1)

-- f
addMinH :: HoraH -> Int -> HoraH
addMinH t am = minToHH (hToMinH t + am)

-- ** 5 **

data Semaforo = Verde | Amarelo | Vermelho deriving (Show,Eq)

-- a

next :: Semaforo -> Semaforo 
next Verde = Amarelo
next Amarelo = Vermelho
next Vermelho = Verde

-- b

stop :: Semaforo -> Bool
stop Verde = False
stop _ = True

-- c

safe :: Semaforo -> Semaforo -> Bool
safe s1 s2 = s1 == Vermelho && s2 == Vermelho

-- ** 6 **

data Ponto' = Cartesiano Double Double | Polar Double Double deriving (Show,Eq)

-- a

posx :: Ponto' -> Double
posx (Cartesiano x y) = abs y
posx (Polar l a) = cos a * l 

-- b

posy :: Ponto' -> Double
posy (Cartesiano x y) = abs x
posy (Polar l a) = sin a * l

-- c

raio :: Ponto' -> Double 
raio (Cartesiano x y) = sqrt (y^2 + x^2)
raio (Polar l a) = l

-- d

angulo :: Ponto' -> Double
angulo (Cartesiano x y) = atan (y/x)
angulo (Polar l a) = a

-- e

dist' :: Ponto' -> Ponto' -> Double
dist' d1 d2 = let x1 = posx d1
                  x2 = posx d2
                  y1 = posy d1
                  y2 = posy d2
              in sqrt ((x2 - x1) ^ 2 + (y2 - y1) ^ 2)

-- ** 7 ** 

data Figura = Circulo Ponto Double
            | Rectangulo Ponto Ponto
            | Triangulo Ponto Ponto Ponto
               deriving (Show,Eq)

-- a 

posx' :: Ponto -> Double
posx' (x,y) = y

posy' :: Ponto -> Double
posy' (x,y) = x

poligono :: Figura -> Bool
poligono (Circulo _ _) = False
poligono (Rectangulo c1 c2) = posy' c1 /= posy' c2 && posx' c1 /= posx' c2 
poligono (Triangulo c1 c2 c3) = posx' c1 /= posx' c2 || posx' c1 /= posx' c3 || posx' c2 /= posx' c3 &&
                                posy' c1 /= posy' c2 || posy' c1 /= posy' c3 || posy' c2 /= posy' c3 

-- b

vertices :: Figura -> [Ponto]
vertices (Circulo _ _) = []
vertices (Rectangulo c1 c2) = [c1, (posx' c1, posy' c2), c2, (posx' c2, posy' c1)]
vertices (Triangulo c1 c2 c3) = [c1, c2, c3]

-- c

area :: Figura -> Double
area (Triangulo p1 p2 p3) =
      let a = dist p1 p2     
          b = dist p2 p3
          c = dist p3 p1
          s = (a+b+c) / 2 
      in sqrt (s*(s-a)*(s-b)*(s-c))
area (Rectangulo p1 p2) = abs (posx' p2 - posx' p1) * abs (posy' p2 - posy' p1)
area (Circulo p r) = pi * (r^2)

-- d

perimetro' :: Figura -> Double
perimetro' (Circulo c r) = 2 * pi * r
perimetro' (Rectangulo c1 c2) = 2 * (abs (posx' c2 - posx' c1)) + 2 * (abs (posy' c2 - posy' c1))
perimetro' (Triangulo c1 c2 c3) = dist c1 c2 + dist c1 c3 + dist c2 c3

-- ** 8 **

-- a

isLower' :: Char -> Bool
isLower' a = ord a >= 97 && ord a <= 122

-- b

isDigit' :: Char -> Bool
isDigit' a = ord a >= 48 && ord a <= 57

-- c

isAlpha' :: Char -> Bool
isAlpha' a = ord a >= 65 && ord a <=90 || ord a >= 97 && ord a <= 122 

-- d 

toUpper' :: Char -> Char 
toUpper' a 
  | isLower' a = chr (ord a - 32)
  | otherwise = error "Não é uma letra minúscula"

-- e

intToDigit' :: Int -> Char 
intToDigit' a 
  | a >= 0 && a <= 9 = chr (a + 48)
  | otherwise = error "Não é um número"

-- f

digitToInt' :: Char -> Int
digitToInt' ch
    | isDigit ch = ord ch - 48
    | otherwise  = error "Não é um dígito"