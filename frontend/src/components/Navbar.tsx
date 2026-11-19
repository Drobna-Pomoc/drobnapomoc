import { NavLink } from 'react-router-dom'
import './Navbar.css'

const navItems = [
    {label: 'Jak to działa', path: '/info/jak-to-dziala'},
    {label: 'Bezpieczeństwo', path: '/info/bezpieczenstwo'},
    {label: 'Pomoc dla Ciebie', path: '/info/pomoc-dla-ciebie'},
    {label: 'Dla bliskiej osoby', path: '/info/dla-bliskiej-osoby'},
    {label: 'Dla wykonawców', path: '/info/dla-wykonawcow'},
    {label: 'Zadzwoń teraz', path: '/info/zadzwon-teraz'}
]

const Navbar = (() => {
  return (
    <nav className="navbar">
        {navItems.map((item) => (
            <NavLink
                key={item.path}
                to={item.path}
                className="navbar-link"
            >
                {item.label}
            </NavLink>
        ))}
    </nav>
    )
})

export default Navbar