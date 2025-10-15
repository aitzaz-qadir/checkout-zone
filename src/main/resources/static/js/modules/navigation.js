// Navigation module
import * as Dashboard from './dashboard.js';

export function showLogin() {
    hideAllSections();
    document.getElementById('loginSection').style.display = 'block';
}

export function showRegister() {
    hideAllSections();
    document.getElementById('registerSection').style.display = 'block';
}

export function showEquipment() {
    hideAllSections();
    document.getElementById('equipmentSection').style.display = 'block';
    // Refresh dashboard when showing equipment
    Dashboard.loadDashboard();
}

export function showMyRequests(currentUser) {
    if (!currentUser) {
        alert('Please login first');
        showLogin();
        return;
    }
    hideAllSections();
    document.getElementById('requestsSection').style.display = 'block';
}

export function showPendingRequests(currentUser) {
    if (!currentUser) {
        alert('Please login first');
        showLogin();
        return;
    }
    hideAllSections();
    document.getElementById('approvalsSection').style.display = 'block';
}

export function showCheckedOut(currentUser) {
    if (!currentUser) {
        alert('Please login first');
        showLogin();
        return;
    }
    hideAllSections();
    document.getElementById('checkedOutSection').style.display = 'block';
}

export function hideAllSections() {
    document.getElementById('loginSection').style.display = 'none';
    document.getElementById('registerSection').style.display = 'none';
    document.getElementById('equipmentSection').style.display = 'none';
    document.getElementById('requestsSection').style.display = 'none';
    document.getElementById('approvalsSection').style.display = 'none';
    document.getElementById('checkedOutSection').style.display = 'none';
}
