/**
 * Authentication Functions for FrizzlenMod Admin Panel
 */

/**
 * Handles the login form submission
 * 
 * @param {Event} event The form submit event
 * @returns {boolean} False to prevent default form submission
 */
async function handleLogin(event) {
    event.preventDefault();
    
    // Get form data
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    
    // Validate form data
    if (!username || !password) {
        showError('Please enter both username and password');
        return false;
    }
    
    try {
        // Show loading indicator
        showLoading(true);
        
        // Send login request
        const response = await apiPost(API.auth.login(), { username, password });
        
        // Check if login was successful
        if (response && response.success && response.data && response.data.token) {
            // Save token to local storage
            localStorage.setItem('token', response.data.token);
            localStorage.setItem('username', username);
            localStorage.setItem('role', response.data.role || 'ADMIN');
            
            // Hide login form and show admin panel
            document.getElementById('login-container').style.display = 'none';
            document.getElementById('admin-panel').style.display = 'block';
            
            // Initialize the admin panel
            initAdminPanel();
            
            // Update user info
            updateUserInfo();
        } else {
            showError(response.error || 'Login failed. Please check your credentials.');
        }
    } catch (error) {
        showError('Login failed: ' + (error.message || 'Unknown error'));
    } finally {
        // Hide loading indicator
        showLoading(false);
    }
    
    return false;
}

/**
 * Checks if the user is authenticated and shows the appropriate screen
 */
function checkAuth() {
    const token = localStorage.getItem('token');
    
    if (token) {
        // Hide login form and show admin panel
        document.getElementById('login-container').style.display = 'none';
        document.getElementById('admin-panel').style.display = 'block';
        
        // Initialize the admin panel
        initAdminPanel();
        
        // Update user info
        updateUserInfo();
    } else {
        // Show login form and hide admin panel
        document.getElementById('login-container').style.display = 'flex';
        document.getElementById('admin-panel').style.display = 'none';
    }
}

/**
 * Logs the user out
 */
function logout() {
    // Clear local storage
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('role');
    
    // Show login form and hide admin panel
    document.getElementById('login-container').style.display = 'flex';
    document.getElementById('admin-panel').style.display = 'none';
}

/**
 * Updates the user info in the navbar
 */
function updateUserInfo() {
    const username = localStorage.getItem('username');
    const role = localStorage.getItem('role');
    
    if (username) {
        document.getElementById('user-name').textContent = username;
        document.getElementById('user-role').textContent = role || 'User';
    }
}

/**
 * Shows or hides the loading indicator
 * 
 * @param {boolean} show Whether to show or hide the loading indicator
 */
function showLoading(show) {
    const loadingElement = document.getElementById('loading');
    
    if (loadingElement) {
        loadingElement.style.display = show ? 'flex' : 'none';
    }
}

/**
 * Initializes the authentication system
 */
function initAuth() {
    // Add login form submit handler
    const loginForm = document.getElementById('login-form');
    
    if (loginForm) {
        loginForm.addEventListener('submit', handleLogin);
    }
    
    // Add logout button handler
    const logoutButton = document.getElementById('logout-btn');
    
    if (logoutButton) {
        logoutButton.addEventListener('click', logout);
    }
    
    // Check authentication
    checkAuth();
} 