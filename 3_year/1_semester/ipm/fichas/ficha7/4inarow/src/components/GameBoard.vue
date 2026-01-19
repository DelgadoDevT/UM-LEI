<script>
import { Game } from '../models/game';

export default {
  props: ['game'],
  emits: ['play'],
  data() {
    return {
      numColumns: Game.NUM_COLUMNS,
      numRows: Game.NUM_ROWS
    }
  },
  methods: {
    applyRed(column, row) {
      return this.game.getCell(column, row) === true;
    },
    applyYellow(column, row) {
      return this.game.getCell(column, row) === false;
    }
  }
}
</script>

<template>
  <div class="board">
    <div v-for="c in numColumns" class="column" :key="c"
      @click="$emit('play', c - 1)">
      <div v-for="l in numRows" :key="l"
        :class="['cell', {
          yellow: applyYellow(c - 1, l - 1),
          red: applyRed(c - 1, l - 1)
        }]">
      </div>
    </div>
  </div>
</template>

<style scoped>
.board {
  display: flex;
  width: 40%;
  margin: 20px auto auto;
  background-color: #4357A0;
  padding: 10px;
  border-radius: 10px;
}

.column {
  display: flex;
  flex-direction: column;
  flex-grow: 1;
}

.cell {
  border-radius: 100%;
  background-color: #001773;
  max-height: 100%;
  max-width: 100%;
  aspect-ratio: 1;
  margin: 4px;
}

.yellow {
  background-color: var(--color-yellow);
}

.red {
  background-color: var(--color-red);
}
</style>