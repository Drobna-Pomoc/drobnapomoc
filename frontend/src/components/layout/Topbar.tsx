import './Topbar.css'
import logoImg from '../../assets/logo.png'
import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

const Topbar = () => {
  const { user, logout } = useAuth();

  return (
    <div className="topbar">
      <div className="topbar-left">
        <Link to="/">
          <img
            src={logoImg}
            alt="Logo Drobna Pomoc"
            className="topbar-logo"
          />
        </Link>
      </div>
      <div className="topbar-center">
        <span className="topbar-phone-label"> Zadzwoń - pomożemy od razu</span>
        <span className="topbar-phone-number">📞 22 112 17 17</span>
      </div>
      <div className="topbar-right">
        {user ? (
          <>
            {/* <span className="topbar-user-greeting">Witaj, {user.firstName}!</span> */}
            <Link to="/dashboard" className="topbar-btn topbar-btn-panel">
              Przejdź do panelu
            </Link>
            <button onClick={logout} className="topbar-btn topbar-btn-login">
              Wyloguj się
            </button>
          </>
        ) : (
          <>
            <Link to="/login" className="topbar-btn topbar-btn-login">
              Zaloguj się
            </Link>
            <Link to="/register" className="topbar-btn topbar-btn-register">
              Zarejestruj się
            </Link>
          </>
        )}
      </div>
    </div>
  )
}

export default Topbar