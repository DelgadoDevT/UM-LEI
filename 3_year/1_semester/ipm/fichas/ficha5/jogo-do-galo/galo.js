let board = [
    [0, 0, 0],
    [0, 0, 0],
    [0, 0, 0]
]

// 0 -> celula vazia
// 1 -> jogador X
// -1 -> jogador O

let player = true; // X -> true, 0 -> false
let gameStatus;
let gameOver = false;

document.addEventListener("DOMContentLoaded", function() {
    const board = document.getElementById('board');
    gameStatus = document.getElementById('game-status');
    gameStatus.textContent = "Jogador X";

    for (let i = 0; i < 3; i++) {
        for (let j = 0; j < 3; j++) {
            const slot = document.createElement('div');
            slot.className = 'slot';
            slot.addEventListener('click', (event) => slotClickHandler(event, i, j));
            board.appendChild(slot);
        }
    }

    document.getElementById('reset-btn').addEventListener('click', reset);
})

// 3) Implementar as jogadas
function slotClickHandler(event, i, j) {
    /*
        condição ternária
        se player for true -> põe 1
        se player for false -> põe -1
    */

    if (gameOver) return;
    // 4) Impedir que se carregue em célula já preenchida
    if (board[i][j] !== 0) {
        alert('Célula já preenchida');
        return;
    }

    board[i][j] = player ? 1 : -1;
    event.target.textContent = player ? 'X' : 'O';
    player = !player;
    gameStatus.textContent = `Jogador ${player ? 'X' : 'O'}`;

    checkGame();
}

function reset() {
    board = [
        [0, 0, 0],
        [0, 0, 0],
        [0, 0, 0]
    ]

    player = true;
    gameStatus.textContent = "Jogador X";
    gameOver = false;

    const slots = document.getElementsByClassName('slot');
    for (let slot of slots) {
        slot.textContent = '';
    }
}

function checkGame() {
    let result = 0;
    // 0 -> jogo continua
    // 1 -> X ganhou
    // 2 -> O ganhou
    // 3 -> empate

    // Verificar linhas e colunas
    for (let i = 0; i < 3; i++) {
        const rowSum = board[i][0] + board[i][1] + board[i][2];
        const columnSum = board[0][i] + board[1][i] + board[2][i];

        if (rowSum === 3 || columnSum === 3) result = 1;
        if (rowSum === -3 || columnSum === -3) result = 2;
    }

    // Verificar diagonais
    const diagonal1 = board[0][0] + board[1][1] + board[2][2];
    const diagonal2 = board[0][2] + board[1][1] + board[2][0];

    if (diagonal1 === 3 || diagonal2 === 3) result = 1;
    if (diagonal1 === -3 || diagonal2 === -3) result = 2;

    // Verificar empate
    if (result === 0 && board.flat().every(cell => cell !== 0)) {
        result = 3;
    }

    // Mostrar resultado
    if (result > 0) {
        gameOver = true;
        if (result === 1) {
            gameStatus.textContent = 'Jogo Terminado! Ganhou o jogador X';
        } else if (result === 2) {
            gameStatus.textContent = 'Jogo Terminado! Ganhou o jogador O';
        } else {
            gameStatus.textContent = 'Jogo Terminado! Empataram';
        }
    }
}