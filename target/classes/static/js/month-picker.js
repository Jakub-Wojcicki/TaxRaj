document.addEventListener('DOMContentLoaded', function () {

    const fields = document.querySelectorAll('.month-picker');

    fields.forEach(field => {
        flatpickr(field, {
            locale: 'pl',
            dateFormat: 'Y-m',
            defaultDate: field.value || null,
            plugins: [
                new monthSelectPlugin({
                    shorthand: false,
                    dateFormat: 'Y-m',
                    altFormat: 'F Y',
                    theme: 'light'
                })
            ],
            maxDate: 'today'
        });
    });

});