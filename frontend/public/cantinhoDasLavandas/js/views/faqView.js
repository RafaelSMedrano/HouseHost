export function renderFaqView(containerId) {
    const container = document.getElementById(containerId);

    if (!container) {
        return;
    }

    container.innerHTML = `
<!-- ══ PAGE: FAQ ══ -->
<div class="page active" id="page-faq">
  <div class="faq-layout">

    <div class="faq-sidebar-inner">
      <div class="page-hero-eyebrow" style="color:rgba(255,255,255,0.3)"><span style="width:24px;height:1px;background:rgba(255,255,255,0.2);display:inline-block;margin-right:12px"></span>FAQ</div>
      <div style="font-family:var(--serif);font-size:28px;font-weight:900;color:#fff;margin-top:12px;line-height:1.2">Dúvidas<br>frequentes</div>
      <div class="faq-nav">
        <div class="faq-cat selected"><span class="fc-icon">🏡</span><span class="fc-text">Reservas</span></div>
        <div class="faq-cat"><span class="fc-icon">🔥</span><span class="fc-text">Acomodações</span></div>
        <div class="faq-cat"><span class="fc-icon">🐾</span><span class="fc-text">Pets</span></div>
        <div class="faq-cat"><span class="fc-icon">🗺️</span><span class="fc-text">Localização</span></div>
        <div class="faq-cat"><span class="fc-icon">💳</span><span class="fc-text">Pagamento</span></div>
      </div>
      <div style="margin-top:40px;padding:20px;background:rgba(255,255,255,0.05);border:1px solid rgba(255,255,255,0.08)">
        <div style="font-size:12px;color:rgba(255,255,255,0.4);line-height:1.7">Não encontrou sua resposta?</div>
        <button class="sidebar-cta" style="margin-top:12px" onclick="goPage('contato')">Fale conosco</button>
      </div>
    </div>

    <div class="faq-main">
      <div class="section-eyebrow">Tudo sobre a pousada</div>
      <h2 class="section-h2">Suas <em>perguntas,</em><br>nossas respostas</h2>

      <div class="faq-items" style="margin-top:48px">

        <div class="faq-item">
          <div class="faq-q" onclick="toggleFaq(this)">
            <span class="faq-q-text">Qual é a política de cancelamento?</span>
            <span class="faq-chevron">⌄</span>
          </div>
          <div class="faq-a"><div class="faq-a-inner">Cancelamentos com mais de 48 horas de antecedência recebem reembolso integral. Entre 24 e 48 horas: 50% de reembolso. Menos de 24 horas: sem reembolso, exceto emergências médicas comprovadas. Em alta temporada (julho, feriados prolongados), pode haver condições específicas informadas no ato da reserva.</div></div>
        </div>

        <div class="faq-item">
          <div class="faq-q" onclick="toggleFaq(this)">
            <span class="faq-q-text">A hospedagem é da casa inteira?</span>
            <span class="faq-chevron">⌄</span>
          </div>
          <div class="faq-a"><div class="faq-a-inner">Sim! O café da manhã colonial mineiro está incluso em todas as acomodações sem exceção. É servido das 7h30 às 10h no salão principal. O cardápio varia diariamente e inclui: pão de queijo fresquinho, bolos artesanais, tapiocas, frios, queijos da serra, frutas da estação, geleias, chocolate quente e sucos naturais.</div></div>
        </div>

        <div class="faq-item">
          <div class="faq-q" onclick="toggleFaq(this)">
            <span class="faq-q-text">Aceita pets? Há alguma restrição?</span>
            <span class="faq-chevron">⌄</span>
          </div>
          <div class="faq-a"><div class="faq-a-inner">A hospedagem acontece em uma casa privativa. Para pets, confirme conosco antes da reserva para alinharmos porte, quantidade e cuidados durante a estadia. A responsabilidade por danos ao mobiliário ou áreas da casa é do hóspede.</div></div>
        </div>

        <div class="faq-item">
          <div class="faq-q" onclick="toggleFaq(this)">
            <span class="faq-q-text">Qual é a estadia mínima?</span>
            <span class="faq-chevron">⌄</span>
          </div>
          <div class="faq-a"><div class="faq-a-inner">Durante a semana (segunda a quinta), aceitamos reservas a partir de 1 diária. Nos finais de semana (sexta a domingo), a mínima é de 2 diárias. Em alta temporada (julho, Carnaval, Natal, Ano Novo e feriados prolongados), a mínima pode ser de 3 diárias. Consulte disponibilidade.</div></div>
        </div>

        <div class="faq-item">
          <div class="faq-q" onclick="toggleFaq(this)">
            <span class="faq-q-text">Há Wi-Fi na casa?</span>
            <span class="faq-chevron">⌄</span>
          </div>
          <div class="faq-a"><div class="faq-a-inner">Sim, a casa tem Wi-Fi incluso e gratuito. Por ser uma região serrana, pode haver instabilidade ocasional, então vale considerar isso se a viagem incluir trabalho remoto ou chamadas importantes.</div></div>
        </div>

        <div class="faq-item">
          <div class="faq-q" onclick="toggleFaq(this)">
            <span class="faq-q-text">As lareiras são a lenha ou a gás?</span>
            <span class="faq-chevron">⌄</span>
          </div>
          <div class="faq-a"><div class="faq-a-inner">A casa é preparada para uma estadia confortável em Monte Verde. Caso precise de alguma informação específica sobre aquecimento, estrutura ou itens disponíveis, fale conosco antes da reserva.</div></div>
        </div>

        <div class="faq-item">
          <div class="faq-q" onclick="toggleFaq(this)">
            <span class="faq-q-text">Como chegar de ônibus ou sem carro?</span>
            <span class="faq-chevron">⌄</span>
          </div>
          <div class="faq-a"><div class="faq-a-inner">É possível chegar à cidade de Camanducaia (30 km de Monte Verde) de ônibus a partir de São Paulo, Campinas ou BH. De Camanducaia, há serviço de táxi e transporte local até Monte Verde. A pousada pode indicar contatos de motoristas locais confiáveis. Consulte-nos antes de viajar.</div></div>
        </div>

        <div class="faq-item">
          <div class="faq-q" onclick="toggleFaq(this)">
            <span class="faq-q-text">Posso fazer surpresa de aniversário ou aniversário de casamento?</span>
            <span class="faq-chevron">⌄</span>
          </div>
          <div class="faq-a"><div class="faq-a-inner">Com muito prazer! Entre em contato antes da chegada e nossa equipe prepara a surpresa: decoração com flores de lavanda, mensagem personalizada, champagne, bolo artesanal ou outros mimos. Informe o motivo no campo de observações da reserva ou nos acione via WhatsApp.</div></div>
        </div>

      </div>
    </div>
  </div>
</div><!-- /faq -->
    `;
}
