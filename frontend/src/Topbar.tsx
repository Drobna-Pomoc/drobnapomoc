import { Link } from "react-router-dom"
import Advertisement from "./Advertisement"

const Topbar = () => {
  return (
    <div>
        <Link to='/' >HomePage</Link>
        <Advertisement />
        <Link to ='login'>Login</Link>
        <Link to='register'>Register</Link>
    </div>
  )
}

export default Topbar