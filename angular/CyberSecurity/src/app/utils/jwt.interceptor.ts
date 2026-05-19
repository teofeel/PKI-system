import { HttpErrorResponse, HttpInterceptorFn } from "@angular/common/http"
import { inject } from "@angular/core"
import { Router } from "@angular/router"
import { BehaviorSubject, catchError, filter, switchMap, take, throwError } from "rxjs";
import { AuthService } from "../service/auth.service";

let isRefreshing = false;
const refreshTokenSubject = new BehaviorSubject<string | null>(null);

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
    const authService = inject(AuthService);
    const router = inject(Router);

    const token = localStorage.getItem("jwt");
    let authReq = req;

    if (token) {
        authReq = req.clone({
            setHeaders: {
                Authorization: `Bearer ${token}`
            }
        });
    }

    return next(authReq).pipe(
        catchError((err: HttpErrorResponse) => {
            if (err.status === 401 && !authReq.url.includes('/auth/refresh') && !authReq.url.includes('/auth/login')) {
                if (!isRefreshing) {
                    isRefreshing = true;
                    refreshTokenSubject.next(null);

                    return authService.refreshToken().pipe(
                        switchMap((res) => {
                            isRefreshing = false;
                            refreshTokenSubject.next(res.accessToken);
                            return next(authReq.clone({
                                setHeaders: {
                                    Authorization: `Bearer ${res.accessToken}`
                                }
                            }));
                        }),
                        catchError((refreshErr) => {
                            isRefreshing = false;
                            authService.clearSession();
                            router.navigate(['/login']);
                            return throwError(() => refreshErr);
                        })
                    );
                } else {
                    return refreshTokenSubject.pipe(
                        filter(t => t !== null),
                        take(1),
                        switchMap((newJwt) => next(authReq.clone({
                            setHeaders: {
                                Authorization: `Bearer ${newJwt}`
                            }
                        })))
                    );
                }
            }
            return throwError(() => err);
        })
    );
}