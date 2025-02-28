/**
 * Dashboard Functions for FrizzlenMod Admin Panel
 * This file contains functions specific to the dashboard section
 */

/**
 * Loads the dashboard content
 * This is already implemented in main.js, so this is just a wrapper
 */
function loadDashboardContent() {
    // Call the loadDashboard function from main.js
    loadDashboard();
}

/**
 * Refresh dashboard data on a timer
 * @param {number} interval - Refresh interval in milliseconds
 */
function setupDashboardRefresh(interval = 60000) {
    // Clear any existing interval
    if (window.dashboardRefreshInterval) {
        clearInterval(window.dashboardRefreshInterval);
    }
    
    // Set up new interval
    window.dashboardRefreshInterval = setInterval(() => {
        // Only refresh if dashboard is currently visible
        if (currentSection === 'dashboard') {
            loadDashboard();
        }
    }, interval);
}

/**
 * Initialize dashboard with refresh
 */
function initDashboard() {
    // Set up refresh interval (every minute)
    setupDashboardRefresh(60000);
}

// Add event listener to initialize dashboard when the section is loaded
document.addEventListener('DOMContentLoaded', () => {
    // Get the dashboard nav link
    const dashboardLink = document.querySelector('.nav-link[data-section="dashboard"]');
    
    // Add click event listener
    if (dashboardLink) {
        dashboardLink.addEventListener('click', initDashboard);
    }
}); 