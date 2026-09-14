import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { UserDetailsComponent } from './user-details.component';
import { UsersService } from '../_service/users.service';
import { BooksService } from '../_service/books.service';
import { BorrowService } from '../_service/borrow.service';

describe('UserDetailsComponent', () => {
  let component: UserDetailsComponent;
  let fixture: ComponentFixture<UserDetailsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ UserDetailsComponent ],
      providers: [
        { provide: ActivatedRoute, useValue: { snapshot: { params: { userId: 1 } } } },
        { provide: BooksService, useValue: {} },
        { provide: BorrowService, useValue: { getBooksBorrowedByUser: () => of([]) } },
        { provide: UsersService, useValue: { getUserById: () => of({}) } }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UserDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
