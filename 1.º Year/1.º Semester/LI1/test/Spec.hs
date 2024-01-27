module Main where

import Test.HUnit

import LI12324

import Task1

import Task2

import Task3

import Task4

test_suite_tarefa_01 :: Test
test_suite_tarefa_01 = test [
    "testscolisoesParede"      ~: testscolisoesParede,
    "testscolisoesPersonagens" ~: testscolisoesPersonagens
    ]

test_suite_tarefa_02 :: Test
test_suite_tarefa_02 = test [ 
    "testsvalida"             ~: testsvalida,
    "testsvalidaChao"         ~: testsvalidaChao,
    "testsvalidaRessalto"     ~: testsvalidaRessalto,
    "testsvalidaChoquePs"     ~: testsvalidaChoquePs,
    "testsvalidaMinInimigos"  ~: testsvalidaMinInimigos,
    "testsvalidaVidaFan"      ~: testsvalidaVidaFan,
    "testsvalidaCheckBloc"    ~: testsvalidaCheckBloc,
    "testsvalidaAlcLargo"     ~: testsvalidaAlcLargo,
    "testsvalidaColEntidades" ~: testsvalidaColEntidades
    ]

test_suite_tarefa_03 :: Test
test_suite_tarefa_03 = test [ 
    "testsmovimenta"            ~: testsmovimenta,
    "testsmovInimigos"          ~: testsmovInimigos,
    "testsmovJogador"           ~: testsmovJogador,
    "testsperderVida"           ~: testsperderVida,
    "testsgamaIni"              ~: testsgamaIni,
    "testsfallVoid"             ~: testsfallVoid,
    "testsdanoDuro"             ~: testsdanoDuro,
    "testsrecColecEFilterColec" ~: testsrecColecEFilterColec,
    "testsmenosAl"              ~: testsmenosAl,
    "testsnoPassPar"            ~: testsnoPassPar
    ]

test_suite_tarefa_04 :: Test
test_suite_tarefa_04 = test [ 
    "testsAtualiza" ~: testsAtualiza,
    "testsAcao"     ~: testsAcao
    ]

-- Definitions of some maps
mapa1 = Mapa ((0.5, 0.5), Este) (1.5, 1.5) [[Vazio, Vazio, Vazio], [Vazio, Vazio, Vazio], [Vazio, Vazio, Vazio]]
mapa2 = Mapa ((0.5, 0.5), Este) (1.5, 1.5) [[Vazio, Vazio, Vazio], [Vazio, Plataforma, Vazio], [Vazio, Vazio, Vazio]]

-- Definitions of some Entities
entidade1 = Personagem (1, 0) Jogador (1.5, 1.5) Este (1.0, 1.0) False False 3 0 (False, 0)
entidade2 = Personagem (1, 0) Jogador (2.5, 2.5) Este (1.5, 1.5) False False 3 0 (False, 0)
entidade3 = Personagem (1, 0) Fantasma (0.5, 0.5) Este (1.0, 1.0) False False 3 0 (False, 0)

testscolisoesParede :: Test
testscolisoesParede = test [
    "testColisaoPlataforma1"  ~: True  ~=? colisoesParede mapa2 entidade1, -- Collision with Platforms 
    "testColisaoPlataforma2"  ~: False ~=? colisoesParede mapa1 entidade1, -- No collision with platforms 
    "testColisaoLimitesMapa1" ~: True  ~=? colisoesParede mapa1 entidade2, -- Collision with map boundaries
    "testColisaoLimitesMapa2" ~: False ~=? colisoesParede mapa1 entidade1  -- No collision with map boundaries
    ]

testscolisoesPersonagens :: Test
testscolisoesPersonagens = test [
    "testPersonagensColidem"    ~: True  ~=? colisoesPersonagens entidade1 entidade2, -- Collision with Entities
    "testPersonagensNaoColidem" ~: False ~=? colisoesPersonagens entidade2 entidade3  -- No collision with Entities
    ]

-- Definitions of some maps
mapa3 = Mapa ((1.5, 1.5), Este) (5, 5) [[Vazio, Vazio, Vazio], [Vazio, Vazio, Vazio], [Vazio, Vazio, Vazio]]
mapa4 = Mapa ((1.5, 1.5), Este) (5, 5) [[Vazio, Plataforma, Vazio], [Vazio, Escada, Vazio], [Plataforma, Plataforma, Plataforma]]
mapa5 = Mapa ((1.5, 1.5), Este) (5, 5) [[Vazio, Alcapao, Vazio], [Vazio, Escada, Vazio], [Plataforma, Plataforma, Plataforma]]
mapa6 = Mapa ((1.5,1.5),Este) (1.5,1.5) [[Vazio, Vazio, Vazio], [Vazio, Vazio, Vazio], [Plataforma, Plataforma, Plataforma]]
mapa7 = Mapa ((1.5,1.5),Este) (1.5,1.5) [[Vazio, Alcapao, Vazio], [Vazio, Escada, Vazio], [Vazio, Vazio, Plataforma]]

-- Definitions of some enemies
inimigos1 = [(Personagem (1, 0) MacacoMalvado (0.5, 0.5) Este (1, 1) False False 3 0 (False, 0)),(Personagem (1, 0) Fantasma (0.5, 0.5) Este (1, 1) False True 3 0 (False, 0))]
inimigos2 = [(Personagem (1, 0) Fantasma (0.5, 0.5) Este (1, 1) False True 2 0 (False, 0)),(Personagem (1, 0) Fantasma (0.5, 0.5) Este (1, 1) False True 1 0 (False, 0))]
inimigos3 = [(Personagem (1, 0) Fantasma (1.5, 1.5) Este (1, 1) False True 1 0 (False, 0))]
inimigos4 = [(Personagem (1, 0) Fantasma (2.5, 1.5) Este (1, 1) False True 1 0 (False, 0)),(Personagem (1, 0) Fantasma (2.5, 1.5) Este (1, 1) False True 1 0 (False, 0))]
inimigos5 = [(Personagem (1, 0) Fantasma (1.5, 1.5) Este (1, 1) False False 2 0 (False, 0))]

-- Definitions of some collectibles
colec1 = [(Moeda,(1.5,1.5)),(Martelo,(1.5,1.5))]
colec2 = [(Martelo,(2.5,2.5))]
colec3 = [(Moeda, (1.5,1.5))]

-- Definitions of some players
jogador1 = Personagem (1, 0) Jogador (1.5, 0.5) Este (1, 1) False False 3 0 (False, 0)
jogador2 = Personagem (1, 0) Jogador (0.5, 1.5) Este (2, 1) False True 3 0 (False, 0)
jogador3 = Personagem (1, 0) Jogador (2.5, 2.5) Este (2, 1) False True 3 0 (False, 0)
jogador4 = Personagem (1, 0) Jogador (0.5, 0.5) Este (1, 1) False False 3 0 (False, 0)

testsvalida :: Test
testsvalida = test [
       "validaFst" ~: True  ~=? valida (Jogo mapa6 inimigos4 colec3 jogador4), -- Valid game
       "validaSnd" ~: False ~=? valida (Jogo mapa7 inimigos5 colec2 jogador3)  -- Game not valid
       ]

testsvalidaChao :: Test
testsvalidaChao = test [
        "validaChaoFst" ~: True  ~=? chao mapa6, -- The last row of the map is made up of platforms
        "validaChaoSnd" ~: False ~=? chao mapa3  -- Some block in the last row of the map that isn't a Platform
        ]

testsvalidaRessalto :: Test
testsvalidaRessalto = test [
        "validaRessaltoFst" ~: True  ~=? ressalto inimigos4 jogador4, -- All enemies have the bouncing property True, while the player has it False
        "validaRessaltoSnd" ~: False ~=? ressalto inimigos5 jogador3  -- Not all enemies have the boucing property True , while the player has False
        ]

testsvalidaChoquePs :: Test
testsvalidaChoquePs = test [
        "validaChoquePsFst" ~: True  ~=? not (choquePs mapa6 inimigos4), -- The player's starting position does not collide with the starting position of another character
        "validaChoquePsSnd" ~: False ~=? not (choquePs mapa7 inimigos5)  -- The player's starting position collides with the starting position of another character
        ]

testsvalidaMinInimigos :: Test
testsvalidaMinInimigos = test [
        "validaMinInimigosFst" ~: True  ~=? minInimigos inimigos1, -- The game's enemy list has more than 2 enemies
        "validaMinInimigosSnd" ~: False ~=? minInimigos inimigos5  -- The game's enemy list has less than 2 enemies
        ]

testsvalidaVidaFan :: Test
testsvalidaVidaFan = test [
        "validaVidaFanFst" ~: True  ~=? vidaFan (vidaFanAux inimigos4), -- Phantom Enemies have exactly 1 life
        "validaVidaFanSnd" ~: False ~=? vidaFan (vidaFanAux inimigos5) -- Phantom Enemies don't have exactly 1 life
        ]

testsvalidaCheckBloc :: Test
testsvalidaCheckBloc = test [
        "validaCheckBlocFst" ~: True  ~=? checkBloc mapa6 (extEscada mapa6 (findBlocEAux mapa6)), -- Stairs must not begin/end in trapdoors, and at least one of their ends must be of the Platform type
        "validaCheckBlocSnd" ~: False ~=? checkBloc mapa7 (extEscada mapa7 (findBlocEAux mapa7))  -- The previous condition does not apply
        ]                                 

testsvalidaAlcLargo :: Test
testsvalidaAlcLargo = test [
        "validaAlcLargoFst" ~: True  ~=? alcLargo jogador4, -- The width of the trapdoor must not be less than the width of the player
        "validaAlcLargoSnd" ~: False ~=? alcLargo jogador3  -- The width of the trapdoor is less than the width of the player
        ]

testsvalidaColEntidades :: Test
testsvalidaColEntidades = test [
        "validaColEntidadesFst" ~: True  ~=? colEntidades inimigos4 jogador4 colec3 mapa6, -- There are no characters or collectibles "inside" platforms or trapdoors
        "validaColEntidadesSnd" ~: False ~=? colEntidades inimigos5 jogador3 colec2 mapa7  -- There are characters or collectibles "inside" platforms or trapdoors
        ]

-- Definitions of some games
jogo1 = Jogo mapa8 inimigos13 colec4 jogador5
jogo2 = Jogo mapa9 inimigos14 colec5 jogador6
jogo3 = Jogo mapa10 inimigos8 colec5 jogador7
jogo4 = Jogo mapa10 inimigos8 colec6 jogador11
jogo5 = Jogo mapa10 inimigos8 colec8 jogador13
jogo6 = Jogo mapa10 inimigos8 colec7 jogador11
jogo7 = Jogo mapa10 inimigos8 colec8 jogador12
jogo9 = Jogo mapa8 inimigos6 colec5 jogador5
jogo10 = Jogo mapa10 inimigos8 colec5 jogador18
jogo11 = Jogo mapa12 inimigos8 colec5 jogador7
jogo16 = Jogo mapa10 inimigos9 colec5 jogador7

-- Definitions of some maps
mapa8 = Mapa ((2.5, 1.5), Este) (5, 5) [[Plataforma, Plataforma, Plataforma, Plataforma, Plataforma], [Vazio, Vazio, Vazio, Vazio, Vazio], [Vazio, Alcapao, Vazio, Vazio, Vazio], [Vazio, Vazio, Vazio, Vazio, Vazio], [Plataforma, Plataforma, Plataforma, Plataforma, Plataforma]]
mapa9 = Mapa ((2.5, 1.5), Este) (5, 5) [[Plataforma, Plataforma, Plataforma, Plataforma, Plataforma], [Vazio, Vazio, Vazio, Vazio, Vazio], [Vazio, Vazio, Vazio, Vazio, Vazio], [Vazio, Vazio, Vazio, Vazio, Vazio], [Plataforma, Plataforma, Plataforma, Plataforma, Plataforma]]
mapa10 = Mapa ((2.5, 1.5), Este) (5, 5) [[Plataforma, Plataforma, Plataforma, Plataforma, Plataforma], [Vazio, Vazio, Vazio, Vazio, Vazio], [Plataforma, Vazio, Vazio, Vazio, Vazio], [Vazio, Vazio, Vazio, Vazio, Vazio], [Plataforma, Plataforma, Plataforma, Plataforma, Plataforma]]
mapa11 = Mapa ((2.5, 1.5), Este) (5, 5) [[Plataforma, Plataforma, Plataforma, Plataforma, Plataforma], [Vazio, Plataforma, Plataforma, Vazio, Vazio], [Plataforma, Vazio, Plataforma, Vazio, Vazio], [Vazio, Plataforma, Vazio, Vazio, Vazio], [Plataforma, Plataforma, Plataforma, Plataforma, Plataforma]]
mapa12 = Mapa ((2.5, 1.5), Este) (5, 5) [[Plataforma, Plataforma, Plataforma, Plataforma, Plataforma], [Vazio, Vazio, Vazio, Vazio, Vazio], [Vazio, Plataforma, Vazio, Vazio, Vazio], [Vazio, Vazio, Vazio, Vazio, Vazio], [Plataforma, Plataforma, Plataforma, Plataforma, Plataforma]]

-- Definitions of some enemies
inimigos6 = [(Personagem (1, 0) Fantasma (3.0, 1.5) Oeste (1, 1) False True 1 0 (False, 0)),(Personagem (1, 0) Fantasma (0.5, 1.5) Oeste (1, 1) False True 1 0 (False, 0)),(Personagem (1, 0) Fantasma (2.5, 2.5) Oeste (1, 1) False True 0 0 (False, 0))]
inimigos7 = [(Personagem (1, 0) Fantasma (0.5, 1.5) Oeste (1, 1) False True 1 0 (False, 0))]
inimigos8 = [(Personagem (0, 0) Fantasma (3.5, 3.5) Oeste (1, 1) False True 1 0 (False, 0))]
inimigos9 = [(Personagem (0, 0) Fantasma (2.5, 3.5) Oeste (1, 1) False True 1 0 (False, 0))]
inimigos10 = [(Personagem (1, 0) Fantasma (3.0, 1.5) Oeste (1, 1) False True 1 0 (False, 0)),(Personagem (1, 0) Fantasma (0.5, 1.5) Oeste (1, 1) False True 1 0 (False, 0))]
inimigos11 = [(Personagem (1, 0) Fantasma (0.5, 1.5) Oeste (1, 1) False True 1 0 (False, 0))] 
inimigos12 = [(Personagem (1, 0) Fantasma (2.5, 1.5) Oeste (1, 1) False True 2 0 (False, 0))] 
inimigos13 = [(Personagem (1, 0) Fantasma (2.5, 1.5) Oeste (1, 1) False True 1 0 (False, 0))]
inimigos14 = []
inimigos17 = [(Personagem (0, -1) Fantasma (2.5, 1.5) Este (1, 1) True True 3 0 (False, 0)),(Personagem (-1, 0) Fantasma (2.5, 1.5) Oeste (1, 1) False True 3 0 (False, 0))]
inimigos18 = [(Personagem (0, 0) Fantasma (2.5, 0.5) Este (1, 1) True True 3 0 (False, 0)),(Personagem (0, 0) Fantasma (1.5, 1.5) Oeste (1, 1) False True 3 0 (False, 0))]

-- Definitions of some collectibles
colec4 = [(Moeda, (1.5,1.5)), (Martelo,(4.5,4.5))] 
colec5 = [(Martelo,(4.5,4.5))]
colec6 = [(Moeda,(2.5,2.5))]
colec7 = [(Martelo,(2.5,2.5))]
colec8 = []

-- Definitions of some players
jogador5 = Personagem (0, 0) Jogador (1.5, 1.5) Este (1, 1) False True 3 0 (True, 5) 
jogador6 = Personagem (0, 0) Jogador (1.5, 11.5) Este (1, 1) False True 2 1 (True, 5)
jogador7 = Personagem (0, 0) Jogador (0.5, 1.5) Este (1, 1) False True 3 0 (False, 0)
jogador8 = Personagem (0, 0) Jogador (0.5, 3.5) Este (1, 1) False True 3 0 (False, 0)
jogador9 = Personagem (0, 10) Jogador (1.5, 1.5) Este (1, 1) False True 3 0 (True, 5)
jogador10 = Personagem (0, 10) Jogador (1.5, 1.5) Este (1, 1) False True 2 0 (True, 5)
jogador11 = Personagem (0, 0) Jogador (2.5, 2.5) Este (1, 1) False True 3 0 (False, 0)
jogador12 = Personagem (0, 0) Jogador (2.5, 2.5) Este (1, 1) False True 3 0 (True, 10)
jogador13 = Personagem (0, 0) Jogador (2.5, 2.5) Este (1, 1) False True 3 1 (False, 0)
jogador14 = Personagem (5, 0) Jogador (0.5, 1.5) Oeste (1, 1) False True 2 0 (True, 5)
jogador15 = Personagem (0, 0) Jogador (0.5, 1.5) Oeste (1, 1) False True 2 0 (True, 5)
jogador16 = Personagem (5, 10) Jogador (2.5, 2.5) Oeste (1, 1) False True 2 0 (True, 5)
jogador17 = Personagem (0, 0) Jogador (1.5, 1.5) Este (1, 1) False True 3 0 (True, 5)
jogador18 = Personagem (0, 10) Jogador (1.5, 1.5) Este (1, 1) False True 3 0 (False, 0)
jogador29 = Personagem (1, 0) Jogador (1.5, 1.5) Este (1, 1) False True 3 0 (False, 0)
jogador30 = Personagem (0, 0) Jogador (2.5, 1.5) Este (1, 1) False True 3 0 (False, 0)

-- Definitions of some map blocks
bloco1 = [[Plataforma, Plataforma, Plataforma, Plataforma, Plataforma], [Vazio, Vazio, Vazio, Vazio, Vazio], [Vazio, Vazio, Vazio, Vazio, Vazio], [Vazio, Vazio, Vazio, Vazio, Vazio], [Plataforma, Plataforma, Plataforma, Plataforma, Plataforma]]

-- Definitions of certain times
tempo1 = 1

-- Definitions of some seeds 
semente1 = 1

testsmovimenta :: Test
testsmovimenta = test [
        "movimentaFst" ~: jogo2  ~=? movimenta semente1 tempo1 jogo1, -- All the functions responsible for movement act simultaneously
        "movimentaSnd" ~: jogo16 ~=? movimenta semente1 tempo1 jogo3  -- No function responsible for movement acts simultaneously, with the exception of the random movement of enemies
        ]

testsmovInimigos :: Test
testsmovInimigos = test [
        "movInimigosFst" ~: inimigos18  ~=? movInimigo tempo1 inimigos17, -- All enemies move
        "movInimigosSnd" ~: inimigos18  ~=? movInimigo tempo1 inimigos18  -- No enemy moves
        ]

testsmovJogador :: Test
testsmovJogador = test [
        "movJogadorFst" ~: jogador30 ~=? movJogador tempo1 jogador29, -- The player moves
        "movJogadorSnd" ~: jogador17 ~=? movJogador tempo1 jogador17  -- The player does not move
        ]

testsperderVida :: Test
testsperderVida = test [
        "perderVidaFst" ~: inimigos13 ~=? perderVida inimigos12 jogador5, -- The enemy loses a life when contacting a player with active damage applied
        "perderVidaSnd" ~: inimigos8  ~=? perderVida inimigos8 jogador7   -- The enemy doesn't lose a life
        ]

testsgamaIni :: Test
testsgamaIni = test [
        "gamaIniFst" ~: inimigos10 ~=? gamaIni inimigos6, -- The enemy is filtered out if he has less than one life
        "gamaIniSnd" ~: inimigos8  ~=? gamaIni inimigos8  -- The enemy, as he has more than one life, is not filtered out
        ]

testsfallVoid :: Test
testsfallVoid = test [
        "fallVoidFst" ~: jogador9 ~=? fallVoid mapa9 jogador5, -- The player is affected by gravity because he is standing on an empty block
        "fallVoidSnd" ~: jogador8 ~=? fallVoid mapa10 jogador8 -- The player is not affected by gravity because he is standing on a platform block
        ]

testsdanoDuro :: Test
testsdanoDuro = test [
        "danoDuroFst" ~: jogador10 ~=? danoDuro inimigos11 jogador9, -- The player loses a life when contacting an enemy
        "danoDuroSnd" ~: jogador5  ~=? danoDuro inimigos8 jogador5   -- The player does not lose a life because he is not in contact with an enemy
        ]

testsrecColecEFilterColec :: Test
testsrecColecEFilterColec = test [
        "recColecFst"    ~: (colec8,jogador13)  ~=? recColec jogo4, -- The player picks up a hammer and gains applies damage for 10 seconds
        "recColecSnd"    ~: (colec8,jogador12)  ~=? recColec jogo6, -- The player collects a coin and increases their score
        "recColecThr"    ~: (colec5,jogador7)  ~=? recColec jogo3   -- The player doesn't collect anything
        ]

testsmenosAl :: Test
testsmenosAl = test [
        "menosAlFst" ~: bloco1 ~=? menosAl mapa8 jogador17, -- The player steps on a trapdoor tile, which is altered by an empty tile
        "menosAlSnd" ~: bloco1 ~=? menosAl mapa9 jogador5   -- The player does not step on a trapdoor block, meaning the map is not altered
        ]

testsnoPassPar :: Test
testsnoPassPar = test [
        "noPassParFst" ~: jogador15 ~=? noPassPar mapa11 jogador14, -- The character doesn't cross platform blocks or leave the map, even when moving in the direction to do so 
        "noPassParSnd" ~: jogador16 ~=? noPassPar mapa10 jogador16  -- The character doesn't leave the map, because they haven't moved
        ]

-- Definitions of enemy actions
acaoi1 = [Just AndarDireita, Just AndarDireita]
acaoi2 = [Nothing]

-- Definitions of player
acaoj1 = (Subir)
acaoj2 = (Descer)
acaoj3 = (AndarDireita)
acaoj4 = (AndarEsquerda)
acaoj5 = (Saltar)
acaoj6 = (Parar)
acaoj7 = (Nothing)
acaoj8 = (Just Subir)

-- Definitions of some players
jogador19 = Personagem (5, 0) Jogador (0.5, 0.5) Oeste (1, 1) True True 2 0 (True, 5)
jogador20 = Personagem (5, -1) Jogador (0.5, 0.5) Oeste (1, 1) True True 2 0 (True, 5)
jogador21 = Personagem (5, 1) Jogador (0.5, 0.5) Oeste (1, 1) True True 2 0 (True, 5)
jogador22 = Personagem (0, 0) Jogador (1.5, 1.5) Oeste (1, 1) False True 2 0 (True, 5)
jogador23 = Personagem (1, 0) Jogador (1.5, 1.5) Este (1, 1) False True 2 0 (True, 5)
jogador24 = Personagem (-1, 0) Jogador (1.5, 1.5) Oeste (1, 1) False True 2 0 (True, 5)
jogador25 = Personagem (0, -1) Jogador (1.5, 1.5) Oeste (1, 1) False True 2 0 (True, 5)
jogador26 = Personagem (0, 0) Jogador (1.5, 1.5) Oeste (1, 1) True True 2 0 (True, 5)
jogador27 = Personagem (5, 0) Jogador (0.5, 0.5) Oeste (1, 1) False True 2 0 (True, 5)
jogador28 = Personagem (0, 0) Jogador (1.5, 1.5) Oeste (1, 1) False True 2 0 (True, 5)

-- Definitions of some maps
mapa13 = Mapa ((1.5, 1.5), Este) (5, 5) [[Vazio, Vazio, Vazio], [Vazio, Vazio, Vazio], [Escada, Vazio, Vazio]]
mapa14 = Mapa ((1.5, 1.5), Este) (5, 5) [[Vazio, Vazio, Vazio], [Vazio, Vazio, Vazio], [Plataforma, Plataforma, Plataforma]]
mapa15 = Mapa ((1.5, 1.5), Este) (5, 5) [[Vazio, Vazio, Vazio], [Vazio, Vazio, Plataforma], [Escada, Plataforma, Plataforma]]
mapa16 = Mapa ((1.5, 1.5), Este) (5, 5) [[Vazio, Vazio, Vazio], [Vazio, Vazio, Vazio], [Vazio, Plataforma, Vazio]]
mapa17 = Mapa ((1.5, 1.5), Este) (5, 5) [[Escada, Vazio, Vazio], [Vazio, Vazio, Plataforma], [Plataforma, Plataforma, Plataforma]]
mapa18 = Mapa ((1.5, 1.5), Este) (5, 5) [[Escada, Vazio, Vazio], [Vazio, Vazio, Vazio], [Vazio, Vazio, Vazio]]
mapa19 = Mapa ((1.5, 1.5), Este) (5, 5) [[Vazio, Vazio, Vazio], [Plataforma, Vazio, Vazio], [Plataforma, Plataforma, Plataforma]]

-- Definitions of some games
jogo12 = (Jogo mapa17 inimigos15 colec1 jogador19)
jogo13 = (Jogo mapa17 inimigos16 colec1 jogador19)
jogo14 = (Jogo mapa17 inimigos15 colec1 jogador20)
jogo15 = (Jogo mapa17 inimigos16 colec1 jogador20)

-- Definitions of some enemies
inimigos15 = [(Personagem (0, 0) Fantasma (1.5, 1.5) Este (1, 1) False True 1 0 (False, 0)),(Personagem (0, 0) Fantasma (0.5, 1.5) Oeste (1, 1) False False 1 0 (False, 0))]
inimigos16 = [(Personagem (-2, 0) Fantasma (1.5, 1.5) Oeste (1, 1) False True 1 0 (False, 0)),(Personagem (2, 0) Fantasma (0.5, 1.5) Este (1, 1) False False 1 0 (False, 0))]

testsAtualiza :: Test
testsAtualiza = test [
        "atualizaAll"      ~: jogo15 ~=? atualiza acaoi1 acaoj8 jogo12, -- All enemies and the player are updated according to a certain list of actions
        "atualizaJogador"  ~: jogo14 ~=? atualiza acaoi2 acaoj8 jogo12, -- The player is updated according to a certain command
        "atualizaInimigos" ~: jogo13 ~=? atualiza acaoi1 acaoj7 jogo12, -- Enemies are updated through a list of actions
        "AtualizaInércia"  ~: jogo9  ~=? atualiza acaoi2 acaoj7 jogo9   -- All enemies and the player are updated so that their speed does not change
        ]

testsAcao :: Test
testsAcao = test [
        "subirFst"         ~: jogador20 ~=? atualizaAcao mapa12 acaoj1 jogador19, -- The player climbs a block of ladder
        "subirSnd"         ~: jogador27 ~=? atualizaAcao mapa13 acaoj1 jogador27, -- The player doesn't climb a ladder block
        "descerFst"        ~: jogador21 ~=? atualizaAcao mapa12 acaoj2 jogador19, -- The player descends a block of ladder
        "descerSnd"        ~: jogador27 ~=? atualizaAcao mapa13 acaoj2 jogador27, -- The player doesn´t descends a block of ladder
        "andarDireitaFst"  ~: jogador23 ~=? atualizaAcao mapa14 acaoj3 jogador22, -- The player moves to the right
        "andarDireitaSnd"  ~: jogador24 ~=? atualizaAcao mapa15 acaoj3 jogador22, -- The player does not move to the right
        "andarEsquerdaFst" ~: jogador24 ~=? atualizaAcao mapa14 acaoj4 jogador22, -- The player moves to the left
        "andarEsquerdaSnd" ~: jogador23 ~=? atualizaAcao mapa19 acaoj4 jogador22, -- The player does not move to the left
        "saltarFst"        ~: jogador25 ~=? atualizaAcao mapa16 acaoj5 jogador22, -- The player jumps
        "saltarSnd"        ~: jogador26 ~=? atualizaAcao mapa16 acaoj5 jogador26, -- The player doesn't jump
        "pararFst"         ~: jogador28 ~=? atualizaAcao mapa12 acaoj6 jogador25  -- The player remains inert
        ]

main :: IO ()
main = do runTestTTAndExit $ test [test_suite_tarefa_01, test_suite_tarefa_02, test_suite_tarefa_03, test_suite_tarefa_04]