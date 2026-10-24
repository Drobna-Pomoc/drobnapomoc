import { Link } from "react-router-dom"
import LoginForm from "./LoginForm"

const LoginPage = (() => {
    return (
        <div>
            <LoginForm />
            <Link to="../register">Zarejestruj się</Link>
        </div>
    )
})

export default LoginPage