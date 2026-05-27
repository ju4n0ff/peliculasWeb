# AGENTS

## Repo shape (do not mix these by accident)
- `backend/` is the real API (Spring Boot 4.0.5 + JPA + Security + JWT + Lombok).
- `frontend/` is the real SPA (Angular 20 standalone app with Karma/Jasmine).

## Verified run commands
- Backend dev server (Windows PowerShell): `./mvnw.cmd spring-boot:run` from `backend/`.
- Backend tests: `./mvnw.cmd test` from `backend/`.
- Single backend test: `./mvnw.cmd -Dtest=BackendApplicationTests test` from `backend/`.
- Frontend install: `npm install` from `frontend/`.
- Frontend dev server: `npm run start` (alias of `ng serve`) from `frontend/`.
- Frontend build: `npm run build` from `frontend/`.
- Frontend tests: `npm run test` from `frontend/`.

## Runtime wiring that agents often miss
- Frontend services (`auth.ts`, `catalogo.ts`, `social.ts`) hardcode `http://localhost:8080/api/...`, so backend must run on port `8080`.
- Backend CORS allows `http://localhost:4200` and `https://peliculas-web-tau.vercel.app` in `SecurityConfig.java:64`. `SocialController.java` also has a local `@CrossOrigin`.
- Security is feature-flagged by `app.security.enabled` in gitignored `application.properties`; currently `false` so all requests permitted.
- `application.properties` is now tracked with `${...}` env-var placeholders. Set env vars locally or create `application-local.properties` for overrides.
- Backend test suite is minimal: one `@SpringBootTest` context-loads test.

## API endpoints
- `/api/usuarios` (login, registro, verificación email)
- `/api/peliculas`, `/api/series` (catalog listing)
- `/api/social` (reseñas, calificaciones, favoritos, comentarios con like/dislike)

## Frontend structure
- Bootstrap: `main.ts` -> `app.config.ts` (providers: router + HttpClient) -> `App` (standalone, hosts `<app-header>` + `<router-outlet>` + `<app-footer>`).
- Routes: `login`, `home`, `peliculas`, `series`, `generos/:genero`, `detalle/:tipo/:slug`, `perfil`, `admin` (guarded by `adminGuard`), `contactos`, `nosotros`, `terminos`, `privacidad`.
- Services in `services/`: `auth`, `catalogo` (películas/series), `social` (reseñas, calificaciones, favoritos).
- State stored in `localStorage`: `token`, `userId`, `nombreUsuario`, `rol`.

## Safety and hygiene notes
- `backend/src/main/resources/application.properties` is now tracked with `${...}` env-var placeholders; never log or leak them.
- Build outputs and deps (`backend/target`, `frontend/dist`, `frontend/node_modules`, `frontend/.angular/cache`) should not be edited manually.
