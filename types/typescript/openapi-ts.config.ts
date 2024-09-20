import {defineConfig} from '@hey-api/openapi-ts';

export default defineConfig({
  client: "@hey-api/client-axios",
  input: "http://127.0.0.1:8080/api-docs",
  output: "./gen",
  services: {
    asClass: true
  }
});
