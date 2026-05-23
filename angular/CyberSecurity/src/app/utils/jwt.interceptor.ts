import { HttpInterceptorFn, HttpErrorResponse, HttpRequest, HttpHandlerFn, HttpEvent } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../service/auth.service';
import { catchError, switchMap, throwError, BehaviorSubject, filter, take, Observable } from 'rxjs';

let isRefreshing = false;
const refreshTokenSubject: BehaviorSubject<string | null> = new BehaviorSubject<string | null>(null);

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
    const authService = inject(AuthService);
    const jwt = localStorage.getItem('jwt');

    if (jwt) {
        req = req.clone({
            setHeaders: { Authorization: `Bearer ${jwt}` }
        });
    }

    return next(req).pipe(
        catchError((error) => {
            if (error instanceof HttpErrorResponse && (error.status === 403 || error.status === 401)) {
                return handleTokenRefresh(req, next, authService);
            }
            return throwError(() => error);
        })
    );
};

const handleTokenRefresh = (
    req: HttpRequest<unknown>,
    next: HttpHandlerFn,
    authService: AuthService
): Observable<HttpEvent<unknown>> => {
    if (!isRefreshing) {
        isRefreshing = true;
        refreshTokenSubject.next(null);

        const refreshToken = localStorage.getItem('refreshToken');

        return authService.refreshToken().pipe(
            switchMap((response: any) => {
                isRefreshing = false;

                localStorage.setItem('jwt', response.accessToken);
                refreshTokenSubject.next(response.accessToken);

                return next(req.clone({
                    setHeaders: { Authorization: `Bearer ${response.accessToken}` }
                }));
            }),
            catchError((refreshError) => {
                isRefreshing = false;
                authService.logout();
                return throwError(() => refreshError);
            })
        );
    } else {
        return refreshTokenSubject.pipe(
            filter(token => token !== null),
            take(1),
            switchMap((jwt) => {
                return next(req.clone({
                    setHeaders: { Authorization: `Bearer ${jwt}` }
                }));
            })
        );
    }
};