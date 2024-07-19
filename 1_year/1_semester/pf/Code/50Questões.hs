module Questoes50 where

-- 1

enumFromTo' :: Int -> Int -> [Int]
enumFromTo' a b 
  | a > b = []
  | otherwise = a : enumFromTo' (a + 1) b

-- 2

enumFromThenTo' :: Int -> Int -> Int -> [Int]
enumFromThenTo' a b c 
   | a > c && b >= a || a < c && b < a = []
   | otherwise = a : enumFromThenTo' b (2 * b - a) c 

-- 3

(+++) :: [a] -> [a] -> [a]
(+++) [] l = l 
(+++) (l:ls) lt = l : (+++) ls lt 

-- 4 

(!!!) :: [a] -> Int -> a
(!!!) (l:ls) 0 = l
(!!!) (l:ls) n = (!!!) ls (n-1)

-- 5

reverse' :: [a] -> [a]
reverse' [] = []
reverse' (l:ls) = reverse' ls ++ [l]

-- 6

take' :: Int -> [a] -> [a]
take' 0 l = []
take' n (l:ls) = l : take' (n-1) ls

-- 7 

drop' :: Int -> [a] -> [a]
drop' 0 l = l
drop' n (l:ls) = drop' (n-1) ls  

-- 8

zip' :: [a] -> [b] -> [(a,b)]
zip' [] _ = []
zip' _ [] = [] 
zip' (x:xs) (y:ys) = (x,y) : zip' xs ys  

-- 9

replicate' :: Int -> a -> [a]
replicate' 0 _ = []
replicate' r e = e : replicate' (r-1) e

-- 10

intersperce' :: a -> [a] -> [a]
intersperce' n [] = []
intersperce' n (l:ls) = l : n : intersperce' n ls 

-- 11

group' :: Eq a => [a] -> [[a]]
group' [] = []
group' (l:ls) = (l:x) : group' y
    where (x,y) = span (== l) ls

-- 12

concat' :: [[a]] -> [a]
concat' [] = []
concat' (l:ls) = l ++ concat' ls  

-- 13

inits :: [a] -> [[a]] 
inits [] = [[]]
inits l = inits (init l) ++ [l]

-- 14

tails :: [a] -> [[a]]
tails [] = [[]]
tails l = l : tails (tail l)

-- 15 

heads :: [[a]] -> [a]
heads [] = []
heads ([]:ls) = heads ls
heads (l:ls) = head l : heads ls

-- 16

total :: [[a]] -> Int 
total [] = 0
total (l:ls) = length l + total ls

-- 17

fun :: [(a,b,c)] -> [(a,c)]
fun [] = []
fun ((a,b,c):r) = (a,c) : fun r
 
-- 18

cola :: [(String,b,c)] -> String
cola [] = ""
cola ((a,b,c):r) = a ++ cola r 

-- 19

idade :: Int -> Int -> [(String,Int)] -> [String]
idade _ _ [] = []
idade y i ((a,b):r)
   | y - b >= i = a : idade y i r 
   | otherwise = idade y i r

-- 20

powerEnumFrom :: Int -> Int -> [Int]
powerEnumFrom n 1 = [1]
powerEnumFrom n m
  | m > 1 = powerEnumFrom n (m-1) ++ [n^(m-1)]
  | otherwise = []

-- 21

isPrime :: Int -> Bool
isPrime n = n >= 2 && isPrimeH n 2
    where 
      isPrimeH :: Int -> Int -> Bool
      isPrimeH n m 
        | m * m > n = True 
        | mod n m == 0 = False
        | otherwise = isPrimeH n (m+1)

-- 22

isPrefixOf :: Eq a => [a] -> [a] -> Bool
isPrefixOf [] _ = True
isPrefixOf _ [] = False
isPrefixOf (x:xs) (y:ys) 
   | x == y = isPrefixOf xs ys 
   | otherwise = False

-- 23

isSuffixOf :: Eq a => [a] -> [a] -> Bool
isSuffixOf [] _ = True
isSuffixOf _ [] = False
isSuffixOf x y
   | last x == last y = isSuffixOf (init x) (init y)
   | otherwise = False

-- 24

isSubsequenceOf :: Eq a => [a] -> [a] -> Bool
isSubsequenceOf [] _ = True
isSubsequenceOf _ [] = False
isSubsequenceOf (x:xs) (y:ys)
   | x == y = isSubsequenceOf xs ys 
   | otherwise = isSubsequenceOf (x:xs) ys

-- 25

elemIndices :: Eq a => a -> [a] -> [Int]
elemIndices p l = elemH p l 0 

elemH :: Eq a => a -> [a] -> Int -> [Int]
elemH _ [] _ = []
elemH p (l:ls) n
   | p == l = n : elemH p ls (n+1)
   | otherwise = elemH p ls (n+1)

-- 26

nub :: Eq a => [a] -> [a]
nub [] = []
nub (l:ls) = if l `elem` ls then nub ls else l : nub ls

-- 27

delete :: Eq a => a -> [a] -> [a]
delete _ [] = []
delete n (l:ls) = if n == l then ls else l : delete n ls 

-- 28

(\\) :: Eq a => [a] -> [a] -> [a]
(\\) l [] = l 
(\\) [] _ = []
(\\) (l:ls) (r:rs) = if l == r then (\\) ls rs else l : (\\) ls (r:rs) 

-- 29

union :: Eq a => [a] -> [a] -> [a]
union l [] = l
union [] _ = []
union l (r:rs) = if r `elem` l then union l rs else union (l ++ [r]) rs

-- 30

intersect :: Eq a => [a] -> [a] -> [a]
intersect _ [] = []
intersect [] _ = []
intersect (l:ls) r = if l `elem` r then l : intersect ls r else intersect ls r

-- 31

insert :: Ord a => a -> [a] -> [a]
insert x [] = [x]
insert x (l:ls) 
   | x > l = l : insert x ls 
   | otherwise = x : (l:ls)

-- 32

unwords' :: [String] -> String
unwords' [] = ""
unwords' (l:ls) = l ++ (if null ls then "" else " ") ++ unwords' ls

-- 33

unlines' :: [String] -> String 
unlines' [] = ""
unlines' (l:ls) = l ++ "\n" ++ unlines' ls 

-- 34

pMaior :: Ord a => [a] -> Int 
pMaior l = pMaiorH l 0

pMaiorH :: Ord a => [a] -> Int -> Int
pMaiorH [x] n = n
pMaiorH (l:ls:lr) n
  | ls > l = pMaiorH (ls:lr) (n+1)
  | otherwise = n  

-- 35

lookup' :: Eq a => a -> [(a,b)] -> Maybe b 
lookup' _ [] = Nothing
lookup' e ((a,b):c) 
  | e == a = Just b 
  | otherwise = lookup' e c

-- 36

preCrescente :: Ord a => [a] -> [a]
preCrescente [] = []
preCrescente [x] = [x]
preCrescente (a:b:c)
   | a <= b = a : preCrescente (b:c)
   | otherwise = [a]

-- 37

iSort :: Ord a => [a] -> [a]
iSort [] = []
iSort (l:ls) = insert l (iSort ls)

-- 38

menor :: String -> String -> Bool
menor _ "" = False
menor "" _ = True
menor (x:xs) (y:ys)
  | x < y = True 
  | x == y = menor xs ys 
  | otherwise = False

-- 39

elemMSet :: Eq a => a -> [(a,Int)] -> Bool
elemMSet _ [] = False
elemMSet e ((l,ls):lr) = e == l || elemMSet e lr

-- 40 

converteMSet :: [(a,Int)] -> [a]
converteMSet [] = []
converteMSet ((l,ls):lr) = replicate ls l ++ converteMSet lr

-- 41

insereMSet :: Eq a => a -> [(a,Int)] -> [(a,Int)]
insereMSet x [] = [(x,1)]
insereMSet x ((l,ls):lr) = if x == l then ((l,ls+1):lr) else (l,ls) : insereMSet x lr

-- 42 

removeMSet :: Eq a => a -> [(a,Int)] -> [(a,Int)]
removeMSet _ [] = []
removeMSet x ((l,ls):lr) 
   | x == l = if ls > 1 then (l,ls-1) : lr else lr 
   | otherwise = (l,ls) : removeMSet x lr

-- 43 

constroiMSet :: Ord a => [a] -> [(a,Int)]
constroiMSet [] = []
constroiMSet (l:ls) = insereMSet l (constroiMSet ls)

-- 44

partitionEithers :: [Either a b] -> ([a],[b])
partitionEithers [] = ([],[])
partitionEithers ((Left l):ls) = (l:xs,ys)
     where (xs,ys) = partitionEithers ls 
partitionEithers ((Right l):ls) = (xs,l:ys)
     where (xs,ys) = partitionEithers ls 

-- 45

catMaybes :: [Maybe a] -> [a]
catMaybes [] = []
catMaybes (l:ls) = case l of 
                    Just x  -> x : catMaybes ls 
                    Nothing -> catMaybes ls

-- 46

data Movimento = Norte | Sul | Este | Oeste deriving Show

caminho :: (Int,Int) -> (Int,Int) -> [Movimento]
caminho (x1,y1) (x2,y2)
    | x1 < x2 = Este  : caminho (x1+1,y1) (x2,y2)
    | x1 > x2 = Oeste : caminho (x1-1,y1) (x2,y2)
    | y1 < y2 = Norte : caminho (x1,y1+1) (x2,y2)
    | y1 > y2 = Sul   : caminho (x1,y1-1) (x2,y2)
    | otherwise = []

-- 47

posicao :: (Int,Int) -> [Movimento] -> (Int,Int)
posicao p [] = p
posicao (x, y) (Norte:t) = posicao (x, y + 1) t
posicao (x, y) (Sul:t) = posicao (x, y - 1) t
posicao (x, y) (Este:t) = posicao (x + 1, y) t
posicao (x, y) (Oeste:t) = posicao (x - 1, y) t

hasLoops :: (Int,Int) -> [Movimento] -> Bool
hasLoops _ [] = False
hasLoops p m = p == posicao p m || hasLoops p (init m)

-- 48

type Ponto = (Float,Float)
data Rectangulo = Rect Ponto Ponto

contaQuadrados :: [Rectangulo] -> Int
contaQuadrados [] = 0
contaQuadrados ((Rect (x1,y1) (x2,y2)):lr)
   | abs (x2-x1) == abs (y2-y1) = 1 + contaQuadrados lr 
   | otherwise = contaQuadrados lr

-- 49

areaTotal :: [Rectangulo] -> Float
areaTotal [] = 0
areaTotal ((Rect (x1,y1) (x2,y2)):lr) = abs (x2-x1) * abs (y2-y1) + areaTotal lr

-- 50

data Equipamento = Bom | Razoavel | Avariado deriving Show

naoReparar :: [Equipamento] -> Int
naoReparar [] = 0 
naoReparar (e:es) = case e of
                     Avariado -> naoReparar es
                     _        -> 1 + naoReparar es