/**
 * Main JavaScript file for FrizzlenMod Admin Panel
 */

// Global variables
let currentSection = 'dashboard';
const PAGE_SIZE = 10;

/**
 * Initializes the admin panel after login
 */
function initAdminPanel() {
    try {
        console.log('Initializing admin panel');
        
        // First, ensure the admin panel is visible
        const adminPanel = document.getElementById('admin-panel');
        if (adminPanel) {
            adminPanel.classList.remove('d-none');
            adminPanel.style.display = 'block';
            
            // Also make sure login container is hidden
            const loginContainer = document.getElementById('login-container');
            if (loginContainer) {
                loginContainer.style.display = 'none';
            }
        } else {
            console.error('Admin panel element not found');
            return;
        }
        
        // Setup navigation
        setupNavigation();
        
        // Load dashboard content
        loadDashboard();
        
        // Set up event handlers
        setupEventHandlers();
        
        console.log('Admin panel initialization complete');
    } catch (error) {
        console.error('Error initializing admin panel:', error);
        
        // Still ensure the admin panel is visible even if there's an error
        const adminPanel = document.getElementById('admin-panel');
        if (adminPanel) {
            adminPanel.classList.remove('d-none');
            adminPanel.setAttribute('style', 'display: block !important');
            
            // Also make sure login container is hidden
            const loginContainer = document.getElementById('login-container');
            if (loginContainer) {
                loginContainer.classList.add('d-none');
                loginContainer.setAttribute('style', 'display: none !important');
            }
            
            // Create an error message
            const errorDiv = document.createElement('div');
            errorDiv.className = 'alert alert-danger m-3';
            errorDiv.innerHTML = `
                <h4 class="alert-heading">Error Initializing Admin Panel</h4>
                <p>There was an error initializing the admin panel:</p>
                <p><code>${error.message || 'Unknown error'}</code></p>
                <hr>
                <p class="mb-0">Some features may not work correctly. Please check the console for more details.</p>
            `;
            
            // Add error message at the top of the admin panel
            adminPanel.insertBefore(errorDiv, adminPanel.firstChild);
        }
    }
}

/**
 * Sets up the navigation
 */
function setupNavigation() {
    // Get all nav links
    const navLinks = document.querySelectorAll('.nav-link[data-section]');
    
    if (!navLinks || navLinks.length === 0) {
        console.warn('No navigation links found');
        return;
    }
    
    // Add click event to each nav link
    navLinks.forEach(link => {
        link.addEventListener('click', function(event) {
            event.preventDefault();
            
            // Get the section ID
            const sectionId = this.getAttribute('data-section');
            
            if (!sectionId) {
                console.warn('Navigation link has no data-section attribute');
                return;
            }
            
            // Update current section
            currentSection = sectionId;
            
            // Toggle active state
            navLinks.forEach(l => l.classList.remove('active'));
            this.classList.add('active');
            
            // Hide all content sections
            const contentSections = document.querySelectorAll('.content-section');
            contentSections.forEach(section => {
                section.style.display = 'none';
            });
            
            // Show the selected section
            const selectedSection = document.getElementById(`${sectionId}-section`);
            if (selectedSection) {
                selectedSection.style.display = 'block';
                
                // Load section content
                loadSectionContent(sectionId);
            } else {
                console.warn(`Section with ID "${sectionId}-section" not found`);
            }
        });
    });
    
    // Set dashboard as active by default
    const dashboardLink = document.querySelector('.nav-link[data-section="dashboard"]');
    if (dashboardLink) {
        dashboardLink.classList.add('active');
        
        // Show dashboard section
        const dashboardSection = document.getElementById('dashboard-section');
        if (dashboardSection) {
            dashboardSection.style.display = 'block';
        } else {
            console.warn('Dashboard section not found');
        }
        
        // Hide other sections
        document.querySelectorAll('.content-section:not(#dashboard-section)').forEach(section => {
            section.style.display = 'none';
        });
    } else {
        console.warn('Dashboard navigation link not found');
    }
}

/**
 * Loads the content for a specific section
 * 
 * @param {string} sectionId The ID of the section to load
 */
function loadSectionContent(sectionId) {
    console.log('Loading content for section:', sectionId);
    
    try {
        switch (sectionId) {
            case 'dashboard':
                loadDashboard();
                break;
            case 'punishments':
                if (typeof loadPunishments === 'function') {
                    loadPunishments();
                } else {
                    console.error('loadPunishments function not found');
                    showSectionError('punishments', 'The punishments module could not be loaded');
                }
                break;
            case 'appeals':
                if (typeof loadAppeals === 'function') {
                    loadAppeals();
                } else {
                    console.error('loadAppeals function not found');
                    showSectionError('appeals', 'The appeals module could not be loaded');
                }
                break;
            case 'modlogs':
                if (typeof loadModLogs === 'function') {
                    loadModLogs();
                } else {
                    console.error('loadModLogs function not found');
                    showSectionError('modlogs', 'The moderation logs module could not be loaded');
                }
                break;
            case 'users':
                if (typeof loadUsers === 'function') {
                    loadUsers();
                } else {
                    console.error('loadUsers function not found');
                    showSectionError('users', 'The user management module could not be loaded');
                }
                break;
            default:
                console.warn('Unknown section:', sectionId);
                break;
        }
    } catch (error) {
        console.error(`Error in loadSectionContent for ${sectionId}:`, error);
        showSectionError(sectionId, error.message || 'An unexpected error occurred');
    }
}

/**
 * Shows an error message in the section's error container
 * 
 * @param {string} sectionId The section ID
 * @param {string} message The error message
 */
function showSectionError(sectionId, message) {
    const errorContainer = document.getElementById(`${sectionId}-error`);
    if (errorContainer) {
        errorContainer.innerHTML = `
            <div class="alert alert-danger">
                <strong>Error:</strong> ${message}
            </div>
        `;
    } else {
        console.error(`Error container not found for section: ${sectionId}`);
    }
}

/**
 * Sets up event handlers for the admin panel
 */
function setupEventHandlers() {
    // Player search form
    const playerSearchForm = document.getElementById('player-search-form');
    if (playerSearchForm) {
        playerSearchForm.addEventListener('submit', (e) => {
            e.preventDefault();
            const playerName = document.getElementById('player-search-input').value;
            if (playerName) {
                loadPlayerPunishments(playerName);
            }
        });
    }
    
    // Ban player form
    const banPlayerForm = document.getElementById('ban-player-form');
    if (banPlayerForm) {
        banPlayerForm.addEventListener('submit', handleBanPlayer);
    }
    
    // Mute player form
    const mutePlayerForm = document.getElementById('mute-player-form');
    if (mutePlayerForm) {
        mutePlayerForm.addEventListener('submit', handleMutePlayer);
    }
    
    // User form
    const userForm = document.getElementById('user-form');
    if (userForm) {
        userForm.addEventListener('submit', handleUserForm);
    }
    
    // Password form
    const passwordForm = document.getElementById('password-form');
    if (passwordForm) {
        passwordForm.addEventListener('submit', handlePasswordForm);
    }
}

/**
 * Shows a loading spinner
 * 
 * @param {string} elementId The ID of the element to show the spinner in
 */
function showSpinner(elementId) {
    const element = document.getElementById(elementId);
    if (element) {
        // Check if this is a table body element
        if (element.tagName === 'TBODY') {
            element.innerHTML = '<tr><td colspan="7" class="text-center"><div class="spinner-border text-primary" role="status"><span class="visually-hidden">Loading...</span></div></td></tr>';
        } else {
            element.innerHTML = '<div class="spinner-container"><div class="spinner-border text-primary" role="status"><span class="visually-hidden">Loading...</span></div></div>';
        }
    }
}

/**
 * Shows an error message
 * 
 * @param {string} message The error message
 * @param {string} elementId The ID of the element to show the error in
 */
function showErrorMessage(message, elementId) {
    const element = document.getElementById(elementId);
    if (element) {
        element.innerHTML = `<div class="alert alert-danger">${message}</div>`;
    }
}

/**
 * Creates a card for the dashboard
 * 
 * @param {string} title The card title
 * @param {string} value The card value
 * @param {string} icon The card icon
 * @param {string} color The card color
 * @returns {string} The card HTML
 */
function createDashboardCard(title, value, icon, color) {
    return `
        <div class="col-md-3 col-sm-6 mb-4">
            <div class="card">
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <h6 class="text-muted">${title}</h6>
                            <h2 class="display-4 fw-bold">${value}</h2>
                        </div>
                        <div class="fs-1 text-${color}">
                            <i class="bi ${icon}"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `;
}

/**
 * Loads the dashboard content
 */
async function loadDashboard() {
    try {
        // Show loading indicators
        showSpinner('dashboard-stats');
        showSpinner('recent-modlogs');
        showSpinner('recent-appeals');
        
        // Clear previous errors
        const dashboardError = document.getElementById('dashboard-error');
        if (dashboardError) {
            dashboardError.innerHTML = '';
        }
        
        let hasErrors = false;
        let hasData = false;
        let errorMessage = '';
        
        try {
            // Fetch dashboard stats
            const statsResponse = await apiGet(API.dashboard.stats());
            if (statsResponse && statsResponse.success) {
                // Display dashboard stats
                displayDashboardStats(statsResponse.data);
                hasData = true;
            } else if (statsResponse) {
                errorMessage += `Stats: ${statsResponse.error || 'Unknown error'}<br>`;
                hasErrors = true;
            }
        } catch (statsError) {
            errorMessage += `Could not load dashboard stats.<br>`;
            hasErrors = true;
            console.error('Error loading stats:', statsError);
        }
        
        // If we had errors with the stats, show a friendly message in the stats container
        if (hasErrors && !hasData) {
            const statsContainer = document.getElementById('dashboard-stats');
            if (statsContainer) {
                statsContainer.innerHTML = `
                    <div class="alert alert-warning">
                        <h5>Dashboard data unavailable</h5>
                        <p>Backend API endpoints not found or not responding. This could be because:</p>
                        <ul>
                            <li>The server is still being developed and these endpoints are not implemented yet</li>
                            <li>There is a configuration issue with the API endpoints</li>
                        </ul>
                    </div>
                `;
            }
        }
        
        // Load recent moderation logs
        try {
            const logsResponse = await apiGet(API.dashboard.recentModLogs());
            if (logsResponse && logsResponse.success) {
                displayRecentModLogs(logsResponse.logs);
            } else {
                document.getElementById('recent-modlogs').innerHTML = `
                    <div class="alert alert-warning">
                        <p>Recent moderation logs unavailable. ${logsResponse?.error || 'API endpoint not responding.'}</p>
                    </div>
                `;
            }
        } catch (logsError) {
            console.error('Error loading recent logs:', logsError);
            document.getElementById('recent-modlogs').innerHTML = `
                <div class="alert alert-warning">
                    <p>Recent moderation logs unavailable. API endpoint not responding.</p>
                </div>
            `;
        }
        
        // Load recent appeals
        try {
            const appealsResponse = await apiGet(API.dashboard.recentAppeals());
            if (appealsResponse && appealsResponse.success) {
                displayRecentAppeals(appealsResponse.appeals);
            } else {
                document.getElementById('recent-appeals').innerHTML = `
                    <div class="alert alert-warning">
                        <p>Recent appeals unavailable. ${appealsResponse?.error || 'API endpoint not responding.'}</p>
                    </div>
                `;
            }
        } catch (appealsError) {
            console.error('Error loading recent appeals:', appealsError);
            document.getElementById('recent-appeals').innerHTML = `
                <div class="alert alert-warning">
                    <p>Recent appeals unavailable. API endpoint not responding.</p>
                </div>
            `;
        }
    } catch (error) {
        console.error('Error loading dashboard:', error);
    }
}

/**
 * Displays the dashboard statistics
 * 
 * @param {Object} stats The dashboard statistics
 */
function displayDashboardStats(stats) {
    const dashboardStats = document.getElementById('dashboard-stats');
    
    if (!dashboardStats) return;
    
    let html = '<div class="row">';
    
    // Active bans card
    html += createDashboardCard(
        'Active Bans',
        stats.activeBans || 0,
        'bi-slash-circle',
        'danger'
    );
    
    // Active mutes card
    html += createDashboardCard(
        'Muted Players',
        stats.activeMutes || 0,
        'bi-mic-mute',
        'warning'
    );
    
    // Pending appeals card
    html += createDashboardCard(
        'Pending Appeals',
        stats.pendingAppeals || 0,
        'bi-exclamation-triangle',
        'primary'
    );
    
    // Total users card
    html += createDashboardCard(
        'Total Users',
        stats.totalUsers || 0,
        'bi-people',
        'success'
    );
    
    html += '</div>';
    
    dashboardStats.innerHTML = html;
}

/**
 * Displays recent moderation logs
 * 
 * @param {Array} logs The recent moderation logs
 */
function displayRecentModLogs(logs) {
    const recentModLogs = document.getElementById('recent-modlogs');
    
    if (!recentModLogs) return;
    
    if (!logs || logs.length === 0) {
        recentModLogs.innerHTML = '<div class="alert alert-info">No recent moderation logs.</div>';
        return;
    }
    
    let html = `
        <div class="table-responsive">
            <table class="table table-striped">
                <thead>
                    <tr>
                        <th>Time</th>
                        <th>Moderator</th>
                        <th>Action</th>
                        <th>Target</th>
                        <th>Reason</th>
                    </tr>
                </thead>
                <tbody>
    `;
    
    logs.forEach(log => {
        html += `
            <tr>
                <td>${formatDate(log.timestamp)}</td>
                <td>${log.moderator}</td>
                <td>${log.action}</td>
                <td>${log.target}</td>
                <td>${truncateString(log.reason, 50)}</td>
            </tr>
        `;
    });
    
    html += `
                </tbody>
            </table>
        </div>
        <a href="#" class="btn btn-sm btn-primary" onclick="loadSection('modlogs')">View All Logs</a>
    `;
    
    recentModLogs.innerHTML = html;
}

/**
 * Displays recent appeals
 * 
 * @param {Array} appeals The recent appeals
 */
function displayRecentAppeals(appeals) {
    const recentAppeals = document.getElementById('recent-appeals');
    
    if (!recentAppeals) return;
    
    if (!appeals || appeals.length === 0) {
        recentAppeals.innerHTML = '<div class="alert alert-info">No recent appeals.</div>';
        return;
    }
    
    let html = `
        <div class="table-responsive">
            <table class="table table-striped">
                <thead>
                    <tr>
                        <th>Date</th>
                        <th>Player</th>
                        <th>Status</th>
                        <th>Action</th>
                    </tr>
                </thead>
                <tbody>
    `;
    
    appeals.forEach(appeal => {
        const statusClass = appeal.status === 'PENDING' ? 'warning' : 
                           appeal.status === 'APPROVED' ? 'success' : 'danger';
        
        html += `
            <tr>
                <td>${formatDate(appeal.submissionTime)}</td>
                <td>${appeal.playerName}</td>
                <td><span class="badge bg-${statusClass}">${appeal.status}</span></td>
                <td>
                    <a href="#" class="btn btn-sm btn-primary" onclick="viewAppeal('${appeal.id}')">View</a>
                </td>
            </tr>
        `;
    });
    
    html += `
                </tbody>
            </table>
        </div>
        <a href="#" class="btn btn-sm btn-primary" onclick="loadSection('appeals')">View All Appeals</a>
    `;
    
    recentAppeals.innerHTML = html;
}

/**
 * Loads a specific section
 * 
 * @param {string} sectionId The ID of the section to load
 */
function loadSection(sectionId) {
    console.log('Loading section:', sectionId);
    
    try {
        // Hide all content sections first
        document.querySelectorAll('.content-section').forEach(section => {
            section.classList.add('d-none');
        });
        
        // Show the selected section
        const selectedSection = document.getElementById(sectionId + '-section');
        if (selectedSection) {
            selectedSection.classList.remove('d-none');
            console.log('Section displayed:', sectionId);
            
            // Update active nav item
            document.querySelectorAll('.nav-link').forEach(link => {
                link.classList.remove('active');
            });
            
            const activeNavItem = document.querySelector(`.nav-link[data-section="${sectionId}"]`);
            if (activeNavItem) {
                activeNavItem.classList.add('active');
            }
            
            // Load the section content
            try {
                loadSectionContent(sectionId);
            } catch (error) {
                console.error(`Error loading ${sectionId} content:`, error);
                const errorContainer = document.getElementById(`${sectionId}-error`);
                if (errorContainer) {
                    errorContainer.innerHTML = `
                        <div class="alert alert-danger">
                            <strong>Error loading content:</strong> ${error.message || 'Unknown error'}
                        </div>
                    `;
                }
            }
        } else {
            console.error('Section not found:', sectionId + '-section');
            // Fallback to dashboard
            loadSection('dashboard');
        }
    } catch (error) {
        console.error('Error in loadSection:', error);
    }
}

// Initialize auth when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    console.log("DOM fully loaded, initializing application");
    
    // First, hide any loading indicators
    const loadingElements = document.querySelectorAll('.loading-overlay, #loading');
    loadingElements.forEach(el => {
        if (el) {
            el.classList.add('d-none');
            el.style.display = 'none';
        }
    });
    
    // Initialize auth system
    if (typeof initAuth === 'function') {
        initAuth();
    } else {
        console.error('initAuth function not found');
    }
    
    // If token exists in local storage, force admin panel visibility
    const token = localStorage.getItem('token');
    if (token) {
        console.log("Token found in localStorage, ensuring admin panel is visible");
        const loginContainer = document.getElementById('login-container');
        const adminPanel = document.getElementById('admin-panel');
        
        if (loginContainer) {
            loginContainer.classList.add('d-none');
            loginContainer.setAttribute('style', 'display: none !important');
        }
        
        if (adminPanel) {
            adminPanel.classList.remove('d-none');
            adminPanel.setAttribute('style', 'display: block !important');
            
            // Force visibility of dashboard section
            const dashboardSection = document.getElementById('dashboard-section');
            if (dashboardSection) {
                dashboardSection.style.display = 'block';
            }
        }
    }
    
    // Check if Bootstrap is available
    if (typeof bootstrap === 'undefined') {
        console.error('Bootstrap JavaScript is not loaded properly. Some UI components may not work.');
    } else {
        console.log('Bootstrap JavaScript is properly loaded.');
    }
});

/**
 * Loads the punishments section
 */
async function loadPunishments() {
    try {
        // Show loading indicator
        showSpinner('active-punishments');
        
        // Fetch punishments data
        const response = await apiGet(API.punishments.all());
        
        if (response && response.success) {
            // Display punishments
            displayPunishments(response.punishments);
            
            // Also load punishment history
            loadPunishmentHistory();
        } else {
            showErrorMessage('Failed to load punishments: ' + (response?.error || 'Unknown error'), 'punishments-error');
        }
        
    } catch (error) {
        console.error('Error loading punishments:', error);
        showErrorMessage('Failed to load punishments: ' + error.message, 'punishments-error');
    }
}

/**
 * Loads the punishment history
 * @param {number} page The page number (0-based)
 */
async function loadPunishmentHistory(page = 0) {
    try {
        // Show loading indicator
        showSpinner('punishment-history');
        
        // Get page size from select element
        const pageSize = document.getElementById('punishments-page-size')?.value || 10;
        
        // Fetch punishment history data
        // Using the base punishments endpoint since the history/all endpoint doesn't exist
        const response = await apiGet(`${API.punishments.all()}?page=${page}&size=${pageSize}&includeExpired=true`);
        
        if (response && response.success) {
            // Display punishment history
            displayPunishmentHistory(response.punishments, response);
            
            // Update pagination
            const paginationContainer = document.getElementById('punishments-pagination');
            if (paginationContainer) {
                paginationContainer.innerHTML = createPagination(response, loadPunishmentHistory);
            }
        } else {
            console.error('Failed to load punishment history:', response?.error || 'Unknown error');
            document.getElementById('punishment-history').innerHTML = 
                '<tr><td colspan="6" class="text-center">Failed to load punishment history.</td></tr>';
        }
        
    } catch (error) {
        console.error('Error loading punishment history:', error);
        document.getElementById('punishment-history').innerHTML = 
            '<tr><td colspan="6" class="text-center">Error loading punishment history.</td></tr>';
    }
}

/**
 * Displays punishment history
 * 
 * @param {Array} punishments The punishment history
 * @param {Object} paginationData The pagination data
 */
function displayPunishmentHistory(punishments, paginationData) {
    const historyElement = document.getElementById('punishment-history');
    
    if (!historyElement) {
        console.error('Element with ID "punishment-history" not found');
        return;
    }
    
    if (!punishments || punishments.length === 0) {
        historyElement.innerHTML = '<tr><td colspan="6" class="text-center">No punishment history found.</td></tr>';
        return;
    }
    
    let html = '';
    
    punishments.forEach(punishment => {
        const typeClass = punishment.type === 'BAN' ? 'danger' : 
                        punishment.type === 'MUTE' ? 'warning' : 'info';
        
        html += `
            <tr>
                <td>${punishment.playerName}</td>
                <td><span class="badge bg-${typeClass}">${punishment.type}</span></td>
                <td>${truncateString(punishment.reason, 50) || 'N/A'}</td>
                <td>${punishment.moderator || 'N/A'}</td>
                <td>${formatDate(punishment.timestamp)}</td>
                <td>${punishment.duration || (punishment.type === 'WARNING' ? `Warnings: ${punishment.count}` : 'Permanent')}</td>
            </tr>
        `;
    });
    
    historyElement.innerHTML = html;
    
    // Setup page size change event
    const pageSizeSelect = document.getElementById('punishments-page-size');
    if (pageSizeSelect) {
        pageSizeSelect.onchange = () => loadPunishmentHistory(0); // Reset to first page when changing page size
    }
}

/**
 * Displays all punishments
 * 
 * @param {Array} punishments The punishments list
 */
function displayPunishments(punishments) {
    const punishmentsList = document.getElementById('active-punishments');
    
    if (!punishmentsList) {
        console.error('Element with ID "active-punishments" not found');
        return;
    }
    
    if (!punishments || punishments.length === 0) {
        punishmentsList.innerHTML = '<tr><td colspan="7" class="text-center">No active punishments found.</td></tr>';
        return;
    }
    
    let html = '';
    
    punishments.forEach(punishment => {
        const typeClass = punishment.type === 'BAN' ? 'danger' : 
                        punishment.type === 'MUTE' ? 'warning' : 'info';
        
        html += `
            <tr>
                <td>${punishment.playerName}</td>
                <td><span class="badge bg-${typeClass}">${punishment.type}</span></td>
                <td>${truncateString(punishment.reason, 50) || 'N/A'}</td>
                <td>${punishment.moderator || 'N/A'}</td>
                <td>${formatDate(punishment.timestamp)}</td>
                <td>${punishment.duration || 'Permanent'}</td>
                <td>
                    <button class="btn btn-sm btn-primary" onclick="viewPlayerPunishments('${punishment.playerName}')">View</button>
                    ${punishment.type === 'BAN' ? 
                        `<button class="btn btn-sm btn-success" onclick="unbanPlayer('${punishment.playerName}')">Unban</button>` : ''}
                    ${punishment.type === 'MUTE' ? 
                        `<button class="btn btn-sm btn-success" onclick="unmutePlayer('${punishment.playerName}')">Unmute</button>` : ''}
                </td>
            </tr>
        `;
    });
    
    punishmentsList.innerHTML = html;
}

/**
 * Loads punishments for a specific player
 * 
 * @param {string} playerName The player name
 */
async function loadPlayerPunishments(playerName) {
    try {
        // Show loading indicator
        showSpinner('player-punishments');
        
        // Fetch player punishments data
        const response = await apiGet(API.punishments.player(playerName));
        
        if (response && response.success) {
            // Display player punishments
            displayPlayerPunishments(response.player, response.punishments);
            
            // Show player punishments section
            document.getElementById('all-punishments').style.display = 'none';
            document.getElementById('player-punishments').style.display = 'block';
        } else {
            showErrorMessage('Failed to load player punishments: ' + (response?.error || 'Unknown error'), 'punishments-error');
        }
        
    } catch (error) {
        console.error('Error loading player punishments:', error);
        showErrorMessage('Failed to load player punishments: ' + error.message, 'punishments-error');
    }
}

/**
 * Displays punishments for a specific player
 * 
 * @param {string} playerName The player name
 * @param {Array} punishments The punishments list
 */
function displayPlayerPunishments(playerName, punishments) {
    const playerPunishments = document.getElementById('player-punishments');
    
    if (!playerPunishments) return;
    
    let html = `
        <div class="d-flex justify-content-between align-items-center mb-3">
            <h3>Punishments for ${playerName}</h3>
            <button class="btn btn-secondary" onclick="backToPunishments()">Back to All Punishments</button>
        </div>
    `;
    
    if (!punishments || punishments.length === 0) {
        html += '<div class="alert alert-info">No punishments found for this player.</div>';
    } else {
        html += `
            <div class="table-responsive">
                <table class="table table-striped">
                    <thead>
                        <tr>
                            <th>Type</th>
                            <th>Reason</th>
                            <th>Moderator</th>
                            <th>Date</th>
                            <th>Duration</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
        `;
        
        punishments.forEach(punishment => {
            const typeClass = punishment.type === 'BAN' ? 'danger' : 
                            punishment.type === 'MUTE' ? 'warning' : 
                            punishment.type === 'WARNING' ? 'info' : 'secondary';
            
            html += `
                <tr>
                    <td><span class="badge bg-${typeClass}">${punishment.type}</span></td>
                    <td>${truncateString(punishment.reason, 50) || 'N/A'}</td>
                    <td>${punishment.moderator || 'N/A'}</td>
                    <td>${formatDate(punishment.timestamp)}</td>
                    <td>${punishment.duration || (punishment.type === 'WARNING' ? `Warnings: ${punishment.count}` : 'Permanent')}</td>
                    <td>
                        ${punishment.type === 'BAN' && !punishment.expired ? 
                            `<button class="btn btn-sm btn-success" onclick="unbanPlayer('${playerName}')">Unban</button>` : ''}
                        ${punishment.type === 'MUTE' && !punishment.expired ? 
                            `<button class="btn btn-sm btn-success" onclick="unmutePlayer('${playerName}')">Unmute</button>` : ''}
                    </td>
                </tr>
            `;
        });
        
        html += `
                    </tbody>
                </table>
            </div>
        `;
    }
    
    // Add buttons for punishment actions
    html += `
        <div class="mt-4">
            <h4>Actions</h4>
            <div class="btn-group">
                <button class="btn btn-danger" data-bs-toggle="modal" data-bs-target="#ban-player-modal" onclick="preparePlayerAction('${playerName}', 'ban')">Ban Player</button>
                <button class="btn btn-warning" data-bs-toggle="modal" data-bs-target="#mute-player-modal" onclick="preparePlayerAction('${playerName}', 'mute')">Mute Player</button>
                <button class="btn btn-info" onclick="warnPlayer('${playerName}')">Warn Player</button>
            </div>
        </div>
    `;
    
    playerPunishments.innerHTML = html;
}

/**
 * Prepares a punishment action modal for a player
 * 
 * @param {string} playerName The player name
 * @param {string} action The action to perform (ban or mute)
 */
function preparePlayerAction(playerName, action) {
    if (action === 'ban') {
        document.getElementById('ban-player-name').value = playerName;
    } else if (action === 'mute') {
        document.getElementById('mute-player-name').value = playerName;
    }
}

/**
 * Handles the ban player form submission
 * 
 * @param {Event} event The form submit event
 */
async function handleBanPlayer(event) {
    event.preventDefault();
    
    const playerName = document.getElementById('ban-player-name').value;
    const reason = document.getElementById('ban-reason').value;
    const duration = document.getElementById('ban-duration').value;
    
    if (!playerName || !reason) {
        showError('Player name and reason are required.');
        return;
    }
    
    try {
        // Show loading indicator
        showLoading(true);
        
        // Send ban request
        const response = await apiPost(API.punishments.ban(), {
            playerName,
            reason,
            duration: duration || 'permanent'
        });
        
        if (response && response.success) {
            // Hide modal
            const modal = bootstrap.Modal.getInstance(document.getElementById('ban-player-modal'));
            modal.hide();
            
            // Show success message
            alert('Player banned successfully.');
            
            // Reload punishments
            if (currentSection === 'punishments') {
                loadPunishments();
            }
        } else {
            showError(response?.error || 'Failed to ban player.');
        }
    } catch (error) {
        showError('Failed to ban player: ' + error.message);
    } finally {
        // Hide loading indicator
        showLoading(false);
    }
}

/**
 * Handles the mute player form submission
 * 
 * @param {Event} event The form submit event
 */
async function handleMutePlayer(event) {
    event.preventDefault();
    
    const playerName = document.getElementById('mute-player-name').value;
    const reason = document.getElementById('mute-reason').value;
    const duration = document.getElementById('mute-duration').value;
    
    if (!playerName || !reason) {
        showError('Player name and reason are required.');
        return;
    }
    
    try {
        // Show loading indicator
        showLoading(true);
        
        // Send mute request
        const response = await apiPost(API.punishments.mute(), {
            playerName,
            reason,
            duration: duration || 'permanent'
        });
        
        if (response && response.success) {
            // Hide modal
            const modal = bootstrap.Modal.getInstance(document.getElementById('mute-player-modal'));
            modal.hide();
            
            // Show success message
            alert('Player muted successfully.');
            
            // Reload punishments
            if (currentSection === 'punishments') {
                loadPunishments();
            }
        } else {
            showError(response?.error || 'Failed to mute player.');
        }
    } catch (error) {
        showError('Failed to mute player: ' + error.message);
    } finally {
        // Hide loading indicator
        showLoading(false);
    }
}

/**
 * Unbans a player
 * 
 * @param {string} playerName The player name to unban
 */
async function unbanPlayer(playerName) {
    if (!confirm(`Are you sure you want to unban ${playerName}?`)) {
        return;
    }
    
    try {
        // Show loading indicator
        showLoading(true);
        
        // Send unban request
        const response = await apiPost(API.punishments.unban(playerName), {});
        
        if (response && response.success) {
            // Show success message
            alert('Player unbanned successfully.');
            
            // Reload punishments
            if (currentSection === 'punishments') {
                if (document.getElementById('all-punishments').style.display === 'none') {
                    loadPlayerPunishments(playerName);
                } else {
                    loadPunishments();
                }
            }
        } else {
            showError(response?.error || 'Failed to unban player.');
        }
    } catch (error) {
        showError('Failed to unban player: ' + error.message);
    } finally {
        // Hide loading indicator
        showLoading(false);
    }
}

/**
 * Unmutes a player
 * 
 * @param {string} playerName The player name to unmute
 */
async function unmutePlayer(playerName) {
    if (!confirm(`Are you sure you want to unmute ${playerName}?`)) {
        return;
    }
    
    try {
        // Show loading indicator
        showLoading(true);
        
        // Send unmute request
        const response = await apiPost(API.punishments.unmute(playerName), {});
        
        if (response && response.success) {
            // Show success message
            alert('Player unmuted successfully.');
            
            // Reload punishments
            if (currentSection === 'punishments') {
                if (document.getElementById('all-punishments').style.display === 'none') {
                    loadPlayerPunishments(playerName);
                } else {
                    loadPunishments();
                }
            }
        } else {
            showError(response?.error || 'Failed to unmute player.');
        }
    } catch (error) {
        showError('Failed to unmute player: ' + error.message);
    } finally {
        // Hide loading indicator
        showLoading(false);
    }
}

/**
 * Warns a player
 * 
 * @param {string} playerName The player name to warn
 * @param {string} reason Optional reason for warning, will prompt if not provided
 */
async function warnPlayer(playerName, reason) {
    // If reason is not provided, prompt for it
    if (!reason) {
        reason = prompt(`Enter reason for warning ${playerName}:`);
        
        if (!reason) {
            return; // User cancelled
        }
    }
    
    try {
        // Show loading indicator
        showLoading(true);
        
        // Send warn request
        const response = await apiPost(API.punishments.warn(playerName), {
            reason
        });
        
        if (response && response.success) {
            // Show success message
            alert(`Player warned successfully. This is warning #${response.warningCount}`);
            
            // Reload punishments
            if (currentSection === 'punishments') {
                loadPunishments();
            }
        } else {
            showError(response?.error || 'Failed to warn player.');
        }
    } catch (error) {
        showError('Failed to warn player: ' + error.message);
    } finally {
        // Hide loading indicator
        showLoading(false);
    }
}

/**
 * Goes back to the all punishments view
 */
function backToPunishments() {
    document.getElementById('all-punishments').style.display = 'block';
    document.getElementById('player-punishments').style.display = 'none';
    loadPunishments();
}

// Add global error handler for unhandled promise rejections
window.addEventListener('unhandledrejection', function(event) {
    console.error('Unhandled promise rejection:', event.reason);
    showError('An unexpected error occurred. Please check the console for details.');
});

/**
 * Shows or hides a loading indicator
 * 
 * @param {boolean} show Whether to show or hide the loading indicator
 */
function showLoading(show) {
    const loadingIndicator = document.getElementById('loading-indicator');
    
    if (!loadingIndicator) {
        // Create loading indicator if it doesn't exist
        const indicator = document.createElement('div');
        indicator.id = 'loading-indicator';
        indicator.className = 'loading-overlay';
        indicator.innerHTML = '<div class="spinner-border text-primary" role="status"><span class="visually-hidden">Loading...</span></div>';
        
        // Add styles
        const style = document.createElement('style');
        style.textContent = `
            .loading-overlay {
                position: fixed;
                top: 0;
                left: 0;
                width: 100%;
                height: 100%;
                background-color: rgba(0, 0, 0, 0.5);
                display: flex;
                justify-content: center;
                align-items: center;
                z-index: 9999;
            }
        `;
        
        document.head.appendChild(style);
        document.body.appendChild(indicator);
    }
    
    // Show or hide the loading indicator
    document.getElementById('loading-indicator').style.display = show ? 'flex' : 'none';
}

/**
 * View punishments for a specific player
 * 
 * @param {string} playerName The player name
 */
function viewPlayerPunishments(playerName) {
    if (!playerName) return;
    
    // Load player punishments
    loadPlayerPunishments(playerName);
} 