import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import RegisterForm from '../components/forms/RegisterForm';

describe('RegisterForm', () => {
  it('renders all fields and register button', () => {
    render(<RegisterForm />);

    expect(screen.getByLabelText(/login/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/^hasło:/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/powtórz hasło/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /zarejestruj się/i })).toBeInTheDocument();
  });
});
