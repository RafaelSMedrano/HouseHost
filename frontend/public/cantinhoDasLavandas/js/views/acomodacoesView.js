export function renderAcomodacoesView(containerId) {
    const container = document.getElementById(containerId);

    if (!container) {
        return;
    }

    container.innerHTML = `
<!-- ══ PAGE: ACOMODAÇÕES ══ -->
<div class="page active" id="page-acomodacoes">

  <div class="acomod-hero">
    <div class="acomod-hero-text">
      <div class="page-hero-eyebrow">02 · Acomodações</div>
      <h1 class="page-hero-h1">A casa<br><em>privativa</em></h1>
      <p class="page-hero-desc">Uma casa única para sua estadia em Monte Verde: quarto de casal, quarto para duas pessoas em camas de solteiro, sala, cozinha e banheiro.</p>
    </div>
  </div>

  <div class="rooms-list">

    <div class="room-detail">
      <div class="rd-visual rd-photo"><img src="assets/images/Entrada.jpeg" alt="Entrada da casa do Refúgio Cantinho das Lavandas"></div>
      <div class="rd-info">
        <div class="rd-room-num">01</div>
        <div class="rd-name">Casa privativa</div>
        <p class="rd-desc">A hospedagem acontece em uma única casa, reservada integralmente para o seu grupo. É uma instalação completa e privativa, pensada para quem quer chegar, fechar a porta e viver Monte Verde com conforto, calma e autonomia.</p>
        <div class="rd-amenities">
          <span class="rd-tag">🏡 Casa inteira</span>
          <span class="rd-tag">🛏️ 2 quartos</span>
          <span class="rd-tag">🛋️ Sala</span>
          <span class="rd-tag">🍳 Cozinha</span>
          <span class="rd-tag">🚿 Banheiro</span>
          <span class="rd-tag">🌿 Área externa</span>
        </div>
        <button class="rd-book" onclick="goPage('reserva')">Reservar a casa →</button>
      </div>
    </div>

    <div class="room-detail">
      <div class="rd-visual rd-photo"><img src="assets/images/QuartoCasal.jpeg" alt="Quarto de casal da casa"></div>
      <div class="rd-info">
        <div class="rd-room-num">02</div>
        <div class="rd-name">Quarto de casal</div>
        <p class="rd-desc">O quarto principal acomoda um casal com uma atmosfera simples, íntima e acolhedora. É o espaço de descanso da casa para quem busca uma estadia reservada na serra.</p>
        <div class="rd-amenities">
          <span class="rd-tag">👥 2 pessoas</span>
          <span class="rd-tag">🛏️ Cama de casal</span>
          <span class="rd-tag">🌙 Ambiente reservado</span>
        </div>
      </div>
    </div>

    <div class="room-detail">
      <div class="rd-visual rd-photo"><img src="assets/images/QuartoSolteiro.jpeg" alt="Quarto com duas camas de solteiro"></div>
      <div class="rd-info">
        <div class="rd-room-num">03</div>
        <div class="rd-name">Quarto para duas pessoas</div>
        <p class="rd-desc">O segundo quarto acomoda duas pessoas em camas de solteiro, ideal para filhos, amigos ou familiares que viajam junto e querem manter o conforto de uma casa completa.</p>
        <div class="rd-amenities">
          <span class="rd-tag">👥 2 pessoas</span>
          <span class="rd-tag">🛏️ 2 camas de solteiro</span>
          <span class="rd-tag">👨‍👩‍👧 Viagem em grupo</span>
        </div>
      </div>
    </div>

    <div class="room-detail">
      <div class="rd-visual rd-photo"><img src="assets/images/CantinhoSala.jpeg" alt="Sala da casa"></div>
      <div class="rd-info">
        <div class="rd-room-num">04</div>
        <div class="rd-name">Sala, cozinha e banheiro</div>
        <p class="rd-desc">A casa conta com sala para convivência, cozinha equipada para preparar refeições com tranquilidade e banheiro. A proposta é uma estadia independente, com o ritmo de uma casa de montanha.</p>
        <div class="rd-amenities">
          <span class="rd-tag">🛋️ Sala</span>
          <span class="rd-tag">🍳 Cozinha equipada</span>
          <span class="rd-tag">🚿 Banheiro</span>
          <span class="rd-tag">☕ Mesa para refeições</span>
        </div>
      </div>
    </div>

  </div>

  <!-- Políticas -->
  <div style="padding:80px;background:var(--lav-d);display:grid;grid-template-columns:repeat(3,1fr);gap:2px">
    <div style="background:rgba(255,255,255,0.05);padding:36px 30px;border:1px solid rgba(255,255,255,0.06)">
      <div style="font-size:28px;margin-bottom:12px">☕</div>
      <div style="font-family:var(--serif);font-size:17px;font-weight:700;color:#fff;margin-bottom:8px">Casa completa</div>
      <div style="font-size:12px;color:rgba(255,255,255,0.45);line-height:1.7">Hospedagem em casa privativa com sala, cozinha, dois quartos e banheiro para uso exclusivo do grupo.</div>
    </div>
    <div style="background:rgba(255,255,255,0.05);padding:36px 30px;border:1px solid rgba(255,255,255,0.06)">
      <div style="font-size:28px;margin-bottom:12px">🕐</div>
      <div style="font-family:var(--serif);font-size:17px;font-weight:700;color:#fff;margin-bottom:8px">Check-in & check-out</div>
      <div style="font-size:12px;color:rgba(255,255,255,0.45);line-height:1.7">Entrada a partir das 14h. Saída até 12h. Late check-out disponível sob consulta e disponibilidade.</div>
    </div>
    <div style="background:rgba(255,255,255,0.05);padding:36px 30px;border:1px solid rgba(255,255,255,0.06)">
      <div style="font-size:28px;margin-bottom:12px">🐾</div>
      <div style="font-family:var(--serif);font-size:17px;font-weight:700;color:#fff;margin-bottom:8px">Hospedagem privativa</div>
      <div style="font-size:12px;color:rgba(255,255,255,0.45);line-height:1.7">Uma reserva por vez, sem divisão de áreas internas com outros hóspedes.</div>
    </div>
  </div>

  <div style="text-align:center;padding:80px;background:var(--warm)">
    <div style="font-family:var(--serif);font-size:clamp(28px,4vw,42px);font-weight:900;color:var(--ink);margin-bottom:16px">Pronto para reservar?</div>
    <p style="font-family:var(--italic);font-style:italic;font-size:15px;color:var(--ink3);margin-bottom:32px">Reserve a casa inteira para sua estadia em Monte Verde.</p>
    <button class="rd-book" style="display:inline-flex;margin:0 auto" onclick="goPage('reserva')">Verificar disponibilidade →</button>
  </div>

</div><!-- /acomodacoes -->
    `;
}
