import React, { useEffect, useState } from 'react';
import { api, clearToken, getToken, setToken } from './api.js';

const emptyRegister = { email: '', password: '', name: '' };
const emptyLogin = { email: '', password: '' };

export default function App() {
  const [token, setTokenState] = useState(getToken());
  const [products, setProducts] = useState([]);
  const [cart, setCart] = useState({ items: [], totalAmount: 0 });
  const [orders, setOrders] = useState([]);
  const [registerForm, setRegisterForm] = useState(emptyRegister);
  const [loginForm, setLoginForm] = useState(emptyLogin);
  const [error, setError] = useState('');
  const [view, setView] = useState('login');
  const [showPayment, setShowPayment] = useState(false);
  const [paymentOrder, setPaymentOrder] = useState(null);
  const [paymentForm, setPaymentForm] = useState({ cardNumber: '', cvc: '' });
  const [paymentError, setPaymentError] = useState('');

  useEffect(() => {
    api.listProducts().then(setProducts).catch(() => {});
  }, []);

  useEffect(() => {
    if (!token) {
      return;
    }
    api.getCart().then(setCart).catch((err) => setError(err.message));
    api.listOrders().then(setOrders).catch(() => {});
  }, [token]);

  const onRegister = async (e) => {
    e.preventDefault();
    setError('');
    try {
      const result = await api.register(registerForm);
      setToken(result.token);
      setTokenState(result.token);
      setRegisterForm(emptyRegister);
    } catch (err) {
      setError(err.message);
    }
  };

  const onLogin = async (e) => {
    e.preventDefault();
    setError('');
    try {
      const result = await api.login(loginForm);
      setToken(result.token);
      setTokenState(result.token);
      setLoginForm(emptyLogin);
    } catch (err) {
      setError(err.message);
    }
  };

  const onLogout = () => {
    clearToken();
    setTokenState('');
    setCart({ items: [], totalAmount: 0 });
    setOrders([]);
  };

  const refreshCart = async () => {
    const data = await api.getCart();
    setCart(data);
  };

  const refreshOrders = async () => {
    const data = await api.listOrders();
    setOrders(data);
  };

  const addToCart = async (productId) => {
    setError('');
    try {
      await api.addToCart({ productId, quantity: 1 });
      await refreshCart();
    } catch (err) {
      setError(err.message);
    }
  };

  const checkout = async () => {
    setError('');
    try {
      await api.checkout();
      await refreshCart();
      await refreshOrders();
      await api.listProducts().then(setProducts);
    } catch (err) {
      setError(err.message);
    }
  };

  const validateCardFormat = () => {
    if (!/^\d{6}$/.test(paymentForm.cardNumber)) {
      setPaymentError('Hatali kart formati');
      return false;
    }
    if (paymentError === 'Hatali kart formati') {
      setPaymentError('');
    }
    return true;
  };

  const payOrder = async (orderId) => {
    setError('');
    setPaymentError('');
    setPaymentOrder(orderId);
    setPaymentForm({ cardNumber: '', cvc: '' });
    setShowPayment(true);
  };

  const submitPayment = async () => {
    setPaymentError('');
    try {
      if (!validateCardFormat()) {
        return;
      }
      if (paymentForm.cardNumber !== '123456') {
        throw new Error('Hatali kart bilgisi');
      }
      if (!/^\d{3}$/.test(paymentForm.cvc)) {
        throw new Error('Hatali cvc numarasi');
      }
      if (paymentForm.cvc !== '789') {
        throw new Error('Hatali cvc numarasi');
      }
      await api.payOrder(paymentOrder, {
        method: 'CARD',
        forceFail: false,
        cardNumber: paymentForm.cardNumber,
        cvc: paymentForm.cvc
      });
      await refreshOrders();
      setShowPayment(false);
    } catch (err) {
      setPaymentError(err.message);
    }
  };

  const cancelOrder = async (orderId) => {
    setError('');
    try {
      await api.cancelOrder(orderId);
      await refreshOrders();
      await api.listProducts().then(setProducts);
    } catch (err) {
      setError(err.message);
    }
  };

  const shipOrder = async (orderId) => {
    setError('');
    try {
      await api.shipOrder(orderId);
      await refreshOrders();
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <div className="app">
      <header>
        <h1>YDG Order Stock</h1>
        {token ? (
          <button onClick={onLogout}>Logout</button>
        ) : null}
      </header>

      {error ? (
        <div className="toast" data-testid="toast-error">{error}</div>
      ) : null}

      {showPayment ? (
        <div className="modal">
          <div className="modal-content">
            <h3>Card Payment</h3>
            {paymentError ? (
              <div className="modal-error" data-testid="payment-error">{paymentError}</div>
            ) : null}
            <label>Card Number</label>
            <input
              data-testid="card-number"
              value={paymentForm.cardNumber}
              onChange={(e) => setPaymentForm({ ...paymentForm, cardNumber: e.target.value })}
              onBlur={validateCardFormat}
            />
            <label>CVC</label>
            <input
              data-testid="card-cvc"
              value={paymentForm.cvc}
              onChange={(e) => setPaymentForm({ ...paymentForm, cvc: e.target.value })}
              onFocus={() => {
                validateCardFormat();
              }}
            />
            <div className="modal-actions">
              <button data-testid="pay-confirm" onClick={submitPayment}>Pay</button>
              <button onClick={() => setShowPayment(false)}>Close</button>
            </div>
          </div>
        </div>
      ) : null}

      {!token ? (
        <section className="auth">
          <div className="tabs">
            <button className={view === 'login' ? 'active' : ''} onClick={() => setView('login')}>Login</button>
            <button className={view === 'register' ? 'active' : ''} onClick={() => setView('register')}>Register</button>
          </div>

          {view === 'login' ? (
            <form onSubmit={onLogin} className="card">
              <label>Email</label>
              <input
                data-testid="login-email"
                value={loginForm.email}
                onChange={(e) => setLoginForm({ ...loginForm, email: e.target.value })}
              />
              <label>Password</label>
              <input
                type="password"
                data-testid="login-password"
                value={loginForm.password}
                onChange={(e) => setLoginForm({ ...loginForm, password: e.target.value })}
              />
              <button type="submit" data-testid="login-submit">Login</button>
            </form>
          ) : (
            <form onSubmit={onRegister} className="card">
              <label>Name</label>
              <input
                data-testid="register-name"
                value={registerForm.name}
                onChange={(e) => setRegisterForm({ ...registerForm, name: e.target.value })}
              />
              <label>Email</label>
              <input
                data-testid="register-email"
                value={registerForm.email}
                onChange={(e) => setRegisterForm({ ...registerForm, email: e.target.value })}
              />
              <label>Password</label>
              <input
                type="password"
                data-testid="register-password"
                value={registerForm.password}
                onChange={(e) => setRegisterForm({ ...registerForm, password: e.target.value })}
              />
              <button type="submit" data-testid="register-submit">Register</button>
            </form>
          )}
        </section>
      ) : (
        <main className="grid">
          <section className="card">
            <h2>Products</h2>
            <ul>
              {products.map((product) => (
                <li key={product.id}>
                  <div>
                    <strong>{product.title}</strong>
                    <div>Price: {product.price}</div>
                    <div>Stock: {product.stockQuantity}</div>
                  </div>
                  <button
                    data-testid={`product-add-to-cart-${product.id}`}
                    onClick={() => addToCart(product.id)}
                  >
                    Add to cart
                  </button>
                </li>
              ))}
            </ul>
          </section>

          <section className="card">
            <h2>Cart</h2>
            {cart.items.length === 0 ? (
              <p>Cart is empty.</p>
            ) : (
              <ul>
                {cart.items.map((item) => (
                  <li key={item.productId}>
                    <div>
                      {item.title} x {item.quantity} = {item.price}
                    </div>
                    <button
                      data-testid={`cart-remove-${item.productId}`}
                      onClick={async () => {
                        try {
                          await api.removeCartItem(item.productId);
                          await refreshCart();
                        } catch (err) {
                          setError(err.message);
                        }
                      }}
                    >
                      Remove
                    </button>
                  </li>
                ))}
              </ul>
            )}
            <div className="total">Total: {cart.totalAmount}</div>
            <button data-testid="cart-checkout" onClick={checkout} disabled={!cart.items.length}>
              Checkout
            </button>
          </section>

          <section className="card">
            <h2>Orders</h2>
            {orders.length === 0 ? (
              <p>No orders yet.</p>
            ) : (
              <ul>
                {orders.map((order) => (
                  <li key={order.id}>
                    <div>
                      <strong>Order #{order.id}</strong>
                      <div data-testid={`order-status-${order.id}`}>Status: {order.status}</div>
                      <div>Total: {order.totalAmount}</div>
                    </div>
                    <div className="order-actions">
                      {order.status === 'CREATED' ? (
                        <>
                          <button data-testid="pay-submit" onClick={() => payOrder(order.id)}>Pay</button>
                          <button onClick={() => cancelOrder(order.id)}>Cancel</button>
                        </>
                      ) : null}
                      {order.status === 'PAID' ? (
                        <button onClick={() => shipOrder(order.id)}>Ship</button>
                      ) : null}
                    </div>
                  </li>
                ))}
              </ul>
            )}
          </section>
        </main>
      )}
    </div>
  );
}
