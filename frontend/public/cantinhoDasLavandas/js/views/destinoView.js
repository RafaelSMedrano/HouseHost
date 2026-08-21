export function renderDestinoView(containerId) {
    const container = document.getElementById(containerId);

    if (!container) {
        return;
    }

    container.innerHTML = `
<!-- ══ PAGE: MONTE VERDE (DESTINO) ══ -->
<div class="page active" id="page-destino">

  <div class="destino-intro">
    <div class="di-left">
      <div class="page-hero-eyebrow"><span style="width:32px;height:1px;background:rgba(255,255,255,0.25);display:inline-block;margin-right:14px"></span>04 · Monte Verde</div>
      <h1 class="page-hero-h1" style="font-size:clamp(44px,7vw,80px)">O destino<br><em>mais frio</em><br>do Brasil</h1>
      <p class="page-hero-desc">A 1.600 metros de altitude na Serra da Mantiqueira, Monte Verde é um vilarejo encantado — parte alpino, parte mineiro, completamente único.</p>
      <div style="margin-top:40px;padding-top:40px;border-top:1px solid rgba(255,255,255,0.1)">
        <p style="font-size:13px;color:rgba(255,255,255,0.45);line-height:1.9;font-weight:300">Monte Verde é um distrito de Camanducaia, no sul de Minas Gerais, na divisa com São Paulo. A arquitetura da vila mistura influências europeias com o jeitinho mineiro — ruas de paralelepípedo, lojinhas aconchegantes, restaurantes com chaminé e o som de sinos ao entardecer.</p>
      </div>
    </div>
    <div class="di-right">
      <div class="section-eyebrow">Por que visitar</div>
      <h2 class="section-h2">Quatro razões<br>para <em>se apaixonar</em></h2>
      <div class="destino-cards">
        <div class="dc-item">
          <div class="dc-icon">🌡️</div>
          <div class="dc-title">Frio de verdade</div>
          <div class="dc-desc">Temperatura pode chegar a -5°C no inverno. Uma raridade no Brasil. Casaco, cobertor e lareira são obrigatórios.</div>
        </div>
        <div class="dc-item">
          <div class="dc-icon">🌿</div>
          <div class="dc-title">Natureza preservada</div>
          <div class="dc-desc">Mata Atlântica intocada, trilhas ecológicas, fauna exótica e paisagens que parecem saídas de um filme europeu.</div>
        </div>
        <div class="dc-item">
          <div class="dc-icon">🏘️</div>
          <div class="dc-title">Vila charmosa</div>
          <div class="dc-desc">Centrinho com arquitetura alpina, chocolaterias, cafés, restaurantes e lojinhas. Tudo pequeno, tudo com amor.</div>
        </div>
        <div class="dc-item">
          <div class="dc-icon">🍴</div>
          <div class="dc-title">Gastronomia única</div>
          <div class="dc-desc">Fondue de queijo e chocolate, truta fresca, culinária mineira premium com ingredientes da serra.</div>
        </div>
      </div>
    </div>
  </div>

  <!-- Clima por estação -->
  <div class="climate-strip">
    <div class="cs-item">
      <div class="cs-season">Inverno (Jun–Ago)</div>
      <div class="cs-temp">−5°</div>
      <div class="cs-desc">Geadas, neblina densa, épocas de neve rara. A temporada mais mágica.</div>
    </div>
    <div class="cs-item">
      <div class="cs-season">Outono (Mar–Mai)</div>
      <div class="cs-temp">12°</div>
      <div class="cs-desc">Folhagens douradas, menos turistas. Ideal para trilhas tranquilas.</div>
    </div>
    <div class="cs-item">
      <div class="cs-season">Primavera (Set–Nov)</div>
      <div class="cs-temp">18°</div>
      <div class="cs-desc">Florada das lavandas, clima ameno. A melhor época para o jardim.</div>
    </div>
    <div class="cs-item">
      <div class="cs-season">Verão (Dez–Fev)</div>
      <div class="cs-temp">22°</div>
      <div class="cs-desc">Mais chuvas, verde intenso. Baixa temporada com preços acessíveis.</div>
    </div>
  </div>

  <!-- Distâncias -->
  <div class="distances-editorial">
    <div class="de-title">KM</div>
    <div>
      <div style="margin-bottom:32px">
        <div class="section-eyebrow">Como chegar</div>
        <h2 class="section-h2">De onde<br><em>você vem?</em></h2>
        <p class="section-body">Acesso pela Rodovia dos Tropeiros (SP-50) ou pela MG-010, com trecho por estrada de terra de boa qualidade. GPS funciona bem até a chegada.</p>
      </div>
      <div class="de-list">
        <div class="de-item"><div><div class="de-from">São Paulo (SP)</div><div class="de-sub">Via Rodovia dos Tropeiros</div></div><div><span class="de-km">160</span><span class="de-unit"> km</span></div></div>
        <div class="de-item"><div><div class="de-from">Campinas (SP)</div><div class="de-sub">Via BR-267</div></div><div><span class="de-km">120</span><span class="de-unit"> km</span></div></div>
        <div class="de-item"><div><div class="de-from">Belo Horizonte (MG)</div><div class="de-sub">Via BR-381</div></div><div><span class="de-km">480</span><span class="de-unit"> km</span></div></div>
        <div class="de-item"><div><div class="de-from">Rio de Janeiro (RJ)</div><div class="de-sub">Via Dutra / Vale do Paraíba</div></div><div><span class="de-km">340</span><span class="de-unit"> km</span></div></div>
        <div class="de-item"><div><div class="de-from">Campos do Jordão (SP)</div><div class="de-sub">Serra da Mantiqueira</div></div><div><span class="de-km">35</span><span class="de-unit"> km</span></div></div>
      </div>
      <div style="margin-top:24px;padding:20px;background:var(--lav-p);border:1px solid var(--lav-l)">
        <div style="font-size:13px;color:var(--lav-d);line-height:1.7">📍 <strong>Endereço:</strong> Rua Mercúrio, 162 — Monte Verde — MG, CEP 37653-000</div>
      </div>
    </div>
  </div>

</div><!-- /destino -->
    `;
}
