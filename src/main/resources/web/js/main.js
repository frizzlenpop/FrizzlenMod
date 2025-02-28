/**
 * Main JavaScript file for FrizzlenMod Admin Panel
 */

// Global variables
let currentSection = 'dashboard';
const PAGE_SIZE = 10;

/**
 * Initializes the admin panel
 */
function initAdminPanel() {
    // Set up navigation
    setupNavigation();
    
    // Load dashboard by default
    loadDashboard();
    
    // Set up event listeners for various actions
    setupEventListeners();
}

/**
 * Sets up navigation event listeners
 */
function setupNavigation() {
    const navLinks = document.querySelectorAll('.nav-link');
    
    navLinks.forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            
            // Remove active class from all links
            navLinks.forEach(l => l.classList.remove('active'));
            
            // Add active class to clicked link
            link.classList.add('active');
            
            // Get the section ID from data attribute
            const sectionId = link.dataset.section;
            
            // Hide all sections
            document.querySelectorAll('.content-section').forEach(section => {
                section.style.display = 'none';
            });
            
            // Show the selected section
            document.getElementById(sectionId + '-section').style.display = 'block';
            
            // Load the section content
            currentSection = sectionId;
            loadSectionContent(sectionId);
        });
    });
}

/**
 * Loads the content for a specific section
 * 
 * @param {string} sectionId The ID of the section to load
 */
function loadSectionContent(sectionId) {
    switch (sectionId) {
        case 'dashboard':
            loadDashboard();
            break;
        case 'punishments':
            loadPunishments();
            break;
        case 'appeals':
            loadAppeals();
            break;
        case 'modlogs':
            loadModLogs();
            break;
        case 'users':
            loadUsers();
            break;
    }
}

/**
 * Sets up event listeners for various actions
 */
function setupEventListeners() {
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
        element.innerHTML = '<div class="spinner-container"><div class="spinner-border text-primary" role="status"><span class="visually-hidden">Loading...</span></div></div>';
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
        
        // Fetch dashboard data
        const statsResponse = await apiGet(API.dashboard.stats());
        const recentLogsResponse = await apiGet(API.dashboard.recentModLogs());
        const recentAppealsResponse = await apiGet(API.dashboard.recentAppeals());
        
        if (statsResponse && statsResponse.success) {
            // Display dashboard stats
            displayDashboardStats(statsResponse.data);
        }
        
        if (recentLogsResponse && recentLogsResponse.success) {
            // Display recent moderation logs
            displayRecentModLogs(recentLogsResponse.data);
        }
        
        if (recentAppealsResponse && recentAppealsResponse.success) {
            // Display recent appeals
            displayRecentAppeals(recentAppealsResponse.data);
        }
        
    } catch (error) {
        console.error('Error loading dashboard:', error);
        showErrorMessage('Failed to load dashboard data: ' + error.message, 'dashboard-error');
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
    // Find the nav link and click it
    const navLink = document.querySelector(`.nav-link[data-section="${sectionId}"]`);
    if (navLink) {
        navLink.click();
    }
}

/**
 * Initializes the application
 */
function init() {
    // Initialize authentication
    initAuth();
    
    // Initialize admin panel (will be called after successful login)
    // The admin panel initialization is handled by the checkAuth function in auth.js
}

// Initialize the application when the DOM is loaded
document.addEventListener('DOMContentLoaded', init);

/**
 * Loads the punishments section
 */
async function loadPunishments() {
    try {
        // Show loading indicator
        showSpinner('punishments-list');
        
        // Fetch punishments data
        const response = await apiGet(API.punishments.all());
        
        if (response && response.success) {
            // Display punishments
            displayPunishments(response.punishments);
        } else {
            showErrorMessage('Failed to load punishments: ' + (response?.error || 'Unknown error'), 'punishments-error');
        }
        
    } catch (error) {
        console.error('Error loading punishments:', error);
        showErrorMessage('Failed to load punishments: ' + error.message, 'punishments-error');
    }
}

/**
 * Displays all punishments
 * 
 * @param {Array} punishments The punishments list
 */
function displayPunishments(punishments) {
    const punishmentsList = document.getElementById('punishments-list');
    
    if (!punishmentsList) return;
    
    if (!punishments || punishments.length === 0) {
        punishmentsList.innerHTML = '<div class="alert alert-info">No active punishments found.</div>';
        return;
    }
    
    let html = `
        <div class="table-responsive">
            <table class="table table-striped">
                <thead>
                    <tr>
                        <th>Player</th>
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
    
    html += `
                </tbody>
            </table>
        </div>
    `;
    
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