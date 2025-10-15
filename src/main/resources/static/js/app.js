// Main application file - orchestrates all modules
import * as Auth from './modules/auth.js';
import * as Navigation from './modules/navigation.js';
import * as Equipment from './modules/equipment.js';
import * as Requests from './modules/requests.js';
import * as Approvals from './modules/approvals.js';
import * as Returns from './modules/returns.js';
import * as Dashboard from './modules/dashboard.js';
import { getAuthHeaders, getDefaultReturnDate } from './utils/helpers.js';
import ThemeManager from './utils/theme.js';

// Global state
let currentUser = null;
let returnModal = null;
let requestModal = null;
let addEquipmentModal = null;

// On page load
document.addEventListener('DOMContentLoaded', function() {
    // Initialize authentication state first
    Auth.initializeAuth();

    // Load dashboard and equipment on initial page load
    Dashboard.loadDashboard();
    Equipment.loadEquipment(currentUser);

    // Initialize equipment view mode from localStorage
    Equipment.initializeViewMode();

    // Initialize modals only if they exist
    const requestModalElement = document.getElementById('requestModal');
    const returnModalElement = document.getElementById('returnModal');
    const addEquipmentModalElement = document.getElementById('addEquipmentModal');

    if (requestModalElement) {
        requestModal = new bootstrap.Modal(requestModalElement);
        Requests.setRequestModal(requestModal);
    }
    if (returnModalElement) {
        returnModal = new bootstrap.Modal(returnModalElement);
        Returns.setReturnModal(returnModal);
    }
    if (addEquipmentModalElement) {
        addEquipmentModal = new bootstrap.Modal(addEquipmentModalElement);
        Equipment.setAddEquipmentModal(addEquipmentModal);
    }

    // Setup login form
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', function(e) {
            e.preventDefault();
            handleLogin();
        });
    }

    // Setup registration form
    const registerForm = document.getElementById('registerForm');
    if (registerForm) {
        registerForm.addEventListener('submit', function(e) {
            e.preventDefault();
            handleRegister();
        });
    }

    // Set minimum date for needed by date (today)
    const neededByInput = document.getElementById('requestNeededBy');
    if (neededByInput) {
        const today = new Date().toISOString().split('T')[0];
        neededByInput.setAttribute('min', today);
    }
});

// Login handler
async function handleLogin() {
    const user = await Auth.login();
    if (user) {
        currentUser = user;
        Auth.setCurrentUser(user);
        showEquipment();
    }
}

// Registration handler
async function handleRegister() {
    const user = await Auth.register();
    if (user) {
        currentUser = user;
        Auth.setCurrentUser(user);
        showEquipment();
    }
}

// Navigation functions - exposed globally
window.showLogin = function() {
    Navigation.showLogin();
};

window.showRegister = function() {
    Navigation.showRegister();
};

window.showEquipment = function() {
    Navigation.showEquipment();
    Equipment.loadEquipment(currentUser);
};

window.showMyRequests = function() {
    Navigation.showMyRequests(currentUser);
    if (currentUser) {
        Requests.loadMyRequests(currentUser, getAuthHeaders);
    }
};

window.showPendingRequests = function() {
    Navigation.showPendingRequests(currentUser);
    if (currentUser) {
        Approvals.loadPendingApprovals(getAuthHeaders, getDefaultReturnDate);
    }
};

window.showCheckedOut = function() {
    Navigation.showCheckedOut(currentUser);
    if (currentUser) {
        Returns.loadCheckedOut(getAuthHeaders);
    }
};

window.logout = function() {
    Auth.logout();
    currentUser = null;
    showEquipment();
};

// Equipment functions - exposed globally
window.requestEquipment = function(equipment) {
    Requests.requestEquipment(equipment, currentUser, Navigation.showLogin);
};

window.submitRequest = function() {
    Requests.submitRequest(currentUser, getAuthHeaders, () => {
        Equipment.loadEquipment(currentUser);
        Dashboard.loadDashboard(); // Refresh dashboard after new request
    });
};

window.showAddEquipmentModal = function() {
    Equipment.showAddEquipmentModal();
};

window.submitAddEquipment = function() {
    Equipment.submitAddEquipment(getAuthHeaders, () => {
        Dashboard.loadDashboard(); // Refresh dashboard after adding equipment
    });
};

// Equipment filtering function - exposed globally
window.filterEquipmentByType = function() {
    Equipment.filterEquipmentByType();
};

// View toggle function - exposed globally
window.toggleView = function(viewMode) {
    Equipment.toggleView(viewMode);
};

// Approvals functions - exposed globally
window.approveRequest = function(requestId) {
    Approvals.approveRequest(requestId, currentUser, getAuthHeaders, () => {
        Approvals.loadPendingApprovals(getAuthHeaders, getDefaultReturnDate);
        Dashboard.loadDashboard(); // Refresh dashboard after approval
    });
};

window.rejectRequest = function(requestId) {
    Approvals.rejectRequest(requestId, currentUser, getAuthHeaders, () => {
        Approvals.loadPendingApprovals(getAuthHeaders, getDefaultReturnDate);
        Dashboard.loadDashboard(); // Refresh dashboard after rejection
    });
};

window.fulfillRequest = function(requestId) {
    Approvals.fulfillRequest(requestId, currentUser, getAuthHeaders, () => {
        Approvals.loadPendingApprovals(getAuthHeaders, getDefaultReturnDate);
        Dashboard.loadDashboard(); // Refresh dashboard after fulfillment
    });
};

// Returns functions - exposed globally
window.showReturnModal = function(recordId, equipmentName) {
    Returns.showReturnModal(recordId, equipmentName);
};

window.submitReturn = function() {
    Returns.submitReturn(currentUser, getAuthHeaders, () => Returns.loadCheckedOut(getAuthHeaders), () => {
        Equipment.loadEquipment(currentUser);
        Dashboard.loadDashboard(); // Refresh dashboard after return
    });
};

// Theme toggle function is handled by the ThemeManager utility - no need to redefine it here