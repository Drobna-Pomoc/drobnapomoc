function RegisterForm() {
  return (
    <form>
        <div>
            <label htmlFor='login'>Login: </label>
            <input type='text' name='login' id='login' />
        </div>
        <div>
            <label htmlFor='password'>Hasło: </label>
            <input type='text' name='password' id='password' />
        </div>
        <div>
            <label htmlFor='confirm_password'>Powtórz hasło: </label>
            <input type='text' name='confirm_password' id='confirm_password' />
        </div>
        <button type='submit'>Zarejestruj się</button>
    </form>
  )
}

export default RegisterForm