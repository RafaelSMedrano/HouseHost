export function renderExperienceFaunaView(containerId, options = {}) {
    const container = document.getElementById(containerId);

    if (!container) {
        return;
    }

    container.innerHTML = `
<div class="page active experience-page">
<section class="trail-details experience-detail-page open" id="experience-page-fauna">
    <div class="trail-details-head">
      <div>
        <div class="section-eyebrow">Natureza</div>
        <h2 class="section-h2">Fauna e Mata<br><em>Atlântica</em></h2>
      </div>
      <button class="trail-back" type="button" data-experience-back>← Voltar</button>
    </div>
    <div class="trail-grid">
      <article class="trail-card"><div class="trail-kicker">Aves</div><h3>Olhe devagar</h3><p>A região da Mantiqueira favorece observação casual de aves, especialmente de manhã. Caminhe em silêncio e observe árvores, fios e áreas de mata.</p><div class="trail-meta"><span>Manhã</span><span>Silêncio</span><span>Binóculo ajuda</span></div></article>
      <article class="trail-card"><div class="trail-kicker">Mata</div><h3>Altitude</h3><p>Monte Verde combina clima frio, relevo e vegetação de serra. Em trilhas, fique nos caminhos demarcados para proteger plantas e evitar acidentes.</p><div class="trail-meta"><span>Trilhas</span><span>Serra</span><span>Preservação</span></div></article>
      <article class="trail-card"><div class="trail-kicker">Na casa</div><h3>Jardim vivo</h3><p>O jardim e áreas externas podem atrair pequenos pássaros e insetos polinizadores. Evite alimentar animais silvestres.</p><div class="trail-meta"><span>Jardim</span><span>Polinizadores</span><span>Sem alimentar</span></div></article>
      <article class="trail-card trail-card-note"><div class="trail-kicker">Cuidado</div><h3>Respeite a fauna</h3><p>Não faça barulho excessivo, não colete plantas e não tente aproximar animais. A melhor experiência é observar sem interferir.</p><div class="trail-meta"><span>Baixo impacto</span><span>Observação</span><span>Natureza</span></div></article>
    </div>
    <div class="experience-references">
      <div class="experience-references-title">Referências e onde encontrar</div>
      <a href="https://monteverde.org.br/" target="_blank" rel="noreferrer">Portal de Monte Verde: trilhas e natureza</a>
      <a href="https://www.wikiaves.com.br/wiki/serra_da_mantiqueira" target="_blank" rel="noreferrer">Aves na Serra da Mantiqueira</a>
      <a href="https://www.google.com/search?q=observa%C3%A7%C3%A3o+de+aves+Monte+Verde+MG" target="_blank" rel="noreferrer">Observação de aves em Monte Verde</a>
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
