export function renderPublicFooterWidget(containerId) {
    const container = document.getElementById(containerId);

    if (!container) {
        return;
    }

    container.innerHTML = `
<footer class="site-footer" id="siteFooter">
  <div class="sf-grid">
    <div class="sf-brand">
      <div class="sf-logo">Refúgio Cantinho das Lavandas</div>
      <div class="sf-tagline">Monte Verde · Serra da Mantiqueira · 1.600m</div>
      <p class="sf-desc">Uma pousada boutique onde o frio das montanhas encontra o calor genuíno do aconchego mineiro. Lavandas, lareira e memórias para a vida toda.</p>
      <div class="sf-social">
        <a class="sfs-btn" href="https://www.instagram.com/cantinhodaslavandas.mv/" target="_blank" rel="noreferrer">Instagram</a>
        <a class="sfs-btn" href="https://wa.me/5512992525319" target="_blank" rel="noreferrer">WhatsApp</a>
      </div>
    </div>
    <div>
      <div class="sf-col-title">Navegação</div>
      <div class="sf-links">
        <a class="sf-link" onclick="goPage('home')">Início</a>
        <a class="sf-link" onclick="goPage('acomodacoes')">Acomodações</a>
        <a class="sf-link" onclick="goPage('experiencias')">Experiências</a>
        <a class="sf-link" onclick="goPage('destino')">Monte Verde</a>
        <a class="sf-link" onclick="goPage('galeria')">Galeria</a>
        <a class="sf-link" onclick="goPage('faq')">FAQ</a>
        <a class="sf-link" onclick="goPage('contato')">Contato</a>
        <a class="sf-link" onclick="goPage('reserva')">Reservar agora</a>
      </div>
    </div>
    <div>
      <div class="sf-col-title">Informações</div>
      <div class="sf-links">
        <a class="sf-link">📍 Rua Mercúrio, 162 · Monte Verde, MG · CEP 37653-000</a>
        <a class="sf-link" href="https://wa.me/5512992525319" target="_blank" rel="noreferrer">📞 +55 12 99252-5319</a>
        <a class="sf-link">🕐 Seg–Dom · 7h às 22h</a>
        <a class="sf-link" style="margin-top:12px" href="#politica-de-privacidade" onclick="event.preventDefault();goPage('privacidade')">Política de privacidade</a>
        <a class="sf-link">Termos de hospedagem</a>
      </div>
    </div>
  </div>
  <div class="sf-bottom">
    <span class="sf-copy">© 2025 Refúgio Cantinho das Lavandas. Todos os direitos reservados.</span>
    <span class="sf-copy">Camanducaia · Minas Gerais · Brasil 🇧🇷</span>
  </div>
</footer>

    `;
}

export function clearPublicFooterWidget(containerId) {
    const container = document.getElementById(containerId);

    if (container) {
        container.innerHTML = "";
    }
}
