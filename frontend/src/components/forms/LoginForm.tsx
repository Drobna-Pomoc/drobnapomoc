import { useState } from 'react';
import type { FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import './LoginForm.css';

function LoginForm() {
  const { login } = useAuth();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    try {
      await login({ email, password });
      navigate('/dashboard');
    } catch (err) {
      if (err instanceof Error) {
        setError(err.message);
      } else {
        setError('Wystąpił nieoczekiwany błąd');
      }
    }
  };

  return (
    <form className="login-form" onSubmit={handleSubmit}>
      {error && (
        <p className="login-error" role="alert">
          {error}
        </p>
      )}
      <div className="form-group">
        <label className="form-label" htmlFor="email">
          E-mail:{' '}
        </label>
        <input
          className="form-input"
          type="email"
          name="email"
          id="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />
      </div>
      <div className="form-group">
        <label className="form-label" htmlFor="password">
          Hasło:{' '}
        </label>
        <input
          className="form-input"
          type="password"
          name="password"
          id="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />
      </div>
      <button className="login-submit-btn" type="submit">
        Zaloguj się
      </button>
    </form>
  );
}

export default LoginForm;