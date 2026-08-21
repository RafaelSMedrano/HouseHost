export function renderGaleriaView(containerId) {
    const container = document.getElementById(containerId);

    if (!container) {
        return;
    }

    const pousadaImages = [
        { src: "assets/images/Entrada.jpeg", alt: "Entrada da casa do Refúgio Cantinho das Lavandas", title: "Entrada" },
        { src: "assets/images/Jardim.jpeg", alt: "Jardim do Refúgio Cantinho das Lavandas", title: "Jardim" },
        { src: "assets/images/Lavandas.jpeg", alt: "Lavandas no jardim da pousada", title: "Lavandas" },
        { src: "assets/images/Lavanda2.jpeg", alt: "Detalhe das lavandas da pousada", title: "Lavandas em detalhe" },
        { src: "assets/images/MesaExterior.jpeg", alt: "Mesa externa da casa", title: "Área externa" },
        { src: "assets/images/CantinhoSala.jpeg", alt: "Sala aconchegante da casa", title: "Sala" },
        { src: "assets/images/Cozinha.jpeg", alt: "Cozinha da casa", title: "Cozinha" },
        { src: "assets/images/CozinhaMesa.jpeg", alt: "Mesa da cozinha preparada para hospedagem", title: "Mesa da cozinha" },
        { src: "assets/images/CozinhaEntardecer.jpeg", alt: "Cozinha iluminada no entardecer", title: "Cozinha ao entardecer" },
        { src: "assets/images/QuartoCasal.jpeg", alt: "Quarto de casal da casa", title: "Quarto de casal" },
        { src: "assets/images/QuartoSolteiro.jpeg", alt: "Quarto com duas camas de solteiro", title: "Quarto com duas camas" },
        { src: "assets/images/Banheiro.jpeg", alt: "Banheiro da casa", title: "Banheiro" },
        { src: "assets/images/Banheiro2.jpeg", alt: "Segundo ângulo do banheiro da casa", title: "Banheiro em detalhe" },
        { src: "assets/images/lavandas_fundo.jpg", alt: "Lavandas usadas como imagem de fundo do site", title: "Lavandas ao fundo" },
    ];

    const galleryItems = pousadaImages.map((image) => `
    <figure class="gm-item">
      <img class="gm-photo" src="${image.src}" alt="${image.alt}">
      <figcaption class="gm-overlay">
        <span>${image.title}</span>
      </figcaption>
    </figure>
    `).join("");

    container.innerHTML = `
<!-- ══ PAGE: GALERIA ══ -->
<div class="page active" id="page-galeria">

  <div class="gallery-header">
    <div class="page-hero-eyebrow" style="color:rgba(255,255,255,0.35)"><span style="width:32px;height:1px;background:rgba(255,255,255,0.2);display:inline-block;margin-right:14px"></span>05 · Galeria</div>
    <h1 class="page-hero-h1" style="color:#fff;font-size:clamp(44px,7vw,80px)">Imagens<br><em>que contam</em></h1>
    <p class="page-hero-desc">Uma antecipação visual do que te espera. Cada foto é um convite.</p>
  </div>

  <div class="gallery-masonry">
${galleryItems}
  </div>

  <div style="text-align:center;padding:80px 48px;background:var(--warm)">
    <p style="font-family:var(--italic);font-style:italic;font-size:16px;color:var(--ink3);margin-bottom:24px">Siga nossa jornada no Instagram e veja mais momentos do dia a dia da pousada.</p>
    <div style="display:flex;gap:14px;justify-content:center;flex-wrap:wrap">
      <button class="rd-book" style="display:inline-flex" onclick="window.open('https://www.instagram.com/cantinhodaslavandas.mv/', '_blank', 'noopener,noreferrer')">Seguir no Instagram</button>
      <button class="rd-book" style="background:var(--lav-d);display:inline-flex" onclick="goPage('reserva')">Reservar agora</button>
    </div>
  </div>

</div><!-- /galeria -->
    `;
}
