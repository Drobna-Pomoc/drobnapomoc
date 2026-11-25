import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import Topbar from '../components/layout/Topbar';
import * as AuthContext from '../context/AuthContext';
import type { CurrentUser } from '../api/generated/model';

describe('Topbar', () => {
  const mockLogout = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders phone number and login/register buttons when user is guest', () => {
    vi.spyOn(AuthContext, 'useAuth').mockReturnValue({
      user: null,
      isLoading: false,
      login: vi.fn(),
      logout: mockLogout,
      register: vi.fn()
    });

    render(
      <BrowserRouter>
        <Topbar />
      </BrowserRouter>
    );

    expect(screen.getByText(/22 112 17 17/)).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /zaloguj się/i })).toHaveAttribute('href', '/login');
    expect(screen.getByRole('link', { name: /zarejestruj się/i })).toHaveAttribute('href', '/register');
    expect(screen.queryByRole('button', { name: /wyloguj się/i })).not.toBeInTheDocument();
  });

  it('renders panel link and logout button when user is logged in', async () => {
    const user = userEvent.setup();
    const mockUser: CurrentUser = {
      id: 1,
      email: 'anna@example.com',
      firstName: 'Anna',
      lastName: 'Kowalska',
      phone: '+48123456789',
      roles: ['CLIENT'],
    };

    vi.spyOn(AuthContext, 'useAuth').mockReturnValue({
      user: mockUser,
      isLoading: false,
      login: vi.fn(),
      logout: mockLogout,
      register: vi.fn()
    });

    render(
      <BrowserRouter>
        <Topbar />
      </BrowserRouter>
    );

    expect(screen.getByRole('link', { name: /przejdź do panelu/i })).toHaveAttribute('href', '/dashboard');
    const logoutBtn = screen.getByRole('button', { name: /wyloguj się/i });
    expect(logoutBtn).toBeInTheDocument();

    await user.click(logoutBtn);
    expect(mockLogout).toHaveBeenCalledTimes(1);
  });
});
