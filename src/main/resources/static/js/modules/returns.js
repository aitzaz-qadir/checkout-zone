// Returns module

let returnModal = null;

export function setReturnModal(modal) {
    returnModal = modal;
}

export async function loadCheckedOut(getAuthHeaders) {
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
                                <button class="btn btn-warning btn-sm mt-2" onclick="window.showReturnModal(${record.id}, '${record.equipment.name}')">
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

export function showReturnModal(recordId, equipmentName) {
    document.getElementById('returnRecordId').value = recordId;
    document.getElementById('returnEquipmentName').value = equipmentName;
    document.getElementById('returnCondition').value = '';
    document.getElementById('returnNotes').value = '';

    // Hide any previous messages
    document.getElementById('returnError').style.display = 'none';
    document.getElementById('returnSuccess').style.display = 'none';

    returnModal.show();
}

export async function submitReturn(currentUser, getAuthHeaders, loadCheckedOut, loadEquipment) {
    const recordId = document.getElementById('returnRecordId').value;
    const condition = document.getElementById('returnCondition').value;
    const notes = document.getElementById('returnNotes').value || 'Equipment returned';

    if (!condition) {
        alert('Please select the equipment condition');
        return;
    }

    try {
        const response = await fetch(`/api/checkout/records/${recordId}/return`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify({
                managerId: currentUser.id,
                condition: condition,
                notes: notes
            })
        });

        if (response.ok) {
            document.getElementById('returnSuccess').textContent = 'Equipment returned successfully!';
            document.getElementById('returnSuccess').style.display = 'block';
            document.getElementById('returnError').style.display = 'none';

            // Close modal and refresh after 2 seconds
            setTimeout(() => {
                returnModal.hide();
                loadCheckedOut();
                loadEquipment(); // Refresh equipment list too
            }, 2000);
        } else {
            const error = await response.json();
            document.getElementById('returnError').textContent = error.error || 'Failed to process return';
            document.getElementById('returnError').style.display = 'block';
            document.getElementById('returnSuccess').style.display = 'none';
        }
    } catch (error) {
        console.error('Error processing return:', error);
        document.getElementById('returnError').textContent = 'Failed to process return';
        document.getElementById('returnError').style.display = 'block';
    }
}

