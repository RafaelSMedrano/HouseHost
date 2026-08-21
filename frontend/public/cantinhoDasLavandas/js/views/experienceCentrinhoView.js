export function renderExperienceCentrinhoView(containerId, options = {}) {
    const container = document.getElementById(containerId);

    if (!container) {
        return;
    }

    container.innerHTML = `
<div class="page active experience-page">
<section class="trail-details experience-detail-page open" id="experience-page-centrinho">
    <div class="trail-details-head">
      <div>
        <div class="section-eyebrow">Centrinho</div>
        <h2 class="section-h2">Lojas, cafés<br><em>e avenida principal</em></h2>
      </div>
      <button class="trail-back" type="button" data-experience-back>← Voltar</button>
    </div>
    <div class="trail-grid">
      <article class="trail-card"><div class="trail-kicker">Passeio leve</div><h3>Avenida Monte Verde</h3><p>O centrinho reúne restaurantes, lojas, cafés, chocolaterias e produtos de frio. É o passeio mais simples para o primeiro dia.</p><div class="trail-meta"><span>Caminhada</span><span>Lojas</span><span>Restaurantes</span></div></article>
      <article class="trail-card"><div class="trail-kicker">Compras</div><h3>Produtos locais</h3><p>Procure queijos, doces, chocolates, malhas, lembranças e itens de inverno. Vá com tempo para entrar nas galerias.</p><div class="trail-meta"><span>Queijos</span><span>Doces</span><span>Malhas</span></div></article>
      <article class="trail-card"><div class="trail-kicker">Noite</div><h3>Movimento</h3><p>À noite, o centrinho concentra jantar e passeio. Em fins de semana frios, espere mais movimento e filas.</p><div class="trail-meta"><span>Jantar</span><span>Frio</span><span>Fins de semana</span></div></article>
      <article class="trail-card trail-card-note"><div class="trail-kicker">Estratégia</div><h3>Vá em horários alternativos</h3><p>Para fotos e compras tranquilas, manhãs e tardes fora do pico tendem a ser mais agradáveis que sábado à noite.</p><div class="trail-meta"><span>Manhã</span><span>Menos fila</span><span>Fotos</span></div></article>
    </div>
    <div class="experience-references">
      <div class="experience-references-title">Referências e onde encontrar</div>
      <a href="https://monteverde.org.br/" target="_blank" rel="noreferrer">Portal de Monte Verde</a>
      <a href="https://www.google.com/maps/search/Avenida+Monte+Verde+MG" target="_blank" rel="noreferrer">Avenida Monte Verde no Google Maps</a>
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
