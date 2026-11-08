import { cp, mkdir, writeFile } from "node:fs/promises";
import { resolve } from "node:path";
import { fileURLToPath } from "node:url";

const projectRoot = resolve(fileURLToPath(new URL("..", import.meta.url)));
const staticDirectory = resolve(projectRoot, "demoappgym/src/main/resources/static");
const outputDirectory = resolve(projectRoot, "dist");
const apiBaseUrl = process.env.APP_API_BASE_URL?.trim().replace(/\/+$/, "") || "/api";

if (!apiBaseUrl.startsWith("/")) {
  const parsedApiUrl = new URL(apiBaseUrl);
  if (!["http:", "https:"].includes(parsedApiUrl.protocol)) {
    throw new Error("APP_API_BASE_URL debe usar una URL http o https.");
  }
}

await mkdir(outputDirectory, { recursive: true });
await cp(staticDirectory, outputDirectory, { recursive: true, force: true });
await writeFile(
  resolve(outputDirectory, "assets/config.js"),
  `window.APP_CONFIG = Object.freeze({ apiBaseUrl: ${JSON.stringify(apiBaseUrl)} });\n`,
  "utf8"
);

console.log(`Frontend listo en ${outputDirectory}`);
