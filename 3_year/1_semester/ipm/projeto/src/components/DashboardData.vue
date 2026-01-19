<template>
<div class="dashboard-panel data-dashboard">
    <div class="dashboard-header">
        <h2>Detailed Data</h2>
        <p>{{ listingsData.length }} listings found</p>
    </div>
    
    <template v-if="listingsData.length > 0">
        <div class="data-table-container">
            <table class="data-table">
                <thead>
                    <tr>
                        <th>Name</th>
                        <th>Municipality</th>
                        <th>Type</th>
                        <th>Price</th>
                        <th>Reviews</th>
                        <th>Rating</th>
                        <th>Occupation/year</th>
                        <th>Host</th>
                        <th>URL</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="listing in paginatedListings" :key="listing.id">
                        <td>{{ listing.name }}</td>
                        <td>{{ listing.municipality || 'N/A' }}</td>
                        <td>{{ listing.type }}</td>
                        <td>{{ listing.price === '€0.00' ? 'N/A' : listing.price }}</td>
                        <td>{{ listing.reviews }}</td>
                        <td> 
                            <div class="star-rating">
                                <i v-for="n in 5" :key="n" 
                                :class="getStarClass(listing.rating, n)"
                                class="star-icon"></i>
                                <span class="rating-value" v-if="listing.rating > 0">
                                    {{ listing.rating.toFixed(1) }}
                                </span>
                                <span class="no-rating" v-else>No rating</span>
                            </div>
                        </td>
                        <td>{{ listing.occupation }} days</td>
                        <td>{{ listing.host }}</td>
                        <td>
                            <a v-if="listing.url" :href="listing.url" target="_blank" class="url-link" title="Open listing URL">
                                <i class="fas fa-external-link-alt"></i>
                            </a>
                            <span v-else class="no-url">—</span>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
    </template>
    <div class="no-data-message" v-else>
        No data available for the selected filters.
    </div>
    
    <div class="pagination">
        <div class="pagination-left">
            <button @click="previousPage" :disabled="currentPage === 1" class="pagination-btn">‹</button>
            <span class="page-info">Page {{ currentPage }} of {{ totalPages }}</span>
            <button @click="nextPage" :disabled="currentPage === totalPages" class="pagination-btn">›</button>
        </div>
        
        <div class="pagination-center">
            <span class="results-info">
                Showing {{ ((currentPage - 1) * itemsPerPage) + 1 }}-{{ Math.min(currentPage * itemsPerPage, listingsData.length) }} of {{ listingsData.length }}
            </span>
            <div class="items-per-page-selector">
                <label for="items-per-page">Entries per page:</label>
                <select id="items-per-page" :value="itemsPerPage" @change="updateItemsPerPage($event)">
                    <option value="10">10</option>
                    <option value="25">25</option>
                    <option value="50">50</option>
                    <option value="100">100</option>
                    <option value="250">250</option>
                    <option value="500">500</option>
                </select>
            </div>
        </div>
        
        <div class="pagination-right">
            <div class="export-dropdown">
                <button class="export-btn" @click="toggleExportDropdown">
                    <i class="fas fa-download"></i>
                    Export
                </button>
                <div v-if="showExportDropdown" class="export-options">
                    <button @click="exportData('csv')" class="export-option">
                        <i class="fas fa-file-csv"></i>
                        Export as CSV
                    </button>
                    <button @click="exportData('json')" class="export-option">
                        <i class="fas fa-file-code"></i>
                        Export as JSON
                    </button>
                </div>
            </div>
        </div>
    </div>
</div>
</template>

<script>
/**
 * DashboardData Component
 * * Renders a detailed table of property listings with pagination and export functionality (CSV/JSON).
 */
export default {
    name: 'DashboardData',
    props: {
        listingsData: Array,
        currentPage: Number,
        itemsPerPage: Number,
    },
    data() {
        return {

            showExportDropdown: false
        }
    },
    computed: {
        /**
         * Returns the slice of listings corresponding to the current page.
         */
        paginatedListings() {
            const start = (this.currentPage - 1) * this.itemsPerPage;
            const end = start + this.itemsPerPage;
            return this.listingsData.slice(start, end);
        },
        
        /**
         * Calculates the total number of pages based on data length and items per page.
         */
        totalPages() {
            return Math.ceil(this.listingsData.length / this.itemsPerPage);
        }
    },
    methods: {
        getStarClass(rating, starNumber) {
            if (!rating || rating === 0) {
                return 'far fa-star';
            }
            if (rating >= starNumber) {
                return 'fas fa-star';
            } else if (rating >= (starNumber - 0.5)) {
                return 'fas fa-star-half-alt';
            } else {
                return 'far fa-star';
            }
        },

        /**
         * Navigates to the previous page if available.
         */
        previousPage() {
            if (this.currentPage > 1) {
                this.$emit('update:currentPage', this.currentPage - 1);
            }
        },

        /**
         * Navigates to the next page if available.
         */
        nextPage() {
            if (this.currentPage < this.totalPages) {
                this.$emit('update:currentPage', this.currentPage + 1);
            }
        },

        /**
         * Updates the number of items shown per page and resets to page 1.
         * @param {Event} event - The change event from the select element
         */
        updateItemsPerPage(event) {
            const newItemsPerPage = parseInt(event.target.value);
            this.$emit('update:currentPage', 1);
            this.$emit('update:itemsPerPage', newItemsPerPage);
        },

        /**
         * Toggles the visibility of the export menu.
         */
        toggleExportDropdown() {
            this.showExportDropdown = !this.showExportDropdown;
        },

        /**
         * Handles the export action selection.
         * @param {string} format - 'csv' or 'json'
         */
        exportData(format) {
            this.showExportDropdown = false;
            
            if (format === 'csv') {
                this.exportToCSV();
            } else if (format === 'json') {
                this.exportToJSON();
            }
        },

        /**
         * Generates and downloads a CSV file containing the current listings data.
         * Handles proper escaping of CSV fields.
         */
        exportToCSV() {
            if (!this.listingsData || this.listingsData.length === 0) {
                alert('No data available to export.');
                return;
            }
            
            console.log('Exporting filtered data as CSV...');
            
            let csv = 'Name,Municipality,Type,Price,Reviews,Occupation,Host,URL\n';
            
            this.listingsData.forEach(listing => {
                // Helper to escape special characters for CSV format
                const escapeCSV = (field) => {
                    if (field === null || field === undefined) return '""';
                    const stringField = String(field);
                    if (stringField.includes(',') || stringField.includes('"') || stringField.includes('\n')) {
                        return '"' + stringField.replace(/"/g, '""') + '"';
                    }
                    return stringField;
                };
                
                csv += `${escapeCSV(listing.name)},${escapeCSV(listing.municipality)},${escapeCSV(listing.type)},${escapeCSV(listing.price)},${escapeCSV(listing.reviews)},${escapeCSV(listing.occupation)},${escapeCSV(listing.host)},${escapeCSV(listing.url)}\n`;
            });
            
            // Create blob and trigger download
            const dataBlob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
            const link = document.createElement('a');
            link.href = URL.createObjectURL(dataBlob);
            link.download = `airbnb_data_${new Date().toISOString().split('T')[0]}.csv`;
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            URL.revokeObjectURL(link.href);
        },

        /**
         * Generates and downloads a JSON file containing the current listings data.
         */
        exportToJSON() {
            if (!this.listingsData || this.listingsData.length === 0) {
                alert('No data available to export.');
                return;
            }
            
            console.log('Exporting filtered data as JSON...');
            
            const exportData = {
                metadata: {
                    exportDate: new Date().toISOString(),
                    totalListings: this.listingsData.length,
                    filters: 'Applied filters'
                },
                listings: this.listingsData.map(listing => ({
                    name: listing.name || '',
                    municipality: listing.municipality || '',
                    type: listing.type || '',
                    price: listing.price || 0,
                    reviews: listing.reviews || 0,
                    occupation: listing.occupation || 0,
                    host: listing.host || '',
                    url: listing.url || ''
                }))
            };
            
            // Create blob and trigger download
            const dataStr = JSON.stringify(exportData, null, 2);
            const dataBlob = new Blob([dataStr], { type: 'application/json;charset=utf-8;' });
            const link = document.createElement('a');
            link.href = URL.createObjectURL(dataBlob);
            link.download = `airbnb_data_${new Date().toISOString().split('T')[0]}.json`;
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            URL.revokeObjectURL(link.href);
        }
    }
}
</script>

<style scoped>
.data-dashboard {
    background: rgba(2, 9, 49, 0.4);
    padding: 30px;
    border-radius: 12px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.data-dashboard .dashboard-header {
    background: transparent;
    padding: 0;
    border-radius: 0;
    margin-bottom: 30px;
    box-shadow: none;
}

.data-dashboard .dashboard-header h2 {
    margin: 0;
    font-size: 1.8rem;
    color: white;
    font-weight: 600;
}

.data-dashboard .dashboard-header p {
    margin: 5px 0 0 0;
    color: rgba(255, 255, 255, 0.8);
    font-size: 1rem;
}

.data-table-container {
    background: rgba(255, 255, 255, 0.1);
    border-radius: 8px;
    border: 1px solid rgba(255, 255, 255, 0.2);
    overflow: hidden;
    margin-bottom: 20px;
}

.data-table {
    width: 100%;
    border-collapse: collapse;
}

.data-table th {
    background: rgba(255, 255, 255, 0.15);
    color: white;
    padding: 15px 12px;
    text-align: left;
    font-weight: 600;
    font-size: 0.9rem;
    border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.data-table td {
    padding: 12px;
    color: rgba(255, 255, 255, 0.9);
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    font-size: 0.9rem;
}

.data-table tr:hover {
    background: rgba(255, 255, 255, 0.05);
}

.pagination {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 25px;
    padding: 10px 18px; 
    background: rgba(255, 255, 255, 0.1);
    border-radius: 8px;
    border: 1px solid rgba(255, 255, 255, 0.2);
    gap: 12px; 
    min-height: 46px; 
}

.pagination-left {
    display: flex;
    align-items: center;
    gap: 6px; 
}

.pagination-btn {
    background: rgba(255, 255, 255, 0.2);
    color: white;
    border: 1px solid rgba(255, 255, 255, 0.3);
    border-radius: 5px; 
    cursor: pointer;
    font-size: 0.82rem; 
    min-width: 34px; 
    height: 28px;
    transition: all 0.3s ease;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0; 
    box-sizing: border-box; 
}

.pagination-btn:hover:not(:disabled) {
    background: rgba(255, 255, 255, 0.3);
    border-color: rgba(255, 255, 255, 0.4);
}

.pagination-btn:disabled {
    opacity: 0.5;
    cursor: not-allowed;
}

.page-info {
    color: white;
    font-weight: 500;
    font-size: 0.82rem; 
    margin: 0 6px;
}

.results-info {
    color: rgba(255, 255, 255, 0.8);
    font-size: 0.82rem; 
}

.export-dropdown {
    position: relative;
}

.export-btn {
    background: rgba(255, 255, 255, 0.2);
    color: white;
    border: 1px solid rgba(255, 255, 255, 0.3);
    padding: 5px 12px;
    border-radius: 5px; 
    cursor: pointer;
    font-size: 0.82rem;
    display: flex;
    align-items: center;
    gap: 5px; 
    height: 28px; 
    transition: all 0.3s ease;
}

.export-btn:hover {
    background: rgba(255, 255, 255, 0.3);
    border-color: rgba(255, 255, 255, 0.4);
}

.export-options {
    position: absolute;
    top: 100%;
    right: 0;
    margin-top: 4px; 
    background: rgba(2, 9, 49, 0.95);
    border: 1px solid rgba(255, 255, 255, 0.2);
    border-radius: 5px; 
    padding: 5px;
    min-width: 145px;
    z-index: 1000;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.export-option {
    background: transparent;
    color: white;
    border: none;
    padding: 7px 9px; 
    border-radius: 3px;
    cursor: pointer;
    font-size: 0.82rem;
    width: 100%;
    text-align: left;
    display: flex;
    align-items: center;
    gap: 5px;
    transition: background 0.3s ease;
}

.export-option:hover {
    background: rgba(255, 255, 255, 0.1);
}

.url-link {
    color: #4dabf7;
    text-decoration: none;
    font-size: 14px;
    padding: 5px;
    border-radius: 3px;
    transition: all 0.2s ease;
    display: inline-flex;
    align-items: center;
    justify-content: center;
}

.url-link:hover {
    color: #228be6;
    background-color: rgba(77, 171, 247, 0.1);
    transform: scale(1.1);
}

.no-url {
    color: rgba(255, 255, 255, 0.5);
    font-style: italic;
}

.items-per-page-selector {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-left: 20px;
}

.items-per-page-selector label {
    font-size: 12px;
    color: rgba(255, 255, 255, 0.8);
    white-space: nowrap;
}

.items-per-page-selector select {
    padding: 6px 10px;
    border: 1px solid rgba(255, 255, 255, 0.3);
    border-radius: 4px;
    background-color: rgba(2, 9, 49, 0.6);
    color: white;
    font-size: 12px;
    cursor: pointer;
    transition: border-color 0.3s ease;
}

.items-per-page-selector select:hover {
    border-color: rgba(255, 255, 255, 0.5);
}

.items-per-page-selector select:focus {
    outline: none;
    border-color: rgba(255, 255, 255, 0.7);
}

.pagination-center {
    display: flex;
    align-items: center;
    gap: 15px;
}

.results-info {
    font-size: 12px;
    color: rgba(255, 255, 255, 0.8);
    white-space: nowrap;
}

.star-rating {
    display: flex;
    align-items: center;
    gap: 2px;
}

.star-icon {
    font-size: 12px;
    color: #FFD700;
}

.rating-value {
    margin-left: 6px;
    font-size: 11px;
    color: rgba(255, 255, 255, 0.8);
    font-weight: 500;
}

.no-rating {
    font-size: 11px;
    color: rgba(255, 255, 255, 0.5);
    font-style: italic;
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