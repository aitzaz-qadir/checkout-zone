// Requests module

let requestModal = null;
let selectedEquipment = null;

export function setRequestModal(modal) {
    requestModal = modal;
}

export function requestEquipment(equipment, currentUser, showLogin) {
    if (!currentUser) {
        alert('Please login first');
        showLogin();
        return;
    }

    selectedEquipment = equipment;

    // Populate modal
    document.getElementById('requestEquipmentId').value = equipment.id;
    document.getElementById('requestEquipmentName').value = equipment.name;
    document.getElementById('requestPurpose').value = '';

    // Set default needed by date (7 days from now)
    const neededBy = new Date();
    neededBy.setDate(neededBy.getDate() + 7);
    document.getElementById('requestNeededBy').value = neededBy.toISOString().split('T')[0];

    // Hide any previous messages
    document.getElementById('requestError').style.display = 'none';
    document.getElementById('requestSuccess').style.display = 'none';

    // Show modal
    requestModal.show();
}

export async function submitRequest(currentUser, getAuthHeaders, loadEquipment) {
    const purpose = document.getElementById('requestPurpose').value;
    const neededBy = document.getElementById('requestNeededBy').value;

    if (!purpose || !neededBy) {
        alert('Please fill in all fields');
        return;
    }

    try {
        const response = await fetch('/api/checkout/request', {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify({
                userId: currentUser.id,
                equipmentIds: [selectedEquipment.id],
                purpose: purpose,
                neededByDate: neededBy
            })
        });

        if (response.ok) {
            document.getElementById('requestSuccess').textContent = 'Request submitted successfully!';
            document.getElementById('requestSuccess').style.display = 'block';
            document.getElementById('requestError').style.display = 'none';

            // Close modal after 2 seconds
            setTimeout(() => {
                requestModal.hide();
                loadEquipment();
            }, 2000);
        } else {
            const error = await response.json();
            document.getElementById('requestError').textContent = error.error || 'Failed to submit request';
            document.getElementById('requestError').style.display = 'block';
            document.getElementById('requestSuccess').style.display = 'none';
        }
    } catch (error) {
        console.error('Error submitting request:', error);
        document.getElementById('requestError').textContent = 'Failed to submit request';
        document.getElementById('requestError').style.display = 'block';
    }
}

export async function loadMyRequests(currentUser, getAuthHeaders) {
    try {
        const response = await fetch(`/api/checkout/requests/user/${currentUser.id}`, {
            headers: getAuthHeaders()
        });

        const requests = await response.json();
        const requestsList = document.getElementById('requestsList');
        requestsList.innerHTML = '';

        if (requests.length === 0) {
            requestsList.innerHTML = '<p class="text-center">No requests found</p>';
            return;
        }

        requests.forEach(request => {
            const statusColor = request.status === 'PENDING' ? 'warning' :
            request.status === 'APPROVED' ? 'success' :
            request.status === 'REJECTED' ? 'danger' : 'secondary';

            const equipmentNames = request.equipmentItems.map(e => e.name).join(', ');

            const card = `
                <div class="card mb-3">
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-start">
                            <div>
                                <h5 class="card-title">Request #${request.id}</h5>
                                <p class="card-text">
                                    <strong>Equipment:</strong> ${equipmentNames}<br>
                                    <strong>Purpose:</strong> ${request.purpose}<br>
                                    <strong>Needed By:</strong> ${request.neededByDate || 'N/A'}<br>
                                    <strong>Requested:</strong> ${request.requestedDate}
                                </p>
                            </div>
                            <span class="badge bg-${statusColor}">${request.status}</span>
                        </div>
                        ${request.approvalNotes ? `<p class="card-text"><small class="text-muted">Notes: ${request.approvalNotes}</small></p>` : ''}
                    </div>
                </div>
            `;
            requestsList.innerHTML += card;
        });
    } catch (error) {
        console.error('Error loading requests:', error);
        document.getElementById('requestsList').innerHTML = '<p class="text-danger">Failed to load requests</p>';
    }
}

