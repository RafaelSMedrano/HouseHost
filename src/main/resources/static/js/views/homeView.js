export function renderHomeView(containerId, username) {
    const container = document.getElementById(containerId);

    container.className = "dashboard-home";
    container.innerHTML = `
<!-- SIDEBAR -->
<div class="sidebar">
  <div class="brand">
    <div class="brand-badge">
      <svg width="22" height="22" viewBox="0 0 22 22" fill="none" xmlns="http://www.w3.org/2000/svg">
        <circle cx="11" cy="11" r="8" stroke="rgba(255,255,255,0.85)" stroke-width="1.5"/>
        <path d="M11 6.5C11 6.5 7.5 8.8 7.5 12C7.5 15.2 11 15.5 11 15.5C11 15.5 14.5 15 14.5 12C14.5 9 11 6.5 11 6.5Z" fill="rgba(255,255,255,0.55)"/>
        <path d="M11 3.5V5.5M11 16.5V18.5M4 11H6M16 11H18" stroke="rgba(255,255,255,0.50)" stroke-width="1.3" stroke-linecap="round"/>
      </svg>
    </div>
    <div class="brand-name">Cantinho das Lavandas</div>
    <div class="brand-loc">Monte Verde · MG</div>
  </div>

  <nav>
    <div class="nav-group">Geral</div>
    <div class="nav-item active"><i class="ti ti-layout-dashboard"></i> Dashboard</div>
    <div class="nav-item"><i class="ti ti-door"></i> Quartos <span class="nav-badge nb-rose">2</span></div>

    <div class="nav-group">Hospedagem</div>
    <div class="nav-item"><i class="ti ti-login"></i> Check-in</div>
    <div class="nav-item"><i class="ti ti-logout"></i> Check-out</div>
    <div class="nav-item"><i class="ti ti-calendar-event"></i> Reservas</div>
    <div class="nav-item"><i class="ti ti-users"></i> Hóspedes</div>

    <div class="nav-group">Operação</div>
    <div class="nav-item"><i class="ti ti-package"></i> Consumíveis <span class="nav-badge nb-amber">3</span></div>
    <div class="nav-item"><i class="ti ti-tool"></i> Manutenção</div>
    <div class="nav-item"><i class="ti ti-chart-bar"></i> Relatórios</div>

    <div class="nav-group">Sistema</div>
    <div class="nav-item"><i class="ti ti-settings"></i> Configurações</div>
  </nav>

  <div class="sidebar-user">
    <div class="avatar">AM</div>
    <div>
      <div class="user-name">Ana Mota</div>
      <div class="user-role">Recepcionista</div>
    </div>
  </div>
</div>

<!-- MAIN -->
<div class="main">
  <div class="topbar">
    <div>
      <div class="page-title">Visão Geral</div>
      <div class="page-sub">Terça-feira, 12 de maio de 2026</div>
    </div>
    <div class="topbar-right">
      <div class="tabs">
        <div class="tab active">Hoje</div>
        <div class="tab">Semana</div>
        <div class="tab">Mês</div>
      </div>
      <button class="btn btn-primary"><i class="ti ti-plus"></i> Novo check-in</button>
    </div>
  </div>

  <div class="content">
    <!-- MÉTRICAS -->
    <div class="metrics">
      <div class="mc">
        <div class="mc-label">Ocupação</div>
        <div class="mc-value">73%</div>
        <div class="mc-sub"><span class="chip chip-green">↑ 8%</span> vs ontem</div>
      </div>
      <div class="mc">
        <div class="mc-label">Check-ins hoje</div>
        <div class="mc-value">4</div>
        <div class="mc-sub"><span class="chip chip-lav">2 pendentes</span></div>
      </div>
      <div class="mc">
        <div class="mc-label">Check-outs hoje</div>
        <div class="mc-value">3</div>
        <div class="mc-sub"><span class="chip chip-amber">1 em andamento</span></div>
      </div>
      <div class="mc">
        <div class="mc-label">Receita do mês</div>
        <div class="mc-value">R$18.420</div>
        <div class="mc-sub"><span class="chip chip-green">↑ 12%</span> vs mai/25</div>
      </div>
    </div>

    <!-- QUARTOS -->
    <div>
      <div class="section-head">
        <div class="section-title">Status dos quartos</div>
        <div class="legend">
          <div class="leg"><span class="dot dot-occ"></span>Ocupado</div>
          <div class="leg"><span class="dot dot-free"></span>Disponível</div>
          <div class="leg"><span class="dot dot-clean"></span>Limpeza</div>
          <div class="leg"><span class="dot dot-maint"></span>Manutenção</div>
        </div>
      </div>
      <div class="rooms">
        <div class="room occ">
          <div class="room-num">01</div>
          <div class="room-type">Suíte Lavanda</div>
          <div class="room-st"><span class="dot dot-occ"></span><span class="st-occ">Ocupado</span></div>
          <div class="room-guest">Família Mendes</div>
          <div class="room-out">Saída: 14/05</div>
        </div>
        <div class="room">
          <div class="room-num">02</div>
          <div class="room-type">Chalé Serrano</div>
          <div class="room-st"><span class="dot dot-free"></span><span class="st-free">Disponível</span></div>
          <div class="room-guest" style="color:var(--lav-light)">—</div>
        </div>
        <div class="room occ">
          <div class="room-num">03</div>
          <div class="room-type">Quarto Vista</div>
          <div class="room-st"><span class="dot dot-occ"></span><span class="st-occ">Ocupado</span></div>
          <div class="room-guest">Sr. Carvalho</div>
          <div class="room-out">Saída: 13/05</div>
        </div>
        <div class="room clean">
          <div class="room-num">04</div>
          <div class="room-type">Chalé Serrano</div>
          <div class="room-st"><span class="dot dot-clean"></span><span class="st-clean">Limpeza</span></div>
          <div class="room-guest">Saída realizada</div>
        </div>
        <div class="room occ">
          <div class="room-num">05</div>
          <div class="room-type">Suíte Master</div>
          <div class="room-st"><span class="dot dot-occ"></span><span class="st-occ">Ocupado</span></div>
          <div class="room-guest">Casal Oliveira</div>
          <div class="room-out">Saída: 15/05</div>
        </div>
        <div class="room">
          <div class="room-num">06</div>
          <div class="room-type">Quarto Vista</div>
          <div class="room-st"><span class="dot dot-free"></span><span class="st-free">Disponível</span></div>
          <div class="room-guest" style="color:var(--lav-light)">—</div>
        </div>
        <div class="room maint">
          <div class="room-num">07</div>
          <div class="room-type">Chalé Serrano</div>
          <div class="room-st"><span class="dot dot-maint"></span><span class="st-maint">Manutenção</span></div>
          <div class="room-guest">Aquecedor</div>
        </div>
        <div class="room occ">
          <div class="room-num">08</div>
          <div class="room-type">Suíte Lavanda</div>
          <div class="room-st"><span class="dot dot-occ"></span><span class="st-occ">Ocupado</span></div>
          <div class="room-guest">Família Costa</div>
          <div class="room-out">Saída: 16/05</div>
        </div>
        <div class="room">
          <div class="room-num">09</div>
          <div class="room-type">Quarto Vista</div>
          <div class="room-st"><span class="dot dot-free"></span><span class="st-free">Disponível</span></div>
          <div class="room-guest" style="color:var(--lav-light)">—</div>
        </div>
        <div class="room occ">
          <div class="room-num">10</div>
          <div class="room-type">Suíte Master</div>
          <div class="room-st"><span class="dot dot-occ"></span><span class="st-occ">Ocupado</span></div>
          <div class="room-guest">Dr. Pires</div>
          <div class="room-out">Saída: 14/05</div>
        </div>
      </div>
    </div>

    <!-- PAINEIS INFERIORES -->
    <div class="panels">
      <div class="panel">
        <div class="panel-title">Check-ins & check-outs de hoje</div>
        <div class="ci-item">
          <div>
            <div class="ci-name">Rodrigo e Patrícia Lima</div>
            <div class="ci-info">2 adultos · 3 noites · Check-in</div>
          </div>
          <div class="ci-right">
            <div class="ci-room">Qto 02</div>
            <div class="ci-time">Prev. 14h00</div>
            <button class="act act-in">Registrar ↓</button>
          </div>
        </div>
        <div class="ci-item">
          <div>
            <div class="ci-name">Amanda Vieira</div>
            <div class="ci-info">1 adulto · 2 noites · Check-in</div>
          </div>
          <div class="ci-right">
            <div class="ci-room">Qto 06</div>
            <div class="ci-time">Prev. 16h00</div>
            <button class="act act-in">Registrar ↓</button>
          </div>
        </div>
        <div class="ci-item">
          <div>
            <div class="ci-name">Paulo Mendes</div>
            <div class="ci-info">1 adulto · Check-out</div>
          </div>
          <div class="ci-right">
            <div class="ci-room">Qto 03</div>
            <div class="ci-time">Até 12h00</div>
            <button class="act act-out">Checkout ↑</button>
          </div>
        </div>
        <div class="ci-item">
          <div>
            <div class="ci-name">Família Costa</div>
            <div class="ci-info">4 pessoas · Check-out</div>
          </div>
          <div class="ci-right">
            <div class="ci-room">Qto 08</div>
            <div class="ci-time">Até 12h00</div>
            <button class="act act-out">Checkout ↑</button>
          </div>
        </div>
      </div>

      <div class="panel">
        <div class="panel-title">Consumíveis — estoque atual</div>
        <div class="cons">
          <div class="con-item">
            <div class="con-icon" style="background:var(--lav-pale)"><i class="ti ti-droplet" style="color:var(--lav)"></i></div>
            <div class="con-body">
              <div class="con-name">Amenidades (xampu, sabonete)</div>
              <div class="con-bar"><div class="con-fill fill-ok" style="width:72%"></div></div>
            </div>
            <div class="con-qty qty-ok">72 un.</div>
          </div>
          <div class="con-item">
            <div class="con-icon" style="background:#FFF5E6"><i class="ti ti-bed" style="color:var(--amber)"></i></div>
            <div class="con-body">
              <div class="con-name">Enxoval (toalhas, lençóis)</div>
              <div class="con-bar"><div class="con-fill fill-low" style="width:28%"></div></div>
            </div>
            <div class="con-qty qty-low">28 jogos</div>
          </div>
          <div class="con-item">
            <div class="con-icon" style="background:#FFF5E6"><i class="ti ti-coffee" style="color:var(--amber)"></i></div>
            <div class="con-body">
              <div class="con-name">Café, chá e sachês</div>
              <div class="con-bar"><div class="con-fill fill-low" style="width:35%"></div></div>
            </div>
            <div class="con-qty qty-low">35 cx.</div>
          </div>
          <div class="con-item">
            <div class="con-icon" style="background:var(--rose-pale)"><i class="ti ti-spray" style="color:var(--rose)"></i></div>
            <div class="con-body">
              <div class="con-name">Produtos de limpeza ⚠</div>
              <div class="con-bar"><div class="con-fill fill-crit" style="width:8%"></div></div>
            </div>
            <div class="con-qty qty-crit">8 un.</div>
          </div>
          <div class="con-item">
            <div class="con-icon" style="background:var(--sage-pale)"><i class="ti ti-leaf" style="color:var(--sage)"></i></div>
            <div class="con-body">
              <div class="con-name">Mini geleias artesanais</div>
              <div class="con-bar"><div class="con-fill fill-ok" style="width:60%"></div></div>
            </div>
            <div class="con-qty qty-ok">60 un.</div>
          </div>
        </div>
      </div>
    </div>

  </div><!-- /content -->
</div><!-- /main -->
    `;

    const userName = container.querySelector(".user-name");
    if (userName) {
        userName.innerText = username;
    }

    const userAvatar = container.querySelector(".avatar");
    if (userAvatar) {
        userAvatar.innerText = initialsFor(username);
    }

    document.querySelectorAll('.tab').forEach(t => {
    t.addEventListener('click', () => {
      document.querySelectorAll('.tab').forEach(x => x.classList.remove('active'));
      t.classList.add('active');
    });
  });
  document.querySelectorAll('.nav-item').forEach(n => {
    n.addEventListener('click', () => {
      document.querySelectorAll('.nav-item').forEach(x => x.classList.remove('active'));
      n.classList.add('active');
    });
  });
}

function initialsFor(username) {
    return username
        .split(/\s+/)
        .filter(Boolean)
        .slice(0, 2)
        .map(part => part[0].toUpperCase())
        .join("") || "HH";
}
