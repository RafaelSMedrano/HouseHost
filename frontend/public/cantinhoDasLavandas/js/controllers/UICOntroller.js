import { renderAcomodacoesView } from "../views/acomodacoesView.js?v=2026-06-01-real-house";
import { renderContatoView } from "../views/contatoView.js?v=2026-07-23-privacy-policy";
import { renderDestinoView } from "../views/destinoView.js?v=2026-06-03-contact-info";
import { renderExperienceBemestarView } from "../views/experienceBemestarView.js?v=2026-06-01-experience-references";
import { renderExperienceCavaloView } from "../views/experienceCavaloView.js?v=2026-06-01-experience-references";
import { renderExperienceCentrinhoView } from "../views/experienceCentrinhoView.js?v=2026-06-01-experience-references";
import { renderExperienceChocolateView } from "../views/experienceChocolateView.js?v=2026-06-01-experience-references";
import { renderExperienceFaunaView } from "../views/experienceFaunaView.js?v=2026-06-01-experience-references";
import { renderExperienceFotografiaView } from "../views/experienceFotografiaView.js?v=2026-06-01-experience-references";
import { renderExperienceFrioView } from "../views/experienceFrioView.js?v=2026-06-01-experience-references";
import { renderExperienceGastronomiaView } from "../views/experienceGastronomiaView.js?v=2026-06-01-experience-references";
import { renderExperienceLavandasView } from "../views/experienceLavandasView.js?v=2026-06-01-experience-references";
import { renderExperiencePorDoSolView } from "../views/experiencePorDoSolView.js?v=2026-06-01-experience-references";
import { renderExperienceTrilhasView } from "../views/experienceTrilhasView.js?v=2026-06-01-experience-references";
import { renderExperienciasView } from "../views/experienciasView.js?v=2026-06-01-experience-pages";
import { renderFaqView } from "../views/faqView.js?v=2026-06-01-real-house";
import { renderGaleriaView } from "../views/galeriaView.js?v=2026-06-03-contact-info";
import { renderHomeView } from "../views/homeView.js?v=2026-06-08-logo-path";
import { renderPoliticaPrivacidadeView } from "../views/politicaPrivacidadeView.js?v=2026-07-28-versioned-privacy-policy";
import { renderReservaView } from "../views/reservaView.js?v=2026-08-21-public-booking-email";
import { clearPublicFooterWidget, renderPublicFooterWidget } from "../widgets/publicFooterWidget.js?v=2026-07-23-privacy-policy";
import { renderPublicSidebarWidget, setPublicSidebarActive } from "../widgets/publicSidebarWidget.js?v=2026-06-03-sidebar-socials";
import {
    focusReservationPrivacyFeedback,
    initCursor,
    initPageInteractions,
    installPublicGlobals,
    isReservationSubmissionInProgress,
} from "./publicInteractions.js?v=2026-08-21-public-booking-email";

export function startPublicUIController(containerId) {
    const container = document.getElementById(containerId);
    let activeView = null;

    if (!container) {
        return;
    }

    container.className = "public-site";
    container.innerHTML = `
        <div class="cursor" id="cursor"></div>
        <div class="cursor-ring" id="cursorRing"></div>
        <button class="sidebar-toggle" id="sidebarToggle" type="button" aria-label="Abrir menu">
          <span class="st-line"></span>
          <span class="st-line"></span>
          <span class="st-line"></span>
        </button>
        <div id="sidebar-container"></div>
        <main class="main" id="mainContent">
            <div id="public-page-container"></div>
            <div id="public-footer-container"></div>
        </main>
        <div class="toast" id="toast" role="status" aria-live="polite" aria-atomic="true"><span class="toast-icon" aria-hidden="true">✓</span><span id="toastMsg"></span></div>
    `;

    installPublicGlobals(navigateTo);
    initCursor();
    renderPublicSidebarWidget("sidebar-container", {
        onNavigate: navigateTo,
    });

    document.getElementById("sidebarToggle").addEventListener("click", () => {
        document.getElementById("sidebar")?.classList.toggle("open");
    });

    navigateTo(viewFromLocation(), false);

    window.addEventListener("popstate", (event) => {
        navigateTo(event.state?.publicView || viewFromLocation(), false);
    });

    function navigateTo(view, updateHistory = true) {
        if (activeView === view) {
            return;
        }

        if (activeView === "reserva" && isReservationSubmissionInProgress()) {
            if (!updateHistory) {
                window.history.pushState(
                        { publicView: activeView },
                        "",
                        targetUrlForView(activeView)
                );
            }
            window.showToast?.("Aguarde a conclusão do envio antes de sair desta página.");
            focusReservationPrivacyFeedback();
            return;
        }

        if (updateHistory) {
            window.history.pushState({ publicView: view }, "", targetUrlForView(view));
        }

        if (view === "home") {
            renderHomePanel();
            return;
        }

        if (view === "acomodacoes") {
            renderAcomodacoesPanel();
            return;
        }

        if (view === "experiencias") {
            renderExperienciasPanel();
            return;
        }

        if (view === "destino") {
            renderDestinoPanel();
            return;
        }

        if (view === "galeria") {
            renderGaleriaPanel();
            return;
        }

        if (view === "faq") {
            renderFaqPanel();
            return;
        }

        if (view === "contato") {
            renderContatoPanel();
            return;
        }

        if (view === "reserva") {
            renderReservaPanel();
            return;
        }

        if (view === "privacidade") {
            renderPoliticaPrivacidadePanel();
        }
    }

    function viewFromLocation() {
        return window.location.hash === "#politica-de-privacidade" ? "privacidade" : "home";
    }

    function targetUrlForView(view) {
        return view === "privacidade"
            ? "#politica-de-privacidade"
            : `${window.location.pathname}${window.location.search}`;
    }

    function renderHomePanel() {
        renderPublicView("home", renderHomeView);
    }

    function renderAcomodacoesPanel() {
        renderPublicView("acomodacoes", renderAcomodacoesView);
    }

    function renderExperienciasPanel() {
        renderPublicView("experiencias", (targetContainerId) => {
            renderExperienciasView(targetContainerId, {
                onOpenExperience: renderExperiencePanel,
            });
        });
    }

    function renderExperiencePanel(experience) {
        const views = {
            trilhas: renderExperienceTrilhasView,
            gastronomia: renderExperienceGastronomiaView,
            frio: renderExperienceFrioView,
            chocolate: renderExperienceChocolateView,
            lavandas: renderExperienceLavandasView,
            fauna: renderExperienceFaunaView,
            bemestar: renderExperienceBemestarView,
            centrinho: renderExperienceCentrinhoView,
            pordosol: renderExperiencePorDoSolView,
            cavalo: renderExperienceCavaloView,
            fotografia: renderExperienceFotografiaView,
        };
        const renderView = views[experience];

        if (!renderView) {
            renderExperienciasPanel();
            return;
        }

        renderPublicView("experiencias", (targetContainerId) => {
            renderView(targetContainerId, {
                onBack: renderExperienciasPanel,
            });
        });
    }

    function renderDestinoPanel() {
        renderPublicView("destino", renderDestinoView);
    }

    function renderGaleriaPanel() {
        renderPublicView("galeria", renderGaleriaView);
    }

    function renderFaqPanel() {
        renderPublicView("faq", renderFaqView);
    }

    function renderContatoPanel() {
        renderPublicView("contato", renderContatoView);
    }

    function renderReservaPanel() {
        renderPublicView("reserva", renderReservaView);
    }

    function renderPoliticaPrivacidadePanel() {
        renderPublicView("privacidade", renderPoliticaPrivacidadeView);
    }

    function renderPublicView(view, renderView) {
        renderView("public-page-container");
        activeView = view;
        if (view === "reserva") {
            clearPublicFooterWidget("public-footer-container");
        } else {
            renderPublicFooterWidget("public-footer-container");
        }
        setPublicSidebarActive(view);
        document.getElementById("sidebar")?.classList.remove("open");
        window.scrollTo({ top: 0, behavior: "auto" });
        initPageInteractions(view);
    }
}
