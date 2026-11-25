import { render, screen, act } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import App from '../App';

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

describe('App', () => {
  it('renders header, main content, and footer', async () => {
    await act(async () => {
      render(<App />);
    });

    expect(screen.getByRole('banner')).toBeInTheDocument();
    expect(screen.getByRole('main')).toBeInTheDocument();
    expect(screen.getByRole('contentinfo')).toBeInTheDocument();
  });
});