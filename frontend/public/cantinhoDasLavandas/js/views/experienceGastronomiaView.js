export function renderExperienceGastronomiaView(containerId, options = {}) {
    const container = document.getElementById(containerId);

    if (!container) {
        return;
    }

    container.innerHTML = `
<div class="page active experience-page">
<section class="trail-details experience-detail-page open" id="experience-page-gastronomia">
    <div class="trail-details-head">
      <div>
        <div class="section-eyebrow">Gastronomia</div>
        <h2 class="section-h2">Fondue, truta<br><em>e cozinha mineira</em></h2>
      </div>
      <button class="trail-back" type="button" data-experience-back>← Voltar</button>
    </div>
    <div class="trail-grid">
      <article class="trail-card">
        <div class="trail-kicker">Noite clássica</div>
        <h3>Fondue</h3>
        <p>Monte Verde tem tradição de fondue no frio: queijo, carne e chocolate aparecem em muitos restaurantes do centrinho. É uma boa escolha para noites de baixa temperatura.</p>
        <div class="trail-meta"><span>Jantar</span><span>Frio</span><span>Casal ou grupo</span></div>
      </article>
      <article class="trail-card">
        <div class="trail-kicker">Serra</div>
        <h3>Truta</h3>
        <p>A truta é um prato recorrente na Mantiqueira e costuma aparecer grelhada, com molhos, amêndoas ou acompanhamentos mineiros.</p>
        <div class="trail-meta"><span>Almoço</span><span>Regional</span><span>Restaurantes</span></div>
      </article>
      <article class="trail-card">
        <div class="trail-kicker">Cafés</div>
        <h3>Doces e cafés</h3>
        <p>Reserve uma tarde para cafés, doces, chocolate quente e vitrines de produtos locais. O passeio combina bem com caminhada leve pelo centrinho.</p>
        <div class="trail-meta"><span>Tarde</span><span>Centrinho</span><span>Sem pressa</span></div>
      </article>
      <article class="trail-card trail-card-note">
        <div class="trail-kicker">Dica prática</div>
        <h3>Reserve no inverno</h3>
        <p>Em fins de semana frios e feriados, restaurantes lotam. Vale reservar jantar e checar horários, especialmente para fondue.</p>
        <div class="trail-meta"><span>Feriado</span><span>Alta demanda</span><span>Reserva</span></div>
      </article>
    </div>
    <div class="experience-references">
      <div class="experience-references-title">Referências e onde encontrar</div>
      <a href="https://monteverde.org.br/gastronomia/" target="_blank" rel="noreferrer">Portal de Monte Verde: gastronomia</a>
      <a href="https://www.google.com/maps/search/restaurantes+Monte+Verde+MG" target="_blank" rel="noreferrer">Restaurantes em Monte Verde no Google Maps</a>
      <a href="https://www.google.com/maps/search/fondue+Monte+Verde+MG" target="_blank" rel="noreferrer">Fondue em Monte Verde no Google Maps</a>
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
