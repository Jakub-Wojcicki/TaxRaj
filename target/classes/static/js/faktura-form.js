document.addEventListener('DOMContentLoaded', function () {

    const tbody    = document.getElementById('positionsBody');
    const template = document.getElementById('positionTemplate');
    const addBtn   = document.getElementById('addRow');

    // Dodaj wiersz
    addBtn.addEventListener('click', function () {
        const index = tbody.querySelectorAll('.position-row').length;
        const html  = template.innerHTML.replace(/INDEX/g, index);
        tbody.insertAdjacentHTML('beforeend', html);
        przeliczWszystko();
    });

    // Delegacja — usuwanie wiersza i przeliczanie przy zmianie input-ów
    tbody.addEventListener('click', function (e) {
        const removeBtn = e.target.closest('.remove-row');
        if (removeBtn) {
            if (tbody.querySelectorAll('.position-row').length > 1) {
                removeBtn.closest('tr').remove();
            } else {
                // Ostatni wiersz — wyczyść zamiast usuwać
                const row = removeBtn.closest('tr');
                row.querySelectorAll('input').forEach(i => i.value = '');
            }
            przenumerujWiersze();
            przeliczWszystko();
        }
    });

    tbody.addEventListener('input', function (e) {
        if (e.target.matches('.qty, .price, .vat')) {
            przeliczWiersz(e.target.closest('tr'));
            przeliczPodsumowanie();
        }
    });

    function przeliczWiersz(row) {
        let qty = parseFloat(row.querySelector('.qty')?.value);
        if (!qty || qty <= 0) qty = 1;  // ← NOWE: domyślnie 1 sztuka
        const price = parseFloat(row.querySelector('.price')?.value) || 0;
        const vat   = parseFloat(row.querySelector('.vat')?.value)   || 0;

        const netto  = qty * price;
        const brutto = netto * (1 + vat / 100);

        row.querySelector('.net-value').textContent   = netto.toFixed(2);
        row.querySelector('.gross-value').textContent = brutto.toFixed(2);
    }

    function przeliczPodsumowanie() {
        let totalNet = 0;
        let totalGross = 0;
        tbody.querySelectorAll('.position-row').forEach(row => {
            totalNet   += parseFloat(row.querySelector('.net-value').textContent)   || 0;
            totalGross += parseFloat(row.querySelector('.gross-value').textContent) || 0;
        });
        document.querySelector('.total-net').textContent   = totalNet.toFixed(2);
        document.querySelector('.total-gross').textContent = totalGross.toFixed(2);
    }

    function przeliczWszystko() {
        tbody.querySelectorAll('.position-row').forEach(przeliczWiersz);
        przeliczPodsumowanie();
    }

    function przenumerujWiersze() {
        tbody.querySelectorAll('.position-row').forEach((row, i) => {
            row.querySelectorAll('input').forEach(input => {
                if (input.name) {
                    input.name = input.name.replace(/pozycje\[\d+\]/, `pozycje[${i}]`);
                }
            });
        });
    }

    // Inicjalne przeliczenie (jeśli edycja faktury z pozycjami)
    przeliczWszystko();

    const typSelect = document.getElementById('typFaktury');
    const numerInput = document.getElementById('numerFaktury');

    if (typSelect && numerInput) {
        typSelect.addEventListener('change', async function () {
            try {
                const response = await fetch('/faktury/generuj-numer?typ=' + this.value);
                if (response.ok) {
                    numerInput.value = await response.text();
                }
            } catch (e) {
                console.error('Nie udało się wygenerować numeru:', e);
            }
        });
    }

});