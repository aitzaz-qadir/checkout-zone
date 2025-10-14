// Dashboard module - handles loading and displaying dashboard metrics
import { getAuthHeaders } from '../utils/helpers.js';

export async function loadDashboard() {
    try {
        // Load equipment data to calculate metrics
        const [equipmentResponse, requestsResponse] = await Promise.all([
            fetch('/api/equipment'),
            fetch('/api/checkout-requests', {
                headers: getAuthHeaders()
            }).catch(() => ({ json: () => [] })) // Handle case when not logged in
        ]);

        const equipment = await equipmentResponse.json();
        const requests = requestsResponse.json ? await requestsResponse.json() : [];

        // Calculate metrics
        const availableCount = equipment.filter(item => item.status === 'AVAILABLE').length;
        const checkedOutCount = equipment.filter(item => item.status === 'CHECKED_OUT').length;
        const pendingCount = requests.filter(req => req.status === 'PENDING').length;

        // Update dashboard display
        updateDashboardCounts(availableCount, checkedOutCount, pendingCount);

    } catch (error) {
        console.error('Error loading dashboard:', error);
        // Set default values if error occurs
        updateDashboardCounts(0, 0, 0);
    }
}

function updateDashboardCounts(available, checkedOut, pending) {
    const availableElement = document.getElementById('availableCount');
    const checkedOutElement = document.getElementById('checkedOutCount');
    const pendingElement = document.getElementById('pendingCount');

    if (availableElement) availableElement.textContent = available;
    if (checkedOutElement) checkedOutElement.textContent = checkedOut;
    if (pendingElement) pendingElement.textContent = pending;
}
