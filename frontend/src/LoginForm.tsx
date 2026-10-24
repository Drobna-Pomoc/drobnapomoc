function LoginForm() {
  return (
    <form>
        <label htmlFor="login">Login: </label>
        <input type="text" name="login" />
        <label htmlFor="password">Hasło: </label>
        <input type="text" name ="password"/>
    </form>
  )
}

export default LoginForm