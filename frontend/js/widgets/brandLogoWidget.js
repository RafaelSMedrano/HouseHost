export function renderBrandLogoMarkup(variant = "default") {
    return `
        <div class="hh-logo hh-logo--${variant}" aria-label="Cantinho das Lavandas">
            <img class="hh-logo-image" src="../docs/LOGO.jpeg" alt="Cantinho das Lavandas">
        </div>
    `;
}
