import { renderHook, act } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { AuthProvider, useAuth } from '../context/AuthContext';
import * as authApi from '../api/generated/auth';
import type { CurrentUser } from '../api/generated/model';

vi.mock('../api/generated/auth', () => ({
  login: vi.fn(),
}));

describe('AuthContext', () => {
  const mockUser: CurrentUser = {
    id: 1,
    email: 'anna.kowalska@example.com',
    firstName: 'Anna',
    lastName: 'Kowalska',
    phone: '+48123456789',
    roles: ['CLIENT'],
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('throws an error if useAuth is used outside AuthProvider', () => {
    expect(() => renderHook(() => useAuth())).toThrow(
      'useAuth must be used within an AuthProvider'
    );
  });

  it('provides null user initially', () => {
    const { result } = renderHook(() => useAuth(), { wrapper: AuthProvider });
    expect(result.current.user).toBeNull();
  });

  it('successfully logs in a user when API returns 200', async () => {
    vi.mocked(authApi.login).mockResolvedValueOnce({
      status: 200,
      data: mockUser,
      headers: new Headers(),
    });

    const { result } = renderHook(() => useAuth(), { wrapper: AuthProvider });

    await act(async () => {
      await result.current.login({
        email: 'anna.kowalska@example.com',
        password: 'sekret123',
      });
    });

    expect(result.current.user).toEqual(mockUser);
  });

  it('throws an error when API returns non-200 status', async () => {
    vi.mocked(authApi.login).mockResolvedValueOnce({
      status: 401,
      data: {
        code: 'INVALID_CREDENTIALS',
        message: 'Niepoprawny email lub hasło.',
      },
      headers: new Headers(),
    });

    const { result } = renderHook(() => useAuth(), { wrapper: AuthProvider });

    await expect(
      result.current.login({
        email: 'anna.kowalska@example.com',
        password: 'wrong',
      })
    ).rejects.toThrow('Niepoprawny email lub hasło.');

    expect(result.current.user).toBeNull();
  });

  it('throws a default error message when API returns non-200 without message', async () => {
    vi.mocked(authApi.login).mockResolvedValueOnce({
      status: 400,
      data: {} as never,
      headers: new Headers(),
    });

    const { result } = renderHook(() => useAuth(), { wrapper: AuthProvider });

    await expect(
      result.current.login({
        email: 'test@example.com',
        password: '123',
      })
    ).rejects.toThrow('Błąd logowania');

    expect(result.current.user).toBeNull();
  });

  it('logs out a user and resets user to null', async () => {
    vi.mocked(authApi.login).mockResolvedValueOnce({
      status: 200,
      data: mockUser,
      headers: new Headers(),
    });

    const { result } = renderHook(() => useAuth(), { wrapper: AuthProvider });

    await act(async () => {
      await result.current.login({
        email: 'anna.kowalska@example.com',
        password: 'sekret123',
      });
    });

    expect(result.current.user).toEqual(mockUser);

    act(() => {
      result.current.logout();
    });

    expect(result.current.user).toBeNull();
  });
});
