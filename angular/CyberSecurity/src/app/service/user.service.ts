import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../environments/environment';
import { GetAccountDetailsDTO } from '../model/GetAccountDetailsDTO';
import { Observable } from 'rxjs';
import { UpdateUserDTO } from '../model/UpdateUserDTO';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private readonly http = inject(HttpClient);

  getUsers() {
    return this.http.get<any>(`${environment.apiHost}/users`);
  }

  getUser(): Observable<GetAccountDetailsDTO> {
    return this.http.get<GetAccountDetailsDTO>(`${environment.apiHost}/user`)
  }

  updateUser(data: UpdateUserDTO){
    return this.http.put<GetAccountDetailsDTO>(`${environment.apiHost}/user`, data)
  }

}
