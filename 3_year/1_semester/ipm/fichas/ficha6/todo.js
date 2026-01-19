const app = Vue.createApp({
  data() {
    return {
      todos: [
        'Learn a new course',
        'Read a book',
        'Go to the gym',
        'Go shopping'
      ],
      today: new Date().toLocaleDateString(),
      enteredTodo: ''
    }
  },

  // computed são valores calculados automaticamente
  computed: {
    isAddDisabled() {
        return this.enteredTodo.trim() === '';
    }
  },

  methods: {
    submitTodo() {
      if (this.enteredTodo.trim() !== '' && !this.todos.includes(this.enteredTodo)) {
        this.todos.push(this.enteredTodo)
        this.enteredTodo = '';
      }
    },

    deleteTodo(index) {
        this.todos.splice(index, 1);
    }
  }
});

// 1) Ligar a app Vue ao HTML
app.mount('#app');