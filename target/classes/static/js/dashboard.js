$(document).ready(function () {

  $('#invoiceTable').DataTable({
    language: {
      search: 'Szukaj:',
      lengthMenu: 'Pokaż _MENU_ wierszy',
      info: 'Wyświetlono _START_–_END_ z _TOTAL_',
      infoEmpty: 'Brak danych',
      paginate: { previous: '‹', next: '›' },
      zeroRecords: 'Brak wyników'
    },
    initComplete: function () {
      $(this.api().table().container()).closest('.table-card').addClass('dt-ready');
    },
    ordering: false,
    pageLength: 10
  });

  const root   = getComputedStyle(document.documentElement);
  const gold   = root.getPropertyValue('--gold').trim()        || '#b8973a';
  const green  = root.getPropertyValue('--green').trim()       || '#2d7a4f';
  const red    = root.getPropertyValue('--red').trim()         || '#c0392b';
  const blue   = root.getPropertyValue('--blue').trim()        || '#2563a8';
  const border = root.getPropertyValue('--paper-dark').trim()  || '#ece8dc';
  const muted  = root.getPropertyValue('--ink-muted').trim()   || '#8a8a80';

  const data = window.dashboardData || {};

  function hexToRgb(hex) {
    const m = hex.match(/^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i);
    return m ? `${parseInt(m[1],16)},${parseInt(m[2],16)},${parseInt(m[3],16)}` : '0,0,0';
  }

  function buildLineChart(canvasId, labels, values, color, suffix, isInt) {
    const canvas = document.getElementById(canvasId);
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    const gradient = ctx.createLinearGradient(0, 0, 0, 200);
    const rgb = hexToRgb(color);
    gradient.addColorStop(0, `rgba(${rgb},.18)`);
    gradient.addColorStop(1, `rgba(${rgb},.00)`);

    new Chart(ctx, {
      type: 'line',
      data: {
        labels: labels.length ? labels : ['Brak'],
        datasets: [{
          label: suffix,
          data: values.length ? values.map(Number) : [0],
          borderColor: color,
          backgroundColor: gradient,
          borderWidth: 2,
          pointBackgroundColor: color,
          pointRadius: 4,
          pointHoverRadius: 6,
          fill: true,
          tension: .4,
        }],
      },
      options: {
        responsive: true,
        plugins: {
          legend: { display: false },
          tooltip: {
            backgroundColor: '#1a1a18',
            titleColor: '#d4af5a',
            bodyColor: '#c0bdb0',
            padding: 10,
            callbacks: {
              label: ctx => isInt
                  ? ' ' + ctx.parsed.y + ' ' + suffix
                  : ' ' + ctx.parsed.y.toLocaleString('pl-PL') + ' ' + suffix
            }
          }
        },
        scales: {
          x: { grid: { color: border }, ticks: { color: muted, font: { size: 12 } } },
          y: {
            grid: { color: border },
            ticks: {
              color: muted,
              font: { size: 12 },
              stepSize: isInt ? 1 : undefined,
              precision: isInt ? 0 : undefined,
              callback: v => isInt ? v : v.toLocaleString('pl-PL') + ' zł'
            }
          }
        }
      }
    });
  }

  function buildDonut(canvasId, statusMap, colorMap, labelMap) {
    const canvas = document.getElementById(canvasId);
    if (!canvas) return;

    const statusLabels = Object.keys(statusMap);
    const statusValues = Object.values(statusMap).map(Number);

    const colors = statusLabels.map(s => colorMap[s] || muted);
    const labels = statusLabels.map(s => labelMap[s] || s);

    new Chart(canvas, {
      type: 'doughnut',
      data: {
        labels: labels.length ? labels : ['Brak danych'],
        datasets: [{
          data: statusValues.length ? statusValues : [1],
          backgroundColor: colors.length ? colors : [border],
          borderWidth: 0,
          hoverOffset: 6,
        }],
      },
      options: {
        responsive: true,
        cutout: '68%',
        plugins: {
          legend: {
            position: 'bottom',
            labels: { color: '#4a4a44', font: { size: 12 }, padding: 14, boxWidth: 10, boxHeight: 10 }
          },
          tooltip: {
            backgroundColor: '#1a1a18',
            titleColor: '#d4af5a',
            bodyColor: '#c0bdb0',
            padding: 10,
          }
        }
      }
    });
  }

  /* ───── WIDOK OGÓLNY ───── */
  buildLineChart('invoicesChart',
      data.miesiace || [],
      data.licznikFakturMiesieczny || [],
      blue, 'faktur', true);

  buildDonut('declarationsChart',
      data.statusyDeklaracji || {},
      { 'ROBOCZA': gold, 'ZATWIERDZONA': blue, 'ZLOZONA': green, 'KOREKTA': red },
      { 'ROBOCZA': 'Robocze', 'ZATWIERDZONA': 'Zatwierdzone', 'ZLOZONA': 'Złożone', 'KOREKTA': 'Korekty' });

  /* ───── WIDOK KLIENTA ───── */
  buildLineChart('revenueChart',
      data.miesiace || [],
      data.kwotyMiesieczne || [],
      gold, 'zł', false);

  buildDonut('statusChart',
      data.statusyFaktur || {},
      { 'ZAPLACONA': green, 'NIEZAPLACONA': red, 'CZESCIOWO_ZAPLACONA': gold,
        'PRZETERMINOWANA': 'rgba(192,57,43,.55)', 'ANULOWANA': muted },
      { 'ZAPLACONA': 'Zapłacone', 'NIEZAPLACONA': 'Niezapłacone', 'CZESCIOWO_ZAPLACONA': 'Częściowe',
        'PRZETERMINOWANA': 'Przeterminowane', 'ANULOWANA': 'Anulowane' });

});