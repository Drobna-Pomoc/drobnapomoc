import { render, screen, act } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, it, expect, vi } from 'vitest';
import { AuthProvider } from '../context/AuthContext';
import LoginPage from '../pages/LoginPage';
import RegisterPage from '../pages/RegisterPage';

vi.mock('../api/generated/auth', () => ({
  login: vi.fn(),
  logout: vi.fn(),
  register: vi.fn(),
  getCurrentUser: vi.fn().mockResolvedValue({
    status: 401,
    data: { code: 'UNAUTHENTICATED', message: 'Brak sesji' },
    headers: new Headers(),
  }),
}));

describe('LoginPage and RegisterPage', () => {
  it('renders LoginPage with LoginForm and register link', async () => {
    await act(async () => {
      render(
        <MemoryRouter>
          <AuthProvider>
            <LoginPage />
          </AuthProvider>
        </MemoryRouter>
      );
    });

    expect(screen.getByLabelText(/e-mail/i)).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /zarejestruj się/i })).toBeInTheDocument();
  });

  it('renders RegisterPage with RegisterForm and login link', async () => {
    await act(async () => {
      render(
        <MemoryRouter>
          <AuthProvider>
            <RegisterPage />
          </AuthProvider>
        </MemoryRouter>
      );
    });

    expect(screen.getByLabelText(/e-mail/i)).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /zaloguj się/i })).toBeInTheDocument();
  });
});