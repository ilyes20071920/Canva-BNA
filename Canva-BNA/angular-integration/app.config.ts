// app.config.ts — Angular 17+ standalone app configuration
// Shows how to wire the interceptor with provideHttpClient

import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { jwtInterceptor } from './interceptors/jwt.interceptor';
// import { routes } from './app.routes';   // Uncomment with your actual routes

export const appConfig: ApplicationConfig = {
  providers: [
    // provideRouter(routes),              // Uncomment with your actual routes
    provideHttpClient(
      withInterceptors([jwtInterceptor])  // <-- Register the JWT interceptor here
    ),
  ],
};
