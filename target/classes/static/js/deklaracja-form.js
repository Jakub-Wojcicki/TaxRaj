document.addEventListener('DOMContentLoaded', function () {

    const vatNaliczony = document.getElementById('vatNaliczony');
    const vatNalezny   = document.getElementById('vatNalezny');
    const saldoOutput  = document.getElementById('saldoPodglad');

    function przeliczSaldo() {
        const nali = parseFloat(vatNaliczony.value) || 0;
        const nal  = parseFloat(vatNalezny.value)   || 0;
        const saldo = nal - nali;
        const prefix = saldo > 0 ? 'do zapłaty: ' : (saldo < 0 ? 'do zwrotu: ' : '');
        saldoOutput.value = prefix + Math.abs(saldo).toFixed(2) + ' zł';
    }

    if (vatNaliczony && vatNalezny && saldoOutput) {
        vatNaliczony.addEventListener('input', przeliczSaldo);
        vatNalezny.addEventListener('input', przeliczSaldo);
        przeliczSaldo();
    }

});