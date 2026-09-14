import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { NgForm } from '@angular/forms';
import { Observable } from 'rxjs';
import { Users } from '../_model/users';
import { UserAuthService } from './user-auth.service';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class UsersService {

  
  requestHeader = new HttpHeaders(
    { 'No-Auth': 'True' }
  );

  private readonly baseURL = `${environment.apiUrl}/admin/users`;
  constructor(
    private httpClient: HttpClient,
    private userAuthService: UserAuthService
  ) { }

  public login(loginData: NgForm) {
    return this.httpClient.post(`${environment.apiUrl}/authenticate`, loginData, {
      headers: this.requestHeader,
    });
  }

  public roleMatch(allowedRoles: any): boolean {
    const userRoles: any = this.userAuthService.getRoles();

    if (!userRoles || !Array.isArray(userRoles)) {
      return false;
    }

    const normalizedAllowed = (allowedRoles || []).map((role: string) =>
      this.normalizeRoleName(role)
    );

    for (const userRole of userRoles) {
      const normalizedUserRole = this.normalizeRoleName(userRole?.roleName);
      if (normalizedAllowed.includes(normalizedUserRole)) {
        return true;
      }
    }

    return false;
  }

  private normalizeRoleName(roleName?: string): string {
    if (!roleName) {
      return '';
    }

    const value = roleName.trim();
    const upperValue = value.toUpperCase();

    if (upperValue === 'ADMIN' || upperValue === 'ROLE_ADMIN' || upperValue === 'BIBLIOTHECAIRE' || upperValue === 'ROLE_BIBLIOTHECAIRE') {
      return 'ADMIN';
    }

    if (upperValue === 'USER' || upperValue === 'ROLE_USER' || upperValue === 'ADHERENT' || upperValue === 'ROLE_ADHERENT') {
      return 'USER';
    }

    return upperValue;
  }

  getUsersList(): Observable<Users[]> {
    return this.httpClient.get<Users[]>(`${this.baseURL}`);
  }

  createUser(user: Users): Observable<Object> {
    return this.httpClient.post(`${this.baseURL}`, user);
  }

  getUserById(userId: number): Observable<Users> {
    return this.httpClient.get<Users>(`${this.baseURL}/${userId}`);
  }

  updateUser(userId: number, user: Users): Observable<Object> {
    return this.httpClient.put(`${this.baseURL}/${userId}`, user);
  }

}
