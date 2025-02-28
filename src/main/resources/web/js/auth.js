/**
 * Authentication Functions for FrizzlenMod Admin Panel
 */

/**
 * Gets the authentication token from local storage
 * 
 * @returns {string|null} The auth token or null if not found
 */
function getToken() {
    return localStorage.getItem('token');
}

/**
 * Handles the login form submission
 * 
 * @param {Event} event The form submit event
 * @returns {boolean} False to prevent default form submission
 */
async function handleLogin(event) {
    event.preventDefault();
    console.log("Login form submitted"); // Debug log
    
    // Get form data
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    
    console.log("Login attempt with username:", username); // Debug log
    
    // Validate form data
    if (!username || !password) {
        showError('Please enter both username and password');
        return false;
    }
    
    try {
        // Show loading indicator
        showLoading(true);
        
        // Clear previous error
        const loginError = document.getElementById('login-error');
        if (loginError) {
            loginError.classList.add('d-none');
            loginError.textContent = '';
        }
        
        // Send login request
        console.log("Sending login request to:", API.auth.login()); // Debug log
        const response = await apiPost(API.auth.login(), { username, password });
        
        console.log('Login response:', response); // Debug log
        
        // Check if login was successful - handle all possible response formats
        if (response) {
            // Check for direct error in response 
            if (!response.success && response.error) {
                showError(response.error || 'Login failed. Please check your credentials.');
                return false;
            }
            
            // Get token - handle different response formats
            let token = null;
            let role = 'ADMIN'; // Default role
            
            if (response.token) {
                // Format 1: Direct token in response
                token = response.token;
                role = response.role || role;
            } else if (response.success && response.data && response.data.token) {
                // Format 2: Wrapped in data object
                token = response.data.token;
                role = response.data.role || role;
            }
            
            if (token) {
                // Store auth data
                localStorage.setItem('token', token);
                localStorage.setItem('username', username);
                localStorage.setItem('role', role);
                
                console.log("Login successful, token stored");
                
                // Hide login form and show admin panel
                const loginContainer = document.getElementById('login-container');
                const adminPanel = document.getElementById('admin-panel');
                
                if (loginContainer) {
                    loginContainer.classList.add('d-none');
                    loginContainer.style.display = 'none';
                }
                
                if (adminPanel) {
                    adminPanel.classList.remove('d-none');
                    adminPanel.style.display = 'block';
                }
                
                // Initialize the admin panel
                if (typeof initAdminPanel === 'function') {
                    initAdminPanel();
                } else {
                    console.error('initAdminPanel function not found');
                }
                
                // Update user info
                updateUserInfo();
                
                // Redirect to make sure the page refreshes properly
                // Use a small timeout to ensure local storage is updated
                setTimeout(() => {
                    window.location.href = 'redirect.html';
                }, 100);
                
                return true;
            } else {
                showError('Login failed: Invalid server response (missing token)');
            }
        } else {
            showError('Login failed: No response from server');
        }
    } catch (error) {
        console.error('Login error:', error); // Debug log
        showError('Login failed: ' + (error.message || 'Unknown error'));
    } finally {
        // Hide loading indicator
        showLoading(false);
    }
    
    return false;
}

/**
 * Shows an error message specific to the login form
 * 
 * @param {string} message The error message to show
 */
function showError(message) {
    console.error('Login error:', message);
    
    // First try to find login-error element
    const loginError = document.getElementById('login-error');
    if (loginError) {
        loginError.textContent = message;
        loginError.classList.remove('d-none');
        
        // Auto-hide after 5 seconds
        setTimeout(() => {
            loginError.classList.add('d-none');
        }, 5000);
    } else {
        // Fallback to alert if login-error element not found
        alert(message);
    }
}

/**
 * Checks if the user is authenticated and shows the appropriate screen
 */
function checkAuth() {
    console.log("Checking authentication status");
    const token = localStorage.getItem('token');
    
    if (token) {
        console.log("Token found, showing admin panel");
        // Hide login form and show admin panel
        const loginContainer = document.getElementById('login-container');
        const adminPanel = document.getElementById('admin-panel');
        
        if (loginContainer) {
            loginContainer.classList.add('d-none');
            loginContainer.setAttribute('style', 'display: none !important');
        }
        
        if (adminPanel) {
            adminPanel.classList.remove('d-none');
            adminPanel.setAttribute('style', 'display: block !important');
            
            // Force display of content sections
            const contentSections = document.querySelectorAll('.content-section');
            contentSections.forEach(section => {
                if (section.id === 'dashboard-section') {
                    section.style.display = 'block';
                } else {
                    section.style.display = 'none';
                }
            });
        }
        
        // Initialize the admin panel
        initAdminPanel();
        
        // Update user info
        updateUserInfo();
    } else {
        console.log("No token found, showing login form");
        // Show login form and hide admin panel
        const loginContainer = document.getElementById('login-container');
        const adminPanel = document.getElementById('admin-panel');
        
        if (loginContainer) {
            loginContainer.classList.remove('d-none');
            loginContainer.setAttribute('style', 'display: flex !important');
        }
        
        if (adminPanel) {
            adminPanel.classList.add('d-none');
            adminPanel.setAttribute('style', 'display: none !important');
        }
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
    const loginContainer = document.getElementById('login-container');
    const adminPanel = document.getElementById('admin-panel');
    
    if (loginContainer) {
        loginContainer.style.display = 'flex';
    }
    
    if (adminPanel) {
        adminPanel.classList.add('d-none');
        adminPanel.style.display = 'none';
    }
    
    // Reload the page to ensure a clean state
    window.location.href = 'index.html';
}

/**
 * Updates the user info in the navbar
 */
function updateUserInfo() {
    const username = localStorage.getItem('username');
    const role = localStorage.getItem('role');
    
    if (username) {
        // Check if elements exist before trying to update them
        const userNameElement = document.getElementById('user-name');
        const userRoleElement = document.getElementById('user-role');
        
        if (userNameElement) {
            userNameElement.textContent = username;
        } else {
            console.warn('Element with id "user-name" not found');
        }
        
        if (userRoleElement) {
            userRoleElement.textContent = role || 'User';
        } else {
            console.warn('Element with id "user-role" not found');
        }
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
        if (show) {
            loadingElement.classList.remove('d-none');
            loadingElement.style.display = 'block';
        } else {
            loadingElement.classList.add('d-none');
            loadingElement.style.display = 'none';
        }
    } else {
        console.warn("Loading element not found");
    }
}

/**
 * Initializes the authentication system
 */
function initAuth() {
    console.log("Initializing auth system");
    
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
    
    // Check if we came from the redirect page
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.has('cache')) {
        console.log("Detected redirect with cache parameter, checking authentication");
    }
    
    // Check authentication status
    checkAuth();
    
    console.log("Auth system initialized");
} 