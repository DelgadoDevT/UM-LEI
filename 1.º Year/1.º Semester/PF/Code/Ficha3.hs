module Ficha3 where

-- ** 1 **

data Hora = H Int Int
          deriving Show

type Etapa = (Hora, Hora)
type Viagem = [Etapa]

viagem1 :: Viagem
viagem1 = [(H 9 30, H 10 25), (H 11 20, H 12 45), (H 13 30, H 14 45)] 

-- a

validaE :: Etapa -> Bool
validaE (H h1 m1, H h2 m2) = (h2 > h1 || h2 == h1 && m1 < m2) && (h1 <= 24 && h1 >= 0 && h2 <= 24 && h2 >= 0 && m1 <= 60 && m1 >= 0 && m2 <= 60 && m2 >= 0)

-- b

validaV :: Viagem -> Bool
validaV [] = True
validaV [h] = validaE h
validaV (l@(h,m):ls) = validaE l && validaV ls && horToMin m < horToMin h2 
    where 
      (h2,m2) = head ls 

horToMin :: Hora -> Int
horToMin (H h m) = h * 60 + m

-- c

durVia :: Viagem -> Etapa
durVia l = (fst $ head l, snd $ last l)

-- d

tViagem :: Viagem -> Hora
tViagem [] = H 0 0
tViagem ((ti,tf):es) = somH (difH ti tf) (tViagem es)

somH :: Hora -> Hora -> Hora
somH h1 h2 = minToHor ((horToMin h2) + (horToMin h1))

difH :: Hora -> Hora -> Hora
difH h1 h2 = minToHor ((horToMin h2) - (horToMin h1))

-- e

tEspera :: Viagem -> Hora 
tEspera l = minToHor (f l)
  where 
    f :: Viagem -> Int
    f [] = 0 
    f [(h1, h2)] = 0
    f ((h1,h2):(h3,h4):r) = horToMin h3 - horToMin h2 + f ((h3,h4):r)

minToHor :: Int -> Hora
minToHor m = (H (m `div` 60) (m `mod` 60))

-- f

tTotal :: Viagem -> Hora
tTotal v = minToHor ((h2 * 60) - (h1 * 60) + (m2 - m1))
   where
    (H h1 m1) = fst (head v)
    (H h2 m2) = snd (last v)

-- ** 2 **  

type Poligonal = [Ponto]

data Ponto = Cartesiano Double Double | Polar Double Double
           deriving (Show,Eq)

data Figura = Circulo Ponto Double
            | Rectangulo Ponto Ponto
            | Triangulo Ponto Ponto Ponto
            deriving (Show,Eq)

-- a

comPol :: Poligonal -> Double
comPol [] = 0
comPol [_] = 0
comPol ((Cartesiano x1 y1):(Cartesiano x2 y2):r) =
    sqrt ((x2-x1)^2 + (y2-y1)^2) + comPol ((Cartesiano x2 y2):r)

-- b

lf :: Poligonal -> Bool
lf l = head l == last l && length l >= 3

-- c

triangula :: Poligonal -> [Figura]
triangula (p1:p2:p3:ps)
    | p1 == p3 = []
    | otherwise = Triangulo p1 p2 p3 : triangula (p1:p3:ps)
triangula _ = []

-- d

areaP :: Poligonal -> Double
areaP p = areaT (triangula p)

areaT :: [Figura] -> Double
areaT [] = 0
areaT (l:ls) =  area l + areaT ls

area :: Figura -> Double
area (Triangulo p1 p2 p3) =
      let a = dist p1 p2     
          b = dist p2 p3
          c = dist p3 p1
          s = (a+b+c) / 2 
      in sqrt (s*(s-a)*(s-b)*(s-c))

dist :: Ponto -> Ponto -> Double
dist ( Cartesiano x1 y1) (Cartesiano x2 y2) = sqrt ((x2-x1)^2 + (y2-y1)^2)

-- e

mover :: Poligonal -> Ponto -> Poligonal
mover l p = p : l

-- f

zoom :: Double -> Poligonal -> Poligonal
zoom _ [] = []
zoom f (l:ls) = l : map (escP f l) ls

escP :: Double -> Ponto -> Ponto -> Ponto
escP f (Cartesiano x1 y1) (Cartesiano x2 y2) =
  Cartesiano (x1 + f * (x2 - x1)) (y1 + f * (y2 - y1))
escP f (Polar r1 theta1) (Polar r2 theta2) =
  Polar (r1 + f * (r2 - r1)) (theta1 + f * (theta2 - theta1))
escP _ _ _ = error "Incompatibilidade com tipos de pontos"

-- ** 3 **

type Nome = String
data Contacto = Casa Integer | Trab Integer | Tlm Integer | Email String
              deriving Show
type Agenda = [(Nome, [Contacto])]

agenda1 :: Agenda
agenda1 = [
    ("Jarilene", [Tlm 926843246, Email "jari123@hotmail.com"]),
    ("Jacinto", [Casa 987417593, Tlm 987417594]),
    ("Javier", [Casa 098123576, Trab 987654321])
    ]

-- a

acrescEmail :: Nome -> String -> Agenda -> Agenda
acrescEmail n c [] = [(n, [Email c])]
acrescEmail n c ((l,ls):lt)
    | n == l = (l, Email c : ls) : lt
    | otherwise = (l,ls) : acrescEmail n c lt

-- b

verEmails :: Nome -> Agenda -> Maybe [String]
verEmails _ [] = Nothing
verEmails n ((l,ls):lt)
    | n == l = Just (onMail ls) 
    | otherwise = verEmails n lt

onMail :: [Contacto] -> [String]
onMail [] = []
onMail (l:ls) = case l of 
                 Email n -> n : onMail ls 
                 _       -> onMail ls 

-- c

consTelefs :: [Contacto] -> [Integer]
consTelefs [] = []
consTelefs (n:ns) = case n of 
                     Casa num -> num : consTelefs ns
                     Trab num -> num : consTelefs ns
                     Tlm num  -> num : consTelefs ns

-- d

casa :: Nome -> Agenda -> Maybe Integer
casa _ [] = Nothing
casa n ((no, contactos):nr) =
    case lookup n ((no, contactos):nr) of
        Just contatos -> obterNumeroCasa contatos
        Nothing -> casa n nr

obterNumeroCasa :: [Contacto] -> Maybe Integer
obterNumeroCasa [] = Nothing
obterNumeroCasa (Casa num : _) = Just num
obterNumeroCasa (_ : rest) = obterNumeroCasa rest

-- ** 4 **

type Dia = Int
type Mes = Int
type Ano = Int
type Nome2 = String

data Data = D Dia Mes Ano
          deriving Show

type TabDN = [(Nome, Data)]

-- a

procura :: Nome2 -> TabDN -> Maybe Data
procura nome tabDN = lookup nome tabDN

-- b 

idade :: Data -> Nome2 -> TabDN -> Maybe Int
idade dataAtual nome tabDN = do
  dataNascimento <- procura nome tabDN
  return $ calcularIdade dataAtual dataNascimento

calcularIdade :: Data -> Data -> Int
calcularIdade (D diaAtual mesAtual anoAtual) (D diaNasc mesNasc anoNasc)
  | mesAtual > mesNasc || (mesAtual == mesNasc && diaAtual >= diaNasc) = anoAtual - anoNasc
  | otherwise = anoAtual - anoNasc - 1

tabelaNascimentos = [("Alice", D 12 5 1990), ("Bob", D 3 8 1985),("Jose", D 13 4 1976)]
dataAtual = D 15 5 2023

-- c

anterior :: Data -> Data -> Bool
anterior (D d m a) (D d' m' a')
  | a < a' || a == a' && m < m' || a == a' && m == m' && d < d' = True 
  | otherwise = False 

-- d

ordena :: TabDN -> TabDN
ordena [] = []
ordena (l:ls) = insert l (ordena ls)

insert :: (Nome, Data) -> TabDN -> TabDN
insert f [] = [f]
insert (n,d) ((n',d'):r)
  | anterior d d' = ((n,d):(n',d'):r)
  | otherwise = (n',d') : insert (n,d) r

-- e

porIdade :: Data -> TabDN -> [(Nome,Int)]
porIdade y t = porIdadeH y (ordena t)

porIdadeH :: Data -> TabDN -> [(Nome,Int)]
porIdadeH _ [] = []
porIdadeH y ((t,ts):tt) = [(t, calcularIdade ts y)] ++ porIdade y tt

-- ** 5 ** 

data Movimento = Credito Float | Debito Float
               deriving Show

data Extracto = Ext Float [(Data, String, Movimento)]
              deriving Show
              
-- a 

extValor :: Extracto -> Float -> [Movimento]
extValor (Ext _ []) _ = []
extValor (Ext n ((d,s,m):r)) va
   | abs (case m of Credito x -> x; Debito x -> x) > va = m : extValor (Ext n r) va
   | otherwise = extValor (Ext n r) va

extracto1 = Ext 100.0 [(D 1 1 2023, "Compra", Debito 30.0), (D 2 1 2023, "Saldo", Credito 50.0)]

-- b 

filtro :: Extracto -> [String] -> [(Data,Movimento)]
filtro (Ext _ []) _ = []
filtro (Ext si ((t,d,m):re)) [] = []
filtro (Ext si ((t, d, m):re)) (s:ss)
   | s == d = (t, m) : filtro (Ext si re) ss
   | otherwise = filtro (Ext si re) (s:ss)

-- c

creDeb :: Extracto -> (Float,Float)
creDeb (Ext _ []) = (0,0)
creDeb (Ext i ((_,_,t):l)) = (x + a, y + b)
      where 
        (x,y) = creDeb (Ext i l)
        (a,b) = case t of 
                 Credito v -> (v,0)
                 Debito  v -> (0,v)

-- d

saldo :: Extracto -> Float 
saldo (Ext _ []) = 0
saldo (Ext i ((_,_,t):l)) = case t of 
                              Credito v -> v + saldo (Ext i l)
                              Debito  v -> (-v) + saldo (Ext i l)