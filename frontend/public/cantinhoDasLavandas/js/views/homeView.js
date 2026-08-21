export function renderHomeView(containerId) {
    const container = document.getElementById(containerId);

    if (!container) {
        return;
    }

    container.innerHTML = `
<!-- ══ PAGE: HOME ══ -->
<div class="page active" id="page-home">

  <section class="home-hero">
    <div class="home-bg"></div>
    <canvas class="stars-canvas" id="starsCanvas"></canvas>
    <img class="home-hero-logo" src="assets/logo/logo.svg?v=2026060802" alt="Cantinho das Lavandas">

    <!-- SVG Montanhas Serrana -->
    <svg class="mountains-svg" viewBox="0 0 1200 500" preserveAspectRatio="none" xmlns="http://www.w3.org/2000/svg">
      <defs>
        <linearGradient id="m1" x1="0%" y1="0%" x2="0%" y2="100%">
          <stop offset="0%" stop-color="#253a20"/><stop offset="100%" stop-color="#141e10"/>
        </linearGradient>
        <linearGradient id="m2" x1="0%" y1="0%" x2="0%" y2="100%">
          <stop offset="0%" stop-color="#1a2e16"/><stop offset="100%" stop-color="#0e140a"/>
        </linearGradient>
        <linearGradient id="m3" x1="0%" y1="0%" x2="0%" y2="100%">
          <stop offset="0%" stop-color="#101a0e"/><stop offset="100%" stop-color="#080c06"/>
        </linearGradient>
      </defs>
      <!-- Camada 1 - distante -->
      <path d="M0 300 L100 240 L220 280 L340 210 L460 250 L580 185 L700 230 L820 190 L940 235 L1060 200 L1180 240 L1200 235 L1200 500 L0 500Z" fill="url(#m1)" opacity="0.6"/>
      <!-- Camada 2 - média -->
      <path d="M0 360 L80 310 L200 345 L320 280 L450 325 L570 265 L690 315 L810 270 L930 320 L1060 280 L1160 320 L1200 308 L1200 500 L0 500Z" fill="url(#m2)" opacity="0.8"/>
      <!-- Camada 3 - próxima -->
      <path d="M0 420 L60 385 L160 410 L280 355 L420 400 L540 360 L660 405 L780 365 L900 410 L1040 370 L1140 408 L1200 390 L1200 500 L0 500Z" fill="url(#m3)"/>
      <!-- Luna -->
      <circle cx="920" cy="80" r="44" fill="#f0e8d8" opacity="0.15"/>
      <circle cx="920" cy="80" r="36" fill="#f8f0e0" opacity="0.2"/>
    </svg>

    <div class="hero-main">
      <div class="hero-text">
        <div class="hero-kicker">Serra da Mantiqueira · 1.600m de altitude</div>
        <h1 class="hero-h1">
          Entre<br>lavandas<br><em>e montanhas</em>
        </h1>
        <p class="hero-sub">Uma pousada boutique onde o frio das montanhas encontra o calor do aconchego</p>
        <div class="hero-btns">
          <button class="hbtn-primary" onclick="goPage('reserva')">Reservar agora</button>
          <button class="hbtn-outline" onclick="goPage('acomodacoes')">Ver a casa</button>
        </div>
      </div>
    </div>

    <div class="hero-strip">
      <div class="hs-item" onclick="goPage('acomodacoes')"><div class="hs-num">1</div><div class="hs-label">Casa privativa</div></div>
      <div class="hs-item" onclick="goPage('acomodacoes')"><div class="hs-num">2</div><div class="hs-label">Quartos</div></div>
      <div class="hs-item" onclick="goPage('acomodacoes')"><div class="hs-num">4</div><div class="hs-label">Hóspedes</div></div>
      <div class="hs-item" onclick="goPage('acomodacoes')"><div class="hs-num">5</div><div class="hs-label">Ambientes internos</div></div>
    </div>
  </section>

  <!-- Teaser: Acomodações -->
  <div class="home-teaser">
    <div class="ht-left">
      <div class="section-eyebrow">Hospede-se</div>
      <h2 class="section-h2">Uma casa inteira,<br><em>só para você</em></h2>
      <p class="section-body">A pousada recebe uma reserva por vez em uma casa privativa com quarto de casal, quarto para duas pessoas em camas de solteiro, sala, cozinha equipada e banheiro.</p>
      <div class="home-house-highlights">
        <span>1 quarto casal</span>
        <span>1 quarto duplo solteiro</span>
        <span>Sala</span>
        <span>Cozinha</span>
        <span>Banheiro</span>
      </div>
      <div class="home-accommodation-carousel" onclick="goPage('acomodacoes')">
        <div class="hac-track">
          <figure class="hac-slide">
            <img src="assets/images/QuartoCasal.jpeg" alt="Quarto de casal do Refúgio Cantinho das Lavandas">
            <figcaption>Quarto casal</figcaption>
          </figure>
          <figure class="hac-slide">
            <img src="assets/images/CantinhoSala.jpeg" alt="Sala aconchegante do Refúgio Cantinho das Lavandas">
            <figcaption>Sala aconchegante</figcaption>
          </figure>
          <figure class="hac-slide">
            <img src="assets/images/CozinhaMesa.jpeg" alt="Mesa da cozinha preparada para hospedagem">
            <figcaption>Cozinha equipada</figcaption>
          </figure>
          <figure class="hac-slide">
            <img src="assets/images/Jardim.jpeg" alt="Jardim do Refúgio Cantinho das Lavandas">
            <figcaption>Jardim</figcaption>
          </figure>
        </div>
        <div class="hac-dots" aria-hidden="true">
          <span></span>
          <span></span>
          <span></span>
          <span></span>
        </div>
      </div>
      <a class="section-link" style="margin-top:28px" onclick="goPage('acomodacoes')">Ver detalhes da casa</a>
    </div>
  </div>

  <!-- Teaser: Experiências -->
  <div class="home-exp">
    <div>
      <div class="section-eyebrow" style="color:var(--lav-l)"><span style="background:var(--lav-l);width:24px;height:1px;display:inline-block;margin-right:12px"></span>O que fazer</div>
      <h2 class="section-h2" style="color:#fff">Monte Verde vai<br><em>além da pousada</em></h2>
      <p class="section-body" style="color:rgba(255,255,255,0.5)">Trilhas na Mantiqueira, fondue de queijo, fábricas de chocolate, mirantes de tirar o fôlego e muito mais a poucos passos de você.</p>
      <a class="section-link section-link-light" style="margin-top:8px" onclick="goPage('experiencias')">Explorar experiências</a>
    </div>
    <div class="home-exp-grid">
      <div class="hex-item"><div class="hex-icon">🥾</div><div class="hex-title">Trilhas</div><div class="hex-desc">Mirantes e cachoeiras a 1.800m</div></div>
      <div class="hex-item"><div class="hex-icon">🫕</div><div class="hex-title">Fondue</div><div class="hex-desc">Gastronomia alpino-mineira</div></div>
      <div class="hex-item"><div class="hex-icon">🍫</div><div class="hex-title">Chocolate</div><div class="hex-desc">Fábricas artesanais na vila</div></div>
      <div class="hex-item"><div class="hex-icon">❄️</div><div class="hex-title">Frio extremo</div><div class="hex-desc">Até -5°C nos invernos</div></div>
    </div>
  </div>

  <!-- Quote -->
  <div class="home-quote">
    <div class="quote-mark">"</div>
    <blockquote class="quote-text">Tudo maravilhoso. Foram muito atenciosos no check-in e check-out, deixaram água e chá para nós. Ficamos bem confortáveis: cama confortável, chuveiro quentinho, cobertores, toalhas, travesseiros e itens de banho. Eles pensaram em cada detalhe. Recomendo demais.</blockquote>
    <div class="quote-author">Hóspede real · Feedback via Instagram</div>
    <div style="margin-top:32px;display:flex;justify-content:center;gap:14px;flex-wrap:wrap">
      <button class="hbtn-primary" onclick="goPage('reserva')">Viver essa experiência</button>
      <button class="hbtn-outline" style="color:var(--ink);border-color:var(--sand)" onclick="goPage('destino')">Conhecer Monte Verde</button>
    </div>
  </div>

</div><!-- /home -->
    `;
}
