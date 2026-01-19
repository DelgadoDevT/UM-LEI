<template>
<div id="app">
    <router-view></router-view>
</div>
</template>

<script>
export default {
    name: 'App'
}
</script>

<style>
* {
    box-sizing: border-box;
}

body, html {
    height: 100%;
    margin: 0;
    padding: 0;
}

body {
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
    background: #030D46; 
}
</style>
