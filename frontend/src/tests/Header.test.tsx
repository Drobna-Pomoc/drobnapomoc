import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { describe, it, expect, vi } from 'vitest';
import Header from '../components/layout/Header';
import * as AuthContext from '../context/AuthContext';

describe('Header', () => {
  it('renders header with topbar and navbar', () => {
    vi.spyOn(AuthContext, 'useAuth').mockReturnValue({
      user: null,
      login: vi.fn(),
      logout: vi.fn(),
    });

    render(
      <BrowserRouter>
        <Header />
      </BrowserRouter>
    );

    expect(screen.getByRole('banner')).toBeInTheDocument();
    expect(screen.getByText(/22 112 17 17/)).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Jak to działa' })).toBeInTheDocument();
  });
});
