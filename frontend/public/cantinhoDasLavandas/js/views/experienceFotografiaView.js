export function renderExperienceFotografiaView(containerId, options = {}) {
    const container = document.getElementById(containerId);

    if (!container) {
        return;
    }

    container.innerHTML = `
<div class="page active experience-page">
<section class="trail-details experience-detail-page open" id="experience-page-fotografia">
    <div class="trail-details-head">
      <div>
        <div class="section-eyebrow">Fotografia</div>
        <h2 class="section-h2">Neblina, jardim<br><em>e montanhas</em></h2>
      </div>
      <button class="trail-back" type="button" data-experience-back>← Voltar</button>
    </div>
    <div class="trail-grid">
      <article class="trail-card"><div class="trail-kicker">Manhã</div><h3>Neblina</h3><p>Saia cedo para fotografar neblina, geada em períodos frios e ruas mais vazias. A luz costuma ser mais suave.</p><div class="trail-meta"><span>Amanhecer</span><span>Neblina</span><span>Ruas vazias</span></div></article>
      <article class="trail-card"><div class="trail-kicker">Casa</div><h3>Detalhes</h3><p>Fotografe texturas da casa, mesa posta, jardim, lavandas e objetos de viagem. Histórias pequenas rendem boas imagens.</p><div class="trail-meta"><span>Detalhes</span><span>Jardim</span><span>Interiores</span></div></article>
      <article class="trail-card"><div class="trail-kicker">Trilhas</div><h3>Mirantes</h3><p>Nos mirantes, proteja celular ou câmera do frio e da umidade. Evite se aproximar de bordas para buscar enquadramentos.</p><div class="trail-meta"><span>Mirantes</span><span>Segurança</span><span>Umidade</span></div></article>
      <article class="trail-card trail-card-note"><div class="trail-kicker">Equipamento</div><h3>Simples funciona</h3><p>Celular com bateria carregada, pano para lente e uma camada extra de roupa já resolvem boa parte do passeio fotográfico.</p><div class="trail-meta"><span>Bateria</span><span>Pano de lente</span><span>Casaco</span></div></article>
    </div>
    <div class="experience-references">
      <div class="experience-references-title">Referências e onde encontrar</div>
      <a href="https://monteverde.org.br/" target="_blank" rel="noreferrer">Portal de Monte Verde</a>
      <a href="https://www.google.com/maps/search/mirante+Monte+Verde+MG" target="_blank" rel="noreferrer">Mirantes em Monte Verde no Google Maps</a>
      <a href="https://ingressos.monteverde.org.br/" target="_blank" rel="noreferrer">Trilhas e ingressos para mirantes</a>
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
