import { Link } from "react-router-dom"

const Topbar = () => {
  return (
    <div>
        <Link to='/' >HomePage</Link>
        <Link to ='login'>Login</Link>
        <Link to='register'>Register</Link>
    </div>
  )
}

export default Topbar