// Global variables
let currentUser = null;
let requestModal = null;
let selectedEquipment = null;

// On page load
document.addEventListener('DOMContentLoaded', function() {
    loadEquipment();

    // Initialize modal
    requestModal = new bootstrap.Modal(document.getElementById('requestModal'));

    // Setup login form
    document.getElementById('loginForm').addEventListener('submit', function(e) {
        e.preventDefault();
        login();
    });

    // Set minimum date for needed by date (today)
    const today = new Date().toISOString().split('T')[0];
    document.getElementById('requestNeededBy').setAttribute('min', today);
});

// Show/Hide sections
function showLogin() {
    hideAllSections();
    document.getElementById('loginSection').style.display = 'block';
}

function showEquipment() {
    hideAllSections();
    document.getElementById('equipmentSection').style.display = 'block';
    loadEquipment();
}

function showMyRequests() {
    if (!currentUser) {
        alert('Please login first');
        showLogin();
        return;
    }
    hideAllSections();
    document.getElementById('requestsSection').style.display = 'block';
    loadMyRequests();
}

function showPendingRequests() {
    if (!currentUser) {
        alert('Please login first');
        showLogin();
        return;
    }
    hideAllSections();
    document.getElementById('approvalsSection').style.display = 'block';
    loadPendingApprovals();
}

function hideAllSections() {
    document.getElementById('loginSection').style.display = 'none';
    document.getElementById('equipmentSection').style.display = 'none';
    document.getElementById('requestsSection').style.display = 'none';
    document.getElementById('approvalsSection').style.display = 'none';
}

// Login function
async function login() {
    const username = document.getElementById('loginUsername').value;
    const password = document.getElementById('loginPassword').value;

    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ username, password })
        });

        if (response.ok) {
            const data = await response.json();
            currentUser = data;

            // Store credentials for future requests (Basic Auth)
            const credentials = btoa(username + ':' + password);
            localStorage.setItem('authCredentials', credentials);
            localStorage.setItem('currentUser', JSON.stringify(data));

            // Update UI
            document.getElementById('loginBtn').style.display = 'none';
            document.getElementById('userInfo').style.display = 'block';
            document.getElementById('logoutBtn').style.display = 'block';
            document.getElementById('userName').textContent = data.username;

            // Show manager nav if applicable
            if (data.role === 'EQUIPMENT_MANAGER' || data.role === 'ADMIN') {
                document.getElementById('managerNav').style.display = 'block';
                document.getElementById('managerNav2').style.display = 'block';
                document.getElementById('addEquipmentBtn').style.display = 'block';
            }

            showEquipment();
        } else {
            document.getElementById('loginError').textContent = 'Invalid username or password';
            document.getElementById('loginError').style.display = 'block';
        }
    } catch (error) {
        console.error('Login error:', error);
        alert('Login failed. Please try again.');
    }
}

// Logout function
function logout() {
    currentUser = null;
    localStorage.removeItem('authCredentials');
    localStorage.removeItem('currentUser');

    document.getElementById('loginBtn').style.display = 'block';
    document.getElementById('userInfo').style.display = 'none';
    document.getElementById('logoutBtn').style.display = 'none';
    document.getElementById('managerNav').style.display = 'none';
    document.getElementById('managerNav2').style.display = 'none';
    document.getElementById('addEquipmentBtn').style.display = 'none';

    showEquipment();
}

// Load equipment
async function loadEquipment() {
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
                                `<button class="btn btn-sm btn-primary" onclick='requestEquipment(${JSON.stringify(item)})'>
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

// Request equipment
function requestEquipment(equipment) {
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

// Submit request
async function submitRequest() {
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

// Load my requests
async function loadMyRequests() {
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

// Load pending approvals (for managers)
async function loadPendingApprovals() {
    try {
        // Load both pending AND approved requests
        const response = await fetch('/api/checkout/requests', {
            headers: getAuthHeaders()
        });

        const allRequests = await response.json();

        // Filter for pending and approved requests
        const pendingRequests = allRequests.filter(r => r.status === 'PENDING');
        const approvedRequests = allRequests.filter(r => r.status === 'APPROVED');

        const approvalsList = document.getElementById('approvalsList');
        approvalsList.innerHTML = '';

        // Show Pending Requests
        if (pendingRequests.length > 0) {
            approvalsList.innerHTML += '<h4 class="mt-3">Pending Approval</h4>';

            pendingRequests.forEach(request => {
                const equipmentNames = request.equipmentItems.map(e => e.name).join(', ');

                const card = `
                    <div class="card mb-3">
                        <div class="card-body">
                            <div class="d-flex justify-content-between align-items-start">
                                <div>
                                    <h5 class="card-title">Request #${request.id}</h5>
                                    <p class="card-text">
                                        <strong>Requested by:</strong> ${request.requestedBy.firstName} ${request.requestedBy.lastName} (${request.requestedBy.username})<br>
                                        <strong>Department:</strong> ${request.requestedBy.department || 'N/A'}<br>
                                        <strong>Equipment:</strong> ${equipmentNames}<br>
                                        <strong>Purpose:</strong> ${request.purpose}<br>
                                        <strong>Needed By:</strong> ${request.neededByDate || 'N/A'}<br>
                                        <strong>Requested:</strong> ${request.requestedDate}
                                    </p>
                                    <div class="mt-3">
                                        <textarea class="form-control mb-2" id="notes-${request.id}"
                                            placeholder="Add approval/rejection notes (optional)" rows="2"></textarea>
                                        <button class="btn btn-success btn-sm me-2" onclick="approveRequest(${request.id})">
                                            <i class="bi bi-check-circle"></i> Approve
                                        </button>
                                        <button class="btn btn-danger btn-sm" onclick="rejectRequest(${request.id})">
                                            <i class="bi bi-x-circle"></i> Reject
                                        </button>
                                    </div>
                                </div>
                                <span class="badge bg-warning">PENDING</span>
                            </div>
                        </div>
                    </div>
                `;
                approvalsList.innerHTML += card;
            });
        }

        // Show Approved Requests (ready to fulfill)
        if (approvedRequests.length > 0) {
            approvalsList.innerHTML += '<h4 class="mt-4">Ready to Fulfill</h4>';

            approvedRequests.forEach(request => {
                const equipmentNames = request.equipmentItems.map(e => e.name).join(', ');

                const card = `
                    <div class="card mb-3 border-success">
                        <div class="card-body">
                            <div class="d-flex justify-content-between align-items-start">
                                <div>
                                    <h5 class="card-title">Request #${request.id}</h5>
                                    <p class="card-text">
                                        <strong>Requested by:</strong> ${request.requestedBy.firstName} ${request.requestedBy.lastName}<br>
                                        <strong>Equipment:</strong> ${equipmentNames}<br>
                                        <strong>Purpose:</strong> ${request.purpose}<br>
                                        <strong>Approved by:</strong> ${request.approvedBy ? request.approvedBy.username : 'N/A'}<br>
                                        ${request.approvalNotes ? `<strong>Notes:</strong> ${request.approvalNotes}<br>` : ''}
                                    </p>
                                    <div class="mt-3">
                                        <label class="form-label">Expected Return Date:</label>
                                        <input type="date" class="form-control mb-2" id="returnDate-${request.id}"
                                            value="${getDefaultReturnDate()}" min="${new Date().toISOString().split('T')[0]}">
                                        <button class="btn btn-primary btn-sm" onclick="fulfillRequest(${request.id})">
                                            <i class="bi bi-box-arrow-right"></i> Hand Out Equipment
                                        </button>
                                    </div>
                                </div>
                                <span class="badge bg-success">APPROVED</span>
                            </div>
                        </div>
                    </div>
                `;
                approvalsList.innerHTML += card;
            });
        }

        if (pendingRequests.length === 0 && approvedRequests.length === 0) {
            approvalsList.innerHTML = '<p class="text-center">No pending or approved requests</p>';
        }

    } catch (error) {
        console.error('Error loading pending approvals:', error);
        document.getElementById('approvalsList').innerHTML = '<p class="text-danger">Failed to load requests</p>';
    }
}

// Helper function to get default return date (7 days from now)
function getDefaultReturnDate() {
    const date = new Date();
    date.setDate(date.getDate() + 7);
    return date.toISOString().split('T')[0];
}

// Fulfill request (hand out equipment)
async function fulfillRequest(requestId) {
    const returnDate = document.getElementById(`returnDate-${requestId}`).value;

    if (!returnDate) {
        alert('Please select an expected return date');
        return;
    }

    if (!confirm('Are you sure you want to hand out this equipment?')) {
        return;
    }

    try {
        const response = await fetch(`/api/checkout/requests/${requestId}/fulfill`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify({
                managerId: currentUser.id,
                expectedReturnDate: returnDate
            })
        });

        if (response.ok) {
            alert('Equipment handed out successfully!');
            loadPendingApprovals();
        } else {
            const error = await response.json();
            alert('Failed to fulfill request: ' + (error.error || 'Unknown error'));
        }
    } catch (error) {
        console.error('Error fulfilling request:', error);
        alert('Failed to fulfill request');
    }
}

// Approve request
async function approveRequest(requestId) {
    const notes = document.getElementById(`notes-${requestId}`).value || 'Approved';

    if (!confirm('Are you sure you want to approve this request?')) {
        return;
    }

    try {
        const response = await fetch(`/api/checkout/requests/${requestId}/approve`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify({
                approverId: currentUser.id,
                notes: notes
            })
        });

        if (response.ok) {
            alert('Request approved successfully!');
            loadPendingApprovals();
        } else {
            const error = await response.json();
            alert('Failed to approve request: ' + (error.error || 'Unknown error'));
        }
    } catch (error) {
        console.error('Error approving request:', error);
        alert('Failed to approve request');
    }
}

// Reject request
async function rejectRequest(requestId) {
    const notes = document.getElementById(`notes-${requestId}`).value;

    if (!notes) {
        alert('Please provide a reason for rejection');
        return;
    }

    if (!confirm('Are you sure you want to reject this request?')) {
        return;
    }

    try {
        const response = await fetch(`/api/checkout/requests/${requestId}/reject`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify({
                approverId: currentUser.id,
                notes: notes
            })
        });

        if (response.ok) {
            alert('Request rejected');
            loadPendingApprovals();
        } else {
            const error = await response.json();
            alert('Failed to reject request: ' + (error.error || 'Unknown error'));
        }
    } catch (error) {
        console.error('Error rejecting request:', error);
        alert('Failed to reject request');
    }
}

function showAddEquipmentModal() {
    alert('Add equipment functionality coming soon!');
}

// Helper function to make authenticated requests
function getAuthHeaders() {
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

// Show checked out items
function showCheckedOut() {
    if (!currentUser) {
        alert('Please login first');
        showLogin();
        return;
    }
    hideAllSections();
    document.getElementById('checkedOutSection').style.display = 'block';
    loadCheckedOut();
}

// Update hideAllSections to include the new section
function hideAllSections() {
    document.getElementById('loginSection').style.display = 'none';
    document.getElementById('equipmentSection').style.display = 'none';
    document.getElementById('requestsSection').style.display = 'none';
    document.getElementById('approvalsSection').style.display = 'none';
    document.getElementById('checkedOutSection').style.display = 'none';
}

// Load checked out items
async function loadCheckedOut() {
    try {
        const response = await fetch('/api/checkout/records/current', {
            headers: getAuthHeaders()
        });

        const records = await response.json();
        const checkedOutList = document.getElementById('checkedOutList');
        checkedOutList.innerHTML = '';

        if (records.length === 0) {
            checkedOutList.innerHTML = '<p class="text-center">No equipment currently checked out</p>';
            return;
        }

        records.forEach(record => {
            const isOverdue = new Date(record.expectedReturnDate) < new Date();

            const card = `
                <div class="card mb-3 ${isOverdue ? 'border-danger' : ''}">
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-start">
                            <div>
                                <h5 class="card-title">${record.equipment.name}</h5>
                                <p class="card-text">
                                    <strong>Checked out by:</strong> ${record.user.firstName} ${record.user.lastName} (${record.user.username})<br>
                                    <strong>Department:</strong> ${record.user.department || 'N/A'}<br>
                                    <strong>Checkout Date:</strong> ${record.checkoutDate}<br>
                                    <strong>Expected Return:</strong> ${record.expectedReturnDate}<br>
                                    <strong>Condition at Checkout:</strong> ${record.conditionAtCheckout}<br>
                                    <strong>Handed out by:</strong> ${record.checkedOutByManager ? record.checkedOutByManager.username : 'N/A'}
                                </p>
                                ${isOverdue ? '<span class="badge bg-danger mb-2">OVERDUE</span><br>' : ''}
                                <button class="btn btn-warning btn-sm mt-2" onclick="showReturnModal(${record.id}, '${record.equipment.name}')">
                                    <i class="bi bi-box-arrow-left"></i> Process Return
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            `;
            checkedOutList.innerHTML += card;
        });
    } catch (error) {
        console.error('Error loading checked out items:', error);
        document.getElementById('checkedOutList').innerHTML = '<p class="text-danger">Failed to load checked out items</p>';
    }
}