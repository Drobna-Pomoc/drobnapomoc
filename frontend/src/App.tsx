import { useState } from 'react'
import { BrowserRouter, Routes, Route } from 'react-router-dom'

import './App.css'
import HomePage from './HomePage'
import RegisterPage from './RegisterPage'
import LoginPage from './LoginPage'
import Header from './Header'
import Footer from './Footer'

function App() {
    const [count, setCount] = useState(0)

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
