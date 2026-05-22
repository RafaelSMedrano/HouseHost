export function renderBrandLogoMarkup(variant = "default") {
    return `
        <div class="hh-logo hh-logo--${variant}" aria-label="Cantinho das Lavandas">
            <span class="hh-logo-fallback" aria-hidden="true">HH</span>
            <img
                class="hh-logo-image"
                src="../docs/LOGO.jpeg"
                alt="Cantinho das Lavandas"
                onerror="this.closest('.hh-logo').classList.add('hh-logo--fallback')"
            >
        </div>
    `;
}
