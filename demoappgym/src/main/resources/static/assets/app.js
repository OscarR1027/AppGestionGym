(() => {
  "use strict";

  const API = "/api";
  const tokenKey = "pulso.auth";
  const resources = {
    users: {
      label: "Clientes", singular: "cliente", path: "/users", id: "id",
      description: "Personas registradas en el gimnasio.",
      columns: [["id", "ID"], ["name", "Nombre"], ["mail", "Correo"], ["phoneNumber", "Teléfono"], ["role", "Rol"]],
      fields: [
        { name: "name", label: "Nombre", required: true },
        { name: "mail", label: "Correo electrónico", type: "email", required: true },
        { name: "password", label: "Contraseña", type: "password", required: true, help: "Al editar, escribe la nueva contraseña." },
        { name: "address", label: "Dirección", required: true },
        { name: "phoneNumber", label: "Teléfono", type: "number", required: true },
        { name: "physicalConditionReport", label: "Reporte de condición física", type: "textarea" },
        { name: "role", label: "Rol", type: "select", options: [["USER", "Cliente"], ["COACH", "Entrenador"], ["ADMIN", "Administrador"]] }
      ],
      payload: form => ({ name: form.name, mail: form.mail, password: form.password, address: form.address, phoneNumber: Number(form.phoneNumber), physicalConditionReport: form.physicalConditionReport || null, role: form.role || "USER" })
    },
    coaches: {
      label: "Entrenadores", singular: "entrenador", path: "/coachs", id: "id",
      description: "Administra los entrenadores y sus asignaciones.",
      columns: [["id", "ID"], ["nameCoach", "Nombre"]],
      fields: [{ name: "nameCoach", label: "Nombre del entrenador", required: true }]
    },
    products: {
      label: "Inventario", singular: "producto", path: "/products", id: "id",
      description: "Control de productos, precios y existencias.",
      columns: [["id", "ID"], ["nameProduct", "Producto"], ["value", "Precio"], ["amount", "Existencias"]],
      fields: [
        { name: "nameProduct", label: "Nombre del producto", required: true },
        { name: "value", label: "Precio", type: "number", min: 0, step: "0.01", required: true },
        { name: "amount", label: "Cantidad disponible", type: "number", min: 0, required: true }
      ],
      payload: form => ({ nameProduct: form.nameProduct, value: Number(form.value), amount: Number(form.amount) })
    },
    services: {
      label: "Servicios", singular: "servicio", path: "/gymservice", id: "idService",
      description: "Servicios que se incluyen en los planes.",
      columns: [["idService", "ID"], ["serviceName", "Servicio"]],
      fields: [{ name: "serviceName", label: "Nombre del servicio", required: true }]
    },
    plans: {
      label: "Planes", singular: "plan", path: "/typeplan", id: "idTypePlan",
      description: "Planes de membresía y servicios incluidos.",
      columns: [["idTypePlan", "ID"], ["namePlan", "Plan"], ["value", "Precio"], ["services", "Servicios"]],
      fields: [
        { name: "namePlan", label: "Nombre del plan", required: true },
        { name: "value", label: "Precio", type: "number", min: 0, required: true },
        { name: "serviceIds", label: "Servicios incluidos", type: "checks", source: "services", valueKey: "idService", textKey: "serviceName", wide: true }
      ],
      payload: (form, checked) => ({ namePlan: form.namePlan, value: Number(form.value), serviceIds: checked })
    },
    affiliations: {
      label: "Afiliaciones", singular: "afiliación", path: "/affiliation", id: "id",
      description: "Membresías, vigencias y entrenadores asignados.",
      columns: [["id", "ID"], ["userName", "Cliente"], ["typePlanName", "Plan"], ["affiliationDate", "Inicio"], ["endAffiliation", "Vencimiento"], ["coaches", "Entrenadores"]],
      fields: [
        { name: "userId", label: "Cliente", type: "selectData", source: "users", valueKey: "id", textKey: "name", required: true },
        { name: "typePlanId", label: "Plan", type: "selectData", source: "plans", valueKey: "idTypePlan", textKey: "namePlan", required: true },
        { name: "affiliationDate", label: "Fecha de inicio", type: "date", required: true },
        { name: "endAffiliation", label: "Fecha de vencimiento", type: "date", required: true },
        { name: "coachIds", label: "Entrenadores asignados", type: "checks", source: "coaches", valueKey: "idCoach", textKey: "nameCoach", wide: true },
        { name: "coachStartDate", label: "Inicio de asignación", type: "date" },
        { name: "coachEndDate", label: "Fin de asignación", type: "date" }
      ],
      payload: (form, checked) => ({
        userId: Number(form.userId), typePlanId: Number(form.typePlanId),
        affiliationDate: form.affiliationDate, endAffiliation: form.endAffiliation,
        coaches: checked.map(coachId => ({ coachId: Number(coachId), startDate: form.coachStartDate || form.affiliationDate, endDate: form.coachEndDate || form.endAffiliation }))
      })
    }
  };

  const navItems = [
    ["dashboard", "Resumen", "▦"], ["affiliations", "Afiliaciones", "◉"], ["users", "Clientes", "♙"],
    ["coaches", "Entrenadores", "♧"], ["products", "Inventario", "⬡"], ["sales", "Ventas y facturas", "▤"],
    ["plans", "Planes", "◇"], ["services", "Servicios", "＋"]
  ];
  const state = {
    auth: sessionStorage.getItem(tokenKey) || "",
    email: sessionStorage.getItem("pulso.email") || "",
    view: "dashboard",
    cache: {},
    search: "",
    logoutInProgress: false
  };
  const loginScreen = document.querySelector("#login-screen");
  const appShell = document.querySelector("#app-shell");
  const pageContent = document.querySelector("#page-content");
  const dialog = document.querySelector("#form-dialog");
  const dialogForm = document.querySelector("#dialog-form");

  const escapeHtml = value => String(value ?? "").replace(/[&<>"']/g, char => ({
    "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
  })[char]);
  const formatMoney = value => new Intl.NumberFormat("es-CO", { style: "currency", currency: "COP", maximumFractionDigits: 0 }).format(Number(value || 0));
  const formatDate = value => value ? new Intl.DateTimeFormat("es-CO", { day: "2-digit", month: "short", year: "numeric" }).format(new Date(`${value}T12:00:00`)) : "—";
  const today = () => {
    const date = new Date();
    const local = new Date(date.getTime() - date.getTimezoneOffset() * 60_000);
    return local.toISOString().slice(0, 10);
  };
  const authHeaders = () => state.auth ? { Authorization: `Basic ${state.auth}` } : {};

  async function api(path, options = {}, allowUnauthorized = false) {
    const headers = { ...authHeaders(), ...(options.headers || {}) };
    if (options.body !== undefined) headers["Content-Type"] = "application/json";
    const response = await fetch(`${API}${path}`, { ...options, headers });
    if (response.status === 401) {
      if (!allowUnauthorized) logout("La sesión expiró. Ingresa nuevamente.");
      const error = new Error("Credenciales no válidas.");
      error.status = response.status;
      throw error;
    }
    if (!response.ok) {
      let detail = "";
      try {
        const body = await response.json();
        detail = body.message || body.error || "";
      } catch (_) {
        detail = await response.text().catch(() => "");
      }
      if (response.status === 403) detail = "No tienes permisos para esta operación.";
      const error = new Error(detail || `La solicitud falló (${response.status}).`);
      error.status = response.status;
      throw error;
    }
    if (response.status === 204) return null;
    const type = response.headers.get("content-type") || "";
    return type.includes("application/json") ? response.json() : null;
  }

  function notify(message, isError = false) {
    const toast = document.querySelector("#toast");
    toast.textContent = message;
    toast.classList.toggle("error", isError);
    toast.hidden = false;
    window.clearTimeout(notify.timer);
    notify.timer = window.setTimeout(() => { toast.hidden = true; }, 4200);
  }

  function showLogin(error = "") {
    loginScreen.hidden = false;
    appShell.hidden = true;
    const errorElement = document.querySelector("#login-error");
    errorElement.textContent = error;
    errorElement.hidden = !error;
  }

  function showApp() {
    loginScreen.hidden = true;
    appShell.hidden = false;
    document.querySelector("#signed-in-email").textContent = state.email || "Usuario";
    document.querySelector("#today-label").textContent = new Intl.DateTimeFormat("es-CO", { weekday: "long", day: "numeric", month: "long" }).format(new Date());
    renderNavigation();
    renderView();
  }

  function logout(message = "") {
    if (state.logoutInProgress) return;
    state.logoutInProgress = true;
    state.auth = "";
    state.email = "";
    state.cache = {};
    sessionStorage.removeItem(tokenKey);
    sessionStorage.removeItem("pulso.email");
    showLogin(message);
  }

  async function load(key, path = resources[key]?.path) {
    if (!path) throw new Error("Recurso no configurado.");
    state.cache[key] = await api(path);
    return state.cache[key];
  }

  function renderNavigation() {
    document.querySelector("#navigation").innerHTML = navItems.map(([id, label, icon]) =>
      `<button type="button" class="nav-link ${state.view === id ? "active" : ""}" data-view="${id}"><span class="nav-icon">${icon}</span>${label}</button>`
    ).join("");
  }

  function renderView() {
    renderNavigation();
    state.search = "";
    const titles = {
      dashboard: ["Resumen general", "Indicadores y actividad reciente del gimnasio"],
      affiliations: ["Afiliaciones", "Controla planes vigentes, vencimientos y entrenadores"],
      users: ["Clientes", "Administra los usuarios registrados"],
      coaches: ["Entrenadores", "Administra el equipo de entrenamiento"],
      products: ["Inventario", "Gestiona existencias y precios de productos"],
      sales: ["Ventas y facturas", "Registra ventas y consulta comprobantes"],
      plans: ["Planes", "Configura precios y servicios incluidos"],
      services: ["Servicios", "Catálogo de servicios del gimnasio"]
    };
    const [title, subtitle] = titles[state.view] || titles.dashboard;
    document.querySelector("#page-title").textContent = title;
    document.querySelector("#page-subtitle").textContent = subtitle;
    if (state.view === "dashboard") return renderDashboard();
    if (state.view === "sales") return renderSales();
    renderResource(state.view);
  }

  async function safeLoad(key) {
    try {
      return await load(key);
    } catch (error) {
      pageContent.innerHTML = `<div class="card empty-state">${escapeHtml(error.message)}<br><br><button class="button" data-action="retry">Reintentar</button></div>`;
      throw error;
    }
  }

  async function renderDashboard() {
    pageContent.innerHTML = `<div class="loading">Cargando el resumen...</div>`;
    const keys = ["users", "affiliations", "products", "coaches", "sales"];
    const results = await Promise.allSettled(keys.map(key => load(key)));
    const data = Object.fromEntries(keys.map((key, index) => [key, results[index].status === "fulfilled" ? results[index].value : null]));
    const errs = results.filter(result => result.status === "rejected");
    const users = data.users || [];
    const affiliations = data.affiliations || [];
    const products = data.products || [];
    const sales = data.sales || [];
    const revenueRequest = api("/sales/monthly-revenue").catch(() => null);
    const [revenue, expiring] = await Promise.all([
      revenueRequest,
      api("/affiliation/expiring-soon").catch(() => [])
    ]);
    const active = affiliations.filter(item => item.endAffiliation >= today()).length;
    const lowStock = products.filter(item => Number(item.amount) <= 5);
    const recentSales = [...sales].sort((a, b) => String(b.saleDate).localeCompare(String(a.saleDate))).slice(0, 5);
    const revenueValue = revenue?.totalValue ?? sales.filter(item => String(item.saleDate || "").startsWith(today().slice(0, 7))).reduce((sum, item) => sum + Number(item.totalValue || 0), 0);
    const bars = [34, 49, 40, 63, 55, 76, 61, 86, 70, 94, 78, Math.max(37, Math.min(100, Math.round(revenueValue ? 100 : 43)))];
    pageContent.innerHTML = `
      ${errs.length ? `<div class="card alert-item" style="margin-bottom:16px"><span class="alert-icon">!</span><div><strong>Parte de los datos no está disponible</strong><small>Inicia sesión con una cuenta administradora para consultar todas las áreas. ${errs.length} solicitud(es) no se pudieron cargar.</small></div></div>` : ""}
      <div class="metric-grid">
        ${metric("CLIENTES", users.length, "Personas registradas", "♙")}
        ${metric("AFILIACIONES VIGENTES", active, "Con vencimiento pendiente", "◉")}
        ${metric("PRODUCTOS CON POCO STOCK", lowStock.length, "Cinco unidades o menos", "⬡")}
        ${metric("INGRESOS DEL MES", formatMoney(revenueValue), "Ventas registradas", "↗")}
      </div>
      <div class="dashboard-grid">
        <section class="card panel">
          <div class="section-heading"><div><p class="eyebrow">INGRESOS · ESTE MES</p><p class="revenue-value">${formatMoney(revenueValue)}</p><p class="muted">Total informado por las ventas registradas</p></div><span class="pill">Este mes</span></div>
          <div class="bar-chart" aria-label="Vista visual de ingresos">${bars.map((height, index) => `<span class="bar" style="height:${height}%" title="${index + 1}"></span>`).join("")}</div>
          <div class="chart-caption"><span>Inicio del mes</span><span>Hoy</span></div>
        </section>
        <section class="card panel">
          <div class="section-heading"><div><h2>Alertas operativas</h2><p class="muted">Prioridades para revisar</p></div></div>
          <div class="alert-list">
            <div class="alert-item"><span class="alert-icon">▣</span><div><strong>${lowStock.length} productos con poco stock</strong><small>${lowStock.length ? escapeHtml(lowStock.slice(0, 2).map(item => item.nameProduct).join(", ")) : "No hay productos por reponer"}</small></div></div>
            <div class="alert-item"><span class="alert-icon">◷</span><div><strong>${expiring.length} afiliaciones por vencer</strong><small>Vencen durante los próximos tres días</small></div></div>
            <div class="alert-item"><span class="alert-icon">＋</span><div><strong>${(data.coaches || []).length} entrenadores</strong><small>Registrados en el equipo</small></div></div>
          </div>
        </section>
        <section class="card panel">
          <div class="section-heading"><div><h2>Ventas recientes</h2><p class="muted">Últimas operaciones registradas</p></div><button class="button small" data-view="sales">Ver todas →</button></div>
          ${recentSales.length ? tableHtml(recentSales, [["saleDate", "Fecha"], ["userName", "Cliente"], ["products", "Productos"], ["totalValue", "Total"]], "sale") : `<div class="empty-state">Aún no hay ventas registradas.</div>`}
        </section>
        <section class="card panel">
          <div class="section-heading"><div><h2>Accesos rápidos</h2><p class="muted">Tareas frecuentes</p></div></div>
          <div class="alert-list">
            <button class="alert-item quick-link" data-action="new-sale"><span class="alert-icon">＋</span><span><strong>Registrar una venta</strong><small>Productos y cliente</small></span></button>
            <button class="alert-item quick-link" data-action="create-affiliation"><span class="alert-icon">◉</span><span><strong>Crear afiliación</strong><small>Asignar plan a un cliente</small></span></button>
            <button class="alert-item quick-link" data-view="products"><span class="alert-icon">⬡</span><span><strong>Revisar inventario</strong><small>${lowStock.length} productos con poco stock</small></span></button>
          </div>
        </section>
      </div>`;
  }

  function metric(label, value, note, icon) {
    return `<article class="card metric-card"><div class="metric-top">${label}<span class="metric-icon">${icon}</span></div><p class="metric-value">${escapeHtml(value)}</p><p class="metric-note">${note}</p></article>`;
  }

  async function renderResource(key) {
    const config = resources[key];
    if (!config) return;
    pageContent.innerHTML = `<div class="loading">Cargando ${escapeHtml(config.label.toLowerCase())}...</div>`;
    try {
      const rows = await load(key);
      const extraButton = key === "affiliations" ? `<button class="button" data-action="expiring">Por vencer</button>` : "";
      pageContent.innerHTML = `
        <div class="toolbar"><div><h2>${escapeHtml(config.label)}</h2><p class="muted">${escapeHtml(config.description)}</p></div>
          <div class="toolbar-actions"><input class="search" type="search" placeholder="Buscar..." aria-label="Buscar ${escapeHtml(config.label.toLowerCase())}"><button class="button" data-action="refresh">↻ Actualizar</button>${extraButton}<button class="button primary" data-action="create">＋ Nuevo ${escapeHtml(config.singular)}</button></div>
        </div>
        <div id="resource-table">${tableHtml(rows, config.columns, key)}</div>`;
    } catch (error) {
      pageContent.innerHTML = `<div class="card empty-state">${escapeHtml(error.message)}<br><br><button class="button" data-action="retry">Reintentar</button></div>`;
    }
  }

  async function renderSales() {
    pageContent.innerHTML = `<div class="loading">Cargando ventas...</div>`;
    try {
      const [sales, revenue] = await Promise.all([load("sales"), api("/sales/monthly-revenue")]);
      pageContent.innerHTML = `
        <div class="metric-grid">${metric("VENTAS REGISTRADAS", sales.length, "Comprobantes en el sistema", "▤")}${metric("INGRESOS DEL MES", formatMoney(revenue?.totalValue), "Total del mes actual", "↗")}</div>
        <div class="toolbar"><div><h2>Historial de ventas</h2><p class="muted">Consulta, edita o imprime los comprobantes.</p></div>
          <div class="toolbar-actions"><input class="search" type="search" placeholder="Buscar venta o cliente..." aria-label="Buscar ventas"><button class="button" data-action="refresh">↻ Actualizar</button><button class="button primary" data-action="new-sale">＋ Nueva venta</button></div>
        </div>
        <div id="resource-table">${tableHtml(sales, [["id", "Factura"], ["saleDate", "Fecha"], ["userName", "Cliente"], ["products", "Artículos"], ["totalValue", "Total"]], "sales")}</div>`;
    } catch (error) {
      pageContent.innerHTML = `<div class="card empty-state">${escapeHtml(error.message)}<br><br><button class="button" data-action="retry">Reintentar</button></div>`;
    }
  }

  function cellValue(row, key) {
    const value = row[key];
    if (key === "saleDate" || key === "affiliationDate" || key === "endAffiliation") return escapeHtml(formatDate(value));
    if (key === "value" || key === "totalValue") return escapeHtml(formatMoney(value));
    if (key === "products" && Array.isArray(value)) return escapeHtml(value.map(item => `${item.productName} ×${item.amount}`).join(", "));
    if (key === "services" && Array.isArray(value)) return escapeHtml(value.map(item => item.serviceName).join(", ") || "Sin servicios");
    if (key === "coaches" && Array.isArray(value)) return escapeHtml(value.map(item => item.coachName).join(", ") || "Sin asignar");
    if (key === "role") return `<span class="pill neutral">${escapeHtml(value || "USER")}</span>`;
    if (key === "amount" && Number(value) <= 5) return `<span class="pill warn">${escapeHtml(value)}</span>`;
    return escapeHtml(value ?? "—");
  }

  function tableHtml(rows, columns, key) {
    if (!Array.isArray(rows) || !rows.length) return `<div class="empty-state">No hay registros todavía. Usa el botón para crear el primero.</div>`;
    return `<div class="table-wrap"><table><thead><tr>${columns.map(([, label]) => `<th>${escapeHtml(label)}</th>`).join("")}<th>Acciones</th></tr></thead><tbody>
      ${rows.map(row => `<tr>${columns.map(([field]) => `<td>${cellValue(row, field)}</td>`).join("")}
        <td><div class="table-actions">${key === "sales" || key === "sale" ? `<button class="button small" data-action="invoice" data-id="${escapeHtml(row.id)}">Factura</button>` : ""}
        ${key === "sales" ? `<button class="button small" data-action="edit-sale" data-id="${escapeHtml(row.id)}">Editar</button><button class="button small danger" data-action="delete-sale" data-id="${escapeHtml(row.id)}">Eliminar</button>` : ""}
        ${key === "sale" ? "" : key !== "sales" ? `<button class="button small" data-action="edit" data-resource="${escapeHtml(key)}" data-id="${escapeHtml(row[resources[key].id])}">Editar</button><button class="button small danger" data-action="delete" data-resource="${escapeHtml(key)}" data-id="${escapeHtml(row[resources[key].id])}">Eliminar</button>` : ""}
        </div></td></tr>`).join("")}
    </tbody></table></div>`;
  }

  function filteredTable(query) {
    const key = state.view;
    const config = resources[key];
    const rows = state.cache[key] || [];
    const filtered = rows.filter(row => JSON.stringify(row).toLocaleLowerCase("es").includes(query.toLocaleLowerCase("es")));
    document.querySelector("#resource-table").innerHTML = tableHtml(filtered, config.columns, key);
  }

  function fieldMarkup(field, value = "", edit = false) {
    const required = field.required ? "required" : "";
    const min = field.min !== undefined ? `min="${field.min}"` : "";
    const step = field.step ? `step="${field.step}"` : "";
    const wide = field.wide || field.type === "textarea" ? "wide" : "";
    if (field.type === "select" || field.type === "selectData") {
      let options = field.options || [];
      if (field.type === "selectData") options = (state.cache[field.source] || []).map(item => [item[field.valueKey], item[field.textKey]]);
      return `<label class="${wide}">${escapeHtml(field.label)}<select name="${escapeHtml(field.name)}" ${required}><option value="">Selecciona...</option>${options.map(([id, text]) => `<option value="${escapeHtml(id)}" ${String(value) === String(id) ? "selected" : ""}>${escapeHtml(text)}</option>`).join("")}</select></label>`;
    }
    if (field.type === "checks") {
      const entries = state.cache[field.source] || [];
      let selected = [];
      if (Array.isArray(value)) selected = value.map(item => String(item[field.valueKey] ?? item));
      return `<div class="${wide}"><span class="field-label">${escapeHtml(field.label)}</span><div class="check-list">${entries.length ? entries.map(item => {
        const id = item[field.valueKey];
        return `<label class="check-item"><input type="checkbox" name="${escapeHtml(field.name)}" value="${escapeHtml(id)}" ${selected.includes(String(id)) ? "checked" : ""}>${escapeHtml(item[field.textKey])}</label>`;
      }).join("") : `<span class="muted">No hay opciones registradas.</span>`}</div></div>`;
    }
    const help = field.help ? `<small class="muted">${escapeHtml(field.help)}</small>` : "";
    if (field.type === "textarea") return `<label class="${wide}">${escapeHtml(field.label)}<textarea name="${escapeHtml(field.name)}">${escapeHtml(value)}</textarea>${help}</label>`;
    const type = field.type || "text";
    return `<label class="${wide}">${escapeHtml(field.label)}<input name="${escapeHtml(field.name)}" type="${type}" value="${escapeHtml(value)}" ${required} ${min} ${step} ${type === "password" && edit ? "autocomplete=\"new-password\"" : ""}>${help}</label>`;
  }

  async function openResourceForm(key, id = null) {
    const config = resources[key];
    if (!config) return;
    for (const field of config.fields) {
      if (field.source && !state.cache[field.source]) await load(field.source);
    }
    let row = null;
    if (id !== null) row = (state.cache[key] || []).find(item => String(item[config.id]) === String(id)) || await api(`${config.path}/${id}`);
    if (key === "affiliations" && row?.coaches) row = { ...row, coachIds: row.coaches, coachStartDate: row.coaches[0]?.startDate || "", coachEndDate: row.coaches[0]?.endDate || "" };
    const fields = config.fields
      .filter(field => !(key === "users" && id === null && field.name === "role"))
      .map(field => fieldMarkup(field, row?.[field.name] ?? "", id !== null))
      .join("");
    openDialog(`${id ? "Editar" : "Nuevo"} ${config.singular}`, `${id && key === "users" ? "Al guardar se actualizará la contraseña con el valor que escribas." : "Completa los datos y guarda los cambios."}`,
      `<div class="form-grid">${fields}</div>`,
      async formData => {
        const form = Object.fromEntries(formData.entries());
        const checked = [...dialogForm.querySelectorAll('input[type="checkbox"]:checked')].map(input => input.value);
        const payload = config.payload ? config.payload(form, checked) : form;
        await api(id ? `${config.path}/${id}` : config.path, { method: id ? "PUT" : "POST", body: JSON.stringify(payload) });
        state.cache[key] = null;
        notify(`${config.singular[0].toUpperCase()}${config.singular.slice(1)} ${id ? "actualizado" : "creado"} correctamente.`);
        renderView();
      });
  }

  function openDialog(title, subtitle, body, onSubmit, submitText = "Guardar") {
    dialogForm.innerHTML = `<div class="dialog-inner"><div class="dialog-head"><div><h2>${escapeHtml(title)}</h2><p>${escapeHtml(subtitle)}</p></div><button class="icon-button" type="button" data-dialog-close aria-label="Cerrar">×</button></div>${body}<div class="dialog-actions"><button class="button" type="button" data-dialog-close>Cancelar</button><button class="button primary" type="submit">${escapeHtml(submitText)}</button></div></div>`;
    dialogForm.onsubmit = async event => {
      event.preventDefault();
      const submitButton = dialogForm.querySelector('[type="submit"]');
      submitButton.disabled = true;
      try {
        await onSubmit(new FormData(dialogForm));
        dialog.close();
      } catch (error) {
        notify(error.message, true);
      } finally {
        submitButton.disabled = false;
      }
    };
    dialog.showModal();
  }

  function saleFormMarkup(sale = null) {
    const users = state.cache.users || [];
    const products = state.cache.products || [];
    const selectedUser = sale?.userId || "";
    const selectedProducts = sale?.products || [{ productId: "", amount: 1 }];
    return `
      <div class="form-grid">
        <label class="wide">Cliente<select name="userId" required><option value="">Selecciona un cliente...</option>${users.map(user => `<option value="${escapeHtml(user.id)}" ${String(selectedUser) === String(user.id) ? "selected" : ""}>${escapeHtml(user.name)} · ${escapeHtml(user.mail)}</option>`).join("")}</select></label>
        <div class="wide"><label>Productos</label><div class="line-items" id="sale-lines">${selectedProducts.map(item => saleLineMarkup(item, products)).join("")}</div><button class="button small" type="button" data-action="add-sale-line">＋ Agregar producto</button></div>
      </div>
      <p id="sale-total" class="invoice-total"></p>`;
  }

  function saleLineMarkup(item = {}, products = state.cache.products || []) {
    const id = item.productId ?? "";
    return `<div class="sale-line"><select name="productId" required><option value="">Producto...</option>${products.map(product => `<option value="${escapeHtml(product.id)}" data-price="${escapeHtml(product.value)}" data-stock="${escapeHtml(product.amount)}" ${String(id) === String(product.id) ? "selected" : ""}>${escapeHtml(product.nameProduct)} · ${formatMoney(product.value)} (${escapeHtml(product.amount)} disp.)</option>`).join("")}</select><input name="amount" type="number" min="1" value="${escapeHtml(item.amount || 1)}" required aria-label="Cantidad"><button class="icon-button" type="button" data-action="remove-sale-line" aria-label="Quitar producto">×</button></div>`;
  }

  async function openSaleForm(id = null) {
    try {
      const [users, products] = await Promise.all([load("users"), load("products")]);
      if (!users.length || !products.length) throw new Error("Registra al menos un cliente y un producto antes de crear una venta.");
      const sale = id ? await api(`/sales/${id}`) : null;
      openDialog(id ? "Editar venta" : "Nueva venta", "Selecciona el cliente y los productos. El inventario se actualiza al registrar.", saleFormMarkup(sale),
        async formData => {
          const productIds = [...dialogForm.querySelectorAll('select[name="productId"]')];
          const amounts = [...dialogForm.querySelectorAll('input[name="amount"]')];
          const productTotals = new Map();
          productIds.forEach((select, index) => {
            const productId = Number(select.value);
            const amount = Number(amounts[index].value);
            productTotals.set(productId, (productTotals.get(productId) || 0) + amount);
          });
          const saleProducts = [...productTotals].map(([productId, amount]) => ({ productId, amount }));
          if (saleProducts.some(item => !item.productId || item.amount < 1)) throw new Error("Selecciona cada producto e indica una cantidad válida.");
          const payload = { userId: Number(formData.get("userId")), products: saleProducts };
          await api(id ? `/sales/${id}` : "/sales", { method: id ? "PUT" : "POST", body: JSON.stringify(payload) });
          state.cache.sales = null;
          state.cache.products = null;
          notify(id ? "Venta actualizada." : "Venta registrada correctamente.");
          renderView();
        }, id ? "Guardar venta" : "Registrar venta");
      updateSaleTotal();
    } catch (error) {
      notify(error.message, true);
    }
  }

  function updateSaleTotal() {
    const total = [...dialogForm.querySelectorAll(".sale-line")].reduce((sum, line) => {
      const option = line.querySelector("select").selectedOptions[0];
      const price = Number(option?.dataset.price || 0);
      const amount = Number(line.querySelector('input[name="amount"]').value || 0);
      return sum + price * amount;
    }, 0);
    const output = dialogForm.querySelector("#sale-total");
    if (output) output.textContent = `Total estimado: ${formatMoney(total)}`;
  }

  async function showInvoice(id) {
    try {
      const invoice = await api(`/sales/${id}/factura`);
      const lines = (invoice.products || []).map(product => `<tr><td>${escapeHtml(product.productName)}</td><td>${escapeHtml(product.amount)}</td><td>${formatMoney(product.unitPrice)}</td><td>${formatMoney(product.subtotal)}</td></tr>`).join("");
      openDialog(`Factura #${invoice.id}`, `Fecha: ${formatDate(invoice.saleDate)} · Cliente: ${invoice.userName}`,
        `<div class="table-wrap"><table><thead><tr><th>Producto</th><th>Cant.</th><th>Precio</th><th>Subtotal</th></tr></thead><tbody>${lines || `<tr><td colspan="4">Sin productos</td></tr>`}</tbody></table></div><p class="invoice-total">Total: ${formatMoney(invoice.totalValue)}</p>`,
        async () => {}, "Cerrar");
      const submit = dialogForm.querySelector('[type="submit"]');
      submit.type = "button";
      submit.textContent = "Imprimir";
      submit.addEventListener("click", () => window.print());
      dialogForm.querySelector('[data-dialog-close]').addEventListener("click", () => dialog.close());
    } catch (error) {
      notify(error.message, true);
    }
  }

  async function deleteResource(key, id) {
    const config = resources[key];
    if (!window.confirm(`¿Eliminar este registro de ${config.singular}? Esta acción no se puede deshacer.`)) return;
    try {
      await api(`${config.path}/${id}`, { method: "DELETE" });
      state.cache[key] = null;
      notify("Registro eliminado.");
      renderView();
    } catch (error) {
      notify(error.message, true);
    }
  }

  async function deleteSale(id) {
    if (!window.confirm(`¿Eliminar la venta #${id}? Esta acción no se puede deshacer.`)) return;
    try {
      await api(`/sales/${id}`, { method: "DELETE" });
      state.cache.sales = null;
      notify("Venta eliminada.");
      renderSales();
    } catch (error) {
      notify(error.message, true);
    }
  }

  async function showExpiring() {
    try {
      const affiliations = await api("/affiliation/expiring-soon");
      state.cache.affiliations = affiliations;
      document.querySelector("#resource-table").innerHTML = tableHtml(affiliations, resources.affiliations.columns, "affiliations");
      notify(`${affiliations.length} afiliaciones vencen durante los próximos tres días.`);
    } catch (error) {
      notify(error.message, true);
    }
  }

  document.querySelector("#login-form").addEventListener("submit", async event => {
    event.preventDefault();
    const submitButton = event.currentTarget.querySelector('[type="submit"]');
    if (submitButton.disabled) return;
    submitButton.disabled = true;
    const form = new FormData(event.currentTarget);
    const email = String(form.get("email") || "").trim();
    const password = String(form.get("password") || "");
    try {
      const response = await fetch(`${API}/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password })
      });
      if (response.status === 401) {
        throw new Error("Correo o contraseña incorrectos.");
      }
      if (response.status === 403) {
        throw new Error("La cuenta no tiene un rol asignado. Contacta al administrador del sistema.");
      }
      if (!response.ok) {
        throw new Error(`El servidor respondió con error (${response.status}).`);
      }
      const account = await response.json();
      if (account.role !== "ADMIN") {
        throw new Error("El usuario existe, pero no tiene el rol ADMIN requerido para este panel.");
      }
      state.auth = btoa(unescape(encodeURIComponent(`${email}:${password}`)));
      state.email = account.email;
      state.logoutInProgress = false;
      sessionStorage.setItem(tokenKey, state.auth);
      sessionStorage.setItem("pulso.email", email);
      state.cache = {};
      showApp();
    } catch (error) {
      state.auth = "";
      showLogin(error instanceof TypeError
        ? "No fue posible conectar con la API. Confirma que Spring Boot esté ejecutándose."
        : error.message || "No se pudo iniciar sesión.");
    } finally {
      submitButton.disabled = false;
    }
  });

  document.querySelector("#logout-button").addEventListener("click", logout);
  document.querySelector("#navigation").addEventListener("click", event => {
    const button = event.target.closest("[data-view]");
    if (!button) return;
    state.view = button.dataset.view;
    renderView();
  });

  document.body.addEventListener("click", async event => {
    const viewButton = event.target.closest("[data-view]");
    if (viewButton && !viewButton.closest("#navigation")) {
      state.view = viewButton.dataset.view;
      renderView();
      return;
    }
    const closeButton = event.target.closest("[data-dialog-close]");
    if (closeButton) { dialog.close(); return; }
    const actionButton = event.target.closest("[data-action]");
    if (!actionButton) return;
    const { action, resource, id } = actionButton.dataset;
    if (action === "new-sale") { await openSaleForm(); return; }
    if (action === "create-affiliation") { state.view = "affiliations"; renderView(); setTimeout(() => openResourceForm("affiliations"), 0); return; }
    if (action === "create") { await openResourceForm(state.view); return; }
    if (action === "edit") { await openResourceForm(resource, id); return; }
    if (action === "delete") { await deleteResource(resource, id); return; }
    if (action === "invoice") { await showInvoice(id); return; }
    if (action === "edit-sale") { await openSaleForm(id); return; }
    if (action === "delete-sale") { await deleteSale(id); return; }
    if (action === "refresh" || action === "retry") { state.cache[state.view] = null; renderView(); return; }
    if (action === "expiring") { await showExpiring(); return; }
    if (action === "add-sale-line") {
      document.querySelector("#sale-lines").insertAdjacentHTML("beforeend", saleLineMarkup());
      updateSaleTotal();
      return;
    }
    if (action === "remove-sale-line") {
      const line = actionButton.closest(".sale-line");
      if (document.querySelectorAll(".sale-line").length > 1) line.remove();
      else { line.querySelector("select").value = ""; line.querySelector('input[name="amount"]').value = 1; }
      updateSaleTotal();
    }
  });

  pageContent.addEventListener("input", event => {
    if (event.target.matches(".search")) filteredTable(event.target.value);
  });
  dialogForm.addEventListener("change", event => {
    if (event.target.matches(".sale-line select, .sale-line input")) updateSaleTotal();
  });
  dialog.addEventListener("click", event => {
    if (event.target === dialog) dialog.close();
  });

  if (state.auth) {
    api("/users").then(() => showApp()).catch(() => logout("La sesión expiró. Ingresa nuevamente."));
  } else {
    showLogin();
  }
})();
