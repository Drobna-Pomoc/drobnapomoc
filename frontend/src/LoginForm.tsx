function LoginForm() {
    return (
        <form>
            <div>
                <label htmlFor='email'>E-mail: </label>
                <input type='text' name='email' />
            </div>
            <div>
                <label htmlFor='password'>Hasło: </label>
                <input type='text' name ='password'/>
            </div>
            <button type='submit'>Zaloguj się</button>
        </form>
    )
}

export default LoginForm