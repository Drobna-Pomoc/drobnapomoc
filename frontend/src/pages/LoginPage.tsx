import { Link } from 'react-router-dom'
import LoginForm from '../components/forms/LoginForm'

const LoginPage = (() => {
    return (
        <div style={{ padding: '40px', textAlign: 'center'}}>
            <LoginForm />
            <Link to='../register'>Zarejestruj się</Link>
        </div>
    )
})

export default LoginPage