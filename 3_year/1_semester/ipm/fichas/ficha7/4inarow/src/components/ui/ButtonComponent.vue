<template>
  <button type="button">
    <slot></slot>
  </button>
</template>

<style scoped>
button {
  cursor: pointer;
  outline: none;
  border: 1px solid var(--color-accent);
  padding: 0.8rem 1.5rem;
  border-radius: 5px;
  font-weight: 600;
  background-color: var(--color-accent);
  color: var(--color-light);
  transition: all 0.3s ease-in;
}

button:hover {
  background-color: var(--color-light);
  color: var(--color-accent)
}

button:disabled,
button:disabled:hover {
  background-color: var(--color-accent);
  color: var(--color-light);
  opacity: 0.5;
  cursor: not-allowed;
}
</style>