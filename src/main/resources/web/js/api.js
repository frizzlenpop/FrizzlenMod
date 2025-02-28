/**
 * API Utility Functions for FrizzlenMod Admin Panel
 */

// API configuration
const API_URL = window.location.origin + '/api';

// API endpoints
const API = {
    // Authentication endpoints
    auth: {
        login: () => `${API_URL}/auth/login`,
    },
    
    // Dashboard endpoints
    dashboard: {
        stats: () => `${API_URL}/admin/dashboard/stats`,
        recentModLogs: () => `${API_URL}/admin/dashboard/recent-logs`,
        recentAppeals: () => `${API_URL}/admin/dashboard/recent-appeals`,
    },
    
    // Punishments endpoints
    punishments: {
        all: () => `${API_URL}/admin/punishments`,
        player: (name) => `${API_URL}/admin/punishments/player/${name}`,
        ban: () => `${API_URL}/admin/punishments/ban`,
        tempBan: () => `${API_URL}/admin/punishments/tempban`,
        unban: (player) => `${API_URL}/admin/punishments/unban/${player}`,
        mute: () => `${API_URL}/admin/punishments/mute`,
        tempMute: () => `${API_URL}/admin/punishments/tempmute`,
        unmute: (player) => `${API_URL}/admin/punishments/unmute/${player}`,
        warn: (player) => `${API_URL}/admin/punishments/warn/${player}`,
        history: (player) => `${API_URL}/admin/punishments/history/${player}`,
    },
    
    // Appeals endpoints
    appeals: {
        all: (page, pageSize) => `${API_URL}/admin/appeals?page=${page || 0}&size=${pageSize || 10}`,
        byStatus: (status, page, pageSize) => `${API_URL}/admin/appeals/status/${status}?page=${page || 0}&size=${pageSize || 10}`,
        get: (id) => `${API_URL}/admin/appeals/${id}`,
        approve: (id) => `${API_URL}/admin/appeals/${id}/approve`,
        deny: (id) => `${API_URL}/admin/appeals/${id}/deny`,
        comment: (id) => `${API_URL}/admin/appeals/${id}/comment`,
        submit: () => `${API_URL}/appeals/submit`,
        status: (id) => `${API_URL}/appeals/status/${id}`,
    },
    
    // Mod logs endpoints
    modlogs: {
        all: (page, pageSize) => `${API_URL}/admin/modlogs?page=${page || 0}&size=${pageSize || 10}`,
        player: (name, page, pageSize) => `${API_URL}/admin/modlogs/player/${name}?page=${page || 0}&size=${pageSize || 10}`,
        action: (action, page, pageSize) => `${API_URL}/admin/modlogs/action/${action}?page=${page || 0}&size=${pageSize || 10}`,
        timeRange: (start, end, page, pageSize) => `${API_URL}/admin/modlogs/timerange?start=${start}&end=${end}&page=${page || 0}&size=${pageSize || 10}`,
    },
    
    // User management endpoints
    users: {
        all: () => `${API_URL}/admin/users`,
        create: () => `${API_URL}/admin/users`,
        get: (username) => `${API_URL}/admin/users/${username}`,
        updatePassword: (username) => `${API_URL}/admin/users/${username}/password`,
        updateRole: (username) => `${API_URL}/admin/users/${username}/role`,
        delete: (username) => `${API_URL}/admin/users/${username}`,
    }
};

/**
 * Makes an API request with the auth token
 * 
 * @param {string} url The API URL
 * @param {Object} options Fetch options
 * @returns {Promise} The fetch promise
 */
async function apiRequest(url, options = {}) {
    const token = localStorage.getItem('token');
    
    // Set default options
    const defaultOptions = {
        headers: {
            'Content-Type': 'application/json',
        },
    };
    
    // Add authorization header if token exists
    if (token) {
        defaultOptions.headers['Authorization'] = `Bearer ${token}`;
    }
    
    // Merge options
    const mergedOptions = {
        ...defaultOptions,
        ...options,
        headers: {
            ...defaultOptions.headers,
            ...options.headers,
        },
    };
    
    try {
        const response = await fetch(url, mergedOptions);
        
        // If unauthorized, logout
        if (response.status === 401) {
            logout();
            return null;
        }
        
        // Parse the JSON response
        const data = await response.json();
        
        // Check for API errors
        if (!response.ok) {
            throw new Error(data.error || 'API Error');
        }
        
        return data;
    } catch (error) {
        console.error('API Request Error:', error);
        throw error;
    }
}

/**
 * Makes a GET request to the API
 * 
 * @param {string} url The API URL
 * @returns {Promise} The fetch promise
 */
async function apiGet(url) {
    return apiRequest(url, { method: 'GET' });
}

/**
 * Makes a POST request to the API
 * 
 * @param {string} url The API URL
 * @param {Object} data The data to send
 * @returns {Promise} The fetch promise
 */
async function apiPost(url, data) {
    return apiRequest(url, {
        method: 'POST',
        body: JSON.stringify(data),
    });
}

/**
 * Makes a PUT request to the API
 * 
 * @param {string} url The API URL
 * @param {Object} data The data to send
 * @returns {Promise} The fetch promise
 */
async function apiPut(url, data) {
    return apiRequest(url, {
        method: 'PUT',
        body: JSON.stringify(data),
    });
}

/**
 * Makes a DELETE request to the API
 * 
 * @param {string} url The API URL
 * @returns {Promise} The fetch promise
 */
async function apiDelete(url) {
    return apiRequest(url, { method: 'DELETE' });
}

/**
 * Shows an error message
 * 
 * @param {string} message The error message
 */
function showError(message) {
    alert(message);
}

/**
 * Formats a date
 * 
 * @param {number} timestamp The timestamp in milliseconds
 * @returns {string} The formatted date
 */
function formatDate(timestamp) {
    const date = new Date(timestamp);
    return date.toLocaleString();
}

/**
 * Creates pagination HTML
 * 
 * @param {Object} response The paginated response
 * @param {Function} loadPage The function to load a page
 * @returns {string} The pagination HTML
 */
function createPagination(response, loadPage) {
    const { page, totalPages } = response;
    
    if (totalPages <= 1) {
        return '';
    }
    
    let html = '<ul class="pagination">';
    
    // Previous button
    if (page > 0) {
        html += `<li class="page-item"><a class="page-link" href="#" onclick="event.preventDefault(); ${loadPage.name}(${page - 1})">Previous</a></li>`;
    } else {
        html += '<li class="page-item disabled"><a class="page-link" href="#">Previous</a></li>';
    }
    
    // Page numbers
    const startPage = Math.max(0, page - 2);
    const endPage = Math.min(totalPages - 1, page + 2);
    
    for (let i = startPage; i <= endPage; i++) {
        if (i === page) {
            html += `<li class="page-item active"><a class="page-link" href="#">${i + 1}</a></li>`;
        } else {
            html += `<li class="page-item"><a class="page-link" href="#" onclick="event.preventDefault(); ${loadPage.name}(${i})">${i + 1}</a></li>`;
        }
    }
    
    // Next button
    if (page < totalPages - 1) {
        html += `<li class="page-item"><a class="page-link" href="#" onclick="event.preventDefault(); ${loadPage.name}(${page + 1})">Next</a></li>`;
    } else {
        html += '<li class="page-item disabled"><a class="page-link" href="#">Next</a></li>';
    }
    
    html += '</ul>';
    
    return html;
}

/**
 * Truncates a string
 * 
 * @param {string} str The string to truncate
 * @param {number} maxLength The maximum length
 * @returns {string} The truncated string
 */
function truncateString(str, maxLength = 100) {
    if (!str) return '';
    if (str.length <= maxLength) return str;
    return str.substring(0, maxLength) + '...';
}

/**
 * Shows a spinner in a container
 * 
 * @param {string} containerId The ID of the container
 */
function showSpinner(containerId) {
    const container = document.getElementById(containerId);
    if (container) {
        container.innerHTML = '<div class="spinner-container"><div class="spinner-border text-primary" role="status"><span class="visually-hidden">Loading...</span></div></div>';
    }
}

/**
 * Shows an error message in a container
 * 
 * @param {string} message The error message
 * @param {string} containerId The ID of the container
 */
function showErrorMessage(message, containerId) {
    const container = document.getElementById(containerId);
    if (container) {
        container.innerHTML = `<div class="alert alert-danger">${message}</div>`;
    }
} 