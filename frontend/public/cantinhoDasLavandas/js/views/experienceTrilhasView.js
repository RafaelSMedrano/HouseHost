export function renderExperienceTrilhasView(containerId, options = {}) {
    const container = document.getElementById(containerId);

    if (!container) {
        return;
    }

    container.innerHTML = `
<div class="page active experience-page">
<section class="trail-details experience-detail-page open" id="experience-page-trilhas">
    <div class="trail-details-head">
      <div>
        <div class="section-eyebrow">Trilhas de Monte Verde</div>
        <h2 class="section-h2">Caminhadas, mirantes<br><em>e serra</em></h2>
      </div>
      <button class="trail-back" type="button" data-experience-back>← Voltar</button>
    </div>

    <div class="trail-grid">
      <article class="trail-card">
        <div class="trail-kicker">Mais conhecida</div>
        <h3>Pedra Redonda</h3>
        <p>Trilha fácil, muito procurada por quem quer uma vista ampla sem uma caminhada longa. O acesso fica no final da Avenida das Montanhas.</p>
        <div class="trail-meta">
          <span>926 m</span>
          <span>1h30 ida e volta</span>
          <span>Fácil</span>
        </div>
        <a href="https://monteverde.org.br/trilha-pedra-redonda/" target="_blank" rel="noreferrer">Portal de Monte Verde</a>
      </article>

      <article class="trail-card">
        <div class="trail-kicker">Clássica da serra</div>
        <h3>Platô</h3>
        <p>Rota de nível moderado, com subida desde o início e mirantes no caminho. Em dias claros, o alto oferece vista para o Vale do Paraíba.</p>
        <div class="trail-meta">
          <span>2,5 km ida e volta</span>
          <span>2h média</span>
          <span>Moderada</span>
        </div>
        <a href="https://ingressos.monteverde.org.br/entrada-trilha-do-plato-5549" target="_blank" rel="noreferrer">Ingressos e regras</a>
      </article>

      <article class="trail-card">
        <div class="trail-kicker">Com guia</div>
        <h3>Chapéu do Bispo</h3>
        <p>Trilha curta pelo acesso da Avenida das Montanhas, mas com piso irregular e trechos de atenção. O Portal informa necessidade de guia credenciado.</p>
        <div class="trail-meta">
          <span>710 m</span>
          <span>40 min estimados</span>
          <span>Fácil</span>
        </div>
        <a href="https://monteverde.org.br/trilha-chapeu-do-bispo/" target="_blank" rel="noreferrer">Portal de Monte Verde</a>
      </article>

      <article class="trail-card trail-card-note">
        <div class="trail-kicker">Planejamento</div>
        <h3>Antes de ir</h3>
        <p>As trilhas podem exigir ingresso, agendamento, guia ou guarda-parque. Use calçado adequado, leve água e saco para lixo, respeite as bordas e confira as regras oficiais antes de sair.</p>
        <div class="trail-meta">
          <span>Agendamento</span>
          <span>Clima muda rápido</span>
          <span>Sem improviso</span>
        </div>
        <a href="https://ingressos.monteverde.org.br/" target="_blank" rel="noreferrer">Consultar ingressos</a>
      </article>
    </div>
    <div class="experience-references">
      <div class="experience-references-title">Referências e onde encontrar</div>
      <a href="https://monteverde.org.br/trilha-pedra-redonda/" target="_blank" rel="noreferrer">Portal de Monte Verde: Pedra Redonda</a>
      <a href="https://monteverde.org.br/trilha-chapeu-do-bispo/" target="_blank" rel="noreferrer">Portal de Monte Verde: Chapéu do Bispo</a>
      <a href="https://ingressos.monteverde.org.br/" target="_blank" rel="noreferrer">Ingressos e regras das trilhas</a>
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
