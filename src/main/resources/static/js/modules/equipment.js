// Equipment module

let addEquipmentModal = null;
let currentViewMode = 'grid'; // Default to grid view
let currentEquipmentData = []; // Store equipment data for view switching
let allEquipmentData = []; // Store all equipment for filtering

export function setAddEquipmentModal(modal) {
    addEquipmentModal = modal;
}

// Equipment filtering by type
export function filterEquipmentByType() {
    const selectedType = document.getElementById('equipmentTypeFilter').value;
    const filteredEquipment = selectedType
        ? allEquipmentData.filter(item => item.type === selectedType)
        : allEquipmentData;

    currentEquipmentData = filteredEquipment;

    // Re-render with filtered data
    const currentUser = localStorage.getItem('currentUser') ? JSON.parse(localStorage.getItem('currentUser')) : null;
    if (currentViewMode === 'grid') {
        renderGridView(filteredEquipment, currentUser);
    } else {
        renderListView(filteredEquipment, currentUser);
    }
}

// Function to get equipment type icon
function getEquipmentIcon(type) {
    const iconMap = {
        'Laptop': 'bi-laptop',
        'Desktop': 'bi-pc-display',
        'Monitor': 'bi-display',
        'Camera': 'bi-camera',
        'Tablet': 'bi-tablet',
        'Phone': 'bi-phone',
        'Projector': 'bi-projector',
        'Other': 'bi-gear'
    };
    return iconMap[type] || 'bi-gear';
}

// Function to render equipment in grid view
function renderGridView(equipment, currentUser) {
    const equipmentList = document.getElementById('equipmentList');
    equipmentList.className = 'row equipment-grid-view';
    equipmentList.innerHTML = '';

    equipment.forEach(item => {
        const statusColor = item.status === 'AVAILABLE' ? 'success' :
        item.status === 'CHECKED_OUT' ? 'warning' : 'secondary';

        const icon = getEquipmentIcon(item.type);

        const card = `
            <div class="col-md-4">
                <div class="card equipment-card">
                    <div class="card-body">
                        <div class="d-flex align-items-center mb-2">
                            <div class="equipment-grid-icon">
                                <i class="bi ${icon}"></i>
                            </div>
                            <div class="flex-grow-1">
                                <h5 class="card-title mb-1">${item.name}</h5>
                                <h6 class="card-subtitle text-muted">${item.brand} ${item.model || ''}</h6>
                            </div>
                        </div>
                        <p class="card-text">
                            <span class="badge bg-${statusColor} status-badge">${item.status}</span>
                            <span class="badge bg-info status-badge">${item.condition}</span>
                        </p>
                        <p class="card-text">
                            <small class="text-muted">
                                <i class="bi bi-geo-alt"></i> ${item.location || 'N/A'}<br>
                                <i class="bi bi-tag"></i> ${item.type}
                            </small>
                        </p>
                        ${item.status === 'AVAILABLE' && currentUser ?
                            `<button class="btn btn-sm btn-primary" onclick='window.requestEquipment(${JSON.stringify(item)})'>
                                Request Checkout
                            </button>` : ''}
                    </div>
                </div>
            </div>
        `;
        equipmentList.innerHTML += card;
    });

    if (equipment.length === 0) {
        equipmentList.innerHTML = '<div class="col-12"><p class="text-center">No equipment available</p></div>';
    }
}

// Function to render equipment in list view
function renderListView(equipment, currentUser) {
    const equipmentList = document.getElementById('equipmentList');
    equipmentList.className = 'equipment-list-view';
    equipmentList.innerHTML = '';

    equipment.forEach(item => {
        const statusColor = item.status === 'AVAILABLE' ? 'success' :
        item.status === 'CHECKED_OUT' ? 'warning' : 'secondary';

        const icon = getEquipmentIcon(item.type);

        const listItem = `
            <div class="equipment-list-item">
                <div class="equipment-list-info">
                    <div class="equipment-list-icon">
                        <i class="bi ${icon}"></i>
                    </div>
                    <div class="equipment-list-details">
                        <div class="equipment-list-primary">
                            <div class="equipment-list-name">${item.name}</div>
                            <div class="equipment-list-meta">${item.brand} ${item.model || ''}</div>
                        </div>
                        <div class="equipment-list-secondary">
                            <div class="equipment-list-badges">
                                <span class="badge bg-${statusColor}">${item.status}</span>
                                <span class="badge bg-info">${item.condition}</span>
                                <span class="badge bg-secondary">${item.type}</span>
                            </div>
                            <div class="equipment-list-location">
                                <i class="bi bi-geo-alt"></i>
                                <span>${item.location || 'N/A'}</span>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="equipment-list-actions">
                    ${item.status === 'AVAILABLE' && currentUser ?
                        `<button class="btn btn-primary btn-sm" onclick='window.requestEquipment(${JSON.stringify(item)})'>
                            <i class="bi bi-plus-circle"></i> Request
                        </button>` : ''}
                </div>
            </div>
        `;
        equipmentList.innerHTML += listItem;
    });

    if (equipment.length === 0) {
        equipmentList.innerHTML = '<div class="text-center p-4"><p>No equipment available</p></div>';
    }
}

export async function loadEquipment(currentUser) {
    try {
        const response = await fetch('/api/equipment');
        const equipment = await response.json();

        // Store equipment data for view switching
        currentEquipmentData = equipment;
        allEquipmentData = equipment; // Store all equipment data for filtering

        // Render based on current view mode
        if (currentViewMode === 'grid') {
            renderGridView(equipment, currentUser);
        } else {
            renderListView(equipment, currentUser);
        }

    } catch (error) {
        console.error('Error loading equipment:', error);
        const equipmentList = document.getElementById('equipmentList');
        equipmentList.innerHTML = '<div class="col-12"><div class="alert alert-danger">Error loading equipment</div></div>';
    }
}

// Function to toggle between grid and list views
export function toggleView(viewMode) {
    currentViewMode = viewMode;

    // Update button states
    const gridBtn = document.getElementById('gridViewBtn');
    const listBtn = document.getElementById('listViewBtn');

    if (viewMode === 'grid') {
        gridBtn.classList.add('active');
        listBtn.classList.remove('active');
        document.body.classList.remove('list-view');
    } else {
        listBtn.classList.add('active');
        gridBtn.classList.remove('active');
        document.body.classList.add('list-view');
    }

    // Re-render equipment with current data
    const currentUser = localStorage.getItem('currentUser') ? JSON.parse(localStorage.getItem('currentUser')) : null;
    if (currentViewMode === 'grid') {
        renderGridView(currentEquipmentData, currentUser);
    } else {
        renderListView(currentEquipmentData, currentUser);
    }

    // Store preference in localStorage
    localStorage.setItem('equipmentViewMode', viewMode);
}

// Function to initialize view mode from localStorage
export function initializeViewMode() {
    const savedViewMode = localStorage.getItem('equipmentViewMode');
    if (savedViewMode && (savedViewMode === 'grid' || savedViewMode === 'list')) {
        currentViewMode = savedViewMode;
        toggleView(savedViewMode);
    }
}

export function showAddEquipmentModal() {
    // Clear form
    document.getElementById('addEquipmentForm').reset();
    document.getElementById('addEquipmentError').style.display = 'none';
    document.getElementById('addEquipmentSuccess').style.display = 'none';

    addEquipmentModal.show();
}

export async function submitAddEquipment(getAuthHeaders, callback) {
    // Get form values
    const equipment = {
        internalId: document.getElementById('equipInternalId').value,
        serialNumber: document.getElementById('equipSerialNumber').value,
        name: document.getElementById('equipName').value,
        type: document.getElementById('equipType').value,
        brand: document.getElementById('equipBrand').value,
        model: document.getElementById('equipModel').value,
        condition: document.getElementById('equipCondition').value,
        status: 'AVAILABLE',  // New equipment is always available
        location: document.getElementById('equipLocation').value,
        purchasePrice: document.getElementById('equipPurchasePrice').value || null,
        currentValue: document.getElementById('equipCurrentValue').value || null,
        acquisitionDate: document.getElementById('equipAcquisitionDate').value || null,
        warrantyExpiry: document.getElementById('equipWarrantyExpiry').value || null,
        notes: document.getElementById('equipNotes').value
    };

    // Basic validation
    if (!equipment.internalId || !equipment.name || !equipment.type || !equipment.condition) {
        alert('Please fill in all required fields (marked with *)');
        return;
    }

    try {
        const response = await fetch('/api/equipment', {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify(equipment)
        });

        if (response.ok) {
            document.getElementById('addEquipmentSuccess').textContent = 'Equipment added successfully!';
            document.getElementById('addEquipmentSuccess').style.display = 'block';
            document.getElementById('addEquipmentError').style.display = 'none';

            // Close modal and refresh after 2 seconds
            setTimeout(() => {
                addEquipmentModal.hide();
                window.showEquipment();
                if (callback) callback(); // Call the callback if provided
            }, 2000);
        } else {
            const error = await response.json();
            document.getElementById('addEquipmentError').textContent = error.error || 'Failed to add equipment';
            document.getElementById('addEquipmentError').style.display = 'block';
            document.getElementById('addEquipmentSuccess').style.display = 'none';
        }
    } catch (error) {
        console.error('Error adding equipment:', error);
        document.getElementById('addEquipmentError').textContent = 'Failed to add equipment';
        document.getElementById('addEquipmentError').style.display = 'block';
    }
}
