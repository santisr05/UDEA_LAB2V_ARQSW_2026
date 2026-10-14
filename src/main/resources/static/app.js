(() => {
  'use strict';

  // Misma URL que sirve la página: no hace falta configurar CORS.
  const API = '/api';

  const money = new Intl.NumberFormat('es-CO', {
    style: 'currency',
    currency: 'COP',
    maximumFractionDigits: 2,
  });

  let customers = [];

  // ---------------------------------------------------------------
  // Utilidades
  // ---------------------------------------------------------------
  const $ = (id) => document.getElementById(id);

  const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

  // Todo texto que viene del usuario se inserta con textContent (nunca innerHTML).
  function cell(text, className) {
    const td = document.createElement('td');
    td.textContent = text;
    if (className) td.className = className;
    return td;
  }

  function emptyRow(tbody, columns, message) {
    const tr = document.createElement('tr');
    const td = cell(message, 'empty');
    td.colSpan = columns;
    tr.appendChild(td);
    tbody.appendChild(tr);
  }

  function setStatus(message, type) {
    const box = $('status');
    box.className = 'status' + (type ? ' ' + type : '');
    if (!message) {
      box.textContent = '';
      return;
    }
    const prefix = type === 'ok' ? 'Listo: ' : type === 'error' ? 'Error: ' : '';
    box.textContent = prefix + message;
  }

  function pad(n) {
    return String(n).padStart(2, '0');
  }

  // Se muestra tal cual la entrega el servidor (sin convertir zonas horarias).
  function formatDateTime(value) {
    if (Array.isArray(value)) {
      const [y, mo, d, h = 0, mi = 0, s = 0] = value;
      return `${y}-${pad(mo)}-${pad(d)} ${pad(h)}:${pad(mi)}:${pad(s)}`;
    }
    if (typeof value === 'string') {
      return value.replace('T', ' ').slice(0, 19);
    }
    return '';
  }

  function customerLabel(customer) {
    return `${customer.accountNumber} - ${customer.firstName} ${customer.lastName}`;
  }

  function findCustomer(accountNumber) {
    return customers.find((c) => c.accountNumber === accountNumber);
  }

  // ---------------------------------------------------------------
  // Acceso a la API
  // ---------------------------------------------------------------
  class ApiError extends Error {
    constructor(status, message) {
      super(message);
      this.status = status;
    }
  }

  function extractMessage(body, status) {
    if (status >= 500) {
      return 'El servidor tuvo un problema. Intenta de nuevo en unos segundos.';
    }
    if (body && typeof body === 'object') {
      return body.detail || body.message || body.title || 'Solicitud no válida (' + status + ').';
    }
    if (typeof body === 'string' && body) {
      return body;
    }
    return status === 404 ? 'No se encontró el recurso.' : 'Solicitud no válida (' + status + ').';
  }

  async function api(path, options = {}) {
    const response = await fetch(API + path, {
      headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
      ...options,
    });
    const text = await response.text();
    let body = null;
    if (text) {
      try {
        body = JSON.parse(text);
      } catch (e) {
        body = text;
      }
    }
    if (!response.ok) {
      throw new ApiError(response.status, extractMessage(body, response.status));
    }
    return body;
  }

  // En Render gratis el servicio puede estar dormido: la primera carga reintenta.
  async function apiWithRetry(path, tries = 8, delayMs = 5000) {
    for (let attempt = 1; ; attempt++) {
      try {
        return await api(path);
      } catch (err) {
        const transient = !(err instanceof ApiError) || [502, 503, 504].includes(err.status);
        if (!transient || attempt >= tries) throw err;
        setStatus(
          `Conectando con el servidor (puede tardar si estaba en reposo). Intento ${attempt} de ${tries}...`,
          'info'
        );
        await sleep(delayMs);
      }
    }
  }

  // ---------------------------------------------------------------
  // Vista 1: clientes
  // ---------------------------------------------------------------
  function renderCustomers() {
    const tbody = $('customers-body');
    tbody.textContent = '';
    if (customers.length === 0) {
      emptyRow(tbody, 4, 'No hay clientes registrados.');
      return;
    }
    customers.forEach((c) => {
      const tr = document.createElement('tr');
      tr.appendChild(cell(c.id));
      tr.appendChild(cell(`${c.firstName} ${c.lastName}`));
      tr.appendChild(cell(c.accountNumber));
      tr.appendChild(cell(money.format(c.balance), 'num'));
      tbody.appendChild(tr);
    });
  }

  function fillSelect(select, placeholder) {
    const previous = select.value;
    select.textContent = '';
    const first = document.createElement('option');
    first.value = '';
    first.textContent = placeholder;
    select.appendChild(first);
    customers.forEach((c) => {
      const option = document.createElement('option');
      option.value = c.accountNumber;
      option.textContent = customerLabel(c);
      select.appendChild(option);
    });
    if (previous && findCustomer(previous)) {
      select.value = previous;
    }
  }

  function updateSenderHint() {
    const customer = findCustomer($('transfer-sender').value);
    $('sender-hint').textContent = customer
      ? 'Saldo disponible: ' + money.format(customer.balance)
      : '';
  }

  async function loadCustomers() {
    customers = await apiWithRetry('/customers');
    renderCustomers();
    fillSelect($('transfer-sender'), 'Selecciona la cuenta de origen');
    fillSelect($('transfer-receiver'), 'Selecciona la cuenta de destino');
    fillSelect($('history-account'), 'Selecciona una cuenta');
    updateSenderHint();
  }

  async function onCreateCustomer(event) {
    event.preventDefault();
    const form = event.target;
    const button = form.querySelector('button[type="submit"]');
    const payload = {
      firstName: form.elements.firstName.value.trim(),
      lastName: form.elements.lastName.value.trim(),
      accountNumber: form.elements.accountNumber.value.trim(),
      balance: Number(form.elements.balance.value),
    };

    if (!payload.firstName || !payload.lastName || !payload.accountNumber) {
      setStatus('Completa nombre, apellido y número de cuenta.', 'error');
      return;
    }
    if (form.elements.balance.value === '' || !Number.isFinite(payload.balance) || payload.balance < 0) {
      setStatus('El saldo inicial debe ser un número mayor o igual a cero.', 'error');
      return;
    }

    button.disabled = true;
    try {
      const created = await api('/customers', { method: 'POST', body: JSON.stringify(payload) });
      setStatus(`Cliente creado: ${created.firstName} ${created.lastName} (cuenta ${created.accountNumber}).`, 'ok');
      form.reset();
      await loadCustomers();
    } catch (err) {
      setStatus(
        err instanceof ApiError ? err.message : 'No se pudo conectar con el servidor.',
        'error'
      );
    } finally {
      button.disabled = false;
    }
  }

  // ---------------------------------------------------------------
  // Vista 2: transferencia
  // ---------------------------------------------------------------
  async function onTransfer(event) {
    event.preventDefault();
    const form = event.target;
    const button = form.querySelector('button[type="submit"]');
    const sender = $('transfer-sender').value;
    const receiver = $('transfer-receiver').value;
    const amount = Number($('transfer-amount').value);

    if (!sender || !receiver) {
      setStatus('Selecciona la cuenta de origen y la de destino.', 'error');
      return;
    }
    if (sender === receiver) {
      setStatus('La cuenta de origen y la de destino no pueden ser la misma.', 'error');
      return;
    }
    if ($('transfer-amount').value === '' || !Number.isFinite(amount) || amount <= 0) {
      setStatus('El monto debe ser mayor que cero.', 'error');
      return;
    }

    button.disabled = true;
    try {
      const result = await api('/transactions', {
        method: 'POST',
        body: JSON.stringify({
          senderAccountNumber: sender,
          receiverAccountNumber: receiver,
          amount,
        }),
      });
      setStatus(
        `Transferencia #${result.id} realizada: ${money.format(result.amount)} de la cuenta ` +
          `${result.senderAccountNumber} a la cuenta ${result.receiverAccountNumber}.`,
        'ok'
      );
      $('transfer-amount').value = '';
      await loadCustomers();
    } catch (err) {
      setStatus(
        err instanceof ApiError ? err.message : 'No se pudo conectar con el servidor.',
        'error'
      );
    } finally {
      button.disabled = false;
    }
  }

  // ---------------------------------------------------------------
  // Vista 3: historial por cliente
  // ---------------------------------------------------------------
  async function loadHistory() {
    const account = $('history-account').value;
    const tbody = $('history-body');
    const caption = $('history-caption');
    tbody.textContent = '';

    if (!account) {
      caption.textContent = 'Selecciona una cuenta para ver sus transacciones';
      return;
    }

    try {
      const list = await api('/transactions/' + encodeURIComponent(account));
      const owner = findCustomer(account);
      caption.textContent = 'Transacciones de la cuenta ' + (owner ? customerLabel(owner) : account);

      if (!list || list.length === 0) {
        emptyRow(tbody, 5, 'Esta cuenta aún no tiene transacciones.');
        return;
      }

      [...list]
        .sort((a, b) => b.id - a.id)
        .forEach((tx) => {
          const sent = tx.senderAccountNumber === account;
          const counterpart = sent ? tx.receiverAccountNumber : tx.senderAccountNumber;
          const known = findCustomer(counterpart);
          const tr = document.createElement('tr');
          tr.appendChild(cell(tx.id));
          tr.appendChild(cell(formatDateTime(tx.timestamp)));
          tr.appendChild(cell(sent ? 'Enviada' : 'Recibida'));
          tr.appendChild(cell(known ? customerLabel(known) : counterpart));
          tr.appendChild(cell((sent ? '- ' : '+ ') + money.format(tx.amount), 'num'));
          tbody.appendChild(tr);
        });
    } catch (err) {
      setStatus(
        err instanceof ApiError ? err.message : 'No se pudo conectar con el servidor.',
        'error'
      );
    }
  }

  // ---------------------------------------------------------------
  // Pestañas (accesibles: roles ARIA y navegación con flechas)
  // ---------------------------------------------------------------
  function setupTabs() {
    const tabs = Array.from(document.querySelectorAll('[role="tab"]'));

    function select(tab) {
      tabs.forEach((t) => {
        const active = t === tab;
        t.setAttribute('aria-selected', String(active));
        t.tabIndex = active ? 0 : -1;
        $(t.getAttribute('aria-controls')).hidden = !active;
      });
      setStatus('');
    }

    tabs.forEach((tab, index) => {
      tab.addEventListener('click', () => select(tab));
      tab.addEventListener('keydown', (event) => {
        let target = null;
        if (event.key === 'ArrowRight') target = tabs[(index + 1) % tabs.length];
        if (event.key === 'ArrowLeft') target = tabs[(index - 1 + tabs.length) % tabs.length];
        if (target) {
          event.preventDefault();
          target.focus();
          select(target);
        }
      });
    });
  }

  // ---------------------------------------------------------------
  // Arranque
  // ---------------------------------------------------------------
  async function init() {
    setupTabs();
    $('customer-form').addEventListener('submit', onCreateCustomer);
    $('transfer-form').addEventListener('submit', onTransfer);
    $('transfer-sender').addEventListener('change', updateSenderHint);
    $('history-account').addEventListener('change', loadHistory);
    $('history-refresh').addEventListener('click', async () => {
      try {
        await loadCustomers();
      } catch (e) {
        // el historial vuelve a pedir sus propios datos
      }
      await loadHistory();
    });
    $('customers-refresh').addEventListener('click', async () => {
      try {
        await loadCustomers();
        setStatus('Lista de clientes actualizada.', 'ok');
      } catch (err) {
        setStatus('No se pudo actualizar la lista.', 'error');
      }
    });

    try {
      await loadCustomers();
      setStatus('');
    } catch (err) {
      setStatus(
        err instanceof ApiError ? err.message : 'No se pudo conectar con el servidor.',
        'error'
      );
    }
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
