import { http, HttpResponse } from 'msw';
import type { CurrentUser, LoginInput } from '../api/generated/model';

const mockUser: CurrentUser = {
  id: 1,
  email: 'anna.kowalska@example.com',
  firstName: 'Anna',
  lastName: 'Kowalska',
  phone: '+48123456789',
  roles: ['CLIENT'],
};

export const handlers = [
  http.post('*/v1/auth/login', async ({ request }) => {
    const credentials = (await request.json()) as LoginInput;

    if (
      credentials.email === 'anna.kowalska@example.com' &&
      credentials.password === 'sekret123'
    ) {
      return HttpResponse.json(mockUser, { status: 200 });
    }

    return HttpResponse.json(
      {
        code: 'INVALID_CREDENTIALS',
        message: 'Niepoprawny email lub hasło.',
      },
      { status: 401 }
    );
  }),
];
