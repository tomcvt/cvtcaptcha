
export const Navbar = {
    init: function() {
        document.querySelectorAll('.navbar-item.dropdown').forEach(item => {
            const link = item.querySelector('.navbar-link');
            const menu = item.querySelector('.dropdown-menu');
            if (!link || !menu) return;
            item.addEventListener('mouseenter', () => {
                menu.classList.add('open');
            });
            item.addEventListener('mouseleave', () => {
                menu.classList.remove('open');
            });
            link.addEventListener('click', e => {
                if (window.innerWidth < 900) {
                    e.preventDefault();
                    menu.classList.toggle('open');
                }
            });
        });
    }
};
