export function renderPublicSidebarWidget(containerId, options = {}) {
    const container = document.getElementById(containerId);
    const onNavigate = typeof options.onNavigate === "function" ? options.onNavigate : null;

    if (!container) {
        return;
    }

    container.innerHTML = `
<aside class="sidebar" id="sidebar">
  <div class="sidebar-top">
    <button class="sidebar-logo" type="button" data-view="home">
      <div class="sidebar-name">Refúgio Cantinho<br>das Lavandas</div>
      <div class="sidebar-place">Monte Verde · MG · 1.600m</div>
    </button>
  </div>

  <nav class="sidebar-nav">
    <div class="nav-section-label">Principal</div>
    <a class="nav-item active" id="ni-home" data-view="home">
      <span class="ni-text">Início</span>
      <span class="ni-dot"></span>
    </a>
    <a class="nav-item" id="ni-acomodacoes" data-view="acomodacoes">
      <span class="ni-text">Acomodações</span>
      <span class="ni-dot"></span>
    </a>
    <a class="nav-item" id="ni-experiencias" data-view="experiencias">
      <span class="ni-text">Experiências</span>
      <span class="ni-dot"></span>
    </a>
    <a class="nav-item" id="ni-destino" data-view="destino">
      <span class="ni-text">Monte Verde</span>
      <span class="ni-dot"></span>
    </a>
    <a class="nav-item" id="ni-galeria" data-view="galeria">
      <span class="ni-text">Galeria</span>
      <span class="ni-dot"></span>
    </a>

    <div class="nav-section-label" style="margin-top:24px">Informações</div>
    <a class="nav-item" id="ni-faq" data-view="faq">
      <span class="ni-text">Perguntas freq.</span>
      <span class="ni-dot"></span>
    </a>
    <a class="nav-item" id="ni-contato" data-view="contato">
      <span class="ni-text">Contato</span>
      <span class="ni-dot"></span>
    </a>
  </nav>

  <div class="sidebar-footer">
    <button class="sidebar-cta" type="button" data-view="reserva">Reservar agora</button>
    <div class="sidebar-socials">
      <a class="soc-link" href="https://www.instagram.com/cantinhodaslavandas.mv/" target="_blank" rel="noreferrer">
        <span class="soc-icon soc-icon-instagram" aria-hidden="true"></span>
        <span>Insta</span>
      </a>
      <a class="soc-link" href="https://wa.me/5512992525319" target="_blank" rel="noreferrer">
        <span class="soc-icon soc-icon-whatsapp" aria-hidden="true"></span>
        <span>WhatsApp</span>
      </a>
    </div>
  </div>
</aside>
    `;

    container.querySelectorAll("[data-view]").forEach((item) => {
        item.addEventListener("click", () => {
            if (onNavigate) {
                onNavigate(item.dataset.view);
            }
        });
    });
}

export function setPublicSidebarActive(view) {
    document.querySelectorAll(".nav-item").forEach((item) => item.classList.remove("active"));
    document.getElementById(`ni-${view}`)?.classList.add("active");
}
