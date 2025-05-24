// Fonction pour formater en USD
function formatUSD(amount) {
    return Number(amount).toLocaleString('en-US', {
        style: 'currency',
        currency: 'USD',
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });
}

// Appliquer le formatage au chargement de la page
document.addEventListener('DOMContentLoaded', function() {
    const currencyElements = document.querySelectorAll('.currency');
    currencyElements.forEach(element => {
        const amount = element.getAttribute('data-amount');
        if (amount !== null) {
            element.textContent = formatUSD(amount);
        } else {
            element.textContent = formatUSD(0); // Par défaut $0.00 si null
        }
    });
});