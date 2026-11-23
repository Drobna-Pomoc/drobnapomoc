import { Link } from 'react-router-dom'
import RegisterForm from '../components/forms/RegisterForm'

const RegisterPage = (() => {
    return (
        <div style={{ padding: '40px', textAlign: 'center'}}>
            <RegisterForm />
            <Link to='../login'>Zaloguj się</Link>
        </div>
    )
})

export default RegisterPage