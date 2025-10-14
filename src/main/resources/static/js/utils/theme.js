// Theme management utility
class ThemeManager {
    constructor() {
        this.currentTheme = this.getStoredTheme() || 'light';
        this.initTheme();
    }

    initTheme() {
        // Apply the stored theme on page load
        this.applyTheme(this.currentTheme);
        this.updateToggleButton();
    }

    getStoredTheme() {
        return localStorage.getItem('checkout-zone-theme');
    }

    storeTheme(theme) {
        localStorage.setItem('checkout-zone-theme', theme);
    }

    applyTheme(theme) {
        const html = document.documentElement;

        if (theme === 'dark') {
            html.setAttribute('data-theme', 'dark');
        } else {
            html.removeAttribute('data-theme');
        }

        this.currentTheme = theme;
        this.storeTheme(theme);
        this.updateToggleButton();
    }

    toggleTheme() {
        const newTheme = this.currentTheme === 'light' ? 'dark' : 'light';
        this.applyTheme(newTheme);
    }

    updateToggleButton() {
        const toggleButton = document.getElementById('themeToggle');
        const icon = toggleButton?.querySelector('i');

        if (icon) {
            if (this.currentTheme === 'dark') {
                icon.className = 'bi bi-sun-fill';
                toggleButton.setAttribute('title', 'Switch to light mode');
            } else {
                icon.className = 'bi bi-moon-fill';
                toggleButton.setAttribute('title', 'Switch to dark mode');
            }
        }
    }

    getCurrentTheme() {
        return this.currentTheme;
    }
}

// Create global theme manager instance
window.themeManager = new ThemeManager();

// Global function for the onclick handler
window.toggleTheme = function() {
    window.themeManager.toggleTheme();
};

export default ThemeManager;
