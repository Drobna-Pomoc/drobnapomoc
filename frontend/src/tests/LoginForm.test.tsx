import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import LoginForm from '../components/forms/LoginForm';
import * as AuthContext from '../context/AuthContext';

const mockNavigate = vi.fn();

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

describe('LoginForm', () => {
  const mockLogin = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
    vi.spyOn(AuthContext, 'useAuth').mockReturnValue({
      user: null,
      isLoading: false,
      login: mockLogin,
      logout: vi.fn(),
      register: vi.fn()
    });
  });

  it('renders email and password inputs and a submit button', () => {
    render(<LoginForm />);

    expect(screen.getByLabelText(/e-mail/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/hasło/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /zaloguj się/i })).toBeInTheDocument();
  });

  it('updates input values as user types', async () => {
    const user = userEvent.setup();
    render(<LoginForm />);

    const emailInput = screen.getByLabelText(/e-mail/i);
    const passwordInput = screen.getByLabelText(/hasło/i);

    await user.type(emailInput, 'test@example.com');
    await user.type(passwordInput, 'sekret123');

    expect(emailInput).toHaveValue('test@example.com');
    expect(passwordInput).toHaveValue('sekret123');
  });

  it('submits form, calls login, and navigates to /dashboard on success', async () => {
    const user = userEvent.setup();
    mockLogin.mockResolvedValueOnce(undefined);

    render(<LoginForm />);

    await user.type(screen.getByLabelText(/e-mail/i), 'anna@example.com');
    await user.type(screen.getByLabelText(/hasło/i), 'sekret123');
    await user.click(screen.getByRole('button', { name: /zaloguj się/i }));

    expect(mockLogin).toHaveBeenCalledWith({
      email: 'anna@example.com',
      password: 'sekret123',
    });
    expect(mockNavigate).toHaveBeenCalledWith('/dashboard');
  });

  it('displays error message when login rejects with Error', async () => {
    const user = userEvent.setup();
    mockLogin.mockRejectedValueOnce(new Error('Niepoprawny email lub hasło.'));

    render(<LoginForm />);

    await user.type(screen.getByLabelText(/e-mail/i), 'anna@example.com');
    await user.type(screen.getByLabelText(/hasło/i), 'wrong');
    await user.click(screen.getByRole('button', { name: /zaloguj się/i }));

    await waitFor(() => {
      expect(screen.getByText('Niepoprawny email lub hasło.')).toBeInTheDocument();
    });
    expect(mockNavigate).not.toHaveBeenCalled();
  });

  it('displays fallback error message when login rejects with non-Error', async () => {
    const user = userEvent.setup();
    mockLogin.mockRejectedValueOnce('Unexpected failure');

    render(<LoginForm />);

    await user.click(screen.getByRole('button', { name: /zaloguj się/i }));

    await waitFor(() => {
      expect(screen.getByText('Wystąpił nieoczekiwany błąd')).toBeInTheDocument();
    });
  });
});
