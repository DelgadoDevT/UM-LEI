/* Este documento apresenta uma solução em construção. É fornecido como 
   material de estudo. Recomenda-se que o revejam e melhorem conforme   
   forem adquirindo novos conhecimentos. */

/* 
Importa o objeto ResultadoJogada do ficheiro resultadoJogada.js e o objeto ResultadoJogo do ficheiro resultadoJogo.js.
*/
import { PlayResult } from "./playResult";
import { GameResult } from "./gameResult";


/* 
Este ficheiro exporta uma classe Jogo que representa o jogo 4 em linha.
*/
export class Game {
  static NUM_COLUMNS = 7;
  static NUM_ROWS = 6;

  board = [];      // Array de arrays com o estado do tabuleiro.
  player = false;  // Indica o jogador que tem a vez de jogar. false - jogador amarelo, true - jogador vermelho.
  winner = null;   // Indica o vencedor do jogo (cf. ResultadoJogo). null - jogo em curso.
  isOver = false;  // Indica se o jogo terminou.

  /*
  O construtor da classe Jogo inicializa o tabuleiro, o jogador a jogar e o vencedor.
  */
  constructor() {
    this.reset();
  }

  /*
  A função jogar(coluna) recebe um inteiro coluna e devolve um dos seguintes valores:
  - ResultadoJogada.ERRO_JOGOTERMINADO - O jogo já terminou.
  - ResultadoJogada.ERRO_COLUNAINVALIDA - A coluna não é válida.
  - ResultadoJogada.ERRO_COLUNACHEIA - A coluna está cheia.
  - ResultadoJogada.TERMINOU - A jogada terminou o jogo.
  - ResultadoJogada.SUCESSO - A jogada foi realizada com sucesso e o jogo continua.
  */
  play(column) {
    if (this.isOver) {
      return PlayResult.ERROR_GAME_OVER;
    }
    if (column < 0 || column >= Game.NUM_COLUMNS) {
      return PlayResult.ERROR_INVALID_COLUMN;
    }
    else if (this.board[column].length == Game.NUM_ROWS) {
      return PlayResult.ERROR_FULL_COLUMN;
    }
    else {
      this.board[column].push(this.player);  // Adiciona a jogada ao tabuleiro.
      this.player = !this.player;       // Alterna o jogador a jogar. 

      this.checkIsOver();

      return this.isOver ? PlayResult.GAME_OVER : PlayResult.SUCCESS;
    }
  }

  /*
  A função validarSeTerminou() verifica se o jogo terminou e quem é o vencedor.
  */
  checkIsOver() {
    for (let index = 0; index < this.board.length; index++) {
      const column = this.board[index];
      for (let index2 = 0; index2 < column.length; index2++) {
        // Construir a string com as jogadas da linha que começa na posição (index,index2) (nota: não existir jogada numa posição o valor será 'null'/'undefined')
        let row = this.getRowCoordinates(index, index2)
                                 .map((x) => this.getInternalCell(x[0],x[1]))
                                 .join("");
        // Se a string contiver 4 'false' seguidos, o jogador amarelo venceu. Se a string contiver 4 'true' seguidos, o jogador vermelho venceu. Caso contrário, não há vencedor.
        this.winner = row.includes("falsefalsefalsefalse") ? GameResult.YELLOW :
                        (row.includes("truetruetruetrue") ? GameResult.RED : null);
        // Se houver um vencedor, o jogo terminou.
        if (this.winner !== null) {
          this.isOver = true;
          return;
        }
        // Construir a string com as jogadas da coluna que começa na posição (index,index2)
        let column = this.getColumnCoordinates(index, index2)
                                 .map((x) => this.getInternalCell(x[0],x[1]))
                                 .join("");
        // Ver se alguém venceu na coluna.
        this.winner = column.includes("falsefalsefalsefalse") ? GameResult.YELLOW :
                        (column.includes("truetruetruetrue") ? GameResult.RED : null);
        // Se houver um vencedor, o jogo terminou.
        if (this.winner !== null) {
          this.isOver = true;
          return;
        }
        // Construir a string com as jogadas da diagonal superior que começa na posição (index,index2) 
        let upperDiagonal = this.getUpperDiagonalCoordinates(index, index2)
                                 .map((x) => this.getInternalCell(x[0],x[1]))
                                 .join("");
        // Ver se alguém venceu na diagonal superior.
        this.winner = upperDiagonal.includes("falsefalsefalsefalse") ? GameResult.YELLOW :
                        (upperDiagonal.includes("truetruetruetrue") ? GameResult.RED : null);
        // Se houver um vencedor, o jogo terminou.
        if (this.winner !== null) {
          this.isOver = true;
          return;
        }
        
        let lowerDiagonal = this.getLowerDiagonalCoordinates(index, index2)
                                 .map((x) => this.getInternalCell(x[0],x[1]))
                                 .join("");
        // Ver se alguém venceu na diagonal superior.
        this.winner = lowerDiagonal.includes("falsefalsefalsefalse") ? GameResult.YELLOW :
                        (lowerDiagonal.includes("truetruetruetrue") ? GameResult.RED : null);
        // Se houver um vencedor, o jogo terminou.
        if (this.winner !== null) {
          this.isOver = true;
          return;
        }
      }
    }
    // Se o tabuleiro estiver cheio, o jogo terminou empatado.
    this.isOver = this.isBoardFull();
    
    if (this.isOver) {
      this.winner = GameResult.DRAW;
    }
  }

  /*
  A função getCelula(coluna, linha) recebe um inteiro coluna e um inteiro linha e devolve o valor da célula correspondente. Se a coluna ou a linha não forem válidas, ou não existir uma jogada na célula, a função devolve 'null' / 'undefined'. */
  getCell(column, row) {
    const inversedRow = Game.NUM_ROWS - row - 1;
    return this.getInternalCell(column, inversedRow);
  }

  /* 
  A função getCelulaInterna(coluna, linha) recebe um inteiro coluna e um inteiro linha e devolve o valor da célula correspondente. Se a coluna ou a linha não forem válidas, a função devolve 'null'. Se ainda não existir uma jogada na célula, devolve 'undefined'.
  */
  getInternalCell(column, row) {
    return column < 0 || column >= Game.NUM_COLUMNS || row < 0 || row >= Game.NUM_ROWS 
      ? 'null' 
      : this.board[column][row];
  }

  /*
  A função getCoordenadasLinha recebe as coordenadas de uma coluna e uma linha e retorna um array com as coordenadas das 4 células que formam uma linha para a direita.
  */
  getRowCoordinates(column, row) {
    return  [[column, row], [column + 1, row], [column + 2, row], [column + 3, row]];
  }
  
  /*
  A função getCoordenadasColuna recebe as coordenadas de uma coluna e uma linha e retorna um array com as coordenadas das 4 células que formam uma coluna para cima.
  */
  getColumnCoordinates(column, row) {
    return [[column, row], [column, row + 1], [column, row + 2], [column, row + 3]];
  }
  
  /*
  A função getCoordenadasDiagonalSuperior recebe as coordenadas de uma coluna e uma linha e retorna um array com as coordenadas das 4 células que formam uma diagonal no sentido ascendente da esquerda para a direita.
  */
  getUpperDiagonalCoordinates(column, row) {
    return [[column, row], [column+1, row + 1], [column + 2, row + 2], [column + 3, row + 3]];
  }
  
  /*
  A função getCoordenadasDiagonalInferior recebe as coordenadas de uma coluna e uma linha e retorna um array com as coordenadas das 4 células que formam uma diagonal no sentido descendente da esquerda para a direita.
  */
  getLowerDiagonalCoordinates(column, row) {
    return [[column, row], [column + 1, row - 1], [column + 2, row - 2], [column + 3, row - 3]];
  }
  
  /*
  A função tabuleiroCheio() verifica se o tabuleiro está cheio.
  O tabuleiro está cheio se todas as colunas estiverem cheias.
  */
  isBoardFull() {
    return this.board.every(c => c.length === Game.NUM_ROWS);
  }

  reset() {
    this.board = [];
    
    for (let i = 0; i < Game.NUM_COLUMNS; i++) {
      this.board.push([]);
    }

    this.player = Math.round(Math.random()) === 0;
    this.winner = null;        
    this.isOver = false;      
  }
}