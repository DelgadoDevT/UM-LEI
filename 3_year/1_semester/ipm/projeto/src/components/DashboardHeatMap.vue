<template>
<div class="dashboard-panel">
    <div class="dashboard-header">
        <h2>Heat Map by Municipality</h2>
        <p>Distribution of listings and occupancy</p>
    </div>
    
    <div class="heatmap-grid" v-if="heatMapData.length > 0" :style="{ '--columns': gridColumns }">
        <div class="heatmap-item" v-for="item in visibleItems" :key="item.name">
            <div class="item-header">
                <h3>{{ item.name }}</h3>
                <div :class="['alert-icon', getHeatmapAlertClass(item.alertCount)]">
                    <i class="fas fa-exclamation-triangle"></i>
                    <span>{{ item.alertCount }}</span>
                </div>
            </div>
            
            <div class="item-stats">
                <div class="stat">
                    <span>{{ item.listingsCount }}</span>
                    Listings
                </div>
                <div class="stat">
                    <span>{{ item.avgPrice }}</span>
                    Avg. Price
                </div>
                <div class="stat">
                    <span>{{ item.occupation }}%</span>
                    Occupation
                </div>
            </div>

            <div class="indicator-bars" :class="getHeatmapIndicatorBars(item.occupation)">
                <div class="bar bar-1"></div>
                <div class="bar bar-2"></div>
            </div>
        </div>
    </div>

    <div class="view-controls" v-if="heatMapData.length > initialItemsToShow">
        <button @click="showAll = !showAll" class="view-toggle">
            {{ showAll ? 'Show Less' : `View All (${heatMapData.length})` }}
        </button>
    </div>

    <div class="no-data-message" v-else>
        No heatmap data available for the selected filters.
    </div>
</div>
</template>

<script>
/**
 * DashboardHeatMap Component
 * * Displays a responsive grid of heatmap items representing data by municipality.
 * Shows alerts, stats (listings, price, occupancy), and visual indicators.
 */
export default {
    name: 'DashboardHeatMap',
    props: {
        heatMapData: Array
    },
    data() {
        return {
            /** Toggle state for showing all items or just the initial set */
            showAll: false,
            /** Number of columns in the grid (responsive) */
            gridColumns: 4,
            /** Number of items to show initially before expanding */
            initialItemsToShow: 8
        }
    },
    mounted() {
        // Initialize responsive layout calculation on mount
        this.calculateGridColumns();
        window.addEventListener('resize', this.calculateGridColumns);
    },
    beforeUnmount() {
        // Clean up event listener
        window.removeEventListener('resize', this.calculateGridColumns);
    },
    computed: {
        /** Returns the subset of items to display based on the toggle state */
        visibleItems() {
            return this.showAll ? this.heatMapData : this.heatMapData.slice(0, this.initialItemsToShow);
        }
    },
    methods: {
        /**
         * Adjusts the grid layout based on the window width.
         */
        calculateGridColumns() {
            const width = window.innerWidth;
            if (width >= 1920) {
                this.gridColumns = 4;
                this.initialItemsToShow = 12;
            } else if (width >= 1440) {
                this.gridColumns = 3;
                this.initialItemsToShow = 9;
            } else if (width >= 1024) {
                this.gridColumns = 3;
                this.initialItemsToShow = 9;
            } else if (width >= 768) {
                this.gridColumns = 2;
                this.initialItemsToShow = 6;
            } else {
                this.gridColumns = 1;
                this.initialItemsToShow = 4;
            }
        },
        /**
         * Returns the appropriate CSS class for the alert icon based on alert count.
         * @param {number} alertCount
         */
        getHeatmapAlertClass(alertCount) {
            if (alertCount > 15) return 'alert-high';
            return 'alert-medium';
        },
        /**
         * Returns the CSS class for the color indicator bars based on occupancy percentage.
         * @param {number} occupation - Percentage value
         */
        getHeatmapIndicatorBars(occupation) {
            if (occupation >= 80) {
                return 'bar-red';
            } else if (occupation >= 70) {
                return 'bar-orange-red';
            } else if (occupation >= 50) {
                return 'bar-yellow-orange';
            } else {
                return 'bar-green-yellow';
            }
        }
    }
}
</script>

<style scoped>
.dashboard-panel {
    display: flex;
    flex-direction: column;
    min-height: 0;
}

.dashboard-header {
    margin-bottom: 25px;
    padding-bottom: 15px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    flex-shrink: 0;
}

.dashboard-header h2 {
    margin: 0;
    font-size: 1.8rem;
    font-weight: 600;
    color: white;
}

.dashboard-header p {
    margin: 5px 0 0 0;
    font-size: 1rem;
    color: #b4b4b7;
}

.heatmap-grid {
    display: grid;
    grid-template-columns: repeat(var(--columns), minmax(280px, 1fr));
    gap: 18px;
    flex: 1;
    min-height: 0;
    align-content: start;
}

.heatmap-item {
    background: linear-gradient(145deg, #040f50, #163140);
    border-radius: 12px;
    padding: 20px 25px;
    display: flex;
    flex-direction: column;
    gap: 15px;
    border: 1px solid rgba(255, 255, 255, 0.1);
    height: fit-content;
}

.heatmap-item:hover {
    background: rgba(255, 255, 255, 0.15);
    transform: translateY(-2px);
}

.item-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.item-header h3 {
    margin: 0;
    font-size: 1.25rem;
    font-weight: 600;
    color: #e2e8f0;
}

.alert-icon {
    font-size: 0.8rem;
    font-weight: 600;
    padding: 4px 8px;
    border-radius: 6px;
    display: flex;
    align-items: center;
    gap: 5px;
}

.alert-icon i {
    font-size: 0.9rem;
}

.alert-icon.alert-high {
    background-color: rgba(233, 67, 90, 0.2);
    color: #E9435A;
}

.alert-icon.alert-medium {
    background-color: rgba(249, 168, 37, 0.2);
    color: #F9A825;
}

.item-stats {
    display: flex;
    justify-content: space-between;
    background: rgba(2, 9, 49, 0.6);
    padding: 15px;
    border-radius: 8px;
}

.stat {
    display: flex;
    flex-direction: column;
    align-items: center;
    color: #9ca3af;
    font-size: 0.85rem;
}

.stat span {
    font-size: 1.2rem;
    font-weight: 600;
    color: white;
    margin-bottom: 4px;
}

.indicator-bars {
    width: 100%;
    height: 10px;
    border-radius: 5px;
    display: flex;
    overflow: hidden;
    background: rgba(0, 0, 0, 0.2);
}

.indicator-bars .bar {
    flex: 1;
    height: 100%;
    transition: background-color 0.3s ease;
}

.indicator-bars.bar-red .bar { 
    background-color: #E9435A; 
}

.indicator-bars.bar-orange-red .bar-1 { 
    background-color: #F9A825;
}
.indicator-bars.bar-orange-red .bar-2 { 
    background-color: #E9435A;
}

.indicator-bars.bar-yellow-orange .bar-1 { 
    background-color: #FFD700;
}
.indicator-bars.bar-yellow-orange .bar-2 { 
    background-color: #F9A825;
}

.indicator-bars.bar-green-yellow .bar-1 { 
    background-color: #2D9C92;
}
.indicator-bars.bar-green-yellow .bar-2 { 
    background-color: #FFD700;
}

.view-controls {
    display: flex;
    justify-content: center;
    margin-top: 15px;
    padding-top: 15px;
    border-top: 1px solid rgba(255, 255, 255, 0.1);
    flex-shrink: 0;
}

.view-toggle {
    background: rgba(255, 255, 255, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.2);
    color: white;
    padding: 8px 16px;
    border-radius: 6px;
    cursor: pointer;
    font-size: 0.9rem;
    transition: all 0.3s ease;
}

.view-toggle:hover {
    background: rgba(255, 255, 255, 0.15);
    transform: translateY(-1px);
}

.no-data-message {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100%;
    min-height: 150px;
    color: #999;
    font-style: italic;
    font-size: 14px;
}

@media (max-width: 768px) {
    .heatmap-grid {
        grid-template-columns: 1fr !important;
    }
    
    .heatmap-item {
        padding: 15px 20px;
    }
    
    .dashboard-header h2 {
        font-size: 1.5rem;
    }
}
</style>