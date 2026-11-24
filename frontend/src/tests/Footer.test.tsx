import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import Footer from '../components/layout/Footer';

describe('Footer', () => {
  it('renders copyright with current year and opening hours', () => {
    render(<Footer />);
    const year = new Date().getFullYear();

    expect(screen.getByText(new RegExp(`© ${year} Drobna Pomoc`, 'i'))).toBeInTheDocument();
    expect(screen.getByText(/infolinia czynna/i)).toBeInTheDocument();
  });
});
