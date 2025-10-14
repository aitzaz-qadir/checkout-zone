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

