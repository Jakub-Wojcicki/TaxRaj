$(document).ready(function () {
    $('#invoiceTable').DataTable({
        language: { /* ... */ },
        ordering: false,    // ← WYŁĄCZ sortowanie DataTables
        paging: true,
        searching: true,
        pageLength: 10,
        initComplete: function () {
            $(this.api().table().container()).closest('.table-card').addClass('dt-ready');
        }
    });
});