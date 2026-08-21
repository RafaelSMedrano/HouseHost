export function renderExperienceFrioView(containerId, options = {}) {
    const container = document.getElementById(containerId);

    if (!container) {
        return;
    }

    container.innerHTML = `
<div class="page active experience-page">
<section class="trail-details experience-detail-page open" id="experience-page-frio">
    <div class="trail-details-head">
      <div>
        <div class="section-eyebrow">Clima de serra</div>
        <h2 class="section-h2">Frio, neblina<br><em>e geada</em></h2>
      </div>
      <button class="trail-back" type="button" data-experience-back>← Voltar</button>
    </div>
    <div class="trail-grid">
      <article class="trail-card">
        <div class="trail-kicker">Inverno</div>
        <h3>Leve roupa quente</h3>
        <p>Monte Verde fica em altitude elevada, então manhãs e noites podem ser bem frias. Casaco pesado, meia, touca e calçado fechado fazem diferença.</p>
        <div class="trail-meta"><span>Junho a agosto</span><span>Noites frias</span><span>Casaco</span></div>
      </article>
      <article class="trail-card">
        <div class="trail-kicker">Manhã</div>
        <h3>Geada e neblina</h3>
        <p>Em períodos frios, a paisagem pode amanhecer com geada ou neblina. A luz da manhã rende boas fotos e pede deslocamentos com calma.</p>
        <div class="trail-meta"><span>Amanhecer</span><span>Fotos</span><span>Baixa temperatura</span></div>
      </article>
      <article class="trail-card">
        <div class="trail-kicker">Estradas</div>
        <h3>Planeje deslocamentos</h3>
        <p>Com chuva, frio intenso ou neblina, dirija devagar e confirme as condições de trilhas e passeios. A serra muda rápido.</p>
        <div class="trail-meta"><span>Neblina</span><span>Chuva</span><span>Serra</span></div>
      </article>
      <article class="trail-card trail-card-note">
        <div class="trail-kicker">Na casa</div>
        <h3>Ritmo de descanso</h3>
        <p>O frio é parte da experiência: prepare bebidas quentes, cozinhe com calma e aproveite a casa como base para dias mais lentos.</p>
        <div class="trail-meta"><span>Descanso</span><span>Casa</span><span>Aconchego</span></div>
      </article>
    </div>
    <div class="experience-references">
      <div class="experience-references-title">Referências e onde encontrar</div>
      <a href="https://www.google.com/search?q=previs%C3%A3o+do+tempo+Monte+Verde+MG" target="_blank" rel="noreferrer">Previsão de Monte Verde</a>
      <a href="https://monteverde.org.br/" target="_blank" rel="noreferrer">Portal de Monte Verde</a>
      <a href="https://www.google.com/search?q=Monte+Verde+MG+geada+frio" target="_blank" rel="noreferrer">Webcams/notícias de Monte Verde</a>
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
