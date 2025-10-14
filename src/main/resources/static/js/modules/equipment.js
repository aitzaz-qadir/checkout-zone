// Equipment module

let addEquipmentModal = null;

export function setAddEquipmentModal(modal) {
    addEquipmentModal = modal;
}

export async function loadEquipment(currentUser) {
    try {
        const response = await fetch('/api/equipment');
        const equipment = await response.json();

        const equipmentList = document.getElementById('equipmentList');
        equipmentList.innerHTML = '';

        equipment.forEach(item => {
            const statusColor = item.status === 'AVAILABLE' ? 'success' :
            item.status === 'CHECKED_OUT' ? 'warning' : 'secondary';

            const card = `
                <div class="col-md-4">
                    <div class="card equipment-card">
                        <div class="card-body">
                            <h5 class="card-title">${item.name}</h5>
                            <h6 class="card-subtitle mb-2 text-muted">${item.brand} ${item.model || ''}</h6>
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
    } catch (error) {
        console.error('Error loading equipment:', error);
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
