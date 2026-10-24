import { Link } from "react-router-dom"
import RegisterForm from "./RegisterForm"

const RegisterPage = (() => {
    return (
        <div>
            <RegisterForm />
            <Link to="../login">Zaloguj się</Link>
        </div>
    )
})

export default RegisterPage