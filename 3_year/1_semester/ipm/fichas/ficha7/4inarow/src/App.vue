<!-- Este documento apresenta uma solução em construção. É fornecido como 
     material de estudo. Recomenda-se que o revejam e melhorem conforme   
     forem adquirindo novos conhecimentos. -->

<script>
import NavBar from './components/NavBar.vue';

export default {
  components: {
    NavBar
  },
  data() {
    return {
      player: false,
      winner: null
    }
  },
  methods: { 
    changePlayer(player) {
      this.player = player;
    },
    changeWinner(winner) {
      this.winner = winner;
    },
    reset(player) {
      this.player = player;
      this.winner = null;
    }
  }
}
</script>

<template>
  <nav-bar></nav-bar>
  <!-- 2.a) router view -->
    <router-view></router-view>    
</template>
