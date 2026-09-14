import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';

import { LoginComponent } from './login.component';
import { UsersService } from '../_service/users.service';
import { UserAuthService } from '../_service/user-auth.service';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FormsModule, RouterTestingModule],
      declarations: [LoginComponent],
      providers: [
        { provide: UsersService, useValue: { login: () => of({ user: { role: [{ roleName: 'Admin' }], userId: 1, name: 'Test User' }, jwtToken: 'abc' }) } },
        { provide: UserAuthService, useValue: { setRoles: () => {}, setToken: () => {}, setUserId: () => {}, setName: () => {} } }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
