    document.addEventListener('DOMContentLoaded', function () {
    const dropdownBtn = document.getElementById('userDropdownBtn');
    const dropdownMenu = document.getElementById('userDropdownMenu');

    dropdownBtn.addEventListener('click', function (e) {
    e.stopPropagation();
    dropdownMenu.classList.toggle('show');
});

    document.addEventListener('click', function () {
    dropdownMenu.classList.remove('show');
});
});