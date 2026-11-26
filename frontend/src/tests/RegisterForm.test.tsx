import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import RegisterForm from '../components/forms/RegisterForm';
import * as AuthContext from '../context/AuthContext';

const mockNavigate = vi.fn();

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

describe('RegisterForm', () => {
  const mockRegister = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
    vi.spyOn(AuthContext, 'useAuth').mockReturnValue({
      user: null,
      isLoading: false,
      login: vi.fn(),
      logout: vi.fn(),
      register: mockRegister,
    });
  });

  it('renders all fields and register button', () => {
    render(<RegisterForm />);

    expect(screen.getByLabelText(/e-mail/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/imię/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/nazwisko/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/nr telefonu/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/^hasło:/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/powtórz hasło/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /zarejestruj się/i })).toBeInTheDocument();
  });

  it('shows validation error when firstName is empty', async () => {
    const user = userEvent.setup();
    render(<RegisterForm />);

    await user.click(screen.getByRole('button', { name: /zarejestruj się/i }));

    expect(screen.getByText('Podaj imię')).toBeInTheDocument();
    expect(mockRegister).not.toHaveBeenCalled();
  });

  it('shows validation error when password is shorter than 8 characters', async () => {
    const user = userEvent.setup();
    render(<RegisterForm />);

    await user.type(screen.getByLabelText(/imię/i), 'Anna');
    await user.type(screen.getByLabelText(/^hasło:/i), '1234567');
    await user.click(screen.getByRole('button', { name: /zarejestruj się/i }));

    expect(screen.getByText('Hasło musi mieć przynajmniej 8 znaków')).toBeInTheDocument();
    expect(mockRegister).not.toHaveBeenCalled();
  });

  it('shows validation error when passwords do not match', async () => {
    const user = userEvent.setup();
    render(<RegisterForm />);

    await user.type(screen.getByLabelText(/imię/i), 'Anna');
    await user.type(screen.getByLabelText(/^hasło:/i), 'sekret123');
    await user.type(screen.getByLabelText(/powtórz hasło/i), 'innehaslo123');
    await user.click(screen.getByRole('button', { name: /zarejestruj się/i }));

    expect(screen.getByText('Hasła nie są zgodne')).toBeInTheDocument();
    expect(mockRegister).not.toHaveBeenCalled();
  });

  it('submits form, calls register, and navigates to /dashboard on success', async () => {
    const user = userEvent.setup();
    mockRegister.mockResolvedValueOnce(undefined);

    render(<RegisterForm />);

    await user.type(screen.getByLabelText(/e-mail/i), 'anna@example.com');
    await user.type(screen.getByLabelText(/imię/i), 'Anna');
    await user.type(screen.getByLabelText(/nazwisko/i), 'Kowalska');
    await user.type(screen.getByLabelText(/nr telefonu/i), '+48123456789');
    await user.type(screen.getByLabelText(/^hasło:/i), 'sekret123');
    await user.type(screen.getByLabelText(/powtórz hasło/i), 'sekret123');
    await user.click(screen.getByRole('button', { name: /zarejestruj się/i }));

    expect(mockRegister).toHaveBeenCalledWith({
      email: 'anna@example.com',
      firstName: 'Anna',
      lastName: 'Kowalska',
      phone: '+48123456789',
      password: 'sekret123',
    });
    expect(mockNavigate).toHaveBeenCalledWith('/dashboard');
  });

  it('displays error message when register rejects with Error', async () => {
    const user = userEvent.setup();
    mockRegister.mockRejectedValueOnce(new Error('Konto z tym adresem email juz istnieje.'));

    render(<RegisterForm />);

    await user.type(screen.getByLabelText(/e-mail/i), 'anna@example.com');
    await user.type(screen.getByLabelText(/imię/i), 'Anna');
    await user.type(screen.getByLabelText(/^hasło:/i), 'sekret123');
    await user.type(screen.getByLabelText(/powtórz hasło/i), 'sekret123');
    await user.click(screen.getByRole('button', { name: /zarejestruj się/i }));

    await waitFor(() => {
      expect(screen.getByText('Konto z tym adresem email juz istnieje.')).toBeInTheDocument();
    });
    expect(mockNavigate).not.toHaveBeenCalled();
  });

  it('displays fallback error message when register rejects with non-Error', async () => {
    const user = userEvent.setup();
    mockRegister.mockRejectedValueOnce('Unexpected failure');

    render(<RegisterForm />);

    await user.type(screen.getByLabelText(/imię/i), 'Anna');
    await user.type(screen.getByLabelText(/^hasło:/i), 'sekret123');
    await user.type(screen.getByLabelText(/powtórz hasło/i), 'sekret123');
    await user.click(screen.getByRole('button', { name: /zarejestruj się/i }));

    await waitFor(() => {
      expect(screen.getByText('Wystąpił nieoczekiwany błąd podczas rejestracji')).toBeInTheDocument();
    });
  });
});