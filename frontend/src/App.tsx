import { BrowserRouter, Routes, Route } from 'react-router-dom'

import Footer from './components/layout/Footer'
import Header from './components/layout/Header'
import HomePage from './pages/HomePage'
import InfoPage from './pages/InfoPage'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'

function App() {
    return (
        <BrowserRouter>
            <div className="app-container">
                <Header />
                <main className="main-content">
                    <Routes>
                        <Route path="/" element={<HomePage />} />
                        <Route path="/info/:slug" element={<InfoPage />} />
                        <Route path="/login" element={<LoginPage />} />
                        <Route path="/register" element={<RegisterPage />} />
                    </Routes>
                </main>
                <Footer />
            </div>
        </BrowserRouter>
    )
}

export default App
