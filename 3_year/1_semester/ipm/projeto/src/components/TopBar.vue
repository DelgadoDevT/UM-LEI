<template>
<header class="top-header">
    <div class="header-content">
        <div class="header-left">
            <h1 class="page-title">{{ activeItem }}</h1>
        </div>
        <div class="header-right">
            <span class="properties-count">{{ proprietesNum }} properties found</span>
        </div>
    </div>
</header>
</template>

<script>
export default {
    name: 'TopBar',
    props: {
        activeItem: String,
        proprietesNum: Number
    }
}
</script>

<style scoped>
.top-header {
    position: fixed;
    top: 0;
    left: 280px;
    right: 0;
    z-index: 1000;
    background: #030D46;
    padding: 1rem 1.5rem;
    height: 80px;
    display: flex;
    align-items: center;
}

.header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;
}

.header-left {
    display: flex;
    align-items: center;
    gap: 1rem;
}

.page-title {
    font-size: 1.5rem;
    font-weight: 600;
    color: white;
}

.header-right {
    display: flex;
    align-items: center;
}

.properties-count {
    font-size: 1rem;
    color: rgba(255, 255, 255, 0.9);
    font-weight: 500;
}

@media (max-width: 768px) {
    .top-header {
        position: relative;
        left: 0;
        width: 100%;
    }
}
</style>
