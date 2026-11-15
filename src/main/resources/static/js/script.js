// Navegação e indicação visual da página atual.
const menuToggle = document.querySelector('.toggle-btn');
const navigation = document.getElementById('sidebar');
menuToggle?.addEventListener('click', () => {
    const expanded = navigation?.classList.toggle('active') || false;
    menuToggle.setAttribute('aria-expanded', String(expanded));
});
function toggleSubmenu(event, element) {
    event.preventDefault();
    const expanded = element.parentElement.classList.toggle('open');
    element.setAttribute('aria-expanded', String(expanded));
}
document.querySelectorAll('#sidebar a[href]').forEach(link => {
    if (link.getAttribute('href') === window.location.pathname) {
        link.classList.add('is-current');
        link.setAttribute('aria-current', 'page');
        const settings = link.closest('.settings-menu');
        if (settings) settings.querySelector(':scope > a').classList.add('is-current');
    }
});
