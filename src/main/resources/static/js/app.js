// Main application file - orchestrates all modules
import * as Auth from './modules/auth.js';
import * as Navigation from './modules/navigation.js';
import * as Equipment from './modules/equipment.js';
import * as Requests from './modules/requests.js';
import * as Approvals from './modules/approvals.js';
import * as Returns from './modules/returns.js';
import { getAuthHeaders, getDefaultReturnDate } from './utils/helpers.js';
import ThemeManager from './utils/theme.js';

// Global state
let currentUser = null;
let returnModal = null;
let requestModal = null;
let addEquipmentModal = null;

// On page load
document.addEventListener('DOMContentLoaded', function() {
    // Load equipment on initial page load
    Equipment.loadEquipment(currentUser);

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

// Navigation functions - exposed globally
window.showLogin = function() {
    Navigation.showLogin();
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
    Requests.submitRequest(currentUser, getAuthHeaders, () => Equipment.loadEquipment(currentUser));
};

window.showAddEquipmentModal = function() {
    Equipment.showAddEquipmentModal();
};

window.submitAddEquipment = function() {
    Equipment.submitAddEquipment(getAuthHeaders);
};

// Approvals functions - exposed globally
window.approveRequest = function(requestId) {
    Approvals.approveRequest(requestId, currentUser, getAuthHeaders, () => Approvals.loadPendingApprovals(getAuthHeaders, getDefaultReturnDate));
};

window.rejectRequest = function(requestId) {
    Approvals.rejectRequest(requestId, currentUser, getAuthHeaders, () => Approvals.loadPendingApprovals(getAuthHeaders, getDefaultReturnDate));
};

window.fulfillRequest = function(requestId) {
    Approvals.fulfillRequest(requestId, currentUser, getAuthHeaders, () => Approvals.loadPendingApprovals(getAuthHeaders, getDefaultReturnDate));
};

// Returns functions - exposed globally
window.showReturnModal = function(recordId, equipmentName) {
    Returns.showReturnModal(recordId, equipmentName);
};

window.submitReturn = function() {
    Returns.submitReturn(currentUser, getAuthHeaders, () => Returns.loadCheckedOut(getAuthHeaders), () => Equipment.loadEquipment(currentUser));
};
