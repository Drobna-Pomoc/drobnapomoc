import { render, screen, act } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, it, expect, vi } from 'vitest';
import { AuthProvider } from '../context/AuthContext';
import RegisterForm from '../components/forms/RegisterForm';

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

describe('RegisterForm', () => {
  it('renders all fields and register button', async () => {
    await act(async () => {
      render(
        <MemoryRouter>
          <AuthProvider>
            <RegisterForm />
          </AuthProvider>
        </MemoryRouter>
      );
    });

    expect(screen.getByLabelText(/e-mail/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/imię/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/nazwisko/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/nr telefonu/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/^hasło:/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/powtórz hasło/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /zarejestruj się/i })).toBeInTheDocument();
  });
});