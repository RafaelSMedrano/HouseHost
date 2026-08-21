export function renderBrandLogoMarkup(variant = "default") {
    return `
        <div class="hh-logo hh-logo--${variant}" aria-label="Cantinho das Lavandas">
            <img class="hh-logo-image" src="/assets/logo/logo.svg?v=202606061916" alt="Cantinho das Lavandas">
        </div>
    `;
}
