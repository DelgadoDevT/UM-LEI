<template>
<div id="dashboard-app">
    <TopBar :activeItem="activeItem" :proprietesNum="proprietesNum" />
    
    <div class="dashboard-container">
        <SideBar
            :user="user"
            :activeItem="activeItem"
            :discoverItems="discoverItems"
            :filters="filters"
            :filterData="filterData"
            :allCountries="allCountries"
            :filteredCities="filteredCities"
            :filteredMunicipalities="filteredMunicipalities"
            @set-active-item="setActiveItem"
            @clear-all-filters="clearAllFilters"
            @logout="logout"
        />

        <div class="main-content">
            <div class="content-area">
                <DashboardOverview
                    v-if="activeItem === 'Overview'"
                    :proprietesNum="proprietesNum"
                    :overviewStats="overviewStats"
                    :cityComparison="cityComparison"
                    :cityComparisonTitle="cityComparisonTitle"
                    :cityComparisonMax="cityComparisonMax"
                    :municipalityPrices="municipalityPrices"
                    :municipalityMaxPrice="municipalityMaxPrice"
                    :propertyTypes="propertyTypes"
                    :overviewPieChartStyle="overviewPieChartStyle"
                    :timeSeriesMonths="timeSeriesMonths"
                    :timeSeriesData="timeSeriesData"
                    :timeSeriesMaxY="timeSeriesMaxY"
                    :worldMapData="worldMapData"
                />

                <DashboardHeatMap
                    v-if="activeItem === 'Heat Map'"
                    :heatMapData="heatMapData"
                />

                <DashboardAlerts
                    v-if="activeItem === 'Alerts'"
                    :alertsData="alertsData"
                />

                <DashboardExecutive
                    v-if="activeItem === 'Executive'"
                    :proprietesNum="proprietesNum"
                    :executiveStats="executiveStats"
                    :irregularityTypes="irregularityTypes"
                    :priorityCases="priorityCases"
                    :pieChartStyle="pieChartStyle"
                />

                <DashboardData
                    v-if="activeItem === 'Data'"
                    :show-export-dropdown="showExportDropdown"
                    :listingsData="listingsData"
                    :currentPage="currentPage"
                    :itemsPerPage="itemsPerPage"
                    @update:currentPage="currentPage = $event"
                    @toggle-export-dropdown="showExportDropdown = !showExportDropdown"
                    @update:itemsPerPage="itemsPerPage = $event"
                />

                <div class="dashboard-footer">
                    <div class="footer-content">
                        <span class="footer-left">© 2025 InsideAirbnb Analytics - Interface Pessoa-Maquina LEI 25/26</span>
                        <span class="footer-right">Developed for Airbnb housing impact analysis</span>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
</template>

<script>
// Imports for components used in the dashboard layout
import TopBar from '../components/TopBar.vue'
import SideBar from '../components/SideBar.vue'
import DashboardOverview from '../components/DashboardOverview.vue'
import DashboardHeatMap from '../components/DashboardHeatMap.vue'
import DashboardAlerts from '../components/DashboardAlerts.vue'
import DashboardExecutive from '../components/DashboardExecutive.vue'
import DashboardData from '../components/DashboardData.vue'

/**
 * Dashboard Main View
 * * This is the central component of the application.
 * It manages the global state, handles data loading, filtering logic,
 * and coordinates the display of different dashboard sub-views (Overview, Heat Map, etc.).
 */
export default {
    name: 'Dashboard',
    components: {
        TopBar,
        SideBar,
        DashboardOverview,
        DashboardHeatMap,
        DashboardAlerts,
        DashboardExecutive,
        DashboardData
    },
    data() {
        // Retrieve user session from local storage
        const savedUser = JSON.parse(localStorage.getItem('loggedUser') || '{}');

        return {
            /** Current user information */
            user: {
                name: savedUser.name,
                role: savedUser.role,
                profilePic: savedUser.profilePic || savedUser.pic || null
            },
            /** Controls the currently visible dashboard panel */
            activeItem: 'Overview',
            /** Navigation items for the sidebar */
            discoverItems: [
                { name: 'Overview', icon: 'fas fa-chart-pie' },
                { name: 'Heat Map', icon: 'fas fa-map' },
                { name: 'Alerts', icon: 'fas fa-bell' },
                { name: 'Executive', icon: 'fas fa-chart-bar' },
                { name: 'Data', icon: 'fas fa-database' }
            ],
            /** Global filter state object */
            filters: {
                country: '',
                city: '',
                municipality: '',
                propertyType: '',
                dateStart: '',
                dateEnd: '',
                priceMin: '',
                priceMax: '',
                occupancyMin: '',
                occupancyMax: '',
                minReviews: 0,
                minRating: 0 
            },
            /** Stores available options for filters (locations, types) */
            filterData: {
                propertyTypes: [], 
                locations: [] 
            },
            /** Total count of filtered properties */
            proprietesNum: 0,
            /** Key statistics for the overview panel */
            overviewStats: {
                averagePrice: 0,
                averageOccupancy: 0,
                highOccupancy: 0
            },
            /** Data for comparing city metrics */
            cityComparison: {},
            /** Data for average prices by municipality */
            municipalityPrices: [],
            /** Breakdown of property types counts */
            propertyTypes: {},
            /** Data points for the world map visualization */
            worldMapData: [],
            /** Dynamic max Y-axis value for time series charts */
            timeSeriesMaxY: 100, 
            /** X-axis labels (months/dates) for time series */
            timeSeriesMonths: [], 
            /** Datasets for the time series chart (price, occupancy, listings) */
            timeSeriesData: {
                price: [],
                occupancy: [],
                listings: []
            },
            /** Data for the heatmap visualization */
            heatMapData: [],
            /** List of generated alerts based on property irregularities */
            alertsData: [],
            /** High-level stats for the executive summary view */
            executiveStats: {
                criticalCount: 0,
                compliance: { rate: 0, change: "+0%", label: "0 de 0 propriedades" },
                nonCompliant: { count: 0, label: "Critical" },
                fiscalImpact: { amount: "€0M", label: "Losses", sublabel: "Uncollected revenue (annual)" }
            },
            /** Distribution of irregularity types for charts */
            irregularityTypes: [],
            /** List of high-priority cases for executive review */
            priorityCases: [],
            /** Current page number for the data table pagination */
            currentPage: 1,
            /** Number of rows per page in the data table */
            itemsPerPage: 25,
            /** Leaflet map instance reference */
            worldMap: null,
            /** Raw data fetched from the JSON database */
            rawData: [],
        };
    },
    computed: {
        /** Returns a list of unique countries available in the dataset */
        allCountries() {
            return this.filterData.locations.map(loc => loc.country).filter(country => country !== 'Unknown');
        },

        /** Returns cities belonging to the selected country filter */
        filteredCities() {
            if (!this.filters.country) return [];
            // Converte o input e o nome do país para minúsculas para comparar
                const searchCountry = this.filters.country.toLowerCase();
                const selectedCountry = this.filterData.locations.find(loc => loc.country.toLowerCase() === searchCountry);
                return selectedCountry ? selectedCountry.cities.map(city => city.name) : [];
        },

        /** Returns municipalities belonging to the selected city filter */
        filteredMunicipalities() {
            if (!this.filters.city) return [];
            // Converte o input e o nome do país para minúsculas para comparar
            const searchCountry = this.filters.country.toLowerCase();
            const selectedCountry = this.filterData.locations.find(loc => loc.country.toLowerCase() === searchCountry);
            if (!selectedCountry) return [];
            const selectedCity = selectedCountry.cities.find(city => city.name === this.filters.city);
            return selectedCity ? selectedCity.municipalities : [];
        },

        /** Calculates the max value for the city comparison chart scale */
        cityComparisonMax() {
            const values = Object.values(this.cityComparison).flatMap(city => Object.values(city));
            if (values.length === 0) return 200;
            const maxData = Math.max(...values);
            return Math.max(200, Math.ceil(maxData / 50) * 50);
        },

        /** Calculates the max value for the municipality price chart scale */
        municipalityMaxPrice() {
            if (this.municipalityPrices.length === 0) return 1;
            const prices = this.municipalityPrices.map(m => m.price);
            const max = Math.max(...prices);
            return Math.ceil(max / 50) * 50;
        },
    
        /** Generates the conic-gradient style for the property type pie chart */
        overviewPieChartStyle() {
            const p = this.propertyTypes;
            if (!p || (p.entireHomes === 0 && p.privateRooms === 0 && p.sharedRooms === 0 && p.hotelRooms === 0)) {
                return { background: '#6c757d' }; 
            }

            let cumulativePercentage = 0;
            const parts = [
                { value: p.entireHomes, color: '#2D9C92' },
                { value: p.privateRooms, color: '#163140' },
                { value: p.sharedRooms, color: '#40017a' }, 
                { value: p.hotelRooms, color: '#F9A825' }
            ];

            const gradientParts = parts.filter(type => type.value > 0).map(type => {
                const start = cumulativePercentage;
                const end = cumulativePercentage + type.value;
                cumulativePercentage = end;
                return `${type.color} ${start}% ${end}%`;
            });

            return { background: `conic-gradient(${gradientParts.join(', ')})` };
        },

        /** Generates the conic-gradient style for the irregularities pie chart */
        pieChartStyle() {
            let cumulativePercentage = 0;
            const gradientParts = this.irregularityTypes.map(type => {
                const start = cumulativePercentage;
                const end = cumulativePercentage + type.value;
                cumulativePercentage = end;
                return `${type.color} ${start}% ${end}%`;
            });
            return { background: `conic-gradient(${gradientParts.join(', ')})` };
        },

        /** Generates a dynamic title for the city comparison chart */
        cityComparisonTitle() {
            const cities = Object.keys(this.cityComparison);
            if (cities.length === 0) {
                return 'Loading city data...';
            }
            if (cities.length === 1) {
                return `Analysis for ${cities[0]}`;
            }
            const lastCity = cities[cities.length - 1];
            const otherCities = cities.slice(0, -1).join(', ');
            return `Comparative analysis of ${otherCities} and ${lastCity}`;
        },
    },

    watch: {
        // Reset dependent location filters when parent location changes
        'filters.country'(newCountry, oldCountry) {
            if (newCountry !== oldCountry) {
                this.filters.city = '';
                this.filters.municipality = '';
            }
        },

        'filters.city'(newCity, oldCity) {
            if (newCity !== oldCity) {
                this.filters.municipality = '';
            }
        },

        // Re-process data whenever any filter changes
        filters: {
            handler() {
                this.applyFiltersAndProcessData();
            },
            deep: true
        }
    },

    methods: {
        /** Resets all filter inputs to their default states */
        clearAllFilters() {
            this.filters = {
                country: '',
                city: '',
                municipality: '',
                propertyType: '',
                dateStart: '',
                dateEnd: '',
                priceMin: '',
                priceMax: '',
                occupancyMin: '',
                occupancyMax: '',
                minReviews: 0,
                minRating: 0
            };
            
            console.log('All filters cleared');
        },

        /** Utility to capitalize the first letter of a string */
        capitalize(str) {
            if (typeof str !== 'string' || !str) return str;
            return str.charAt(0).toUpperCase() + str.slice(1).toLowerCase();
        },

        /** Updates the main content view based on sidebar selection and reset the dashboard position to the top*/
        setActiveItem(itemName) {
            this.activeItem = itemName;
            this.$nextTick(() => {
                const contentArea = document.querySelector('.content-area');
                if (contentArea) {
                    contentArea.scrollTop = 0;
                }
            });
        },

        /**
        * Fetches listing data from the JSON Server API or fallback to static file.
        * Populates initial filter options and processes data.
        */
        async loadData() {
            try {
                let data = [];

                try {
                    const response = await fetch('http://localhost:3001/airbnb_listings');
                    if (response.ok) {
                        data = await response.json();
                        console.log('✅ Data loaded from JSON Server');
                    } else {
                        throw new Error('JSON Server not available');
                    }
                } catch (serverError) {
                    console.log('🔄 JSON Server not available, using static db.json');
                    const response = await fetch('/db.json');
                    const dbData = await response.json();
                    data = dbData.airbnb_listings || [];
                }


                data.forEach(d => {
                    d.city = this.capitalize(d.city);
                    d.country = this.capitalize(d.country);
                    d.municipality = this.capitalize(d.municipality);
                });

                this.rawData = data;


                const types = new Set(this.rawData.map(d => d.room_type));
                this.filterData.propertyTypes = [...types].filter(t => t).sort();

                const locationsMap = {};
                this.rawData.forEach(d => {
                    if (!d.country || !d.city) return;

                    if (!locationsMap[d.country]) {
                        locationsMap[d.country] = { country: d.country, cities: {} };
                    }
                    if (!locationsMap[d.country].cities[d.city]) {
                        locationsMap[d.country].cities[d.city] = { name: d.city, municipalities: new Set() };
                    }
                    locationsMap[d.country].cities[d.city].municipalities.add(d.municipality);
                });

                this.filterData.locations = Object.values(locationsMap).map(country => {
                    country.cities = Object.values(country.cities).map(city => {
                        city.municipalities = [...city.municipalities].sort();
                        return city;
                    }).sort((a, b) => a.name.localeCompare(b.name));
                    return country;
                }).sort((a, b) => a.country.localeCompare(b.country));

                this.applyFiltersAndProcessData();

            } catch (error) {
                console.error('Error loading data:', error);
            }
        },

        /**
         * Core logic for filtering raw data and calculating metrics for all dashboard panels.
         * Updates state for charts, tables, maps, and alerts.
         */
        applyFiltersAndProcessData() {
            this.currentPage = 1;
            
            let fullFilteredData = [...this.rawData];

            if (this.filters.country) {
                const searchCountry = this.filters.country.toLowerCase();
                fullFilteredData = fullFilteredData.filter(d => 
                    d.country && d.country.toLowerCase() === searchCountry
                );
            }

            if (this.filters.city) {
                fullFilteredData = fullFilteredData.filter(d => d.city === this.filters.city);
            }

            if (this.filters.municipality) {
                fullFilteredData = fullFilteredData.filter(d => d.municipality === this.filters.municipality);
            }

            // Apply Attribute Filters
            if (this.filters.propertyType) {
                fullFilteredData = fullFilteredData.filter(d => d.room_type === this.filters.propertyType);
            }

            if (this.filters.dateStart) {
                try {
                    const startDate = new Date(this.filters.dateStart);
                    startDate.setHours(0, 0, 0, 0); 
                    fullFilteredData = fullFilteredData.filter(d => d.last_scraped && (new Date(d.last_scraped) >= startDate));
                } catch (e) { console.error("Invalid start date", e); }
            }

            if (this.filters.dateEnd) {
                 try {
                    const endDate = new Date(this.filters.dateEnd);
                    endDate.setHours(23, 59, 59, 999);
                    fullFilteredData = fullFilteredData.filter(d => d.last_scraped && (new Date(d.last_scraped) <= endDate));
                } catch (e) { console.error("Invalid end date", e); }
            }

            if (this.filters.priceMin !== '') {
                fullFilteredData = fullFilteredData.filter(d => d.price_eur >= parseFloat(this.filters.priceMin));
            }

            if (this.filters.priceMax !== '') {
                fullFilteredData = fullFilteredData.filter(d => d.price_eur <= parseFloat(this.filters.priceMax));
            }

            if (this.filters.occupancyMin !== '') {
                const maxAvailability = 365 - parseFloat(this.filters.occupancyMin);
                fullFilteredData = fullFilteredData.filter(d => d.availability_365 <= maxAvailability);
            }

            if (this.filters.occupancyMax !== '') {
                const minAvailability = 365 - parseFloat(this.filters.occupancyMax);
                fullFilteredData = fullFilteredData.filter(d => d.availability_365 >= minAvailability);
            }

            if (this.filters.minReviews > 0) {
                fullFilteredData = fullFilteredData.filter(d => d.reviews_count >= this.filters.minReviews);
            }

            if (this.filters.minRating > 0) {
                fullFilteredData = fullFilteredData.filter(d => d.review_rating >= this.filters.minRating);
            }
            
            // Calculate city comparison separately to maintain context
            if (fullFilteredData.length === 0) {
                 this.cityComparison = {};
            } else {
                this.calculateCityComparison(fullFilteredData);
            }

            // Handle empty results scenario
            if (fullFilteredData.length === 0) {
                this.proprietesNum = 0;
                this.overviewStats = { averagePrice: 0, averageOccupancy: 0, highOccupancy: 0 };
                this.municipalityPrices = [];
                this.propertyTypes = { entireHomes: 0, privateRooms: 0, sharedRooms: 0, hotelRooms: 0 };
                this.worldMapData = [];
                this.listingsData = [];
                this.heatMapData = [];
                this.alertsData= [];
                this.calculateCityComparison(fullFilteredData)
                this.calculateExecutiveData([]);
                this.calculateTemporalEvolution(fullFilteredData); 
                return; 
            }

            // Calculate Overview Stats
            this.proprietesNum = fullFilteredData.length;
            
            const validPrices = fullFilteredData.filter(d => d.price_eur).map(d => d.price_eur);
            this.overviewStats.averagePrice = validPrices.length > 0 
                ? Math.round(validPrices.reduce((a, b) => a + b, 0) / validPrices.length)
                : 0;
            
            const availableData = fullFilteredData.filter(d => d.availability_365 !== null);
            const avgAvailability = availableData.length > 0
                ? availableData.map(d => d.availability_365).reduce((a, b) => a + b, 0) / availableData.length
                : 0;
            this.overviewStats.averageOccupancy = Math.round(365 - avgAvailability);
            
            this.overviewStats.highOccupancy = fullFilteredData.filter(d => 
                d.availability_365 !== null && (365 - d.availability_365) > 300
            ).length;

            // Process Municipality Data for Heatmap
            const municipalitiesMap = {};
            fullFilteredData.forEach(listing => {
                if (!listing.municipality) return; 

                if (!municipalitiesMap[listing.municipality]) {
                    municipalitiesMap[listing.municipality] = {
                        name: listing.municipality,
                        prices: [],
                        occupancyDays: [],
                        alertCount: 0,
                        listingsCount: 0
                    };
                }
                
                const muni = municipalitiesMap[listing.municipality];
                muni.listingsCount++;
                
                if (listing.price_eur !== null) {
                    muni.prices.push(listing.price_eur);
                }
                if (listing.availability_365 !== null) {
                    muni.occupancyDays.push(365 - listing.availability_365);
                }
                if (listing.irregularity_type !== 'Compliant') {
                    muni.alertCount++;
                }
            });

            const processedMunicipalities = Object.values(municipalitiesMap).map(muni => {
                const avgPrice = muni.prices.length > 0
                    ? Math.round(muni.prices.reduce((a, b) => a + b, 0) / muni.prices.length)
                    : 0;

                const avgDays = muni.occupancyDays.length > 0
                    ? muni.occupancyDays.reduce((a, b) => a + b, 0) / muni.occupancyDays.length
                    : 0;
                const occupationPercent = Math.round((avgDays / 365) * 100);

                return {
                    name: muni.name,
                    listingsCount: muni.listingsCount,
                    avgPrice: avgPrice,
                    occupation: occupationPercent,
                    alertCount: muni.alertCount
                };
            });

            this.heatMapData = processedMunicipalities
                .sort((a, b) => b.listingsCount - a.listingsCount)
                .map(muni => ({
                    ...muni,
                    avgPrice: `€${muni.avgPrice}`
                }));

            this.municipalityPrices = processedMunicipalities
                .sort((a, b) => b.avgPrice - a.avgPrice)
                .slice(0, 6)
                .map(muni => ({
                    name: muni.name,
                    price: muni.avgPrice
                }));

            // Calculate Property Types Distribution
            const propertyCounts = {};
            fullFilteredData.forEach(listing => {
                const roomType = listing.room_type || 'Unknown';
                propertyCounts[roomType] = (propertyCounts[roomType] || 0) + 1;
            });
            const totalProperties = fullFilteredData.length;
            this.propertyTypes = {
                entireHomes: totalProperties > 0 ? Math.round(((propertyCounts['Entire home/apt'] || 0) + (propertyCounts['Entire rental unit'] || 0)) / totalProperties * 100) : 0,
                privateRooms: totalProperties > 0 ? Math.round(((propertyCounts['Private room'] || 0) + (propertyCounts['Private room in rental unit'] || 0)) / totalProperties * 100) : 0,
                sharedRooms: totalProperties > 0 ? Math.round((propertyCounts['Shared room'] || 0) / totalProperties * 100) : 0,
                hotelRooms: totalProperties > 0 ? Math.round((propertyCounts['Hotel room'] || 0) / totalProperties * 100) : 0
            };

            // Prepare Map Data
            const filteredCitiesForMap = [...new Set(fullFilteredData.map(d => d.city))].filter(c => c);
            this.worldMapData = fullFilteredData
                .filter(listing => listing.latitude && listing.longitude)
                .map(listing => ({
                    lat: parseFloat(listing.latitude),
                    lng: parseFloat(listing.longitude),
                    name: listing.name || 'Property',
                    city: listing.city,
                    country: listing.country,
                    price: listing.price_eur,
                    occupancy: listing.availability_365 ? Math.round((365 - listing.availability_365) / 365 * 100) : null,
                    url: listing.listing_url || listing.url
                }));

            // Generate Alerts List
            this.alertsData = fullFilteredData
                .filter(listing => listing.irregularity_type !== 'Compliant')
                .map(listing => {
                    let details = 'Unknown irregularity';
                    let type = 'default';
                    let alertType = listing.irregularity_type;

                    switch (listing.irregularity_type) {
                        case 'Licensing':
                            details = 'Property may not have a valid accommodation license.';
                            type = 'multiple-properties'; 
                            alertType = 'Licensing Issue';
                            break;
                        case 'Taxation':
                            details = 'Suspected tax non-compliance or fiscal irregularity.';
                            type = 'multiple-properties'; 
                            alertType = 'Tax Issue';
                            break;
                        case 'Zoning':
                            details = 'Property may be operating in an unauthorized tourist zone.';
                            type = 'high-occupancy'; 
                            alertType = 'Zoning Issue';
                            break;
                        case 'Over-Occupation':
                            details = 'Property exceeds allowed occupancy limits for its type.';
                            type = 'high-occupancy'; 
                            alertType = 'Over-Occupation';
                            break;
                        default:
                            details = `Unknown irregularity: ${listing.irregularity_type}`;
                            type = 'multiple-properties'; 
                            alertType = listing.irregularity_type;
                            break;
                    }

                    return {
                        id: listing.listing_url,
                        propertyName: listing.name,
                        details: details,
                        type: type,
                        alertType: alertType
                    };
                });

            // Trigger calculations for other charts/views
            this.calculateTemporalEvolution(fullFilteredData);
            this.calculateExecutiveData(fullFilteredData);
            
            // Populate main listings table data
            this.listingsData = fullFilteredData.map(listing => {
                const occupationDays = listing.availability_365 !== null ? (365 - listing.availability_365) : 'N/A';
                return {
                    id: listing.listing_url,
                    name: listing.name,
                    municipality: listing.municipality,
                    type: listing.room_type,
                    price: listing.price_eur !== null ? `€${listing.price_eur.toFixed(2)}` : 'N/A',
                    reviews: listing.reviews_count,
                    rating: listing.review_rating || 0,
                    occupation: occupationDays,
                    host: listing.host_name,
                    url: listing.url
                };
            });
        },

        /**
         * Aggregates data by date to show trends over time (Price, Occupancy, Listings).
         * @param {Array} fullFilteredData - The currently filtered dataset
         */
        calculateTemporalEvolution(fullFilteredData) {
            const dailyStats = {};

            fullFilteredData.forEach(d => {
                const dateStr = d.last_scraped;
                if (!dateStr || d.price_eur === null || d.availability_365 === null) {
                    return; 
                }
                
                if (!dailyStats[dateStr]) {
                    dailyStats[dateStr] = {
                        prices: [],
                        occupancies: [],
                        listingsCount: 0
                    };
                }
                
                dailyStats[dateStr].prices.push(d.price_eur);
                const occupancyDays = 365 - d.availability_365;
                dailyStats[dateStr].occupancies.push((occupancyDays / 365) * 100); 
                dailyStats[dateStr].listingsCount++;
            });

            const sortedDates = Object.keys(dailyStats).sort((a, b) => new Date(a) - new Date(b));

            const newPriceData = [];
            const newOccupancyData = [];
            const newListingData = [];
            const newListingCounts = []; 
            const newDateLabels = []; 

            sortedDates.forEach(dateStr => {
                const stats = dailyStats[dateStr];
                
                const [year, month, day] = dateStr.split('-');
                newDateLabels.push(`${day}-${month}`); 

                const avgPrice = stats.prices.length > 0
                    ? stats.prices.reduce((a, b) => a + b, 0) / stats.prices.length
                    : 0;
                
                const avgOccupancy = stats.occupancies.length > 0
                    ? stats.occupancies.reduce((a, b) => a + b, 0) / stats.occupancies.length
                    : 0;

                newPriceData.push(avgPrice);
                newOccupancyData.push(avgOccupancy);
                newListingCounts.push(stats.listingsCount);
            });

            const maxListings = Math.max(...newListingCounts, 1);
            newListingCounts.forEach(count => {
                newListingData.push((count / maxListings) * 100);
            });

            this.timeSeriesMonths = newDateLabels;
            this.timeSeriesData.price = newPriceData;
            this.timeSeriesData.occupancy = newOccupancyData;
            this.timeSeriesData.listings = newListingData;
            
            const maxPrice = Math.max(...newPriceData, 0);
            const maxOccupancy = Math.max(...newOccupancyData, 0);
            const trueMax = Math.max(100, maxPrice, maxOccupancy);
            this.timeSeriesMaxY = Math.ceil(trueMax / 50) * 50;
        },

        /**
         * Calculates statistics for comparing different cities (Top 6).
         * @param {Array} data - Dataset to analyze (usually filtered by country)
         */
        calculateCityComparison(data) {
            const cities = [...new Set(data.map(d => d.city))].filter(c => c);
            
            const allCityComparison = {}; 
            
            cities.forEach(city => {
                const cityData = data.filter(d => d.city === city);
                
                const cityAvailableData = cityData.filter(d => d.availability_365 !== null);
                const totalAvailableDays = cityAvailableData.length > 0
                    ? cityAvailableData.map(d => d.availability_365).reduce((a, b) => a + b, 0)
                    : 0;
                const avgCityOccupancy = cityAvailableData.length > 0
                    ? Math.round(100 - (totalAvailableDays / (cityAvailableData.length * 365) * 100))
                    : 0;
                
                const cityPriceData = cityData.filter(d => d.price_eur);
                const avgCityPrice = cityPriceData.length > 0
                    ? cityPriceData.map(d => d.price_eur).reduce((a, b) => a + b, 0) / cityPriceData.length
                    : 0;
                
                allCityComparison[city] = {
                    occupancyRate: avgCityOccupancy,
                    averagePrice: Math.round(avgCityPrice),
                    totalListings: cityData.length
                };
            });

            const cityArray = Object.entries(allCityComparison);
            cityArray.sort((a, b) => b[1].totalListings - a[1].totalListings);
            const top6Array = cityArray.slice(0, 6);
            const top6CityComparison = Object.fromEntries(top6Array);
            
            this.cityComparison = top6CityComparison;
        },
        
        /**
         * Handles user logout: clears filters, state, localStorage and redirects to login.
         */
        logout() {
            this.filters = {
                country: '', city: '', municipality: '', propertyType: '',
                dateStart: '', dateEnd: '', priceMin: '', priceMax: '',
                occupancyMin: '', occupancyMax: '', minReviews: 0, minRating: 0
            };
            this.activeItem = 'Overview'
            localStorage.removeItem('loggedUser');
            this.$router.push('/');
        },

        /**
         * Calculates high-level metrics and identifies priority cases for the Executive view.
         * @param {Array} fullFilteredData - The filtered dataset
         */
        calculateExecutiveData(fullFilteredData) {
            if (!fullFilteredData || fullFilteredData.length === 0) {
                this.executiveStats = {
                    criticalCount: 0,
                    compliance: { rate: 0, change: "+0%", label: "0 de 0 propriedades" },
                    nonCompliant: { count: 0, label: "Critical" },
                    fiscalImpact: { amount: "€0M", label: "Losses", sublabel: "Uncollected revenue (annual)" }
                };
                this.irregularityTypes = [];
                this.priorityCases = [];
                return;
            }

            const totalProperties = fullFilteredData.length;
            const nonCompliantData = fullFilteredData.filter(d => d.irregularity_type !== 'Compliant');
            const criticalCount = nonCompliantData.length;
            const complianceRate = totalProperties > 0 ? Math.round(((totalProperties - criticalCount) / totalProperties) * 100) : 0;

            // Estimate fiscal impact based on potential lost revenue
            const fiscalImpact = nonCompliantData.reduce((total, property) => {
                const price = property.price_eur || 100;
                const occupancy = property.availability_365 !== null ? (365 - property.availability_365) / 365 : 0.5;
                return total + (price * occupancy * 100);
            }, 0);

            this.executiveStats = {
                criticalCount: criticalCount,
                compliance: { 
                    rate: complianceRate, 
                    change: "+0%",
                    label: `${totalProperties - criticalCount} de ${totalProperties} propriedades`
                },
                nonCompliant: { 
                    count: criticalCount, 
                    label: "Critical" 
                },
                fiscalImpact: { 
                    amount: `€${(fiscalImpact / 1000).toFixed(1)}K`,
                    label: "Losses", 
                    sublabel: "Uncollected revenue (annual)" 
                }
            };

            const irregularityCounts = {};
            nonCompliantData.forEach(property => {
                const type = property.irregularity_type || 'Unknown';
                irregularityCounts[type] = (irregularityCounts[type] || 0) + 1;
            });

            const colorMap = {
                'Licensing': '#8A6FDD',
                'Zoning': '#E9435A',
                'Over-Occupation': '#3CC3DF',
                'Taxation': '#F9A825',
                'Unknown': '#6c757d'
            };

            this.irregularityTypes = Object.entries(irregularityCounts).map(([name, count]) => ({
                name: name,
                value: criticalCount > 0 ? Math.round((count / criticalCount) * 100) : 0,
                count: count,
                color: colorMap[name] || '#6c757d'
            }));

            // Identify and format priority cases for review
            this.priorityCases = nonCompliantData
                .map(property => {
                    let priorityClass = 'priority-high';
                    let priorityLabel = 'High';
                    
                    if (property.irregularity_type === 'Licensing') {
                        priorityClass = 'priority-critical';
                        priorityLabel = 'Critical';
                    }

                    return {
                        id: property.listing_url?.split('/').pop() || 'Unknown',
                        propertyName: property.name || 'Unknown Property',
                        issue: property.irregularity_type,
                        location: property.municipality || 'Unknown',
                        priorityClass: priorityClass,
                        originalData: property,
                        tags: [
                            { label: priorityLabel, class: `tag-${priorityLabel.toLowerCase()}` },
                            { label: property.irregularity_type, class: 'tag-default' }
                        ]
                    };
                });
        },
    },
    
    async mounted() {
        // Check auth and load data on mount
        const savedUser = JSON.parse(localStorage.getItem('loggedUser') || '{}');
        if (!savedUser.name) {
            this.$router.push('/');
        }
        
        await this.loadData();
    },
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

.dashboard-container {
    display: flex;
    height: 100vh;
    background: url('../assets/images/background.png') center/cover no-repeat fixed, #000000;
    overflow: hidden;
}

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
    font-size: 2.5rem;
    margin-right: 15px;
    color: #2D9C92;
}

.profile-image {
    width: 50px;
    height: 50px;
    border-radius: 50%;
    object-fit: cover;
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

.main-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    height: 100vh;
    margin-left: 280px;
    overflow: hidden;
}

.content-area {
    flex: 1;
    padding: 20px;
    overflow-y: auto;
    background: transparent;
    margin-top: 80px;
    display: flex;
    flex-direction: column;
}

.dashboard-panel {
    flex: 1;
    display: flex;
    flex-direction: column;
    min-height: min-content;
}

.dashboard-header {
    margin-bottom: 30px;
    padding-bottom: 15px;
}

.dashboard-header h2 {
    margin: 0;
    font-size: 1.8rem;
    font-weight: 600;
}

.dashboard-header p {
    margin: 5px 0 0 0;
    font-size: 1rem;
}

.dashboard-footer {
    margin-top: 20px; 
    margin-bottom: -20px;
    margin-left: -20px;
    margin-right: -20px;
    padding: 15px; 
    color: #b4b4b7;
    background-color: #030D46;
    font-size: 0.8rem;
}

.footer-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;
}

.footer-left {
    text-align: left;
}

.footer-right {
    text-align: right;
}

@media (max-width: 768px) {
    .dashboard-container {
        flex-direction: column;
    }
    
    .sidebar {
        width: 100%;
        height: auto;
        position: relative;
    }
    
    .top-header {
        position: relative;
        left: 0;
        width: 100%;
    }
    
    .main-content {
        margin-left: 0;
        height: auto;
    }
    
    .content-area {
        margin-top: 0;
    }
}
</style>
