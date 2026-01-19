<template>
<div id="login-app">
    <div class="login-container">
        <div class="login-panel">
            <div class="login-form">

                <div class="logo">
                    <i class="fas fa-home logo-icon"></i>
                    <div class="logo-text">INSIDE AIRBNB</div>
                </div>
                <h2 class="login-title">Login</h2>
                <form @submit.prevent="handleLogin">
                    <div class="input-group">
                        <i class="fas fa-user input-icon"></i>
                        <input
                            type="email"
                            v-model="email"
                            placeholder="Email Address"
                            required
                        >
                    </div>
                    <div class="input-group">
                        <i class="fas fa-lock input-icon"></i>
                        <input
                            type="password"
                            v-model="password"
                            placeholder="Password"
                            required
                        >
                    </div>
                    <router-link to="/maintenance" class="forgot-password">Forgot password?</router-link>
                    <button type="submit" class="btn btn-login">Login</button>
                </form>
                <p class="register-link">
                    Not a member yet? 
                    <router-link to="/maintenance">Register!</router-link>
                </p>
                <div class="separator">
                    <span>or</span>
                </div>
                <button type="button" class="btn btn-google" @click="goToMaintenance">
                    <img src="../assets/images/google-logo.png" alt="Google Logo"/>
                    Log in with Google
                </button>
            </div>
        </div>
        <div class="info-panel">
            <div class="info-content">
                <h1 class="info-title">InsideAirbnb</h1>
                <div class="illustration-placeholder">
                    <img src="../assets/images/login-side.png" alt="Ilustração do Airbnb"/>
                </div>
                <p class="tagline">
                    How is Airbnb really being used in and affecting
                    the neighbourhoods of your city?
                </p>
            </div>
        </div>
    </div>
</div>
</template>

<script>
export default {
    name: 'Login',
    data() {
        return {
            accounts: [],      
            email: '',         
            password: '',      
            loggedUser: null,  
            role: null         
        };
    },
    methods: {
        // Handle login form submission
        handleLogin() {
            // Find matching user credentials
            const user = this.accounts.find(
                acc => acc.email === this.email && acc.password === this.password
            );
            
            if (user) {
                this.loggedUser = user;
                this.email = '';
                this.password = '';
               
                // Store user session in localStorage
                const profilePicPath = user.pic ? (import.meta.env.DEV ? `./src/assets/images/${user.pic}` : `/images/${user.pic}`) : null;

                localStorage.setItem('loggedUser', JSON.stringify({
                    name: user.name,
                    role: user.role,
                    profilePic: profilePicPath,
                    email: user.email
                }));
                
                // Redirect to dashboard
                this.$router.push('/dashboard');
            } else {
                alert('Email ou password incorretos');
            }
        },

        // Redirect to maintenance page
        goToMaintenance() {
            this.$router.push('/maintenance');
        },

        // Load users from JSON database
        async loadUsers() {
            try {
                try {
                    const response = await fetch('http://localhost:3001/users');
                    if (response.ok) {
                        const usersData = await response.json();
                        this.accounts = usersData || [];
                        console.log('✅ Users loaded from JSON Server');
                        return;
                    }
                } catch (serverError) {
                    console.log('🔄 JSON Server not available for users, using static db.json');
                }

                const response = await fetch('/db.json');
                const dbData = await response.json();
                this.accounts = dbData.users || [];

            } catch (err) {
                console.error('Error loading users:', err);
                this.accounts = [];
            }
        },
    },

    // Load users when component mounts
    async mounted() {
        await this.loadUsers();
    },
}
</script>

<style scoped>
.login-container {
    display: flex;
    min-height: 100vh;
    width: 100%;
    overflow: hidden; 
}

.login-panel {
    flex: 1;
    background: linear-gradient(270deg, #030D46 0%, #163140 100%);
    background-blend-mode: darken;
    mix-blend-mode: normal;
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 3rem;
    min-width: 450px;
}

.login-form {
    width: 100%;
    max-width: 400px;
    text-align: center;
    margin: 0 auto;
}

.logo {
    display: flex;
    flex-direction: column;
    align-items: center;
    margin-bottom: 1.5rem;
}

.logo-icon {
    font-size: 5.5rem;
    color: #2D9C92; 
    margin-bottom: 1.75rem;
}

.logo-text {
    font-size: 1.8rem;
    font-weight: 700;
    letter-spacing: 0.5px;
    text-align: center;
}

.login-title {
    font-size: 2rem;
    font-weight: 600;
    margin-bottom: 2.5rem;
    text-align: center;
}

.input-group {
    position: relative;
    margin-bottom: 1.5rem;
}

.input-icon {
    position: absolute;
    left: 15px;
    top: 50%;
    transform: translateY(-50%);
    color: #9CA3AF;
}

.input-group input {
    width: 100%;
    padding: 1rem 1rem 1rem 3.5rem;
    border-radius: 9999px;
    border: none;
    background-color: #374151;
    color: white;
    font-size: 1rem;
}

.input-group input::placeholder {
    color: #9CA3AF;
}

.input-group input:focus {
    outline: none;
    box-shadow: 0 0 0 2px #2D9C92;
}

.forgot-password {
    display: block;
    text-align: right;
    font-size: 0.9rem;
    color: #FFFFFF;
    text-decoration: none;
    margin-top: -0.8rem;
    margin-bottom: 2rem;
}

.forgot-password:hover {
    text-decoration: underline;
}

.register-link {
    text-align: center;
    font-size: 0.9rem;
    color: #FFFFFF;
    margin-top: 1.5rem;
}

.register-link a {
    color: #2D9C92;
    font-weight: 600;
    text-decoration: none;
}

.register-link a:hover {
    text-decoration: underline;
}

.btn {
    width: 100%;
    padding: 1rem;
    border: none;
    border-radius: 9999px; 
    font-size: 1rem;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s ease-in-out;
}

.btn-login {
    background: linear-gradient(90deg, #3B82F6, #60A5FA);
    color: white;;
    margin-bottom: 1.5rem;
    padding: 0.5rem 1rem
}

.btn-login:hover {
    opacity: 0.9;
}

.btn-google {
    background-color: white;
    color: #1F2937;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0.3rem 1rem;
}

.btn-google:hover {
    background-color: #F3F4F6;
}

.btn-google img {
    height: 1.75rem; 
    width: auto;   
    margin-right: 0.75rem; 
}

.separator {
    display: flex;
    align-items: center;
    text-align: center;
    color: #FFFFFF;
    margin: 2rem 0;
    font-size: 0.9rem;
}

.separator::before,
.separator::after {
    content: '';
    flex: 1;
    border-bottom: 2px solid #FFFFFF;
}

.separator span {
    padding: 0 1rem;
}

.info-panel {
    flex: 1;
    background-color: #f7f8f5;
    color: #1F2937;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 3rem;
}

.info-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    max-width: 700px;
}

.info-title {
    font-size: 5rem;
    font-weight: 700;
    color: #2D9C92;
    margin-bottom: 6rem;
}

.illustration-placeholder { 
    width: 100%; 
    height: 300px;
    border-radius: 12px; 
    display: flex; 
    align-items: center; 
    justify-content: center; 
    margin-bottom: 2.5rem; 
    padding: 1rem; }

.tagline {
    font-size: 32px;
    font-weight: 500;
    color: #374151;
    text-align: center;
    line-height: 1.6;
    font-family: "Geologica";
    margin-top: 3rem;
}

@media (max-width: 900px) {
    .login-container {
        flex-direction: column;
    }

    .info-panel {
        display: none;
    }

    .login-panel {
        min-width: 100%;
        min-height: 100vh;
    }
}
</style>
