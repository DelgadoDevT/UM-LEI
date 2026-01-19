<template>
<div class="dashboard-panel executive-dashboard">
    
    <div class="executive-stats">

    <div class="executive-stat-card">
        <div class="stat-value">
            {{ proprietesNum > 0 ? executiveStats.compliance.rate : 0 }}% 
        </div>
        <div class="stat-label"><i class="fas fa-check-circle"></i> Compliance Rate</div>
        <div class="stat-subtitle">{{ executiveStats.compliance.label }}</div>
        <div class="compliance-bar">
            <div class="compliance-bar-fill" :style="{ width: (proprietesNum > 0 ? executiveStats.compliance.rate : 0) + '%' }"></div>
        </div>
    </div>
    
    <div class="executive-stat-card">
        <div class="stat-value">
            {{ proprietesNum > 0 ? executiveStats.nonCompliant.count : 0 }} 
            <span class="critical-text">({{ executiveStats.nonCompliant.label }})</span>
        </div>
        <div class="stat-label"><i class="fas fa-exclamation-circle"></i> Non-Compliant</div>
        <div class="stat-subtitle">Require immediate action</div>
    </div>
    
    <div class="executive-stat-card">
        <div class="stat-value">
            {{ proprietesNum > 0 ? executiveStats.fiscalImpact.amount : '€0' }} 
            <span class="negative">({{ executiveStats.fiscalImpact.label }})</span>
        </div>
        <div class="stat-label"><i class="fas fa-euro-sign"></i> Fiscal Impact</div>
        <div class="stat-subtitle">{{ executiveStats.fiscalImpact.sublabel }}</div>
    </div>
</div>
    
    <div class="executive-content">
        
        <div class="irregularities-chart chart-card-exec">
            <h3>Irregularities by Type</h3>
            <p>Distribution of identified issues</p>
            <div class="chart-placeholder" v-if="irregularityTypes.length > 0">
                <div class="pie-chart-exec" :style="pieChartStyle"></div>
                
                <div class="distribution-legend">
                    <div v-for="type in irregularityTypes" :key="type.name" class="legend-item">
                        <span class="legend-dot" :style="{ backgroundColor: type.color }"></span>
                        <span>{{ type.name }} ({{ type.value }}%)</span>
                    </div>
                </div>
            </div>
            <div class="no-data-message" v-else>
                No irregularities found for the selected filters.
            </div>
        </div>
        
        <div class="priority-cases chart-card-exec">
            <div class="priority-cases-header">
                <div>
                    <h3>Priority Cases</h3>
                    <p>Irregularities requiring immediate action</p>
                </div>
                <div class="export-dropdown">
                    <button class="btn-case export" @click="toggleExecutiveExport = !toggleExecutiveExport">
                        Export
                        <i class="fas fa-download"></i>
                    </button>
                    <div v-if="toggleExecutiveExport" class="export-options">
                        <button @click="exportExecutiveData('json')" class="export-option">
                            <i class="fas fa-file-code"></i> Export as JSON
                        </button>
                        <button @click="exportExecutiveData('csv')" class="export-option">
                            <i class="fas fa-file-csv"></i> Export as CSV
                        </button>
                    </div>
                </div>
            </div>
            <div class="cases-list" v-if="priorityCases.length > 0">
                <div class="case-item" v-for="caseItem in priorityCases" :key="caseItem.id">
                    <div class="case-details">
                        <div class="case-header">
                            <i class="fas fa-exclamation-triangle icon-alert" :class="caseItem.priorityClass"></i>
                            <h4>{{ caseItem.propertyName }}</h4>
                        </div>
                        <p>{{ getIssueDescription(caseItem.originalData?.irregularity_type) }}</p>
                        <div class="case-meta">
                            <span><i class="fas fa-barcode"></i> {{ caseItem.id }}</span>
                            <span><i class="fas fa-map-marker-alt"></i> {{ caseItem.location }}</span>
                        </div>
                    </div>
                    <div class="case-tags-actions">
                        <div class="case-tags">
                            <span v-for="tag in caseItem.tags" :key="tag.label" class="tag" :class="tag.class">
                                {{ tag.label }}
                            </span>
                        </div>
                        <div class="case-buttons">
                            <button class="btn-case details" @click="seeDetails(caseItem)">See details</button>
                            <button class="btn-case investigate" @click="investigate(caseItem)">Investigate</button>
                        </div>
                    </div>
                </div>
            </div>
            <div class="no-data-message" v-else>
                No priority cases found for the selected filters.
            </div>
        </div>
    </div>
</div>
</template>

<script>
/**
 * DashboardExecutive Component
 * * Provides an executive summary view with high-level statistics, 
 * charts for irregularity distribution, and a list of priority cases requiring attention.
 */
export default {
    name: 'DashboardExecutive',
    props: {
        proprietesNum: Number,
        executiveStats: Object,
        irregularityTypes: Array,
        priorityCases: Array,
        pieChartStyle: Object
    },
    data() {
        return {
            // Controls visibility of the export dropdown menu in the priority cases section
            toggleExecutiveExport: false
        }
    },
    methods: {
        /**
         * Helper to get a human-readable description for a given irregularity type code.
         * @param {string} irregularityType - The code key for the irregularity
         * @returns {string} - Descriptive text
         */
        getIssueDescription(irregularityType) {
            const descriptions = {
                'Licensing': 'No local accommodation license',
                'Zoning': 'Area not authorized for tourist accommodation',
                'Over-Occupation': 'Property exceeds allowed occupancy limits for its type',
                'Taxation': 'Suspected tax non-compliance or fiscal irregularity'
            };
            return descriptions[irregularityType] || 'Unknown regulatory issue';
        },

        /**
         * Displays a detail view (alert) for a specific case.
         * @param {Object} caseItem - The priority case object
         */
        seeDetails(caseItem) {
            const property = caseItem.originalData;
            const details = `
                Property: ${property.name || 'N/A'}
                Location: ${property.municipality || 'N/A'}, ${property.city || 'N/A'}
                Type: ${property.room_type || 'N/A'}
                Price: €${property.price_eur || 'N/A'}
                Occupancy: ${property.availability_365 !== null ? (365 - property.availability_365) + ' days/year' : 'N/A'}
                Reviews: ${property.reviews_count || 0}
                Rating: ${property.review_rating || 'N/A'}
                Irregularity: ${property.irregularity_type || 'N/A'}
            `;
            
            alert(`Property Details:\n${details}`);
        },

        /**
         * Initiates an investigation action for a specific case.
         * @param {Object} caseItem - The priority case object
         */
        investigate(caseItem) {
            const property = caseItem.originalData;
            const caseId = Math.floor(100000 + Math.random() * 900000);
            console.log('Investigating property:', property);
            
            alert(`Investigation started for: ${property.name || 'Unknown Property'}\n\nCase ID: ${caseId}\n`);
        },

        /**
         * Exports executive dashboard data to CSV or JSON format.
         * @param {string} format - 'csv' or 'json'
         */
        exportExecutiveData(format) {
            this.toggleExecutiveExport = false;
            
            const executiveData = {
                stats: this.executiveStats,
                irregularities: this.irregularityTypes,
                priorityCases: this.priorityCases.map(caseItem => ({
                    propertyName: caseItem.propertyName,
                    issue: this.getIssueDescription(caseItem.originalData?.irregularity_type),
                    location: caseItem.location,
                    priority: caseItem.tags[0].label,
                    irregularityType: caseItem.originalData?.irregularity_type || 'Unknown'
                })),
                exportDate: new Date().toISOString(),
                totalProperties: this.proprietesNum
            };
            
            if (format === 'json') {
                const dataStr = JSON.stringify(executiveData, null, 2);
                const dataBlob = new Blob([dataStr], { type: 'application/json' });
                
                const link = document.createElement('a');
                link.href = URL.createObjectURL(dataBlob);
                link.download = `executive_dashboard_data_${new Date().toISOString().split('T')[0]}.json`;
                document.body.appendChild(link);
                link.click();
                document.body.removeChild(link);
            } else if (format === 'csv') {
                let csv = 'Property Name,Issue,Location,Priority,Irregularity Type\n';
                this.priorityCases.forEach(caseItem => {
                    csv += `"${caseItem.propertyName}","${this.getIssueDescription(caseItem.originalData?.irregularity_type)}","${caseItem.location}","${caseItem.tags[0].label}","${caseItem.originalData?.irregularity_type || 'Unknown'}"\n`;
                });
                
                const dataBlob = new Blob([csv], { type: 'text/csv' });
                const link = document.createElement('a');
                link.href = URL.createObjectURL(dataBlob);
                link.download = `executive_priority_cases_${new Date().toISOString().split('T')[0]}.csv`;
                document.body.appendChild(link);
                link.click();
                document.body.removeChild(link);
            }
        }
    }
}
</script>

<style scoped>
.executive-dashboard {
    display: flex;
    flex-direction: column;
    gap: 30px;
    background: transparent;
}

.executive-stats {
    display: grid;
    grid-template-columns: 1fr;
    gap: 20px;
}

.executive-stat-card.critical-banner {
    background: rgba(220, 53, 69, 0.1);
    border: 1px solid rgba(220, 53, 69, 0.5);
    color: white;
    padding: 15px 20px;
    border-radius: 12px;
    font-size: 1rem;
    font-weight: 500;
    display: flex;
    align-items: center;
    gap: 10px;
}

.executive-stat-card.critical-banner::before {
   content: none;
}

.executive-stat-card.critical-banner i {
    font-size: 1.2rem;
    color: #dc3545;
}

.executive-stat-card {
    background: rgba(2, 9, 49, 0.4);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 12px;
    padding: 25px;
}

.executive-stat-card .stat-value {
    font-size: 2rem;
    font-weight: 700;
    color: white;
    margin-bottom: 8px;
}

.executive-stat-card .stat-value .positive {
    font-size: 1rem;
    font-weight: 600;
    color: #3CC3DF;
}

.executive-stat-card .stat-value .critical-text {
    font-size: 1rem;
    font-weight: 600;
    color: #dc3545;
}

.executive-stat-card .stat-value .negative {
    font-size: 1rem;
    font-weight: 600;
    color: #E9435A;
}

.executive-stat-card .stat-label {
    font-size: 1rem;
    font-weight: 500;
    color: #b4b4b7;
    margin-bottom: 8px;
    display: flex;
    align-items: center;
    gap: 8px;
}

.executive-stat-card .stat-subtitle {
    font-size: 0.85rem;
    color: rgba(255, 255, 255, 0.7);
}

.compliance-bar {
    width: 100%;
    height: 8px;
    background: rgba(255, 255, 255, 0.2);
    border-radius: 4px;
    margin-top: 15px;
    overflow: hidden;
}

.compliance-bar-fill {
    width: 72%;
    height: 100%;
    background: #3CC3DF;
    border-radius: 4px;
}

.executive-content {
    display: grid;
    grid-template-columns: 1fr;
    gap: 30px;
}

.chart-card-exec {
    background: rgba(2, 9, 49, 0.4);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 12px;
    padding: 25px;
}

.chart-card-exec h3 {
    font-size: 1.5rem;
    font-weight: 600;
    color: white;
    margin: 0 0 5px 0;
}

.chart-card-exec p {
    font-size: 1rem;
    color: #b4b4b7;
    margin: 0 0 25px 0;
}

.irregularities-chart .chart-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 30px;
}

.pie-chart-exec {
    width: 200px;
    height: 200px;
    border-radius: 50%;
    position: relative;
    flex-shrink: 0;
}

.pie-chart-exec::before {
    content: '';
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 60%;
    height: 60%;
    background: rgba(2, 9, 49, 0.6);
    border-radius: 50%;
}

.distribution-legend {
    display: flex;
    flex-direction: column;
    flex-wrap: nowrap;
    justify-content: center;
    gap: 15px;
    width: auto;
}

.distribution-legend .legend-item {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 0.95rem;
    color: white;
}

.legend-item .legend-dot {
    width: 12px;
    height: 12px;
    border-radius: 3px;
    flex-shrink: 0;
}

.legend-dot.licensing { background: #8A6FDD; }
.legend-dot.over-occupation { background: #3CC3DF; }
.legend-dot.taxation { background: #F9A825; }
.legend-dot.zoning { background: #E9435A; }

.priority-cases-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 25px;
}

.priority-cases-header p {
    margin-bottom: 0;
}

.btn-case {
    padding: 8px 15px;
    border-radius: 6px;
    font-size: 0.9rem;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.3s ease;
    border: none;
    display: inline-flex;
    align-items: center;
    gap: 6px;
}

.btn-case.export {
    background: rgba(255, 255, 255, 0.1);
    color: white;
    border: 1px solid rgba(255, 255, 255, 0.2);
}
.btn-case.export:hover {
    background: rgba(255, 255, 255, 0.2);
}

.cases-list {
    display: flex;
    flex-direction: column;
    gap: 15px;
}

.case-item {
    background: rgba(255, 255, 255, 0.05);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 8px;
    padding: 20px;
    display: flex;
    flex-direction: column;
    gap: 15px;
}

.case-item:hover {
    background: rgba(255, 255, 255, 0.15);
    transform: translateY(-2px);
}

.case-header {
    display: flex;
    align-items: center;
    gap: 10px;
}

.case-header .icon-alert {
    font-size: 1.2rem;
}

.case-header .icon-alert.priority-critical {
    color: #dc3545;
}
.case-header .icon-alert.priority-high {
    color: #ffc107;
}

.case-header h4 {
    margin: 0;
    font-size: 1.2rem;
    font-weight: 600;
    color: white;
}

.case-details p {
    font-size: 0.95rem;
    color: #b4b4b7;
    margin: 0;
}

.case-meta {
    display: flex;
    gap: 20px;
    font-size: 0.85rem;
    color: rgba(255, 255, 255, 0.6);
    margin-top: 10px;
}

.case-meta span {
    display: flex;
    align-items: center;
    gap: 6px;
}

.case-tags-actions {
    display: flex;
    flex-direction: column;
    gap: 15px;
}

.case-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
}

.tag {
    padding: 4px 10px;
    border-radius: 15px;
    font-size: 0.8rem;
    font-weight: 500;
}

.tag.tag-critical {
    background: #dc3545;
    color: white;
}

.tag.tag-high {
    background: #ffc107;
    color: #111;
}

.tag.tag-default {
    background: rgba(255, 255, 255, 0.2);
    color: white;
}

.case-buttons {
    display: flex;
    gap: 10px;
    justify-content: flex-start;
}

.btn-case.details {
    background: rgba(255, 255, 255, 0.15);
    color: white;
}

.btn-case.investigate {
    background: #3CC3DF;
    color: #020931;
}
.btn-case.investigate:hover {
    background: #60d5ed;
}

.btn-case.details:hover {
    background: rgba(255, 255, 255, 0.25);
}

.export-dropdown {
    position: relative;
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

@media (min-width: 768px) {
    .irregularities-chart .chart-placeholder {
        flex-direction: row;
        justify-content: center;
        align-items: center;
        gap: 50px;
    }

    .case-item {
        flex-direction: row;
        justify-content: space-between;
        align-items: flex-start;
    }

    .case-tags-actions {
        align-items: flex-end;
        flex-shrink: 0;
    }

    .case-buttons {
        justify-content: flex-end;
    }
}

@media (min-width: 1024px) {
    .executive-stats {
        grid-template-columns: repeat(3, 1fr);
    }

    .executive-stat-card.critical-banner {
        grid-column: 1 / -1;
    }
}
</style>