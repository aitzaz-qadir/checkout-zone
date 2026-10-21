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

// Set a dashboard counter's text; numbers count up from the current value
function setCount(id, value) {
    const element = document.getElementById(id);
    if (!element) return;
    if (typeof value !== 'number') {
        element.textContent = value;
        return;
    }
    const from = parseInt(element.textContent, 10) || 0;
    const start = performance.now();
    // Ease-out over 600ms: fast at first, slowing near the end
    const step = (now) => {
        const progress = Math.min((now - start) / 600, 1);
        element.textContent = Math.round(from + (value - from) * (1 - Math.pow(1 - progress, 3)));
        if (progress < 1) requestAnimationFrame(step);
    };
    requestAnimationFrame(step);
}
