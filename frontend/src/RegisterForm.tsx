function RegisterForm() {
  return (
    <form>
        <label htmlFor="login">Login: </label>
        <input type="text" name="login" />
        <label htmlFor="password">Hasło: </label>
        <input type="text" name ="password"/>
        <label htmlFor="confirm_password">Powtórz hasło: </label>
        <input type="text" name ="confirm_password"/>
    </form>
  )
}

export default RegisterForm