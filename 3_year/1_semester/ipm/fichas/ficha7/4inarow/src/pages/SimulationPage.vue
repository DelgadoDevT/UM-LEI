<script>
import { Game } from "../models/game";

export default {
  data() {
    return {
      game: new Game(),
      running: false,
      simulation: {}
    }
  },
  methods: {
    play(column) {
      this.game.play(column);
    },
    simulate() {
      
    },
    async getSimulation() {
      
    }
  },
  created() {
    this.getSimulation();
  }
}
</script>

<template>
<div class="title">Simulate Last Game</div>
  <game-board :game="game"></game-board>
  <div class="button-container">
    <button-component>Simulate</button-component>
  </div>
</template>

<style scoped>
.title {
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