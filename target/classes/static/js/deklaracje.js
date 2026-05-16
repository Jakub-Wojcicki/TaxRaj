$(document).ready(function () {
    $('#declarationTable').DataTable({
        language: { /* ... */ },
        ordering: false,
        pageLength: 10,
        initComplete: function () {
            $(this.api().table().container()).closest('.table-card').addClass('dt-ready');
        }
    });
});