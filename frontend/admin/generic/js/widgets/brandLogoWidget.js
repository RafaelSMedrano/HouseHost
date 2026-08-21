export function renderBrandLogoMarkup(variant = "default") {
    return `
        <div class="hh-logo hh-logo--${variant}" aria-label="HouseHost">
            <span class="hh-logo-text">HouseHost</span>
        </div>
    `;
}
