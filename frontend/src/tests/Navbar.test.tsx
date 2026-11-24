import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { describe, it, expect } from 'vitest';
import Navbar from '../components/layout/Navbar';

describe('Navbar', () => {
  it('renders all navigation links with correct paths', () => {
    render(
      <BrowserRouter>
        <Navbar />
      </BrowserRouter>
    );

    expect(screen.getByRole('link', { name: 'Jak to działa' })).toHaveAttribute('href', '/info/jak-to-dziala');
    expect(screen.getByRole('link', { name: 'Bezpieczeństwo' })).toHaveAttribute('href', '/info/bezpieczenstwo');
    expect(screen.getByRole('link', { name: 'Pomoc dla Ciebie' })).toHaveAttribute('href', '/info/pomoc-dla-ciebie');
    expect(screen.getByRole('link', { name: 'Dla bliskiej osoby' })).toHaveAttribute('href', '/info/dla-bliskiej-osoby');
    expect(screen.getByRole('link', { name: 'Dla wykonawców' })).toHaveAttribute('href', '/info/dla-wykonawcow');
    expect(screen.getByRole('link', { name: 'Zadzwoń teraz' })).toHaveAttribute('href', '/info/zadzwon-teraz');
  });
});
