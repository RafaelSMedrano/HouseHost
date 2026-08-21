export function renderExperienciasView(containerId, options = {}) {
    const container = document.getElementById(containerId);

    if (!container) {
        return;
    }

    const openExperience = typeof options.onOpenExperience === "function" ? options.onOpenExperience : () => {};

    container.innerHTML = `
<!-- ══ PAGE: EXPERIÊNCIAS ══ -->
<div class="page active" id="page-experiencias">

  <div class="exp-hero" style="background:var(--sage)">
    <div class="page-hero-eyebrow" style="color:rgba(255,255,255,0.4)"><span style="width:32px;height:1px;background:rgba(255,255,255,0.3);display:inline-block;margin-right:14px"></span>03 · Experiências</div>
    <h1 class="page-hero-h1" style="color:#fff">O que<br><em style="color:var(--sage-l)">fazer</em> aqui</h1>
    <p class="page-hero-desc" style="color:rgba(255,255,255,0.55)">Monte Verde é um universo condensado em uma pequena vila serrana. Cada canto tem uma história, um sabor, uma aventura esperando por você.</p>
  </div>

  <div class="exp-mosaic">
    <div class="em-item em-e1" data-experience="trilhas">
      <div class="em-bg">🥾</div><div class="em-overlay"></div>
      <div class="em-content">
        <div class="em-title">Trilhas na Mantiqueira</div>
        <div class="em-desc">Pedra Redonda, Platô, Chapéu do Bispo e outros percursos de serra com mirantes, vento frio e mata de altitude. Clique para ver opções.</div>
        <a class="em-link">Ver trilhas →</a>
      </div>
    </div>
    <div class="em-item em-e2" data-experience="gastronomia">
      <div class="em-bg">🫕</div><div class="em-overlay"></div>
      <div class="em-content">
        <div class="em-title">Gastronomia alpina</div>
        <div class="em-desc">Fondue, truta, cafés, cervejarias, cozinha mineira e restaurantes do centrinho para noites frias e almoços demorados.</div>
        <a class="em-link">Ver roteiro →</a>
      </div>
    </div>
    <div class="em-item em-e3" data-experience="frio">
      <div class="em-bg">❄️</div><div class="em-overlay"></div>
      <div class="em-content">
        <div class="em-title">Frio de verdade</div>
        <div class="em-desc">Altitude, neblina, geada em alguns invernos e aquele clima de serra que pede casaco, vinho e manhã sem pressa.</div>
        <a class="em-link">Ver cuidados →</a>
      </div>
    </div>
    <div class="em-item em-e4" data-experience="chocolate">
      <div class="em-bg">🍫</div><div class="em-overlay"></div>
      <div class="em-content">
        <div class="em-title">Fábricas de chocolate</div>
        <div class="em-desc">Chocolaterias, cafés e lojas especializadas fazem parte do passeio clássico pelo centrinho de Monte Verde.</div>
        <a class="em-link">Ver paradas →</a>
      </div>
    </div>
    <div class="em-item em-e5" data-experience="lavandas">
      <div class="em-bg">🌸</div><div class="em-overlay"></div>
      <div class="em-content">
        <div class="em-title">Jardim de lavandas</div>
        <div class="em-desc">Lavandas, jardim da casa e cantinhos externos para aproveitar luz suave, silêncio e fotos sem sair da hospedagem.</div>
        <a class="em-link">Ver detalhes →</a>
      </div>
    </div>
  </div>

  <div class="exp-list">
    <div class="el-item" data-experience="fauna">
      <div class="el-icon">🦋</div>
      <div class="el-title">Fauna da Mata Atlântica</div>
      <div class="el-desc">Tucanos, beija-flores, papagaios e borboletas raras habitam a vegetação nativa ao redor da pousada. Para quem ama natureza, é um paraíso.</div>
    </div>
    <div class="el-item" data-experience="bemestar">
      <div class="el-icon">🧖</div>
      <div class="el-title">Spa & bem-estar</div>
      <div class="el-desc">Parceria com spas locais para massagens, aromaterapia e terapias holísticas. Agendamento diretamente pela pousada.</div>
    </div>
    <div class="el-item" data-experience="centrinho">
      <div class="el-icon">🛍️</div>
      <div class="el-title">Centrinho de Monte Verde</div>
      <div class="el-desc">Lojinhas de artesanato, cachaçarias, queijarias, roupas de frio e cafés encantadores. Tudo a poucos minutos a pé da pousada.</div>
    </div>
    <div class="el-item" data-experience="pordosol">
      <div class="el-icon">🌄</div>
      <div class="el-title">Pôr do sol no vale</div>
      <div class="el-desc">O espetáculo acontece no fim da tarde, quando o céu se pinta de laranja e roxo sobre as montanhas.</div>
    </div>
    <div class="el-item" data-experience="cavalo">
      <div class="el-icon">🏇</div>
      <div class="el-title">Passeios a cavalo</div>
      <div class="el-desc">Haras locais oferecem passeios guiados pela mata e pelos campos, para iniciantes e experientes. Agendamento prévio necessário.</div>
    </div>
    <div class="el-item" data-experience="fotografia">
      <div class="el-icon">📸</div>
      <div class="el-title">Fotografia de natureza</div>
      <div class="el-desc">O nascer do sol sobre a neblina do vale é um dos cenários mais fotografados de Minas Gerais. Traga sua câmera.</div>
    </div>
  </div>

</div><!-- /experiencias -->
    `;

    container.querySelectorAll("[data-experience]").forEach((item) => {
        item.addEventListener("click", () => {
            openExperience(item.dataset.experience);
        });
    });
}
