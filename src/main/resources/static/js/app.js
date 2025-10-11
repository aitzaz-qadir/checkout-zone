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

// Load pending approvals
function loadPendingApprovals() {
    document.getElementById('approvalsList').innerHTML = '<p>Coming soon...</p>';
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