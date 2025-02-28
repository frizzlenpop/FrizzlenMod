/**
 * Punishments Functions for FrizzlenMod Admin Panel
 * This file contains functions specific to the punishments section
 */

/**
 * Load punishments content
 * This is a wrapper for the loadPunishments function in main.js
 */
function loadPunishmentsContent() {
    // Call the loadPunishments function from main.js
    loadPunishments();
}

/**
 * Warns a player with additional confirmation
 * 
 * @param {string} playerName The player name to warn
 */
function confirmWarnPlayer(playerName) {
    const reason = prompt(`Enter reason for warning ${playerName}:`);
    
    if (!reason) {
        return; // User cancelled
    }
    
    if (confirm(`Are you sure you want to warn ${playerName} for: "${reason}"?`)) {
        // Call the warnPlayer function from main.js with the player name and reason
        warnPlayer(playerName, reason);
    }
}

/**
 * Shows ban history for a player
 * 
 * @param {string} playerName The player name to show history for
 */
async function showBanHistory(playerName) {
    try {
        // Show loading indicator
        showLoading(true);
        
        // Fetch ban history
        const response = await apiGet(API.punishments.history(playerName));
        
        if (response && response.success) {
            // Display history in a modal
            displayBanHistory(playerName, response.history);
        } else {
            showError(response?.error || 'Failed to load ban history.');
        }
    } catch (error) {
        showError('Failed to load ban history: ' + error.message);
    } finally {
        // Hide loading indicator
        showLoading(false);
    }
}

/**
 * Displays ban history in a modal
 * 
 * @param {string} playerName The player name
 * @param {Array} history The ban history
 */
function displayBanHistory(playerName, history) {
    // Create modal content
    let content = `<h5>Ban History for ${playerName}</h5>`;
    
    if (!history || history.length === 0) {
        content += '<p>No ban history found for this player.</p>';
    } else {
        content += '<ul class="list-group">';
        
        history.forEach(entry => {
            const date = formatDate(entry.timestamp);
            const status = entry.active ? 
                '<span class="badge bg-danger">Active</span>' : 
                '<span class="badge bg-secondary">Expired</span>';
            
            content += `
                <li class="list-group-item">
                    <div class="d-flex justify-content-between">
                        <span>${date}</span>
                        ${status}
                    </div>
                    <p><strong>Moderator:</strong> ${entry.moderator}</p>
                    <p><strong>Reason:</strong> ${entry.reason || 'No reason provided'}</p>
                    <p><strong>Duration:</strong> ${entry.duration || 'Permanent'}</p>
                </li>
            `;
        });
        
        content += '</ul>';
    }
    
    // Create modal if it doesn't exist
    let modal = document.getElementById('history-modal');
    
    if (!modal) {
        modal = document.createElement('div');
        modal.id = 'history-modal';
        modal.className = 'modal fade';
        modal.tabIndex = -1;
        modal.innerHTML = `
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">Player History</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                    </div>
                    <div class="modal-body" id="history-modal-content">
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
                    </div>
                </div>
            </div>
        `;
        
        document.body.appendChild(modal);
    }
    
    // Update modal content
    document.getElementById('history-modal-content').innerHTML = content;
    
    // Show modal
    const bsModal = new bootstrap.Modal(modal);
    bsModal.show();
}

/**
 * Fixes element IDs that might not match between HTML and JS
 */
function fixPunishmentModalIDs() {
    // Fix forms
    const banPlayerForm = document.getElementById('ban-player-form');
    if (banPlayerForm) {
        banPlayerForm.addEventListener('submit', handleBanPlayer);
    }
    
    // Fix modal IDs
    const banModal = document.getElementById('ban-player-modal');
    if (!banModal && document.getElementById('banPlayerModal')) {
        document.getElementById('banPlayerModal').id = 'ban-player-modal';
    }
    
    // Fix other potential ID mismatches
    const mutePlayerForm = document.getElementById('mute-player-form');
    if (mutePlayerForm) {
        mutePlayerForm.addEventListener('submit', handleMutePlayer);
    }
    
    const muteModal = document.getElementById('mute-player-modal');
    if (!muteModal && document.getElementById('mutePlayerModal')) {
        document.getElementById('mutePlayerModal').id = 'mute-player-modal';
    }
}

// Initialize punishments section when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    // Fix potential ID mismatches
    fixPunishmentModalIDs();
    
    // Add click handler for punishments nav link
    const punishmentsLink = document.querySelector('.nav-link[data-section="punishments"]');
    if (punishmentsLink) {
        punishmentsLink.addEventListener('click', loadPunishmentsContent);
    }
}); 