<template>
  <nav id="nav">
    <h3>4 in a Row</h3>
    <div class="nav-items">
      <router-link to="/game">Game</router-link>
      <router-link to="/statistics">Statistics</router-link>
      <router-link to="/simulation">Simulation</router-link>
    </div>
  </nav>
</template>

<style scoped>
#nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  background-color: #F5F5F5;
}

a {
  text-decoration: none;
  padding: 0.5rem 1.5rem;
  font-weight: 600;
  margin-left: 10px;
  border: 1px solid var(--color-accent);
  border-radius: 5px;
  color: var(--color-accent);
  transition: all 0.3s ease-in;
}

a.router-link-active {
  background-color: var(--color-accent);
  color: var(--color-light);
}
</style>