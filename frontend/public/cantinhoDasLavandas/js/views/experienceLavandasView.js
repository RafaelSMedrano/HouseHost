export function renderExperienceLavandasView(containerId, options = {}) {
    const container = document.getElementById(containerId);

    if (!container) {
        return;
    }

    container.innerHTML = `
<div class="page active experience-page">
<section class="trail-details experience-detail-page open" id="experience-page-lavandas">
    <div class="trail-details-head">
      <div>
        <div class="section-eyebrow">Na hospedagem</div>
        <h2 class="section-h2">Jardim, lavandas<br><em>e área externa</em></h2>
      </div>
      <button class="trail-back" type="button" data-experience-back>← Voltar</button>
    </div>
    <div class="trail-grid">
      <article class="trail-card">
        <div class="trail-kicker">Casa</div>
        <h3>Tempo do lado de fora</h3>
        <p>A área externa funciona como extensão da estadia: um lugar para café, conversa, leitura e fotos sem precisar sair.</p>
        <div class="trail-meta"><span>Café</span><span>Descanso</span><span>Silêncio</span></div>
      </article>
      <article class="trail-card">
        <div class="trail-kicker">Fotografia</div>
        <h3>Luz suave</h3>
        <p>Manhã e fim de tarde costumam render a melhor luz para fotos no jardim e nas lavandas, com cores mais suaves.</p>
        <div class="trail-meta"><span>Manhã</span><span>Fim de tarde</span><span>Fotos</span></div>
      </article>
      <article class="trail-card">
        <div class="trail-kicker">Experiência</div>
        <h3>Sem deslocamento</h3>
        <p>Em dias de frio, chuva ou descanso, o jardim ajuda a manter a experiência da serra sem depender de agenda externa.</p>
        <div class="trail-meta"><span>Baixo esforço</span><span>Casa</span><span>Contemplação</span></div>
      </article>
      <article class="trail-card trail-card-note">
        <div class="trail-kicker">Cuidado</div>
        <h3>Respeite o jardim</h3>
        <p>Evite pisar nos canteiros, arrancar flores ou movimentar vasos. Assim o espaço permanece bonito para os próximos hóspedes.</p>
        <div class="trail-meta"><span>Preservação</span><span>Lavandas</span><span>Uso comum</span></div>
      </article>
    </div>
    <div class="experience-references">
      <div class="experience-references-title">Referências e onde encontrar</div>
      <a href="https://www.google.com/search?q=Cantinho+das+Lavandas+Monte+Verde" target="_blank" rel="noreferrer">Fotos e áreas da hospedagem</a>
      <a href="https://monteverde.org.br/" target="_blank" rel="noreferrer">Monte Verde no Portal oficial</a>
      <a href="https://www.google.com/search?q=lavandas+Monte+Verde+MG" target="_blank" rel="noreferrer">Jardins e lavandas em Monte Verde</a>
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
