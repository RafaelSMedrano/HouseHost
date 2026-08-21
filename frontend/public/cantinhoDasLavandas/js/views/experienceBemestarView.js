export function renderExperienceBemestarView(containerId, options = {}) {
    const container = document.getElementById(containerId);

    if (!container) {
        return;
    }

    container.innerHTML = `
<div class="page active experience-page">
<section class="trail-details experience-detail-page open" id="experience-page-bemestar">
    <div class="trail-details-head">
      <div>
        <div class="section-eyebrow">Bem-estar</div>
        <h2 class="section-h2">Spa, massagem<br><em>e descanso</em></h2>
      </div>
      <button class="trail-back" type="button" data-experience-back>← Voltar</button>
    </div>
    <div class="trail-grid">
      <article class="trail-card"><div class="trail-kicker">Relaxamento</div><h3>Massagens</h3><p>Monte Verde tem spas e serviços de massagem voltados ao turismo de descanso. Verifique horários, localização e necessidade de reserva antecipada.</p><div class="trail-meta"><span>Reserva</span><span>Casal</span><span>Relax</span></div></article>
      <article class="trail-card"><div class="trail-kicker">Ritual simples</div><h3>Dia leve</h3><p>Combine café sem pressa, caminhada curta, banho quente e descanso na casa. Nem todo dia precisa de agenda cheia.</p><div class="trail-meta"><span>Casa</span><span>Sem pressa</span><span>Frio</span></div></article>
      <article class="trail-card"><div class="trail-kicker">Corpo</div><h3>Pós-trilha</h3><p>Depois de caminhada na serra, massagem ou alongamento leve ajudam a recuperar o corpo antes do jantar.</p><div class="trail-meta"><span>Trilha</span><span>Recuperação</span><span>Noite</span></div></article>
      <article class="trail-card trail-card-note"><div class="trail-kicker">Dica</div><h3>Agende antes</h3><p>Em feriados e alta temporada, serviços de spa podem ter pouca disponibilidade. Resolva isso antes de chegar.</p><div class="trail-meta"><span>Alta temporada</span><span>Horário</span><span>Planejamento</span></div></article>
    </div>
    <div class="experience-references">
      <div class="experience-references-title">Referências e onde encontrar</div>
      <a href="https://www.mirantedacolyna.com.br/spa-em-monte-verde" target="_blank" rel="noreferrer">Samadhi SPA - Mirante da Colyna</a>
      <a href="https://www.meissnerhof.com.br/spa-monte-verde" target="_blank" rel="noreferrer">Spa Zen - Hotel Meissner Hof</a>
      <a href="https://www.google.com/maps/search/spa+massagem+Monte+Verde+MG" target="_blank" rel="noreferrer">Spas em Monte Verde no Google Maps</a>
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
