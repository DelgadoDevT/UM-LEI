module Main where

import System.Random
import Data.Char

-- ** 1 **

-- a

bingo :: IO ()
bingo = do l <- geraNumeros [] 
           print l

geraNumeros :: [Int] -> IO [Int]
geraNumeros l 
   | length l == 9 = return l
   | otherwise      = do n <- randomRIO (1,9)
                         print n
                         getChar
                         if n `elem` l 
                         then geraNumeros l 
                         else geraNumeros (n:l)
-- b

mastermind :: IO ()
mastermind = do
    ss <- geraSeqSecreta
    print ss
    jogar ss

jogar :: (Int, Int, Int, Int) -> IO ()
jogar cs = do
    ns <- lerNumeros
    if cs == ns
        then print "Acertou!"
        else do
            let (i, _) = feedback cs ns
            putStrLn $ "Iguais: " ++ show i
            jogar cs

geraSeqSecreta :: IO (Int, Int, Int, Int)
geraSeqSecreta = do
    c1 <- randomRIO (0, 9)
    c2 <- randomRIO (0, 9)
    c3 <- randomRIO (0, 9)
    c4 <- randomRIO (0, 9)
    return (c1, c2, c3, c4)

lerNumeros :: IO (Int, Int, Int, Int)
lerNumeros = do
    putStrLn "Introduza 4 dígitos"
    [d1, d2, d3, d4] <- fmap (map digitToInt) getLine
    return (d1, d2, d3, d4)

feedback :: (Int, Int, Int, Int) -> (Int, Int, Int, Int) -> (Int, Int)
feedback (c1, c2, c3, c4) (d1, d2, d3, d4) =
    let iguais = length (filter id [c1 == d1, c2 == d2, c3 == d3, c4 == d4])
    in (iguais, 0)

-- 2 

data Aposta = Ap [Int] (Int,Int) deriving Show

-- a

valida :: Aposta -> Bool
valida (Ap n (e1,e2)) = (length n) == 5 && e1 `elem` [1..9] && e2 `elem` [1..9] && all (\x -> x `elem` [1..50]) n

-- b

comuns :: Aposta -> Aposta -> (Int,Int)
comuns (Ap n es1) (Ap cn es2) = (na,ea)
   where
    na = length $ filter (==True) (map (\x -> x `elem` cn) n)
    ea = compEst es1 es2
      where 
        compEst (e1,e2) (e3,e4)
          | e1 == e3 && e2 == e4 = 2
          | e1 == e3 = 1
          | e2 == e4 = 1
          | otherwise = 0

-- c

instance Eq Aposta where
    (==) = comparaA

comparaA :: Aposta -> Aposta -> Bool
comparaA a1 a2 = rn == 5 && re == 2
  where 
    (rn,re) = comuns a1 a2

premio :: Aposta -> Aposta -> Maybe Int
premio a1 a2 = case comuns a1 a2 of
                    (5, 2) -> Just 1
                    (5, 1) -> Just 2
                    (5, 0) -> Just 3
                    (4, 2) -> Just 4
                    (4, 1) -> Just 5
                    (4, 0) -> Just 6
                    (3, 2) -> Just 7
                    (2, 2) -> Just 8
                    (3, 1) -> Just 9
                    (3, 0) -> Just 10
                    (1, 2) -> Just 11
                    (2, 1) -> Just 12
                    (2, 0) -> Just 13
                    _      -> Nothing

-- d

leAposta :: IO Aposta
leAposta = do
  putStrLn "Digite os 5 números do Euromilhões (separados por espaço):"
  nu <- getLine
  putStrLn "Digite as 2 estrelas da Euromilões (separadas por espaço):"
  es <- getLine
  let nul = map read (words nu)
      esl = map read (words es)
  if valida (Ap nul (head esl, last esl))
    then return (Ap nul (head esl, last esl))
    else do
      putStrLn "A aposta não é válida. Tente novamente."
      leAposta

joga :: Aposta -> IO ()
joga key = do
     ap <- leAposta
     let pr = premio key ap
     case pr of
        Just p -> putStrLn $ "Prémio: " ++ show p
        Nothing -> putStrLn "Aposta sem prémio"
                 
-- e 

geraChave :: IO Aposta
geraChave = do 
    n1 <- randomRIO (1,50)
    n2 <- randomRIO (1,50)
    n3 <- randomRIO (1,50)
    n4 <- randomRIO (1,50)
    n5 <- randomRIO (1,50)
    e1 <- randomRIO (1,9)
    e2 <- randomRIO (1,9)
    return (Ap [n1,n2,n3,n4,n5] (e1,e2))

-- f

main :: IO ()
main = do ch <- geraChave
          ciclo ch

menu :: IO String
menu = do { putStrLn menutxt
          ; putStr "Opcao: "
          ; c <- getLine
          ; return c
          }
    where menutxt = unlines ["",
                             "Apostar ............................. 1",
                             "Gerar nova chave .................... 2",
                             "",
                             "Sair ................................ 0"]

ciclo :: Aposta -> IO ()
ciclo ch = do
    putStrLn "Que começe o jogo"
    opcao <- menu
    case opcao of
        "1" -> do
            joga ch
            ciclo ch
        "2" -> do
            nch <- geraChave
            ciclo nch
        "0" -> putStrLn "Obrigado por jogar!"
        _ -> do
            putStrLn "Opção inválida. Tente novamente."
            ciclo ch