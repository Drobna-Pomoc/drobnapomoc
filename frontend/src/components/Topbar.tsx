import './Topbar.css'
import logoImg from '../assets/logo.png'

const Topbar = () => {
  const isLoggedIn = false;
  return (
    <div className="topbar">
      <div className="topbar-left">
        <img
          src={logoImg}
          alt="Logo Drobna Pomoc"
          className="topbar-logo"
        />
      </div>
      <div className="topbar-center">
        <span className="topbar-phone-label"> Zadzwoń - pomożemy od razu</span>
        <span className="topbar-phone-number">📞 22 112 17 17</span>
      </div>
      <div className="topbar-right">
        {isLoggedIn ? (
          <button className="topbar-btn topbar-btn-panel">
            Przejdź do panelu
          </button>
        ) : (
          <>
            <button className="topbar-btn topbar-btn-login">
              Zaloguj się
            </button>
            <button className="topbar-btn topbar-btn-register">
              Zarejestruj się
            </button>
          </>
          )}
      </div>
    </div>
  )
}

export default Topbar