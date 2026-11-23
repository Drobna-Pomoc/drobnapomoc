import { createContext, useContext, useState, type ReactNode } from 'react';
import type { CurrentUser, LoginInput } from '../api/generated/model';
import { login as apiLogin } from '../api/generated/auth';

interface AuthContextType {
  user: CurrentUser | null;
  login: (credentials: LoginInput) => Promise<void>;
  logout: () => void;
}

  const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<CurrentUser | null>(null);

  const login = async (credentials: LoginInput) => {
    const response = await apiLogin(credentials);

    if (response.status === 200) {
      setUser(response.data);
    } else {
      throw new Error(response.data.message || 'Błąd logowania');
    }
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

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
