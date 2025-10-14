// Approvals module

export async function loadPendingApprovals(getAuthHeaders, getDefaultReturnDate) {
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
                                        <button class="btn btn-success btn-sm me-2" onclick="window.approveRequest(${request.id})">
                                            <i class="bi bi-check-circle"></i> Approve
                                        </button>
                                        <button class="btn btn-danger btn-sm" onclick="window.rejectRequest(${request.id})">
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
                                        <button class="btn btn-primary btn-sm" onclick="window.fulfillRequest(${request.id})">
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

export async function fulfillRequest(requestId, currentUser, getAuthHeaders, loadPendingApprovals) {
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

export async function approveRequest(requestId, currentUser, getAuthHeaders, loadPendingApprovals) {
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

export async function rejectRequest(requestId, currentUser, getAuthHeaders, loadPendingApprovals) {
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

