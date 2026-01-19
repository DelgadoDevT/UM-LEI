<template>
<div class="maintenance-container">
    <div class="maintenance-content">
        <video 
            class="meme-img"
            src="../assets/videos/manutencao.mp4" 
            controls  
            loop 
            playsinline
        >
            Your browser does not support videos.
        </video>

        <h1>Oops! The development team was too lazy to create this page.</h1>
        <p>Pretend it doesn't exist and life moves on.</p>

        <button @click="goBack" class="btn btn-back">Back to Login</button>
    </div>
</div>
</template>

<script>
export default {
    name: 'Maintenance',
    methods: {
        // Navigate back to login page
        goBack() {
            this.$router.push('/');
        }
    }
}
</script>

<style scoped>

.maintenance-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    min-height: 100vh;
    width: 100%;
    padding: 2rem;
    text-align: center;
    background: linear-gradient(270deg, #030D46 0%, #163140 100%);
}

.maintenance-content {
    max-width: 900px;
}

.maintenance-content .meme-img {
    max-width: 100%;
    height: auto;
    border-radius: 12px;
    margin-bottom: 2.5rem;
    background-color: #374151; 
}

.maintenance-content h1 {
    font-size: 2.5rem;
    font-weight: 700;
    color: #FFFFFF;
    margin-bottom: 1rem;
}

.maintenance-content p {
    font-size: 1.15rem;
    color: #E5E7EB;
    line-height: 1.6;
    margin-bottom: 2.5rem;
}


.btn-back {
    text-decoration: none; 
    display: inline-block;
    background-color: #374151; 
    color: white;
    padding: 0.8rem 1.5rem;
    border-radius: 9999px;
    border: none;
    font-size: 1rem;
    font-weight: 600;
    cursor: pointer;
    transition: background-color 0.2s;
}


.btn-back:hover {
    background-color: #4B5563;
}
</style>