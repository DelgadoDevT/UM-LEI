<script>
import { Game } from "../models/game";
import { PlayResult } from "../models/playResult";
import { GameResult } from "../models/gameResult";

export default {
  data() {
    return {
      game: new Game(),
      statistics: {
        YELLOW: 0,
        RED: 0,
        DRAW: 0
      }
    }
  },

  methods: {
    async getStatistics() {
      try {
        const response = await fetch("http://localhost:3000/statistics/1");

        if (!response.ok) throw new Error("Something went wrong");

        const data = await response.json();

        // Garantir defaults
        this.statistics = {
          YELLOW: data.YELLOW || 0,
          RED: data.RED || 0,
          DRAW: data.DRAW || 0
        };

      } catch (error) {
        console.log(error);
      }
    },

    async updateSimulation() {
      const winner = this.game.winner;

      if (winner === GameResult.YELLOW) {
        this.statistics.YELLOW = (this.statistics.YELLOW || 0) + 1;

      } else if (winner === GameResult.RED) {
        this.statistics.RED = (this.statistics.RED || 0) + 1;

      } else if (winner === GameResult.DRAW) {
        this.statistics.DRAW = (this.statistics.DRAW || 0) + 1;
      }
    },

    async updateStatistics() {
      await fetch("http://localhost:3000/statistics", {
        method: "PATCH",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify(this.statistics)
      });
    },

    async saveGame() {
      // TODO: implementação real
      await fetch("http://localhost:3000/games" , {
        method: "POST",
        header: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            game: this.game,
            date: new Date().toISOString()
        }),
      })
    },

    async play(column) {
      const res = this.game.play(column);

      if (res === PlayResult.ERROR_FULL_COLUMN) {
        alert("Column is full!");

      } else if (res === PlayResult.ERROR_GAME_OVER) {
        alert('Game over. Click "New Game" to play another game.');

      } else if (res === PlayResult.GAME_OVER) {
        // Atualiza estatísticas locais
        await this.updateSimulation();

        // Envia para o backend
        await this.updateStatistics();

        this.saveGame();
      }
    },

    reset() {
      this.game.reset();
      this.startPlayer = this.game.player;
      this.plays = [];
    }
  },

  computed: {
    gameStatus() {
      const winner = this.game.winner;

      if (winner === GameResult.YELLOW) return "Winner: Yellow";
      if (winner === GameResult.RED) return "Winner: Red";
      if (winner === GameResult.DRAW) return "Draw";

      return this.game.player ? "Player: Red" : "Player: Yellow";
    }
  },

  created() {
    this.getStatistics();
  }
}
</script>

<template>
  <div class="status">{{ gameStatus }}</div>

  <game-board :game="game" @play="play" />

  <div class="button-container">
    <button-component @click="reset">New Game</button-component>
  </div>
</template>

<style scoped>
.status {
  font-size: 1.2rem;
  font-weight: 600;
  text-align: center;
  margin: 40px 0;
}

.button-container {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}
</style>