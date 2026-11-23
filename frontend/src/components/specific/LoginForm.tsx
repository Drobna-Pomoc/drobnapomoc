import { useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

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
        <form onSubmit={handleSubmit}>
            {error && <p style={{ color: 'red' }}>{error}</p>}
            <div>
                <label htmlFor='email'>E-mail: </label>
                <input type='text' name='email' id='email' value={email} onChange={(e) => setEmail(e.target.value)}/>
            </div>
            <div>
                <label htmlFor='password'>Hasło: </label>
                <input type='password' name='password' id='password' value={password} onChange={(e) => setPassword(e.target.value)}/>
            </div>
            <button type='submit'>Zaloguj się</button>
        </form>
    );
}

export default LoginForm;