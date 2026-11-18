import { BrowserRouter, Routes, Route } from 'react-router-dom'

import Footer from './components/Footer'
import Header from './components/Header'

function App() {
    return (
        <div className="app-container">
            <Header />
            <main className="main-content">
                <p>Tu pojawi się treść strony głównej</p>
            </main>
            <Footer />
        </div>
    )
}

export default App
