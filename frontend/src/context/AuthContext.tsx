import { createContext, useContext, useState, type ReactNode } from 'react';
import type { CurrentUser } from '../types/auth';

interface AuthContextType {
  user: CurrentUser | null;
  login: () => void;
  logout: () => void;
}

  const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<CurrentUser | null>(null);

  // Na tym etapie: prosta funkcja symulująca zalogowanie użytkownika (Anna Kowalska)
  const login = () => {
    setUser({
      id: 1,
      email: 'anna.kowalska@example.com',
      firstName: 'Anna',
      lastName: 'Kowalska',
      phone: '+48123456789',
      roles: ['CLIENT'],
    });
  };

  const logout = () => {
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

// Własny hook, aby łatwo pobierać dane w komponentach: const { user, login } = useAuth();
// eslint-disable-next-line react-refresh/only-export-components
export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
