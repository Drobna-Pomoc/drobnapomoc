import { useState } from 'react';
import type { FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

function RegisterForm() {
    const { register } = useAuth();
    const [email, setEmail] = useState('');
    const [firstName, setFirstName] = useState('');
    const [lastName, setLastName] = useState('');
    const [phone, setPhone] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState<string | null>(null);
    const navigate = useNavigate();

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault();
        setError(null);
        try {
            if (!firstName) {
                setError('Podaj imię');
                return;
            }
            if (password.length < 8) {
                setError('Hasło musi mieć przynajmniej 8 znaków');
                return;
            }
            if (password !== confirmPassword) {
                setError('Hasła nie są zgodne');
                return;
            }
            await register({email, password, firstName, lastName, phone});
            navigate('/dashboard');
        } catch (err) {
            if (err instanceof Error) {
                setError(err.message);
            } else {
                setError('Wystąpił nieoczekiwany błąd podczas rejestracji');
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
                <label htmlFor='firstName'>Imię: </label>
                <input type='text' name='firstName' id='firstName' value={firstName} onChange={(e) => setFirstName(e.target.value)}/>
            </div>
            <div>
                <label htmlFor='lastName'>Nazwisko: </label>
                <input type='text' name='lastName' id='lastName' value={lastName} onChange={(e) => setLastName(e.target.value)}/>
            </div>
            <div>
                <label htmlFor='phone'>Nr telefonu: </label>
                <input type='text' name='phone' id='phone' value={phone} onChange={(e) => setPhone(e.target.value)}/>
            </div>
            <div>
                <label htmlFor='password'>Hasło: </label>
                <input type='password' name='password' id='password' value={password} onChange={(e) => setPassword(e.target.value)}/>
            </div>
            <div>
                <label htmlFor='confirmPassword'>Powtórz hasło: </label>
                <input type='password' name='confirmPassword' id='confirmPassword' value={confirmPassword} onChange={(e) => setConfirmPassword(e.target.value)}/>
            </div>
            <button type='submit'>Zarejestruj się</button>
        </form>
    );
}

export default RegisterForm;