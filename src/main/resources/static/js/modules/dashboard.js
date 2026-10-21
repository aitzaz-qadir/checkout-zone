// Dashboard module - handles loading and displaying dashboard metrics
import { getAuthHeaders, getCurrentUser } from '../utils/helpers.js';

export async function loadDashboard() {
    // Equipment counts (public)
    try {
        const response = await fetch('/api/equipment');
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        const equipment = await response.json();
        setCount('availableCount', equipment.filter(item => item.status === 'AVAILABLE').length);
        setCount('checkedOutCount', equipment.filter(item => item.status === 'CHECKED_OUT').length);
    } catch (error) {
        console.error('Error loading equipment counts:', error);
        setCount('availableCount', '—');
        setCount('checkedOutCount', '—');
    }

    // Pending count (managers and admins only)
    const user = getCurrentUser();
    if (!user || (user.role !== 'EQUIPMENT_MANAGER' && user.role !== 'ADMIN')) {
        setCount('pendingCount', '—');
        return;
    }
    try {
        const response = await fetch('/api/checkout/requests/pending', { headers: getAuthHeaders() });
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        const pending = await response.json();
        setCount('pendingCount', pending.length);
    } catch (error) {
        console.error('Error loading pending count:', error);
        setCount('pendingCount', '—');
    }
}

// Set a dashboard counter's text
function setCount(id, value) {
    const element = document.getElementById(id);
    if (element) element.textContent = value;
}
