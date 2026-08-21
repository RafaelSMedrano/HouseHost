export function renderContatoView(containerId) {
    const container = document.getElementById(containerId);

    if (!container) {
        return;
    }

    container.innerHTML = `
<!-- ══ PAGE: CONTATO ══ -->
<div class="page active" id="page-contato">
  <div class="contato-grid">

    <div class="cg-left">
      <div>
        <div class="page-hero-eyebrow"><span style="width:32px;height:1px;background:rgba(255,255,255,0.25);display:inline-block;margin-right:14px"></span>07 · Contato</div>
        <h1 class="page-hero-h1" style="font-size:clamp(44px,7vw,78px)">Fale<br><em>conosco</em></h1>
        <p class="page-hero-desc">Nossa equipe responde com muito carinho — via WhatsApp geralmente em menos de 30 minutos.</p>
      </div>

      <div class="contact-infos">
        <div class="ci-item">
          <div class="ci-icon">📍</div>
          <div>
            <div class="ci-label">Endereço</div>
            <div class="ci-value">Rua Mercúrio, 162<br>Monte Verde · MG<br>CEP 37653-000</div>
          </div>
        </div>
        <div class="ci-item">
          <div class="ci-icon">📞</div>
          <div>
            <div class="ci-label">Telefone & WhatsApp</div>
            <div class="ci-value"><a href="https://wa.me/5512992525319" target="_blank" rel="noreferrer"><strong>+55 12 99252-5319</strong></a><br>Atendimento via WhatsApp</div>
          </div>
        </div>
        <div class="ci-item" hidden>
          <div class="ci-icon">📧</div>
          <div>
            <div class="ci-label">E-mail</div>
            <div class="ci-value"><strong>contato@cantinhodaslavandas.com.br</strong><br>Resposta em até 24h úteis</div>
          </div>
        </div>
        <div class="ci-item">
          <div class="ci-icon">📸</div>
          <div>
            <div class="ci-label">Redes sociais</div>
            <div class="ci-value"><a href="https://www.instagram.com/cantinhodaslavandas.mv/" target="_blank" rel="noreferrer">@cantinhodaslavandas.mv</a><br>Instagram</div>
          </div>
        </div>
      </div>

      <div class="cg-footer">
        Refúgio Cantinho das Lavandas<br>
        Camanducaia · MG<br>
        © 2025 Todos os direitos reservados
      </div>
    </div>

    <div class="cg-right">
      <div class="section-eyebrow">Envie uma mensagem</div>
      <h2 class="section-h2" style="font-size:32px">Fale com<br><em>a equipe</em></h2>
      <p class="section-body" style="font-size:13px;max-width:100%;margin-bottom:32px">Para reservas, use nossa <a onclick="goPage('reserva')" style="color:var(--lav);cursor:pointer;text-decoration:underline">página de reservas</a>. Para dúvidas, parcerias ou feedbacks, use o formulário abaixo.</p>

      <form class="contact-form" onsubmit="submitContact(event)">
        <div class="cf-row">
          <div class="cf-group">
            <label class="cf-label">Nome *</label>
            <input type="text" class="cf-input" placeholder="Seu nome" required>
          </div>
          <div class="cf-group" hidden>
            <label class="cf-label">E-mail *</label>
            <input type="email" class="cf-input" placeholder="seu@email.com">
          </div>
        </div>
        <div class="cf-group">
          <label class="cf-label">Assunto</label>
          <select class="cf-input" style="padding:13px 16px">
            <option>Dúvida sobre reserva</option>
            <option>Informações sobre a casa</option>
            <option>Parceria / Imprensa</option>
            <option>Feedback / Avaliação</option>
            <option>Outro assunto</option>
          </select>
        </div>
        <div class="cf-group">
          <label class="cf-label">Mensagem *</label>
          <textarea class="cf-input" placeholder="Como podemos ajudar?" style="min-height:130px" required></textarea>
        </div>
        <button type="submit" class="cf-submit">Enviar mensagem</button>
      </form>
    </div>

  </div>
</div><!-- /contato -->
    `;
}
