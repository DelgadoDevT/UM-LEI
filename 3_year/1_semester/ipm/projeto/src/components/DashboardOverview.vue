<template>
<div class="dashboard-panel overview-dashboard">
    <div class="stats-grid">
        <div class="stat-card">
            <div class="stat-icon">
                <i class="fas fa-home"></i>
            </div>
            <div class="stat-value">{{ proprietesNum > 0 ? proprietesNum : '---' }}</div>
            <div class="stat-title">Total Listings</div>
            <div class="stat-label">Active Properties</div>
        </div>
        
        <div class="stat-card">
            <div class="stat-icon">
                <i class="fas fa-euro-sign"></i>
            </div>
            <div class="stat-value">€{{ proprietesNum > 0 ? overviewStats.averagePrice : '---' }}</div>
            <div class="stat-title">Average Price</div>
            <div class="stat-label">Per night</div>
        </div>
        
        <div class="stat-card">
            <div class="stat-icon">
                <i class="fas fa-calendar-alt"></i>
            </div>
            <div class="stat-value">{{ proprietesNum > 0 ? overviewStats.averageOccupancy : '---'}} days</div>
            <div class="stat-title">Average Occupancy</div>
            <div class="stat-label">Per year</div>
        </div>
        
        <div class="stat-card">
            <div class="stat-icon">
                <i class="fas fa-chart-line"></i>
            </div>
            <div class="stat-value">{{ proprietesNum > 0 ? overviewStats.highOccupancy : '---'}}</div>
            <div class="stat-title">High Occupancy</div>
            <div class="stat-label">> 300 days/year</div>
        </div>
    </div>
    
    <div class="charts-layout">
        <div class="city-comparison-full">
            <div class="chart-header">
                <h3>City Comparison ({{Object.keys(cityComparison).length}} most listed)</h3>
                <p>{{ cityComparisonTitle || 'Comparative analysis...' }}</p>
            </div>
            <div class="city-comparison-chart">
                <div class="chart-main" style="min-height: 175px;">
                    <template v-if="Object.keys(cityComparison).length > 0">
                        <div class="chart-y-axis">
                            <span>{{ cityComparisonMax }}</span>
                            <span>{{ Math.round(cityComparisonMax * 0.75) }}</span>
                            <span>{{ Math.round(cityComparisonMax * 0.5) }}</span>
                            <span>{{ Math.round(cityComparisonMax * 0.25) }}</span>
                            <span>0</span>
                        </div>
                        <div class="chart-bars">
                            <div v-for="(cityData, cityName) in cityComparison" :key="cityName" class="city-bar">
                                <div class="bar-group">
                                    <div class="bar occupancy" :style="{height: (cityData.occupancyRate / cityComparisonMax * 100) + '%'}">
                                        <span class="bar-value-label">{{ cityData.occupancyRate }}</span>
                                    </div>
                                    <div class="bar price" :style="{height: (cityData.averagePrice / cityComparisonMax * 100) + '%'}">
                                        <span class="bar-value-label">{{ cityData.averagePrice }}</span>
                                    </div>
                                    <div class="bar listings" :style="{height: (cityData.totalListings / cityComparisonMax * 100) + '%'}">
                                        <span class="bar-value-label">{{ cityData.totalListings }}</span>
                                    </div>
                                </div>
                                <div class="city-name">{{ cityName }}</div>
                            </div>
                        </div>
                    </template>
                    <div class="no-data-message" v-else>
                        No data available for the selected filters.
                    </div>
                </div>
                <div class="chart-legend">
                    <div class="legend-item">
                        <span class="legend-color occupancy"></span>
                        <span>Occupancy Rate (%)</span>
                    </div>
                    <div class="legend-item">
                        <span class="legend-color price"></span>
                        <span>Average Price (€)</span>
                    </div>
                    <div class="legend-item">
                        <span class="legend-color listings"></span>
                        <span>Total Listings</span>
                    </div>
                </div>
            </div>
        </div>
        
        <div class="half-width-charts">
            <div class="chart-card" id="municipality-chart-card">
                <div class="chart-header">
                    <div class="chart-title-row">
                        <h3>Prices by Municipality</h3>
                        <div class="chart-actions">
                            <div class="share-dropdown-container">
                                <button class="action-btn" @click="toggleShareDropdown('municipality')">Share</button>
                                <div v-if="showMunicipalityShare" class="share-options">
                                    <button @click="shareChart('Prices by Municipality', 'twitter')" class="share-option twitter">
                                        <i class="fab fa-twitter"></i> Twitter
                                    </button>
                                    <button @click="shareChart('Prices by Municipality', 'facebook')" class="share-option facebook">
                                        <i class="fab fa-facebook-f"></i> Facebook
                                    </button>
                                    <button @click="shareChart('Prices by Municipality', 'linkedin')" class="share-option linkedin">
                                        <i class="fab fa-linkedin-in"></i> LinkedIn
                                    </button>
                                    <button @click="shareChart('Prices by Municipality', 'copy')" class="share-option copy">
                                        <i class="fas fa-link"></i> Copy Link
                                    </button>
                                </div>
                            </div>
                            <div class="download-dropdown-container"> 
                                <button class="action-btn" @click="toggleDownloadDropdown('municipality')">Download</button>
                                <div v-if="showMunicipalityDownload" class="download-options">
                                    <button @click="downloadChart('municipality-chart-card', 'png')" class="download-option">
                                        <i class="fas fa-file-image"></i> PNG
                                    </button>
                                    <button @click="downloadChart('municipality-chart-card', 'jpeg')" class="download-option">
                                        <i class="fas fa-file-image"></i> JPEG
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                    <p>Top 8 municipalities by average price</p>
                </div>
                
                <div class="bars-list" v-if="municipalityPrices.length > 0">
                    <div v-for="municipality in municipalityPrices" :key="municipality.name" class="municipality-bar">
                        <span class="bar-label">{{ municipality.name }}</span>
                        <div class="bar-container">
                            <div class="bar-fill" :style="{width: (municipality.price / municipalityMaxPrice * 100) + '%'}">
                                <span class="bar-value">€{{ municipality.price }}</span>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="no-data-message" v-else>
                    No data available for the selected filters.
                </div>
            </div>
            
            <div class="chart-card" id="property-type-chart-card">
                <div class="chart-header">
                    <div class="chart-title-row">
                        <h3>Property Type Distribution</h3>
                        <div class="chart-actions">
                            <div class="share-dropdown-container">
                                <button class="action-btn" @click="toggleShareDropdown('propertyType')">Share</button>
                                <div v-if="showPropertyTypeShare" class="share-options">
                                    <button @click="shareChart('Property Type Distribution', 'twitter')" class="share-option twitter">
                                        <i class="fab fa-twitter"></i> Twitter
                                    </button>
                                    <button @click="shareChart('Property Type Distribution', 'facebook')" class="share-option facebook">
                                        <i class="fab fa-facebook-f"></i> Facebook
                                    </button>
                                    <button @click="shareChart('Property Type Distribution', 'linkedin')" class="share-option linkedin">
                                        <i class="fab fa-linkedin-in"></i> LinkedIn
                                    </button>
                                    <button @click="shareChart('Property Type Distribution', 'copy')" class="share-option copy">
                                        <i class="fas fa-link"></i> Copy Link
                                    </button>
                                </div>
                            </div>
                            <div class="download-dropdown-container">
                                <button class="action-btn" @click="toggleDownloadDropdown('propertyType')">Download</button>
                                <div v-if="showPropertyTypeDownload" class="download-options">
                                    <button @click="downloadChart('property-type-chart-card', 'png')" class="download-option">
                                        <i class="fas fa-file-image"></i> PNG
                                    </button>
                                    <button @click="downloadChart('property-type-chart-card', 'jpeg')" class="download-option">
                                        <i class="fas fa-file-image"></i> JPEG
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                    <p>Breakdown of accommodation types</p>
                </div>
                
                <div class="property-type-distribution">
                    <div class="pie-chart-container">
                        <div class="pie-chart" :style="overviewPieChartStyle"></div>
                    </div>
                    <div class="distribution-legend">
                        <div class="legend-item">
                            <span class="legend-dot home"></span>
                            <span>Home/Apartment {{ propertyTypes.entireHomes }}%</span>
                        </div>
                        <div class="legend-item">
                            <span class="legend-dot private"></span>
                            <span>Private Rooms {{ propertyTypes.privateRooms }}%</span>
                        </div>
                        <div class="legend-item">
                            <span class="legend-dot shared"></span>
                            <span>Shared Rooms {{ propertyTypes.sharedRooms }}%</span>
                        </div>
                        <div class="legend-item">
                            <span class="legend-dot hotel"></span>
                            <span>Hotel Rooms {{ propertyTypes.hotelRooms }}%</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="time-analysis-dashboard">
            
            <div class="time-analysis-content">
                <div class="dashboard-header">
                    <h2>Temporal Evolution (Last Months)</h2>
                    <p>Time series analysis for research</p>
                </div>

                <template v-if="timeSeriesMonths.length > 0">
                <div class="chart-legend">
                    <div class="legend-item">
                        <span class="legend-color price"></span>
                        <span>Average Price (€)</span>
                    </div>
                    <div class="legend-item">
                        <span class="legend-color occupancy"></span>
                        <span>Occupancy Rate (%)</span>
                    </div>
                    <div class="legend-item">
                        <span class="legend-color listings"></span>
                        <span>Total Listings</span>
                    </div>
                </div>

                <div class="time-chart-container">
                    <div class="time-y-axis">
                        <span>{{ timeSeriesMaxY }}</span>
                        <span>{{ Math.round(timeSeriesMaxY * 0.8) }}</span>
                        <span>{{ Math.round(timeSeriesMaxY * 0.6) }}</span>
                        <span>{{ Math.round(timeSeriesMaxY * 0.4) }}</span>
                        <span>{{ Math.round(timeSeriesMaxY * 0.2) }}</span>
                        <span>0</span>
                    </div>

                    <div class="time-chart-area">
                        <div class="grid-lines">
                            <div class="grid-line"></div>
                            <div class="grid-line"></div>
                            <div class="grid-line"></div>
                            <div class="grid-line"></div>
                            <div class="grid-line"></div>
                        </div>

                        <div class="chart-lines">
                            <svg class="line-svg" viewBox="0 0 1000 200" preserveAspectRatio="none">
                                <path class="line-path price-line" :d="getLinePath(timeSeriesData.price)" />
                                <path class="line-path occupancy-line" :d="getLinePath(timeSeriesData.occupancy)" />
                                <path class="line-path listings-line" :d="getLinePath(timeSeriesData.listings)" />
                            </svg>
                        </div>

                        <div class="data-points">
                            <div v-for="(point, index) in timeSeriesData.price" :key="'price-'+index" 
                                class="data-point price-point" 
                                :style="getPointPosition(point, index)"
                                @mouseenter="showTimeTooltip($event, 'Price', point, index)"
                                @mouseleave="hideTimeTooltip">
                            </div>
                            <div v-for="(point, index) in timeSeriesData.occupancy" :key="'occupancy-'+index" 
                                class="data-point occupancy-point" 
                                :style="getPointPosition(point, index)"
                                @mouseenter="showTimeTooltip($event, 'Occupancy', point, index)"
                                @mouseleave="hideTimeTooltip">
                            </div>
                            <div v-for="(point, index) in timeSeriesData.listings" :key="'listings-'+index" 
                                class="data-point listings-point" 
                                :style="getPointPosition(point, index)"
                                @mouseenter="showTimeTooltip($event, 'Listings', point, index)"
                                @mouseleave="hideTimeTooltip">
                            </div>
                        </div>
                    </div>
                </div>

                <div class="time-x-axis">
                    <span v-for="month in timeSeriesMonths" :key="month" class="month-label">
                        {{ month }}
                    </span>
                </div>

                </template>
                <div class="no-data-message" v-else>
                    No data available for the selected filters.
                </div>
            </div>
        </div>

        <div class="world-map-card">
            <div class="chart-header">
                <h3>Property Locations</h3>
                <p>Interactive map showing individual property locations</p>
            </div>
            <div id="world-map" v-if="worldMapData.length > 0"></div>
            <div class="map-stats" v-if="worldMapData.length > 0">
                <span>Showing {{ worldMapData.length }} properties</span>
            </div>
            <div class="no-data-message" v-else>
                No property location data available.
            </div>
        </div>
    </div>

    <!-- Tooltip -->
    <div v-if="tooltip.visible" class="chart-tooltip" :style="{ top: tooltip.top, left: tooltip.left }">
        {{ tooltip.content }}
    </div>
</div>
</template>

<script>
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import 'leaflet.markercluster/dist/MarkerCluster.css';
import 'leaflet.markercluster/dist/MarkerCluster.Default.css';
import 'leaflet.markercluster';

export default {
    name: 'DashboardOverview',
    props: {
        proprietesNum: Number,
        overviewStats: Object,
        cityComparison: Object,
        cityComparisonTitle: String,
        cityComparisonMax: Number,
        municipalityPrices: Array,
        municipalityMaxPrice: Number,
        propertyTypes: Object,
        overviewPieChartStyle: Object,
        timeSeriesMonths: Array,
        timeSeriesData: Object,
        timeSeriesMaxY: Number,
        worldMapData: Array
    },
    data() {
        return {
            showMunicipalityDownload: false,
            showPropertyTypeDownload: false,
            showMunicipalityShare: false,
            showPropertyTypeShare: false,
            tooltip: {
                visible: false,
                top: '0px',
                left: '0px',
                content: ''
            },
            worldMap: null
        }
    },
    watch: {
        // Reinitialize map when data changes
        worldMapData: {
            handler(newData) {
                if (newData && newData.length > 0) {
                    this.$nextTick(() => {
                        this.initWorldMap();
                    });
                }
            },
            immediate: true
        }
    },
    methods: {
        // Time series chart tooltip
        showTimeTooltip(event, type, value, index) {
            const month = this.timeSeriesMonths[index] || 'Unknown';
            let displayValue = value;
            
            if (type === 'Price') {
                displayValue = `€${Math.round(value)}`;
            } else if (type === 'Occupancy') {
                displayValue = `${Math.round(value)}%`;
            } else if (type === 'Listings') {
                displayValue = `${Math.round((value / 100) * this.proprietesNum)} listings`;
            }
            
            this.tooltip.content = `${type}: ${displayValue} (${month})`;
            this.tooltip.top = `${event.pageY - 40}px`;
            this.tooltip.left = `${event.pageX}px`;
            this.tooltip.visible = true;
        },
        
        hideTimeTooltip() {
            this.tooltip.visible = false;
        },

        // Toggle download dropdowns
        toggleDownloadDropdown(chart) {
            if (chart === 'municipality') {
                this.showMunicipalityDownload = !this.showMunicipalityDownload;
                this.showPropertyTypeDownload = false;
            } else if (chart === 'propertyType') {
                this.showPropertyTypeDownload = !this.showPropertyTypeDownload;
                this.showMunicipalityDownload = false;
            }
            this.showMunicipalityShare = false;
            this.showPropertyTypeShare = false;
        },

        // Toggle share dropdowns
        toggleShareDropdown(chart) {
            if (chart === 'municipality') {
                this.showMunicipalityShare = !this.showMunicipalityShare;
                this.showPropertyTypeShare = false;
            } else if (chart === 'propertyType') {
                this.showPropertyTypeShare = !this.showPropertyTypeShare;
                this.showMunicipalityShare = false;
            }
            this.showMunicipalityDownload = false;
            this.showPropertyTypeDownload = false;
        },

        // Download chart as image
        downloadChart(elementId, format) {
            this.showMunicipalityDownload = false;
            this.showPropertyTypeDownload = false;
            this.showMunicipalityShare = false;
            this.showPropertyTypeShare = false;

            this.$nextTick(() => {
                if (typeof html2canvas === 'undefined') {
                    console.error('html2canvas is not loaded.');
                    alert('Error: Download feature is unavailable.');
                    return;
                }

                const elementToCapture = document.getElementById(elementId);
                if (!elementToCapture) {
                    console.error('Chart element not found:', elementId);
                    return;
                }

                html2canvas(elementToCapture, {
                    backgroundColor: '#020931',
                    useCORS: true,
                    scale: 2
                }).then(canvas => {
                    const link = document.createElement('a');
                    link.href = canvas.toDataURL(format === 'jpeg' ? 'image/jpeg' : 'image/png', 1.0);
                    link.download = `${elementId}_${new Date().toISOString().split('T')[0]}.${format}`;
                    document.body.appendChild(link);
                    link.click();
                    document.body.removeChild(link);
                }).catch(err => {
                    console.error('Error generating chart image:', err);
                    alert('An error occurred while downloading the chart.');
                });
            });
        },

        // Share chart on social media
        shareChart(chartName, platform) {
            this.showMunicipalityShare = false;
            this.showPropertyTypeShare = false;

            const text = encodeURIComponent(`Checking out ${chartName} on my InsideAirbnb Analytics dashboard!`);
            const url = encodeURIComponent(window.location.href);
            
            let shareUrl = '';
            
            switch (platform) {
                case 'twitter':
                    shareUrl = `https://twitter.com/intent/tweet?text=${text}&url=${url}`;
                    break;
                case 'facebook':
                    shareUrl = `https://www.facebook.com/sharer/sharer.php?u=${url}&quote=${text}`;
                    break;
                case 'linkedin':
                    shareUrl = `https://www.linkedin.com/sharing/share-offsite/?url=${url}`;
                    break;
                case 'copy':
                    navigator.clipboard.writeText(window.location.href)
                        .then(() => {
                            alert('Link copied to clipboard!');
                        })
                        .catch(err => {
                            console.error('Failed to copy link: ', err);
                            alert('Failed to copy link to clipboard.');
                        });
                    return;
                default:
                    console.warn('Unknown platform:', platform);
                    return;
            }
            
            if (shareUrl) {
                window.open(shareUrl, '_blank', 'width=600,height=400');
            }
        },

        // Generate SVG path for time series lines
        getLinePath(data) {
            const maxValue = this.timeSeriesMaxY; 
            const width = 1000;
            const height = 200;
            if (!data || data.length === 0) return "M 0 200";
            
            const pointsData = data.length === 1 ? [data[0], data[0]] : data;
            const length = pointsData.length - 1;

            const points = pointsData.map((value, index) => {
                const x = (index / length) * width;
                const yValue = Math.min(value, maxValue);
                const y = height - (yValue / maxValue) * height;
                return `${x},${y}`;
            });
            return `M ${points.join(' L ')}`;
        },

        // Calculate position for data points
        getPointPosition(value, index) {
            const maxValue = this.timeSeriesMaxY;
            const length = (this.timeSeriesData.price.length === 1) ? 1 : (this.timeSeriesData.price.length - 1);
            const x = (index / length) * 100;
            const yValue = Math.min(value, maxValue);
            const y = 100 - (yValue / maxValue) * 100;
            
            return {
                left: `${x}%`,
                top: `${y}%`
            };
        },

        // Initialize Leaflet world map
        initWorldMap() {
            this.$nextTick(() => {
                const mapElement = document.getElementById('world-map');
                if (!mapElement) return;
                
                if (this.worldMap) {
                    this.worldMap.remove();
                }
                
                // Only initialize map if there's data
                if (this.worldMapData && this.worldMapData.length > 0) {
                    this.worldMap = L.map('world-map').setView([30, 0], 2);
                    
                    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                        attribution: '© OpenStreetMap contributors',
                        maxZoom: 17
                    }).addTo(this.worldMap);
                    
                    const markers = L.markerClusterGroup({
                        chunkedLoading: true,
                        maxClusterRadius: 50,
                        disableClusteringAtZoom: 17, 
                        spiderfyOnMaxZoom: true,
                        showCoverageOnHover: false,
                        zoomToBoundsOnClick: true,
                        maxClusterRadius: function(zoom) {
                            return zoom > 15 ? 30 : 50;
                        }
                    });
                    
                    this.worldMapData.forEach(property => {
                        const marker = L.marker([property.lat, property.lng])
                            .bindPopup(`
                                <div class="map-popup">
                                    <strong>${property.name}</strong><br>
                                    ${property.city ? `City: ${property.city}<br>` : ''}
                                    ${property.country ? `Country: ${property.country}<br>` : ''}
                                    ${property.price ? `Price: €${property.price}<br>` : ''}
                                    ${property.occupancy ? `Occupancy: ${property.occupancy}%<br>` : ''}
                                    ${property.url ? `<a href="${property.url}" target="_blank" style="color: #2D9C92; font-weight: bold;">View on Airbnb</a>` : ''}
                                </div>
                            `);
                        
                        markers.addLayer(marker);
                    });
                    
                    this.worldMap.addLayer(markers);
                    
                    if (this.worldMapData.length > 0) {
                        this.worldMap.fitBounds(markers.getBounds(), { 
                            padding: [20, 20],
                            maxZoom: 17
                        });
                    }
                    
                    this.worldMap.on('zoomend', () => {
                        const currentZoom = this.worldMap.getBounds();
                        console.log(`Current zoom: ${currentZoom}`);
                    });
                } else {
                    // Hide map container when no data
                    mapElement.style.display = 'none';
                }
            });
        }
    },
    mounted() {
        if (this.worldMapData && this.worldMapData.length > 0) {
            this.$nextTick(() => {
                this.initWorldMap();
            });
        }
    },

    beforeUnmount() {
        if (this.worldMap) {
            this.worldMap.remove();
            this.worldMap = null;
        }
    },
}
</script>

<style scoped>
.overview-dashboard {
    background: transparent;
}

.stats-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
    margin-bottom: 40px;
}

.stat-card {
    background: rgba(2, 9, 49, 0.4);
    padding: 25px;
    border-radius: 12px;
    border: 1px solid rgba(255, 255, 255, 0.1);
    position: relative;
}

.stat-icon {
    font-size: 2rem;
    color: white;
    margin-bottom: 15px;
}

.stat-value {
    font-size: 1.5rem;
    font-weight: 700;
    color: white;
    margin-bottom: 8px;
}

.stat-title {
    font-size: 0.9rem;
    font-weight: 600;
    color: white;
    margin-bottom: 8px;
}

.stat-label {
    font-size: 0.85rem;
    color: #b4b4b7;
    margin-bottom: 8px;
}

.charts-layout {
    display: flex;
    flex-direction: column;
    gap: 30px;
}

.half-width-charts {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 30px;
}

.city-comparison-full {
    background: rgba(2, 9, 49, 0.4);
    padding: 25px;
    border-radius: 12px;
    border: 1px solid rgba(255, 255, 255, 0.1);
}

.city-comparison-full .chart-header h3,
.city-comparison-full .chart-header p {
    color: white;
    text-align: center;
}

.city-comparison-full .chart-header h3 {
    font-size: 1.5rem;
    margin-bottom: 5px;
}

.city-comparison-full .chart-header p {
    font-size: 1rem;
    opacity: 0.9;
}

.chart-bars {
    display: flex;
    justify-content: center;
    gap: 60px;
    flex: 1;
    margin-top: 20px;
    height: 175px;
}

.city-comparison-chart .city-bar {
    display: flex;
    flex-direction: column;
    height: 100%; 
}

.chart-main {
    display: flex;
    gap: 15px;
    height: 200px;
    justify-content: center;
    margin-bottom: 10px;
    position: relative;
}

.chart-y-axis {
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    height: 100%;
    padding-right: 10px;
    border-right: 2px solid rgba(255, 255, 255, 0.3);
    font-size: 0.8rem;
    color: white;
    text-align: right;
    width: 40px;
    flex-shrink: 0;
    margin-top: 15px;
    height: 75%;
}

.bar {
    width: 25px;
    border-radius: 4px 4px 0 0;
    min-height: 5px;
    display: flex;
    align-items: flex-end; 
    justify-content: center; 
}

.bar-group {
    display: flex;
    align-items: flex-end;
    gap: 8px;
    height: 100%;
}

.bar-value-label {
    display: block;
    text-align: center;
    color: white;
    font-size: 0.85rem;
    font-weight: 600;
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
    line-height: 1;
    padding-bottom: 5px; 
}

.bar.occupancy { background: #2D9C92; } 
.bar.price { background: #3CC3DF; } 
.bar.listings { background: #F9A825; } 

.city-name {
    text-align: center;
    margin-top: 10px;
    font-weight: 500;
    color: white;
}

.chart-card {
    background: rgba(2, 9, 49, 0.4);
    padding: 25px 25px 25px 25px;
    border-radius: 12px;
    border: 1px solid rgba(255, 255, 255, 0.1);
}

.chart-card .chart-header h3,
.chart-card .chart-header p,
.municipality-bar .bar-label,
.property-type-section h4,
.distribution-legend .legend-item span {
    color: white;
}

.chart-card .chart-header p {
    opacity: 0.9;
}

.chart-header h3 {
    margin: 0 0 8px 0;
    font-size: 1.3rem;
}

.chart-header p {
    margin: 0 0 20px 0;
}

.chart-title-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 5px;
}

.chart-actions {
    display: flex;
    gap: 8px;
}

.action-btn {
    padding: 6px 12px;
    border: 1px solid #2D9C92;
    background: #163140;
    border-radius: 4px;
    font-size: 0.8rem;
    cursor: pointer;
    color: white;
    transition: all 0.3s ease;
}

.action-btn:hover {
    background: #2D9C92;
    border-color: #2D9C92;
}

.municipality-bars {
    margin-bottom: 30px;
}

.municipality-bar {
    display: flex;
    align-items: center;
    margin-bottom: 12px;
    gap: 15px;
}

.bar-label {
    width: 120px;
    font-size: 0.9rem;
    font-weight: 500;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.bar-container {
    flex: 1;
    background: rgba(255, 255, 255, 0.2);
    height: 25px;
    border-radius: 12px;
}

.bar-fill {
    background: linear-gradient(90deg, #2D9C92, #163140);
    height: 100%;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: flex-end;
    padding-right: 10px;
    min-width: 50px;
}

.bar-value {
    color: white;
    font-size: 0.8rem;
    font-weight: 600;
}

.property-type-section {
    margin-top: 25px;
    padding-top: 20px;
    border-top: 1px solid rgba(255, 255, 255, 0.2);
}

.property-type-section h4 {
    margin-bottom: 15px;
}

.property-type-distribution {
    display: flex;
    align-items: center;
    gap: 40px;
    justify-content: center;
}

.pie-chart {
    width: 180px;
    height: 180px;
    border-radius: 50%;
    background: conic-gradient(
        #2D9C92 0% 29%,
        #163140 29% 53%,
        #40017a 53% 77%,
        #6c757d 77% 100%
    );
    flex-shrink: 0;
}

.distribution-legend {
    display: grid;
    grid-template-columns: 1fr;
    gap: 12px;
}

.legend-dot {
    width: 12px;
    height: 12px;
    border-radius: 50%;
    display: inline-block;
    flex-shrink: 0;
    margin-right: 8px;
}

.legend-dot.home { background: #2D9C92; }
.legend-dot.private { background: #163140; }
.legend-dot.shared { background: #40017a; }
.legend-dot.hotel { background: #F9A825; }

.chart-legend {
    display: flex;
    justify-content: center;
    flex-wrap: wrap;
    gap: 20px;
    margin-top: 15px;
}

.legend-item {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 0.95rem;
    color: white;
}

.legend-color {
    width: 12px;
    height: 12px;
    border-radius: 2px;
}

.legend-color.occupancy { background: #2D9C92; }
.legend-color.price { background: #3CC3DF; }
.legend-color.listings { background: #F9A825; }

.world-map-card {
    background: rgba(2, 9, 49, 0.4);
    padding: 25px;
    border-radius: 12px;
    border: 1px solid rgba(255, 255, 255, 0.1);
    width: 100%;
    margin-top: 30px;
}

.world-map-card .chart-header h3,
.world-map-card .chart-header p {
    color: white;
    text-align: center;
}

.world-map-card .chart-header h3 {
    font-size: 1.5rem;
    margin-bottom: 5px;
}

.world-map-card .chart-header p {
    font-size: 1rem;
    opacity: 0.9;
    margin-bottom: 20px;
}

.map-stats {
    text-align: center;
    margin-top: 10px;
    color: #b4b4b7;
    font-size: 0.9rem;
}

#world-map {
    height: 500px;
    width: 100%;
    border-radius: 8px;
}

#world-map:empty {
    display: none;
}

.leaflet-container {
    background: rgba(255, 255, 255, 0.95) !important;
    border-radius: 8px;
}

.time-analysis-dashboard {
    width: 100%;
}

.time-analysis-content {
    background: rgba(2, 9, 49, 0.4);
    padding: 25px;
    border-radius: 12px;
    border: 1px solid rgba(255, 255, 255, 0.1);
}

.time-analysis-content .dashboard-header {
    margin-bottom: 0; 
    text-align: center;
}

.time-analysis-content .dashboard-header h2,
.time-analysis-content .dashboard-header p {
    color: white;
}

.time-chart-container {
    display: flex;
    height: 300px; 
    margin: 20px 0;
}

.time-y-axis {
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    padding-right: 15px;
    border-right: 2px solid rgba(255, 255, 255, 0.3);
    font-size: 0.8rem;
    color: white;
    text-align: right;
    width: 40px;
    flex-shrink: 0;
    padding-top: 5px; 
    padding-bottom: 5px;
    height: 100%;
}

.time-chart-area {
    flex: 1;
    position: relative;
    margin-left: 15px;
    background: transparent;
}

.grid-lines {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    display: flex;
    flex-direction: column;
    justify-content: space-between; 
}

.grid-line {
    border-top: 1px solid rgba(255, 255, 255, 0.2);
    width: 100%;
}

.grid-lines::after {
    content: '';
    display: block;
    border-top: 1px solid rgba(255, 255, 255, 0.2);
    width: 100%;
    position: absolute;
    bottom: 0;
}

.chart-lines {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
}

.line-svg {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    overflow: visible;
}

.line-path {
    fill: none;
    stroke-width: 3;
    stroke-linecap: round;
    stroke-linejoin: round;
}

.price-line { 
    stroke: #3CC3DF; 
    stroke-dasharray: 2000;
    stroke-dashoffset: 2000;
    animation: drawLine 2s ease-in-out forwards;
}

.occupancy-line { 
    stroke: #2D9C92; 
    stroke-dasharray: 2000;
    stroke-dashoffset: 2000;
    animation: drawLine 2s ease-in-out 0.3s forwards;
}

.listings-line { 
    stroke: #F9A825; 
    stroke-dasharray: 2000;
    stroke-dashoffset: 2000;
    animation: drawLine 2s ease-in-out 0.6s forwards;
}

@keyframes drawLine {
    to {
        stroke-dashoffset: 0;
    }
}

.data-points {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
}

.data-point {
    position: absolute;
    width: 8px;
    height: 8px;
    border-radius: 50%;
    transform: translate(-50%, -50%);
    border: 2px solid white;
    opacity: 1;
    cursor: pointer;
    transition: transform 0.2s ease;
    z-index: 10;
}

.data-point:hover {
    transform: translate(-50%, -50%) scale(1.8);
}

.price-point { background: #3CC3DF; }
.occupancy-point { background: #2D9C92; }
.listings-point { background: #F9A825; }

.time-x-axis {
    display: flex;
    justify-content: space-between;
    padding: 0 0 0 55px;
    margin-top: 10px;
}

.month-label {
    font-size: 0.8rem;
    color: white;
    text-align: center;
    flex: 1;
}

.chart-tooltip {
    position: fixed;
    background: rgba(12, 26, 44, 0.9);
    color: white;
    padding: 8px 12px;
    border-radius: 5px;
    font-size: 0.9rem;
    font-weight: 600;
    pointer-events: none;
    transform: translate(-50%, -120%);
    transition: opacity 0.2s ease-in-out;
    white-space: nowrap;
    z-index: 9999;
    border: 1px solid rgba(255, 255, 255, 0.2);
    box-shadow: 0 4px 12px rgba(0,0,0,0.5);
}

.download-dropdown-container {
    position: relative;
    display: inline-block;
}

.download-options {
    position: absolute;
    top: 100%;
    right: 0;
    background: #0d1a2c; 
    border: 1px solid rgba(255, 255, 255, 0.2);
    border-radius: 4px;
    box-shadow: 0 4px 12px rgba(0,0,0,0.5);
    z-index: 10;
    width: 150px;
    margin-top: 5px;
}

.download-option {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px 15px;
    background: none;
    border: none;
    color: white;
    cursor: pointer;
    width: 100%;
    text-align: left;
    font-size: 0.9rem;
}

.download-option:hover {
    background: #1c3a5f;
}

.download-option i {
    margin-right: 5px;
}

.share-dropdown-container {
    position: relative;
    display: inline-block;
}

.share-options {
    position: absolute;
    top: 100%;
    right: 0;
    background: #0d1a2c; 
    border: 1px solid rgba(255, 255, 255, 0.2);
    border-radius: 4px;
    box-shadow: 0 4px 12px rgba(0,0,0,0.5);
    z-index: 10;
    width: 180px;
    margin-top: 5px;
}

.share-option {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px 15px;
    background: none;
    border: none;
    color: white;
    cursor: pointer;
    width: 100%;
    text-align: left;
    font-size: 0.9rem;
    transition: background-color 0.3s ease;
}

.share-option:hover {
    background: #1c3a5f;
}

.share-option.twitter:hover {
    background: #1da1f2;
}

.share-option.facebook:hover {
    background: #1877f2;
}

.share-option.linkedin:hover {
    background: #0a66c2;
}

.share-option.copy:hover {
    background: #2D9C92;
}

.share-option i {
    margin-right: 5px;
    width: 16px;
    text-align: center;
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
</style>