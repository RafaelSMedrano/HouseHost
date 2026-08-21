export function renderExperienceCavaloView(containerId, options = {}) {
    const container = document.getElementById(containerId);

    if (!container) {
        return;
    }

    container.innerHTML = `
<div class="page active experience-page">
<section class="trail-details experience-detail-page open" id="experience-page-cavalo">
    <div class="trail-details-head">
      <div>
        <div class="section-eyebrow">Passeios</div>
        <h2 class="section-h2">Cavalo, haras<br><em>e campos</em></h2>
      </div>
      <button class="trail-back" type="button" data-experience-back>← Voltar</button>
    </div>
    <div class="trail-grid">
      <article class="trail-card"><div class="trail-kicker">Guiado</div><h3>Passeio assistido</h3><p>Haras e operadores locais oferecem passeios com acompanhamento. Confirme duração, idade mínima e nível de experiência exigido.</p><div class="trail-meta"><span>Guia</span><span>Haras</span><span>Reserva</span></div></article>
      <article class="trail-card"><div class="trail-kicker">Família</div><h3>Para iniciantes</h3><p>Se nunca cavalgou, escolha percursos curtos e informe sua experiência antes de sair. O objetivo é passeio, não desafio.</p><div class="trail-meta"><span>Iniciante</span><span>Curto</span><span>Calma</span></div></article>
      <article class="trail-card"><div class="trail-kicker">Roupa</div><h3>Vá adequado</h3><p>Use calça confortável e calçado fechado. Evite chinelos, bolsas soltas e roupas que prendam nos equipamentos.</p><div class="trail-meta"><span>Calça</span><span>Calçado fechado</span><span>Conforto</span></div></article>
      <article class="trail-card trail-card-note"><div class="trail-kicker">Bem-estar animal</div><h3>Escolha bem</h3><p>Prefira operadores que cuidem bem dos animais, respeitem limites de carga e não forcem percursos em clima ruim.</p><div class="trail-meta"><span>Responsável</span><span>Clima</span><span>Animais</span></div></article>
    </div>
    <div class="experience-references">
      <div class="experience-references-title">Referências e onde encontrar</div>
      <a href="https://gamelinhastour.com.br/passeio/cavalgada-floresta-monte-verde" target="_blank" rel="noreferrer">Cavalgada na floresta - Gamelinha's Tour</a>
      <a href="https://www.google.com/maps/search/passeio+a+cavalo+Monte+Verde+MG" target="_blank" rel="noreferrer">Passeios a cavalo em Monte Verde no Google Maps</a>
      <a href="https://www.novo.monteverde.com.br/atrativos" target="_blank" rel="noreferrer">Atrativos de Monte Verde</a>
    </div>
  </section>
</div>
    `;

    container.querySelector("[data-experience-back]")?.addEventListener("click", () => {
        if (typeof options.onBack === "function") {
            options.onBack();
        }
    });
}
