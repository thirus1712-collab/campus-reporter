/**
 * Smart Campus Infrastructure Problem Reporter
 * Central API Client Wrapper (Spring Boot REST Communication)
 *
 * Base URL: https://campus-reporter-backend.onrender.com/api
 */

const API_CONFIG = {
  baseUrl: "https://campus-reporter-backend.onrender.com/api",
  timeoutMs: 45000
};

const apiClient = {
  /**
   * Helper to construct auth headers with Bearer token
   */
  getAuthHeaders() {
    const headers = {
      "Content-Type": "application/json",
      "Accept": "application/json"
    };

    if (typeof authManager !== "undefined") {
      const token = authManager.getToken();
      if (token) {
        headers["Authorization"] = `Bearer ${token}`;
      }
      const user = authManager.getCurrentUser();
      if (user) {
        if (user.email) headers["X-User-Email"] = user.email;
        if (user.role) headers["X-User-Role"] = user.role;
        if (user.department) headers["X-User-Dept"] = user.department;
      }
    }
    return headers;
  },

  /**
   * Generic request helper with timeout and standardized error handling
   */
  async request(endpoint, options = {}) {
    const url = `${API_CONFIG.baseUrl}${endpoint}`;
    const timeoutMs = options.timeoutMs || API_CONFIG.timeoutMs;
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), timeoutMs);

    try {
      const defaultHeaders = this.getAuthHeaders();
      if (options.body instanceof FormData) {
        delete defaultHeaders["Content-Type"];
      }
      const customHeaders = options.headers || {};

      const response = await fetch(url, {
        ...options,
        signal: controller.signal,
        headers: { ...defaultHeaders, ...customHeaders }
      });

      clearTimeout(timeoutId);

      const contentType = response.headers.get("content-type") || "";
      let data = null;
      if (contentType.includes("application/json")) {
        data = await response.json();
      } else {
        const text = await response.text();
        try {
          data = JSON.parse(text);
        } catch {
          data = { message: text };
        }
      }

      if (!response.ok) {
        if (response.status === 401) {
          console.warn("Session expired or unauthorized request.");
        }
        const errMessage = (data && (data.message || data.error)) || `HTTP Error ${response.status}: ${response.statusText}`;
        const error = new Error(errMessage);
        error.status = response.status;
        error.statusText = response.statusText;
        error.data = data;
        throw error;
      }

      return data;

    } catch (err) {
      clearTimeout(timeoutId);
      if (err.name === "AbortError") {
        const timeoutErr = new Error("Server request timed out. Please check your network connection and retry.");
        timeoutErr.isNetworkError = true;
        throw timeoutErr;
      }
      if (err.message === "Failed to fetch" || err instanceof TypeError) {
        const netErr = new Error("Unable to connect to the backend server (Failed to fetch). Please ensure the Spring Boot backend is running on http://localhost:8080.");
        netErr.isNetworkError = true;
        throw netErr;
      }
      throw err;
    }
  },

  /**
   * Check backend health
   * GET /api/health
   */
  async checkHealth() {
    try {
      return await this.request("/health", { method: "GET" });
    } catch (err) {
      return {
        status: "DOWN",
        error: err.message,
        tip: "Ensure Spring Boot is running on port 8080 (./gradlew.bat bootRun)"
      };
    }
  },

  /**
   * Authenticate user with email and password
   * POST /api/auth/login
   */
  async login(email, password) {
    return await this.request("/auth/login", {
      method: "POST",
      body: JSON.stringify({ email, password })
    });
  },

  /**
   * Get authenticated user profile
   * GET /api/auth/me
   */
  async getMe() {
    return await this.request("/auth/me", { method: "GET" });
  },

  /**
   * Log out session
   * POST /api/auth/logout
   */
  async logout() {
    return await this.request("/auth/logout", { method: "POST" });
  },

  /**
   * Submit Complaint to Backend Database
   * Supports both multipart/form-data (with file uploads) and JSON payloads
   * POST /api/complaints
   */
  async submitComplaint(data) {
    const isFormData = typeof FormData !== "undefined" && data instanceof FormData;
    return await this.request("/complaints", {
      method: "POST",
      body: isFormData ? data : JSON.stringify(data)
    });
  },

  /**
   * Retrieve the logged-in student's complaints
   * GET /api/complaints/mine
   */
  async getMyComplaints() {
    return await this.request("/complaints/mine", { method: "GET" });
  },

  /**
   * Retrieve complaints assigned to a department
   * GET /api/complaints/department?dept={dept}
   */
  async getDepartmentComplaints(departmentCode) {
    const query = departmentCode ? `?dept=${encodeURIComponent(departmentCode)}` : "";
    return await this.request(`/complaints/department${query}`, { method: "GET" });
  },

  /**
   * Get all complaints across campus (Admin)
   * GET /api/complaints/all[?status={status}]
   */
  async getAllComplaintsAdmin(status = "") {
    const query = (status && status !== "ALL") ? `?status=${encodeURIComponent(status)}` : "";
    return await this.request(`/complaints/all${query}`, { method: "GET" });
  },

  /**
   * Get specific complaint details
   * GET /api/complaints/{ticketId}
   */
  async getComplaint(ticketId) {
    return await this.request(`/complaints/${encodeURIComponent(ticketId)}`, { method: "GET" });
  },

  /**
   * Department accepts complaint -> Under Review
   * PATCH /api/complaints/{ticketId}/accept
   */
  async acceptComplaint(ticketId, actionData = {}) {
    return await this.request(`/complaints/${encodeURIComponent(ticketId)}/accept`, {
      method: "PATCH",
      body: JSON.stringify(actionData)
    });
  },

  /**
   * Department begins physical repair -> In Progress
   * PATCH /api/complaints/{ticketId}/progress
   */
  async startWork(ticketId, actionData = {}) {
    return await this.request(`/complaints/${encodeURIComponent(ticketId)}/progress`, {
      method: "PATCH",
      body: JSON.stringify(actionData)
    });
  },

  /**
   * Department marks repair complete with multipart evidence images & optional acknowledgement
   * POST /api/complaints/{ticketId}/complete
   */
  async completeComplaint(ticketId, formData) {
    return await this.request(`/complaints/${encodeURIComponent(ticketId)}/complete`, {
      method: "POST",
      body: formData,
      timeoutMs: 60000
    });
  },

  /**
   * Helper to resolve relative file URLs to absolute backend URLs with auth token
   */
  getFileUrl(path) {
    if (!path) return "";
    let fullUrl = path;
    if (!path.startsWith("http://") && !path.startsWith("https://") && !path.startsWith("data:") && !path.startsWith("blob:")) {
      const host = API_CONFIG.baseUrl.replace(/\/api$/, "");
      fullUrl = `${host}${path.startsWith("/") ? "" : "/"}${path}`;
    }
    if (typeof authManager !== "undefined") {
      const token = authManager.getToken();
      if (token && !fullUrl.includes("token=")) {
        const sep = fullUrl.includes("?") ? "&" : "?";
        fullUrl = `${fullUrl}${sep}token=${encodeURIComponent(token)}`;
      }
    }
    return fullUrl;
  },

  /**
   * Student verifies repair -> Closed
   * PATCH /api/complaints/{ticketId}/verify
   */
  async verifyResolution(ticketId, feedback = "") {
    return await this.request(`/complaints/${encodeURIComponent(ticketId)}/verify`, {
      method: "PATCH",
      body: JSON.stringify({ feedback, notes: "Verified by student" })
    });
  },

  /**
   * Student disputes repair -> returns to In Progress
   * PATCH /api/complaints/{ticketId}/dispute
   */
  async disputeResolution(ticketId, reason = "") {
    return await this.request(`/complaints/${encodeURIComponent(ticketId)}/dispute`, {
      method: "PATCH",
      body: JSON.stringify({ reason })
    });
  },

  /**
   * Department declines complaint with mandatory reason
   * PATCH /api/complaints/{ticketId}/decline
   */
  async declineComplaint(ticketId, reason, staffName = "") {
    return await this.request(`/complaints/${encodeURIComponent(ticketId)}/decline`, {
      method: "PATCH",
      body: JSON.stringify({ reason, staffName })
    });
  },

  /**
   * Student metrics
   * GET /api/complaints/metrics/student
   */
  async getStudentMetrics() {
    return await this.request("/complaints/metrics/student", { method: "GET" });
  },

  /**
   * Department metrics
   * GET /api/complaints/metrics/department?dept={dept}
   */
  async getDepartmentMetrics(dept) {
    return await this.request(`/complaints/metrics/department?dept=${encodeURIComponent(dept)}`, { method: "GET" });
  },

  /**
   * Admin executive metrics
   * GET /api/complaints/metrics/admin
   */
  async getAdminMetrics() {
    return await this.request("/complaints/metrics/admin", { method: "GET" });
  },

  /**
   * List all user accounts (Admin)
   * GET /api/admin/users
   */
  async getAdminUsers() {
    return await this.request("/admin/users", { method: "GET" });
  },

  /**
   * Create new user account with secure password hashing (Admin)
   * POST /api/admin/users
   */
  async createAdminUser(userData) {
    return await this.request("/admin/users", {
      method: "POST",
      body: JSON.stringify(userData)
    });
  },

  /**
   * Deactivate a user account (Admin)
   * PATCH /api/admin/users/{userId}/deactivate
   */
  async deactivateAdminUser(userId) {
    return await this.request(`/admin/users/${encodeURIComponent(userId)}/deactivate`, {
      method: "PATCH"
    });
  }
};

