const API_BASE = '/api';

export function getToken() {
  return localStorage.getItem('token');
}

export function setToken(token) {
  localStorage.setItem('token', token);
}

export function clearToken() {
  localStorage.removeItem('token');
}

async function request(path, options = {}) {
  const headers = options.headers || {};
  const token = getToken();
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
    headers['X-Forwarded-Authorization'] = `Bearer ${token}`;
  }
  if (options.body && !headers['Content-Type']) {
    headers['Content-Type'] = 'application/json';
  }

  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers
  });

  if (!response.ok) {
    const text = await response.text();
    let message = text;
    if (!message) {
      if (response.status === 401) {
        message = 'Giris gerekli';
      } else if (response.status === 403) {
        message = 'Yetkisiz';
      } else {
        message = 'Islem yapilamadi';
      }
    }
    throw new Error(message);
  }
  if (response.status === 204) {
    return null;
  }
  return response.json();
}

export const api = {
  register: (payload) => request('/auth/register', { method: 'POST', body: JSON.stringify(payload) }),
  login: (payload) => request('/auth/login', { method: 'POST', body: JSON.stringify(payload) }),
  listProducts: () => request('/products'),
  getCart: () => request('/cart'),
  addToCart: (payload) => request('/cart/items', { method: 'POST', body: JSON.stringify(payload) }),
  updateCartItem: (productId, payload) => request(`/cart/items/${productId}`, { method: 'PATCH', body: JSON.stringify(payload) }),
  removeCartItem: (productId) => request(`/cart/items/${productId}`, { method: 'DELETE' }),
  checkout: () => request('/checkout', { method: 'POST' }),
  listOrders: () => request('/orders'),
  payOrder: (orderId, payload) => request(`/orders/${orderId}/pay`, { method: 'POST', body: JSON.stringify(payload) }),
  cancelOrder: (orderId) => request(`/orders/${orderId}/cancel`, { method: 'POST' }),
  shipOrder: (orderId) => request(`/orders/${orderId}/ship`, { method: 'POST' })
};
