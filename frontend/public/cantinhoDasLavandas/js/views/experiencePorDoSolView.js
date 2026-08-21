export function renderExperiencePorDoSolView(containerId, options = {}) {
    const container = document.getElementById(containerId);

    if (!container) {
        return;
    }

    container.innerHTML = `
<div class="page active experience-page">
<section class="trail-details experience-detail-page open" id="experience-page-pordosol">
    <div class="trail-details-head">
      <div>
        <div class="section-eyebrow">Fim de tarde</div>
        <h2 class="section-h2">Pôr do sol<br><em>na serra</em></h2>
      </div>
      <button class="trail-back" type="button" data-experience-back>← Voltar</button>
    </div>
    <div class="trail-grid">
      <article class="trail-card"><div class="trail-kicker">Luz</div><h3>Golden hour</h3><p>O melhor horário para fotos costuma ser a última hora antes do sol baixar. A luz lateral valoriza montanhas, jardim e fachadas.</p><div class="trail-meta"><span>Fim de tarde</span><span>Fotos</span><span>Luz suave</span></div></article>
      <article class="trail-card"><div class="trail-kicker">Mirantes</div><h3>Com segurança</h3><p>Se for a mirantes, programe a volta ainda com luz ou leve lanterna. Trilhas ao entardecer exigem atenção redobrada.</p><div class="trail-meta"><span>Lanterna</span><span>Volta</span><span>Segurança</span></div></article>
      <article class="trail-card"><div class="trail-kicker">Na casa</div><h3>Sem deslocamento</h3><p>Também dá para aproveitar o fim de tarde na área externa da casa, com bebida quente e câmera por perto.</p><div class="trail-meta"><span>Jardim</span><span>Descanso</span><span>Bebida quente</span></div></article>
      <article class="trail-card trail-card-note"><div class="trail-kicker">Clima</div><h3>Confira nuvens</h3><p>Neblina pode esconder o sol, mas rende fotos atmosféricas. Tenha um plano flexível.</p><div class="trail-meta"><span>Neblina</span><span>Nuvens</span><span>Flexível</span></div></article>
    </div>
    <div class="experience-references">
      <div class="experience-references-title">Referências e onde encontrar</div>
      <a href="https://www.novo.monteverde.com.br/atrativos" target="_blank" rel="noreferrer">Atrativos de Monte Verde</a>
      <a href="https://www.google.com/maps/search/mirante+Monte+Verde+MG" target="_blank" rel="noreferrer">Mirantes em Monte Verde no Google Maps</a>
      <a href="https://www.google.com/search?q=p%C3%B4r+do+sol+Monte+Verde+MG" target="_blank" rel="noreferrer">Pôr do sol em Monte Verde</a>
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
