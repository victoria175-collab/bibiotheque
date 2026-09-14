import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { Observable } from 'rxjs';
import { UserAuthService } from '../_service/user-auth.service';
import { UsersService } from '../_service/users.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {

  constructor(
    private userAuthService: UserAuthService,
    private router: Router,
    private userService: UsersService
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {

    const token = this.userAuthService.getToken();
    if (!token) {
      this.router.navigate(['/login']);
      return false;
    }

    const allowedRoles = (route.data['roles'] as string[] || []).map(role => this.normalizeRole(role));
    const userRoles = this.userAuthService.getRoles() || [];

    const hasAccess = userRoles.some((role: any) => {
      const normalizedRole = this.normalizeRole(role?.roleName ?? role);
      return allowedRoles.includes(normalizedRole);
    });

    if (hasAccess) {
      return true;
    }

    this.router.navigate(['/forbidden']);
    return false;
  }

  private normalizeRole(roleName?: string): string {
    if (!roleName) {
      return '';
    }

    return roleName
      .toString()
      .trim()
      .toUpperCase()
      .replace('ROLE_', '');
  }
}
