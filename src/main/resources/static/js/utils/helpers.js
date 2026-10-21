// Helper utilities
export function getAuthHeaders() {
    const credentials = localStorage.getItem('authCredentials');
    if (credentials) {
        return {
            'Authorization': 'Basic ' + credentials,
            'Content-Type': 'application/json'
        };
    }
    return {
        'Content-Type': 'application/json'
    };
}

export function getDefaultReturnDate() {
    const date = new Date();
    date.setDate(date.getDate() + 7);
    return date.toISOString().split('T')[0];
}

export function getCurrentUser() {
    const userStr = localStorage.getItem('currentUser');
    return userStr ? JSON.parse(userStr) : null;
}

// Turn an enum value like CHECKED_OUT into "Checked out"
export function formatLabel(value) {
    const text = String(value ?? '').toLowerCase().replaceAll('_', ' ');
    return text.charAt(0).toUpperCase() + text.slice(1);
}

// Pill badge for an equipment status (with dot)
export function statusPill(status) {
    const tone = { AVAILABLE: 'success', CHECKED_OUT: 'warning', IN_MAINTENANCE: 'info', RETIRED: 'muted' }[status] || 'muted';
    return `<span class="pill pill-${tone}">${formatLabel(status)}</span>`;
}

// Pill badge for an equipment condition (no dot)
export function conditionPill(condition) {
    const tone = { NEW: 'success', EXCELLENT: 'success', GOOD: 'info', FAIR: 'warning', POOR: 'danger', NEEDS_REPAIR: 'danger' }[condition] || 'muted';
    return `<span class="pill pill-plain pill-${tone}">${formatLabel(condition)}</span>`;
}

