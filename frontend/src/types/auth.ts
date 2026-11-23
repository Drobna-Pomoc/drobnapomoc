export type AccountRole = 'CLIENT' | 'CAREGIVER' | 'CONTRACTOR' | 'CONSULTANT' | 'ADMIN';

export interface LoginInput {
  email: string;
  password: string;
}

export interface CurrentUser {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  phone: string;
  roles: AccountRole[];
}
