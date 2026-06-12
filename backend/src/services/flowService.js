const crypto = require('crypto');
const axios = require('axios');

const FLOW_API_URL = process.env.FLOW_API_URL || 'https://sandbox.flow.cl/api';
const FLOW_API_KEY = process.env.FLOW_API_KEY || '';
const FLOW_SECRET_KEY = process.env.FLOW_SECRET_KEY || '';

function sign(params) {
  const keys = Object.keys(params).sort();
  const toSign = keys.map(k => `${k}${params[k]}`).join('');
  return crypto.createHmac('sha256', FLOW_SECRET_KEY).update(toSign).digest('hex');
}

async function createPayment({ commerceOrder, subject, amount, email, urlConfirmation, urlReturn }) {
  if (!FLOW_API_KEY || FLOW_API_KEY === 'TU_API_KEY_AQUI') {
    throw new Error('FLOW_API_KEY no configurada en .env');
  }
  if (!FLOW_SECRET_KEY || FLOW_SECRET_KEY === 'TU_SECRET_KEY_AQUI') {
    throw new Error('FLOW_SECRET_KEY no configurada en .env');
  }

  const params = {
    apiKey: FLOW_API_KEY,
    commerceOrder: String(commerceOrder),
    subject: String(subject).substring(0, 200),
    currency: 'CLP',
    amount: Math.round(amount),
    email,
    paymentMethod: '9',
    urlConfirmation,
    urlReturn
  };
  params.s = sign(params);

  const formData = new URLSearchParams(params).toString();
  let response;
  try {
    response = await axios.post(`${FLOW_API_URL}/payment/create`, formData, {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
    });
  } catch (err) {
    const status = err.response?.status;
    const body = JSON.stringify(err.response?.data);
    console.error(`[Flow] Error HTTP ${status}: ${body}`);
    throw new Error(`Flow respondió ${status}: ${body}`);
  }

  const { url, token } = response.data;
  return { paymentUrl: `${url}?token=${token}`, token };
}

async function getPaymentStatus(token) {
  const params = { apiKey: FLOW_API_KEY, token };
  params.s = sign(params);

  const response = await axios.get(`${FLOW_API_URL}/payment/getStatus`, { params });
  return response.data;
}

module.exports = { createPayment, getPaymentStatus };
