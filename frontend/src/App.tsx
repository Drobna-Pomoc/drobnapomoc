import { BrowserRouter, Routes, Route } from 'react-router-dom'

import HomePage from './pages/HomePage'
import RegisterPage from './pages/RegisterPage'
import LoginPage from './pages/LoginPage'
import Header from './components/Header'
import Footer from './components/Footer'

function App() {
    return (
        <BrowserRouter>
            <Header />
            <Routes>
                <Route
                    path='/'
                    element={<HomePage />}
                />
                <Route
                    path='/register'
                    element={<RegisterPage />}
                />
                <Route
                    path='/login'
                    element={<LoginPage />}
                />
            </Routes>
            <Footer />
        </BrowserRouter>
    )
}

export default App
