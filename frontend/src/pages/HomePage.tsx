import './HomePage.css'
import seniorImg from '../assets/senior.jpg'

const HomePage = (() => {
    const handleCallClick = () => {
        console.log("Kliknięto przycisk Zadzwoń teraz");
    }
    return (
        <div className="home-container">
            <section className="hero-header">
                <h1 className="hero-title">
                    Pomoc dla seniorów, która ułatwia codzienne życie
                </h1>
                <p className="hero-subtitle">
                    Zakupy, drobne naprawy i codzienne wsparcie dla seniora lub bliskiej osoby.
                </p>
                <p className="hero-subtitle">
                    To daje spokój - Tobie i Twoim bliskim.
                </p>
            </section>
            <section className="hero-body">
                <div className="hero-left">
                    <p className="hero-phone-call">
                        Masz pełną kontrolę - wszystko ustalamy przez telefon
                    </p>
                    <div className="hero-phone-big">
                        📞 22 112 17 17
                    </div>
                    <button
                        type="button"
                        className="hero-cta-btn"
                        onClick={handleCallClick}
                    >
                        Zadzwoń teraz
                    </button>
                </div>
                <div className="hero-right">
                    <img 
                        src={seniorImg}
                        alt="Uśmiechnięta seniorka trzymająca telefon w dłoni"
                        className="hero-image"
                    />
                </div>
            </section>
        </div>
    )
})

export default HomePage