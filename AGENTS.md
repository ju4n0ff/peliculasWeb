# AGENTS

## Repo shape (do not mix these by accident)
- `backend/` is the real API (Spring Boot 4.0.5 + JPA + Security + JWT).
- `frontend/` is the real SPA (Angular 20 standalone app).

## Verified run commands
- Backend dev server (Windows PowerShell): `./mvnw.cmd spring-boot:run` from `backend/`.
- Backend tests: `./mvnw.cmd test` from `backend/`.
- Single backend test: `./mvnw.cmd -Dtest=BackendApplicationTests test` (or another test class name) from `backend/`.
- Frontend install: `npm install` from `frontend/`.
- Frontend dev server: `npm run start` (alias of `ng serve`) from `frontend/`.
- Frontend build: `npm run build` from `frontend/`.
- Frontend tests: `npm run test` from `frontend/`.

## Runtime wiring that agents often miss
- Frontend auth service calls `http://localhost:8080/api/usuarios` directly (`frontend/src/app/services/auth.ts`), so backend must be running on `8080` for login flows.
- Backend CORS is configured for `http://localhost:4200` in `backend/src/main/java/com/peliculas/backend/config/SecurityConfig.java`.
- Security is feature-flagged by `app.security.enabled` (in gitignored `src/main/resources/application.properties` or env); currently `false` so all requests are permitted.
- Initial movie/series seed data is auto-loaded at startup by `backend/src/main/java/com/peliculas/backend/DataLoader.java` when tables are empty.

## Entry points and boundaries
- Backend entry point: `backend/src/main/java/com/peliculas/backend/BackendApplication.java`.
- Main backend API controllers: `/api/usuarios`, `/api/peliculas`, `/api/series` under `backend/src/main/java/com/peliculas/backend/controller/`.
- Frontend bootstrap: `frontend/src/main.ts` -> `frontend/src/app/app.config.ts` -> `frontend/src/app/app.routes.ts`.
- Angular routes (component-routed, no NgModule): `login`, `home`, `peliculas`, `series`, `generos/:genero`, `detalle/:tipo/:slug`, `perfil`, `admin`, `contactos`, `nosotros`, `terminos`, `privacidad`.

## Safety and hygiene notes
- `backend/src/main/resources/application.properties` is gitignored (`backend/.gitignore` line 39-40) and contains DB credentials; do not leak into PRs/logs. Prefer env vars for any security-related task.
- Build outputs and dependencies (`backend/target`, `frontend/dist`, `frontend/node_modules`, `frontend/.angular/cache`) should not be edited manually.
