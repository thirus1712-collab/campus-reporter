/**
 * Smart Campus Infrastructure Problem Reporter
 * Production Authentication & Role-Based Access Control (RBAC) Client
 */

const ROLES = {
  STUDENT: {
    id: "STUDENT",
    label: "Student",
    portalUrl: "student/index.html",
    badgeClass: "role-student"
  },
  ELECTRICAL: {
    id: "ELECTRICAL",
    label: "Electrical Department",
    portalUrl: "department/index.html?dept=ELECTRICAL",
    badgeClass: "role-electrical"
  },
  PLUMBING: {
    id: "PLUMBING",
    label: "Plumbing Department",
    portalUrl: "department/index.html?dept=PLUMBING",
    badgeClass: "role-plumbing"
  },
  CLEANING: {
    id: "CLEANING",
    label: "Cleaning Department",
    portalUrl: "department/index.html?dept=CLEANING",
    badgeClass: "role-cleaning"
  },
  IT_SUPPORT: {
    id: "IT_SUPPORT",
    label: "IT & Technical Support",
    portalUrl: "department/index.html?dept=IT_SUPPORT",
    badgeClass: "role-it"
  },
  ADMIN: {
    id: "ADMIN",
    label: "Campus Administration",
    portalUrl: "admin/index.html",
    badgeClass: "role-admin"
  }
};

const authManager = {
  STORAGE_KEY: "campus_reporter_auth_session",

  /**
   * Get active authenticated user session from localStorage
   */
  getCurrentUser() {
    try {
      const data = localStorage.getItem(this.STORAGE_KEY) || sessionStorage.getItem(this.STORAGE_KEY);
      return data ? JSON.parse(data) : null;
    } catch (e) {
      return null;
    }
  },

  /**
   * Get current auth bearer token
   */
  getToken() {
    const user = this.getCurrentUser();
    return user ? user.token : null;
  },

  /**
   * Save session data
   */
  saveSession(sessionData, remember = true) {
    const storage = remember ? localStorage : sessionStorage;
    storage.setItem(this.STORAGE_KEY, JSON.stringify(sessionData));
  },

  /**
   * Log out user, invalidate server session and redirect
   */
  async logout(redirectTarget = "login.html") {
    try {
      if (typeof apiClient !== "undefined" && apiClient.logout) {
        await apiClient.logout();
      }
    } catch (ignored) {}
    localStorage.removeItem(this.STORAGE_KEY);
    sessionStorage.removeItem(this.STORAGE_KEY);
    window.location.replace(redirectTarget);
  },

  /**
   * Route Guard: verify user is logged in and authorized for this portal.
   * If unauthorized or not logged in, redirects automatically.
   */
  requireAuth(allowedRoles = [], relativePrefix = "../") {
    const user = this.getCurrentUser();
    const loginPage = `${relativePrefix}login.html`;

    if (!user || !user.token) {
      window.location.replace(loginPage);
      return null;
    }

    if (allowedRoles && allowedRoles.length > 0) {
      const isAllowed = allowedRoles.includes(user.role);
      if (!isAllowed) {
        // Redirect to their own authorized portal
        const targetUrl = relativePrefix + (user.portalUrl || (ROLES[user.role] ? ROLES[user.role].portalUrl : "student/index.html"));
        window.location.replace(targetUrl);
        return null;
      }
    }

    // Protect against browser Back/Forward cache (bfcache)
    if (!window.__authBfCacheGuarded) {
      window.__authBfCacheGuarded = true;
      window.addEventListener("pageshow", (event) => {
        if (event.persisted) {
          const recheckUser = this.getCurrentUser();
          if (!recheckUser || !recheckUser.token) {
            window.location.replace(loginPage);
          }
        }
      });
    }

    return user;
  },

  /**
   * Get active department code for department portal
   */
  getActiveDepartment() {
    const params = new URLSearchParams(window.location.search);
    const dept = params.get("dept");
    if (dept && ROLES[dept]) return dept;

    const user = this.getCurrentUser();
    if (user && user.department) {
      return user.department;
    }
    if (user && ["ELECTRICAL", "PLUMBING", "CLEANING", "IT_SUPPORT"].includes(user.role)) {
      return user.role;
    }
    return "ELECTRICAL";
  }
};
