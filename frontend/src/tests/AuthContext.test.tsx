import { renderHook, act, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { AuthProvider, useAuth } from '../context/AuthContext';
import * as authApi from '../api/generated/auth';
import type { CurrentUser } from '../api/generated/model';

vi.mock('../api/generated/auth', () => ({
  login: vi.fn(),
  logout: vi.fn().mockResolvedValue({ status: 204, data: {}, headers: new Headers() }),
  register: vi.fn(),
  getCurrentUser: vi.fn().mockResolvedValue({
    status: 401,
    data: { code: 'UNAUTHENTICATED', message: 'Brak sesji' },
    headers: new Headers(),
  }),
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

  const setupAuth = () => renderHook(() => useAuth(), { wrapper: AuthProvider });

  const performSuccessfulLogin = async (result: ReturnType<typeof setupAuth>['result']) => {
    vi.mocked(authApi.login).mockResolvedValueOnce({
      status: 200,
      data: mockUser,
      headers: new Headers(),
    });

    await act(async () => {
      await result.current.login({
        email: 'anna.kowalska@example.com',
        password: 'sekret123',
      });
    });
  };

  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(authApi.getCurrentUser).mockResolvedValue({
      status: 401,
      data: { code: 'UNAUTHENTICATED', message: 'Brak sesji' },
      headers: new Headers(),
    });
  });

  it('throws an error if useAuth is used outside AuthProvider', () => {
    expect(() => renderHook(() => useAuth())).toThrow(
      'useAuth must be used within an AuthProvider'
    );
  });

  it('provides null user initially when session is unauthenticated', async () => {
    const { result } = setupAuth();
    await waitFor(() => expect(result.current.isLoading).toBe(false));
    expect(result.current.user).toBeNull();
  });

  it('restores user session on mount when getCurrentUser returns 200', async () => {
    vi.mocked(authApi.getCurrentUser).mockResolvedValueOnce({
      status: 200,
      data: mockUser,
      headers: new Headers(),
    });

    const { result } = setupAuth();
    await waitFor(() => expect(result.current.isLoading).toBe(false));
    expect(result.current.user).toEqual(mockUser);
  });

  it('successfully logs in a user when API returns 200', async () => {
    const { result } = setupAuth();
    await waitFor(() => expect(result.current.isLoading).toBe(false));
    await performSuccessfulLogin(result);

    expect(result.current.user).toEqual(mockUser);
  });

  it('successfully registers a user when API returns 201', async () => {
    vi.mocked(authApi.register).mockResolvedValueOnce({
      status: 201,
      data: mockUser,
      headers: new Headers(),
    });

    const { result } = setupAuth();
    await waitFor(() => expect(result.current.isLoading).toBe(false));

    await act(async () => {
      await result.current.register({
        email: 'anna.kowalska@example.com',
        password: 'sekret123',
        firstName: 'Anna',
        lastName: 'Kowalska',
        phone: '+48123456789',
      });
    });

    expect(result.current.user).toEqual(mockUser);
  });

  it('throws an error when register returns non-201 status', async () => {
    vi.mocked(authApi.register).mockResolvedValueOnce({
      status: 409,
      data: {
        code: 'EMAIL_TAKEN',
        message: 'Konto z tym adresem email juz istnieje.',
      },
      headers: new Headers(),
    });

    const { result } = setupAuth();
    await waitFor(() => expect(result.current.isLoading).toBe(false));

    await expect(
      result.current.register({
        email: 'anna.kowalska@example.com',
        password: 'sekret123',
        firstName: 'Anna',
        lastName: 'Kowalska',
        phone: '+48123456789',
      })
    ).rejects.toThrow('Konto z tym adresem email juz istnieje.');

    expect(result.current.user).toBeNull();
  });

  it('throws a default error message when register returns non-201 without message', async () => {
    vi.mocked(authApi.register).mockResolvedValueOnce({
      status: 400,
      data: {} as never,
      headers: new Headers(),
    });

    const { result } = setupAuth();
    await waitFor(() => expect(result.current.isLoading).toBe(false));

    await expect(
      result.current.register({
        email: 'test@example.com',
        password: 'sekret123',
        firstName: 'Jan',
        lastName: 'Test',
        phone: '+48123456789',
      })
    ).rejects.toThrow('Błąd rejestracji');

    expect(result.current.user).toBeNull();
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

    const { result } = setupAuth();
    await waitFor(() => expect(result.current.isLoading).toBe(false));

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

    const { result } = setupAuth();
    await waitFor(() => expect(result.current.isLoading).toBe(false));

    await expect(
      result.current.login({
        email: 'test@example.com',
        password: '123',
      })
    ).rejects.toThrow('Błąd logowania');

    expect(result.current.user).toBeNull();
  });

  it('logs out a user and resets user to null', async () => {
    const { result } = setupAuth();
    await waitFor(() => expect(result.current.isLoading).toBe(false));
    await performSuccessfulLogin(result);
    expect(result.current.user).toEqual(mockUser);

    await act(async () => {
      await result.current.logout();
    });

    expect(result.current.user).toBeNull();
  });
});