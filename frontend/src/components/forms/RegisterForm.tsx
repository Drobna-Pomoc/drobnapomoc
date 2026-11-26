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

    const fields = [
        { id: 'email', label: 'E-mail: ', type: 'email', value: email, setter: setEmail },
        { id: 'firstName', label: 'Imię: ', type: 'text', value: firstName, setter: setFirstName },
        { id: 'lastName', label: 'Nazwisko: ', type: 'text', value: lastName, setter: setLastName },
        { id: 'phone', label: 'Nr telefonu: ', type: 'tel', value: phone, setter: setPhone },
        { id: 'password', label: 'Hasło: ', type: 'password', value: password, setter: setPassword },
        { id: 'confirmPassword', label: 'Powtórz hasło: ', type: 'password', value: confirmPassword, setter: setConfirmPassword },
    ];

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
            {fields.map(({ id, label, type, value, setter }) => (
                <div key={id}>
                    <label htmlFor={id}>{label}</label>
                    <input
                    type={type}
                    name={id}
                    id={id}
                    value={value}
                    onChange={(e) => setter(e.target.value)}
                    />
                </div>
                ))}
            <button type='submit'>Zarejestruj się</button>
        </form>
    );
}

export default RegisterForm;