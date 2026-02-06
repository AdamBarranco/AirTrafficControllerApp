const API_BASE = 'http://localhost:8080/api';
const canvas = document.getElementById('radar');
const ctx = canvas.getContext('2d');
const notifications = document.getElementById('notifications');
const conflictList = document.getElementById('conflictList');
const aircraftCountEl = document.getElementById('aircraftCount');
const conflictCountEl = document.getElementById('conflictCount');
const clearBtn = document.getElementById('clearBtn');

let aircrafts = [];
let conflicts = [];
let lastConflictIds = new Set();

// Add aircraft on canvas click
canvas.addEventListener('click', async (e) => {
    const rect = canvas.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;
    
    try {
        const response = await fetch(`${API_BASE}/aircraft`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ x, y })
        });
        
        if (response.ok) {
            showNotification('Aircraft added successfully', 'success');
        }
    } catch (error) {
        console.error('Error adding aircraft:', error);
        showNotification('Failed to add aircraft', 'error');
    }
});

// Clear all aircraft
clearBtn.addEventListener('click', async () => {
    try {
        await fetch(`${API_BASE}/aircraft`, { method: 'DELETE' });
        aircrafts = [];
        conflicts = [];
        showNotification('All aircraft cleared', 'success');
    } catch (error) {
        console.error('Error clearing aircraft:', error);
    }
});

// Fetch and update aircraft positions
async function updateAircrafts() {
    try {
        const response = await fetch(`${API_BASE}/aircraft`);
        aircrafts = await response.json();
        aircraftCountEl.textContent = `Aircraft: ${aircrafts.length}`;
    } catch (error) {
        console.error('Error fetching aircraft:', error);
    }
}

// Fetch conflicts
async function updateConflicts() {
    try {
        const response = await fetch(`${API_BASE}/conflicts`);
        conflicts = await response.json();
        conflictCountEl.textContent = `Conflicts: ${conflicts.length}`;
        
        // Check for new conflicts
        const currentConflictIds = new Set(
            conflicts.map(c => `${c.aircraft1.id}-${c.aircraft2.id}`)
        );
        
        conflicts.forEach(conflict => {
            const id = `${conflict.aircraft1.id}-${conflict.aircraft2.id}`;
            
            // New conflict detected
            if (!lastConflictIds.has(id) && !conflict.resolved) {
                showNotification(
                    `⚠️ Conflict detected! ${conflict.aircraft1.callSign} and ${conflict.aircraft2.callSign} are too close!`,
                    'warning'
                );
            }
            
            // Conflict resolved
            if (conflict.resolved && lastConflictIds.has(id)) {
                showNotification(
                    `✅ ${conflict.resolution}`,
                    'success'
                );
            }
        });
        
        lastConflictIds = currentConflictIds;
        updateConflictList();
    } catch (error) {
        console.error('Error fetching conflicts:', error);
    }
}

// Update conflict list display
function updateConflictList() {
    if (conflicts.length === 0) {
        conflictList.innerHTML = '<div class="no-conflicts">No conflicts detected</div>';
        return;
    }
    
    conflictList.innerHTML = conflicts.map(conflict => `
        <div class="conflict-item ${conflict.resolved ? 'resolved' : ''}">
            <strong>${conflict.aircraft1.callSign} ↔ ${conflict.aircraft2.callSign}</strong><br>
            Distance: ${conflict.distance.toFixed(1)} units<br>
            ${conflict.resolved ? `✓ ${conflict.resolution}` : '⚠ Resolving...'}
        </div>
    `).join('');
}

// Draw aircraft on canvas
function drawAircrafts() {
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    
    // Draw grid
    ctx.strokeStyle = 'rgba(74, 144, 226, 0.2)';
    ctx.lineWidth = 1;
    for (let i = 0; i < canvas.width; i += 50) {
        ctx.beginPath();
        ctx.moveTo(i, 0);
        ctx.lineTo(i, canvas.height);
        ctx.stroke();
    }
    for (let i = 0; i < canvas.height; i += 50) {
        ctx.beginPath();
        ctx.moveTo(0, i);
        ctx.lineTo(canvas.width, i);
        ctx.stroke();
    }
    
    // Draw conflict zones (red circles)
    ctx.strokeStyle = 'rgba(255, 0, 0, 0.3)';
    ctx.lineWidth = 2;
    conflicts.forEach(conflict => {
        if (!conflict.resolved) {
            ctx.beginPath();
            ctx.arc(conflict.aircraft1.x, conflict.aircraft1.y, 50, 0, 2 * Math.PI);
            ctx.stroke();
        }
    });
    
    // Draw aircraft
    aircrafts.forEach(aircraft => {
        // Check if aircraft is in conflict
        const inConflict = conflicts.some(c => 
            !c.resolved && (c.aircraft1.id === aircraft.id || c.aircraft2.id === aircraft.id)
        );
        
        // Draw aircraft icon
        ctx.save();
        ctx.translate(aircraft.x, aircraft.y);
        ctx.rotate((aircraft.heading + 90) * Math.PI / 180);
        
        // Aircraft body
        ctx.fillStyle = inConflict ? '#ff4444' : '#4CAF50';
        ctx.beginPath();
        ctx.moveTo(0, -10);
        ctx.lineTo(-5, 5);
        ctx.lineTo(0, 2);
        ctx.lineTo(5, 5);
        ctx.closePath();
        ctx.fill();
        
        // Aircraft outline
        ctx.strokeStyle = '#fff';
        ctx.lineWidth = 1;
        ctx.stroke();
        
        ctx.restore();
        
        // Draw call sign
        ctx.fillStyle = inConflict ? '#ff4444' : '#fff';
        ctx.font = '10px monospace';
        ctx.fillText(aircraft.callSign, aircraft.x + 12, aircraft.y - 5);
        
        // Draw velocity vector
        ctx.strokeStyle = inConflict ? 'rgba(255, 68, 68, 0.5)' : 'rgba(76, 175, 80, 0.5)';
        ctx.lineWidth = 1;
        ctx.beginPath();
        ctx.moveTo(aircraft.x, aircraft.y);
        ctx.lineTo(
            aircraft.x + aircraft.velocityX * 10,
            aircraft.y + aircraft.velocityY * 10
        );
        ctx.stroke();
    });
}

// Show notification
function showNotification(message, type = 'info') {
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.textContent = message;
    notifications.appendChild(notification);
    
    setTimeout(() => {
        notification.style.animation = 'slideIn 0.3s ease-out reverse';
        setTimeout(() => notification.remove(), 300);
    }, 4000);
}

// Main update loop
function update() {
    updateAircrafts();
    updateConflicts();
    drawAircrafts();
}

// Start the update loop
setInterval(update, 100);
update();

// Initial message
showNotification('Click on the radar to add aircraft', 'success');
