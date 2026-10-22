/**
 * Smart Campus Infrastructure Problem Reporter
 * Shared Frontend Utilities, Dynamic Location Rules & High-Fidelity Vector Icon Engine
 */

// ===================================================================
// Campus Location Rules & Dynamic Configuration
// ===================================================================
const CAMPUS_BLOCKS = [
  "Academic Block",
  "Civil Block",
  "Mechanical Block",
  "Block 3",
  "Electronics Block"
];

const CAMPUS_FLOORS = [
  { name: "1st Floor", prefix: "LH-1" },
  { name: "2nd Floor", prefix: "LH-2" },
  { name: "3rd Floor", prefix: "LH-3" },
  { name: "4th Floor", prefix: "LH-4" }
];

const SHARED_FLOOR_LOCATIONS = [
  "Male Bathroom",
  "Female Bathroom",
  "Staff Room"
];

/**
 * Generate 10 classrooms dynamically for a floor prefix (e.g. 'LH-1' -> LH-1001 through LH-1010)
 */
function generateClassroomsForFloor(prefix) {
  const classrooms = [];
  for (let i = 1; i <= 10; i++) {
    const numStr = String(i).padStart(3, "0");
    classrooms.push(`${prefix}${numStr}`);
  }
  return classrooms;
}

/**
 * Build location list for a floor: 10 classrooms + 3 shared locations
 */
function getLocationsForFloor(floorName) {
  const floorConfig = CAMPUS_FLOORS.find(f => f.name === floorName);
  if (!floorConfig) return [];
  const classrooms = generateClassroomsForFloor(floorConfig.prefix);
  return [...classrooms, ...SHARED_FLOOR_LOCATIONS];
}

/**
 * Programmatically build the unified CAMPUS_LOCATIONS structure
 */
function buildCampusLocations() {
  const map = {};
  CAMPUS_BLOCKS.forEach(block => {
    map[block] = {};
    CAMPUS_FLOORS.forEach(floor => {
      map[block][floor.name] = getLocationsForFloor(floor.name);
    });
  });
  return map;
}

const CAMPUS_LOCATIONS = buildCampusLocations();

// Emergency Contacts
const CAMPUS_EMERGENCY_CONTACTS = {
  controlRoom: "044-2220-XXXX (Extension 101)",
  fireSafety: "101 / Campus Ext 102",
  medicalClinic: "Campus Health Centre Ext 104",
  note: "For active fires, electrical sparks, gas leaks, structural collapse, or immediate threat to life, call the Control Room immediately. Maintenance portal tickets are routed for scheduled departmental handling."
};

/**
 * Configure Cascading Dropdowns: Block -> Floor -> Room/Location
 * Strictly ensures students cannot select an impossible room or floor.
 */
function setupCascadingLocationDropdowns(blockSelectId, floorSelectId, locationSelectId) {
  const blockSelect = document.getElementById(blockSelectId);
  const floorSelect = document.getElementById(floorSelectId);
  const locationSelect = document.getElementById(locationSelectId);

  if (!blockSelect || !floorSelect || !locationSelect) return;

  // Initial State: Populate Blocks and reset dependent dropdowns
  blockSelect.innerHTML = '<option value="">Select campus block</option>';
  CAMPUS_BLOCKS.forEach(block => {
    const opt = document.createElement("option");
    opt.value = block;
    opt.textContent = block;
    blockSelect.appendChild(opt);
  });

  floorSelect.innerHTML = '<option value="">Select floor level</option>';
  floorSelect.disabled = true;

  locationSelect.innerHTML = '<option value="">Select classroom or area</option>';
  locationSelect.disabled = true;

  // Handle Block Change -> reset and populate Floors
  blockSelect.addEventListener("change", () => {
    const selectedBlock = blockSelect.value;
    floorSelect.innerHTML = '<option value="">Select floor level</option>';
    locationSelect.innerHTML = '<option value="">Select classroom or area</option>';
    floorSelect.value = "";
    locationSelect.value = "";

    if (selectedBlock && CAMPUS_LOCATIONS[selectedBlock]) {
      floorSelect.disabled = false;
      locationSelect.disabled = true;

      const floors = Object.keys(CAMPUS_LOCATIONS[selectedBlock]);
      floors.forEach(floor => {
        const opt = document.createElement("option");
        opt.value = floor;
        opt.textContent = floor;
        floorSelect.appendChild(opt);
      });
    } else {
      floorSelect.disabled = true;
      locationSelect.disabled = true;
    }

    floorSelect.dispatchEvent(new Event("change"));
  });

  // Handle Floor Change -> reset and populate Rooms/Specific Locations
  floorSelect.addEventListener("change", () => {
    const selectedBlock = blockSelect.value;
    const selectedFloor = floorSelect.value;

    locationSelect.innerHTML = '<option value="">Select classroom or area</option>';
    locationSelect.value = "";

    if (selectedBlock && selectedFloor && CAMPUS_LOCATIONS[selectedBlock] && CAMPUS_LOCATIONS[selectedBlock][selectedFloor]) {
      locationSelect.disabled = false;

      const locations = CAMPUS_LOCATIONS[selectedBlock][selectedFloor];
      locations.forEach(loc => {
        const opt = document.createElement("option");
        opt.value = loc;
        opt.textContent = loc;
        locationSelect.appendChild(opt);
      });
    } else {
      locationSelect.disabled = true;
    }

    locationSelect.dispatchEvent(new Event("change"));
  });
}

// ===================================================================
// Enterprise Vector Icon Engine (Lucide-Compatible SVG System)
// Zero external dependency guarantee: works 100% online & offline
// ===================================================================
const ICON_SVGS = {
  "layout-dashboard": '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="svg-icon {c}"><rect width="7" height="9" x="3" y="3" rx="1"/><rect width="7" height="5" x="14" y="3" rx="1"/><rect width="7" height="9" x="14" y="12" rx="1"/><rect width="7" height="5" x="3" y="16" rx="1"/></svg>',
  "bar-chart-3": '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="svg-icon {c}"><path d="M3 3v18h18"/><path d="M18 17V9"/><path d="M13 17V5"/><path d="M8 17v-3"/></svg>',
  "building": '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="svg-icon {c}"><rect width="16" height="20" x="4" y="2" rx="2" ry="2"/><path d="M9 22v-4h6v4"/><path d="M8 6h.01"/><path d="M16 6h.01"/><path d="M8 10h.01"/><path d="M16 10h.01"/><path d="M8 14h.01"/><path d="M16 14h.01"/><path d="M8 18h.01"/><path d="M16 18h.01"/></svg>',
  "map-pin": '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="svg-icon {c}"><path d="M20 10c0 6-8 12-8 12s-8-6-8-12a8 8 0 0 1 16 0Z"/><circle cx="12" cy="10" r="3"/></svg>',
  "wrench": '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="svg-icon {c}"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"/></svg>',
  "zap": '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="svg-icon {c}"><polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/></svg>',
  "droplets": '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="svg-icon {c}"><path d="M7 16.3c2.2 0 4-1.83 4-4.05 0-1.16-.57-2.26-1.71-3.19S7.29 6.75 7 5.3c-.29 1.45-1.14 2.84-2.29 3.76S3 11.1 3 12.25c0 2.22 1.8 4.05 4 4.05z"/><path d="M12.56 6.6A10.97 10.97 0 0 0 14 3.02c.5 2.5 2 4.9 4 6.5s3 3.5 3 5.5a6.98 6.98 0 0 1-11.91 4.97"/></svg>',
  "sparkles": '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="svg-icon {c}"><path d="m12 3-1.912 5.813a2 2 0 0 1-1.275 1.275L3 12l5.813 1.912a2 2 0 0 1 1.275 1.275L12 21l1.912-5.813a2 2 0 0 1 1.275-1.275L21 12l-5.813-1.912a2 2 0 0 1-1.275-1.275L12 3Z"/><path d="M5 3v4"/><path d="M19 17v4"/><path d="M3 5h4"/><path d="M17 19h4"/></svg>',
  "monitor": '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="svg-icon {c}"><rect width="20" height="14" x="2" y="3" rx="2"/><line x1="8" x2="16" y1="21" y2="21"/><line x1="12" x2="12" y1="17" y2="21"/></svg>',
  "shield": '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="svg-icon {c}"><path d="M20 13c0 5-3.5 7.5-7.66 8.95a1 1 0 0 1-.67-.01C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1c2 0 4.5-1.2 6.24-2.72a1.17 1.17 0 0 1 1.52 0C14.51 3.81 17 5 19 5a1 1 0 0 1 1 1z"/></svg>',
  "user": '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="svg-icon {c}"><path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>',
  "users": '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="svg-icon {c}"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>',
  "phone": '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="svg-icon {c}"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/></svg>',
  "calendar": '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="svg-icon {c}"><rect width="18" height="18" x="3" y="4" rx="2" ry="2"/><line x1="16" x2="16" y1="2" y2="6"/><line x1="8" x2="8" y1="2" y2="6"/><line x1="3" x2="21" y1="10" y2="10"/></svg>',
  "clock": '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="svg-icon {c}"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>',
  "inbox": '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="svg-icon {c}"><polyline points="22 12 16 12 14 15 10 15 8 12 2 12"/><path d="M5.45 5.11 2 12v6a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2v-6l-3.45-6.89A2 2 0 0 0 16.76 4H7.24a2 2 0 0 0-1.79 1.11z"/></svg>',
  "clipboard-list": '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="svg-icon {c}"><rect width="8" height="4" x="8" y="2" rx="1" ry="1"/><path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"/><path d="M12 11h4"/><path d="M12 16h4"/><path d="M8 11h.01"/><path d="M8 16h.01"/></svg>',
  "check-circle-2": '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="svg-icon {c}"><circle cx="12" cy="12" r="10"/><path d="m9 12 2 2 4-4"/></svg>',
  "check": '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="svg-icon {c}"><polyline points="20 6 9 17 4 12"/></svg>',
  "alert-triangle": '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="svg-icon {c}"><path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z"/><line x1="12" x2="12" y1="9" y2="13"/><line x1="12" x2="12.01" y1="17" y2="17"/></svg>',
  "x-circle": '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="svg-icon {c}"><circle cx="12" cy="12" r="10"/><line x1="15" x2="9" y1="9" y2="15"/><line x1="9" x2="15" y1="9" y2="15"/></svg>',
  "x": '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="svg-icon {c}"><line x1="18" x2="6" y1="6" y2="18"/><line x1="6" x2="18" y1="6" y2="18"/></svg>',
  "plus": '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="svg-icon {c}"><line x1="12" x2="12" y1="5" y2="19"/><line x1="5" x2="19" y1="12" y2="12"/></svg>',
  "refresh-cw": '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="svg-icon {c}"><path d="M3 12a9 9 0 0 1 9-9 9.75 9.75 0 0 1 6.74 2.74L21 8"/><path d="M21 3v5h-5"/><path d="M21 12a9 9 0 0 1-9 9 9.75 9.75 0 0 1-6.74-2.74L3 16"/><path d="M8 16H3v5"/></svg>',
  "log-out": '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="svg-icon {c}"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" x2="9" y1="12" y2="12"/></svg>',
  "search": '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="svg-icon {c}"><circle cx="11" cy="11" r="8"/><line x1="21" x2="16.65" y1="21" y2="16.65"/></svg>',
  "filter": '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="svg-icon {c}"><polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3"/></svg>',
  "chevron-right": '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="svg-icon {c}"><polyline points="9 18 15 12 9 6"/></svg>',
  "chevron-left": '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="svg-icon {c}"><polyline points="15 18 9 12 15 6"/></svg>',
  "chevron-down": '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="svg-icon {c}"><polyline points="6 9 12 15 18 9"/></svg>',
  "eye": '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="svg-icon {c}"><path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z"/><circle cx="12" cy="12" r="3"/></svg>',
  "eye-off": '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="svg-icon {c}"><path d="M9.88 9.88a3 3 0 1 0 4.24 4.24"/><path d="M10.73 5.08A10.43 10.43 0 0 1 12 5c7 0 10 7 10 7a13.16 13.16 0 0 1-1.67 2.68"/><path d="M6.61 6.61A13.526 13.526 0 0 0 2 12s3 7 10 7a9.74 9.74 0 0 0 5.39-1.61"/><line x1="2" x2="22" y1="2" y2="22"/></svg>',
  "camera": '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="svg-icon {c}"><path d="M14.5 4h-5L7 7H4a2 2 0 0 0-2 2v9a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2h-3l-2.5-3z"/><circle cx="12" cy="13" r="3"/></svg>',
  "image": '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="svg-icon {c}"><rect width="18" height="18" x="3" y="3" rx="2" ry="2"/><circle cx="9" cy="9" r="2"/><path d="m21 15-3.086-3.086a2 2 0 0 0-2.828 0L6 21"/></svg>',
  "maximize-2": '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="svg-icon {c}"><polyline points="15 3 21 3 21 9"/><polyline points="9 21 3 21 3 15"/><line x1="21" x2="14" y1="3" y2="10"/><line x1="3" x2="10" y1="21" y2="14"/></svg>',
  "file-text": '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="svg-icon {c}"><path d="M14.5 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7.5L14.5 2z"/><polyline points="14 2 14 8 20 8"/><line x1="16" x2="8" y1="13" y2="13"/><line x1="16" x2="8" y1="17" y2="17"/><line x1="10" x2="8" y1="9" y2="9"/></svg>',
  "menu": '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="svg-icon {c}"><line x1="4" x2="20" y1="12" y2="12"/><line x1="4" x2="20" y1="6" y2="6"/><line x1="4" x2="20" y1="18" y2="18"/></svg>',
  "arrow-left": '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="svg-icon {c}"><line x1="19" x2="5" y1="12" y2="12"/><polyline points="12 19 5 12 12 5"/></svg>',
  "arrow-right": '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="svg-icon {c}"><line x1="5" x2="19" y1="12" y2="12"/><polyline points="12 5 19 12 12 19"/></svg>',
  "info": '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="svg-icon {c}"><circle cx="12" cy="12" r="10"/><line x1="12" x2="12" y1="16" y2="12"/><line x1="12" x2="12.01" y1="8" y2="8"/></svg>',
  "layers": '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="svg-icon {c}"><polygon points="12 2 2 7 12 12 22 7 12 2"/><polyline points="2 17 12 22 22 17"/><polyline points="2 12 12 17 22 12"/></svg>',
  "clock-arrow": '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="svg-icon {c}"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 14 14"/></svg>',
  "loader": '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="svg-icon {c}"><line x1="12" x2="12" y1="2" y2="6"/><line x1="12" x2="12" y1="18" y2="22"/><line x1="4.93" x2="7.76" y1="4.93" y2="7.76"/><line x1="16.24" x2="19.07" y1="16.24" y2="19.07"/><line x1="2" x2="6" y1="12" y2="12"/><line x1="18" x2="22" y1="12" y2="12"/><line x1="4.93" x2="7.76" y1="19.07" y2="16.24"/><line x1="16.24" x2="19.07" y1="7.76" y2="4.93"/></svg>',
  "lock": '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="svg-icon {c}"><rect width="18" height="11" x="3" y="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>'
};

/**
 * Returns raw SVG string for an icon name with custom size and classes
 */
function getIconSvg(name, options = {}) {
  const size = options.size || 16;
  const className = options.className || "";
  const tpl = ICON_SVGS[name] || ICON_SVGS["file-text"];
  return tpl.replace(/\{s\}/g, String(size)).replace(/\{c\}/g, className);
}

/**
 * Auto-hydrates any elements with data-icon="icon-name"
 */
function renderAllIcons(root = document) {
  if (typeof lucide !== "undefined" && lucide.createIcons) {
    try {
      lucide.createIcons();
    } catch (ignored) {}
  }
  const elements = root.querySelectorAll("[data-icon]");
  elements.forEach(el => {
    const iconName = el.getAttribute("data-icon");
    const iconSize = parseInt(el.getAttribute("data-icon-size") || "16", 10);
    const extraClass = el.getAttribute("data-icon-class") || "";
    el.innerHTML = getIconSvg(iconName, { size: iconSize, className: extraClass });
  });
}

document.addEventListener("DOMContentLoaded", () => {
  renderAllIcons();
});

// ===================================================================
// Notification Toast System
// ===================================================================
function showToast(message, type = "info") {
  let container = document.querySelector(".toast-container");
  if (!container) {
    container = document.createElement("div");
    container.className = "toast-container";
    document.body.appendChild(container);
  }

  const toast = document.createElement("div");
  toast.className = `toast toast-${type}`;
  
  let iconName = "info";
  if (type === "success") iconName = "check-circle-2";
  else if (type === "danger") iconName = "alert-triangle";
  else if (type === "warning") iconName = "alert-triangle";

  toast.innerHTML = `
    <span class="toast-icon">${getIconSvg(iconName, { size: 16 })}</span>
    <span class="toast-message">${escapeHTML(message)}</span>
  `;

  container.appendChild(toast);
  setTimeout(() => {
    toast.style.opacity = "0";
    toast.style.transform = "translateX(20px)";
    setTimeout(() => toast.remove(), 250);
  }, 4000);
}

// ===================================================================
// Enhanced Modal Manager
// ===================================================================
let activeModalStack = [];

function openModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) {
    modal.classList.add("active");
    if (!activeModalStack.includes(modalId)) {
      activeModalStack.push(modalId);
    }
    document.body.style.overflow = "hidden";
    renderAllIcons(modal);
  }
}

function closeModal(modalId) {
  if (typeof stopActiveSlaCountdownTimer === "function") {
    stopActiveSlaCountdownTimer();
  }
  const modal = document.getElementById(modalId);
  if (modal) {
    modal.classList.remove("active");
    activeModalStack = activeModalStack.filter(id => id !== modalId);
  }
  const remaining = document.querySelectorAll(".modal-overlay.active, .modal.active");
  if (remaining.length === 0) {
    document.body.style.overflow = "";
  }
}

function closeTopModal() {
  if (activeModalStack.length > 0) {
    const topId = activeModalStack[activeModalStack.length - 1];
    const topEl = document.getElementById(topId);
    if (topEl && (topEl.getAttribute("data-static-backdrop") === "true" || topEl.dataset.staticBackdrop === "true")) {
      return false;
    }
    closeModal(topId);
    return true;
  }
  const anyActive = document.querySelector(".modal-overlay.active");
  if (anyActive) {
    if (anyActive.getAttribute("data-static-backdrop") === "true" || anyActive.dataset.staticBackdrop === "true") {
      return false;
    }
    closeModal(anyActive.id);
    return true;
  }
  return false;
}

// Global Escape Key Listener for modal dismissal
document.addEventListener("keydown", (e) => {
  if (e.key === "Escape" || e.key === "Esc") {
    closeTopModal();
  }
});

// Global Backdrop Click Listener for modal dismissal
document.addEventListener("click", (e) => {
  if (e.target && e.target.classList && e.target.classList.contains("modal-overlay") && e.target.classList.contains("active")) {
    if (e.target.getAttribute("data-static-backdrop") === "true" || e.target.dataset.staticBackdrop === "true") {
      return;
    }
    closeModal(e.target.id);
  }
});

// ===================================================================
// Date & String Helpers
// ===================================================================
function formatDateTime(timestamp) {
  if (!timestamp) return "N/A";
  const date = new Date(timestamp);
  if (isNaN(date.getTime())) return String(timestamp);
  return date.toLocaleString("en-US", {
    month: "short",
    day: "numeric",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
    hour12: true
  });
}

function formatDate(timestamp) {
  return formatDateTime(timestamp);
}

function formatDisplayDateTimeShort(timestamp) {
  if (!timestamp) return "";
  const d = new Date(timestamp);
  if (isNaN(d.getTime())) return String(timestamp);
  const m = d.toLocaleString("en-US", { month: "short" });
  const day = d.getDate();
  const time = d.toLocaleString("en-US", { hour: "numeric", minute: "2-digit", hour12: true });
  return `${m} ${day}, ${time}`;
}

function escapeHTML(str) {
  if (!str) return "";
  return String(str)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#039;");
}

function getStatusBadgeClass(status) {
  const s = String(status || "").toUpperCase();
  switch (s) {
    case "SUBMITTED": return "badge-submitted";
    case "UNDER_REVIEW": return "badge-under-review";
    case "IN_PROGRESS": return "badge-in-progress";
    case "AWAITING_APPROVAL": return "badge-awaiting-approval";
    case "AWAITING_VERIFICATION": return "badge-verification";
    case "RESOLVED":
    case "CLOSED": return "badge-resolved";
    case "DECLINED":
    case "REWORK_REQUIRED": return "badge-declined";
    default: return "badge-submitted";
  }
}

/**
 * Returns appropriate icon for each department
 */
function getDepartmentIcon(deptStr) {
  const d = String(deptStr || "").toUpperCase();
  if (d.includes("ELECTRICAL")) return "zap";
  if (d.includes("PLUMBING")) return "droplets";
  if (d.includes("CLEANING")) return "sparkles";
  if (d.includes("IT") || d.includes("SUPPORT")) return "monitor";
  return "wrench";
}

// ===================================================================
// Reusable Structured Complaint Metadata Component
// Professional enterprise layout replacing emoji cards with structured panels
// ===================================================================
function renderStructuredComplaintMetadata(c, options = {}) {
  if (!c) return "";

  const normStatus = String(c.statusCode || c.status || "").toUpperCase().replace(/\s+/g, "_");
  const locStr = `${escapeHTML(c.campusBlock || c.block || "Campus")} &bull; ${escapeHTML(c.floorLevel || c.floor || "Floor")} &bull; ${escapeHTML(c.specificLocation || "Area")}`;
  const deptStr = escapeHTML(c.departmentLabel || c.department || "General");
  const reporterStr = escapeHTML(c.studentEmail || c.studentName || "student@smartcampus.edu");
  const phoneStr = escapeHTML(c.studentPhone || "N/A");
  const submittedStr = formatDateTime(c.submittedAt);
  const statusStr = escapeHTML(c.status || "Submitted");
  const statusBadgeCls = getStatusBadgeClass(c.status);
  const deptIconName = getDepartmentIcon(c.department);

  // Original student attachments
  const studentImages = Array.isArray(c.imageUrls) 
    ? c.imageUrls 
    : (Array.isArray(c.attachmentUrls) 
        ? c.attachmentUrls 
        : (c.imageUrl ? [c.imageUrl] : []));

  let studentPhotosHtml = "";
  if (studentImages.length > 0) {
    studentPhotosHtml = `
      <div class="evidence-section-panel">
        <div class="evidence-section-header">
          <div class="evidence-section-title">
            ${getIconSvg("camera", { size: 16 })}
            <span>Student-Submitted Evidence (${studentImages.length} photo${studentImages.length === 1 ? '' : 's'})</span>
          </div>
          <span class="evidence-hint">Click photo to enlarge</span>
        </div>
        <div class="evidence-gallery" role="group" aria-label="Student submitted images">
          ${studentImages.map((img, idx) => {
            const resolvedUrl = typeof apiClient !== 'undefined' ? apiClient.getFileUrl(img) : img;
            return `
              <div class="evidence-thumb-card" 
                   tabindex="0"
                   role="button"
                   aria-label="Enlarge student submitted photo ${idx + 1}"
                   onclick="viewSharedModalImage('${escapeHTML(resolvedUrl)}')"
                   onkeydown="if(event.key==='Enter'||event.key===' '){event.preventDefault();viewSharedModalImage('${escapeHTML(resolvedUrl)}');}">
                <img src="${escapeHTML(resolvedUrl)}" 
                     alt="Student Complaint Photo ${idx + 1}" 
                     loading="lazy"
                     onerror="handleStudentImageError(this, '${escapeHTML(resolvedUrl)}')" />
                <div class="evidence-thumb-zoom">
                  ${getIconSvg("maximize-2", { size: 14 })}
                  <span>Zoom</span>
                </div>
              </div>
            `;
          }).join("")}
        </div>
      </div>
    `;
  } else {
    studentPhotosHtml = `
      <div class="evidence-section-panel">
        <div class="evidence-section-header">
          <div class="evidence-section-title">
            ${getIconSvg("camera", { size: 16 })}
            <span>Student-Submitted Evidence</span>
          </div>
        </div>
        <div class="no-images-banner">
          <span class="no-images-icon">${getIconSvg("image", { size: 20 })}</span>
          <div>
            <div class="no-images-title">No photographic evidence attached</div>
            <div class="no-images-desc">The student reporter did not upload any initial photographs with this ticket.</div>
          </div>
        </div>
      </div>
    `;
  }

  // Repair Proof Evidence (Completed work)
  const proofList = c.repairEvidenceImages || (c.repairEvidenceUrl ? [c.repairEvidenceUrl] : []);
  const hasProof = (Array.isArray(proofList) && proofList.length > 0) || normStatus === "AWAITING_VERIFICATION" || c.verificationRequestedAt;
  let evidenceHtml = "";
  if (hasProof) {
    const completedTime = c.verificationRequestedAt ? formatDateTime(c.verificationRequestedAt) : (c.resolvedAt ? formatDateTime(c.resolvedAt) : "");
    const ack = (c.repairNotes || c.acknowledgement || c.evidenceNotes || "").trim();

    evidenceHtml = `
      <div class="repair-proof-panel">
        <div class="repair-proof-header">
          <div class="repair-proof-title">
            ${getIconSvg("check-circle-2", { size: 17, className: "text-success" })}
            <span>Technician Repair Completion Proof</span>
          </div>
          ${completedTime ? `<div class="repair-proof-date">Completed: ${completedTime}</div>` : ''}
        </div>
        ${ack ? `
          <div class="repair-notes-card">
            <div class="repair-notes-label">
              ${getIconSvg("file-text", { size: 13 })}
              <span>Technician Repair Notes &amp; Acknowledgement</span>
            </div>
            <div class="repair-notes-text">${escapeHTML(ack)}</div>
          </div>
        ` : ''}
        ${proofList.length > 0 ? `
          <div class="evidence-gallery" style="margin-top: 10px;">
            ${proofList.map((img, pIdx) => `
              <div class="evidence-thumb-card" 
                   tabindex="0" 
                   role="button"
                   aria-label="Enlarge repair proof photo ${pIdx + 1}"
                   onclick="viewSharedModalImage('${escapeHTML(typeof apiClient !== 'undefined' ? apiClient.getFileUrl(img) : img)}')">
                <img src="${escapeHTML(typeof apiClient !== 'undefined' ? apiClient.getFileUrl(img) : img)}" alt="Repair Evidence" />
                <div class="evidence-thumb-zoom">
                  ${getIconSvg("maximize-2", { size: 14 })}
                  <span>Zoom</span>
                </div>
              </div>
            `).join("")}
          </div>
        ` : '<p class="repair-proof-recorded">Technician recorded physical repair completion without photo attachments.</p>'}
      </div>
    `;
  }

  // Declined Box
  let declineHtml = "";
  if (normStatus === "DECLINED" || c.declinedReason) {
    declineHtml = `
      <div class="decline-details-box">
        <div class="decline-box-header">
          <span class="decline-box-icon">${getIconSvg("x-circle", { size: 18 })}</span>
          <strong class="decline-box-title">Ticket Declined by Department</strong>
        </div>
        <div class="decline-box-meta">
          Declined by: <strong>${escapeHTML(c.declinedBy || 'Department Staff')}</strong> &bull; ${formatDateTime(c.declinedAt || c.updatedAt)}
        </div>
        <div class="decline-box-content">
          <strong>Official Reason:</strong><br>
          ${escapeHTML(c.declinedReason || 'No reason specified.')}
        </div>
      </div>
    `;
  }

  return `
    <div class="meta-section-container">
      <!-- Row 1: Full-width Location card -->
      <div class="meta-card-box full-width">
        <div class="meta-card-icon-wrap">
          ${getIconSvg("map-pin", { size: 18, className: "meta-icon" })}
        </div>
        <div class="meta-card-body">
          <span class="meta-card-label">CAMPUS LOCATION</span>
          <span class="meta-card-value">${locStr}</span>
        </div>
      </div>

      <!-- Row 2: 2-column grid (Department & Student Reporter) -->
      <div class="meta-grid-2col">
        <div class="meta-card-box">
          <div class="meta-card-icon-wrap">
            ${getIconSvg(deptIconName, { size: 18, className: "meta-icon" })}
          </div>
          <div class="meta-card-body">
            <span class="meta-card-label">ASSIGNED DEPARTMENT</span>
            <span class="meta-card-value">${deptStr}</span>
          </div>
        </div>

        <div class="meta-card-box">
          <div class="meta-card-icon-wrap">
            ${getIconSvg("user", { size: 18, className: "meta-icon" })}
          </div>
          <div class="meta-card-body">
            <span class="meta-card-label">STUDENT REPORTER</span>
            <span class="meta-card-value">${reporterStr}</span>
          </div>
        </div>
      </div>

      <!-- Row 3: 3-column grid (Submitted, Phone, Current Status) -->
      <div class="meta-grid-3col">
        <div class="meta-card-box">
          <div class="meta-card-icon-wrap">
            ${getIconSvg("calendar", { size: 16, className: "meta-icon" })}
          </div>
          <div class="meta-card-body">
            <span class="meta-card-label">SUBMITTED ON</span>
            <span class="meta-card-value font-sm">${submittedStr}</span>
          </div>
        </div>

        <div class="meta-card-box">
          <div class="meta-card-icon-wrap">
            ${getIconSvg("phone", { size: 16, className: "meta-icon" })}
          </div>
          <div class="meta-card-body">
            <span class="meta-card-label">CONTACT PHONE</span>
            <span class="meta-card-value">${phoneStr}</span>
          </div>
        </div>

        <div class="meta-card-box">
          <div class="meta-card-body full-width">
            <span class="meta-card-label">CURRENT STATUS</span>
            <div class="meta-card-status-row">
              <span class="badge ${statusBadgeCls}">${statusStr}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- SLA 7-Day Countdown Timer Section -->
    ${renderSlaCountdownSection(c, options)}

    <!-- Problem Description -->
    <div class="problem-desc-panel">
      <div class="problem-desc-header">
        ${getIconSvg("file-text", { size: 15 })}
        <span>Problem Description</span>
      </div>
      <p class="problem-desc-body">${escapeHTML(c.description || "No description provided.")}</p>
      ${c.acceptedBy ? `
        <div class="workflow-acceptance-banner">
          ${getIconSvg("check", { size: 14, className: "text-success" })}
          <span>Accepted by <strong>${escapeHTML(c.acceptedBy)}</strong> on ${formatDateTime(c.acceptedAt || c.updatedAt)}</span>
        </div>
      ` : ''}
    </div>

    ${studentPhotosHtml}
    ${declineHtml}
    ${evidenceHtml}
  `;
}

// ===================================================================
// SLA 7-Day Countdown Timer Controller & Renderer
// Professional operations countdown replacing decorative cards
// ===================================================================
let activeSlaTimerInterval = null;
let activeSlaTicketId = null;

function stopActiveSlaCountdownTimer() {
  if (activeSlaTimerInterval) {
    clearInterval(activeSlaTimerInterval);
    activeSlaTimerInterval = null;
  }
  activeSlaTicketId = null;
}

function renderSlaCountdownSection(c, options = {}) {
  if (!c) return "";

  const normStatus = String(c.statusCode || c.status || "").toUpperCase().replace(/\s+/g, "_");
  const containerId = `slaCountdownContainer_${escapeHTML(c.ticketId || 'default')}`;

  // 1. Before acceptance (SUBMITTED or unaccepted)
  if (normStatus === "SUBMITTED" || (!c.acceptedAt && normStatus !== "AWAITING_VERIFICATION" && normStatus !== "CLOSED" && normStatus !== "RESOLVED")) {
    return `
      <div id="${containerId}" class="sla-deadline-panel sla-state-pending" data-ticket-id="${escapeHTML(c.ticketId || '')}">
        <div class="sla-panel-header">
          <div class="sla-panel-title-group">
            <span class="sla-panel-icon">${getIconSvg("clock", { size: 17 })}</span>
            <span class="sla-panel-title">Department Resolution SLA (7 Calendar Days)</span>
          </div>
          <span class="sla-panel-badge badge-pending">Awaiting Staff Acceptance</span>
        </div>
        <div class="sla-pending-content">
          <span class="sla-pending-icon">${getIconSvg("clock-arrow", { size: 16 })}</span>
          <span>Countdown timer activates automatically when maintenance staff accepts this ticket.</span>
        </div>
      </div>
    `;
  }

  // 2. Submitted for Student Verification (countdown stopped!)
  if (normStatus === "AWAITING_VERIFICATION" || (c.verificationRequestedAt && normStatus !== "IN_PROGRESS" && normStatus !== "UNDER_REVIEW")) {
    const verifiedDateStr = formatDateTime(c.verificationRequestedAt || c.updatedAt);
    return `
      <div id="${containerId}" class="sla-deadline-panel sla-state-completed" data-ticket-id="${escapeHTML(c.ticketId || '')}">
        <div class="sla-panel-header">
          <div class="sla-panel-title-group">
            <span class="sla-panel-icon text-success">${getIconSvg("check-circle-2", { size: 18 })}</span>
            <span class="sla-panel-title">Department SLA Window Stopped</span>
          </div>
          <span class="sla-panel-badge badge-verified">Evidence Submitted</span>
        </div>
        <div class="sla-completed-body">
          <div class="sla-completed-title">Submitted for Student Verification on ${verifiedDateStr}</div>
          <div class="sla-completed-desc">Repair evidence has been recorded. The staff countdown is frozen pending student confirmation or rework request.</div>
        </div>
      </div>
    `;
  }

  // 3. Closed / Resolved
  if (normStatus === "CLOSED" || normStatus === "RESOLVED") {
    const closedDateStr = formatDateTime(c.resolvedAt || c.updatedAt);
    return `
      <div id="${containerId}" class="sla-deadline-panel sla-state-closed" data-ticket-id="${escapeHTML(c.ticketId || '')}">
        <div class="sla-panel-header">
          <div class="sla-panel-title-group">
            <span class="sla-panel-icon text-success">${getIconSvg("check-circle-2", { size: 18 })}</span>
            <span class="sla-panel-title">Ticket Resolved &amp; Closed</span>
          </div>
          <span class="sla-panel-badge badge-closed">SLA Completed</span>
        </div>
        <div class="sla-closed-text">
          Repair verified and finalized on <strong>${closedDateStr}</strong>. Department SLA successfully met.
        </div>
      </div>
    `;
  }

  // 4. Declined
  if (normStatus === "DECLINED") {
    return `
      <div id="${containerId}" class="sla-deadline-panel sla-state-closed" data-ticket-id="${escapeHTML(c.ticketId || '')}">
        <div class="sla-panel-header">
          <div class="sla-panel-title-group">
            <span class="sla-panel-icon text-danger">${getIconSvg("x-circle", { size: 18 })}</span>
            <span class="sla-panel-title">Ticket Declined</span>
          </div>
          <span class="sla-panel-badge badge-declined">Closed</span>
        </div>
        <div class="sla-closed-text">
          Ticket was declined by department. No active countdown timer.
        </div>
      </div>
    `;
  }

  // 5. Active Work / Rework Period: UNDER_REVIEW or IN_PROGRESS
  const isRework = !!c.reworkStartedAt;
  const deadlineStr = c.workDeadline ? formatDateTime(c.workDeadline) : "7 Days after acceptance";

  let escalationAlertHtml = "";
  if (c.escalatedToAdmin) {
    escalationAlertHtml = `
      <div class="sla-escalation-banner">
        <span class="sla-escalation-icon">${getIconSvg("alert-triangle", { size: 18 })}</span>
        <div>
          <strong>Escalated to Administration:</strong> ${escapeHTML(c.escalationReason || '7-day resolution deadline passed without verification submission.')}
          ${c.escalatedAt ? ` &bull; Escalated on ${formatDateTime(c.escalatedAt)}` : ''}
        </div>
      </div>
    `;
  }

  return `
    <div id="${containerId}" class="sla-deadline-panel sla-state-active" data-ticket-id="${escapeHTML(c.ticketId || '')}" data-deadline="${escapeHTML(c.workDeadline || '')}">
      <div class="sla-panel-header">
        <div class="sla-panel-title-group">
          <span class="sla-panel-icon" id="${containerId}_icon">${getIconSvg("clock", { size: 17 })}</span>
          <span class="sla-panel-title">Time Remaining to Submit Verification</span>
        </div>
        <span class="sla-panel-badge" id="${containerId}_badge">${isRework ? 'Rework SLA' : '7-Day SLA'}</span>
      </div>

      <div class="sla-countdown-strip" id="${containerId}_countdown">
        <div class="sla-countdown-digits" id="${containerId}_text">
          Calculating remaining time...
        </div>
      </div>

      ${escalationAlertHtml}

      <div class="sla-meta-footer">
        <span><strong>Deadline:</strong> ${deadlineStr}</span>
        ${c.acceptedAt ? `<span>&bull; <strong>Accepted:</strong> ${formatDateTime(c.acceptedAt)}</span>` : ''}
        ${isRework ? `<span>&bull; <strong>Rework Started:</strong> ${formatDateTime(c.reworkStartedAt)}</span>` : ''}
      </div>
    </div>
  `;
}

function initSlaCountdownTimer(c, containerId) {
  stopActiveSlaCountdownTimer();
  if (!c || !c.ticketId) return;

  const targetId = containerId || `slaCountdownContainer_${c.ticketId}`;
  const container = document.getElementById(targetId);
  if (!container) return;

  const normStatus = String(c.statusCode || c.status || "").toUpperCase().replace(/\s+/g, "_");

  if (normStatus !== "UNDER_REVIEW" && normStatus !== "IN_PROGRESS") {
    return;
  }
  if (c.verificationRequestedAt) {
    return;
  }

  if (!c.workDeadline) return;
  const deadlineDate = new Date(c.workDeadline);
  if (isNaN(deadlineDate.getTime())) return;

  activeSlaTicketId = c.ticketId;

  function updateTicker() {
    const el = document.getElementById(targetId);
    if (!el) {
      stopActiveSlaCountdownTimer();
      return;
    }

    const textEl = document.getElementById(`${targetId}_text`);
    const badgeEl = document.getElementById(`${targetId}_badge`);
    const iconEl = document.getElementById(`${targetId}_icon`);
    if (!textEl) return;

    const now = Date.now();
    const diff = deadlineDate.getTime() - now;

    if (diff <= 0) {
      const overdueMs = Math.abs(diff);
      const days = Math.floor(overdueMs / (1000 * 60 * 60 * 24));
      const hours = Math.floor((overdueMs % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
      const minutes = Math.floor((overdueMs % (1000 * 60 * 60)) / (1000 * 60));
      const seconds = Math.floor((overdueMs % (1000 * 60)) / 1000);

      el.classList.remove("sla-state-active", "sla-state-warning");
      el.classList.add("sla-state-overdue");
      if (badgeEl) {
        badgeEl.textContent = "OVERDUE";
        badgeEl.className = "sla-panel-badge badge-overdue";
      }
      if (iconEl) {
        iconEl.innerHTML = getIconSvg("alert-triangle", { size: 18, className: "text-danger" });
      }

      textEl.innerHTML = `
        <span class="sla-overdue-tag">OVERDUE</span>
        <span>${days}d ${hours}h ${minutes}m ${seconds}s</span>
      `;
    } else {
      const days = Math.floor(diff / (1000 * 60 * 60 * 24));
      const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
      const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
      const seconds = Math.floor((diff % (1000 * 60)) / 1000);

      if (diff <= 48 * 60 * 60 * 1000) {
        el.classList.remove("sla-state-active", "sla-state-overdue");
        el.classList.add("sla-state-warning");
        if (badgeEl) {
          badgeEl.textContent = "Approaching Deadline";
          badgeEl.className = "sla-panel-badge badge-warning";
        }
        if (iconEl) {
          iconEl.innerHTML = getIconSvg("clock", { size: 18, className: "text-warning" });
        }
      } else {
        el.classList.remove("sla-state-warning", "sla-state-overdue");
        el.classList.add("sla-state-active");
        if (badgeEl) {
          badgeEl.textContent = c.reworkStartedAt ? "Rework SLA" : "7-Day SLA";
          badgeEl.className = "sla-panel-badge badge-active";
        }
        if (iconEl) {
          iconEl.innerHTML = getIconSvg("clock", { size: 18 });
        }
      }

      textEl.textContent = `${days} Days  ${hours} Hours  ${minutes} Mins  ${seconds} Secs`;
    }
  }

  updateTicker();
  activeSlaTimerInterval = setInterval(updateTicker, 1000);
}

// ===================================================================
// Image Viewer & Error Handler
// ===================================================================
function handleStudentImageError(imgEl, originalUrl) {
  if (!imgEl) return;
  imgEl.onerror = null;
  const parent = imgEl.closest(".evidence-thumb-card");
  if (parent) {
    parent.classList.add("image-load-failed");
    parent.style.cursor = "default";
    parent.onclick = null;
    parent.onkeydown = null;
    parent.removeAttribute("role");
    parent.removeAttribute("tabindex");
    parent.innerHTML = `
      <div class="image-fail-box">
        ${getIconSvg("alert-triangle", { size: 20, className: "text-danger" })}
        <span class="image-fail-text">Image Unavailable</span>
      </div>
    `;
    parent.title = "Failed to load image from server.";
  }
}

function viewSharedModalImage(imgUrl) {
  let viewerModal = document.getElementById("sharedEvidenceViewerModal");
  if (!viewerModal) {
    viewerModal = document.createElement("div");
    viewerModal.className = "modal-overlay";
    viewerModal.id = "sharedEvidenceViewerModal";
    viewerModal.style.zIndex = "1050";
    viewerModal.innerHTML = `
      <div class="modal modal-image-viewer" style="max-width: 860px; padding: 1.25rem; background: #ffffff; border-radius: var(--radius-lg); box-shadow: var(--shadow-lg);">
        <div class="modal-header" style="padding-bottom: 10px; margin-bottom: 12px; border-bottom: 1px solid var(--border-color); display: flex; align-items: center; justify-content: space-between;">
          <div class="modal-title" style="display: flex; align-items: center; gap: 8px; font-weight: 600; font-size: 1rem;">
            ${getIconSvg("image", { size: 18 })}
            <span>Photographic Evidence Preview</span>
          </div>
          <button type="button" class="btn-close-modal" onclick="closeModal('sharedEvidenceViewerModal')" aria-label="Close image preview">
            ${getIconSvg("x", { size: 18 })}
          </button>
        </div>
        <div class="image-viewer-frame" style="display: flex; justify-content: center; align-items: center; min-height: 280px; background: #f8fafc; border: 1px solid var(--border-color); border-radius: var(--radius-md); padding: 12px; overflow: hidden;">
          <img id="sharedViewerImg" src="" alt="Proof Preview" style="max-width: 100%; max-height: 75vh; border-radius: 4px; object-fit: contain;" onerror="this.style.display='none'; document.getElementById('sharedViewerError').style.display='block';" />
          <div id="sharedViewerError" style="display: none; padding: 2rem; text-align: center; color: var(--color-danger);">
            <div style="margin-bottom: 8px;">${getIconSvg("alert-triangle", { size: 32, className: "text-danger" })}</div>
            <div style="font-weight: 600; font-size: 0.95rem;">Failed to load full-size image</div>
            <div style="font-size: 0.8rem; color: var(--color-text-muted); margin-top: 4px;">The image resource could not be retrieved from the server.</div>
          </div>
        </div>
        <div class="modal-footer" style="margin-top: 12px; padding-top: 10px; border-top: 1px solid var(--border-color); display: flex; justify-content: flex-end;">
          <button type="button" class="btn btn-secondary btn-sm" onclick="closeModal('sharedEvidenceViewerModal')">Close Preview</button>
        </div>
      </div>
    `;
    document.body.appendChild(viewerModal);
    viewerModal.addEventListener("click", (e) => {
      if (e.target === viewerModal) {
        closeModal("sharedEvidenceViewerModal");
      }
    });
  }
  const imgEl = document.getElementById("sharedViewerImg");
  const errEl = document.getElementById("sharedViewerError");
  if (errEl) errEl.style.display = "none";
  if (imgEl) {
    imgEl.style.display = "block";
    imgEl.src = imgUrl;
  }
  openModal("sharedEvidenceViewerModal");
}
