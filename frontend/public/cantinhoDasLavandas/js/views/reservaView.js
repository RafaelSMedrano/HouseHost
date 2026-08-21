export function renderReservaView(containerId) {
    const container = document.getElementById(containerId);

    if (!container) {
        return;
    }

    container.innerHTML = `
<!-- ══ PAGE: RESERVA ══ -->
<div class="page active" id="page-reserva">
  <div class="reserva-layout">

    <!-- Painel esquerdo: formulário -->
    <div class="reserva-form-panel">

      <!-- Progress -->
      <div class="progress-steps">
        <div class="ps-item" id="ps1"><div class="ps-dot active" id="pd1">1</div><span class="ps-label">Casa</span></div>
        <div class="ps-item" id="ps2"><div class="ps-dot" id="pd2">2</div><span class="ps-label">Período</span></div>
        <div class="ps-item" id="ps3"><div class="ps-dot" id="pd3">3</div><span class="ps-label">Dados</span></div>
        <div class="ps-item" id="ps4"><div class="ps-dot" id="pd4">4</div><span class="ps-label">Termos</span></div>
        <div class="ps-item" id="ps5"><div class="ps-dot" id="pd5">5</div></div>
      </div>

      <!-- STEP 1: Casa -->
      <div class="rf-step active" id="rfStep1">
        <div class="step-header">
          <div class="step-num">01</div>
          <div class="step-title">Casa <em>privativa</em></div>
          <div class="step-desc">A reserva contempla a casa inteira: quarto de casal, quarto com duas camas de solteiro, sala, cozinha e banheiro.</div>
        </div>
        <div class="room-pick room-pick-single">
          <label class="rp-card picked" id="rpc-1">
            <input type="radio" name="quarto" value="Casa privativa" data-price="580" checked onchange="pickRoom(this)">
            <div class="rpc-visual rpcv-1">🏡</div>
            <div class="rpc-info">
              <div class="rpc-name">Casa privativa</div>
              <div class="rpc-tags"><span class="rpc-tag">2 quartos</span><span class="rpc-tag">Sala</span><span class="rpc-tag">Cozinha</span><span class="rpc-tag">Banheiro</span></div>
              <div class="rpc-price">R$ 580 <span>/noite</span></div>
            </div>
          </label>
        </div>
        <span class="ferr" id="ferr-quarto">Confirme a casa para continuar</span>
        <div class="form-btns" style="justify-content:flex-end">
          <button class="fbtn-next" onclick="rfGo(2)">Escolher datas →</button>
        </div>
      </div>

      <!-- STEP 2: Datas -->
      <div class="rf-step" id="rfStep2">
        <div class="step-header">
          <div class="step-num">02</div>
          <div class="step-title">Período da <em>estadia</em></div>
          <div class="step-desc">Check-in a partir das 14h. Check-out até 12h. Estadia mínima de 2 noites nos finais de semana.</div>
        </div>
        <div class="grid2" style="margin-bottom:20px">
          <div class="fld">
            <label class="fl">Check-in <span class="req">*</span></label>
            <div class="fi-wrap"><span class="fi-ico">📅</span><input type="date" class="fi" id="f-ci" oninput="updateSum()"></div>
            <span class="ferr" id="ferr-ci">Selecione a data de entrada</span>
          </div>
          <div class="fld">
            <label class="fl">Check-out <span class="req">*</span></label>
            <div class="fi-wrap"><span class="fi-ico">📅</span><input type="date" class="fi" id="f-co" oninput="updateSum()"></div>
            <span class="ferr" id="ferr-co">Selecione a data de saída</span>
          </div>
        </div>
        <div class="grid2" style="margin-bottom:20px">
          <div class="fld">
            <label class="fl">Hóspedes</label>
            <div class="fi-wrap no-ico"><select class="fs" id="f-hsp" onchange="updateSum()">
              <option data-adults="1" data-children="0">1 adulto</option>
              <option data-adults="2" data-children="0" selected>2 adultos</option>
              <option data-adults="3" data-children="0">3 adultos</option>
              <option data-adults="4" data-children="0">4 adultos</option>
              <option data-adults="2" data-children="1">2 adultos + 1 criança</option>
              <option data-adults="2" data-children="2">2 adultos + 2 crianças</option>
            </select></div>
          </div>
          <div class="fld">
            <label class="fl">Pets?</label>
            <div class="fi-wrap no-ico"><select class="fs" id="f-pet" onchange="updateSum()">
              <option value="0">Não</option><option value="1">Sim — 1 pet</option><option value="2">Sim — 2 pets</option>
            </select></div>
          </div>
        </div>
        <div class="fld">
          <label class="fl">Motivo da visita</label>
          <div class="fi-wrap no-ico"><select class="fs">
            <option>Lazer e descanso</option>
            <option>Aniversário / Data especial</option>
            <option>Lua de mel</option>
            <option>Família</option>
            <option>Trabalho remoto (bleisure)</option>
          </select></div>
        </div>
        <div class="form-btns">
          <button class="fbtn-back" onclick="rfGo(1)">← Voltar</button>
          <button class="fbtn-next" onclick="rfGo(3)">Dados pessoais →</button>
        </div>
      </div>

      <!-- STEP 3: Dados pessoais -->
      <div class="rf-step" id="rfStep3">
        <div class="step-header">
          <div class="step-num">03</div>
          <div class="step-title">Seus <em>dados</em></div>
          <div class="step-desc">Usaremos esses dados apenas para consultar disponibilidade, registrar sua solicitação, enviar comunicações transacionais por email e conversar pelo WhatsApp.</div>
        </div>
        <div class="grid2">
          <div class="fld">
            <label class="fl">Nome <span class="req">*</span></label>
            <div class="fi-wrap"><span class="fi-ico">👤</span><input type="text" class="fi" id="f-nome" placeholder="Nome" minlength="2" maxlength="80"></div>
            <span class="ferr" id="ferr-nome">Informe um nome válido entre 2 e 80 caracteres</span>
          </div>
          <div class="fld">
            <label class="fl">Sobrenome <span class="req">*</span></label>
            <div class="fi-wrap"><span class="fi-ico">👤</span><input type="text" class="fi" id="f-sob" placeholder="Sobrenome" minlength="2" maxlength="80"></div>
            <span class="ferr" id="ferr-sob">Informe um sobrenome válido entre 2 e 80 caracteres</span>
          </div>
          <div class="fld">
            <label class="fl">WhatsApp <span class="req">*</span></label>
            <div class="fi-wrap"><span class="fi-ico">📱</span><input type="tel" class="fi" id="f-tel" placeholder="(00) 00000-0000" maxlength="15" oninput="mF(this)"></div>
            <span class="ferr" id="ferr-tel">Informe um WhatsApp com DDD</span>
          </div>
          <div class="fld">
            <label class="fl" for="f-email">Email <span class="req">*</span></label>
            <div class="fi-wrap"><span class="fi-ico" aria-hidden="true">✉️</span><input type="email" class="fi" id="f-email" placeholder="voce@exemplo.com" required maxlength="255" autocomplete="email" inputmode="email" aria-describedby="ferr-email email-transactional-hint" aria-invalid="false" oninput="validateReservationEmail()" onblur="validateReservationEmail()"></div>
            <span class="ferr" id="ferr-email" role="alert">Informe um email válido com até 255 caracteres</span>
            <span class="fhint" id="email-transactional-hint">Usaremos este email somente para comunicações transacionais sobre sua solicitação, nunca para marketing.</span>
          </div>
          <div class="fld">
            <label class="fl">Cidade de origem</label>
            <div class="fi-wrap"><span class="fi-ico">📍</span><input type="text" class="fi" id="f-cid" placeholder="Ex: São Paulo - SP" maxlength="120"></div>
          </div>
          <div class="fld col2">
            <label class="fl">Pedidos especiais</label>
            <div class="fi-wrap no-ico"><textarea class="fi" id="f-obs" maxlength="500" style="padding-left:16px" placeholder="Ex: aniversário, berço, preferência de horário ou outra observação para a estadia."></textarea></div>
            <span class="fhint">Não envie CPF, dados de cartão, dados bancários ou informações sensíveis desnecessárias.</span>
          </div>
        </div>
        <div class="form-btns">
          <button class="fbtn-back" onclick="rfGo(2)">← Voltar</button>
          <button class="fbtn-next" onclick="rfGo(4)">Revisar termos →</button>
        </div>
      </div>

      <!-- STEP 4: Termos e privacidade -->
      <div class="rf-step" id="rfStep4">
        <div class="step-header">
          <div class="step-num">04</div>
          <div class="step-title">Termos e <em>privacidade</em></div>
          <div class="step-desc">Esta etapa envia uma solicitação de reserva. A confirmação final e os próximos passos serão combinados pelo WhatsApp.</div>
        </div>

        <div style="background:var(--sage-p);border:1px solid var(--sage-l);padding:18px;margin-bottom:18px;font-size:13px;color:var(--sage);line-height:1.7">
          <strong>Como usamos seus dados:</strong> coletamos nome, email, WhatsApp, período, quantidade de hóspedes e observações para consultar disponibilidade, registrar a solicitação e falar com você sobre a estadia. O email será usado somente em comunicações transacionais da solicitação, não para marketing. Não coletamos CPF nem dados financeiros pelo site nesta etapa.
        </div>

        <div class="reservation-policy-panel">
          <div class="reservation-policy-status" id="reservation-policy-status" role="status" aria-live="polite" aria-atomic="true" tabindex="-1">
            Carregando a política de privacidade vigente…
          </div>
          <button class="privacy-retry-button" id="reservation-policy-retry" type="button" onclick="retryPrivacyPolicy()" hidden>Tentar novamente</button>
          <details class="reservation-policy-details" id="reservation-policy-details" hidden>
            <summary id="reservation-policy-summary">Ler a política vigente</summary>
            <div class="reservation-policy-document" id="reservation-policy-document"></div>
          </details>
        </div>

        <p class="reservation-policy-link">
          Você também pode abrir a
          <a href="#politica-de-privacidade" target="_blank" rel="noopener noreferrer">Política de Privacidade em uma nova aba</a>
          sem perder os dados preenchidos.
        </p>

        <div class="privacy-acknowledgement" id="termsLbl" aria-disabled="true">
          <input class="privacy-acknowledgement-checkbox" type="checkbox" id="f-terms" onchange="toggleT()" disabled>
          <label for="f-terms">
            <strong>Confirmo que li a política vigente e aceito os termos da solicitação de reserva.</strong>
            <span>O email será usado apenas para comunicações transacionais da solicitação, sem consentimento de marketing. A confirmação e a forma de pagamento serão combinadas pelo WhatsApp informado; esta etapa não coleta CPF nem pagamento.</span>
          </label>
        </div>
        <span class="ferr" id="ferr-terms" style="margin-top:8px">Leia e confirme a política vigente para enviar a solicitação</span>

        <div class="form-btns">
          <button class="fbtn-back" onclick="rfGo(3)">← Voltar</button>
          <button class="fbtn-next success" id="reservation-submit" onclick="finalizar()" disabled>✓ Enviar solicitação</button>
        </div>
      </div>

      <!-- STEP 5: Confirmação -->
      <div class="rf-step" id="rfStep5">
        <div class="confirm-wrap">
          <div class="confirm-emoji">🌿</div>
          <div class="confirm-h2">Solicitação recebida!</div>
          <p class="confirm-sub">Obrigado, <strong id="cf-nome">hóspede</strong>! Recebemos sua solicitação de reserva. Nossa equipe vai falar com você pelo WhatsApp informado para confirmar disponibilidade e próximos passos.</p>
          <div class="confirm-code" id="cf-code">CL-000000</div>
        </div>
        <div class="confirm-detail-grid">
          <div class="cdg-item"><div class="cdg-label">Hospedagem</div><div class="cdg-val" id="cf-quarto">—</div></div>
          <div class="cdg-item"><div class="cdg-label">Check-in</div><div class="cdg-val" id="cf-ci">—</div></div>
          <div class="cdg-item"><div class="cdg-label">Check-out</div><div class="cdg-val" id="cf-co">—</div></div>
          <div class="cdg-item"><div class="cdg-label">Noites</div><div class="cdg-val" id="cf-noites">—</div></div>
          <div class="cdg-item"><div class="cdg-label">Hóspedes</div><div class="cdg-val" id="cf-hsp">—</div></div>
          <div class="cdg-item" style="grid-column:1/-1">
            <div class="cdg-label">Total estimado</div>
            <div class="cdg-val" style="font-family:var(--serif);font-size:28px;font-weight:900;color:var(--lav-d)" id="cf-total">—</div>
          </div>
        </div>
        <div style="background:var(--sage-p);border:1px solid var(--sage-l);padding:18px;margin-top:20px;font-size:13px;color:var(--sage);line-height:1.7">
          ✅ Dúvidas? Fale com a gente: <strong>+55 12 99252-5319</strong> via WhatsApp
        </div>
        <div style="display:flex;gap:12px;margin-top:24px;flex-wrap:wrap">
          <button class="fbtn-next" onclick="goPage('home')">🏠 Voltar ao site</button>
          <button class="fbtn-back" onclick="window.open('https://wa.me/5512992525319', '_blank', 'noopener,noreferrer')">💬 WhatsApp</button>
        </div>
      </div>

    </div><!-- /form panel -->

    <!-- Painel direito: Resumo -->
    <div class="reserva-summary-panel">
      <div class="rsp-logo">Cantinho das Lavandas</div>
      <div class="rsp-title">Resumo da<br>sua reserva</div>

      <div class="sum-room-preview">
        <div class="srp-thumb">🏡</div>
        <div>
          <div class="srp-name" id="s-quarto">Casa privativa</div>
          <div class="srp-cap" id="s-cap">—</div>
        </div>
      </div>

      <div class="sum-rows">
        <div class="sum-row"><span class="sum-key">Check-in</span><span class="sum-val" id="s-ci">—</span></div>
        <div class="sum-row"><span class="sum-key">Check-out</span><span class="sum-val" id="s-co">—</span></div>
        <div class="sum-row"><span class="sum-key">Noites</span><span class="sum-val" id="s-noites">—</span></div>
        <div class="sum-row"><span class="sum-key">Hóspedes</span><span class="sum-val" id="s-hsp">—</span></div>
        <div class="sum-row"><span class="sum-key">Diária</span><span class="sum-val" id="s-rate">—</span></div>
        <div class="sum-row"><span class="sum-key">Café da manhã</span><span class="sum-val" style="color:var(--sage-l)">Incluso ✓</span></div>
      </div>

      <div class="sum-total">
        <span class="sum-total-label">Total</span>
        <span class="sum-total-val" id="s-total">R$ —</span>
      </div>

      <div class="sum-perks">
        <div class="sum-perk">Melhor preço direto</div>
        <div class="sum-perk">Sem taxas adicionais</div>
        <div class="sum-perk">Cancelamento gratuito 48h</div>
        <div class="sum-perk">Atendimento por WhatsApp</div>
      </div>
    </div>

  </div>
</div><!-- /reserva -->
    `;
}
