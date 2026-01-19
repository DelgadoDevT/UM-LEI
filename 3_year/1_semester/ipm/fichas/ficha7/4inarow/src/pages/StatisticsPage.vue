<script>
import CardComponent from '../components/ui/CardComponent.vue';
import { GameResult } from '../models/gameResult';
import { Game } from '../models/game';

export default {
  components: {
    CardComponent,
  },
  data() {
    return {
      yellow: 0,
      red: 0,
      draw: 0,
      games: []
    }
  },
  methods: {
    async getStatistics() {
      try {
        const response = await fetch('http://localhost:3000/statistics/1');

        if (!response.ok) {
          throw new Error('Something went wrong');
        }

        const data = await response.json();
        this.red = data.red;
        this.yellow = data.yellow;
        this.draw = data.draw;
      } catch (error) {
        console.log(error);
      }
    },
    async getGames() {
      try {
        const response = await fetch('http://localhost:3000/games')

        if (!response.ok) {
          throw new Error("Something went wrong :((");
        }

        const data = await response.json;

        this.games = []
        data.forEach(element => {
          const game = new Game();

          game.board = element.game.board
          game.player = element.game.player
          game.isOver = element.game.isO
        });
      } catch(error) {
        console.log("ERROR: ", error)
      }
    },
    showDate(dateString) {
      const date = new Date(dateString);
      return date.toLocaleDateString() 
        + " " + date.toLocaleTimeString();
    },
    showResult(winner) {
      if (winner === GameResult.YELLOW) {
        return 'Winner: Yellow';
      }

      if (winner === GameResult.RED) {
        return 'Winner: Red';
      }

      return 'Draw';
    }
  },
  created() {
    this.getStatistics();
    this.getGames();
  }
}
</script>

<template>
  <div class="container">
    <card-component class="card yellow">
      <p>Yellow Victories</p>
      <div class="score">{{ yellow }}</div>
    </card-component>
    <card-component class="card red">
      <p>Red Victories</p>
      <div class="score">{{ red }}</div>
    </card-component>
    <card-component class="card draw">
      <p>Draws</p>
      <div class="score">{{ draw }}</div>
    </card-component>
    <card-component class="card history">
      <p>History</p>
      <!-- 4.b) add dynamic route handler -->
      <card-component 
        v-for="g in games" 
        :key="g.id"
        :class="['history-card', {
          red: g.game.winner === 0,
          yellow: g.game.winner === 1,
          draw: g.game.winner === 2
        }]"
        >
        <div class="info-container">
          <p class="date">{{ showDate(g.date) }}</p>
          <div class="score">{{ showResult(g.game.winner) }}</div>
        </div>
        <game-board :game="g.game" class="board"></game-board>
      </card-component>
    </card-component>
  </div>
</template>

<style scoped>
.container {
  margin: 40px 20px;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  grid-template-areas: 
    "y r e"
    "h h h";
  gap: 20px;
}

.card {
  color: var(--color-light);
  background-color: var(--color-primary-dark);
  text-align: center;
  font-weight: 600;
}

.score {
  font-weight: 900;
  font-size: 20px;
}

.history {
  grid-area: h;
  background-color: var(--color-primary);
}

.history-card {
  display: flex;
  cursor: pointer;
  margin-top: 20px;
  transition: all 0.3s ease-in;
  background-color: var(--color-primary-dark);
}

.history-card:hover {
  opacity: 0.6;
}

.info-container {
  flex-grow: 1;
  text-align: left;
}

.board {
  margin: 0;
  max-width: 150px;
}

@media screen and (max-width: 700px) {
  .container {
    grid-template-areas: 
      "y"
      "r"
      "e"
      "h";
    grid-template-columns: 1fr;
  }
}

@media screen and (max-width: 500px) {
  .history-card {
    display: block;
  }

  .info-container {
    text-align: center;
  }

  .board {
    margin: 20px auto auto;
  }
}
</style>