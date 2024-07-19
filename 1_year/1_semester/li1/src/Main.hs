module Main where

import LI12324
import Task1
import Task2
import Task3
import Task4
import Task5
import Graphics.Gloss
import Graphics.Gloss.Interface.Pure.Game
import Graphics.Gloss.Juicy
import System.Random
import System.Exit
import Alterations
import Data.Maybe (fromJust, fromMaybe, isJust)
import Graphics.Gloss 
import LI12324 
import Data.Bool 
import Graphics.Gloss.Interface.IO.Game
import System.Exit

main :: IO ()
main = do
    eI     <- estadoInicial
    putStrLn "Checking game validity..."
    if valida' (jogo eI) 
      then do 
        putStrLn "The game is valid, Starting ..."
        play window backgroundC fr eI desenhaEstado reageEvento reageTempo
      else do 
        putStrLn "The game is not valid, Ending ..."