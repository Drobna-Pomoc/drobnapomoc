import { BrowserRouter, Routes, Route } from 'react-router-dom'

import Footer from './components/Footer'

function App() {
    return (
        <div className="app-container">
            <main className="main-content">
                <p>Tu pojawi się treść strony głównej</p>
            </main>
            <Footer />
        </div>
    )
}

export default App
