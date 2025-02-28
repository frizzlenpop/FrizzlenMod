/**
 * Appeals Functions for FrizzlenMod Admin Panel
 */

/**
 * Loads the appeals with optional status filter
 * 
 * @param {number} page The page number
 * @param {string} status The status filter (PENDING, APPROVED, DENIED)
 */
async function loadAppeals(page = 0, status = null) {
    try {
        console.log("Loading appeals with page:", page, "status:", status);
        
        // Show loading indicator
        showSpinner('appeals-container');
        
        // Clear any previous error messages
        const errorElement = document.getElementById('appeals-error');
        if (errorElement) {
            errorElement.innerHTML = '';
            errorElement.style.display = 'none';
        }
        
        // Fetch appeals data
        let url = status ? 
            API.appeals.byStatus(status, page, PAGE_SIZE) : 
            API.appeals.all(page, PAGE_SIZE);
        
        console.log("Fetching appeals from URL:", url);
        
        const response = await apiGet(url);
        console.log("Appeals API response:", response);
        
        // Check for various response formats and adapt accordingly
        if (response && (response.success || response.appeals)) {
            // Display appeals - some APIs might return appeals directly, others nested
            const appeals = response.appeals || response;
            // Pass pagination if available
            const paginationInfo = response.pagination || {
                page: page,
                totalPages: response.totalPages || 1
            };
            
            displayAppeals(appeals, paginationInfo);
            
            // Update status filter UI
            updateAppealsStatusFilter(status);
        } else {
            console.error("Failed to load appeals:", response?.error || "Unknown error");
            showErrorMessage('Failed to load appeals: ' + (response?.error || 'Unknown error'), 'appeals-error');
        }
        
    } catch (error) {
        console.error('Error loading appeals:', error);
        showErrorMessage('Failed to load appeals: ' + error.message, 'appeals-error');
    }
}

/**
 * Shows an error message in the specified container if not already defined elsewhere
 * 
 * @param {string} message The error message to show
 * @param {string} containerId The ID of the container element
 */
if (typeof showErrorMessage !== 'function') {
    function showErrorMessage(message, containerId) {
        const container = document.getElementById(containerId);
        if (container) {
            container.innerHTML = `<div class="alert alert-danger">${message}</div>`;
            container.style.display = 'block';
        } else {
            console.error(`Error container ${containerId} not found`);
        }
    }
}

/**
 * Displays appeals
 * 
 * @param {Array} appeals The appeals list
 * @param {Object} response The full API response
 */
function displayAppeals(appeals, response) {
    const appealsContainer = document.getElementById('appeals-container');
    
    if (!appealsContainer) return;
    
    if (!appeals || appeals.length === 0) {
        appealsContainer.innerHTML = '<div class="alert alert-info">No appeals found.</div>';
        return;
    }
    
    let html = `
        <div class="table-responsive">
            <table class="table table-striped">
                <thead>
                    <tr>
                        <th>Date</th>
                        <th>Player</th>
                        <th>Ban Reason</th>
                        <th>Status</th>
                        <th>Actions</th>
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
                <td>${truncateString(appeal.banReason, 50) || 'N/A'}</td>
                <td><span class="badge bg-${statusClass}">${appeal.status}</span></td>
                <td>
                    <button class="btn btn-sm btn-primary" onclick="viewAppeal('${appeal.id}')">View</button>
                </td>
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
                ${createPagination(response, (page) => loadAppeals(page, response.statusFilter))}
            </div>
        `;
    }
    
    appealsContainer.innerHTML = html;
}

/**
 * Updates the appeals status filter UI
 * 
 * @param {string} activeStatus The active status filter
 */
function updateAppealsStatusFilter(activeStatus) {
    const filterButtons = document.querySelectorAll('.appeal-status-btn');
    
    filterButtons.forEach(btn => {
        btn.classList.remove('active');
        if ((btn.dataset.status === activeStatus) || 
            (btn.dataset.status === 'all' && !activeStatus)) {
            btn.classList.add('active');
        }
    });
}

/**
 * Loads and displays a specific appeal
 * 
 * @param {string} appealId The appeal ID
 */
async function viewAppeal(appealId) {
    try {
        // Show loading indicator
        showSpinner('appeal-details');
        
        // Show appeal details section
        document.getElementById('appeals-list').style.display = 'none';
        document.getElementById('appeal-details').style.display = 'block';
        
        // Fetch appeal data
        const response = await apiGet(API.appeals.get(appealId));
        
        if (response && response.success) {
            // Display appeal details
            displayAppealDetails(response.appeal);
        } else {
            showErrorMessage('Failed to load appeal: ' + (response?.error || 'Unknown error'), 'appeal-details-error');
        }
        
    } catch (error) {
        console.error('Error loading appeal:', error);
        showErrorMessage('Failed to load appeal: ' + error.message, 'appeal-details-error');
    }
}

/**
 * Displays appeal details
 * 
 * @param {Object} appeal The appeal object
 */
function displayAppealDetails(appeal) {
    const appealDetails = document.getElementById('appeal-details');
    
    if (!appealDetails) return;
    
    const statusClass = appeal.status === 'PENDING' ? 'warning' : 
                       appeal.status === 'APPROVED' ? 'success' : 'danger';
    
    let html = `
        <div class="d-flex justify-content-between align-items-center mb-3">
            <h3>Appeal from ${appeal.playerName}</h3>
            <button class="btn btn-secondary" onclick="backToAppeals()">Back to Appeals</button>
        </div>
        
        <div class="card mb-4">
            <div class="card-header">
                <div class="d-flex justify-content-between align-items-center">
                    <h5 class="mb-0">Appeal Information</h5>
                    <span class="badge bg-${statusClass}">${appeal.status}</span>
                </div>
            </div>
            <div class="card-body">
                <div class="row">
                    <div class="col-md-6">
                        <p><strong>Player Name:</strong> ${appeal.playerName}</p>
                        <p><strong>Submission Date:</strong> ${formatDate(appeal.submissionTime)}</p>
                        <p><strong>Ban Reason:</strong> ${appeal.banReason || 'N/A'}</p>
                    </div>
                    <div class="col-md-6">
                        <p><strong>Status:</strong> ${appeal.status}</p>
                        <p><strong>Contact Email:</strong> ${appeal.contactEmail || 'N/A'}</p>
                        <p><strong>Discord Tag:</strong> ${appeal.discordTag || 'N/A'}</p>
                    </div>
                </div>
                
                <div class="mt-3">
                    <h6>Appeal Text:</h6>
                    <div class="card bg-light">
                        <div class="card-body">
                            <p>${appeal.appealText.replace(/\n/g, '<br>')}</p>
                        </div>
                    </div>
                </div>
                
                ${appeal.adminResponse ? `
                <div class="mt-3">
                    <h6>Admin Response:</h6>
                    <div class="card bg-light">
                        <div class="card-body">
                            <p>${appeal.adminResponse.replace(/\n/g, '<br>')}</p>
                        </div>
                    </div>
                </div>
                ` : ''}
            </div>
        </div>
    `;
    
    // Comments section
    html += `
        <div class="card mb-4">
            <div class="card-header">
                <h5 class="mb-0">Staff Comments</h5>
            </div>
            <div class="card-body">
    `;
    
    if (!appeal.comments || appeal.comments.length === 0) {
        html += '<p class="text-muted">No comments yet.</p>';
    } else {
        appeal.comments.forEach(comment => {
            html += `
                <div class="comment-item">
                    <div class="comment-header">
                        <span>${comment.staffName}</span>
                        <span class="comment-time">${formatDate(comment.timestamp)}</span>
                    </div>
                    <div class="comment-body">
                        ${comment.comment.replace(/\n/g, '<br>')}
                    </div>
                </div>
            `;
        });
    }
    
    // Add comment form for pending appeals
    if (appeal.status === 'PENDING') {
        html += `
            <div class="mt-3">
                <form id="comment-form" onsubmit="handleAddComment(event, '${appeal.id}')">
                    <div class="mb-3">
                        <label for="comment-text" class="form-label">Add Comment</label>
                        <textarea class="form-control" id="comment-text" rows="3" required></textarea>
                    </div>
                    <button type="submit" class="btn btn-primary">Add Comment</button>
                </form>
            </div>
        `;
    }
    
    html += `
            </div>
        </div>
    `;
    
    // Action buttons for pending appeals
    if (appeal.status === 'PENDING') {
        html += `
            <div class="row">
                <div class="col-md-6 mb-3">
                    <div class="card">
                        <div class="card-header bg-success text-white">
                            <h5 class="mb-0">Approve Appeal</h5>
                        </div>
                        <div class="card-body">
                            <form id="approve-form" onsubmit="handleApproveAppeal(event, '${appeal.id}')">
                                <div class="mb-3">
                                    <label for="approve-response" class="form-label">Response to Player</label>
                                    <textarea class="form-control" id="approve-response" rows="3" required></textarea>
                                </div>
                                <button type="submit" class="btn btn-success">Approve & Unban Player</button>
                            </form>
                        </div>
                    </div>
                </div>
                <div class="col-md-6 mb-3">
                    <div class="card">
                        <div class="card-header bg-danger text-white">
                            <h5 class="mb-0">Deny Appeal</h5>
                        </div>
                        <div class="card-body">
                            <form id="deny-form" onsubmit="handleDenyAppeal(event, '${appeal.id}')">
                                <div class="mb-3">
                                    <label for="deny-response" class="form-label">Response to Player</label>
                                    <textarea class="form-control" id="deny-response" rows="3" required></textarea>
                                </div>
                                <button type="submit" class="btn btn-danger">Deny Appeal</button>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        `;
    }
    
    appealDetails.innerHTML = html;
}

/**
 * Handles adding a comment to an appeal
 * 
 * @param {Event} event The form submit event
 * @param {string} appealId The appeal ID
 */
async function handleAddComment(event, appealId) {
    event.preventDefault();
    
    const commentText = document.getElementById('comment-text').value;
    
    if (!commentText) {
        showError('Comment text is required.');
        return;
    }
    
    try {
        // Show loading indicator
        showLoading(true);
        
        // Send comment request
        const response = await apiPost(API.appeals.comment(appealId), {
            comment: commentText
        });
        
        if (response && response.success) {
            // Show success message
            alert('Comment added successfully.');
            
            // Reload appeal
            viewAppeal(appealId);
        } else {
            showError(response?.error || 'Failed to add comment.');
        }
    } catch (error) {
        showError('Failed to add comment: ' + error.message);
    } finally {
        // Hide loading indicator
        showLoading(false);
    }
}

/**
 * Handles approving an appeal
 * 
 * @param {Event} event The form submit event
 * @param {string} appealId The appeal ID
 */
async function handleApproveAppeal(event, appealId) {
    event.preventDefault();
    
    const response = document.getElementById('approve-response').value;
    
    if (!response) {
        showError('Response is required.');
        return;
    }
    
    if (!confirm('Are you sure you want to approve this appeal and unban the player?')) {
        return;
    }
    
    try {
        // Show loading indicator
        showLoading(true);
        
        // Send approve request
        const apiResponse = await apiPost(API.appeals.approve(appealId), {
            response: response
        });
        
        if (apiResponse && apiResponse.success) {
            // Show success message
            alert('Appeal approved successfully.');
            
            // Reload appeal
            viewAppeal(appealId);
        } else {
            showError(apiResponse?.error || 'Failed to approve appeal.');
        }
    } catch (error) {
        showError('Failed to approve appeal: ' + error.message);
    } finally {
        // Hide loading indicator
        showLoading(false);
    }
}

/**
 * Handles denying an appeal
 * 
 * @param {Event} event The form submit event
 * @param {string} appealId The appeal ID
 */
async function handleDenyAppeal(event, appealId) {
    event.preventDefault();
    
    const response = document.getElementById('deny-response').value;
    
    if (!response) {
        showError('Response is required.');
        return;
    }
    
    if (!confirm('Are you sure you want to deny this appeal?')) {
        return;
    }
    
    try {
        // Show loading indicator
        showLoading(true);
        
        // Send deny request
        const apiResponse = await apiPost(API.appeals.deny(appealId), {
            response: response
        });
        
        if (apiResponse && apiResponse.success) {
            // Show success message
            alert('Appeal denied successfully.');
            
            // Reload appeal
            viewAppeal(appealId);
        } else {
            showError(apiResponse?.error || 'Failed to deny appeal.');
        }
    } catch (error) {
        showError('Failed to deny appeal: ' + error.message);
    } finally {
        // Hide loading indicator
        showLoading(false);
    }
}

/**
 * Goes back to the appeals list
 */
function backToAppeals() {
    document.getElementById('appeals-list').style.display = 'block';
    document.getElementById('appeal-details').style.display = 'none';
} 