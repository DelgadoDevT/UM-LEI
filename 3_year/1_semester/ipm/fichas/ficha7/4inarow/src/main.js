import './assets/main.css';
import { createApp } from 'vue';
import router from './router';
import App from './App.vue';
import GameBoard from './components/GameBoard.vue';
import ButtonComponent from './components/ui/ButtonComponent.vue';

const app = createApp(App);

app.component('ButtonComponent', ButtonComponent);
app.component('GameBoard', GameBoard);
app.use(router);
app.mount('#app');