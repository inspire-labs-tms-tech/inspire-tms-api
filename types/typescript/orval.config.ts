import {defineConfig} from "orval";


module.exports = defineConfig({
  ts: {
    input: { target: "http://127.0.0.1:8080/api-docs" },
    output: {
      target: "./gen.ts",
      client: "axios-functions",
      mock: false,
      mode: "single"
    }
  }
});
