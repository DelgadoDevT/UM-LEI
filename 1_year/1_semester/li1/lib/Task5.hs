{-|
Module      : Task5
Description : Graphical application that allows the user to play
Copyright   : Joao Pedro Delgado Teixeira <a106836@alunos.uminho.pt>
              Ricardo Miguel da Silva Morais <a106935@alunos.uminho.pt>

Module for the realisation of Task5 of LI1 in 2023/24.
-}
module Task5 where

import LI12324
import Task1
import Task2
import Task3
import Task4
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

{-| 
In this task we carried out the graphics component of the game, which allowed us to transpose all the previous tasks into a playable executable, based on the @arcade@ game "Donkey Kong".
To do this, we defined new @dates@ and @types@, and carried out all the necessary processes to load the images that will make up the game graphics, from the menus to the collectibles, enemies, etc.
-}

{-| 
The various menus in the game
-}
data Menu = EmJogo1 | MenuInicial | MCreditos | SelecMapa | PauseM | WinM | LoseM | Sure | MenuPkp | PauseP | WinP | LoseP | EmJogo2 deriving Eq

{-| 
The various options in the game
-}
data Opcoes = Limpo | Jogar | Sair | Creditos | Retornar | Retomar | NextMap | SelMap | Mapa1 | Mapa2 | Mapa3 | Plus

{-| 
Game themes/modes
-}
data Temas = PlusK | Classico deriving Eq

{-| 
The various auxiliary icons used during the game
-}
data Icon = TimerM | MoedaP | VidaI | Estrelinha deriving Eq

{-| 
The type used to describe the images of the map blocks
-}
type ImagensBlocos = [(Temas, [(Bloco, Picture)])]

{-| 
The type used to describe the images of the characters in the game
-}
type ImagensPersonagens = [(Temas, [(Entidade, Pictures)])]

{-| 
The type used to describe the images in the game's menus
-}
type ImagensMenus = [(Menu, Pictures)]

{-| 
The type used to describe the images of the game's collectibles
-}
type ImagensColecionaveis = [(Temas , [(Colecionavel, Picture)])]

{-| 
The type used to describe the images of the game icons
-}
type ImagensIcones = [(Icon, Pictures)]

{-| 
The type used to summarise a list of Pictures
-}
type Pictures = [Picture]

{-| 
The date responsible for gathering all types of images into a compact block
-}
data Imagens = Imagens {
             blocos :: ImagensBlocos, -- ^ Describe the images of the map blocks
             personagens :: ImagensPersonagens, -- ^ Describe the images of the characters in the game
             imenus :: ImagensMenus, -- ^ Describe the images of the game menus
             icolec :: ImagensColecionaveis, -- ^ Describe the images of the game collectibles
             iicons :: ImagensIcones -- ^ Describe the images of the game icons
                       }

{-| 
The date responsible for choosing the map to play in the game
-}
data Escolha = PrMapa | SnMapa | TrMapa | Nenhum

{-| 
The date responsible for defining the game itself, with all its parts
-}
data PrimateKong = PrimateKong { jogo :: Jogo -- ^ The map and current entities in the game
                               , menu :: Menu -- ^ Game menus
                               , opcao :: Opcoes -- ^ Game options
                               , imagens :: Imagens -- ^ Images / sprites used in the game
                               , tema :: Temas -- ^ Different game themes
                               , semente :: Semente -- ^ Seed used for in-game randomisation
                               , tempo :: Tempo -- ^ Time running on each map
                               , tecPress :: Tecpress -- ^ Used to continue a movement if the key is pressed.
                               , paridade :: Paridade -- ^ Used for animations with two frames, analysing time parity
                               , escolham :: Escolha -- ^ Chosen map game
                               }

{-| 
Function used to generate the random number that will form the game seed
-}
gerarSeed :: IO Int -- ^ Random value for the game seed
gerarSeed = randomRIO (1, 10000000)

{-| 
Function used to load images of map blocks
-}
gIBlocos :: IO ImagensBlocos -- ^ Images of the game blocks
gIBlocos = do
    Just iplataforma <- loadJuicyPNG "lib/Resources/Blocks/plataforma.png"
    Just ialcapao    <- loadJuicyPNG "lib/Resources/Blocks/alcapao.png"
    Just iescada     <- loadJuicyPNG "lib/Resources/Blocks/escada.png"
    Just uplataforma <- loadJuicyPNG "lib/Resources/Blocks/plataformau.png"
    Just ualcapao    <- loadJuicyPNG "lib/Resources/Blocks/alcapaou.png"
    Just uescada     <- loadJuicyPNG "lib/Resources/Blocks/escadau.png"
    return [(Classico, [(Plataforma, iplataforma), (Alcapao, ialcapao), (Escada, iescada)]),(PlusK, [(Plataforma, uplataforma), (Alcapao, ualcapao), (Escada, uescada)])]

{-| 
Function used to load images of game entities and their variations
-}
gIPersonagens :: IO ImagensPersonagens -- ^ Images of the game's characters
gIPersonagens = do
    Just ijogc  <- loadJuicyPNG "lib/Resources/Creatures/frido_corred.png"
    Just ijocp  <- loadJuicyPNG "lib/Resources/Creatures/frido_parado.png"
    Just ijocs  <- loadJuicyPNG "lib/Resources/Creatures/frido_salta.png"
    Just ijoce  <- loadJuicyPNG "lib/Resources/Creatures/frido_sobe.png"
    Just ijocep <- loadJuicyPNG "lib/Resources/Creatures/frido_espadao_parado.png"
    Just ijocea <- loadJuicyPNG "lib/Resources/Creatures/frido_espadao_saltad.png"
    Just imse   <- loadJuicyPNG "lib/Resources/Creatures/frido_espadao_corree.png"
    Just impe   <- loadJuicyPNG "lib/Resources/Creatures/frido_espadao_saltae.png"
    Just imec   <- loadJuicyPNG "lib/Resources/Creatures/frido_corree.png"
    Just imep   <- loadJuicyPNG "lib/Resources/Creatures/frido_paradoe.png"
    Just imf1   <- loadJuicyPNG "lib/Resources/Creatures/frido_espadao_corred.png"
    Just imf3   <- loadJuicyPNG "lib/Resources/Creatures/frido_espadao_paradoe.png"
    Just imf4   <- loadJuicyPNG "lib/Resources/Creatures/frido_saltae.png"
    Just ii1    <- loadJuicyPNG "lib/Resources/Creatures/fantasma_esquerda.png"
    Just ii2    <- loadJuicyPNG "lib/Resources/Creatures/fantasma_direita.png"
    Just im1    <- loadJuicyPNG "lib/Resources/Creatures/mamaco1.png"
    Just im2    <- loadJuicyPNG "lib/Resources/Creatures/mamaco2.png"
    Just im3    <- loadJuicyPNG "lib/Resources/Creatures/mamaco1d.png"
    Just im4    <- loadJuicyPNG "lib/Resources/Creatures/mamaco2d.png"
    Just ir1    <- loadJuicyPNG "lib/Resources/Creatures/inimigo_ragee.png"
    Just ir2    <- loadJuicyPNG "lib/Resources/Creatures/inimigo_raged.png"
    Just ir3    <- loadJuicyPNG "lib/Resources/Creatures/inimigo_ragees.png"
    Just jr1    <- loadJuicyPNG "lib/Resources/Creatures/frillua_corred.png"
    Just jr2    <- loadJuicyPNG "lib/Resources/Creatures/frillua_corree.png"
    Just jr3    <- loadJuicyPNG "lib/Resources/Creatures/frillua_paradod.png"
    Just jr4    <- loadJuicyPNG "lib/Resources/Creatures/frillua_paradoe.png"
    Just jr5    <- loadJuicyPNG "lib/Resources/Creatures/frillua_escada.png"
    Just jm1    <- loadJuicyPNG "lib/Resources/Creatures/frillua_corremd.png"
    Just jm2    <- loadJuicyPNG "lib/Resources/Creatures/frillua_correme.png"
    Just jm3    <- loadJuicyPNG "lib/Resources/Creatures/frillua_paradomd.png"
    Just jm4    <- loadJuicyPNG "lib/Resources/Creatures/frillua_paradome.png"
    Just jm5    <- loadJuicyPNG "lib/Resources/Creatures/frillua_escadam.png"
    return [(Classico, [(Jogador, [ijogc, ijocp, ijocs, ijoce, ijocep, ijocea, imse, impe, imec, imep, imf1, imf3, imf4]),(Fantasma, [ii1, ii2]),(MacacoMalvado, [im1, im2, im3, im4])]),(PlusK, [(Jogador, [jr1, jr2, jr3, jr4, jr5, jm1, jm2, jm3, jm4, jm5]),(Fantasma, [ir1, ir2, ir3]),(MacacoMalvado, [im1, im2, im3, im4])])]

{-| 
Function used to load Menu images
-}
gIMenu :: IO ImagensMenus -- ^ Images of the game menus
gIMenu = do
    Just mm1 <- loadJuicyPNG "lib/Resources/Menus/mMenu1.png"
    Just mm2 <- loadJuicyPNG "lib/Resources/Menus/mMenu2.png"
    Just mm3 <- loadJuicyPNG "lib/Resources/Menus/mMenu3.png"
    Just mm4 <- loadJuicyPNG "lib/Resources/Menus/mMenu4.png"
    Just mm5 <- loadJuicyPNG "lib/Resources/Menus/mMenu5.png"
    Just cr1 <- loadJuicyPNG "lib/Resources/Menus/credits1.png"
    Just cr2 <- loadJuicyPNG "lib/Resources/Menus/credits2.png"
    Just se1 <- loadJuicyPNG "lib/Resources/Menus/selMap1.png"
    Just se2 <- loadJuicyPNG "lib/Resources/Menus/selMap2.png"
    Just se3 <- loadJuicyPNG "lib/Resources/Menus/selMap3.png"
    Just se4 <- loadJuicyPNG "lib/Resources/Menus/selMap4.png"
    Just pm1 <- loadJuicyPNG "lib/Resources/Menus/pauseM1.png"
    Just pm2 <- loadJuicyPNG "lib/Resources/Menus/pauseM2.png"
    Just pm3 <- loadJuicyPNG "lib/Resources/Menus/pauseM3.png"
    Just pm4 <- loadJuicyPNG "lib/Resources/Menus/pauseM4.png"
    Just mw1 <- loadJuicyPNG "lib/Resources/Menus/vencer1.png"
    Just mw2 <- loadJuicyPNG "lib/Resources/Menus/vencer2.png"
    Just mw3 <- loadJuicyPNG "lib/Resources/Menus/vencer3.png"
    Just mw4 <- loadJuicyPNG "lib/Resources/Menus/vencer4.png"
    Just mw5 <- loadJuicyPNG "lib/Resources/Menus/vencer5.png"
    Just ml1 <- loadJuicyPNG "lib/Resources/Menus/perder1.png"
    Just ml2 <- loadJuicyPNG "lib/Resources/Menus/perder2.png"
    Just ml3 <- loadJuicyPNG "lib/Resources/Menus/perder3.png"
    Just ml4 <- loadJuicyPNG "lib/Resources/Menus/perder4.png"
    Just ml5 <- loadJuicyPNG "lib/Resources/Menus/perder5.png"
    Just ss1 <- loadJuicyPNG "lib/Resources/Menus/sure1.png"
    Just ss2 <- loadJuicyPNG "lib/Resources/Menus/sure2.png"
    Just ss3 <- loadJuicyPNG "lib/Resources/Menus/sure3.png"
    Just pk1 <- loadJuicyPNG "lib/Resources/Menus/pkplus1.png"
    Just pk2 <- loadJuicyPNG "lib/Resources/Menus/pkplus2.png"
    Just pk3 <- loadJuicyPNG "lib/Resources/Menus/pkplus3.png"
    Just pp1 <- loadJuicyPNG "lib/Resources/Menus/pause1.png"
    Just pp2 <- loadJuicyPNG "lib/Resources/Menus/pause2.png"
    Just pp3 <- loadJuicyPNG "lib/Resources/Menus/pause3.png"
    Just pp4 <- loadJuicyPNG "lib/Resources/Menus/pause4.png"
    Just mp1 <- loadJuicyPNG "lib/Resources/Menus/win1.png"
    Just mp2 <- loadJuicyPNG "lib/Resources/Menus/win2.png"
    Just mp3 <- loadJuicyPNG "lib/Resources/Menus/win3.png"
    Just ll1 <- loadJuicyPNG "lib/Resources/Menus/lose1.png"
    Just ll2 <- loadJuicyPNG "lib/Resources/Menus/lose2.png"
    Just ll3 <- loadJuicyPNG "lib/Resources/Menus/lose3.png"
    return [(MenuInicial, [mm1, mm2, mm3, mm4, mm5]),(MCreditos, [cr1, cr2]),(SelecMapa, [se1, se2, se3, se4]),(PauseM, [pm1, pm2, pm3, pm4]),(WinM, [mw1, mw2, mw3, mw4, mw5]),(LoseM, [ml1, ml2, ml3, ml4, ml5]),(Sure, [ss1, ss2, ss3]),(MenuPkp, [pk1, pk2, pk3]),(PauseP, [pp1, pp2, pp3, pp4]),(WinP, [mp1, mp2, mp3]),(LoseP, [ll1, ll2, ll3])]

{-| 
Function used to load images of the game's collectibles
-}
gIColecionavel :: IO ImagensColecionaveis -- ^ Images of the game's collectibles
gIColecionavel = do
    Just iestrela <- loadJuicyPNG "lib/Resources/Collectibles/estrela.png"
    Just imoeda   <- loadJuicyPNG "lib/Resources/Collectibles/moeda.png"
    Just iespada  <- loadJuicyPNG "lib/Resources/Collectibles/espadao.png"
    Just pestrela <- loadJuicyPNG "lib/Resources/Collectibles/estrelau.png"
    Just pmoeda   <- loadJuicyPNG "lib/Resources/Collectibles/moedau.png"
    Just pespada  <- loadJuicyPNG "lib/Resources/Collectibles/espadaou.png"
    return [(Classico, [(Estrela, iestrela),(Moeda, imoeda),(Martelo, iespada)]),(PlusK, [(Estrela, pestrela),(Moeda, pmoeda),(Martelo, pespada)])]

{-| 
Function used to load the icons used during the game
-}
gIIcones :: IO ImagensIcones -- ^ Images of the game icons
gIIcones = do
    Just imoeda   <- loadJuicyPNG "lib/Resources/Icons/imoeda.png"
    Just imespada <- loadJuicyPNG "lib/Resources/Icons/iespada.png"
    Just c1       <- loadJuicyPNG "lib/Resources/Icons/coracao1.png"
    Just c2       <- loadJuicyPNG "lib/Resources/Icons/coracao2.png"
    Just c3       <- loadJuicyPNG "lib/Resources/Icons/coracao3.png"
    Just es1      <- loadJuicyPNG "lib/Resources/Icons/estrela1.png"
    Just es2      <- loadJuicyPNG "lib/Resources/Icons/estrela2.png"
    return [(MoedaP, [imoeda]),(TimerM, [imespada]),(VidaI, [c1, c2, c3]),(Estrelinha, [es1, es2])]                   

{-| 
Function that returns the initial state of the game, i.e. when you open it
-}                                                         
estadoInicial :: IO PrimateKong -- ^ Main game data
estadoInicial = do
    imagensBloco         <- gIBlocos
    imagensEntidades     <- gIPersonagens
    imagensMenus         <- gIMenu
    imagensColecionaveis <- gIColecionavel
    imagensIcones        <- gIIcones
    seed                 <- gerarSeed
    let jogador = Personagem (0, 0) Jogador (-352.0,-448.0) Este (32, 32) False False 9 0 (False, 0)
        inimigos = [Personagem (0, 0) Fantasma (96.0,224.0) Este (32, 32) False True 1 0 (False, 0), Personagem (0, 0) Fantasma (0.0,-160.0) Este (32, 32) False True 1 0 (False, 0), Personagem (0, 0) Fantasma (128.0,-352.0) Oeste (32, 32) False True 1 0 (False, 0), Personagem (0, 0) Fantasma (288.0,-448.0) Oeste (32, 32) False True 1 0 (False, 0), Personagem (0, 0) MacacoMalvado (-320.0,348.0) Este (128, 128) False True 1 0 (False, 0)]
        colecionaveis = [(Martelo, (-256.0,160.0)),(Martelo, (320.0,-64.0)),(Moeda, (288.0,352.0)),(Moeda, (-32.0,32.0)),(Moeda, (-256.0,-224.0)),(Moeda, (352.0,-416.0)),(Estrela, (-128.0,416.0))]
        mapa = Mapa ((0.0,-448.0),Este) (-128.0,416.0) mapa1b
        jogo = Jogo mapa inimigos colecionaveis jogador
        tempo = 0
        press = Tecpress False Nothing False False False
        paridade = (False,False)
        escolham = Nenhum
    return $ PrimateKong jogo MenuInicial Limpo (Imagens imagensBloco imagensEntidades imagensMenus imagensColecionaveis imagensIcones) Classico seed tempo press paridade escolham

{-| 
Function responsible for dictating the size of the game window as well as its name
-}
window :: Display -- ^ Game Window
window = InWindow "PrimateKong" (800,992) (0,0)

{-| 
Function that sets the colour of the game background
-}
backgroundC :: Color -- ^ Background color
backgroundC = makeColor (37/255) (8/255) (37/255) 1.0

{-| 
Function responsible for defining the @frames per second@ of the game 
-}
fr :: Int -- ^ Value of @frames per second@ in the game
fr = 30

{-| 
Function responsible for designing the different game states
-}
desenhaEstado :: PrimateKong -- ^ Main game data
              -> Picture -- Final image, with all the game's graphics
desenhaEstado (PrimateKong j@(Jogo mapa@(Mapa (pos, dir) pos2 bloco) ini col jog) menu opcoes (Imagens imagensBlocos imagensPersonagens imagensMenus imagensColecionaveis imagensIcones) tema seed tempo press par es) =
    case menu of
         MenuInicial -> desenhaMenus menu opcoes imagensMenus jog tempo imagensIcones
         MCreditos -> desenhaMenus menu opcoes imagensMenus jog tempo imagensIcones
         SelecMapa -> desenhaMenus menu opcoes imagensMenus jog tempo imagensIcones
         PauseM -> desenhaMenus menu opcoes imagensMenus jog tempo imagensIcones
         WinM -> desenhaMenus menu opcoes imagensMenus jog tempo imagensIcones
         LoseM -> desenhaMenus menu opcoes imagensMenus jog tempo imagensIcones
         Sure -> desenhaMenus menu opcoes imagensMenus jog tempo imagensIcones
         MenuPkp -> desenhaMenus menu opcoes imagensMenus jog tempo imagensIcones
         PauseP -> desenhaMenus menu opcoes imagensMenus jog tempo imagensIcones
         WinP -> desenhaMenus menu opcoes imagensMenus jog tempo imagensIcones
         LoseP -> desenhaMenus menu opcoes imagensMenus jog tempo imagensIcones
         EmJogo1 -> pictures $ concat [desenhaMapa (-384) (480) bloco tema imagensBlocos, desenhaEntidadeHelp1 ini jog imagensPersonagens press par tema, desenhaColecionavelHelp col imagensColecionaveis menu, desenhaIcon imagensIcones jog tempo]  
         EmJogo2 -> pictures $ concat [desenhaMapa (-384) (480) bloco tema imagensBlocos, desenhaEntidadeHelp1 ini jog imagensPersonagens press par tema, desenhaColecionavelHelp col imagensColecionaveis menu, desenhaIcon imagensIcones jog tempo]  

{-| 
Function that draws some icons in the game under specific conditions
-}
desenhaIcon :: ImagensIcones -- ^ Icon images
            -> Personagem -- ^ Player character
            -> Tempo -- ^ Time in stage
            -> Pictures -- ^ Images with icons
desenhaIcon i (Personagem i1 i2 i3 i4 i5 i6 i7 l i9 (v,tm)) t =
           case (lookup MoedaP i, lookup TimerM i, lookup VidaI i) of 
            (Just mimage, Just tmimage, Just cimage) -> 
                  case l `div` 3 of 
                      3 -> case v of 
                             True -> [translate (-320.0) 416.0 (tmimage !! 0), translate (-320.0) 448.0 (cimage !! 2), color black $ translate (-308) 412 $ scale 0.1 0.1 $ text (show (round tm))]
                             False -> [translate (-320.0) 448.0 (cimage !! 2)]
                      2 -> case v of 
                             True -> [translate (-320.0) 416.0 (tmimage !! 0), translate (-320.0) 448.0 (cimage !! 1), color black $ translate (-308) 412 $ scale 0.1 0.1 $ text (show (round tm))]
                             False -> [translate (-320.0) 448.0 (cimage !! 1)]
                      1 -> case v of 
                             True -> [translate (-320.0) 416.0 (tmimage !! 0), translate (-320.0) 448.0 (cimage !! 0), color black $ translate (-308) 412 $ scale 0.1 0.1 $ text (show (round tm))]
                             False -> [translate (-320.0) 448.0 (cimage !! 0)]
                      _ -> []
            _ -> []

{-| 
Function responsible for designing all the game's menus, depending on your options
-}
desenhaMenus :: Menu -- ^ Current game menu
             -> Opcoes -- ^ Current game option
             -> ImagensMenus -- ^ Menu images
             -> Personagem -- ^ Player character
             -> Tempo -- ^ Time in stage
             -> ImagensIcones -- ^ Icon images
             -> Picture -- ^ Image composed of the momentarily selected menu
desenhaMenus m o i j t ii = case (lookup MenuInicial i, lookup MCreditos i, lookup SelecMapa i, lookup PauseM i, lookup WinM i, lookup LoseM i, lookup Sure i, lookup MenuPkp i, lookup PauseP i, lookup WinP i, lookup LoseP i) of
                            (Just miimages, Just cimages, Just seimages, Just pmimages, Just wimages, Just limages, Just simages, Just pkimages, Just paimages, Just wwimages, Just llimages) ->
                             case m of
                              MenuInicial -> case o of
                                              Limpo    -> translate 0 0 (miimages !! 0)
                                              Jogar    -> translate 0 0 (miimages !! 1)
                                              Sair     -> translate 0 0 (miimages !! 3)
                                              Creditos -> translate 0 0 (miimages !! 2)
                                              Plus     -> translate 0 0 (miimages !! 4)
                                              _        -> Blank
                              MCreditos -> case o of
                                              Limpo    -> translate 0 0 (cimages !! 0)
                                              Retornar -> translate 0 0 (cimages !! 1)
                                              _        -> Blank
                              SelecMapa -> case o of
                                              Limpo    -> translate 0 0 (seimages !! 0)
                                              Mapa1    -> translate 0 0 (seimages !! 1)
                                              Mapa2    -> translate 0 0 (seimages !! 2)
                                              Retornar -> translate 0 0 (seimages !! 3)
                                              _        -> Blank
                              PauseM    -> case o of
                                              Limpo    -> translate 0 0 (pmimages !! 0)
                                              Retomar  -> translate 0 0 (pmimages !! 1)
                                              Retornar -> translate 0 0 (pmimages !! 2)
                                              Sair     -> translate 0 0 (pmimages !! 3)
                                              _        -> Blank
                              WinM      -> case o of
                                              Limpo    -> pictures [translate 0 0 (wimages !! 0), color yellow $ translate (-140) 300 $ scale 0.8 0.8 $ text ("Points:"), color yellow $ translate (-90) 190 $ scale 0.8 0.8 $ text (show (i9 + 500)), color yellow $ translate (-280) (-400) $ scale 0.6 0.6 $ text ("Time: " ++ show min ++ ":" ++ show seg ++ " min"), esH (segToMin t) ii]
                                              Jogar    -> pictures [translate 0 0 (wimages !! 1), color yellow $ translate (-140) 300 $ scale 0.8 0.8 $ text ("Points:"), color yellow $ translate (-90) 190 $ scale 0.8 0.8 $ text (show (i9 + 500)), color yellow $ translate (-280) (-400) $ scale 0.6 0.6 $ text ("Time: " ++ show min ++ ":" ++ show seg ++ " min"), esH (segToMin t) ii]
                                              Retornar -> pictures [translate 0 0 (wimages !! 2), color yellow $ translate (-140) 300 $ scale 0.8 0.8 $ text ("Points:"), color yellow $ translate (-90) 190 $ scale 0.8 0.8 $ text (show (i9 + 500)), color yellow $ translate (-280) (-400) $ scale 0.6 0.6 $ text ("Time: " ++ show min ++ ":" ++ show seg ++ " min"), esH (segToMin t) ii]
                                              SelMap   -> pictures [translate 0 0 (wimages !! 3), color yellow $ translate (-140) 300 $ scale 0.8 0.8 $ text ("Points:"), color yellow $ translate (-90) 190 $ scale 0.8 0.8 $ text (show (i9 + 500)), color yellow $ translate (-280) (-400) $ scale 0.6 0.6 $ text ("Time: " ++ show min ++ ":" ++ show seg ++ " min"), esH (segToMin t) ii]
                                              NextMap  -> pictures [translate 0 0 (wimages !! 4), color yellow $ translate (-140) 300 $ scale 0.8 0.8 $ text ("Points:"), color yellow $ translate (-90) 190 $ scale 0.8 0.8 $ text (show (i9 + 500)), color yellow $ translate (-280) (-400) $ scale 0.6 0.6 $ text ("Time: " ++ show min ++ ":" ++ show seg ++ " min"), esH (segToMin t) ii]
                                              _        -> Blank
                              LoseM     -> case o of
                                              Limpo    -> translate 0 0 (limages !! 0)
                                              Jogar    -> translate 0 0 (limages !! 1)
                                              Retornar -> translate 0 0 (limages !! 2)
                                              SelMap   -> translate 0 0 (limages !! 3)
                                              NextMap  -> translate 0 0 (limages !! 4)
                                              _        -> Blank
                              Sure      -> case o of 
                                              Limpo    -> translate 0 0 (simages !! 0)
                                              Jogar    -> translate 0 0 (simages !! 1)
                                              Retornar -> translate 0 0 (simages !! 2)
                                              _        -> Blank
                              MenuPkp   -> case o of 
                                              Limpo    -> translate 0 0 (pkimages !! 0)
                                              Jogar    -> translate 0 0 (pkimages !! 1)
                                              Retornar -> translate 0 0 (pkimages !! 2)
                                              _        -> Blank
                              PauseP    -> case o of 
                                              Limpo    -> translate 0 0 (paimages !! 0)
                                              Retomar  -> translate 0 0 (paimages !! 1)
                                              Retornar -> translate 0 0 (paimages !! 2)
                                              Sair     -> translate 0 0 (paimages !! 3)
                                              _        -> Blank
                              WinP     -> case o of 
                                              Limpo    -> pictures $ [translate 0 0 (wwimages !! 0), color yellow $ translate (-140) 300 $ scale 0.8 0.8 $ text ("Points:"), color yellow $ translate (-90) 190 $ scale 0.8 0.8 $ text (show (i9 + 500)), color yellow $ translate (-280) (-400) $ scale 0.6 0.6 $ text ("Time: " ++ show min ++ ":" ++ show seg ++ " min"), esH (segToMin t) ii]
                                              Jogar    -> pictures $ [translate 0 0 (wwimages !! 1), color yellow $ translate (-140) 300 $ scale 0.8 0.8 $ text ("Points:"), color yellow $ translate (-90) 190 $ scale 0.8 0.8 $ text (show (i9 + 500)), color yellow $ translate (-280) (-400) $ scale 0.6 0.6 $ text ("Time: " ++ show min ++ ":" ++ show seg ++ " min"), esH (segToMin t) ii]
                                              Retornar -> pictures $ [translate 0 0 (wwimages !! 2), color yellow $ translate (-140) 300 $ scale 0.8 0.8 $ text ("Points:"), color yellow $ translate (-90) 190 $ scale 0.8 0.8 $ text (show (i9 + 500)), color yellow $ translate (-280) (-400) $ scale 0.6 0.6 $ text ("Time: " ++ show min ++ ":" ++ show seg ++ " min"), esH (segToMin t) ii]
                                              _        -> Blank
                              LoseP    -> case o of
                                              Limpo    -> translate 0 0 (llimages !! 0)
                                              Jogar    -> translate 0 0 (llimages !! 1)
                                              Retornar -> translate 0 0 (llimages !! 2)
                                              _        -> Blank
                          where 
                           (Personagem i1 i2 i3 i4 i5 i6 i7 i8 i9 i10) = j
                           (min,seg) = segToMin t
                           esH :: MinSeg -> ImagensIcones -> Picture
                           esH t i = case estrelinhas t of 
                                      1 -> pictures [scale 3 3 $ translate (-40) (-85) (eimages !! 0), scale 3 3 $ translate 0 (-85) (eimages !! 1), scale 3 3 $ translate 40 (-85) (eimages !! 1)] 
                                      2 -> pictures [scale 3 3 $ translate (-40) (-85) (eimages !! 0), scale 3 3 $ translate 0 (-85) (eimages !! 0), scale 3 3 $ translate 40 (-85) (eimages !! 1)] 
                                      3 -> pictures [scale 3 3 $ translate (-40) (-85) (eimages !! 0), scale 3 3 $ translate 0 (-85) (eimages !! 0), scale 3 3 $ translate 40 (-85) (eimages !! 0)] 
                                where 
                                  (Just eimages) = lookup Estrelinha i 

{-| 
Function responsible for drawing the map in general
-}
desenhaMapa :: Float -- ^ Value in x where you start drawing the map
            -> Float -- ^ Value in y where you start drawing the map
            -> [[Bloco]] -- ^ List of map block lists
            -> Temas -- ^ General themes defined
            -> ImagensBlocos -- ^ Images of the map blocks
            -> Pictures -- ^ Map images
desenhaMapa x y mapa tm i = concatMap (\(l, y') -> desenhaLinha x y' l tm i) lC
  where
    lC = zip mapa [y, y - evertical ..]
    evertical = 32.0

{-| 
Function responsible for drawing the lines on the map
-}
desenhaLinha :: Float -- ^ Value in x where the map line starts to be drawn
             -> Float -- ^ Value in y where the map line starts to be drawn
             -> [Bloco] -- ^ List of map blocks
             -> Temas -- ^ General themes defined
             -> ImagensBlocos -- ^ Images of the map blocks
             -> Pictures -- ^ Images of the map lines
desenhaLinha x y li tm i = concatMap (\(b, x') -> desenhaBloco x' y b tm i) bC
  where
    ehorizontal = 32.0
    bC = zip li [x, x + ehorizontal ..]

{-| 
Function responsible for drawing the map blocks
-}
desenhaBloco :: Float -- ^ Value in x where the map block begins to be drawn
             -> Float -- ^ Value in y where the map block begins to be drawn
             -> Bloco -- ^ Map block in question
             -> Temas -- ^ General themes defined
             -> ImagensBlocos -- ^ Images of the map blocks
             -> Pictures -- ^ Images of the designed blocks
desenhaBloco x y bloco tm i = case lookup tm i of
  Just tem ->
    case lookup bloco tem of
      Just picture -> [Translate x y (Scale 1 1 picture)]
      Nothing      -> []
  Nothing -> []

{-| 
Function responsible for helping to design all the entities in the game
-}
desenhaEntidadeHelp1 :: [Personagem] -- ^ Enemies list
                     -> Personagem -- ^ Player character
                     -> ImagensPersonagens -- ^ Images of the game's characters
                     -> Tecpress -- ^ Pressing a key
                     -> Paridade -- ^ Stage time parity value
                     -> Temas -- ^ Different game themes
                     -> Pictures -- ^ Images of all the entities in the game
desenhaEntidadeHelp1 e p i t pa tem = desenhaEntidadeHelp2 (e ++ [p]) i t pa tem

{-| 
Function responsible for helping to design all the entities in the game
-}
desenhaEntidadeHelp2 :: [Personagem] -- ^ List of all entities
                     -> ImagensPersonagens -- ^ Images of the game's characters
                     -> Tecpress -- ^ Pressing a key
                     -> Paridade -- ^ Stage time parity value
                     -> Temas -- ^ Different game themes
                     -> Pictures -- ^ Images of all the entities in the game
desenhaEntidadeHelp2 [] _ _ _ _ = []
desenhaEntidadeHelp2 (x:xs) i t p tem = desenhaEntidade x i t p tem : desenhaEntidadeHelp2 xs i t p tem

{-| 
Function responsible for designing one game entity at a time
-}
desenhaEntidade :: Personagem -- ^ Character from the game in question
                -> ImagensPersonagens -- ^ Images of the game's characters
                -> Tecpress -- ^ Pressing a key
                -> Paridade -- ^ Stage time parity value
                -> Temas -- ^ Different game themes
                -> Picture -- ^ Image of the designed entity
desenhaEntidade p@(Personagem _ i2 (x, y) i4 _ i6 _ _ _ i10) iL tec par tem =
    case tem of  
      Classico -> 
        case (lookup Classico iL) of
          (Just lI) -> case (lookup Jogador lI, lookup Fantasma lI, lookup MacacoMalvado lI) of
                        (Just plimages, Just fimages, Just mimages) ->
                            case i2 of
                                Jogador ->
                                    case i10 of
                                        (True, _) ->
                                            case pressao tec of
                                                False ->
                                                    case i6 of
                                                        True  -> scale 1 1 (translate (realToFrac x) (realToFrac y) (plimages !! 3))
                                                        False -> case hsalt tec of
                                                                   True -> case i4 of
                                                                            Este -> scale 1 1 (translate (realToFrac x) (realToFrac y) (plimages !! 5))
                                                                            Oeste -> scale 1 1 (translate (realToFrac x) (realToFrac y) (plimages !! 7))
                                                                   False -> case i4 of
                                                                             Este  -> scale 1 1 (translate (realToFrac x) (realToFrac y) (plimages !! 4))
                                                                             Oeste -> scale 1 1 (translate (realToFrac x) (realToFrac y) (plimages !! 11))
                                                True ->
                                                  case i6 of
                                                      True  -> scale 1 1 (translate (realToFrac x) (realToFrac y) (plimages !! 3))
                                                      False -> case i4 of
                                                                Este -> scale 1 1 (translate (realToFrac x) (realToFrac y) (plimages !! 10))
                                                                Oeste -> scale 1 1 (translate (realToFrac x) (realToFrac y) (plimages !! 6))
                                        (False, _) ->
                                            case pressao tec of
                                                False ->
                                                    case i6 of
                                                        True  -> scale 1 1 (translate (realToFrac x) (realToFrac y) (plimages !! 3))
                                                        False -> case hsalt tec of
                                                                   True -> case i4 of
                                                                            Este -> scale 1 1 (translate (realToFrac x) (realToFrac y) (plimages !! 2))
                                                                            Oeste -> scale 1 1 (translate (realToFrac x) (realToFrac y) (plimages !! 12))
                                                                   False -> case i4 of
                                                                             Este  -> scale 1 1 (translate (realToFrac x) (realToFrac y) (plimages !! 1))
                                                                             Oeste -> scale 1 1 (translate (realToFrac x) (realToFrac y) (plimages !! 9))
                                                True ->
                                                  case i6 of
                                                      True  -> scale 1 1 (translate (realToFrac x) (realToFrac y) (plimages !! 3))
                                                      False -> case i4 of
                                                                Este -> scale 1 1 (translate (realToFrac x) (realToFrac y) (plimages !! 0))
                                                                Oeste -> scale 1 1 (translate (realToFrac x) (realToFrac y) (plimages !! 8))
                                Fantasma ->
                                    case i4 of
                                        Este -> scale 1 1 (translate (realToFrac x) (realToFrac y) (fimages !! 1))
                                        Oeste -> scale 1 1 (translate (realToFrac x) (realToFrac y) (fimages !! 0))
                                        _ -> Blank
                                MacacoMalvado ->
                                    case i4 of 
                                      Oeste -> case par of
                                                (True,False) -> translate (realToFrac x) (realToFrac y) $ scale 3 3 (mimages !! 0)
                                                (False,True) -> translate (realToFrac x) (realToFrac y) $ scale 3 3 (mimages !! 1)
                                                _            -> Blank
                                      Este -> case par of 
                                               (True,False) -> translate (realToFrac x) (realToFrac y) $ scale 3 3 (mimages !! 2)
                                               (False,True) -> translate (realToFrac x) (realToFrac y) $ scale 3 3 (mimages !! 3)
                                               _            -> Blank
                                      _ -> Blank   
                        _ -> Blank
      PlusK ->
        case (lookup PlusK iL) of
          (Just lI) ->  case (lookup Jogador lI, lookup Fantasma lI, lookup MacacoMalvado lI) of
                        (Just plimages, Just fimages, Just mimages) ->
                            case i2 of
                                Jogador ->
                                    case i10 of
                                        (True, _) ->
                                            case pressao tec of
                                                False ->
                                                    case i6 of
                                                        True  -> scale 1 1 (translate (realToFrac x) (realToFrac y) (plimages !! 9))
                                                        False -> case i4 of
                                                                    Este  -> scale 1 1 (translate (realToFrac x) (realToFrac y) (plimages !! 7))
                                                                    Oeste -> scale 1 1 (translate (realToFrac x) (realToFrac y) (plimages !! 8))
                                                True ->
                                                  case i6 of
                                                      True  -> scale 1 1 (translate (realToFrac x) (realToFrac y) (plimages !! 9))
                                                      False -> case i4 of
                                                                Este -> scale 1 1 (translate (realToFrac x) (realToFrac y) (plimages !! 5))
                                                                Oeste -> scale 1 1 (translate (realToFrac x) (realToFrac y) (plimages !! 6))
                                        (False, _) ->
                                            case pressao tec of
                                                False ->
                                                    case i6 of
                                                        True  -> scale 1 1 (translate (realToFrac x) (realToFrac y) (plimages !! 4))
                                                        False -> case i4 of 
                                                                      Este -> scale 1 1 (translate (realToFrac x) (realToFrac y) (plimages !! 2))
                                                                      Oeste -> scale 1 1 (translate (realToFrac x) (realToFrac y) (plimages !! 3))
                                                True ->
                                                  case i6 of
                                                      True  -> scale 1 1 (translate (realToFrac x) (realToFrac y) (plimages !! 4))
                                                      False -> case i4 of
                                                                Este -> scale 1 1 (translate (realToFrac x) (realToFrac y) (plimages !! 0))
                                                                Oeste -> scale 1 1 (translate (realToFrac x) (realToFrac y) (plimages !! 1))
                                Fantasma ->
                                    case i6 of 
                                        False -> case i4 of
                                                   Este -> scale 1 1 (translate (realToFrac x) (realToFrac y) (fimages !! 1))
                                                   Oeste -> scale 1 1 (translate (realToFrac x) (realToFrac y) (fimages !! 0))
                                                   _ -> Blank
                                        True -> scale 1 1 (translate (realToFrac x) (realToFrac y) (fimages !! 2))
                                MacacoMalvado ->
                                    case i4 of 
                                      Oeste -> case par of
                                                (True,False) -> translate (realToFrac x) (realToFrac y) $ scale 3 3 (mimages !! 0)
                                                (False,True) -> translate (realToFrac x) (realToFrac y) $ scale 3 3 (mimages !! 1)
                                                _            -> Blank
                                      Este -> case par of 
                                               (True,False) -> translate (realToFrac x) (realToFrac y) $ scale 3 3 (mimages !! 2)
                                               (False,True) -> translate (realToFrac x) (realToFrac y) $ scale 3 3 (mimages !! 3)
                                               _            -> Blank
                                      _ -> Blank   
                        _ -> Blank

{-| 
Function responsible for helping design the game's collectibles
-}
desenhaColecionavelHelp :: [(Colecionavel, Posicao)] -- ^ List of collectibles and their positions in the game
                        -> ImagensColecionaveis -- ^ Images of in-game collectibles
                        -> Menu -- ^ Current Menu
                        -> Pictures -- ^ Images of the game's collectibles
desenhaColecionavelHelp [] _ _  = []
desenhaColecionavelHelp ((x, pos):xs) iC m  = desenhaColecionavel (x, pos) iC m : desenhaColecionavelHelp xs iC m

{-| 
Function responsible for designing the game's collectibles
-}
desenhaColecionavel :: (Colecionavel, Posicao) -- ^ Collectible in question and its position
                    -> ImagensColecionaveis -- ^ Images of in-game collectibles
                    -> Menu -- ^ Current Menu
                    -> Picture -- ^ Image with the designed collectible
desenhaColecionavel (t,(x,y)) iC men =
    case men of 
        EmJogo1 -> case lookup Classico iC of
                         (Just e) -> case lookup t e of 
                                             (Just p) -> case t of 
                                                          Estrela -> translate (realToFrac x) (realToFrac y) p
                                                          Moeda   -> translate (realToFrac x) (realToFrac y) p
                                                          Martelo -> translate (realToFrac x) (realToFrac y) p
                                             _        -> Blank
                         _            -> Blank
        EmJogo2 -> case lookup PlusK iC of
                         (Just e) -> case lookup t e of 
                                             (Just p) -> case t of 
                                                          Estrela -> translate (realToFrac x) (realToFrac y) p
                                                          Moeda   -> translate (realToFrac x) (realToFrac y) p
                                                          Martelo -> translate (realToFrac x) (realToFrac y) p
                                             _        -> Blank
                         _            -> Blank
        _       -> Blank

{-| 
Function responsible for linking the player to the game, allowing it to be controlled via keyboard keys
-}
reageEvento :: Event -- ^ Key pressed
            -> PrimateKong -- ^ Compact game data
            -> PrimateKong -- ^ Compact game data updated after player interaction
reageEvento e pk@(PrimateKong j@(Jogo m@(Mapa m1 m2 m3) i c per) me op im te se t pr par es) =
    case me of
        EmJogo1 -> case e of
                    EventKey (SpecialKey KeySpace) Down _ _ -> pk { menu = PauseM , opcao = Limpo}
                    EventKey (Char 'w') Down _ _ ->
                        if subVazio m3 per
                        then pk { tecPress = pr { pressao = True , acao = (Just Subir) , hsalt = False } }
                        else  if jumpNAssist m per
                              then pk { tecPress = pr { pressao = False , acao = (Just Saltar) , hsalt = True } }
                              else pk
                    EventKey (Char 'w') Up _ _ ->
                        pk { tecPress = pr { pressao = False , acao = (Just Parar) , hsalt = False } }
                    EventKey (Char 's') Down _ _ ->
                        pk { tecPress = pr { pressao = True , acao = (Just Descer) , hsalt = False } }
                    EventKey (Char 's') Up _ _ ->
                        pk { tecPress = pr { pressao = False , acao = (Just Parar) , hsalt = False } }
                    EventKey (Char 'd') Down _ _ ->
                        pk { tecPress = pr { pressao = True , acao = (Just AndarDireita) , pressd = True , hsalt = False }  }
                    EventKey (Char 'd') Up _ _ ->
                        pk { tecPress = pr { pressao = False , acao = (Just Parar) , pressd = False , hsalt = False } }
                    EventKey (Char 'a') Down _ _ ->
                        pk { tecPress = pr { pressao = True , acao = (Just AndarEsquerda) , presse = True , hsalt = False } }
                    EventKey (Char 'a') Up _ _ ->
                        pk { tecPress = pr { pressao = False , acao = (Just Parar) , presse = False , hsalt = False } }
                    _ -> pk
        EmJogo2 -> case e of
                    EventKey (SpecialKey KeySpace) Down _ _ -> pk { menu = PauseP , opcao = Limpo}
                    EventKey (Char 'w') Down _ _ ->
                        if subVazio m3 per
                        then pk { tecPress = pr { pressao = True , acao = (Just Subir) , hsalt = False } }
                        else  if jumpNAssist m per
                              then pk { tecPress = pr { pressao = False , acao = (Just Saltar) , hsalt = True } }
                              else pk
                    EventKey (Char 'w') Up _ _ ->
                        pk { tecPress = pr { pressao = False , acao = (Just Parar) , hsalt = False } }
                    EventKey (Char 's') Down _ _ ->
                        pk { tecPress = pr { pressao = True , acao = (Just Descer) , hsalt = False } }
                    EventKey (Char 's') Up _ _ ->
                        pk { tecPress = pr { pressao = False , acao = (Just Parar) , hsalt = False } }
                    EventKey (Char 'd') Down _ _ ->
                        pk { tecPress = pr { pressao = True , acao = (Just AndarDireita) , pressd = True , hsalt = False }  }
                    EventKey (Char 'd') Up _ _ ->
                        pk { tecPress = pr { pressao = False , acao = (Just Parar) , pressd = False , hsalt = False } }
                    EventKey (Char 'a') Down _ _ ->
                        pk { tecPress = pr { pressao = True , acao = (Just AndarEsquerda) , presse = True , hsalt = False } }
                    EventKey (Char 'a') Up _ _ ->
                        pk { tecPress = pr { pressao = False , acao = (Just Parar) , presse = False , hsalt = False } }
                    _ -> pk 
        MenuInicial -> case op of
                         Limpo    -> if pressao pr == True && presse pr == True && pressd pr == True && hsalt pr == True
                                     then pk {opcao = Plus}
                                     else case e of
                                           EventKey (SpecialKey KeyDown) Down _ _ -> pk { opcao = Jogar, tecPress = pr {pressao = False , presse = False, pressd = False , hsalt = False}}
                                           EventKey (SpecialKey KeyUp) Down _ _ -> pk { opcao = Jogar, tecPress = pr {pressao = False , presse = False, pressd = False , hsalt = False}}
                                           EventKey (Char 's') Down _ _ -> pk {tecPress = pr { pressao = True }}
                                           EventKey (Char 't') Down _ _ -> pk {tecPress = pr { presse = True }}
                                           EventKey (Char 'a') Down _ _ -> pk {tecPress = pr { pressd = True }}
                                           EventKey (Char 'r') Down _ _ -> pk {tecPress = pr { hsalt = True }}
                                           _ -> pk
                         Jogar    -> if pressao pr == True && presse pr == True && pressd pr == True && hsalt pr == True
                                     then pk {opcao = Plus}
                                     else case e of
                                           EventKey (SpecialKey KeyDown) Down _ _ -> pk { opcao = Creditos}
                                           EventKey (SpecialKey KeyUp) Down _ _ -> pk { opcao = Sair}
                                           EventKey (SpecialKey KeyEnter) Down _ _ -> pk { menu = SelecMapa , opcao = Limpo, tema = Classico}
                                           EventKey (Char 's') Down _ _ -> pk {tecPress = pr { pressao = True }}
                                           EventKey (Char 't') Down _ _ -> pk {tecPress = pr { presse = True }}
                                           EventKey (Char 'a') Down _ _ -> pk {tecPress = pr { pressd = True }}
                                           EventKey (Char 'r') Down _ _ -> pk {tecPress = pr { hsalt = True }}
                                           _ ->     pk
                         Sair     -> if pressao pr == True && presse pr == True && pressd pr == True && hsalt pr == True
                                     then pk {opcao = Plus}
                                     else case e of
                                           EventKey (SpecialKey KeyUp) Down _ _  -> pk { opcao = Creditos}
                                           EventKey (SpecialKey KeyDown) Down _ _    -> pk { opcao = Jogar}
                                           EventKey (Char 's') Down _ _ -> pk {tecPress = pr { pressao = True }}
                                           EventKey (Char 't') Down _ _ -> pk {tecPress = pr { presse = True }}
                                           EventKey (Char 'a') Down _ _ -> pk {tecPress = pr { pressd = True }}
                                           EventKey (Char 'r') Down _ _ -> pk {tecPress = pr { hsalt = True }}
                                           EventKey (SpecialKey KeyEnter) Down _ _ -> error "Game Closing"
                                           _ -> pk
                         Creditos -> if pressao pr == True && presse pr == True && pressd pr == True && hsalt pr == True
                                     then pk {opcao = Plus}
                                     else case e of
                                           EventKey (SpecialKey KeyUp) Down _ _ -> pk { opcao = Jogar}
                                           EventKey (SpecialKey KeyDown) Down _ _   -> pk { opcao = Sair}
                                           EventKey (Char 's') Down _ _ -> pk {tecPress = pr { pressao = True }}
                                           EventKey (Char 't') Down _ _ -> pk {tecPress = pr { presse = True }}
                                           EventKey (Char 'a') Down _ _ -> pk {tecPress = pr { pressd = True }}
                                           EventKey (Char 'r') Down _ _ -> pk {tecPress = pr { hsalt = True }}
                                           EventKey (SpecialKey KeyEnter) Down _ _ -> pk { menu = MCreditos , opcao = Limpo}
                                           _ -> pk
                         Plus     -> case e of 
                                       EventKey (SpecialKey KeyDown) Down _ _ -> pk { opcao = Limpo, tecPress = pr {pressao = False , presse = False , pressd = False, hsalt = False}}
                                       EventKey (SpecialKey KeyUp) Down _ _ -> pk { opcao = Limpo, tecPress = pr {pressao = False , presse = False , pressd = False, hsalt = False}}
                                       EventKey (SpecialKey KeyEnter) Down _ _ -> pk { menu = Sure , opcao = Limpo}
                                       _ -> pk
                         _        -> pk
        MCreditos -> case op of
                      Limpo    -> case e of
                                    EventKey (SpecialKey KeyDown) Down _ _ -> pk { opcao = Retornar}
                                    EventKey (SpecialKey KeyUp) Down _ _ -> pk { opcao = Retornar}
                                    _ -> pk
                      Retornar -> case e of
                                    EventKey (SpecialKey KeyEnter) Down _ _ -> pk { menu = MenuInicial , opcao = Limpo}
                                    _ -> pk
                      _        -> pk
        SelecMapa -> case op of
                      Limpo -> case e of
                                 EventKey (SpecialKey KeyUp) Down _ _ -> pk { opcao = Mapa1}
                                 EventKey (SpecialKey KeyDown) Down _ _ -> pk { opcao = Mapa1}
                                 _ -> pk
                      Mapa1 -> case e of
                                 EventKey (SpecialKey KeyUp) Down _ _ -> pk { opcao = Retornar}
                                 EventKey (SpecialKey KeyDown) Down _ _ -> pk { opcao = Mapa2}
                                 EventKey (SpecialKey KeyEnter) Down _ _ -> PrimateKong jogo1 EmJogo1 Limpo im Classico se tempo1 prf par PrMapa 
                                 _ -> pk
                      Mapa2 -> case e of 
                                 EventKey (SpecialKey KeyUp) Down _ _ -> pk { opcao = Mapa1}
                                 EventKey (SpecialKey KeyDown) Down _ _ -> pk { opcao = Retornar}
                                 EventKey (SpecialKey KeyEnter) Down _ _ -> PrimateKong jogo2 EmJogo1 Limpo im Classico se tempo1 prf par SnMapa 
                                 _ -> pk
                      Retornar  -> case e of
                                 EventKey (SpecialKey KeyUp) Down _ _ -> pk { opcao = Mapa2}
                                 EventKey (SpecialKey KeyDown) Down _ _ -> pk { opcao = Mapa1}
                                 EventKey (SpecialKey KeyEnter) Down _ _ -> pk { menu = MenuInicial , opcao = Limpo}
                                 _ -> pk
                      _     -> pk
        PauseM -> case op of
                      Limpo    -> case e of
                                 EventKey (SpecialKey KeyUp) Down _ _ -> pk { opcao = Retomar}
                                 EventKey (SpecialKey KeyDown) Down _ _ -> pk { opcao = Retomar}
                                 _ -> pk
                      Retomar  -> case e of
                                   EventKey (SpecialKey KeyUp) Down _ _ -> pk { opcao = Sair}
                                   EventKey (SpecialKey KeyDown) Down _ _ -> pk { opcao = Retornar}
                                   EventKey (SpecialKey KeyEnter) Down _ _ -> pk {menu = EmJogo1 , opcao = Limpo, tema = Classico, tecPress = pr {pressao = False , presse = False , pressd = False, hsalt = False}}
                                   _ -> pk
                      Retornar -> case e of
                                    EventKey (SpecialKey KeyUp) Down _ _ -> pk { opcao = Retomar}
                                    EventKey (SpecialKey KeyDown) Down _ _ -> pk { opcao = Sair}
                                    EventKey (SpecialKey KeyEnter) Down _ _ -> pk {menu = MenuInicial , opcao = Limpo}
                                    _ -> pk
                      Sair     -> case e of
                                    EventKey (SpecialKey KeyUp) Down _ _ -> pk { opcao = Retornar}
                                    EventKey (SpecialKey KeyDown) Down _ _ -> pk { opcao = Retomar}
                                    EventKey (SpecialKey KeyEnter) Down _ _ -> error "Game Closing"
                                    _ -> pk
        WinM -> case op of
                    Limpo    -> case e of
                                  EventKey (SpecialKey KeyUp) Down _ _ -> pk { opcao = Jogar}
                                  EventKey (SpecialKey KeyDown) Down _ _ -> pk { opcao = Jogar}
                                  EventKey (SpecialKey KeyRight) Down _ _ -> pk { opcao = Jogar}
                                  EventKey (SpecialKey KeyLeft) Down _ _ -> pk { opcao = Jogar}
                                  _ -> pk
                    Jogar    -> case es of 
                                  PrMapa -> case e of
                                              EventKey (SpecialKey KeyRight) Down _ _ -> pk { opcao = SelMap}
                                              EventKey (SpecialKey KeyDown) Down _ _ -> pk { opcao = Retornar}
                                              EventKey (SpecialKey KeyEnter) Down _ _ -> PrimateKong jogo1 EmJogo1 Limpo im Classico se tempo1 prf par PrMapa
                                              _ -> pk
                                  SnMapa -> case e of 
                                              EventKey (SpecialKey KeyRight) Down _ _ -> pk { opcao = SelMap}
                                              EventKey (SpecialKey KeyDown) Down _ _ -> pk { opcao = Retornar}
                                              EventKey (SpecialKey KeyEnter) Down _ _ -> PrimateKong jogo2 EmJogo1 Limpo im Classico se tempo1 prf par SnMapa
                                              _ -> pk
                                  _ -> pk
                    Retornar -> case e of
                                  EventKey (SpecialKey KeyUp) Down _ _ -> pk { opcao = Jogar}
                                  EventKey (SpecialKey KeyRight) Down _ _ -> pk { opcao = NextMap}
                                  EventKey (SpecialKey KeyEnter) Down _ _ -> pk { menu = MenuInicial , opcao = Limpo}
                                  _ -> pk
                    SelMap   -> case e of
                                  EventKey (SpecialKey KeyLeft) Down _ _ -> pk { opcao = Jogar}
                                  EventKey (SpecialKey KeyDown) Down _ _ -> pk { opcao = NextMap}
                                  EventKey (SpecialKey KeyEnter) Down _ _ -> pk { menu = SelecMapa , opcao = Limpo, tema = Classico}
                                  _ -> pk
                    NextMap  -> case e of
                                  EventKey (SpecialKey KeyUp) Down _ _ -> pk { opcao = SelMap}
                                  EventKey (SpecialKey KeyLeft) Down _ _ -> pk { opcao = Retornar}
                                  EventKey (SpecialKey KeyEnter) Down _ _ -> PrimateKong jogo2 EmJogo1 Limpo im Classico se tempo1 pr par SnMapa
                                  _ -> pk
        LoseM -> case op of
                    Limpo    -> case e of
                                  EventKey (SpecialKey KeyUp) Down _ _ -> pk { opcao = Jogar}
                                  EventKey (SpecialKey KeyDown) Down _ _ -> pk { opcao = Jogar}
                                  EventKey (SpecialKey KeyRight) Down _ _ -> pk { opcao = Jogar}
                                  EventKey (SpecialKey KeyLeft) Down _ _ -> pk { opcao = Jogar}
                                  _ -> pk
                    Jogar    -> case es of 
                                  PrMapa -> case e of
                                              EventKey (SpecialKey KeyRight) Down _ _ -> pk { opcao = SelMap}
                                              EventKey (SpecialKey KeyDown) Down _ _ -> pk { opcao = Retornar}
                                              EventKey (SpecialKey KeyEnter) Down _ _ -> PrimateKong jogo1 EmJogo1 Limpo im Classico se tempo1 prf par PrMapa
                                              _ -> pk
                                  SnMapa -> case e of 
                                              EventKey (SpecialKey KeyRight) Down _ _ -> pk { opcao = SelMap}
                                              EventKey (SpecialKey KeyDown) Down _ _ -> pk { opcao = Retornar}
                                              EventKey (SpecialKey KeyEnter) Down _ _ -> PrimateKong jogo2 EmJogo1 Limpo im Classico se tempo1 prf par SnMapa
                                              _ -> pk
                                  _ -> pk
                    Retornar -> case e of
                                  EventKey (SpecialKey KeyUp) Down _ _ -> pk { opcao = Jogar}
                                  EventKey (SpecialKey KeyRight) Down _ _ -> pk { opcao = NextMap}
                                  EventKey (SpecialKey KeyEnter) Down _ _ -> pk { menu = MenuInicial , opcao = Limpo}
                                  _ -> pk
                    SelMap   -> case e of
                                  EventKey (SpecialKey KeyLeft) Down _ _ -> pk { opcao = Jogar}
                                  EventKey (SpecialKey KeyDown) Down _ _ -> pk { opcao = NextMap}
                                  EventKey (SpecialKey KeyEnter) Down _ _ -> pk { menu = SelecMapa , opcao = Limpo}
                                  _ -> pk
                    NextMap  -> case e of
                                  EventKey (SpecialKey KeyUp) Down _ _ -> pk { opcao = SelMap}
                                  EventKey (SpecialKey KeyLeft) Down _ _ -> pk { opcao = Retornar}
                                  EventKey (SpecialKey KeyEnter) Down _ _ -> PrimateKong jogo2 EmJogo1 Limpo im Classico se tempo1 pr par SnMapa 
                                  _ -> pk
        Sure -> case op of 
                    Limpo    -> case e of 
                                  EventKey (SpecialKey KeyUp) Down _ _ -> pk { opcao = Jogar}
                                  EventKey (SpecialKey KeyDown) Down _ _ -> pk { opcao = Jogar}
                                  _ -> pk
                    Jogar    -> case e of 
                                  EventKey (SpecialKey KeyUp) Down _ _ -> pk { opcao = Retornar}
                                  EventKey (SpecialKey KeyDown) Down _ _ -> pk { opcao = Retornar}
                                  EventKey (SpecialKey KeyEnter) Down _ _ -> pk { opcao = Limpo, menu = MenuPkp, tema = PlusK}
                                  _ -> pk
                    Retornar -> case e of 
                                  EventKey (SpecialKey KeyUp) Down _ _ -> pk { opcao = Jogar}
                                  EventKey (SpecialKey KeyDown) Down _ _ -> pk { opcao = Jogar}
                                  EventKey (SpecialKey KeyEnter) Down _ _ -> pk { opcao = Limpo, menu = MenuInicial, tema = Classico, tecPress = pr {pressao = False , presse = False , pressd = False, hsalt = False}}
                                  _ -> pk
        MenuPkp -> case op of 
                    Limpo    -> case e of
                                  EventKey (SpecialKey KeyLeft) Down _ _ -> pk { opcao = Jogar}
                                  EventKey (SpecialKey KeyRight) Down _ _ -> pk { opcao = Jogar}
                                  _ -> pk
                    Jogar    -> case e of 
                                  EventKey (SpecialKey KeyRight) Down _ _ -> pk { opcao = Retornar}
                                  EventKey (SpecialKey KeyLeft) Down _ _ -> pk { opcao = Retornar}
                                  EventKey (SpecialKey KeyEnter) Down _ _ -> PrimateKong jogo3 EmJogo2 Limpo im PlusK se tempo1 prf par TrMapa 
                                  _ -> pk
                    Retornar -> case e of
                                  EventKey (SpecialKey KeyLeft) Down _ _ -> pk { opcao = Jogar}
                                  EventKey (SpecialKey KeyRight) Down _ _ -> pk { opcao = Jogar}
                                  EventKey (SpecialKey KeyEnter) Down _ _ -> pk { opcao = Limpo, menu = MenuInicial, tema = Classico} 
                                  _ -> pk
        PauseP -> case op of 
                    Limpo    -> case e of
                                  EventKey (SpecialKey KeyLeft) Down _ _ -> pk { opcao = Retomar}
                                  EventKey (SpecialKey KeyRight) Down _ _ -> pk { opcao = Retomar}
                                  _ -> pk
                    Retomar  -> case e of 
                                  EventKey (SpecialKey KeyRight) Down _ _ -> pk { opcao = Retornar}
                                  EventKey (SpecialKey KeyLeft) Down _ _ -> pk { opcao = Sair}
                                  EventKey (SpecialKey KeyEnter) Down _ _ -> pk { opcao = Limpo, menu = EmJogo2, tema = PlusK, tecPress = pr {pressao = False , presse = False , pressd = False, hsalt = False}} 
                                  _ -> pk
                    Retornar -> case e of
                                  EventKey (SpecialKey KeyRight) Down _ _ -> pk { opcao = Sair}
                                  EventKey (SpecialKey KeyLeft) Down _ _ -> pk { opcao = Retornar}
                                  EventKey (SpecialKey KeyEnter) Down _ _ -> pk { opcao = Limpo, menu = MenuPkp} 
                                  _ -> pk
                    Sair     -> case e of 
                                  EventKey (SpecialKey KeyRight) Down _ _ -> pk { opcao = Retomar}
                                  EventKey (SpecialKey KeyLeft) Down _ _ -> pk { opcao = Retornar}
                                  EventKey (SpecialKey KeyEnter) Down _ _ -> error "Game Closing"
                                  _ -> pk
        WinP  -> case op of
                    Limpo    -> case e of
                                  EventKey (SpecialKey KeyLeft) Down _ _ -> pk { opcao = Jogar}
                                  EventKey (SpecialKey KeyRight) Down _ _ -> pk { opcao = Jogar}
                                  _ -> pk
                    Jogar    -> case e of 
                                  EventKey (SpecialKey KeyRight) Down _ _ -> pk { opcao = Retornar}
                                  EventKey (SpecialKey KeyLeft) Down _ _ -> pk { opcao = Retornar} 
                                  EventKey (SpecialKey KeyEnter) Down _ _ -> PrimateKong jogo3 EmJogo2 Limpo im PlusK se tempo1 prf par TrMapa 
                                  _ -> pk
                    Retornar -> case e of 
                                  EventKey (SpecialKey KeyLeft) Down _ _ -> pk { opcao = Jogar}
                                  EventKey (SpecialKey KeyRight) Down _ _ -> pk { opcao = Jogar} 
                                  EventKey (SpecialKey KeyEnter) Down _ _ -> pk { opcao = Limpo, menu = MenuPkp, tema = PlusK}
                                  _ -> pk
        LoseP -> case op of 
                    Limpo    -> case e of
                                  EventKey (SpecialKey KeyLeft) Down _ _ -> pk { opcao = Jogar}
                                  EventKey (SpecialKey KeyRight) Down _ _ -> pk { opcao = Jogar}
                                  _ -> pk
                    Jogar    -> case e of 
                                  EventKey (SpecialKey KeyLeft) Down _ _ -> pk { opcao = Retornar}
                                  EventKey (SpecialKey KeyRight) Down _ _ -> pk { opcao = Retornar} 
                                  EventKey (SpecialKey KeyEnter) Down _ _ -> PrimateKong jogo3 EmJogo2 Limpo im PlusK se tempo1 prf par TrMapa 
                                  _ -> pk
                    Retornar -> case e of 
                                  EventKey (SpecialKey KeyLeft) Down _ _ -> pk { opcao = Jogar}
                                  EventKey (SpecialKey KeyRight) Down _ _ -> pk { opcao = Jogar} 
                                  EventKey (SpecialKey KeyEnter) Down _ _ -> pk { opcao = Limpo, menu = MenuPkp, tema = PlusK}
                                  _ -> pk

{-| 
Function responsible for updating the status as a function of time
-}
reageTempo :: Float -- ^ Time since the game opened
           -> PrimateKong -- ^ Compact game data
           -> PrimateKong -- ^ Game data updated according to time
reageTempo tem (PrimateKong j@(Jogo mapa ini col p) me op im te se t pr par es) =
    case me of
        EmJogo1 ->
            let tAtua = realToFrac tem + t
                nJ = movimenta' se tAtua (movCon (Jogo mapa ini col p) pr)
                nP = parI (realToFrac tAtua)
                nM = mLW j me
            in  PrimateKong nJ nM op im te se tAtua pr nP es
        EmJogo2 ->
            let tAtua = realToFrac tem + t
                nJ = movimenta' se tAtua (movCon (Jogo mapa ini col p) pr)
                nP = parI (realToFrac tAtua)
                nM = mLW j me 
            in  PrimateKong nJ nM op im te se tAtua pr nP es
        _ -> PrimateKong j me op im te se t pr par es
        where
         mLW :: Jogo -> Menu -> Menu
         mLW j m 
           | recColecE j && m == EmJogo1 = WinM
           | vidaJog j && m == EmJogo1 = LoseM
           | recColecE j && m == EmJogo2 = WinP
           | vidaJog j && m == EmJogo2 = LoseP
           | otherwise = m

{-| 
Time used to restart the counter at each stage
-}
tempo1 = 0

{-| 
Used to reset key presses
-}
prf = (Tecpress False Nothing False False False)

{-| 
Definition of the first player
-}
jogador1 = Personagem (0, 0) Jogador (-352.0,-448.0) Este (32, 32) False False 9 0 (False, 0)
{-| 
Definition of the first enemies list
-}
inimigos1 = [Personagem (0, 0) Fantasma (96.0,224.0) Este (32, 32) False True 1 0 (False, 0), Personagem (0, 0) Fantasma (0.0,-160.0) Este (32, 32) False True 1 0 (False, 0), Personagem (0, 0) Fantasma (128.0,-352.0) Oeste (32, 32) False True 1 0 (False, 0), Personagem (0, 0) Fantasma (288.0,-448.0) Oeste (32, 32) False True 1 0 (False, 0), Personagem (0, 0) MacacoMalvado (-320.0,348.0) Este (128, 128) False True 1 0 (False, 0)]
{-| 
Definition of the first list of collectibles
-}
colecionaveis1 = [(Martelo, (-256.0,160.0)),(Martelo, (320.0,-64.0)),(Moeda, (288.0,352.0)),(Moeda, (-32.0,32.0)),(Moeda, (-256.0,-224.0)),(Moeda, (352.0,-416.0)),(Estrela, (-128.0,416.0))]
{-| 
Definition of the first map
-}
mapa1 = Mapa ((0.0,-448.0),Este) (-128.0,416.0) mapa1b
{-| 
Definition of the first stage
-}
jogo1 = Jogo mapa1 inimigos1 colecionaveis1 jogador1

{-| 
Definition of the second map
-}
mapa2 = Mapa ((0.0,-448.0),Este) (160.0,416.0) mapa2b

{-| 
Definition of the second player
-}
jogador2 = Personagem (0, 0) Jogador (0.0,-448.0) Este (32, 32) False False 9 0 (False, 0)

{-| 
Definition of the second enemies list
-}
inimigos2 = [Personagem (0, 0) Fantasma (-288.0,288.0) Este (32, 32) False True 1 0 (False, 0), Personagem (0, 0) Fantasma (32.0,192.0) Este (32, 32) False True 1 0 (False, 0), Personagem (0, 0) Fantasma (-256.0,96.0) Oeste (32, 32) False True 1 0 (False, 0), Personagem (0, 0) Fantasma (288.0,-224.0) Oeste (32, 32) False True 1 0 (False, 0), Personagem (0, 0) Fantasma (0.0,-320.0) Oeste (32, 32) False True 1 0 (False, 0), Personagem (0,0) MacacoMalvado (288.0,314.0) Oeste (128, 128) False True 1 0 (False, 0)]

{-| 
Definition of the second list of collectibles
-}
colecionaveis2 = [(Martelo, (0.0,32.0)), (Martelo, (-288.0,-224.0)), (Moeda, (-256.0,320.0)) , (Moeda, (-192.0,320.0)), (Moeda, (-320.0,96.0)), (Moeda, (192.0,-96.0)),(Estrela, (160.0,416.0))]

{-| 
Definition of the second stage
-}
jogo2 = Jogo mapa2 inimigos2 colecionaveis2 jogador2

{-| 
Definition of the third map
-}
mapa3 = Mapa ((0.0,-448.0),Este) (160.0,416.0) mapa3b
{-| 
Definition of the third player
-}
jogador3 = Personagem (0, 0) Jogador ((-288.0,-448.0)) Este (32, 32) False False 9 0 (False, 0)
{-| 
Definition of the third enemies list
-}
inimigos3 = [Personagem (0, 0) Fantasma (288.0,-352.0) Este (32, 32) False True 1 0 (False, 0), Personagem (0, 0) Fantasma (160.0,-256.0) Este (32, 32) False True 1 0 (False, 0), Personagem (0, 0) Fantasma (-96.0,-128.0) Oeste (32, 32) False True 1 0 (False, 0), Personagem (0, 0) Fantasma (64.0,-32.0) Oeste (32, 32) False True 1 0 (False, 0), Personagem (0,0) Fantasma (-32.0,192.0) Oeste (32, 32) False True 1 0 (False, 0), Personagem (0,0) Fantasma (224.0,288.0) Oeste (32, 32) False True 1 0 (False, 0), Personagem (0,0) MacacoMalvado (-320.0,314.0) Este (128, 128) False True 1 0 (False, 0)]
{-| 
Definition of the third list of collectibles
-}
colecionaveis3 = [(Martelo, (-320.0,0.0)), (Martelo, (320.0,64.0)), (Moeda, (224.0,-256.0)) , (Moeda, (160.0,-32.0)), (Moeda, (32.0,192.0)), (Moeda, (256.0,320.0)),(Estrela, (-160.0,416.0))]
{-| 
Definition of the third stage
-}
jogo3 = Jogo mapa3 inimigos3 colecionaveis3 jogador3

{-| 
Definition of the first map matrix
-}
mapa1b :: [[Bloco]] -- ^ Map matrix 1
mapa1b = [[Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma],                              
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Barreira,Vazio,Vazio,Vazio,Barreira,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma,Plataforma,Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Escada,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Barreira,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Barreira,Vazio,Plataforma],
          [Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Alcapao,Alcapao,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Escada,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Barreira,Vazio,Vazio,Vazio,Vazio,Vazio,Barreira,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Escada,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Barreira,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Barreira,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Vazio,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Escada,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Barreira,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Barreira,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma,Plataforma,Alcapao,Plataforma,Alcapao,Plataforma,Alcapao,Plataforma,Alcapao,Plataforma,Plataforma,Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Escada,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Escada,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Escada,Vazio,Vazio,Vazio,Vazio,Barreira,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Escada,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma,Plataforma,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Barreira,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Barreira,Vazio,Vazio,Vazio,Escada,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Vazio,Vazio,Vazio,Vazio,Escada,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Escada,Vazio,Vazio,Vazio,Vazio,Escada,Plataforma],
          [Plataforma,Vazio,Vazio,Barreira,Vazio,Vazio,Vazio,Vazio,Vazio,Barreira,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Barreira,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Escada,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Escada,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Barreira,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Barreira,Escada,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Alcapao,Alcapao,Plataforma,Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Escada,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Barreira,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma]]

{-| 
Definition of the second map matrix
-}
mapa2b :: [[Bloco]] -- ^ Map matrix 2
mapa2b = [[Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Alcapao,Plataforma,Alcapao,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Escada,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Escada,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Barreira,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Escada,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Escada,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Barreira,Vazio,Vazio,Vazio,Vazio,Vazio,Barreira,Vazio,Vazio,Vazio,Barreira,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma,Alcapao,Alcapao,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Escada,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Barreira,Vazio,Vazio,Vazio,Vazio,Vazio,Barreira,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Escada,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Barreira,Plataforma],
          [Plataforma,Vazio,Plataforma,Alcapao,Alcapao,Alcapao,Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Escada,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Escada,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Barreira,Vazio,Vazio,Vazio,Barreira,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Barreira,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Escada,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Escada,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Barreira,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Barreira,Escada,Barreira,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Plataforma,Plataforma,Plataforma,Plataforma,Alcapao,Alcapao,Plataforma,Vazio,Vazio,Vazio,Plataforma,Plataforma,Plataforma,Alcapao,Plataforma,Plataforma,Plataforma,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Escada,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Barreira,Vazio,Vazio,Vazio,Vazio,Vazio,Barreira,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Barreira,Vazio,Vazio,Vazio,Vazio,Vazio,Barreira,Barreira,Plataforma],
          [Plataforma,Vazio,Vazio,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Escada,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Escada,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Barreira,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Barreira,Vazio,Vazio,Vazio,Vazio,Barreira,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Escada,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Escada,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Escada,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Escada,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma]]

{-| 
Definition of the third map matrix
-}                                                                             
mapa3b :: [[Bloco]] -- ^ Map matrix 3
mapa3b = [[Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Plataforma,Alcapao,Alcapao,Alcapao,Alcapao,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Escada,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Escada,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Barreira,Vazio,Vazio,Vazio,Vazio,Vazio,Barreira,Vazio,Plataforma],
          [Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma,Plataforma,Alcapao,Alcapao,Alcapao,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Escada,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Escada,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Barreira,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Barreira,Escada,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Escada,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Escada,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Barreira,Vazio,Vazio,Vazio,Barreira,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Escada,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma,Alcapao,Plataforma,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Barreira,Vazio,Vazio,Vazio,Vazio,Vazio,Escada,Vazio,Vazio,Plataforma],
          [Plataforma,Plataforma,Alcapao,Plataforma,Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Alcapao,Vazio,Vazio,Escada,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Escada,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Escada,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Escada,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Barreira,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Barreira,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Alcapao,Alcapao,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Escada,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Escada,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Escada,Barreira,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Barreira,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Escada,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Barreira,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Escada,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Vazio,Plataforma],
          [Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma,Plataforma]
         ]