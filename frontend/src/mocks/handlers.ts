import { http, HttpResponse } from 'msw';
import type { CurrentUser, LoginInput, RegisterInput } from '../api/generated/model';

const SESSION_KEY = 'activeUser';

const mockUsers: CurrentUser[] = [
  {id: 1, email: 'anna.kowalska@example.com', firstName: 'Anna', lastName: 'Kowalska', phone: '+48123456789', roles: ['CLIENT']},
  {id: 2, email: 'jan.kowalski@example.com', firstName: 'Jan', lastName: 'Kowalski', phone: '+48123456789', roles: ['CLIENT', 'CONTRACTOR']},
  {id: 3, email: 'rafal.kowalenko@example.com', firstName: 'Rafal', lastName: 'Kowalenko', phone: '+48123456789', roles: ['ADMIN']}
]

function getSessionUser(): CurrentUser | null {
  const raw = sessionStorage.getItem(SESSION_KEY);
  if (!raw) {
    return null;
  }
  return JSON.parse(raw) as CurrentUser;
}

export const handlers = [
  http.get('*/v1/auth/me', () => {
    const user = getSessionUser();
    if (!user) {
      return HttpResponse.json(
        { code: 'UNAUTHENTICATED', message: 'Wymagane zalogowanie.' },
        { status: 401 },
      );
    }
    return HttpResponse.json(user, { status: 200 });
  }),

  http.post('*/v1/auth/login', async ({ request }) => {
    const credentials = (await request.json()) as LoginInput;
    const foundUser = mockUsers.find(
      (item) => item.email.toLowerCase() === credentials.email.toLowerCase()
    );
    if (foundUser && credentials.password === 'sekret123') {
      sessionStorage.setItem(SESSION_KEY, JSON.stringify(foundUser));
      return HttpResponse.json(foundUser, { status: 200 });
    }

    return HttpResponse.json(
      { code: 'INVALID_CREDENTIALS', message: 'Niepoprawny email lub hasło.' },
      { status: 401 },
    );
  }),

  http.post('*/v1/auth/logout', () => {
    sessionStorage.removeItem(SESSION_KEY);
    return new HttpResponse(null, { status: 204 });
  }),

  http.post('*/v1/auth/register', async ({ request }) => {
    const body = (await request.json()) as RegisterInput;

    const emailTaken = mockUsers.some(
      (item) => item.email.toLowerCase() === body.email.toLowerCase()
    );
    if (emailTaken) {
      return HttpResponse.json(
      { code: 'EMAIL_TAKEN', message: 'Konto z tym adresem email juz istnieje.' },
      { status: 409 },
    );
    }

    const registeredUser: CurrentUser = {
      id: 12,
      email: body.email,
      firstName: body.firstName,
      lastName: body.lastName,
      phone: body.phone,
      roles: ['CLIENT'],
    };

    sessionStorage.setItem(SESSION_KEY, JSON.stringify(registeredUser));
    return HttpResponse.json(registeredUser, { status: 201 });
  }),
];
