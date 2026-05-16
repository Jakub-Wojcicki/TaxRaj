document.addEventListener('DOMContentLoaded', function () {

  const emailInput    = document.getElementById('email');
  const passwordInput = document.getElementById('password');
  const loginBtn      = document.getElementById('loginBtn');
  const errorMsg      = document.getElementById('errorMsg');
  const errorText     = document.getElementById('errorText');
  const togglePw      = document.getElementById('togglePw');
  const eyeIcon       = document.getElementById('eyeIcon');

  /* Demo credentials — zastąp Spring Security */
  const USERS = [
    { email: 'admin@tax-raj.pl',    password: 'admin123',    rola: 'Administrator' },
    { email: 'ksiegowy@tax-raj.pl', password: 'ksiegowy123', rola: 'Księgowy'      },
  ];

  /* ── TOGGLE PASSWORD ── */
  let pwVisible = false;

  togglePw.addEventListener('click', () => {
    pwVisible = !pwVisible;
    passwordInput.type = pwVisible ? 'text' : 'password';
    eyeIcon.innerHTML  = pwVisible
      ? `<path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8
           a18.45 18.45 0 0 1 5.06-5.94"/>
         <path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8
           a18.5 18.5 0 0 1-2.16 3.19"/>
         <line x1="1" y1="1" x2="23" y2="23"/>`
      : `<path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
         <circle cx="12" cy="12" r="3"/>`;
  });

  /* ── LOGIN ── */
  loginBtn.addEventListener('click', doLogin);
  [emailInput, passwordInput].forEach(el =>
    el.addEventListener('keydown', e => e.key === 'Enter' && doLogin())
  );

  function doLogin() {
    errorMsg.classList.remove('show');

    const email = emailInput.value.trim();
    const pass  = passwordInput.value;

    if (!email || !pass) {
      showError('Wypełnij wszystkie pola.');
      return;
    }

    loginBtn.classList.add('loading');

    setTimeout(() => {
      loginBtn.classList.remove('loading');

      const user = USERS.find(u => u.email === email && u.password === pass);

      if (user) {
        loginBtn.style.background = '#2d7a4f';
        loginBtn.querySelector('.btn-text').textContent = `Witaj, ${user.rola}!`;
        /* TODO: window.location.href = '/dashboard'; */
        setTimeout(() => {
          loginBtn.querySelector('.btn-text').textContent = 'Zaloguj się';
          loginBtn.style.background = '';
        }, 2000);
      } else {
        showError('Nieprawidłowy adres e-mail lub hasło.');
        passwordInput.value = '';
        passwordInput.focus();
      }
    }, 900);
  }

  function showError(msg) {
    errorText.textContent = msg;
    errorMsg.classList.add('show');
  }

});
