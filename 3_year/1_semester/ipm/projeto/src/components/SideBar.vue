<template>
<div class="sidebar">
    <div class="sidebar-header">
        <div class="sidebar-header-content">
            <div class="icon-square">
                <i class="fas fa-home header-icon"></i>
            </div>
            <span class="app-name">InsideAirbnb</span>
        </div>
    </div>

    <div class="sidebar-content">
        <div class="sidebar-main">
            <div class="profile-section">
                <div class="profile-pic">
                    <img v-if="user.profilePic && user.profilePic.trim() !== ''" 
                        :src="user.profilePic" 
                        class="profile-image">
                    <div v-else class="empty-circle"></div>
                </div>
                <div class="profile-info">
                    <h3>{{ user.name }}</h3>
                    <p>{{ user.role }}</p>
                </div>
            </div>

            <nav class="sidebar-nav">
                <div class="nav-section">
                    <h4>Discover</h4>
                    <ul>
                        <li v-for="item in discoverItems" 
                            :key="item.name"
                            :class="{ active: activeItem === item.name }"
                            @click="$emit('set-active-item', item.name)">
                            <i :class="item.icon"></i>
                            <span>{{ item.name }}</span>
                        </li>
                    </ul>
                </div>

                <div class="nav-section">
                    <h4>Filters</h4>
                    
                    <div class="filter-group">
                        <label>Country</label>
                        <input type="text" 
                               v-model="filters.country" 
                               placeholder="Insert Country..."
                               list="country-suggestions">
                        <datalist id="country-suggestions">
                            <option v-for="country in allCountries" :key="country" :value="country"></option>
                        </datalist>
                    </div>

                    <div class="filter-group">
                        <label>City</label>
                        <select v-model="filters.city" :disabled="!filters.country">
                            <option value="">Every city</option>
                            <option v-for="city in filteredCities" :key="city" :value="city">
                                {{ city }}
                            </option>
                        </select>
                    </div>

                    <div class="filter-group">
                        <label>Municipality</label>
                        <select v-model="filters.municipality" :disabled="!filters.city">
                            <option value="">All Municipalities</option>
                            <option v-for="municipality in filteredMunicipalities" :key="municipality" :value="municipality">
                                {{ municipality }}
                            </option>
                        </select>
                    </div>

                    <div class="filter-group">
                        <label>Property Type</label>
                        <select v-model="filters.propertyType">
                            <option value="">All types</option>
                            <option v-for="type in filterData.propertyTypes" :key="type" :value="type">
                                {{ type }}
                            </option>
                        </select>
                    </div>
                    
                    <div class="filter-group">
                        <label>Select date range</label>
                        <div class="date-range">
                            <input type="date" 
                                   v-model="filters.dateStart" 
                                   class="date-input">
                            <span>to</span>
                            <input type="date"
                                   v-model="filters.dateEnd" 
                                   class="date-input">
                        </div>
                    </div>

                    <div class="filter-group">
                        <label>Price Range</label>
                        <div class="range-inputs">
                            <input type="number" v-model="filters.priceMin" placeholder="0" min="0">
                            <span>-</span>
                            <input type="number" v-model="filters.priceMax" placeholder="650" min="0">
                        </div>
                    </div>

                    <div class="filter-group">
                        <label>Annual Occupancy</label>
                        <div class="range-inputs">
                            <input type="number" v-model="filters.occupancyMin" placeholder="0" min="0" max="365">
                            <span>-</span>
                            <input type="number" v-model="filters.occupancyMax" placeholder="365" min="0" max="365">
                        </div>
                    </div>

                    <div class="filter-group">
                        <label>Minimum Reviews</label>
                        <input type="range" min="0" max="100" v-model.number="filters.minReviews" class="slider">
                        <div class="slider-values">
                            <span>0</span>
                            <span>{{ filters.minReviews || 0 }} reviews</span>
                            <span>100</span>
                        </div>
                    </div>

                    <div class="filter-group">
                        <label>Minimum Rating</label>
                        <div class="star-rating-display">
                            <template v-for="n in 5" :key="n">
                                <i :class="getStarClass(n)"></i>
                            </template>
                            <span class="rating-value">{{ filters.minRating.toFixed(1) }}</span>
                        </div>
                        <input type="range" min="0" max="5" step="0.5" v-model.number="filters.minRating" class="slider rating-slider">
                    </div>

                </div>
                <div class="filter-group clear-filters-group">
                    <button @click="$emit('clear-all-filters')" class="clear-filters-btn">
                        <i class="fas fa-times-circle"></i>
                        Clear All Filters
                    </button>
                </div>
            </nav>
        </div>

        <div class="logout-section">
            <button @click="$emit('logout')" class="logout-btn">
                <i class="fas fa-sign-out-alt"></i>
                Logout
            </button>
        </div>
    </div>
</div>
</template>

<script>
export default {
    name: 'SideBar',
    props: {
        user: Object,
        activeItem: String,
        discoverItems: Array,
        filters: Object,
        filterData: Object,
        allCountries: Array,
        filteredCities: Array,
        filteredMunicipalities: Array
    },
    methods: {
        // Calculate star rating display
        getStarClass(n) {
            if (this.filters.minRating >= n) {
                return 'fas fa-star'; 
            } else if (this.filters.minRating >= (n - 0.5)) {
                return 'fas fa-star-half-alt'; 
            } else {
                return 'far fa-star'; 
            }
        }
    }
}
</script>

<style scoped>
.sidebar {
    width: 280px;
    background: linear-gradient(270deg, #030D46 0%, #163140 100%);
    color: white;
    display: flex;
    flex-direction: column;
    height: 100vh;
    position: fixed;
    left: 0;
    top: 0;
}

.sidebar-header {
    padding: 1rem 1.5rem;
    border-bottom: 2px solid rgba(255, 255, 255, 0.1);
    flex-shrink: 0;
    position: sticky;
    top: 0;
    background: linear-gradient(270deg, #030D46 0%, #163140 100%);
    z-index: 10;
}

.sidebar-header-content {
    display: flex;
    align-items: center;
    gap: 1rem;
}

.icon-square {
    background: #2D9C92;
    padding: 0.5rem;
    border-radius: 8px;
}

.header-icon {
    font-size: 2rem;
    color: white;
}

.app-name {
    font-size: 1.5rem;
    font-weight: 700;
    color: white;
}

.sidebar-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow-y: auto;
}

.profile-section {
    position: relative;
    display: flex;
    align-items: center;
    padding: 25px 20px 15px 20px;
    border-top: 2px solid rgba(255, 255, 255, 0.1);
    border-bottom: 2px solid rgba(255, 255, 255, 0.1);
    margin-bottom: 20px;
    flex-shrink: 0;
}

.profile-pic {
    margin-right: 15px;
    display: flex;
    align-items: center;
    justify-content: center;
}

.profile-image {
    width: 50px;
    height: 50px;
    border-radius: 50%;
    object-fit: cover;
    border: 2px solid #2D9C92;
}

.empty-circle {
    width: 50px;
    height: 50px;
    border-radius: 50%;
    content: "";
    background: transparent;
    border: 2px solid #2D9C92;
}

.profile-info h3 {
    margin: 0;
    font-size: 1.1rem;
    font-weight: 600;
}

.profile-info p {
    margin: 0;
    font-size: 0.9rem;
    opacity: 0.8;
}

.sidebar-nav {
    flex: 1;
    padding: 0 20px;
    overflow-y: auto;
    min-height: 0;
}

.nav-section {
    margin-bottom: 30px;
}

.sidebar-nav .nav-section:last-child {
    margin-bottom: 15px; 
}

.nav-section h4 {
    font-size: 0.9rem;
    text-transform: uppercase;
    letter-spacing: 1px;
    margin-bottom: 15px;
    opacity: 0.7;
    font-weight: 600;
}

.nav-section ul {
    list-style: none;
    padding: 0;
    margin: 0;
}

.nav-section li {
    display: flex;
    align-items: center;
    padding: 12px 15px;
    margin-bottom: 5px;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s ease;
    font-size: 0.95rem;
}

.nav-section li:hover {
    background-color: rgba(45, 156, 146, 0.2);
}

.nav-section li.active {
    background-color: #2D9C92;
    font-weight: 600;
}

.nav-section li i {
    margin-right: 12px;
    width: 20px;
    text-align: center;
}

.filter-group {
    margin-bottom: 20px;
}

.filter-group label {
    display: block;
    margin-bottom: 8px;
    font-size: 0.9rem;
    opacity: 0.9;
    font-weight: 500;
}

.filter-group select,
.filter-group input {
    width: 100%;
    padding: 10px 12px;
    border-radius: 6px;
    border: 1px solid rgba(45, 156, 146, 0.5);
    background-color: rgba(45, 156, 146, 0.1);
    color: white;
    font-size: 0.9rem;
    transition: border-color 0.3s, box-shadow 0.3s;
}

.filter-group select:focus,
.filter-group input:focus {
    outline: none;
    border-color: #2D9C92;
    box-shadow: 0 0 0 3px rgba(45, 156, 146, 0.3);
}

.filter-group select:disabled {
    background-color: rgba(255, 255, 255, 0.05);
    border-color: rgba(255, 255, 255, 0.2);
    opacity: 0.6;
    cursor: not-allowed;
}

.filter-group input::placeholder {
    color: rgba(255, 255, 255, 0.6);
}

.filter-group select option {
    background: #163140;
    color: white;
}

.date-range {
    display: flex;
    flex-direction: column;
    gap: 10px;
}

.date-range input {
    width: 100%;
}

.date-range span {
    display: flex;
    align-items: center;
    text-align: center;
    width: 100%;
    color: rgba(255, 255, 255, 0.7);
    font-size: 0.8rem;
    text-transform: uppercase;
    margin: -5px 0;
}

.date-range span::before,
.date-range span::after {
    content: '';
    flex: 1;
    border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.date-range span::before {
    margin-right: 0.5rem;
}

.date-range span::after {
    margin-left: 0.5rem;
}

.range-inputs {
    display: flex;
    align-items: center;
    gap: 10px;
}

.range-inputs input {
    flex: 1;
}

.range-inputs span {
    opacity: 0.7;
}

.filter-group input[type="number"] {
    -moz-appearance: textfield;
}

.filter-group input[type="number"]::-webkit-outer-spin-button,
.filter-group input[type="number"]::-webkit-inner-spin-button {
    -webkit-appearance: none;
    margin: 0;
}

.slider {
    width: 100%;
    height: 6px;
    background: rgba(255, 255, 255, 0.2);
    border-radius: 3px;
    -webkit-appearance: none;
    margin: 10px 0;
}

.slider::-webkit-slider-thumb {
    -webkit-appearance: none;
    width: 18px;
    height: 18px;
    border-radius: 50%;
    background: #2D9C92;
    border: 2px solid white;
    cursor: pointer;
}

.slider-values {
    display: flex;
    justify-content: space-between;
    margin-top: 5px;
    font-size: 0.8rem;
    color: rgba(255, 255, 255, 0.8);
}

.logout-section {
    padding: 15px;
    border-top: 2px solid rgba(255, 255, 255, 0.1);
    margin-top: auto;
    flex-shrink: 0;
}

.logout-btn {
    width: 100%;
    padding: 12px;
    background-color: rgba(255, 255, 255, 0.1);
    color: white;
    border: none;
    border-radius: 8px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
    transition: background-color 0.3s ease;
}

.logout-btn:hover {
    background-color: rgba(255, 255, 255, 0.2);
}

.sidebar-content::-webkit-scrollbar {
    width: 4px;
}

.sidebar-content::-webkit-scrollbar-track {
    background: rgba(255, 255, 255, 0.1);
}

.sidebar-content::-webkit-scrollbar-thumb {
    background: rgba(45, 156, 146, 0.5);
    border-radius: 2px;
}

.star-rating-display {
    display: flex;
    align-items: center;
    gap: 4px;
    margin-bottom: 10px;
    color: #FFD700;
}

.star-rating-display i {
    font-size: 1.1rem;
}

.star-rating-display .far.fa-star {
    color: #aeb9e1;
}

.star-rating-display .rating-value {
    font-size: 0.95rem;
    font-weight: 600;
    color: white;
    margin-left: 8px;
    background: rgba(45, 156, 146, 0.2);
    padding: 2px 6px;
    border-radius: 4px;
}

.slider.rating-slider {
    margin-top: 0;
}

.clear-filters-btn {
    width: 100%;
    margin-top: -20px;
    padding: 12px;
    background-color: rgba(255, 255, 255, 0.1);
    color: white;
    border: none;
    border-radius: 8px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
    transition: background-color 0.3s ease;
}

.clear-filters-btn:hover {
    background-color: rgba(255, 255, 255, 0.2);
}
</style>
