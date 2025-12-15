const API_BASE = '/api';

let authToken = null;

export function setAuthToken(token) {
  authToken = token;
  if (token) {
    localStorage.setItem('authToken', token);
  } else {
    localStorage.removeItem('authToken');
  }
}

export function getAuthToken() {
  if (!authToken) {
    authToken = localStorage.getItem('authToken');
  }
  return authToken;
}

function getHeaders() {
  const headers = {
    'Content-Type': 'application/json',
  };
  const token = getAuthToken();
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  return headers;
}

async function request(endpoint, options = {}) {
  const url = `${API_BASE}${endpoint}`;
  const config = {
    ...options,
    headers: {
      ...getHeaders(),
      ...options.headers,
    },
  };

  try {
    const response = await fetch(url, config);
    const data = await response.json();
    
    if (!response.ok) {
      throw new Error(data.error || 'Request failed');
    }
    
    return data;
  } catch (error) {
    throw error;
  }
}

export const api = {
  // Auth
  async register(username, email, password) {
    return request('/auth/register', {
      method: 'POST',
      body: JSON.stringify({ username, email, password }),
    });
  },

  async login(username, password) {
    const result = await request('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ username, password }),
    });
    if (result.token) {
      setAuthToken(result.token);
    }
    return result;
  },

  async getMe() {
    return request('/me');
  },

  // Todos
  async getTodos() {
    return request('/todos');
  },

  async createTodo(title, description = '') {
    return request('/todos', {
      method: 'POST',
      body: JSON.stringify({ title, description }),
    });
  },

  async updateTodo(id, updates) {
    return request(`/todos/${id}`, {
      method: 'PUT',
      body: JSON.stringify(updates),
    });
  },

  async deleteTodo(id) {
    return request(`/todos/${id}`, {
      method: 'DELETE',
    });
  },
};

