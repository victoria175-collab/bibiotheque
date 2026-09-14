import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { Books } from '../_model/books';
import { Borrow } from '../_model/borrow';
import { BooksService } from '../_service/books.service';
import { BorrowService } from '../_service/borrow.service';
import { UserAuthService } from '../_service/user-auth.service';

@Component({
  selector: 'app-return-book',
  templateUrl: './return-book.component.html',
  styleUrls: ['./return-book.component.css']
})
export class ReturnBookComponent implements OnInit {

  books: Books[];
  borrow: Borrow[];

  constructor(
    private borrowService: BorrowService,
    private booksService: BooksService,
    private userAuthService: UserAuthService
  ) { }

  userId = this.userAuthService.getUserId();
  isAdminMode = this.hasLibrarianRole();

  ngOnInit(): void {
    this.getBooks();
    this.getBorrowings();
  }

  private getBooks() {
    this.booksService.getBooksList().subscribe(data =>{
      this.books = data;
    });
  }

  
  private getBorrowings() {
    const borrowings = this.isAdminMode
      ? this.borrowService.getBorrowList()
      : this.borrowService.getBooksBorrowedByUser(this.userId);

    borrowings.subscribe(data => {
      this.borrow = data;
    })
  }

  brw: Borrow = new Borrow();
  public returnBook(borrowId: number) {
    this.brw.borrowId = borrowId;
    this.borrowService.returnBook(this.brw).subscribe(data => {
      this.getBorrowings();
    },
    error => console.log(error));
  }

  private hasLibrarianRole(): boolean {
    const authService = this.userAuthService as UserAuthService & {
      getRoles?: () => any[];
    };
    const roles = typeof authService.getRoles === 'function'
      ? authService.getRoles() ?? []
      : [];

    return roles.some((role: any) => {
      const roleName = (role?.roleName ?? role ?? '')
        .toString()
        .trim()
        .toUpperCase()
        .replace('ROLE_', '');
      return roleName === 'ADMIN' || roleName === 'BIBLIOTHECAIRE';
    });
  }

}
