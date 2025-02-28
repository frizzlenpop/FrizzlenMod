/**
 * Moderation Logs Functions for FrizzlenMod Admin Panel
 */

/**
 * Loads the moderation logs
 * 
 * @param {number} page The page number
 * @param {string} filter The filter to apply (player, action, or timerange)
 * @param {Object} filterParams The filter parameters
 */
async function loadModLogs(page = 0, filter = null, filterParams = {}) {
    try {
        // Show loading indicator
        showSpinner('modlogs-container');
        
        // Build the API URL based on filter
        let url;
        if (filter === 'player') {
            url = API.modlogs.player(filterParams.playerName, page, PAGE_SIZE);
        } else if (filter === 'action') {
            url = API.modlogs.action(filterParams.action, page, PAGE_SIZE);
        } else if (filter === 'timerange') {
            url = API.modlogs.timeRange(filterParams.start, filterParams.end, page, PAGE_SIZE);
        } else {
            url = API.modlogs.all(page, PAGE_SIZE);
        }
        
        // Fetch moderation logs
        const response = await apiGet(url);
        
        if (response && response.success) {
            // Display moderation logs
            displayModLogs(response.logs, response);
            
            // Update filter UI
            updateModLogsFilterUI(filter, filterParams);
        } else {
            showErrorMessage('Failed to load moderation logs: ' + (response?.error || 'Unknown error'), 'modlogs-error');
        }
        
    } catch (error) {
        console.error('Error loading moderation logs:', error);
        showErrorMessage('Failed to load moderation logs: ' + error.message, 'modlogs-error');
    }
}

/**
 * Displays moderation logs
 * 
 * @param {Array} logs The moderation logs
 * @param {Object} response The full API response
 */
function displayModLogs(logs, response) {
    const modlogsContainer = document.getElementById('modlogs-container');
    
    if (!modlogsContainer) return;
    
    if (!logs || logs.length === 0) {
        modlogsContainer.innerHTML = '<div class="alert alert-info">No moderation logs found.</div>';
        return;
    }
    
    let html = `
        <div class="table-responsive">
            <table class="table table-striped">
                <thead>
                    <tr>
                        <th>Date</th>
                        <th>Moderator</th>
                        <th>Action</th>
                        <th>Target</th>
                        <th>Reason</th>
                        <th>Duration</th>
                    </tr>
                </thead>
                <tbody>
    `;
    
    logs.forEach(log => {
        const actionClass = log.action.includes('BAN') ? 'danger' : 
                          log.action.includes('MUTE') ? 'warning' : 
                          log.action.includes('WARN') ? 'info' : 'secondary';
        
        html += `
            <tr>
                <td>${formatDate(log.timestamp)}</td>
                <td>${log.moderator}</td>
                <td><span class="badge bg-${actionClass}">${log.action}</span></td>
                <td>${log.target}</td>
                <td>${truncateString(log.reason, 50)}</td>
                <td>${log.duration || 'N/A'}</td>
            </tr>
        `;
    });
    
    html += `
                </tbody>
            </table>
        </div>
    `;
    
    // Add pagination
    if (response.totalPages > 1) {
        html += `
            <div class="pagination-container">
                ${createPagination(response, (page) => loadModLogs(page))}
            </div>
        `;
    }
    
    modlogsContainer.innerHTML = html;
}

/**
 * Updates the moderation logs filter UI
 * 
 * @param {string} activeFilter The active filter
 * @param {Object} filterParams The filter parameters
 */
function updateModLogsFilterUI(activeFilter, filterParams) {
    // Update active filter buttons
    const filterButtons = document.querySelectorAll('.modlogs-filter-btn');
    filterButtons.forEach(btn => {
        btn.classList.remove('active');
        if (btn.dataset.filter === activeFilter) {
            btn.classList.add('active');
        }
    });
    
    // Update filter form visibility
    document.getElementById('player-filter-form').style.display = (activeFilter === 'player') ? 'block' : 'none';
    document.getElementById('action-filter-form').style.display = (activeFilter === 'action') ? 'block' : 'none';
    document.getElementById('timerange-filter-form').style.display = (activeFilter === 'timerange') ? 'block' : 'none';
    
    // Update form values if filter is active
    if (activeFilter === 'player' && filterParams.playerName) {
        document.getElementById('player-filter-input').value = filterParams.playerName;
    } else if (activeFilter === 'action' && filterParams.action) {
        document.getElementById('action-filter-select').value = filterParams.action;
    } else if (activeFilter === 'timerange') {
        if (filterParams.start) document.getElementById('start-date-input').value = new Date(filterParams.start).toISOString().split('T')[0];
        if (filterParams.end) document.getElementById('end-date-input').value = new Date(filterParams.end).toISOString().split('T')[0];
    }
}

/**
 * Handles the player filter form submission
 * 
 * @param {Event} event The form submit event
 */
function handlePlayerFilter(event) {
    event.preventDefault();
    
    const playerName = document.getElementById('player-filter-input').value;
    
    if (playerName) {
        loadModLogs(0, 'player', { playerName });
    }
}

/**
 * Handles the action filter form submission
 * 
 * @param {Event} event The form submit event
 */
function handleActionFilter(event) {
    event.preventDefault();
    
    const action = document.getElementById('action-filter-select').value;
    
    if (action) {
        loadModLogs(0, 'action', { action });
    }
}

/**
 * Handles the time range filter form submission
 * 
 * @param {Event} event The form submit event
 */
function handleTimeRangeFilter(event) {
    event.preventDefault();
    
    const startDate = document.getElementById('start-date-input').value;
    const endDate = document.getElementById('end-date-input').value;
    
    if (startDate && endDate) {
        const start = new Date(startDate).getTime();
        const end = new Date(endDate).getTime() + (24 * 60 * 60 * 1000); // Add a day to include the end date
        
        loadModLogs(0, 'timerange', { start, end });
    }
}

/**
 * Clears all filters and loads all moderation logs
 */
function clearModLogsFilters() {
    // Reset form values
    document.getElementById('player-filter-input').value = '';
    document.getElementById('action-filter-select').value = '';
    document.getElementById('start-date-input').value = '';
    document.getElementById('end-date-input').value = '';
    
    // Load all logs
    loadModLogs();
} 