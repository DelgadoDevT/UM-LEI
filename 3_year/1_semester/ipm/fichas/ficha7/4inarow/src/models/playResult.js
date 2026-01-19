/* Este documento apresenta uma solução em construção. É fornecido como 
   material de estudo. Recomenda-se que o revejam e melhorem conforme   
   forem adquirindo novos conhecimentos. */

/* 
  Este ficheiro exporta um objeto que contém os resultados possíveis de uma jogada.
  GAME_OVER - A jogada terminou o jogo.
  SUCCESS - A jogada foi realizada com sucesso e o jogo continua.
  ERROR_INVALID_COLUMN - A coluna não é válida.
  ERROR_GAME_OVER - O jogo já terminou.
  ERROR_FULL_COLUMN - A coluna já está preenchida.
*/
export const PlayResult = {
  GAME_OVER: 0,
  SUCCESS: 1,
  ERROR_INVALID_COLUMN: 2,
  ERROR_GAME_OVER: 3,
  ERROR_FULL_COLUMN: 4
};