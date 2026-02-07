const API_BASE = '/api';
const canvas = document.getElementById('radar');
const ctx = canvas.getContext('2d');
const conflictList = document.getElementById('conflictList');
const aircraftCountEl = document.getElementById('aircraftCount');
const conflictCountEl = document.getElementById('conflictCount');
const scoreEl = document.getElementById('score');
const levelEl = document.getElementById('levelDisplay');
const clearBtn = document.getElementById('clearBtn');
const gameOverOverlay = document.getElementById('gameOverOverlay');
const gameOverStats = document.getElementById('gameOverStats');
const restartBtn = document.getElementById('restartBtn');

let aircrafts = [];
let conflicts = [];
let score = 0;
let currentLevel = 1;
let isGameOver = false;
let flaggedAircraftIds = new Set();

const AIRCRAFT_HIT_RADIUS = 22;

// Convert canvas coordinates from event
function getCanvasCoords(e) {
    const rect = canvas.getBoundingClientRect();
    const scaleX = canvas.width / rect.width;
    const scaleY = canvas.height / rect.height;
    const clientX = e.touches ? e.touches[0].clientX : e.clientX;
    const clientY = e.touches ? e.touches[0].clientY : e.clientY;
    return {
        x: (clientX - rect.left) * scaleX,
        y: (clientY - rect.top) * scaleY
    };
}

// Find aircraft near a point
function findAircraftAt(px, py) {
    for (const ac of aircrafts) {
        const dx = ac.x - px;
        const dy = ac.y - py;
        if (Math.sqrt(dx * dx + dy * dy) < AIRCRAFT_HIT_RADIUS) {
            return ac;
        }
    }
    return null;
}

// Handle tap / click on canvas
function handleCanvasInput(e) {
    e.preventDefault();
    const { x, y } = getCanvasCoords(e);
    const tapped = findAircraftAt(x, y);

    if (tapped) {
        handleAircraftTap(tapped);
    } else {
        addAircraft(x, y);
    }
}

canvas.addEventListener('click', handleCanvasInput);
canvas.addEventListener('touchstart', handleCanvasInput, { passive: false });

// Add aircraft via API
async function addAircraft(x, y) {
    try {
        await fetch(`${API_BASE}/aircraft`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ x, y })
        });
    } catch (error) {
        console.error('Error adding aircraft:', error);
    }
}

// Handle tapping an aircraft to flag a collision
async function handleAircraftTap(aircraft) {
    if (flaggedAircraftIds.has(aircraft.id)) return;
    if (isGameOver) return;

    try {
        const response = await fetch(`${API_BASE}/tap/${aircraft.id}`, { method: 'POST' });
        const data = await response.json();

        if (data.success) {
            score += 10;
            flaggedAircraftIds.add(aircraft.id);

            // Check for level up
            if (data.level > currentLevel) {
                currentLevel = data.level;
                levelEl.textContent = `Level: ${currentLevel}`;
                flashLevel();
            }
        } else {
            score = Math.max(0, score - 5);
        }
        flashScore();
    } catch (error) {
        console.error('Error tapping aircraft:', error);
        // Fallback to local scoring
        const inConflict = conflicts.some(c =>
            !c.resolved && (c.aircraft1.id === aircraft.id || c.aircraft2.id === aircraft.id)
        );
        if (inConflict) {
            score += 10;
            flaggedAircraftIds.add(aircraft.id);
        } else {
            score = Math.max(0, score - 5);
        }
        flashScore();
    }

    scoreEl.textContent = `Score: ${score}`;
}

function flashScore() {
    scoreEl.classList.remove('flash');
    void scoreEl.offsetWidth; // force reflow to restart CSS animation
    scoreEl.classList.add('flash');
}

function flashLevel() {
    levelEl.classList.remove('flash');
    void levelEl.offsetWidth;
    levelEl.classList.add('flash');
}

// Fetch game state from backend
async function updateGameState() {
    try {
        const response = await fetch(`${API_BASE}/gamestate`);
        const state = await response.json();

        if (state.level !== currentLevel) {
            currentLevel = state.level;
            levelEl.textContent = `Level: ${currentLevel}`;
            flashLevel();
        }

        if (state.gameOver && !isGameOver) {
            isGameOver = true;
            gameOverStats.textContent = `You reached Level ${currentLevel} with a score of ${score}.`;
            gameOverOverlay.style.display = 'flex';
        }
    } catch (error) {
        console.error('Error fetching game state:', error);
    }
}

// Restart game
restartBtn.addEventListener('click', async () => {
    try {
        await fetch(`${API_BASE}/reset`, { method: 'POST' });
        isGameOver = false;
        score = 0;
        currentLevel = 1;
        aircrafts = [];
        conflicts = [];
        flaggedAircraftIds.clear();
        scoreEl.textContent = 'Score: 0';
        levelEl.textContent = 'Level: 1';
        gameOverOverlay.style.display = 'none';
    } catch (error) {
        console.error('Error resetting game:', error);
    }
});

// Clear all aircraft
clearBtn.addEventListener('click', async () => {
    try {
        await fetch(`${API_BASE}/aircraft`, { method: 'DELETE' });
        aircrafts = [];
        conflicts = [];
        flaggedAircraftIds.clear();
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

        // Clear flags for resolved conflicts
        const activeIds = new Set();
        conflicts.forEach(c => {
            if (!c.resolved) {
                activeIds.add(c.aircraft1.id);
                activeIds.add(c.aircraft2.id);
            }
        });
        for (const id of flaggedAircraftIds) {
            if (!activeIds.has(id)) flaggedAircraftIds.delete(id);
        }

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

// Draw a realistic airplane shape
function drawAirplane(x, y, heading, color, isFlagged) {
    ctx.save();
    ctx.translate(x, y);
    ctx.rotate((heading + 90) * Math.PI / 180);

    // Glow for flagged aircraft
    if (isFlagged) {
        ctx.shadowColor = '#ffd700';
        ctx.shadowBlur = 12;
    }

    // Fuselage
    ctx.fillStyle = color;
    ctx.beginPath();
    ctx.moveTo(0, -14);
    ctx.quadraticCurveTo(2, -6, 2, 8);
    ctx.lineTo(0, 12);
    ctx.lineTo(-2, 8);
    ctx.quadraticCurveTo(-2, -6, 0, -14);
    ctx.closePath();
    ctx.fill();

    // Main wings
    ctx.beginPath();
    ctx.moveTo(-1, -2);
    ctx.lineTo(-14, 4);
    ctx.lineTo(-13, 6);
    ctx.lineTo(-1, 2);
    ctx.closePath();
    ctx.fill();

    ctx.beginPath();
    ctx.moveTo(1, -2);
    ctx.lineTo(14, 4);
    ctx.lineTo(13, 6);
    ctx.lineTo(1, 2);
    ctx.closePath();
    ctx.fill();

    // Tail wings
    ctx.beginPath();
    ctx.moveTo(-1, 8);
    ctx.lineTo(-6, 12);
    ctx.lineTo(-5, 13);
    ctx.lineTo(-1, 10);
    ctx.closePath();
    ctx.fill();

    ctx.beginPath();
    ctx.moveTo(1, 8);
    ctx.lineTo(6, 12);
    ctx.lineTo(5, 13);
    ctx.lineTo(1, 10);
    ctx.closePath();
    ctx.fill();

    // Outline
    ctx.strokeStyle = 'rgba(255,255,255,0.6)';
    ctx.lineWidth = 0.5;
    ctx.beginPath();
    ctx.moveTo(0, -14);
    ctx.quadraticCurveTo(2, -6, 2, 8);
    ctx.lineTo(0, 12);
    ctx.lineTo(-2, 8);
    ctx.quadraticCurveTo(-2, -6, 0, -14);
    ctx.stroke();

    ctx.shadowColor = 'transparent';
    ctx.shadowBlur = 0;
    ctx.restore();
}

// Draw aircraft on canvas
function drawAircrafts() {
    ctx.clearRect(0, 0, canvas.width, canvas.height);

    // Draw grid
    ctx.strokeStyle = 'rgba(74, 144, 226, 0.15)';
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

    // Draw conflict zones
    ctx.strokeStyle = 'rgba(255, 0, 0, 0.25)';
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
        const inConflict = conflicts.some(c =>
            !c.resolved && (c.aircraft1.id === aircraft.id || c.aircraft2.id === aircraft.id)
        );
        const isFlagged = flaggedAircraftIds.has(aircraft.id);

        const color = isFlagged ? '#ffd700' : inConflict ? '#ff4444' : '#4CAF50';
        drawAirplane(aircraft.x, aircraft.y, aircraft.heading, color, isFlagged);

        // Call sign
        ctx.fillStyle = inConflict ? '#ff4444' : '#ccc';
        ctx.font = '10px -apple-system, sans-serif';
        ctx.fillText(aircraft.callSign, aircraft.x + 16, aircraft.y - 6);

        // Velocity vector
        ctx.strokeStyle = inConflict ? 'rgba(255, 68, 68, 0.4)' : 'rgba(76, 175, 80, 0.4)';
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

// Main update loop
function update() {
    updateAircrafts();
    updateConflicts();
    updateGameState();
    drawAircrafts();
}

setInterval(update, 100);
update();
