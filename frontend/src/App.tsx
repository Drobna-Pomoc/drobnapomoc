import { useState } from 'react'
import { BrowserRouter, Routes, Route } from 'react-router-dom'

import './App.css'
import HomePage from './HomePage'
import RegisterPage from './RegisterPage'
import LoginPage from './LoginPage'
import Nav from './Nav'

function App() {
    const [count, setCount] = useState(0)

    return (
        <BrowserRouter>
            <Nav />
            <Routes>
                <Route
                    path="/"
                    element={<HomePage />}
                />
                <Route
                    path="/register"
                    element={<RegisterPage />}
                />
                <Route
                    path="/login"
                    element={<LoginPage />}
                />
            </Routes>
        </BrowserRouter>
    )
}

export default App
