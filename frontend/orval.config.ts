import { defineConfig } from 'orval';

export default defineConfig({
  auth: {
    input: {
      target: '../api/auth.yaml',
      parserOptions: {
        externalRefs: {
          allow: ['*'],
        },
      },
    },
    output: {
      mode: 'split',
      target: './src/api/generated/auth.ts',
      schemas: './src/api/generated/model',
      client: 'fetch',
      baseUrl: '/v1',
      mock: false,
    },
  },
});

