(() => {
  "use strict";

  const API = (window.APP_CONFIG?.apiBaseUrl || "/api").replace(/\/+$/, "");
  const tokenKey = "pulso.auth";
  const resources = {
    users: {
      label: "Clientes", singular: "cliente", path: "/users", id: "id",
      description: "Personas registradas en el gimnasio.",
      columns: [["photoData", "Foto"], ["id", "ID"], ["name", "Nombre"], ["documentNumber", "Cédula"], ["mail", "Correo"], ["phoneNumber", "Teléfono"], ["role", "Rol"]],
      fields: [
        { name: "name", label: "Nombre", required: true },
        { name: "mail", label: "Correo electrónico", type: "email", required: true },
        { name: "password", label: "Contraseña", type: "password", help: "En edición, deja el campo vacío para conservar la actual o escribe una nueva contraseña." },
        { name: "address", label: "Dirección", required: true },
        { name: "phoneNumber", label: "Teléfono", type: "number", required: true },
        { name: "documentNumber", label: "Cédula de identidad", required: true, createOnlyRequired: true },
        { name: "photoData", label: "Foto de la persona (opcional)", type: "photo", wide: true },
        { name: "physicalConditionReport", label: "Reporte de condición física", type: "textarea" },
        { name: "role", label: "Rol", type: "select", options: [["USER", "Cliente"], ["COACH", "Entrenador"], ["ADMIN", "Administrador"]] }
      ],
      payload: form => ({ name: form.name, mail: form.mail, password: form.password, address: form.address, phoneNumber: Number(form.phoneNumber), documentNumber: form.documentNumber, photoData: form.photoData || null, physicalConditionReport: form.physicalConditionReport || null, role: form.role || "USER" })
    },
    coaches: {
      label: "Entrenadores", singular: "entrenador", path: "/coachs", id: "idCoach",
      description: "Administra los entrenadores y sus asignaciones.",
      columns: [["photoData", "Foto"], ["idCoach", "ID"], ["nameCoach", "Nombre"]],
      fields: [
        { name: "nameCoach", label: "Nombre del entrenador", required: true },
        { name: "photoData", label: "Foto del entrenador (opcional)", type: "photo", wide: true }
      ],
      payload: form => ({ nameCoach: form.nameCoach, photoData: form.photoData || null })
    },
    products: {
      label: "Inventario", singular: "producto", path: "/products", id: "id",
      description: "Control de productos, precios y existencias.",
      columns: [["photoData", "Foto"], ["id", "ID"], ["nameProduct", "Producto"], ["value", "Precio"], ["amount", "Existencias"]],
      fields: [
        { name: "nameProduct", label: "Nombre del producto", required: true },
        { name: "photoData", label: "Foto del producto (opcional)", type: "photo", wide: true },
        { name: "value", label: "Precio", type: "number", min: 0, step: "0.01", required: true },
        { name: "amount", label: "Cantidad disponible", type: "number", min: 0, required: true }
      ],
      payload: form => ({ nameProduct: form.nameProduct, value: Number(form.value), amount: Number(form.amount), photoData: form.photoData || null })
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
      columns: [["id", "ID"], ["userName", "Cliente"], ["typePlanName", "Plan"], ["affiliationDate", "Inicio"], ["endAffiliation", "Vencimiento"], ["invoiceCancelled", "Estado de factura"], ["coaches", "Entrenadores"]],
      fields: [
        { name: "userId", label: "Cliente", type: "selectData", source: "users", valueKey: "id", textKey: "name", required: true },
        { name: "typePlanId", label: "Plan", type: "selectData", source: "plans", valueKey: "idTypePlan", textKey: "namePlan", required: true },
        { name: "affiliationDate", label: "Fecha de inicio", type: "date", required: true },
        { name: "endAffiliation", label: "Fecha de vencimiento", type: "date", required: true },
        { name: "paymentMethod", label: "Forma de pago", type: "select", options: [["CASH", "Efectivo"], ["CARD", "Tarjeta"], ["TRANSFER", "Transferencia"]], required: true },
        { name: "coachIds", label: "Entrenadores asignados", type: "checks", source: "coaches", valueKey: "idCoach", textKey: "nameCoach", wide: true },
        { name: "coachStartDate", label: "Inicio de asignación", type: "date" },
        { name: "coachEndDate", label: "Fin de asignación", type: "date" }
      ],
      payload: (form, checked) => ({
        userId: Number(form.userId), typePlanId: Number(form.typePlanId),
        affiliationDate: form.affiliationDate, endAffiliation: form.endAffiliation, paymentMethod: form.paymentMethod,
        coaches: checked.map(coachId => ({ coachId: Number(coachId), startDate: form.coachStartDate || form.affiliationDate, endDate: form.coachEndDate || form.endAffiliation }))
      })
    },
    sales: {
      label: "Ventas y facturas", singular: "venta", path: "/sales", id: "id",
      description: "Registra ventas y consulta comprobantes.",
      columns: [["invoiceNumber", "Factura"], ["saleDate", "Fecha"], ["userName", "Cliente"], ["products", "Concepto"], ["totalValue", "Total"]]
    }
  };

  const navItems = [
    ["dashboard", "Resumen", "▦"], ["affiliations", "Afiliaciones", "◉"], ["users", "Clientes", "♙"],
    ["coaches", "Entrenadores", "♧"], ["products", "Inventario", "⬡"], ["sales", "Ventas y facturas", "▤"],
    ["plans", "Planes", "◇"], ["services", "Servicios", "＋"]
  ];
  const mobileNavItems = [
    ["dashboard", "Inicio", "⌂"], ["sales", "Ventas", "▤"], ["users", "Clientes", "♙"]
  ];
  const state = {
    auth: sessionStorage.getItem(tokenKey) || "",
    email: sessionStorage.getItem("pulso.email") || "",
    view: "dashboard",
    cache: {},
    search: "",
    invoiceDateRange: { from: "", to: "" },
    logoutInProgress: false
  };
  const loginScreen = document.querySelector("#login-screen");
  const appShell = document.querySelector("#app-shell");
  const pageContent = document.querySelector("#page-content");
  const dialog = document.querySelector("#form-dialog");
  const dialogForm = document.querySelector("#dialog-form");
  let photoCameraStream = null;

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
    const initials = (state.email.split("@")[0] || "AD").slice(0, 2).toUpperCase();
    document.querySelector("#mobile-avatar").textContent = initials;
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
    document.querySelector("#mobile-navigation").innerHTML = [
      ...mobileNavItems.map(([id, label, icon]) =>
        `<button type="button" class="mobile-nav-link ${state.view === id ? "active" : ""}" data-view="${id}" ${state.view === id ? 'aria-current="page"' : ""}><span class="mobile-nav-icon" aria-hidden="true">${icon}</span><span>${label}</span></button>`
      ),
      `<button type="button" class="mobile-nav-link ${!mobileNavItems.some(([id]) => id === state.view) ? "active" : ""}" data-action="mobile-menu" aria-haspopup="dialog"><span class="mobile-nav-icon" aria-hidden="true">☰</span><span>Menú</span></button>`
    ].join("");
  }

  function openMobileMenu() {
    const links = navItems.map(([id, label, icon]) =>
      `<button class="mobile-menu-item ${state.view === id ? "active" : ""}" type="button" data-view="${id}"><span class="mobile-menu-icon" aria-hidden="true">${icon}</span><span>${escapeHtml(label)}</span><span class="mobile-menu-arrow" aria-hidden="true">›</span></button>`
    ).join("");
    const email = escapeHtml(state.email || "Usuario");
    openDialog("Menú", "Accede a todas las secciones y opciones de tu cuenta.",
      `<div class="mobile-menu-content"><div class="mobile-menu-profile"><span class="avatar">${escapeHtml((state.email.split("@")[0] || "AD").slice(0, 2).toUpperCase())}</span><span><strong>${email}</strong><small>Sesión activa</small></span></div><div class="mobile-menu-links">${links}</div><button class="button mobile-menu-logout" type="button" data-action="logout">Cerrar sesión</button></div>`,
      async () => {}, "Listo");
  }

  function renderView() {
    renderNavigation();
    appShell.dataset.view = state.view;
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
    const keys = ["users", "affiliations", "products", "coaches", "sales", "plans"];
    const results = await Promise.allSettled(keys.map(key => load(key)));
    const data = Object.fromEntries(keys.map((key, index) => [key, results[index].status === "fulfilled" ? results[index].value : null]));
    const errs = results.filter(result => result.status === "rejected");
    const users = data.users || [];
    const affiliations = data.affiliations || [];
    const products = data.products || [];
    const sales = data.sales || [];
    const plans = data.plans || [];
    const revenueRequest = api("/sales/monthly-revenue").catch(() => null);
    const [revenue, expiring] = await Promise.all([
      revenueRequest,
      api("/affiliation/expiring-soon").catch(() => [])
    ]);
    const active = affiliations.filter(item => !item.invoiceCancelled && item.endAffiliation >= today()).length;
    const lowStock = products.filter(item => Number(item.amount) <= 5);
    const recentInvoices = [
      ...sales.map(sale => ({ ...sale, invoiceType: "sale" })),
      ...affiliationInvoiceRows(affiliations, plans)
    ].sort((left, right) => String(right.saleDate).localeCompare(String(left.saleDate))).slice(0, 5);
    const revenueValue = monthlyRevenue(sales, affiliations, plans, revenue?.totalValue);
    const currentMonth = today().slice(0, 7);
    const daysElapsed = Number(today().slice(8, 10));
    const dailyRevenue = Array.from({ length: daysElapsed }, (_, index) => {
      const date = `${currentMonth}-${String(index + 1).padStart(2, "0")}`;
      const salesValue = sales.filter(item => item.saleDate === date)
        .filter(item => !item.invoiceCancelled)
        .reduce((sum, item) => sum + Number(item.totalValue || 0), 0);
      const affiliationValue = affiliations
        .filter(item => item.affiliationDate === date && !item.invoiceCancelled)
        .reduce((sum, affiliation) => {
          const plan = plans.find(item => String(item.idTypePlan) === String(affiliation.typePlanId));
          return sum + Number(affiliation.paidValue ?? plan?.value ?? 0);
        }, 0);
      return salesValue + affiliationValue;
    });
    const maxDailyRevenue = Math.max(...dailyRevenue, 0);
    pageContent.innerHTML = `
      ${errs.length ? `<div class="card alert-item" style="margin-bottom:16px"><span class="alert-icon">!</span><div><strong>Parte de los datos no está disponible</strong><small>Inicia sesión con una cuenta administradora para consultar todas las áreas. ${errs.length} solicitud(es) no se pudieron cargar.</small></div></div>` : ""}
      <div class="metric-grid">
        ${metric("Clientes", users.length, "Personas registradas", "♙")}
        ${metric("Afiliaciones vigentes", active, "Con vencimiento pendiente", "◉")}
        ${metric("Productos con poco stock", lowStock.length, "Cinco unidades o menos", "⬡")}
        ${metric("Ingresos del mes", formatMoney(revenueValue), "Ventas y afiliaciones", "↗")}
      </div>
      <div class="dashboard-grid">
        <section class="card panel revenue-panel">
          ${revenueSparkline(dailyRevenue)}
          <div class="section-heading"><div><p class="eyebrow">INGRESOS · ESTE MES</p><p class="revenue-value">${formatMoney(revenueValue)}</p><p class="muted">Ventas y afiliaciones registradas este mes</p></div><span class="pill">Este mes</span></div>
          <div class="bar-chart" aria-label="Ingresos diarios del mes">${dailyRevenue.map((value, index) => {
            const height = maxDailyRevenue ? Math.max(3, Math.round(value / maxDailyRevenue * 100)) : 3;
            return `<span class="bar ${value ? "has-income" : ""}" style="height:${height}%" title="${index + 1}: ${formatMoney(value)}"></span>`;
          }).join("")}</div>
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
          <div class="section-heading"><div><h2>Facturas recientes</h2><p class="muted">Últimas ventas y afiliaciones</p></div><button class="button small" data-view="sales">Ver todas →</button></div>
          ${recentInvoices.length ? tableHtml(recentInvoices, [["saleDate", "Fecha"], ["userName", "Cliente"], ["products", "Concepto"], ["totalValue", "Total"]], "sale") : `<div class="empty-state">Aún no hay facturas registradas.</div>`}
        </section>
      </div>`;
  }

  function revenueSparkline(values) {
    const points = values.slice(-30).length ? values.slice(-30) : [0];
    const max = Math.max(...points, 1);
    const coordinates = points.map((value, index) =>
      `${points.length === 1 ? 50 : index / (points.length - 1) * 100},${30 - value / max * 26}`
    );
    const line = coordinates.map((point, index) => `${index ? "L" : "M"}${point}`).join(" ");
    const area = `${line} L100,34 L0,34 Z`;
    return `<svg class="revenue-sparkline" viewBox="0 0 100 36" preserveAspectRatio="none" aria-hidden="true"><path class="sparkline-area" d="${area}"></path><path class="sparkline-line" d="${line}"></path></svg>`;
  }

  function metric(label, value, note, icon) {
    const paths = {
      "♙": `<circle cx="12" cy="8" r="3.5"/><path d="M5 20a7 7 0 0 1 14 0"/>`,
      "◉": `<circle cx="12" cy="12" r="8"/><circle cx="12" cy="12" r="3"/>`,
      "⬡": `<path d="m12 3 8 4.5v9L12 21l-8-4.5v-9L12 3Z"/><path d="m4.5 7.8 7.5 4.4 7.5-4.4M12 12.2V21"/>`,
      "↗": `<path d="M4 17 10 11l4 4 6-8"/><path d="M14 7h6v6"/>`
    };
    return `<article class="card metric-card"><span class="metric-icon" aria-hidden="true"><svg viewBox="0 0 24 24">${paths[icon] || ""}</svg></span><p class="metric-title">${escapeHtml(label)}</p><p class="metric-value">${escapeHtml(value)}</p><p class="metric-note">${escapeHtml(note)}</p></article>`;
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
      const [sales, affiliations, plans] = await Promise.all([
        load("sales"),
        load("affiliations"),
        load("plans")
      ]);
      const affiliationInvoices = affiliationInvoiceRows(affiliations, plans);
      const invoices = [
        ...sales.map(sale => ({ ...sale, invoiceType: "sale", invoiceNumber: formatInvoiceNumber("F", sale.id, sale.saleDate) })),
        ...affiliationInvoices
      ].sort((left, right) => String(right.saleDate).localeCompare(String(left.saleDate)));
      const revenueValue = monthlyRevenue(sales, affiliations, plans);
      state.cache.invoiceRows = invoices;
      const visibleInvoices = filterInvoicesByDate(invoices);
      const periodRevenue = visibleInvoices
        .filter(invoice => !invoice.invoiceCancelled)
        .reduce((sum, invoice) => sum + Number(invoice.totalValue || 0), 0);
      pageContent.innerHTML = `
        <div class="metric-grid">${metric("FACTURAS DEL PERÍODO", visibleInvoices.length, "Incluye registros anulados", "▤")}${metric("INGRESOS DEL PERÍODO", formatMoney(periodRevenue), "Excluye facturas anuladas", "↗")}${metric("INGRESOS DEL MES", formatMoney(revenueValue), "Ventas y afiliaciones del mes", "↗")}</div>
        <div class="toolbar"><div><h2>Historial de facturas</h2><p class="muted">Consulta ventas y comprobantes de afiliación.</p></div>
          <div class="toolbar-actions"><input class="search" type="search" placeholder="Buscar venta o cliente..." aria-label="Buscar ventas"><button class="button" data-action="refresh">↻ Actualizar</button></div>
        </div>
        <div class="invoice-filters card">
          <label>Desde<input type="date" name="invoice-from" value="${escapeHtml(state.invoiceDateRange.from)}"></label>
          <label>Hasta<input type="date" name="invoice-to" value="${escapeHtml(state.invoiceDateRange.to)}"></label>
          <button class="button primary" data-action="apply-invoice-range">Filtrar fechas</button>
          <button class="button" data-action="invoice-period" data-period="previous-month">Mes pasado</button>
          <button class="button" data-action="invoice-period" data-period="current-month">Este mes</button>
          <button class="button" data-action="invoice-period" data-period="current-year">Este año</button>
          <button class="button" data-action="invoice-period" data-period="all">Todas</button>
        </div>
        <div id="resource-table">${tableHtml(visibleInvoices, resources.sales.columns, "sales")}</div>`;
    } catch (error) {
      pageContent.innerHTML = `<div class="card empty-state">${escapeHtml(error.message)}<br><br><button class="button" data-action="retry">Reintentar</button></div>`;
    }
  }

  function affiliationInvoiceRows(affiliations, plans) {
    return affiliations.map(affiliation => {
      const plan = plans.find(item => String(item.idTypePlan) === String(affiliation.typePlanId));
      return {
        id: `A-${affiliation.id}`,
        affiliationId: affiliation.id,
        invoiceType: "affiliation",
        invoiceNumber: formatInvoiceNumber("AF", affiliation.id, affiliation.affiliationDate),
        saleDate: affiliation.affiliationDate,
        userName: affiliation.userName,
        userDocumentNumber: affiliation.userDocumentNumber,
        userPhoneNumber: affiliation.userPhoneNumber,
        paymentMethod: affiliation.paymentMethod,
        renewal: affiliation.renewal,
        invoiceCancelled: affiliation.invoiceCancelled,
        products: [{ productName: `Afiliación: ${affiliation.typePlanName}`, amount: 1 }],
        totalValue: Number(affiliation.paidValue ?? plan?.value ?? 0)
      };
    });
  }

  function formatInvoiceNumber(prefix, id, date) {
    const year = String(date || today()).slice(0, 4);
    return `${prefix}-${year}-${String(id).padStart(5, "0")}`;
  }

  function filterInvoicesByDate(invoices) {
    const { from, to } = state.invoiceDateRange;
    return invoices.filter(invoice => (!from || invoice.saleDate >= from) && (!to || invoice.saleDate <= to));
  }

  function monthlyRevenue(sales, affiliations, plans, salesRevenue = null) {
    const month = today().slice(0, 7);
    const productSales = salesRevenue ?? sales
      .filter(item => !item.invoiceCancelled && String(item.saleDate || "").startsWith(month))
      .reduce((sum, item) => sum + Number(item.totalValue || 0), 0);
    const affiliationRevenue = affiliations
      .filter(item => !item.invoiceCancelled && String(item.affiliationDate || "").startsWith(month))
      .reduce((sum, affiliation) => {
        const plan = plans.find(item => String(item.idTypePlan) === String(affiliation.typePlanId));
        return sum + Number(affiliation.paidValue ?? plan?.value ?? 0);
      }, 0);
    return Number(productSales) + affiliationRevenue;
  }

  function cellValue(row, key) {
    const value = row[key];
    if (key === "photoData") return value
      ? `<img class="record-photo" src="${escapeHtml(value)}" alt="Foto" loading="lazy">`
      : `<span class="muted">Sin foto</span>`;
    if (key === "products" && row.invoiceCancelled) return `<span class="pill warn">ANULADA</span> ${escapeHtml(value?.map(item => `${item.productName} ×${item.amount}`).join(", ") || "Factura")}`;
    if (key === "saleDate" || key === "affiliationDate" || key === "endAffiliation") return escapeHtml(formatDate(value));
    if (key === "value" || key === "totalValue") return `${row.invoiceCancelled ? `<span class="pill warn">Anulada</span> ` : ""}${escapeHtml(formatMoney(value))}`;
    if (key === "products" && Array.isArray(value)) return escapeHtml(value.map(item => `${item.productName} ×${item.amount}`).join(", "));
    if (key === "services" && Array.isArray(value)) return escapeHtml(value.map(item => item.serviceName).join(", ") || "Sin servicios");
    if (key === "coaches" && Array.isArray(value)) return escapeHtml(value.map(item => item.coachName).join(", ") || "Sin asignar");
    if (key === "invoiceCancelled") return value ? `<span class="pill warn">Anulada</span>` : `<span class="pill neutral">Pagada</span>`;
    if (key === "role") return `<span class="pill neutral">${escapeHtml(value || "USER")}</span>`;
    if (key === "amount" && Number(value) <= 5) return `<span class="pill warn">${escapeHtml(value)}</span>`;
    return escapeHtml(value ?? "—");
  }

  function tableHtml(rows, columns, key) {
    if (!Array.isArray(rows) || !rows.length) return `<div class="empty-state">No hay registros todavía. Usa el botón para crear el primero.</div>`;
    return `<div class="table-wrap"><table><thead><tr>${columns.map(([, label]) => `<th>${escapeHtml(label)}</th>`).join("")}<th>Acciones</th></tr></thead><tbody>
      ${rows.map(row => `<tr>${columns.map(([field]) => `<td>${cellValue(row, field)}</td>`).join("")}
        <td><div class="table-actions">${key === "sales" || key === "sale" ? `<button class="button small" data-action="${row.invoiceType === "affiliation" ? "invoice-affiliation" : "invoice"}" data-id="${escapeHtml(row.invoiceType === "affiliation" ? row.affiliationId : row.id)}">Factura</button>` : ""}
        ${key === "sales" && row.invoiceType !== "affiliation" && !row.invoiceCancelled ? `<button class="button small" data-action="edit-sale" data-id="${escapeHtml(row.id)}">Editar</button>` : ""}
        ${key === "sales" && !row.invoiceCancelled ? `<button class="button small danger" data-action="delete-invoice" data-type="${escapeHtml(row.invoiceType)}" data-id="${escapeHtml(row.invoiceType === "affiliation" ? row.affiliationId : row.id)}" data-renewal="${Boolean(row.renewal)}">Anular</button>` : ""}
        ${key === "sale" ? "" : key !== "sales" ? `${!(key === "affiliations" && row.invoiceCancelled) ? `<button class="button small" data-action="edit" data-resource="${escapeHtml(key)}" data-id="${escapeHtml(row[resources[key].id])}">Editar</button>` : ""}${key === "affiliations" ? !row.invoiceCancelled ? `<button class="button small danger" data-action="delete-invoice" data-type="affiliation" data-renewal="${Boolean(row.renewal)}" data-resource="${escapeHtml(key)}" data-id="${escapeHtml(row[resources[key].id])}">Anular factura</button>` : "" : `<button class="button small danger" data-action="delete" data-resource="${escapeHtml(key)}" data-id="${escapeHtml(row[resources[key].id])}">Eliminar</button>`}` : ""}
        </div></td></tr>`).join("")}
    </tbody></table></div>`;
  }

  function filteredTable(query) {
    const key = state.view;
    const config = resources[key];
    const rows = key === "sales" ? state.cache.invoiceRows || [] : state.cache[key] || [];
    const baseRows = key === "sales" ? filterInvoicesByDate(rows) : rows;
    const filtered = baseRows.filter(row => JSON.stringify(row).toLocaleLowerCase("es").includes(query.toLocaleLowerCase("es")));
    document.querySelector("#resource-table").innerHTML = tableHtml(filtered, config.columns, key);
  }

  function fieldMarkup(field, value = "", edit = false) {
    const isPasswordEdit = edit && field.name === "password";
    const required = (field.required && !(edit && field.createOnlyRequired) || field.name === "password" && !edit) && !isPasswordEdit ? "required" : "";
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
    if (field.type === "photo") {
      const preview = value
        ? `<img class="photo-preview" src="${escapeHtml(value)}" alt="Vista previa de la foto">`
        : `<span class="photo-placeholder">Sin foto seleccionada</span>`;
      return `<div class="${wide} photo-field"><span class="field-label">${escapeHtml(field.label)}</span><div class="photo-actions"><button class="button photo-action" type="button" data-photo-picker="photoFile"><svg class="photo-icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M12 16V4m0 0L7 9m5-5 5 5"/><path d="M5 14v5a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1v-5"/></svg><span>Adjuntar foto</span></button><button class="button photo-action" type="button" data-photo-camera><svg class="photo-icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M4 7h3l2-3h6l2 3h3a2 2 0 0 1 2 2v10H2V9a2 2 0 0 1 2-2Z"/><circle cx="12" cy="13" r="3.5"/></svg><span>Usar cámara</span></button></div><input class="photo-input-hidden" type="file" name="photoFile" accept="image/*" data-photo-input><input type="hidden" name="photoData" value="${escapeHtml(value)}"><div class="camera-panel" data-camera-panel hidden><video class="camera-preview" data-camera-video autoplay playsinline></video><div class="photo-actions"><button class="button small primary" type="button" data-camera-capture>Tomar foto</button><button class="button small" type="button" data-camera-stop>Cerrar cámara</button></div></div><small class="muted">Opcional. Adjunta una imagen o captura usando la cámara conectada.</small><span class="photo-preview-wrap" data-photo-preview>${preview}</span></div>`;
    }
    const help = field.help ? `<small class="muted">${escapeHtml(field.help)}</small>` : "";
    const safeValue = isPasswordEdit ? "" : value;
    if (field.type === "textarea") return `<label class="${wide}">${escapeHtml(field.label)}<textarea name="${escapeHtml(field.name)}">${escapeHtml(value)}</textarea>${help}</label>`;
    const type = field.type || "text";
    const autocomplete = type === "password" ? "autocomplete=\"new-password\"" : "";
    return `<label class="${wide}">${escapeHtml(field.label)}<input name="${escapeHtml(field.name)}" type="${type}" value="${escapeHtml(safeValue)}" ${required} ${min} ${step} ${autocomplete}>${help}</label>`;
  }

  async function openResourceForm(key, id = null) {
    const config = resources[key];
    if (!config) return;
    try {
      for (const field of config.fields) {
        if (field.source && !state.cache[field.source]) await load(field.source);
      }
      let row = null;
      if (id !== null) row = (state.cache[key] || []).find(item => String(item[config.id]) === String(id)) || await api(`${config.path}/${id}`);
      if (key === "affiliations" && row?.coaches) {
        row = {
          ...row,
          coachIds: row.coaches.map(coach => coach.coachId),
          coachStartDate: row.coaches[0]?.startDate || "",
          coachEndDate: row.coaches[0]?.endDate || ""
        };
      }
      const fields = config.fields
        .filter(field => !(key === "users" && id === null && field.name === "role"))
        .map(field => fieldMarkup(field, row?.[field.name] ?? "", id !== null))
        .join("");
      openDialog(`${id ? "Editar" : "Nuevo"} ${config.singular}`, `${id && key === "users" ? "La contraseña solo cambia si la escribes. Déjala vacía para conservar la actual." : "Completa los datos y guarda los cambios."}`,
        `<div class="form-grid">${fields}</div>`,
        async formData => {
          const form = Object.fromEntries(formData.entries());
          form.photoData = formData.get("photoData") || null;
          const checked = [...dialogForm.querySelectorAll('input[type="checkbox"]:checked')].map(input => input.value);
          const payload = config.payload ? config.payload(form, checked) : form;
          let affiliationInvoice = null;
          if (key === "affiliations" && id === null) {
            const plan = (state.cache.plans || []).find(item => String(item.idTypePlan) === String(form.typePlanId));
            if (!plan || plan.value == null) throw new Error("No se encontró el precio del plan para generar el comprobante.");
            const affiliation = await api(config.path, { method: "POST", body: JSON.stringify(payload) });
            affiliationInvoice = { affiliation, plan };
          } else {
            await api(id ? `${config.path}/${id}` : config.path, { method: id ? "PUT" : "POST", body: JSON.stringify(payload) });
          }
          state.cache[key] = null;
          notify(`${config.singular[0].toUpperCase()}${config.singular.slice(1)} ${id ? "actualizado" : "creado"} correctamente.`);
          renderView();
          if (affiliationInvoice) {
            window.setTimeout(() => showAffiliationInvoice(affiliationInvoice.affiliation, affiliationInvoice.plan), 0);
          }
        }, "Guardar", key !== "users" || id !== null);
    } catch (error) {
      notify(error.message, true);
    }
  }

  function readPhotoFile(file) {
    return new Promise((resolve, reject) => {
      const image = new Image();
      const objectUrl = URL.createObjectURL(file);
      image.onload = async () => {
        URL.revokeObjectURL(objectUrl);
        const canvas = document.createElement("canvas");
        const context = canvas.getContext("2d");
        if (!context) {
          reject(new Error("El navegador no pudo procesar la foto."));
          return;
        }
        try {
          let blob = null;
          for (let maxDimension = 1280; maxDimension >= 320; maxDimension = Math.floor(maxDimension * 0.75)) {
            const scale = Math.min(1, maxDimension / Math.max(image.naturalWidth, image.naturalHeight));
            canvas.width = Math.max(1, Math.round(image.naturalWidth * scale));
            canvas.height = Math.max(1, Math.round(image.naturalHeight * scale));
            context.drawImage(image, 0, 0, canvas.width, canvas.height);
            for (let quality = 0.88; quality >= 0.48; quality -= 0.1) {
              blob = await new Promise(resolveBlob => canvas.toBlob(resolveBlob, "image/jpeg", quality));
              if (blob && blob.size <= 1024 * 1024) break;
            }
            if (blob && blob.size <= 1024 * 1024) break;
          }
          if (!blob || blob.size > 1024 * 1024) {
            reject(new Error("No se pudo reducir la foto a un tamaño compatible. Prueba con otra imagen."));
            return;
          }
          const reader = new FileReader();
          reader.onload = () => resolve(String(reader.result));
          reader.onerror = () => reject(new Error("No se pudo leer la foto procesada."));
          reader.readAsDataURL(blob);
        } catch (error) {
          reject(new Error("No se pudo procesar la foto seleccionada."));
        }
      };
      image.onerror = () => {
        URL.revokeObjectURL(objectUrl);
        reject(new Error("El archivo seleccionado no es una imagen compatible."));
      };
      image.src = objectUrl;
    });
  }

  function stopPhotoCamera() {
    if (photoCameraStream) {
      photoCameraStream.getTracks().forEach(track => track.stop());
      photoCameraStream = null;
    }
    const video = dialogForm.querySelector("[data-camera-video]");
    if (video) video.srcObject = null;
    const cameraPanel = dialogForm.querySelector("[data-camera-panel]");
    if (cameraPanel) cameraPanel.hidden = true;
  }

  async function startPhotoCamera() {
    if (!navigator.mediaDevices?.getUserMedia) {
      notify("No es posible tomar una foto: este navegador o dispositivo no permite acceder a una cámara.", true);
      return;
    }
    if (!window.isSecureContext) {
      notify("Para usar la cámara, abre la aplicación desde localhost o una conexión HTTPS.", true);
      return;
    }
    stopPhotoCamera();
    try {
      if (navigator.mediaDevices.enumerateDevices) {
        const devices = await navigator.mediaDevices.enumerateDevices();
        if (!devices.some(device => device.kind === "videoinput")) {
          notify("No es posible tomar una foto: el dispositivo no tiene una cámara disponible o conectada.", true);
          return;
        }
      }
      photoCameraStream = await navigator.mediaDevices.getUserMedia({
        audio: false,
        video: { facingMode: { ideal: "environment" } }
      });
      const video = dialogForm.querySelector("[data-camera-video]");
      const cameraPanel = dialogForm.querySelector("[data-camera-panel]");
      if (!video || !cameraPanel) {
        stopPhotoCamera();
        return;
      }
      video.srcObject = photoCameraStream;
      cameraPanel.hidden = false;
      await video.play();
    } catch (error) {
      stopPhotoCamera();
      if (error.name === "NotFoundError" || error.name === "DevicesNotFoundError") {
        notify("No es posible tomar una foto: el dispositivo no tiene una cámara disponible o conectada.", true);
      } else if (error.name === "NotAllowedError" || error.name === "PermissionDeniedError") {
        notify("No se pudo acceder a la cámara. Permite el acceso a la cámara en el navegador e inténtalo de nuevo.", true);
      } else if (error.name === "NotReadableError" || error.name === "TrackStartError") {
        notify("La cámara está ocupada por otra aplicación. Ciérrala e inténtalo de nuevo.", true);
      } else {
        notify(`No se pudo iniciar la cámara: ${error.message || "error desconocido"}.`, true);
      }
    }
  }

  async function capturePhotoFromCamera() {
    const video = dialogForm.querySelector("[data-camera-video]");
    if (!video || !photoCameraStream || !video.videoWidth || !video.videoHeight) {
      notify("La cámara todavía no está lista. Espera un momento e inténtalo de nuevo.", true);
      return;
    }
    const canvas = document.createElement("canvas");
    canvas.width = video.videoWidth;
    canvas.height = video.videoHeight;
    const context = canvas.getContext("2d");
    if (!context) {
      notify("No se pudo capturar la imagen de la cámara.", true);
      return;
    }
    context.drawImage(video, 0, 0, canvas.width, canvas.height);
    const blob = await new Promise(resolve => canvas.toBlob(resolve, "image/jpeg", 0.9));
    if (!blob) {
      notify("No se pudo generar la foto capturada.", true);
      return;
    }
    const file = new File([blob], "captura.jpg", { type: "image/jpeg" });
    const generation = Number(dialogForm.dataset.photoGeneration || 0) + 1;
    dialogForm.dataset.photoGeneration = String(generation);
    dialogForm.dataset.photoProcessing = "true";
    try {
      const dataUrl = await readPhotoFile(file);
      if (dialogForm.dataset.photoGeneration !== String(generation)) return;
      const photoData = dialogForm.querySelector('input[name="photoData"]');
      const preview = dialogForm.querySelector("[data-photo-preview]");
      if (photoData) photoData.value = dataUrl;
      if (preview) preview.innerHTML = `<img class="photo-preview" src="${escapeHtml(dataUrl)}" alt="Vista previa de la foto">`;
      stopPhotoCamera();
    } catch (error) {
      notify(error.message, true);
    } finally {
      if (dialogForm.dataset.photoGeneration === String(generation)) {
        dialogForm.dataset.photoProcessing = "false";
      }
    }
  }

  function openDialog(title, subtitle, body, onSubmit, submitText = "Guardar", closeOnBackdrop = true) {
    dialog.dataset.closeOnBackdrop = String(closeOnBackdrop);
    dialogForm.dataset.photoProcessing = "false";
    dialogForm.dataset.photoGeneration = "0";
    dialogForm.innerHTML = `<div class="dialog-inner"><div class="dialog-head"><div><h2>${escapeHtml(title)}</h2><p>${escapeHtml(subtitle)}</p></div><button class="icon-button" type="button" data-dialog-close aria-label="Cerrar">×</button></div>${body}<div class="dialog-actions"><button class="button" type="button" data-dialog-close>Cancelar</button><button class="button primary" type="submit">${escapeHtml(submitText)}</button></div></div>`;
    dialogForm.onsubmit = async event => {
      event.preventDefault();
      const submitButton = dialogForm.querySelector('[type="submit"]');
      submitButton.disabled = true;
      try {
        if (dialogForm.dataset.photoProcessing === "true") {
          throw new Error("Espera a que termine el procesamiento de la foto.");
        }
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
        <label class="wide">Forma de pago<select name="paymentMethod" required><option value="">Selecciona...</option><option value="CASH" ${sale?.paymentMethod === "CASH" ? "selected" : ""}>Efectivo</option><option value="CARD" ${sale?.paymentMethod === "CARD" ? "selected" : ""}>Tarjeta</option><option value="TRANSFER" ${sale?.paymentMethod === "TRANSFER" ? "selected" : ""}>Transferencia</option></select></label>
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
          const payload = {
            userId: Number(formData.get("userId")),
            paymentMethod: String(formData.get("paymentMethod") || ""),
            products: saleProducts
          };
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

  function showAffiliationInvoice(affiliation, plan) {
    const coaches = (affiliation.coaches || []).map(coach => escapeHtml(coach.coachName)).join(", ") || "Sin entrenador asignado";
    const total = affiliation.paidValue ?? plan.value;
    const number = formatInvoiceNumber("AF", affiliation.id, affiliation.affiliationDate);
    openDialog(`Factura ${number}`, "Comprobante de afiliación",
      invoicePaper(number, affiliation.affiliationDate, affiliation.userName, affiliation.userDocumentNumber,
        affiliation.userPhoneNumber, affiliation.paymentMethod,
        `<div class="table-wrap"><table><thead><tr><th>Plan</th><th>Vigencia</th><th>Entrenadores</th><th>Valor</th></tr></thead><tbody><tr><td>${escapeHtml(affiliation.typePlanName)}</td><td>${formatDate(affiliation.affiliationDate)} - ${formatDate(affiliation.endAffiliation)}</td><td>${coaches}</td><td>${formatMoney(total)}</td></tr></tbody></table></div>`, total),
      async () => {}, "Imprimir");
    const submit = dialogForm.querySelector('[type="submit"]');
    submit.type = "button";
    submit.addEventListener("click", () => window.print());
  }

  async function showSavedAffiliationInvoice(id) {
    try {
      const affiliation = await api(`/affiliation/${id}`);
      const plans = await load("plans");
      const plan = plans.find(item => String(item.idTypePlan) === String(affiliation.typePlanId));
      if (!plan && affiliation.paidValue == null) throw new Error("No se encontró el plan ni el valor pagado de esta afiliación.");
      showAffiliationInvoice(affiliation, plan || { value: affiliation.paidValue });
    } catch (error) {
      notify(error.message, true);
    }
  }

  async function showInvoice(id) {
    try {
      const invoice = await api(`/sales/${id}/factura`);
      const number = formatInvoiceNumber("F", invoice.id, invoice.saleDate);
      const lines = (invoice.products || []).map(product => `<tr><td>${escapeHtml(product.productName)}</td><td>${escapeHtml(product.amount)}</td><td>${formatMoney(product.unitPrice)}</td><td>${formatMoney(product.subtotal)}</td></tr>`).join("");
      openDialog(`Factura ${number}`, "Comprobante de venta",
        invoicePaper(number, invoice.saleDate, invoice.userName, invoice.userDocumentNumber,
          invoice.userPhoneNumber, invoice.paymentMethod,
          `<div class="table-wrap"><table><thead><tr><th>Producto</th><th>Cant.</th><th>Precio</th><th>Subtotal</th></tr></thead><tbody>${lines || `<tr><td colspan="4">Sin productos</td></tr>`}</tbody></table></div>`, invoice.totalValue),
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

  function invoicePaper(number, date, customer, documentNumber, phoneNumber, paymentMethod, lines, total) {
    return `<article class="invoice-paper">
      <header class="invoice-brand"><img src="assets/nox3-fitness-logo.png" alt="NOX 3 Fitness"><div><strong>NOX 3 FITNESS</strong><span>Teléfono: 3142352292</span><span>Factura ${escapeHtml(number)}</span><span>Fecha: ${formatDate(date)}</span></div></header>
      <section class="invoice-customer"><h3>Datos del cliente</h3><p><strong>Nombre:</strong> ${escapeHtml(customer || "No registrado")}</p><p><strong>Cédula:</strong> ${escapeHtml(documentNumber || "No registrada")}</p><p><strong>Teléfono:</strong> ${escapeHtml(phoneNumber ?? "No registrado")}</p><p><strong>Forma de pago:</strong> ${escapeHtml(paymentMethodLabel(paymentMethod))}</p></section>
      ${lines}<p class="invoice-total">Total pagado: ${formatMoney(total)}</p>
      <footer class="invoice-footer">Gracias por entrenar con NOX 3 Fitness · 3142352292</footer>
    </article>`;
  }

  function paymentMethodLabel(method) {
    return ({ CASH: "Efectivo", CARD: "Tarjeta", TRANSFER: "Transferencia" })[method] || "No registrada";
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

  function deleteInvoice(type, id, renewal = false) {
    const endpoint = type === "affiliation" ? `/affiliation/${id}` : `/sales/${id}`;
    const description = type === "affiliation"
      ? renewal
        ? "Se anulará el cobro y la renovación quedará inactiva; el historial de afiliación se conservará."
        : "Se anulará la factura y se eliminará la afiliación nueva."
      : "Se anulará la venta y se devolverán sus productos al inventario.";
    openDialog("Anular factura", description,
      `<label>Contraseña especial<input name="password" type="password" required autocomplete="off"></label>`,
      async formData => {
        await api(endpoint, { method: "DELETE", body: JSON.stringify({ password: String(formData.get("password") || "") }) });
        state.cache.sales = null;
        state.cache.invoiceRows = null;
        state.cache.affiliations = null;
        if (type === "sale") state.cache.products = null;
        notify(type === "affiliation"
          ? renewal ? "Factura anulada; la afiliación quedó conservada e inactiva." : "Factura y afiliación nueva anuladas."
          : "Factura anulada y productos devueltos al inventario.");
        renderView();
      }, "Confirmar anulación");
  }

  function setInvoicePeriod(period) {
    if (period === "all") {
      state.invoiceDateRange = { from: "", to: "" };
    } else if (period === "current-month") {
      state.invoiceDateRange = { from: `${today().slice(0, 7)}-01`, to: today() };
    } else if (period === "current-year") {
      state.invoiceDateRange = { from: `${today().slice(0, 4)}-01-01`, to: `${today().slice(0, 4)}-12-31` };
    } else {
      const now = new Date();
      const firstDay = new Date(now.getFullYear(), now.getMonth() - 1, 1);
      const lastDay = new Date(now.getFullYear(), now.getMonth(), 0);
      state.invoiceDateRange = { from: localDate(firstDay), to: localDate(lastDay) };
    }
    renderSales();
  }

  function localDate(date) {
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`;
  }

  function applyInvoiceDateRange() {
    const from = pageContent.querySelector('[name="invoice-from"]').value;
    const to = pageContent.querySelector('[name="invoice-to"]').value;
    if (from && to && from > to) {
      notify("La fecha inicial no puede ser posterior a la fecha final.", true);
      return;
    }
    state.invoiceDateRange = { from, to };
    renderSales();
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
      if (response.status === 404) {
        throw new Error("Vercel no encuentra el API de Spring Boot. Configura APP_API_BASE_URL con la URL pública del backend terminada en /api y vuelve a desplegar. Vercel solo aloja el frontend.");
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
      if (dialog.open) dialog.close();
      state.view = viewButton.dataset.view;
      renderView();
      return;
    }
    const closeButton = event.target.closest("[data-dialog-close]");
    if (closeButton) { dialog.close(); return; }
    const actionButton = event.target.closest("[data-action]");
    if (!actionButton) return;
    const { action, resource, id } = actionButton.dataset;
    if (action === "mobile-menu") { openMobileMenu(); return; }
    if (action === "logout") { if (dialog.open) dialog.close(); logout(); return; }
    if (action === "new-sale") { await openSaleForm(); return; }
    if (action === "create-affiliation") { state.view = "affiliations"; renderView(); setTimeout(() => openResourceForm("affiliations"), 0); return; }
    if (action === "create") { await openResourceForm(state.view); return; }
    if (action === "edit") { await openResourceForm(resource, id); return; }
    if (action === "delete") { await deleteResource(resource, id); return; }
    if (action === "invoice") { await showInvoice(id); return; }
    if (action === "invoice-affiliation") { await showSavedAffiliationInvoice(id); return; }
    if (action === "edit-sale") { await openSaleForm(id); return; }
    if (action === "delete-invoice") { deleteInvoice(actionButton.dataset.type || resource, id, actionButton.dataset.renewal === "true"); return; }
    if (action === "apply-invoice-range") { applyInvoiceDateRange(); return; }
    if (action === "invoice-period") { setInvoicePeriod(actionButton.dataset.period); return; }
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
    if (event.target.matches("[data-photo-input]")) {
      const file = event.target.files?.[0];
      const preview = dialogForm.querySelector("[data-photo-preview]");
      if (!preview) return;
      if (!file) {
        return;
      }
      if (!file.type.startsWith("image/")) {
        event.target.value = "";
        notify("Selecciona un archivo de imagen válido.", true);
        return;
      }
      const otherInput = [...dialogForm.querySelectorAll("[data-photo-input]")].find(input => input !== event.target);
      if (otherInput) otherInput.value = "";
      const photoData = dialogForm.querySelector('input[name="photoData"]');
      if (photoData) photoData.value = "";
      preview.innerHTML = `<img class="photo-preview" alt="Vista previa de la foto">`;
      const generation = Number(dialogForm.dataset.photoGeneration || 0) + 1;
      dialogForm.dataset.photoGeneration = String(generation);
      dialogForm.dataset.photoProcessing = "true";
      readPhotoFile(file).then(dataUrl => {
        if (dialogForm.dataset.photoGeneration !== String(generation)) return;
        const image = preview.querySelector("img");
        if (image) image.src = dataUrl;
        if (photoData) photoData.value = dataUrl;
      }).catch(error => {
        if (dialogForm.dataset.photoGeneration === String(generation)) {
          notify(error.message, true);
          preview.innerHTML = `<span class="photo-placeholder">No se pudo procesar la foto</span>`;
        }
      }).finally(() => {
        if (dialogForm.dataset.photoGeneration === String(generation)) {
          dialogForm.dataset.photoProcessing = "false";
        }
      });
    }
  });
  dialogForm.addEventListener("click", event => {
    const picker = event.target.closest("[data-photo-picker]");
    if (picker) {
      const input = dialogForm.querySelector(`input[name="${picker.dataset.photoPicker}"]`);
      if (input) input.click();
      return;
    }
    if (event.target.closest("[data-photo-camera]")) {
      startPhotoCamera();
      return;
    }
    if (event.target.closest("[data-camera-capture]")) {
      capturePhotoFromCamera();
      return;
    }
    if (event.target.closest("[data-camera-stop]")) {
      stopPhotoCamera();
    }
  });
  dialog.addEventListener("close", stopPhotoCamera);
  dialog.addEventListener("click", event => {
    if (event.target === dialog && dialog.dataset.closeOnBackdrop !== "false") dialog.close();
  });

  if (state.auth) {
    api("/users").then(() => showApp()).catch(() => logout("La sesión expiró. Ingresa nuevamente."));
  } else {
    showLogin();
  }
})();
