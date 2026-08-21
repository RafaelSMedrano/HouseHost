export function renderExperienceChocolateView(containerId, options = {}) {
    const container = document.getElementById(containerId);

    if (!container) {
        return;
    }

    container.innerHTML = `
<div class="page active experience-page">
<section class="trail-details experience-detail-page open" id="experience-page-chocolate">
    <div class="trail-details-head">
      <div>
        <div class="section-eyebrow">Chocolate</div>
        <h2 class="section-h2">Chocolaterias<br><em>e cafés</em></h2>
      </div>
      <button class="trail-back" type="button" data-experience-back>← Voltar</button>
    </div>
    <div class="trail-grid">
      <article class="trail-card">
        <div class="trail-kicker">Centrinho</div>
        <h3>Vitrines e degustação</h3>
        <p>O passeio por lojas de chocolate é um clássico de Monte Verde. Combine com café, chocolate quente e compras de lembranças.</p>
        <div class="trail-meta"><span>Tarde</span><span>Caminhada leve</span><span>Lembranças</span></div>
      </article>
      <article class="trail-card">
        <div class="trail-kicker">Frio</div>
        <h3>Chocolate quente</h3>
        <p>Em noites frias, chocolate quente e sobremesas funcionam como uma parada rápida antes ou depois do jantar.</p>
        <div class="trail-meta"><span>Noite</span><span>Sobremesa</span><span>Cafés</span></div>
      </article>
      <article class="trail-card">
        <div class="trail-kicker">Compras</div>
        <h3>Para levar</h3>
        <p>Barras, bombons e doces locais são boas opções para levar para casa ou montar uma mesa simples na própria hospedagem.</p>
        <div class="trail-meta"><span>Presentes</span><span>Doces</span><span>Produtos locais</span></div>
      </article>
      <article class="trail-card trail-card-note">
        <div class="trail-kicker">Roteiro curto</div>
        <h3>Depois do almoço</h3>
        <p>Faça esse passeio em ritmo leve, junto com as lojas e galerias do centrinho. É ideal para dias sem trilha.</p>
        <div class="trail-meta"><span>1 a 2 horas</span><span>Baixo esforço</span><span>Centrinho</span></div>
      </article>
    </div>
    <div class="experience-references">
      <div class="experience-references-title">Referências e onde encontrar</div>
      <a href="https://monteverde.org.br/gastronomia/" target="_blank" rel="noreferrer">Portal de Monte Verde: gastronomia e chocolates</a>
      <a href="https://www.google.com/maps/search/chocolateria+Monte+Verde+MG" target="_blank" rel="noreferrer">Chocolaterias em Monte Verde no Google Maps</a>
      <a href="https://www.google.com/maps/search/caf%C3%A9+Monte+Verde+MG" target="_blank" rel="noreferrer">Cafés em Monte Verde no Google Maps</a>
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
