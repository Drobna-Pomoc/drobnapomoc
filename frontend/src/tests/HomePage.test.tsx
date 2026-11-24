import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, it, expect, vi } from 'vitest';
import HomePage from '../pages/HomePage';

describe('HomePage', () => {
  it('renders hero titles, phone number and call button', async () => {
    const consoleSpy = vi.spyOn(console, 'log').mockImplementation(() => {});
    const user = userEvent.setup();

    render(<HomePage />);

    expect(screen.getByRole('heading', { level: 1 })).toHaveTextContent(
      'Pomoc dla seniorów, która ułatwia codzienne życie'
    );
    expect(screen.getByText(/22 112 17 17/)).toBeInTheDocument();

    const callButton = screen.getByRole('button', { name: /zadzwoń teraz/i });
    expect(callButton).toBeInTheDocument();

    await user.click(callButton);
    expect(consoleSpy).toHaveBeenCalledWith('Kliknięto przycisk Zadzwoń teraz');

    consoleSpy.mockRestore();
  });
});
