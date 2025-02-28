/**
 * User Management Functions for FrizzlenMod Admin Panel
 */

/**
 * Loads the users list
 */
async function loadUsers() {
    try {
        console.log("Loading users list");
        
        // Show loading indicator
        showSpinner('users-container');
        
        // Clear any previous error messages
        const errorElement = document.getElementById('users-error');
        if (errorElement) {
            errorElement.innerHTML = '';
            errorElement.style.display = 'none';
        }
        
        // Fetch users data
        console.log("Fetching users from URL:", API.users.all());
        const response = await apiGet(API.users.all());
        console.log("Users API response:", response);
        
        // Check for various response formats and adapt accordingly
        if (response && (response.success || response.users)) {
            // Display users - some APIs might return users directly, others nested
            const users = response.users || response;
            displayUsers(users);
        } else {
            console.error("Failed to load users:", response?.error || "Unknown error");
            showErrorMessage('Failed to load users: ' + (response?.error || 'Unknown error'), 'users-error');
        }
        
    } catch (error) {
        console.error('Error loading users:', error);
        showErrorMessage('Failed to load users: ' + error.message, 'users-error');
    }
}

/**
 * Displays users
 * 
 * @param {Array} users The users list
 */
function displayUsers(users) {
    const usersContainer = document.getElementById('users-container');
    
    if (!usersContainer) return;
    
    if (!users || users.length === 0) {
        usersContainer.innerHTML = '<div class="alert alert-info">No users found.</div>';
        return;
    }
    
    let html = `
        <div class="table-responsive">
            <table class="table table-striped">
                <thead>
                    <tr>
                        <th>Username</th>
                        <th>Role</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
    `;
    
    users.forEach(user => {
        const roleClass = user.role === 'ADMIN' ? 'danger' : 
                         user.role === 'MODERATOR' ? 'warning' : 'info';
        
        html += `
            <tr>
                <td>${user.username}</td>
                <td><span class="badge bg-${roleClass}">${user.role}</span></td>
                <td>
                    <div class="btn-group">
                        <button class="btn btn-sm btn-warning" data-bs-toggle="modal" data-bs-target="#change-password-modal" onclick="preparePasswordChange('${user.username}')">Change Password</button>
                        <button class="btn btn-sm btn-info" onclick="changeUserRole('${user.username}', '${user.role}')">Change Role</button>
                        <button class="btn btn-sm btn-danger" onclick="deleteUser('${user.username}')">Delete</button>
                    </div>
                </td>
            </tr>
        `;
    });
    
    html += `
                </tbody>
            </table>
        </div>
        
        <div class="mt-3">
            <button class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#add-user-modal">Add User</button>
        </div>
    `;
    
    usersContainer.innerHTML = html;
}

/**
 * Prepares the change password modal for a user
 * 
 * @param {string} username The username
 */
function preparePasswordChange(username) {
    document.getElementById('password-username').value = username;
    document.getElementById('password-username-display').textContent = username;
}

/**
 * Handles the add user form submission
 * 
 * @param {Event} event The form submit event
 */
async function handleUserForm(event) {
    event.preventDefault();
    
    const username = document.getElementById('user-username').value;
    const password = document.getElementById('user-password').value;
    const role = document.getElementById('user-role').value;
    
    if (!username || !password || !role) {
        showError('All fields are required.');
        return;
    }
    
    try {
        // Show loading indicator
        showLoading(true);
        
        // Send create user request
        const response = await apiPost(API.users.create(), {
            username,
            password,
            role
        });
        
        if (response && response.success) {
            // Hide modal
            const modal = bootstrap.Modal.getInstance(document.getElementById('add-user-modal'));
            modal.hide();
            
            // Show success message
            alert('User created successfully.');
            
            // Reload users
            loadUsers();
        } else {
            showError(response?.error || 'Failed to create user.');
        }
    } catch (error) {
        showError('Failed to create user: ' + error.message);
    } finally {
        // Hide loading indicator
        showLoading(false);
    }
}

/**
 * Handles the change password form submission
 * 
 * @param {Event} event The form submit event
 */
async function handlePasswordForm(event) {
    event.preventDefault();
    
    const username = document.getElementById('password-username').value;
    const newPassword = document.getElementById('new-password').value;
    const confirmPassword = document.getElementById('confirm-password').value;
    
    if (!newPassword || !confirmPassword) {
        showError('All fields are required.');
        return;
    }
    
    if (newPassword !== confirmPassword) {
        showError('Passwords do not match.');
        return;
    }
    
    try {
        // Show loading indicator
        showLoading(true);
        
        // Send change password request
        const response = await apiPut(API.users.updatePassword(username), {
            newPassword
        });
        
        if (response && response.success) {
            // Hide modal
            const modal = bootstrap.Modal.getInstance(document.getElementById('change-password-modal'));
            modal.hide();
            
            // Show success message
            alert('Password changed successfully.');
        } else {
            showError(response?.error || 'Failed to change password.');
        }
    } catch (error) {
        showError('Failed to change password: ' + error.message);
    } finally {
        // Hide loading indicator
        showLoading(false);
    }
}

/**
 * Changes a user's role
 * 
 * @param {string} username The username
 * @param {string} currentRole The current role
 */
async function changeUserRole(username, currentRole) {
    const roles = ['ADMIN', 'MODERATOR', 'VIEWER'];
    const otherRoles = roles.filter(r => r !== currentRole);
    
    const newRole = prompt(`Select new role for ${username}:\n${otherRoles.join(', ')}`);
    
    if (!newRole) {
        return;
    }
    
    if (!roles.includes(newRole.toUpperCase())) {
        showError('Invalid role. Please choose from: ' + roles.join(', '));
        return;
    }
    
    try {
        // Show loading indicator
        showLoading(true);
        
        // Send change role request
        const response = await apiPut(API.users.updateRole(username), {
            newRole: newRole.toUpperCase()
        });
        
        if (response && response.success) {
            // Show success message
            alert('Role changed successfully.');
            
            // Reload users
            loadUsers();
        } else {
            showError(response?.error || 'Failed to change role.');
        }
    } catch (error) {
        showError('Failed to change role: ' + error.message);
    } finally {
        // Hide loading indicator
        showLoading(false);
    }
}

/**
 * Deletes a user
 * 
 * @param {string} username The username to delete
 */
async function deleteUser(username) {
    // Don't allow deleting yourself
    const currentUser = localStorage.getItem('username');
    if (username === currentUser) {
        showError('You cannot delete your own account.');
        return;
    }
    
    if (!confirm(`Are you sure you want to delete user ${username}? This action cannot be undone.`)) {
        return;
    }
    
    try {
        // Show loading indicator
        showLoading(true);
        
        // Send delete request
        const response = await apiDelete(API.users.delete(username));
        
        if (response && response.success) {
            // Show success message
            alert('User deleted successfully.');
            
            // Reload users
            loadUsers();
        } else {
            showError(response?.error || 'Failed to delete user.');
        }
    } catch (error) {
        showError('Failed to delete user: ' + error.message);
    } finally {
        // Hide loading indicator
        showLoading(false);
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

// Initialize form event listeners when the document is loaded
document.addEventListener('DOMContentLoaded', () => {
    // Add user form
    const userForm = document.getElementById('user-form');
    if (userForm) {
        userForm.addEventListener('submit', handleUserForm);
    }
    
    // Password form
    const passwordForm = document.getElementById('password-form');
    if (passwordForm) {
        passwordForm.addEventListener('submit', handlePasswordForm);
    }
}); 