import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { FormsModule } from '@angular/forms';
import { of } from 'rxjs';

import { UpdateUserComponent } from './update-user.component';
import { UsersService } from '../_service/users.service';

describe('UpdateUserComponent', () => {
  let component: UpdateUserComponent;
  let fixture: ComponentFixture<UpdateUserComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RouterTestingModule, FormsModule],
      declarations: [UpdateUserComponent],
      providers: [
        { provide: ActivatedRoute, useValue: { snapshot: { params: { userId: 1 } } } },
        { provide: UsersService, useValue: { getUserById: () => of({}), updateUser: () => of({}) } }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(UpdateUserComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
