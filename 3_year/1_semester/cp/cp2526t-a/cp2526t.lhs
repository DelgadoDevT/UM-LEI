\documentclass[11pt, a4paper, fleqn]{article}
\usepackage{cp2526t}
\makeindex

%================= lhs2tex=====================================================%
%include polycode.fmt
%%format (bin (n) (k)) = "\Big(\vcenter{\xymatrix@R=1pt{" n "\\" k "}}\Big)"
%format -|- = "+"
%format . = "\comp "
%format .* = "\star " 
%format .&&&. = "\wedge"
%format .<=. = "\leq"
%format .<==>. = "\Leftrightarrow"
%format .=?=. = "\mathbin{\stackrel{\mathrm{?}}{=}}"
%format .==. = "\equiv"
%format .==>. = "\Rightarrow"
%format (ana (g)) = "\ana{" g "}"
%format (ana' (f) (g)) = "\lanabracket\;\!" f "\:" g "\:\!\ranabracket"
%format (anaForest (f) (g)) = "\lanabracket\;\!" f "\:" g "\:\!\ranabracket_{\textit{\tiny F}}"
%format (anaList (g)) = "\anaList{" g "}"
%format (anaLTree (g)) = "\lanabracket\," g "\,\ranabracket"
%format (anaStream (g)) = "\lanabracket\," g "\,\ranabracket"
%format (anaRose (g)) = "\lanabracket\," g "\,\ranabracket_\textit{\tiny R}"
%format (anaTree (f) (g)) = "\lanabracket\;\!" f "\:" g "\:\!\ranabracket_{\textit{\tiny T}}"
%format (cata (f)) = "\llparenthesis\, " f "\,\rrparenthesis"
%format (cata' (f) (g)) = "\llparenthesis\, " f "\:" g "\,\rrparenthesis"
%format (cataBTree (x)) = "\llparenthesis\, " x "\,\rrparenthesis"
%format (cataForest (f) (g)) = "\llparenthesis\, " f "\:" g "\,\rrparenthesis_{\textit{\tiny F}}"
%format (cataFTree (x)) = "\llparenthesis\, " x "\,\rrparenthesis"
%format (cataList (g)) = "\llparenthesis\, " g "\,\rrparenthesis"
%format (cataNat (g)) = "\cataNat{" g "}"
%format (cataRose (x)) = "\llparenthesis\, " x "\,\rrparenthesis_\textit{\tiny R}"
%format (cataTree (f) (g)) = "\llparenthesis\, " f "\:" g "\,\rrparenthesis_{\textit{\tiny T}}"
%format (const (f)) = "\underline{" f "}"
%format (Cp.cond (p) (f) (g)) = "\mcond{" p "}{" f "}{" g "}"
%format (curry (f)) = "\overline{" f "}"
%format (div (x)(y)) = x "\div " y
%format (either (a) (b)) = "\alt{" a "}{" b "}"
%format (fac (n)) = "{" n "!}"
%format (for (f) (i)) = "\for{" f "}\ {" i "}"
%format (frac (a) (b)) = "\frac{" a "}{" b "}"
%format (frac (n)(m)) = "\frac{" n "}{" m "}"
%format (hylo (g) (h)) = "\llbracket\, " g ",\," h "\,\rrbracket"
%format (hylo' (ft) (ff) (gt) (gf)) = "\llbracket\, " ft "\:" ff ",\," gt "\:" gf "\,\rrbracket"
%format (hyloForest (ft) (ff) (gt) (gf)) = "\llbracket\, " ft "\:" ff ",\," gt "\:" gf "\,\rrbracket_{\textit{\tiny F}}"
%format (hyloRose (g) (h)) = "\llbracket\, " g ",\," h "\,\rrbracket_\textit{\tiny R}"
%format (hyloTree (ft) (ff) (gt) (gf)) = "\llbracket\, " ft "\:" ff ",\," gt "\:" gf "\,\rrbracket_{\textit{\tiny T}}"
%format (kcomp (f)(g)) = f "\kcomp " g
%format (lcbr (x)(y)) = "\begin{lcbr}" x "\\" y "\end{lcbr}"
%format (lcbr3 (x)(y)(z)) = "\begin{lcbr}" x "\\" y "\\" z "\end{lcbr}"
%format (plus (f)(g)) = "{" f "}\plus{" g "}"
%format (Prod (a) (b)) = a >< b
%format (Seq (a)) = "{" a "}^{*}"
%format (split (x) (y)) = "\conj{" x "}{" y "}"
%format (square (x)) = x "^2"
%format (uncurry f) = "\uncurry{" f "}"
%format (underbrace (t) (p)) = "\underbrace{" t "}_{" p "}"
%format % = "\mathbin{/}"
%format `minusNat`= "\mathbin{-}"
%format `ominus` = "\mathbin{\ominus}"
%format ++ = "\mathbin{+\!\!+}"
%format <-> = "{\,\leftrightarrow\,}"
%format <|> = "{\,\updownarrow\,}"
%format <$> = "\mathbin{\mathopen{\langle}\$\mathclose{\rangle}}"
%format ==> = "\Longrightarrow "
%format ==> = "\Rightarrow"
%format >< = "\times"
%format >|<  = "\bowtie "
%format |-> = "\mapsto"
%format B_tree = "\mathsf{B}\mbox{-}\mathsf{tree} "
%format cdots = "\cdots "
%format conc = "\mathsf{conc}"
%format delta = "\Delta "
%format Dist = "\fun{Dist}"
%format Either a b = a "+" b
%format fF = "\fun F "
%format fmap = "\mathsf{fmap}"
%format fromRational = " from_\Q "
%format fst = "\p1"
%format FTree = "{\FTree}"
%format i1 = "i_1"
%format i2 = "i_2"
%format inForest = "\mathsf{in}_{Forest}"
%format inFTree = "\mathsf{in}"
%format inLTree = "\mathsf{in}"
%format inNat = "\mathsf{in}"
%format inT = "\mathsf{in}"
%format Integer  = "\mathbb{Z}"
%format inTree = "\mathsf{in}_{Tree}"
%format IO = "\fun{IO}"
%format l2 = "l_2 "
%format Left = "i_1"
%format length = "\length "
%format LTree = "{\LTree}"
%format map = "\map "
%format matrix = "matrix"
%format muB = "\mu "
%format NA   = "\textsc{na}"
%format Nat0 = "\N_0"
%format NB   = "\textbf{NB}"
%format Null = "1"
%format outForest = "\mathsf{out}_{Forest}"
%format outFTree = "\mathsf{out}"
%format outLTree = "\mathsf{out}"
%format outStream = "\mathsf{out}"
%format outT = "\mathsf{out}"
%format outTree = "\mathsf{out}_{Tree}"
%format p1  = "\p1"
%format p2  = "\p2"
%format pi = "\pi "
%format Rational = "\Q "
%format Right = "i_2"
%format snd = "\p2"
%format succ = "\succ "
%format summation = "{\sum}"
%format TLTree = "\mathsf{TLTree}"
%format toRational = " to_\Q "
%format t1 = "t_1 "
%format t2 = "t_2 "
%format t3 = "t_3 "
%format t4 = "t_4 "
%format t5 = "t_5 "
%------------------------------------------------------------------------------%


%====== DEFINIR GRUPO E ELEMENTOS =============================================%

\group{G01}
\studentA{68243}{José Pedro Pinheiro da Silva}
\studentB{106836}{João Pedro Delgado Teixeira}
\studentC{106928}{Simão Pedro Pacheco Mendes}

%==============================================================================%

\begin{document}
\sffamily
\setlength{\parindent}{0em}
\emergencystretch 3em
\renewcommand{\baselinestretch}{1.25} 
\input{Cover}
\pagestyle{pagestyle}

\newgeometry{left=25mm,right=20mm,top=25mm,bottom=25mm}
\setlength{\parindent}{1em}

\section*{Preâmbulo}

Em \CP\ pretende-se ensinar a progra\-mação de computadores
como uma disciplina científica. Para isso parte-se de um repertório de \emph{combinadores}
que formam uma álgebra da programação % (conjunto de leis universais e seus corolários)
e usam-se esses combinadores para construir programas \emph{composicionalmente},
isto é, agregando programas já existentes.

Na sequência pedagógica dos planos de estudo dos cursos que têm
esta disciplina, opta-se pela aplicação deste método à programação
em \Haskell\ (sem prejuízo da sua aplicação a outras linguagens
funcionais). Assim, o presente trabalho prático coloca os
alunos perante problemas concretos que deverão ser implementados em
\Haskell. Há ainda um outro objectivo: o de ensinar a documentar
programas, a validá-los e a produzir textos técnico-científicos de
qualidade.

Antes de abordarem os problemas propostos no trabalho, os grupos devem ler
com atenção o anexo \ref{sec:documentacao} onde encontrarão as instruções
relativas ao \emph{software} a instalar, etc.

Valoriza-se a escrita de \emph{pouco} código que corresponda a soluções
simples e elegantes que utilizem os combinadores de ordem superior estudados
na disciplina.

\noindent \textbf{Avaliação}. Faz parte da avaliação do trabalho a sua defesa
por parte dos elementos de cada grupo. Estes devem estar preparados para
responder a perguntas sobre \emph{qualquer} dos problemas deste enunciado.
A prestação \emph{individual} de cada aluno nessa defesa oral será uma componente
importante e diferenciadora da avaliação.

%if False
\begin{code}
{-# OPTIONS_GHC -XNPlusKPatterns #-}
{-# LANGUAGE GeneralizedNewtypeDeriving, DeriveDataTypeable, FlexibleInstances #-}
module Main where
import Cp
import List hiding (fac)
import Nat hiding (aux)
import LTree hiding (merge)
import BTree
-- import Exp
import Probability
-- import Svg hiding (for,dup,fdiv)
import Data.Char
import Data.Ratio
import Data.List hiding (find)
import Control.Monad
-- import Control.Monad.State
import Control.Applicative hiding ((<|>),empty)
import System.Process
import Control.Concurrent

main = undefined
\end{code}
%endif

\Problema

Uma serialização (ou travessia) de uma árvore é uma sua representação sob a forma de uma lista. 
Na biblioteca |BTree| encontram-se as funções de serialização |inordt|, |preordt| e |postordt|,
que fazem as travessias \emph{in-order}, \emph{ pre-order} e \emph{post-order}, respectivamente.
Todas essas travessias são catamorfismos que percorrem a árvore argumento em regime \emph{depth-first}.

Pretende-se agora uma função |bforder| que faça a travessia em regime \emph{breadth-first},
isto é, por níveis.
Por exemplo, para a árvore |t1| dada em anexo e mostrada na figura a seguir,

\begin{center}
	\figura
\end{center}

\noindent a função deverá dar a lista

\begin{spec}
	[5,3,7,1,4,6,8]
\end{spec}

\noindent em que se vê como os níveis |5|, depois |3,7| e finalmente |1,4,6,8| foram percorridos.

Pretendemos propor duas versões dessa função:

\begin{enumerate}
\item	Uma delas envolve um catamorfismo de |BTree|s:
\begin{code}
bfsLevels :: BTree a -> [a]
bfsLevels = concat . levels
\end{code}
Complete a definição desse catamorfismo:
\begin{code}
levels :: BTree a -> [[a]]
levels = cataBTree glevels
\end{code}
\item A segunda proposta,
\begin{spec}
bft :: BTree a -> [a] 
\end{spec}
deverá basear-se num anamorfismo de listas.
\end{enumerate}
\textbf{Sugestão}: estudar o artigo \cite{Ok00} cujo PDF está incluído no material deste trabalho. 
Quando fizer testes ao seu código pode, se desejar, usar funções disponíveis na biblioteca
|Exp| para visualizar as árvores em GraphViz (formato .dot).

Justifique devidamente a sua resolução, que deverá vir acompanhada de diagramas explicativos.
Como já se disse, valoriza-se a escrita de \emph{pouco} código que corresponda a soluções
simples e elegantes que utilizem os combinadores de ordem superior estudados
na disciplina.



\Problema

Considere a seguinte função em Haskell:
\begin{quote}
\begin{code}
f x = wrapper . worker where
         wrapper = head
         worker 0 = start x
         worker(n+1) = loop x (worker n)

loop x    [s,         h,       k,     j,     m     ] =
          [h / k + s, x^2 * h, k * j, j + m, m + 8 ]

start x = [x,         x^3,     6,     20,    22    ]
\end{code}
\end{quote}
Pode-se provar pela lei de recursividade mútua que |f x n| calcula o seno hiperbólico de |x|,
|sinh x|, para |n| aproximações da sua série de Taylor. 
Faça a derivação da função dada a partir da referida série de Taylor, apresentando todos os cálculos justificativos, tal como se faz para outras funções no capítulo respectivo do texto base desta UC \cite{Ol98-24}.

\Problema

Quem em Braga observar, ao fim da tarde, o tráfego onde a Avenida Clairmont
Fernand se junta à N101, aproximadamente na coordenada \href{https://maps.app.goo.gl/uCbXLsdibYoochr36}{41°33'46.8"N
8°24'32.4"W} --- ver as setas da figura que se segue --- reparará nas sequências
imparáveis (infinitas!) de veículos provenientes dessas vias de circulação.

Mas também irá observar um comportamento interessante por parte dos condutores desses
veículos: por regra, \emph{cada carro numa via deixa passar, à sua frente, exactamente outro carro da outra via}. 

\begin{center}
	\mapa
\end{center}

Este comportamento \emph{civilizado} chama-se \emph{fair-merge} (ou \emph{fair-interleaving})
de duas sequências infinitas, também designadas \emph{streams} em ciência
da computação. Seja dado o tipo dessas sequências em Haskell,
\begin{code}
data Stream a = Cons (a, Stream a) deriving Show  
\end{code}
para o qual se define também:
\begin{code}
outStream (Cons (x,xs)) = (x,xs)
\end{code}
\noindent O referido comportamento civilizado pode definir-se, em Haskell,
da forma seguinte:\footnote{O facto das sequências serem infinitas não nos
deve preocupar, pois em Haskell isso é lidado de forma transparente por \lazy{lazy
evaluation}.}
\begin{code}
fair_merge :: Either (Stream a, Stream a) (Stream a, Stream a) -> Stream a
fair_merge = either h k where
   h (Cons(x,xs), y) = Cons(x , k(xs,y))
   k (x, Cons(y,ys)) = Cons(y , h(x,ys))
\end{code}

Defina |fair_merge| como um \textbf{anamorfismo} de |Stream|s, usando o combinador
\begin{code}
anaStream g = Cons . (id >< (anaStream g)) . g
\end{code}
e a seguinte estratégia:
\begin{itemize}
\item	Derivar a lei \textbf{dual} da recursividade mútua,
\begin{eqnarray}
	|either f g = ana(either h k)| & \equiv & |lcbr (out . f = fF (either f g) . h)(out . g = fF (either f g) . k)|
	\label{eq:fokkinga_dual}
\end{eqnarray}
	tal como se fez, nas aulas, para a que está no formulário.
\item
	Usar (\ref{eq:fokkinga_dual}) na resolução do problema proposto. 
\end{itemize}
Justificar devidamente a resolução, que deverá vir acompanhada de diagramas explicativos.

\Problema

Como se sabe, é possível pensarmos em catamorfismos, anamorfismos etc \emph{probabilísticos},
quer dizer, programas recursivos que dão distribuições como resultados. Por
exemplo, podemos pensar num combinador
\begin{spec}
pcataList :: (Either () (a, b) -> Dist b) -> [a] -> Dist b
\end{spec}
que é muito parecido com
\begin{spec}
cataList :: (Either () (a, b) -> b) -> [a] -> b
\end{spec}
da biblioteca \List. A principal diferença é que o gene de |pcataList| é uma função probabilística.

Como exemplo de utilização, recorde-se que |cataList (either zero add)| soma todos
os elementos da lista argumento, por exemplo:
\begin{quote}
|cataList (either zero add) [20,10,5] = 35|.
\end{quote}
Considere-se agora a função |padd| (adição probabilística) que,
com probabilidade $90\%$ soma dois números e com probabilidade $10\%$ os subtrai:
\begin{code}
padd(a,b) = D [(a+b,0.9),(a-b,0.1)]
\end{code}
Se se correr
\begin{code}
d4 = pcataList (either pzero padd) [20,10,5] where pzero = return . zero
\end{code}
obter-se-á:
\begin{Verbatim}[fontsize=\small]
35  81.0%
25   9.0%
 5   9.0%
15   1.0%
\end{Verbatim}

Com base neste exemplo, resolva o seguinte
\begin{quote}\em
\textbf{Problema}: Uma unidade militar pretende enviar uma mensagem urgente
a outra, mas tem o aparelho de telegrafia meio avariado. Por experiência,
o telegrafista sabe que a probabilidade de uma palavra se perder (não ser
transmitida) é $5\%$; e que, no final de cada mensagem, o aparelho envia o código
|"stop"|, mas (por estar meio avariado), falha $10\%$ das vezes.

Qual a probabilidade de a palavra |"atacar"| da mensagem 
\begin{quote}
|words "Vamos atacar hoje"|
\end{quote}
se perder, isto é, o resultado da transmissão ser |["Vamos","hoje","stop"]|?
E a de seguirem todas as palavras, mas faltar o |"stop"| no fim? E a da transmissão
ser perfeita?
\end{quote}

Responda a estas perguntas encontrando |gene| tal que
\begin{code}
transmitir = pcataList gene
\end{code}
descreve o comportamento do aparelho.
Justificar devidamente a resolução, que deverá vir acompanhada de diagramas explicativos.
%

\part*{Anexos}

\appendix

\section{Natureza do trabalho a realizar}
\label{sec:documentacao}
Este trabalho teórico-prático deve ser realizado por grupos de 3 alunos.
Os detalhes da avaliação (datas para submissão do relatório e sua defesa
oral) são os que forem publicados na \cp{página da disciplina} na \emph{internet}.

Recomenda-se uma abordagem participativa dos membros do grupo em \textbf{todos}
os exercícios do trabalho, para assim poderem responder a qualquer questão
colocada na \emph{defesa oral} do relatório.

Para cumprir de forma integrada os objectivos do trabalho vamos recorrer
a uma técnica de programa\-ção dita ``\litp{literária}'' \cite{Kn92}, cujo
princípio base é o seguinte:
%
\begin{quote}\em
        Um programa e a sua documentação devem coincidir.
\end{quote}
%
Por outras palavras, o \textbf{código fonte} e a \textbf{documentação} de um
programa deverão estar no mesmo ficheiro.

O ficheiro \texttt{cp2526t.pdf} que está a ler é já um exemplo de
\litp{programação literária}: foi gerado a partir do texto fonte
\texttt{cp2526t.lhs}\footnote{O sufixo `lhs' quer dizer
\emph{\lhaskell{literate Haskell}}.} que encontrará no \MaterialPedagogico\
desta disciplina des\-com\-pactando o ficheiro \texttt{cp2526t.zip}.

Como se mostra no esquema abaixo, de um único ficheiro (|lhs|)
gera-se um PDF ou faz-se a interpretação do código \Haskell\ que ele inclui:

        \esquema

Vê-se assim que, para além do \GHCi, serão necessários os executáveis \PdfLatex\ e
\LhsToTeX. Para facilitar a instalação e evitar problemas de versões e
conflitos com sistemas operativos, é recomendado o uso do \Docker\ tal como
a seguir se descreve.

\section{Docker} \label{sec:docker}

Recomenda-se o uso do \container\ cuja imagem é gerada pelo \Docker\ a partir do ficheiro
\texttt{Dockerfile} que se encontra na diretoria que resulta de descompactar
\texttt{cp2526t.zip}. Este \container\ deverá ser usado na execução
do \GHCi\ e dos comandos relativos ao \Latex. (Ver também a \texttt{Makefile}
que é disponibilizada.)

Após \href{https://docs.docker.com/engine/install/}{instalar o Docker} e
descarregar o referido zip com o código fonte do trabalho,
basta executar os seguintes comandos:
\begin{Verbatim}[fontsize=\small]
    $ docker build -t cp2526t .
    $ docker run -v ${PWD}:/cp2526t -it cp2526t
\end{Verbatim}
\textbf{NB}: O objetivo é que o container\ seja usado \emph{apenas} 
para executar o \GHCi\ e os comandos relativos ao \Latex.
Deste modo, é criado um \textit{volume} (cf.\ a opção \texttt{-v \$\{PWD\}:/cp2526t}) 
que permite que a diretoria em que se encontra na sua máquina local 
e a diretoria \texttt{/cp2526t} no \container\ sejam partilhadas.

Pretende-se então que visualize/edite os ficheiros na sua máquina local e que
os compile no \container, executando:
\begin{Verbatim}[fontsize=\small]
    $ lhs2TeX cp2526t.lhs > cp2526t.tex
    $ pdflatex cp2526t
\end{Verbatim}
\LhsToTeX\ é o pre-processador que faz ``pretty printing'' de código Haskell
em \Latex\ e que faz parte já do \container. Alternativamente, basta executar
\begin{Verbatim}[fontsize=\small]
    $ make
\end{Verbatim}
para obter o mesmo efeito que acima.

Por outro lado, o mesmo ficheiro \texttt{cp2526t.lhs} é executável e contém
o ``kit'' básico, escrito em \Haskell, para realizar o trabalho. Basta executar
\begin{Verbatim}[fontsize=\small]
    $ ghci cp2526t.lhs
\end{Verbatim}

\noindent Abra o ficheiro \texttt{cp2526t.lhs} no seu editor de texto preferido
e verifique que assim é: todo o texto que se encontra dentro do ambiente
\begin{quote}\small\tt
\verb!\begin{code}!
\\ ... \\
\verb!\end{code}!
\end{quote}
é seleccionado pelo \GHCi\ para ser executado.

\section{Em que consiste o TP}

Em que consiste, então, o \emph{relatório} a que se referiu acima?
É a edição do texto que está a ser lido, preenchendo o anexo \ref{sec:resolucao}
com as respostas. O relatório deverá conter ainda a identificação dos membros
do grupo de trabalho, no local respectivo da folha de rosto.

Para gerar o PDF integral do relatório deve-se ainda correr os comando seguintes,
que actualizam a bibliografia (com \Bibtex) e o índice remissivo (com \Makeindex),
\begin{Verbatim}[fontsize=\small]
    $ bibtex cp2526t.aux
    $ makeindex cp2526t.idx
\end{Verbatim}
e recompilar o texto como acima se indicou. (Como já se disse, pode fazê-lo
correndo simplesmente \texttt{make} no \container.)

No anexo \ref{sec:codigo} disponibiliza-se algum código \Haskell\ relativo
aos problemas que são colocados. Esse anexo deverá ser consultado e analisado
à medida que isso for necessário.

Deve ser feito uso da \litp{programação literária} para documentar bem o código que se
desenvolver, em particular fazendo diagramas explicativos do que foi feito e
tal como se explica no anexo \ref{sec:diagramas} que se segue.

\section{Como exprimir cálculos e diagramas em LaTeX/lhs2TeX} \label{sec:diagramas}

Como primeiro exemplo, estudar o texto fonte (\lhstotex{lhs}) do que está a ler\footnote{
Procure e.g.\ por \texttt{"sec:diagramas"}.} onde se obtém o efeito seguinte:\footnote{Exemplos
tirados de \cite{Ol98-24}.}
\begin{eqnarray*}
\start
|
        id = split f g
|
\just\equiv{ universal property }
|
     lcbr(
          p1 . id = f
     )(
          p2 . id = g
     )
|
\just\equiv{ identity }
|
     lcbr(
          p1 = f
     )(
          p2 = g
     )
|
\qed
\end{eqnarray*}

Os diagramas podem ser produzidos recorrendo à \emph{package} \Xymatrix, por exemplo:
\begin{eqnarray*}
\xymatrix@@C=2cm{
    |Nat0|
           \ar[d]_-{|cataNat g|}
&
    |1 + Nat0|
           \ar[d]^{|id + (cataNat g)|}
           \ar[l]_-{|inNat|}
\\
     |B|
&
     |1 + B|
           \ar[l]^-{|g|}
}
\end{eqnarray*}

\section{O mónade das distribuições probabilísticas} \label{sec:probabilities}
%format B = "\mathit B"
%format C = "\mathit C"
Mónades são functores com propriedades adicionais que nos permitem obter
efeitos especiais em progra\-mação. Por exemplo, a biblioteca \Probability\
oferece um mónade para abordar problemas de probabilidades. Nesta biblioteca,
o conceito de distribuição estatística é captado pelo tipo
\begin{eqnarray}
     |newtype Dist a = D {unD :: [(a, ProbRep)]}|
     \label{eq:Dist}
\end{eqnarray}
em que |ProbRep| é um real de |0| a |1|, equivalente a uma escala de $0$ a
$100 \%$.

Cada par |(a,p)| numa distribuição |d::Dist a| indica que a probabilidade
de |a| é |p|, devendo ser garantida a propriedade de  que todas as probabilidades
de |d| somam $100\%$.
Por exemplo, a seguinte distribuição de classificações por escalões de $A$ a $E$,
\[
\begin{array}{ll}
A & \rule{2mm}{3pt}\ 2\%\\
B & \rule{12mm}{3pt}\ 12\%\\
C & \rule{29mm}{3pt}\ 29\%\\
D & \rule{35mm}{3pt}\ 35\%\\
E & \rule{22mm}{3pt}\ 22\%\\
\end{array}
\]
será representada pela distribuição
\begin{code}
d1 :: Dist Char
d1 = D [('A',0.02),('B',0.12),('C',0.29),('D',0.35),('E',0.22)]
\end{code}
que o \GHCi\ mostrará assim:
\begin{Verbatim}[fontsize=\small]
'D'  35.0%
'C'  29.0%
'E'  22.0%
'B'  12.0%
'A'   2.0%
\end{Verbatim}
É possível definir geradores de distribuições, por exemplo distribuições \emph{uniformes},
\begin{code}
d2 = uniform (words "Uma frase de cinco palavras")
\end{code}
isto é
\begin{Verbatim}[fontsize=\small]
     "Uma"  20.0%
   "cinco"  20.0%
      "de"  20.0%
   "frase"  20.0%
"palavras"  20.0%
\end{Verbatim}
distribuição \emph{normais}, eg.\
\begin{code}
d3 = normal [10..20]
\end{code}
etc.\footnote{Para mais detalhes ver o código fonte de \Probability, que é uma adaptação da
biblioteca \PFP\ (``Probabilistic Functional Programming''). Para quem quiser saber mais
recomenda-se a leitura do artigo \cite{EK06}.}
|Dist| forma um \textbf{mónade} cuja unidade é |return a = D [(a,1)]| e cuja composição de Kleisli
é (simplificando a notação)
\begin{spec}
  ((kcomp f g)) a = [(y,q*p) | (x,p) <- g a, (y,q) <- f x]
\end{spec}
em que |g: A -> Dist B| e |f: B -> Dist C| são funções \textbf{monádicas} que representam
\emph{computações probabilísticas}.

Este mónade é adequado à resolução de problemas de \emph{probabilidades e estatística} usando programação funcional, de forma elegante e como caso particular da programação monádica.

\section{Código fornecido}\label{sec:codigo}

\subsection*{Problema 1}

Árvores exemplo:
\begin{code}
t1 :: BTree Int
t1 = Node (5,(Node (3,(Node (1,(Empty,Empty)),Node (4,(Empty,Empty)))),
           Node (7,(Node (6,(Empty,Empty)),Node (8,(Empty,Empty))))))

t2 :: BTree Int
t2 =
  node 1
    (node 2 (node 4 Empty Empty) (node 5 Empty Empty))
    (node 3 (node 6 Empty Empty) (node 7 Empty Empty))

t3 :: BTree Char
t3 =
  node 'A'
    (node 'B' (node 'C' (node 'D' Empty Empty) Empty) Empty)
    (node 'E' Empty Empty)

t4 :: BTree Char
t4 =
  node 'A'
    (node 'B' (node 'C' (node 'D' Empty Empty) Empty) Empty)
    Empty 

t5 :: BTree Int
t5 =
  node 1
   (node 2 (node 4 Empty Empty) Empty)
   (node 3 Empty (node 5 (node 6 Empty Empty) Empty))

node a b c = Node (a,(b,c))
\end{code}

%----------------- Soluções dos alunos -----------------------------------------%

\section{Soluções dos alunos}\label{sec:resolucao}
Os alunos devem colocar neste anexo as suas soluções para os exercícios
propostos, de acordo com o ``layout'' que se fornece.
Não podem ser alterados os nomes ou tipos das funções dadas, mas pode ser
adicionado texto ao anexo, bem como diagramas e/ou outras funções auxiliares
que sejam necessárias.

\noindent
\textbf{Importante}: Não pode ser alterado o texto deste ficheiro fora deste anexo.

% PROBLEMA 1

\subsection*{Problema 1}

\subsubsection*{Introdução e Análise do Problema}
A travessia em largura (\textit{Breadth-First Search} --- BFS) constitui um desafio clássico no paradigma da programação funcional. 
Enquanto as travessias em profundidade (\textit{Depth-First Search} --- DFS) emergem naturalmente 
da estrutura indutiva dos tipos de dados algébricos, com o processamento de cada ramo na totalidade 
antes da transição para o seguinte, a BFS exige uma estratégia de processamento transversal de modo 
a visitar os nós por níveis de profundidade (camada a camada).

Para a definição da estrutura de dados:

\begin{spec}
data BTree a = Empty | Node(a, (BTree a, BTree a)) deriving Show
\end{spec}

A manipulação desta estrutura é efetuada através dos isomorfismos |inBTree| e |outBTree|, 
que respetivamente montam e desmontam a árvore de acordo com a sua assinatura functorial:

\begin{spec}
inBTree :: Either () (b,(BTree b,BTree b)) -> BTree b
inBTree = either (const Empty) Node

outBTree :: BTree a -> Either () (a,(BTree a,BTree a))
outBTree Empty              = Left ()
outBTree (Node (a,(t1,t2))) = Right(a,(t1,t2))
\end{spec}

onde é definido o functor:
\begin{equation}
\begin{cases}
F X = 1 + A \times X^2 \\
F f = id + id \times f^2
\end{cases}
\end{equation}

Exploramos a dualidade entre o catamorfismo e o anamorfismo na realização de uma travessia BFS. 
Propomos, assim, duas soluções distintas para o problema:
\begin{enumerate}
    \item \textbf{Catamorfismo:} Consome a estrutura da árvore para gerar uma representação intermédia 
    estratificada por níveis, tipificada por uma lista de listas (|[[a]]|).
    \item \textbf{Anamorfismo:} Gera a sequência de visita a partir de um estado que simula uma fila de 
    espera (\textit{FIFO}), o que modela o comportamento dinâmico da travessia.
\end{enumerate}

Procurou-se definir as soluções recorrendo ao máximo de primitivas \textit{pointfree} possível, 
com o uso dos combinadores e operadores fornecidos pela equipa docente no contexto do cálculo de programas.

\subsubsection*{1. Solução via Catamorfismo}
A análise do problema iniciou-se com o estudo da base de código fornecida pela equipa docente:

\begin{spec}
bfsLevels :: BTree a -> [a]
bfsLevels = concat . levels

levels :: BTree a -> [[a]]
levels = cataBTree glevels
\end{spec}

A função |bfsLevels| constitui o ponto de entrada e saída para o processamento da |BTree a|. 
Esta utiliza a função predefinida |concat|, que recebe a estrutura estratificada produzida pelo catamorfismo 
e aglutina os vários níveis numa lista única. 

A função |levels| tem como objetivo processar uma |BTree| para gerar uma lista de listas (|[[a]]|), 
onde cada sub-lista de índice $k$ contém exclusivamente os elementos que residem na profundidade $k$ da árvore original, 
ordenados do topo para a base.

Formalmente, |bfslevels| define-se como a composição de uma concatenação e um catamorfismo de árvores binárias, 
cuja tipagem e estrutura recursiva são descritas pelo seguinte diagrama:

\[
\centerline{
    \xymatrix@@C=3.5cm@@R=1.5cm{
        |BTree A|
            \ar@@/_3cm/[dd]_-{|bfsLevels|}
            \ar[d]_-{|levels|}
            \ar@@/^0.5cm/[r]^-{|outBTree|}
    &
        |1 + A >< (BTree A >< BTree A)|
            \ar@@/^0.5cm/[l]^-{|inBTree|}
            \ar[d]^{|id + id >< (levels >< levels)|}
    \\
        |[[A]]|
            \ar[d]_-{|concat|}
    &
        |1 + A >< ([[A]] >< [[A]])|
            \ar[l]^-{|glevels|}
    \\
        |[A]| &
    }
}
\]

\paragraph{Análise do gene |glevels|:}
O gene deste catamorfismo especifica a reconstrução dos níveis da árvore a partir das sub-estruturas já processadas:
\begin{itemize}
    \item \textbf{Caso Base (|Left ()|):} Uma árvore vazia resulta numa lista de níveis vazia (|nil|), representada por uma 
    lista de listas sem elementos (|[]|).
    \item \textbf{Caso Recursivo (|Right (a, (ls, rs))|):} A raiz |a| é isolada como o nível inicial através de |singl a|. 
    Este nível é então colocado à cabeça (via |cons|) da estrutura resultante da fusão das sub-árvores 
    esquerda (|ls|) e direita (|rs|), as quais são combinadas através da função auxiliar |mergeLevels|. 
\end{itemize}

O resultado final do gene é a aglutinação destes componentes numa estrutura de lista de listas (|[[a]]|), 
o que preserva a hierarquia por níveis necessária para o achatamento final pela função |concat|.

A função |mergeLevels| é o componente crítico desta abordagem. Esta opera como uma 
generalização do combinador |zipWith (++)|; 
contudo, ao contrário do |zipWith| definido na linguagem Haskell, esta preserva a cauda 
da estrutura mais profunda caso a árvore não seja balanceada. 
Desta forma, garante-se que nenhum nível é descartado durante a fusão dos ramos.

A implementação em Haskell de |glevels| e da respetiva auxiliar é a seguinte:

\begin{code}
glevels :: Either () (a, ([[a]], [[a]])) -> [[a]]
glevels = either nil (cons . (singl >< mergeLevels)) 
  where
    mergeLevels :: ([[a]], [[a]]) -> [[a]]
    mergeLevels ([], ys) = ys
    mergeLevels (xs, []) = xs
    mergeLevels ((x:xs), (y:ys)) = cons (conc (x, y), mergeLevels (xs, ys))
\end{code}

\subsubsection*{2. Solução via Anamorfismo: |bft|}
A segunda solução aborda a BFS como um processo de produção de uma lista a partir de um estado dinâmico. 
O estado interno é definido como uma \textbf{floresta} (|State = [BTree a]|), que assume o papel de uma 
fila de espera (\textit{FIFO}). 

A função |bft| define-se como um anamorfismo de listas, cujo comportamento é regido pelo gene |genebft|:

\begin{code}
bft :: BTree a -> [a]
bft = anaList genebft . singl 
\end{code}

O diagrama seguinte ilustra a transição de estados e a geração da sequência de visita:

\[
\centerline{
    \xymatrix@@C=2.5cm@@R=2cm{
        |BTree A| 
            \ar[r]^-{|singl|} 
            \ar[dr]_-{|bft|}
        & |[BTree A]| 
            \ar[d]^-{|anaList genebft|}
            \ar[r]^-{|genebft|} 
        & |1 + A >< [BTree A]| 
            \ar[d]^-{|id + id >< (anaList genebft)|} 
        \\
        & |[A]| 
            \ar@@/^0.5cm/[r]^-{|outList|} 
        & |1 + A >< [A]| \ar@@/^0.5cm/[l]^-{|inList|}
    }
}
\]

\paragraph{Análise do gene |genebft|:}
O gene estabelece a gestão da fila de modo a assegurar a ordem de visita transversal:
\begin{enumerate}
    \item \textbf{Condição de Paragem:} Perante uma fila vazia, o anamorfismo termina (|Left ()|).
    \item \textbf{Processamento da Cabeça:} A análise da primeira árvore da fila determina o passo seguinte:
    \begin{itemize}
        \item Caso se trate de uma árvore vazia (|Empty|), o processo ignora este elemento e prossegue 
        de forma recursiva com o restante conteúdo da fila.
        \item Caso se trate de um nó (|Node (a, (l, r))|), o valor |a| é emitido para a estrutura de saída. 
        As sub-árvores |l| e |r| são obrigatoriamente adicionadas ao \textbf{fim} da fila.
    \end{itemize}
\end{enumerate}

Esta inserção no final da estrutura (concatenação) garante a semântica de uma fila de espera, o que 
assegura a visita de todos os nós de um nível $k$ antes do início do processamento de qualquer nó 
pertencente ao nível $k+1$.

A implementação do gene |genebft| em Haskell reflete esta lógica de manipulação de estados:

\begin{code}
genebft :: [BTree a] -> Either () (a, [BTree a])
genebft [] = Left ()
genebft (h:t) = case outBTree h of
    Left () -> genebft t 
    Right (a, (l, r)) -> Right (a, t ++ [l, r])
\end{code}

\subsubsection*{Exemplo de Execução e Verificação}

Para a validação da equivalência funcional entre o catamorfismo (|levels|) e o anamorfismo (|anaList genebft|), 
procede-se à análise da execução sobre a árvore de teste $|t1|$, representada por:

\begin{center}
\begin{tikzpicture}[level distance=1.2cm,
  level 1/.style={sibling distance=3cm},
  level 2/.style={sibling distance=1.5cm},
  level 3/.style={sibling distance=0.8cm},
  nodes={circle, draw, minimum size=0.7cm},
  nil/.style={rectangle, draw, minimum size=0.4cm, inner sep=2pt, font=\scriptsize}]
  
  \node {5}
    child {node {3}
      child {node {1}
        child {node[nil] {nil}}
        child {node[nil] {nil}}
      }
      child {node {4}
        child {node[nil] {nil}}
        child {node[nil] {nil}}
      }
    }
    child {node {7}
      child {node {6}
        child {node[nil] {nil}}
        child {node[nil] {nil}}
      }
      child {node {8}
        child {node[nil] {nil}}
        child {node[nil] {nil}}
      }
    };
\end{tikzpicture}
\end{center}

\paragraph{Catamorfismo (|levels|):}

A abordagem catamórfica processa a árvore de forma \textit{bottom-up}. 
O gene |glevels| combina a raiz de cada sub-árvore com o resultado da fusão dos níveis dos seus ramos. 
Esta fusão é efetuada pela função |mergeLevels|, que aglutina listas de elementos ao mesmo nível de profundidade:

\begin{enumerate}
    \item \textbf{Processamento das Folhas (ex: nó 1):} 
    O catamorfismo recebe |Empty| em ambos os ramos, o que resulta em |([], [])|. A aplicação de |mergeLevels| produz |[]|. 
    O gene acrescenta a raiz, o que resulta em |[1] : [] = [[1]]|.
    
    \item \textbf{Nível Intermédio (ex: nó 3):} 
    As sub-árvores esquerda e direita já foram processadas para |[[1]]| e |[[4]]|, respetivamente.
    \begin{itemize}
        \item |mergeLevels ([[1]], [[4]])| $\to$ |[[1, 4]]|.
        \item O gene prefixa a raiz |3|: |[3] : [[1, 4]]| $\to$ |[[3], [1, 4]]|.
    \end{itemize}

    \item \textbf{Nível Intermédio (ex: nó 7):} 
    De forma análoga, com os resultados |[[6]]| e |[[8]]|:
    \begin{itemize}
        \item |mergeLevels ([[6]], [[8]])| $\to$ |[[6, 8]]|.
        \item O gene prefixa a raiz |7|: |[7] : [[6, 8]]| $\to$ |[[7], [6, 8]]|.
    \end{itemize}

    \item \textbf{Resultado Final (|levels t1|):} 
    A raiz principal |5| combina os resultados dos dois ramos anteriores:
    \begin{itemize}
        \item |mergeLevels ([[3], [1, 4]], [[7], [6, 8]])| $\to$ |[[3, 7], [1, 4, 6, 8]]|.
        \item O gene finaliza com a raiz |5|: |[5] : [[3, 7], [1, 4, 6, 8]]| $\to$ |[[5], [3, 7], [1, 4, 6, 8]]|.
    \end{itemize}
\end{enumerate}
A aplicação final de |concat| lineariza esta estrutura estratificada e produz a sequência: |[5, 3, 7, 1, 4, 6, 8]|.

\paragraph{Anamorfismo (|anaList bft|):}
O anamorfismo opera de forma iterativa sobre uma fila de espera (\textit{FIFO}), onde o estado é 
representado por uma floresta (|State = [BTree a]|). 
O processo desenrola-se através do consumo sucessivo do primeiro elemento da fila e da injeção dos seus 
descendentes no final da mesma:

\begin{enumerate}
    \item \textbf{Estado Inicial (Fila):} |[t1]|
        \begin{itemize}
            \item O gene analisa a raiz de |t1| (valor |5|) e emite-a para a lista de saída.
            \item As sub-árvores |t3| (esquerda) e |t7| (direita) são colocadas no fim da fila (vazia nesta fase).
        \end{itemize}

    \item \textbf{Estado 1 (Fila):} |[t3, t7]|
        \begin{itemize}
            \item Consumo de |t3| (valor |3|).
            \item Os descendentes |t1| e |t4| são concatenados ao final da fila existente (|t7|).
            \item \textbf{Nova Fila:} |[t7, t1, t4]|.
        \end{itemize}

    \item \textbf{Estado 2 (Fila):} |[t7, t1, t4]|
        \begin{itemize}
            \item Consumo de |t7| (valor |7|).
            \item Os descendentes |t6| e |t8| são colocados após |t4|.
            \item \textbf{Nova Fila:} |[t1, t4, t6, t8]|.
        \end{itemize}

    \item \textbf{Estados Seguintes (Folhas):}
        A fila contém agora apenas nós cujos descendentes são |Empty|.
        \begin{itemize}
            \item Para cada nó (|1, 4, 6, 8|), o valor é emitido e dois elementos |Empty| são adicionados à fila.
            \item Perante um elemento |Empty|, o gene descarta-o e prossegue de imediato para o próximo nó, sem emissão de valor, 
            até à exaustão total da fila.
        \end{itemize}
\end{enumerate}

O resultado final é a sequência linear |[5, 3, 7, 1, 4, 6, 8]|, onde a ordem de emissão corresponde estritamente 
à profundidade dos nós na árvore original.

\paragraph{Conclusão da Verificação:}
A análise demonstra que ambos os processos convergem para o mesmo resultado linear. 
Enquanto o catamorfismo organiza a informação por níveis de profundidade antes da junção, o anamorfismo explora a 
árvore através da gestão dinâmica de uma fila. Esta equivalência valida a complementaridade entre a desconstrução 
estrutural e a geração comportamental.

\subsubsection*{Validação exaustiva}

Para validar a robustez da implementação, comparamos os resultados produzidos por |bfsLevels| e |bft| com os valores 
esperados para as árvores de teste |t1| a |t5|:

\begin{code}
exp_t1 :: [Int]
exp_t1 = [5,3,7,1,4,6,8]

exp_t2 :: [Int]
exp_t2 = [1,2,3,4,5,6,7]

exp_t3 :: [Char]
exp_t3 = ['A','B','E','C','D']

exp_t4 :: [Char]
exp_t4 = ['A','B','C','D']

exp_t5 :: [Int]
exp_t5 = [1,2,3,4,5,6]

test_t1 = bfsLevels t1 == exp_t1 && bft t1 == exp_t1
test_t2 = bfsLevels t2 == exp_t2 && bft t2 == exp_t2
test_t3 = bfsLevels t3 == exp_t3 && bft t3 == exp_t3
test_t4 = bfsLevels t4 == exp_t4 && bft t4 == exp_t4
test_t5 = bfsLevels t5 == exp_t5 && bft t5 == exp_t5

tests_P1 :: [(String,Bool)]
tests_P1 =
  [ ("t1", test_t1)
  , ("t2", test_t2)
  , ("t3", test_t3)
  , ("t4", test_t4)
  , ("t5", test_t5)
  ]

all_tests_P1 :: Bool
all_tests_P1 = and (map snd tests_P1)
\end{code}

A execução em GHCi confirma o sucesso de todos os testes, resultando em True para a função |all_tests_P1|. 
Desta forma, verifica-se que ambas as definições produzem a travessia breadth-first correta.

% PROBLEMA 2

\subsection*{Problema 2}

\subsubsection*{Introdução e objetivo}
Pretende-se justificar (derivar) a função Haskell fornecida no enunciado a partir da série de Taylor de
\(\sinh x\), de modo a explicar o significado de cada componente do estado e provar, por indução, que a função
calculada coincide com as somas parciais dessa série.

A função dada é:

\begin{spec}
f x = wrapper . worker where
         wrapper = head
         worker 0 = start x
         worker(n+1) = loop x (worker n)

loop x    [s,         h,       k,     j,     m     ] =
          [h / k + s, x^2 * h, k * j, j + m, m + 8 ]

start x = [x,         x^3,     6,     20,    22    ]
\end{spec}

\noindent Como |wrapper = head|, temos imediatamente:
\[
f\ x\ n \;=\; |head|(|worker|\ n).
\]
Logo, basta compreender e justificar o primeiro componente do estado \([s,h,k,j,m]\).

%--------------------------------------------------------------------
\subsubsection*{1. Série de Taylor de \(\sinh x\) e somas parciais}
Recorde-se a expansão de Taylor em torno de \(0\):
\[
\sinh x \;=\; x + \frac{x^3}{3!} + \frac{x^5}{5!} + \frac{x^7}{7!} + \cdots
\;=\;\sum_{i=0}^{\infty}\frac{x^{2i+1}}{(2i+1)!}.
\]
A aproximação com \(n\) iterações (ou \(n\) passos de refinamento) é a soma parcial
\[
S_n(x) \;=\;\sum_{i=0}^{n}\frac{x^{2i+1}}{(2i+1)!}.
\]
O objetivo passa a ser provar que:
\[
f\ x\ n \;=\; S_n(x).
\]

%--------------------------------------------------------------------
\subsubsection*{2. Ideia do algoritmo: acumular a soma e gerar o próximo termo}
O cálculo de \(S_n(x)\) pode ser visto como um processo iterativo:

\begin{itemize}
\item manter um acumulador \(s\) com a soma já obtida;
\item em cada passo adicionar o próximo termo da série;
\item atualizar esse “próximo termo” para o passo seguinte.
\end{itemize}

No código, essa lógica está explícita na primeira componente de \texttt{loop}:
\[
s' \;=\; s + \frac{h}{k}.
\]
Isto sugere a interpretação:
\[
\frac{h}{k} \text{ é o próximo termo a acrescentar à soma } s.
\]

Assim, o estado \([s,h,k,j,m]\) foi escolhido para que:
\begin{enumerate}
\item \(s\) seja a soma parcial já computada;
\item \(\frac{h}{k}\) seja o termo seguinte da série;
\item os restantes componentes (\(j,m\)) permitam atualizar \(k\) (o fatorial) de forma barata,
sem recalcular fatoriais do início.
\end{enumerate}

%--------------------------------------------------------------------
\subsubsection*{3. Derivação das Leis de Atualização}
Para justificar formalmente os valores do estado e as constantes, derivam-se as 
recorrências matematicamente.

Seja \(t_i = \frac{x^{2i+1}}{(2i+1)!}\) o termo geral. Calcula-se a razão entre o termo seguinte (\(t_{i+1}\)) e o atual (\(t_i\)) 
dividindo as frações correspondentes:

\begin{align*}
\frac{t_{i+1}}{t_i}
&= \frac{ \frac{x^{2i+3}}{(2i+3)!} }{ \frac{x^{2i+1}}{(2i+1)!} }
 = \frac{x^{2i+3}}{(2i+3)!} \times \frac{(2i+1)!}{x^{2i+1}}
 = \frac{x^{2i+3}}{x^{2i+1}} \times \frac{(2i+1)!}{(2i+3)(2i+2)(2i+1)!} \\
&= x^{(2i+3)-(2i+1)} \times \frac{1}{(2i+3)(2i+2)}
 = \frac{x^2}{(2i+3)(2i+2)}.
\end{align*}

Desta razão resulta a relação de recorrência utilizada no algoritmo:
\[
t_{i+1} \;=\; t_i \times \frac{x^2}{(2i+3)(2i+2)}.
\]

Daqui resulta a necessidade de manter o numerador \(h\) e o denominador \(k\) separadamente. 
Aplicando a Lei da Recursividade Mútua (Lei de Fokkinga), decompõe-se a recorrência nas atualizações 
do estado:
\begin{itemize}
    \item \(h_{next} = h \cdot x^2\) (o numerador multiplica-se por \(x^2\));
    \item \(k_{next} = k \cdot (2i+3)(2i+2)\) (o denominador multiplica-se pelo polinómio quadrático).
\end{itemize}

\paragraph{Otimização por Diferenças Finitas (Cálculo de $j$ e $m$)}
Para evitar multiplicações complexas dependentes do índice \(i\) no cálculo de \((2i+3)(2i+2)\), 
aplica-se o Cálculo de Diferenças Finitas. O fator de atualização do fatorial para o passo seguinte 
implica o polinómio:
\[ P(n) = (2n+5)(2n+4) = 4n^2 + 18n + 20 \]
\emph{(Nota: Substitui-se \(i\) por \(n+1\), logo os fatores tornam-se \(2(n+1)+3 = 2n+5\) e \(2(n+1)+2 = 2n+4\)).}

Calculam-se as diferenças sucessivas para reduzir a avaliação polinomial a somas simples:
\begin{enumerate}
    \item \textbf{Primeira Diferença (\(m_n\)):}
    \[ m_n = P(n+1) - P(n) = (4(n+1)^2 + 18(n+1) + 20) - (4n^2 + 18n + 20) = 8n + 22. \]
    \item \textbf{Segunda Diferença (\(\Delta m\)):}
    \[ \Delta m = m_{n+1} - m_n = (8(n+1)+22) - (8n+22) = \mathbf{8}. \]
\end{enumerate}

Visto que a segunda diferença é constante, justifica-se a introdução das variáveis auxiliares 
\(j\) (o valor acumulado do polinómio) e \(m\) (a primeira diferença), resultando nas atualizações 
lineares presentes no código:
\[ j' = j + m \quad \text{e} \quad m' = m + 8. \]

\paragraph{Derivação dos Valores Iniciais (|start|):}
Para a iteração \(n=0\), o estado deve conter a soma atual e preparar o termo seguinte (devido à 
natureza \emph{look-ahead} do ciclo, que calcula valores para a iteração \(n+1\)):

\begin{itemize}
    \item \textbf{Soma atual (\(s_0\)):} Corresponde ao primeiro termo da série (\(i=0\)):
    \[ s_0 = \frac{x^{2(0)+1}}{(2(0)+1)!} = \frac{x^1}{1!} = x. \]
    
    \item \textbf{Próximo Numerador (\(h_0\)) e Denominador (\(k_0\)):} Correspondem ao termo para \(i=1\) (o 
    termo que será somado na próxima execução do corpo do ciclo):
    \[ t_1 = \frac{x^{2(1)+1}}{(2(1)+1)!} = \frac{x^3}{3!} = \frac{x^3}{6}. \]
    Logo, \(h_0 = x^3\) e \(k_0 = 6\).

    \item \textbf{Variáveis Auxiliares (\(j_0, m_0\)):} Calculadas pelas fórmulas de diferenças finitas deduzidas acima, para \(n=0\):
    \[ j_0 = P(0) = 4(0)^2 + 18(0) + 20 = \mathbf{20}. \]
    \[ m_0 = 8(0) + 22 = \mathbf{22}. \]
\end{itemize}

Este cálculo fundamenta a origem exata de todos os componentes do vetor inicial |[x, x^3, 6, 20, 22]|.

%--------------------------------------------------------------------
\subsection*{4. Invariante de correção}
Define-se o invariante central que justifica a devolução das somas parciais corretas por parte da função |head|.

\paragraph{Invariante \(I(n)\).}
Se $|worker|\ n = [s,h,k,j,m]$, então:
\begin{align*}
(\mathrm{I1})\quad & s = \sum_{i=0}^{n}\frac{x^{2i+1}}{(2i+1)!} = S_n(x), \\
(\mathrm{I2})\quad & \frac{h}{k} = \frac{x^{2n+3}}{(2n+3)!}
\quad\text{(o termo seguinte a acrescentar)}.
\end{align*}

Note-se que (I1) é suficiente para a prova, uma vez que:
\[
f\ x\ n = |head|(|worker|\ n) = s = S_n(x).
\]

%--------------------------------------------------------------------
\subsection*{5. Prova por indução sobre \(n\)}

\paragraph{Base (\(n=0\)).}
Temos $|worker|\ 0 = |start|\ x = [x,x^3,6,20,22]$.
Logo:
\[
s=x = \frac{x^1}{1!} = S_0(x),
\]
pelo que (I1) se verifica.

Além disso:
\[
\frac{h}{k}=\frac{x^3}{6}=\frac{x^3}{3!},
\]
valor que coincide com o termo seguinte a \(S_0\). Logo, (I2) também se verifica.

\paragraph{Passo indutivo (\(n \to n+1\)).}
Sob a hipótese de que \(I(n)\) é verdadeiro para $|worker|\ n = [s,h,k,j,m]$, tem-se:
\[
|worker|(n+1) = |loop|\ x\ [s,h,k,j,m] = [s',h',k',j',m'].
\]
Pelo código:
\[
s' = s + \frac{h}{k}.
\]
Com recurso a (I1) e (I2), obtém-se:
\[
s' = S_n(x) + \frac{x^{2n+3}}{(2n+3)!} = S_{n+1}(x),
\]
logo (I1) é válido para \(n+1\).

Para (I2), o programa atualiza $h' = x^2 h$, o que faz avançar o numerador do termo seguinte de $x^{2n+3}$ para $x^{2n+5}$. 
Por construção dos componentes $(k,j,m)$, o valor $k'$ passa do fatorial ímpar $(2n+3)!$ para $(2n+5)!$. Assim:
\[
\frac{h'}{k'}=\frac{x^{2n+5}}{(2n+5)!},
\]
ou seja, exatamente o termo seguinte a $S_{n+1}(x)$. Portanto, $I(n+1)$ verifica-se.

Conclui-se por indução que \(I(n)\) é válido para todo \(n\). Consequentemente:
\[
f\ x\ n = |head|(|worker|\ n) = S_n(x),
\]
isto é, |f x n| calcula a aproximação de $\sinh x$ por $n$ iterações (somas parciais da série).

%--------------------------------------------------------------------
\subsubsection*{6. Mini-exemplo numérico: \(x=1\) (primeiros estados)}
Para \(x=1\), tem-se \(1^2=1\). Logo, $h$ mantém-se em $1$ com o arranque em $h=1^3=1$.
O denominador $k$ (fatoriais ímpares) sofre as alterações principais.

\paragraph{Estado 0:}
\[
|worker|\ 0 = [1,\;1,\;6,\;20,\;22]
\]
\[
f\ 1\ 0 = s = 1 \quad\text{(ou seja, } 1 = 1/1! \text{)}.
\]

\paragraph{Estado 1:}
\[
s' = 1 + 1/6 = 7/6 \approx 1.166666...
\]
\[
|worker|\ 1 = [7/6,\;1,\;120,\;42,\;30]
\]
Logo:
\[
f\ 1\ 1 = 7/6 = 1 + 1/3!
\]

\paragraph{Estado 2:}
\[
s'' = 7/6 + 1/120 = 141/120 = 47/40 = 1.175
\]
\[
|worker|\ 2 = [47/40,\;1,\;5040,\;72,\;38]
\]
Logo:
\[
f\ 1\ 2 = 47/40 = 1 + 1/3! + 1/5!
\]

%--------------------------------------------------------------------
\subsubsection*{Conclusão}
A função \texttt{worker} mantém um estado \([s,h,k,j,m]\) onde:
\begin{itemize}
\item \(s\) é a soma parcial já acumulada;
\item \(\frac{h}{k}\) é o próximo termo da série a adicionar;
\item as atualizações de \(h\) e de \(k\) garantem a progressão para o termo seguinte sem 
recomputações dispendiosas.
\end{itemize}
Como \texttt{f x n = head (worker n)}, conclui-se que \texttt{f x n} devolve a aproximação de \(\sinh x\)
por \(n\) iterações da série de Taylor.

% PROBLEMA 3

\subsection*{Problema 3}

\subsubsection*{Introdução: Streams e Processos Infinitos}

Ao contrário das estruturas de dados finitas, os \textit{Streams} modelam sequências infinitas de dados, 
fundamentais na modelação de processos reativos e fluxos contínuos. No contexto de Haskell, 
esta estrutura define-se pelo tipo indutivo:

\begin{spec}
data Stream a = Cons (a, Stream a) deriving Show
\end{spec}

A semântica desta estrutura é ditada pela assinatura do functor $FX = A \times X$. 
O correspondente destrutor, ou estrutura, define-se por:

\begin{spec}
outStream (Cons (x,xs)) = (x,xs)
\end{spec}

A construção de instâncias desta estrutura realiza-se via anamorfismos (|anaStream|), que permitem a 
expansão de um estado inicial num fluxo infinito através de um gene gerador $g$:

\begin{spec}
anaStream g = Cons . (id >< (anaStream g)) . g
\end{spec}

O presente problema foca-se na implementação de uma fusão justa (|fair_merge|). O objetivo é a 
garantia de que os elementos de dois fluxos de entrada sejam intercalados de forma 
estrita, o que previne fenómenos de inanição (|starvation|) e assegura a vivacidade do sistema.

% --- COMPATIBILIDADE LOCAL ---
\providecommand{\timesi}{\times}
% ------------------------------

\subsubsection*{Dedução da Lei de Recursividade Mútua (Dual)}

A modelação da alternância estrita entre dois fluxos independentes sugere uma dependência mútua. 
Para a formalização desta dinâmica sem recurso a recursividade explícita, 
aplica-se a \textbf{Lei de Fokkinga (Dual)}, ou Lei da Recursividade Mútua para Anamorfismos.

O objetivo desta derivação consiste em determinar o gene $g$ que satisfaz a 
definição de |fair_merge'| como um anamorfismo. Partimos da premissa:

\begin{spec}
fair_merge' = ana g
\end{spec}

Considera-se a definição recursiva original fornecida no enunciado, onde se observa que as funções 
|h| e |k| dependem ciclicamente uma da outra para o processamento do elemento seguinte:

\begin{spec}
fair_merge :: Either (Stream a, Stream a) (Stream a, Stream a) -> Stream a
fair_merge = either h k where
    h (Cons(x,xs), y) = Cons(x , k(xs,y))
    k (x, Cons(y,ys)) = Cons(y , h(x,ys))
\end{spec}

A derivação formal, baseada na propriedade universal dos anamorfismos e nas leis de fusão do coproduto do 
cálculo de programas, segue abaixo para provar que $|either h k = ana (either g1 g2)|$:

\begin{eqnarray*}
\start
|
    r = ana gene 
|
\just\equiv{ r = [h,k]  e gene = [g1, g2]}
|
    either h k = ana (either g1 g2)
|
\just\equiv{ Universal - Ana (56) }
|
    out . (either h k) = F (either h k) . (either g1 g2)
|
\just\equiv{ Fusão-+ (21) }
|
    either (out . h) (out . k) = either (F (either h k) . g1) (F (either h k) . g2)
|
\just\equiv{ Eq-+ (28) }
|   
    lcbr(
        out . h = F (either h k) . g1
    )(
        out . k = F (either h k) . g2
    )
|
\just\equiv{ F (either h k) = id x (either h k) }
|
    lcbr(
        out . h = (id >< either h k) . g1 
    )(
        out . k = (id >< either h k) . g2
    )
|
\qed
\end{eqnarray*}

\subsubsection*{Análise do Sistema e Estratégia de Implementação}

O sistema de equações resultante estabelece as condições de correção para o gene do anamorfismo. 
As igualdades obtidas indicam que cada componente do gene ($g_1$ e $g_2$) deve produzir 
um par contendo o valor de saída imediata e o estado necessário para a iteração seguinte.

Fundamentalmente, este resultado demonstra que, para manter a alternância estrita, o gene deve 
realizar uma "troca de prioridade": sempre que a componente $g_1$ (vinda de $h$) consome 
um elemento, o estado sucessor deve ser injetado de forma a que a componente $g_2$ (vinda de $k$) 
seja a próxima a ser executada. Este comportamento é modelável através de uma 
máquina de estados finita:

\begin{center}
\scalebox{0.85}{
\xy
\xymatrix@@C=3.5cm{
    {\begin{matrix} \text{\textbf{Estado Left}} \\ (s_1, s_2) \end{matrix}} 
        \ar@@/^1.5cm/[r]^-{g_1: \text{consome } s_1 \to i_2} 
    & 
    {\begin{matrix} \text{\textbf{Estado Right}} \\ (tail(s_1), s_2) \end{matrix}} 
        \ar@@/^1.5cm/[l]^-{g_2: \text{consome } s_2 \to i_1}
}
\endxy
}
\end{center}

\paragraph{Análise do Fluxo de Dados:}

Nesta estrutura, as variáveis representam o estado dos fluxos em cada passo:
\begin{itemize}
    \item No \textbf{Estado Left}, a função $g_1$ extrai a cabeça de $s_1$ e preserva $s_2$ intacto, 
    passando o par resultante para o lado direito através da injeção |i2|.
    \item No \textbf{Estado Right}, a prioridade inverte-se: $g_2$ consome a cabeça de $s_2$ e devolve 
    o controlo ao lado esquerdo via |i1|.
\end{itemize}
Esta alternância contínua impede a inanição (|starvation|) de qualquer um dos fluxos, resultando numa 
fusão perfeitamente equilibrada.

Desta forma, os diagramas categoriais seguintes detalham a composição interna de cada componente do gene. 
Estes diagramas seguem rigorosamente as definições de $g_1$ e $g_2$ e evidenciam a desconstrução do estado para a alternância de prioridade.

\paragraph{Gene $g_1$ (Processamento de $s_1$):}
Este componente extrai a cabeça do primeiro fluxo e encapsula o estado sucessor com a injeção $i_2$. 
Este passo transfere a prioridade para o segundo fluxo na iteração seguinte.

%format i1 = "\mathit{i}_1"
%format i2 = "\mathit{i}_2"
\begin{center}
\scalebox{0.9}{
\xymatrix@@C=3.5cm@@R=1.5cm{
    |Stream A >< Stream A| \ar[dr]_{g_1} \ar[r]^-{|split (p1 . out . p1) (split (p2 . out . p1) p2)|} & 
    |A >< (Stream A >< Stream A)| \ar[d]^-{|id >< i2|} \\
    & |A >< ((Stream A >< Stream A) + (Stream A >< Stream A))|
}
}
\end{center}

\paragraph{Gene $g_2$ (Processamento de $s_2$):}
Simetricamente, este componente consome o elemento do segundo fluxo e utiliza a injeção $i_1$. Desta forma, devolve a prioridade ao primeiro fluxo.

\begin{center}
\scalebox{0.9}{
\xymatrix@@C=3.5cm@@R=1.5cm{
    |Stream A >< Stream A| \ar[dr]_{g_2} \ar[r]^-{|split (p1 . out . p2) (split p1 (p2 . out . p2))|} & 
    |A >< (Stream A >< Stream A)| \ar[d]^-{|id >< i1|} \\
    & |A >< ((Stream A >< Stream A) + (Stream A >< Stream A))|
}
}
\end{center}

\subsubsection*{O Diagrama do Anamorfismo}

O processo pode ser visualizado através do seguinte diagrama comutativo. 
O gene $g = [g_1, g_2]$ mapeia o estado atual (uma soma de pares de streams) no 
par $(\text{valor}, \text{próximo estado})$, onde o functor $F f = id \times f$ 
assegura a continuidade da expansão infinita:

%format inStream = "\mathsf{in}"
\begin{center}
\scalebox{0.9}{
\[
\centerline{
    \xymatrix@@C=2.5cm@@R=2cm{
        |(Stream A >< Stream A) + (Stream A >< Stream A)| 
            \ar[r]^-{|either g1 g2|} 
            \ar[d]_-{|fair_merge'|} 
        & |A >< ((Stream A >< Stream A) + (Stream A >< Stream A))| 
            \ar[d]^-{|id >< fair_merge'|} 
        \\
        |Stream A| \ar@@/^0.5cm/[r]^-{|outStream|}
        & |A >< Stream A| 
            \ar@@/^0.5cm/[l]^-{|inStream|}
    }
}
\]
}
\end{center}

Este diagrama explicita a semântica estrutura: o anamorfismo 
"desenrola" o estado inicial aplicando o gene sucessivamente. A "justiça" da 
fusão não reside na estrutura do anamorfismo em si, mas na definição interna de $g_1$ e $g_2$, 
que forçam a alternância entre as injeções |i1| e |i2| do tipo soma.

\subsubsection*{Implementação em Haskell}

A tradução deste formalismo para código Haskell resulta na seguinte definição \textit{point-free}, 
onde a separação de responsabilidades é total: o combinador |anaStream| gere a 
recursividade infinita, enquanto o gene $g$ gere a lógica de intercalação.

\begin{code}
fair_merge' :: Either (Stream a, Stream a) (Stream a, Stream a) -> Stream a
fair_merge' = anaStream g
    where 
        g = either g1 g2
        g1 = (id >< i2) . split (p1 . outStream . p1) (split (p2 . outStream . p1) p2)
        g2 = (id >< i1) . split (p1 . outStream . p2) (split p1 (p2 . outStream . p2))
\end{code}

\subsubsection*{Validação de Resultados e Simulação}

Para validar a implementação do |fair_merge'|, utilizam-se fluxos aritméticos gerados via anamorfismo. 
Define-se a função auxiliar |nextS| para calcular o par (valor, próximo estado), o que evita conflitos de formatação:

\begin{code}
nextS n = (n, n + 4)

s1 = anaStream nextS 1 -- [1, 5, 9, 13, ...]
s2 = anaStream nextS 2 -- [2, 6, 10, 14, ...]
s3 = anaStream nextS 3 -- [3, 7, 11, 15, ...]
s4 = anaStream nextS 4 -- [4, 8, 12, 16, ...]

takeS 0 _ = []
takeS n (Cons (x, xs)) = x : takeS (n-1) xs
\end{code}

A função |takeS| extrai os primeiros $n$ elementos de um |Stream|, permitindo a observação finita dos resultados. 

A simulação adota uma estrutura em cascata: fundem-se dois pares de fluxos e, posteriormente, combinam-se os 
resultados dessas operações. Esta composição verifica a robustez da máquina de estados perante fluxos aninhados:

\begin{code}
f1 = takeS 10 (fair_merge (i1 (fair_merge (i1 (s1, s3)), fair_merge (i1 (s2, s4)))))
f2 = takeS 10 (fair_merge' (i1 (fair_merge' (i1 (s1, s3)), fair_merge' (i1 (s2, s4)))))
\end{code}

\noindent \textbf{Análise do Resultado da Simulação:}

Ao executar |f1| ou |f2|, obtém-se, em ambos os casos, a sequência:
| [1, 2, 3, 4, 5, 6, 7, 8, 9, 10] |

Este resultado comprova a correção do operador através da seguinte lógica de junção:
\begin{itemize}
    \item A fusão da esquerda combina |s1| e |s3|, intercalando os seus elementos para formar a sequência de ímpares: $[1, 3, 5, 7, 9, \dots]$.
    \item A fusão da direita combina |s2| e |s4|, intercalando os seus elementos para formar a sequência de pares: $[2, 4, 6, 8, 10, \dots]$.
    \item A fusão final intercala estas duas sequências (ímpares e pares), o que resulta na sequência numérica contínua.
\end{itemize}

A igualdade verificada entre |f1| e |f2| confirma que a implementação baseada no gene respeita a semântica de fusão justa, 
garantindo que nenhum fluxo é preferido na estrutura de cascata.

% PROBLEMA 4

\subsection*{Problema 4}

\subsubsection*{Introdução e Modelação}
O problema proposto consiste na modelação de um sistema de comunicação falível, um telegrafista 
com uma avaria intermitente, através de um catamorfismo probabilístico. O objetivo é calcular a 
distribuição de probabilidades das mensagens recebidas dada a frase original "Vamos atacar hoje".

O comportamento do sistema caracteriza-se por dois tipos de falha independente:
\begin{enumerate}
    \item \textbf{Falha na transmissão de palavras:} Cada palavra tem uma probabilidade de $5\%$ de se 
    perder durante a transmissão.
    \item \textbf{Falha na terminação:} O código de fim de mensagem (|stop|) deve ser enviado no final, 
    mas falha em $10\%$ das vezes.
\end{enumerate}

Para modelar este comportamento, recorre-se a uma função de ordem superior, |pcataList|, que 
generaliza o conceito de \textit{fold} para o contexto do mónade de probabilidades |Dist|.

\subsubsection*{1. O Catamorfismo Probabilístico (|pcataList|)}

O combinador |pcataList| define-se como um catamorfismo sobre listas cujo gene produz resultados 
probabilísticos. Matematicamente, enquanto um catamorfismo de listas tradicional tem o 
tipo $(1 + A \times B \to B) \to [A] \to B$, a versão probabilística opera no mónade |Dist|, 
tendo o tipo $(1 + A \times B \to \text{Dist } B) \to [A] \to \text{Dist } B$.

A implementação reflete a estrutura algébrica da lista (soma de produtos), tratando explicitamente o 
caso vazio (injeção $i_1$) e o caso não-vazio (injeção $i_2$):

\begin{code}
pcataList :: (Either () (a, b) -> Dist b) -> [a] -> Dist b
pcataList g []     = g (i1 ())
pcataList g (a:as) = do
  b <- pcataList g as
  g (i2 (a,b))
\end{code}

O funcionamento estrutural deste combinador visualiza-se através do seguinte diagrama comutativo:

\[
\centerline{
    \xymatrix@@C=3.5cm@@R=2cm{
        {|[A]|}
            \ar[d]_-{|pcataList g|}
            \ar@@/^0.5cm/[r]^-{|outList|}
        &
            {|1 + A >< [A]|}
            \ar[d]^-{|id + id >< pcataList g|}
            \ar@@/^0.5cm/[l]^-{|inList|}
        \\
        {|Dist B|}
        &
            {|1 + A >< Dist B|}
            \ar[l]^-{|g'|}
    }
}
\]

Neste esquema, a seta inferior $g'$ denota a aplicação monádica do gene. Ao contrário de um 
catamorfismo determinístico, a recursão devolve uma distribuição (|Dist B|). Consequentemente, $g'$ 
incorpora a lógica de sequenciação (o operador \textit{bind} implícito na notação \textit{do}), 
extraindo o resultado da cauda antes da aplicação efetiva de $g$.

O processo de avaliação detalhado ocorre da seguinte forma:
\begin{itemize}
    \item \textbf{Caso Base (|[]|):} O catamorfismo atinge o fim da lista e invoca o gene com $i_1 ()$, 
    permitindo ao gene decidir como terminar a estrutura (neste caso, decidindo se envia "stop").
    \item \textbf{Passo Recursivo (|a:as|):}
    \begin{enumerate}
        \item Avalia-se primeiramente a cauda da lista (|as|), o que resulta numa distribuição de 
        possíveis caudas processadas (|b|).
        \item Para cada resultado possível dessa distribuição, aplica-se o gene à cabeça atual (|a|) 
        emparelhada com esse resultado ($i_2 (a,b)$).
        \item O mónade |Dist| encarrega-se de combinar as probabilidades resultantes (multiplicação de 
        probabilidades para eventos independentes).
    \end{enumerate}
\end{itemize}

\subsubsection*{2. Definição do Gene Probabilístico}

O gene captura o comportamento específico da falha. Este recebe um co-produto que representa os dois 
estados possíveis da travessia da lista. A função |gene| define-se pelo combinador |either|, 
separando a lógica de terminação da lógica de transmissão:

\begin{code}
gene :: Either () (String, [String]) -> Dist [String]
gene = either base step
  where
    base :: () -> Dist [String]
    base () = D [ (["stop"], 0.90), ([], 0.10) ]

    step :: (String, [String]) -> Dist [String]
    step (w, ws) = D [ (ws, 0.05), (w:ws, 0.95) ]
\end{code}

A lógica de decisão pode ser visualizada na árvore de decisão probabilística (Figura \ref{fig:gene_tree}).

\begin{figure}[h]
    \centering
    \begin{tikzpicture}[
        node distance=2.5cm,
        level 1/.style={sibling distance=7cm, level distance=2cm},
        level 2/.style={sibling distance=3.5cm, level distance=2.5cm},
        edge from parent/.style={draw, ->},
        every node/.style={align=center, font=\small}
    ]
    \node[draw, circle, inner sep=3pt, fill=gray!10] (root) {\textbf{Gene}\\ $1 + A \times B$}
        child { node[draw, rectangle, rounded corners] (base) {\textbf{Caso Base ($i_1$)}\\ Fim da Lista}
            child { node[draw=none] {Sucesso (90\%)\\ \texttt{["stop"]}} edge from parent node[left, font=\footnotesize] {$s=0.9$} }
            child { node[draw=none] {Falha (10\%)\\ \texttt{[]}} edge from parent node[right, font=\footnotesize] {$f=0.1$} }
            edge from parent node[above left] {$i_1$}
        }
        child { node[draw, rectangle, rounded corners] (rec) {\textbf{Passo Recursivo ($i_2$)}\\ Palavra $w$, Resto $ws$}
            child { node[draw=none] {Sucesso (95\%)\\ \texttt{w:ws}} edge from parent node[left, font=\footnotesize] {$p=0.95$} }
            child { node[draw=none] {Perda (5\%)\\ \texttt{ws}} edge from parent node[right, font=\footnotesize] {$q=0.05$} }
            edge from parent node[above right] {$i_2$}
        };
    \end{tikzpicture}
    \caption{Diagrama de decisão do Gene: bifurcação entre terminação ($i_1$) e processamento ($i_2$).}
    \label{fig:gene_tree}
\end{figure}

Assim, a função de transmissão final obtém-se pela aplicação do catamorfismo com este gene:
\begin{spec}
transmitir :: [String] -> Dist [String]
transmitir = pcataList gene
\end{spec}

\subsubsection*{3. Cálculo de Probabilidades}

Considere-se a mensagem de entrada $M =$ |["Vamos", "atacar", "hoje"]|.
Definem-se as probabilidades elementares: $p=0.95$ (manter), $q=0.05$ (perder), $s=0.90$ (stop), $f=0.10$ (sem stop).

Dado que o mónade assegura a independência estatística, calculam-se analiticamente os cenários:

\paragraph{Cenário 1: Perder "atacar"} (|["Vamos", "hoje", "stop"]|).
Eventos: "Vamos" segue ($p$), "atacar" falha ($q$), "hoje" segue ($p$), "stop" segue ($s$).
\[ P_1 = p \cdot q \cdot p \cdot s = 0.95^2 \cdot 0.05 \cdot 0.90 \approx \mathbf{4.06\%} \]

\paragraph{Cenário 2: Faltar o "stop"} (|["Vamos", "atacar", "hoje"]|).
Eventos: Três palavras seguem ($p^3$), "stop" falha ($f$).
\[ P_2 = p^3 \cdot f = 0.95^3 \cdot 0.10 \approx \mathbf{8.57\%} \]

\paragraph{Cenário 3: Perfeita} (|["Vamos", "atacar", "hoje", "stop"]|).
Eventos: Tudo segue com sucesso.
\[ P_3 = p^3 \cdot s = 0.95^3 \cdot 0.90 \approx \mathbf{77.16\%} \]

\subsubsection*{4. Validação Experimental}

A validação dos cálculos analíticos é realizada através da execução direta da função |transmitir| 
com a frase do enunciado.

\begin{code}
msg :: [String]
msg = words "Vamos atacar hoje"
\end{code}

A invocação de |transmitir msg| no interpretador gera a distribuição de todas as mensagens possíveis. 
O resultado abaixo (formatado automaticamente pela biblioteca |Probability|) apresenta os cenários 
por ordem decrescente de probabilidade:

\begin{verbatim}
*Problema4> transmitir msg
["Vamos","atacar","hoje","stop"]  77.2%
       ["Vamos","atacar","hoje"]   8.6%
        ["atacar","hoje","stop"]   4.1%
        ["Vamos","atacar","stop"]   4.1%
         ["Vamos","hoje","stop"]   4.1%
              ["Vamos","atacar"]   0.5%
...
\end{verbatim}

\noindent \textbf{Análise dos Resultados:}
Ao comparar este \textit{output} com os valores teóricos calculados na secção anterior, confirma-se a 
correção do modelo:

\begin{enumerate}
    \item O cenário de \textbf{transmissão perfeita} (|["Vamos","atacar","hoje","stop"]|) surge com 
    $77.2\%$, correspondendo ao valor calculado $P_3 \approx 77.16\%$.
    \item O cenário onde \textbf{falta o "stop"} (|["Vamos","atacar","hoje"]|) surge com $8.6\%$, 
    correspondendo a $P_2 \approx 8.57\%$.
    \item O cenário onde se \textbf{perde a palavra "atacar"} (|["Vamos","hoje","stop"]|) surge com 
    $4.1\%$, correspondendo a $P_1 \approx 4.06\%$.
\end{enumerate}

Nota: A ligeira diferença deve-se apenas ao arredondamento para uma casa decimal na visualização do 
interpretador.

%----------------- Índice remissivo (exige makeindex) -------------------------%

\printindex

%----------------- Bibliografia (exige bibtex) --------------------------------%

\bibliographystyle{plain}
\bibliography{cp2526t}

%----------------- Fim do documento -------------------------------------------%
\end{document}
