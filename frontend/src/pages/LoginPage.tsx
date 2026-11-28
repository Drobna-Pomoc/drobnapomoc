import { Link } from 'react-router-dom';
import LoginForm from '../components/forms/LoginForm';
import './LoginPage.css';

const LoginPage = () => {
  return (
    <div className="login-page-container">
      <div className="login-card">
        <h1 className="login-title">Zaloguj się</h1>
        <p className="login-subtitle">Wpisz swój adres e-mail i hasło, aby przejść do panelu.</p>
        <LoginForm />
        <div className="login-footer">
          <span>Nie masz jeszcze konta? </span>
          <Link to="../register" className="login-register-link">
            Zarejestruj się
          </Link>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;