import type { FormEvent } from 'react';
import { useAuth } from '../../context/AuthContext';

function LoginForm() {
    const { login } = useAuth();

    const handleSubmit = (e: FormEvent) => {
        e.preventDefault();
        login();
    };

    return (
        <form onSubmit={handleSubmit}>
            <div>
                <label htmlFor='email'>E-mail: </label>
                <input type='text' name='email' id='email' />
            </div>
            <div>
                <label htmlFor='password'>Hasło: </label>
                <input type='password' name='password' id='password' />
            </div>
            <button type='submit'>Zaloguj się</button>
        </form>
    );
}

export default LoginForm;