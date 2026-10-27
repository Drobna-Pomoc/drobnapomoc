function RegisterForm() {
  return (
    <form>
        <div>
            <label htmlFor='login'>Login: </label>
            <input type='text' name='login' />
        </div>
        <div>
            <label htmlFor='password'>Hasło: </label>
            <input type='text' name ='password'/>
        </div>
        <div>
            <label htmlFor='confirm_password'>Powtórz hasło: </label>
            <input type='text' name ='confirm_password'/>
        </div>
        <button type='submit'>Zarejestruj się</button>
    </form>
  )
}

export default RegisterForm