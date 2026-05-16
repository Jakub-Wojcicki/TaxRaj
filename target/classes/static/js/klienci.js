$(document).ready(function () {
    $('#clientTable').DataTable({
        language: {
            search: 'Szukaj:',
            lengthMenu: 'Pokaż _MENU_ wierszy',
            info: 'Wyświetlono _START_–_END_ z _TOTAL_',
            infoEmpty: 'Brak danych',
            paginate: { previous: '‹', next: '›' },
            zeroRecords: 'Brak wyników'
        },
        order: [[0, 'asc']],
        pageLength: 10,
        initComplete: function () {
            $(this.api().table().container()).closest('.table-card').addClass('dt-ready');
        }
    });
});