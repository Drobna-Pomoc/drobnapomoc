import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { describe, it, expect } from 'vitest';
import InfoPage from '../pages/InfoPage';

describe('InfoPage', () => {
  it('renders info section with slug param', () => {
    render(
      <MemoryRouter initialEntries={['/info/jak-to-dziala']}>
        <Routes>
          <Route path="/info/:slug" element={<InfoPage />} />
        </Routes>
      </MemoryRouter>
    );

    expect(screen.getByRole('heading', { name: 'Podstrona informacyjna' })).toBeInTheDocument();
    expect(screen.getByText('Informacje o sekcji: jak-to-dziala')).toBeInTheDocument();
  });
});
