import { createContext, useContext, useState, useEffect } from 'react';
import type { ReactNode } from 'react';
import type { CurrentUser, LoginInput, RegisterInput } from '../api/generated/model';
import { login as apiLogin, logout as apiLogout, register as apiRegister, getCurrentUser } from '../api/generated/auth';

interface AuthContextType {
  user: CurrentUser | null;
  isLoading: boolean;
  login: (credentials: LoginInput) => Promise<void>;
  logout: () => Promise<void>;
  register: (data: RegisterInput) => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<CurrentUser | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    getCurrentUser()
      .then((response) => {
        if (response.status === 200) {
          setUser(response.data);
        }
      })
      .finally(() => setIsLoading(false));
  }, []);

  const login = async (credentials: LoginInput) => {
    const response = await apiLogin(credentials);
    if (response.status === 200) {
      setUser(response.data);
    } else {
      throw new Error(response.data.message || 'Błąd logowania');
    }
  };

  const logout = async () => {
    await apiLogout();
    setUser(null);
  };

  const register = async (data: RegisterInput) => {
    const response = await apiRegister(data);
    if (response.status === 201) {
      setUser(response.data);
    } else {
      throw new Error(response.data.message || 'Błąd rejestracji');
    }
  };

  return (
    <AuthContext.Provider value={{ user, isLoading, login, logout, register }}>
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
