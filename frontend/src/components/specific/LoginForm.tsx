import { useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

function LoginForm() {
    const { login } = useAuth();
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const navigate = useNavigate();

    const handleSubmit = (e: FormEvent) => {
        e.preventDefault();
        login({ email, password});
        navigate('/');
    };

    return (
        <form onSubmit={handleSubmit}>
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