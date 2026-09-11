import { Component, OnInit } from '@angular/core';
import { NgForm } from '@angular/forms';
import { Router } from '@angular/router';
import { UserAuthService } from '../_service/user-auth.service';
import { UsersService } from '../_service/users.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  errorMessage = '';

  constructor(private userService: UsersService,
    private userAuthSerivce: UserAuthService,
    private router: Router
  ) { }

  ngOnInit() {
    this.errorMessage = sessionStorage.getItem('authErrorMessage') || '';
    sessionStorage.removeItem('authErrorMessage');
  }

  login(loginForm: NgForm) {
    this.errorMessage = '';
    this.userService.login(loginForm.value).subscribe(
      (response: any)=>{
        this.userAuthSerivce.setRoles(response.user.role);
        this.userAuthSerivce.setToken(response.jwtToken);
        this.userAuthSerivce.setUserId(response.user.userId);
        this.userAuthSerivce.setName(response.user.name);

        const role = response.user.role[0].roleName;
        if(role === 'Admin') {
          this.router.navigate(['/books']);
        } else {
          this.router.navigate(['/borrow-book']) //update later
        }
      },
      (error)=>{
        this.errorMessage = this.extractErrorMessage(error) ||
          'Identifiants invalides. Vérifiez votre nom d’utilisateur et votre mot de passe.';
      }
    );
  }

  private extractErrorMessage(error: any): string {
    if (error.error && typeof error.error.message === 'string') {
      return error.error.message;
    }

    if (typeof error.error === 'string') {
      return error.error;
    }

    return '';
  }

}