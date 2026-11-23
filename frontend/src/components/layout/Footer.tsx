import './Footer.css'

const Footer = () => {
  const currentYear = new Date().getFullYear();
  return (
    <footer className="footer">
      <p>
        © {currentYear} Drobna Pomoc - Bezpieczne wsparcie dla seniorów
      </p>
      <p className="footer-subtext">
        Infolinia czynna od poniedziałku do piątku w godzinach 8:00 - 18:00
      </p>
    </footer>
  )
}

export default Footer