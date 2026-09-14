import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { FormsModule } from '@angular/forms';
import { of } from 'rxjs';

import { UpdateBookComponent } from './update-book.component';
import { BooksService } from '../_service/books.service';

describe('UpdateBookComponent', () => {
  let component: UpdateBookComponent;
  let fixture: ComponentFixture<UpdateBookComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RouterTestingModule, FormsModule],
      declarations: [UpdateBookComponent],
      providers: [
        { provide: ActivatedRoute, useValue: { snapshot: { params: { bookId: 1 } } } },
        { provide: BooksService, useValue: { getBookById: () => of({}), updateBook: () => of({}) } }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(UpdateBookComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
