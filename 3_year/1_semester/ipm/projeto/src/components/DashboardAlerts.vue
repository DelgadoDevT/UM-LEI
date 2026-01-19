<template>
<div class="dashboard-panel alerts-dashboard">
    <div class="dashboard-header">
        <h2>Alerts and Anomalies</h2>
        <p>Properties requiring regulatory attention</p>
    </div>
    <div class="alerts-list" v-if="alertsData.length > 0">
        <div class="alert-item" 
            v-for="alert in alertsData" 
            :key="alert.id"
            :class="alert.type"> 
            <div class="alert-content">
                <h4>{{ alert.propertyName }}</h4>
                <p>{{ alert.details }}</p>
            </div>
            <div class="alert-type">
                <span :class="['alert-tag', alert.type]">{{ alert.alertType }}</span>
            </div>
        </div>
    </div>
    <div class="no-data-message" v-else>
        No anomalies found for the selected filters.
    </div>
</div>
</template>

<script>
/**
 * DashboardAlerts Component
 * * This component displays a list of alerts and anomalies related to properties.
 * It renders a list of alert items, each showing property details and the type of issue.
 */
export default {
    name: 'DashboardAlerts',
    props: {
        /**
         * Array of alert objects to be displayed.
         * Each object should contain: id, propertyName, details, type, alertType.
         */
        alertsData: Array
    }
}
</script>

<style scoped>
.alerts-dashboard {
    background: rgba(2, 9, 49, 0.4);
    padding: 30px;
    border-radius: 12px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.alerts-dashboard .dashboard-header {
    background: transparent;
    padding: 0;
    border-radius: 0;
    margin-bottom: 30px;
    box-shadow: none;
}

.alerts-dashboard .dashboard-header h2 {
    margin: 0;
    font-size: 1.8rem;
    color: white;
    font-weight: 600;
}

.alerts-dashboard .dashboard-header p {
    margin: 5px 0 0 0;
    color: rgba(255, 255, 255, 0.8); 
    font-size: 1rem;
}

.alerts-list {
    display: flex;
    flex-direction: column;
    gap: 15px;
}

.alert-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    background: rgba(255, 255, 255, 0.1); 
    padding: 20px;
    border-radius: 8px;
    border: 1px solid rgba(255, 255, 255, 0.2);
    transition: all 0.3s ease;
}

.alert-item:hover {
    background: rgba(255, 255, 255, 0.15);
    transform: translateY(-2px);
}

.alert-content {
    flex: 1;
}

.alert-content h4 {
    margin: 0 0 8px 0;
    color: white; 
    font-size: 1.1rem;
    font-weight: 600;
}

.alert-content p {
    margin: 0;
    color: rgba(255, 255, 255, 0.9);
    font-size: 0.9rem;
}

.alert-type {
    margin-left: 20px;
}

.alert-tag {
    padding: 6px 12px;
    border-radius: 15px;
    font-size: 0.8rem;
    font-weight: 500;
    text-transform: uppercase;
    letter-spacing: 0.5px;
    color: white;
}

.alert-tag.high-occupancy {
    background: #dc3545;
}

.alert-tag.multiple-properties {
    background: #ffc107;
    color: #333;
}

.alert-tag.high-price {
    background: #17a2b8;
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