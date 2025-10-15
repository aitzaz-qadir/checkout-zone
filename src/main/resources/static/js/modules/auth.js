// Authentication module
import { getAuthHeaders } from '../utils/helpers.js';

let currentUser = null;

export function getCurrentUser() {
    return currentUser;
}

export function setCurrentUser(user) {
    currentUser = user;
}

export async function login() {
    const username = document.getElementById('loginUsername').value;
    const password = document.getElementById('loginPassword').value;

    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ username, password })
        });

        if (response.ok) {
            const data = await response.json();
            currentUser = data;

            // Store credentials for future requests (Basic Auth)
            const credentials = btoa(username + ':' + password);
            localStorage.setItem('authCredentials', credentials);
            localStorage.setItem('currentUser', JSON.stringify(data));

            // Update UI
            document.getElementById('loginBtn').style.display = 'none';
            document.getElementById('userInfo').style.display = 'block';
            document.getElementById('logoutBtn').style.display = 'block';
            document.getElementById('userName').textContent = data.username;

            // Show My Requests button when user is logged in
            document.getElementById('myRequestsNav').style.display = 'block';

            // Show manager nav if applicable
            if (data.role === 'EQUIPMENT_MANAGER' || data.role === 'ADMIN') {
                document.getElementById('managerNav').style.display = 'block';
                document.getElementById('managerNav2').style.display = 'block';
                document.getElementById('addEquipmentBtn').style.display = 'block';
            }

            return data;
        } else {
            document.getElementById('loginError').textContent = 'Invalid username or password';
            document.getElementById('loginError').style.display = 'block';
            return null;
        }
    } catch (error) {
        console.error('Login error:', error);
        alert('Login failed. Please try again.');
        return null;
    }
}

export function logout() {
    currentUser = null;
    localStorage.removeItem('authCredentials');
    localStorage.removeItem('currentUser');

    document.getElementById('loginBtn').style.display = 'block';
    document.getElementById('userInfo').style.display = 'none';
    document.getElementById('logoutBtn').style.display = 'none';
    document.getElementById('managerNav').style.display = 'none';
    document.getElementById('managerNav2').style.display = 'none';
    document.getElementById('addEquipmentBtn').style.display = 'none';

    // Hide My Requests button on logout
    document.getElementById('myRequestsNav').style.display = 'none';
}

// Initialize auth state on page load
export function initializeAuth() {
    const storedUser = localStorage.getItem('currentUser');
    if (storedUser) {
        try {
            const userData = JSON.parse(storedUser);
            currentUser = userData;

            // Update UI for logged-in user
            document.getElementById('loginBtn').style.display = 'none';
            document.getElementById('userInfo').style.display = 'block';
            document.getElementById('logoutBtn').style.display = 'block';
            document.getElementById('userName').textContent = userData.username;
            document.getElementById('myRequestsNav').style.display = 'block';

            // Show manager nav if applicable
            if (userData.role === 'EQUIPMENT_MANAGER' || userData.role === 'ADMIN') {
                document.getElementById('managerNav').style.display = 'block';
                document.getElementById('managerNav2').style.display = 'block';
                document.getElementById('addEquipmentBtn').style.display = 'block';
            }
        } catch (error) {
            console.error('Error parsing stored user data:', error);
            // Clear invalid data
            localStorage.removeItem('currentUser');
            localStorage.removeItem('authCredentials');
        }
    }
}
