export function renderBrandLogoMarkup(variant = "default") {
    return `
        <div class="hh-logo hh-logo--${variant} hh-logo--fallback" aria-label="HouseHost">
            <span class="hh-logo-fallback" aria-hidden="true">HH</span>
        </div>
    `;
}
