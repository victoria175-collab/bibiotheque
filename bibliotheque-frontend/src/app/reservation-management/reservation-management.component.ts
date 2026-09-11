import { Component, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { forkJoin } from 'rxjs';

import { Books } from '../_model/books';
import { Users } from '../_model/users';
import { Reservation, ReservationCreateRequest, ReservationDisplay, ReservationStatus } from '../_model/reservation';

import { BooksService } from '../_service/books.service';
import { UsersService } from '../_service/users.service';
import { ReservationService } from '../_service/reservation.service';

@Component({
  selector: 'app-reservation-management',
  templateUrl: './reservation-management.component.html',
  styleUrls: ['./reservation-management.component.css']
})
export class ReservationManagementComponent implements OnInit {

  readonly statuses: ReservationStatus[] = [
    'EN_ATTENTE',
    'DISPONIBLE',
    'ANNULEE',
    'EXPIREE',
    'HONOREE'
  ];

  reservations: ReservationDisplay[] = [];
  books: Books[] = [];
  users: Users[] = [];

  selectedStatus = '';
  currentPage = 1;
  readonly pageSize = 10;

  loading = false;
  loadError = '';
  formError = '';
  actionError = '';

  constructor(
    private readonly reservationService: ReservationService,
    private readonly booksService: BooksService,
    private readonly usersService: UsersService
  ) {}

  ngOnInit(): void {
    this.loadData();
  }

 get filteredReservations(): ReservationDisplay[] {
  if (!this.selectedStatus) {
    return this.reservations;
  }

  return this.reservations.filter(
    reservation => reservation.statut === this.selectedStatus
  );
}

get paginatedReservations(): ReservationDisplay[] {
  const startIndex = (this.currentPage - 1) * this.pageSize;

  return this.filteredReservations.slice(
    startIndex,
    startIndex + this.pageSize
  );
}

get totalPages(): number {
  return Math.ceil(
    this.filteredReservations.length / this.pageSize
  );
}

  loadData(): void {
    this.loading = true;
    this.loadError = '';
    this.actionError = '';

    forkJoin({
      reservations: this.reservationService.getReservations(),
      books: this.booksService.getBooksList(),
      users: this.usersService.getUsersList()
    }).subscribe({
      next: result => {
        this.reservations = this.toDisplayReservations(
          result.reservations,
          result.books,
          result.users
        );

        this.books = result.books;
        this.users = result.users.filter(user => this.isAdherent(user));

        this.loading = false;
      },

      error: (error: HttpErrorResponse) => {
        this.loading = false;
        this.reservations = [];
        this.loadError = this.getLoadErrorMessage(error);
      }
    });
  }

  onStatusChange(status: string): void {
    this.selectedStatus = status;
    this.currentPage = 1;
  }

  createReservation(request: ReservationCreateRequest): void {
    this.formError = '';

    this.reservationService.createReservation(request).subscribe({
      next: () => {
        this.loadData();
      },

      error: (error: HttpErrorResponse) => {
        this.formError = this.getBusinessErrorMessage(error);
      }
    });
  }

  cancelReservation(id: number): void {
    this.actionError = '';

    this.reservationService.cancelReservation(id).subscribe({
      next: updatedReservation => {
        this.reservations = this.reservations.map(reservation =>
          reservation.id === updatedReservation.id
            ? {
                ...reservation,
                statut: updatedReservation.statut
              }
            : reservation
        );
      },

      error: (error: HttpErrorResponse) => {
        this.actionError = this.getBusinessErrorMessage(error);
      }
    });
  }

  deleteReservation(id: number): void {
    this.actionError = '';

    this.reservationService.deleteReservation(id).subscribe({
      next: () => {
        this.reservations = this.reservations.filter(reservation => reservation.id !== id);
        if (this.currentPage > this.totalPages && this.currentPage > 1) {
          this.currentPage--;
        }
      },

      error: (error: HttpErrorResponse) => {
        this.actionError = this.getBusinessErrorMessage(error);
      }
    });
  }

  private isAdherent(user: Users): boolean {
    return Array.isArray(user.role) &&
      user.role.some(
        (role: { roleName?: string }) =>
          role.roleName === 'User'
      );
  }

  private toDisplayReservations(
    reservations: Reservation[],
    books: Books[],
    users: Users[]
  ): ReservationDisplay[] {

    const booksById = new Map(
      books.map(book => [book.bookId, book])
    );

    const usersById = new Map(
      users.map(user => [user.userId, user])
    );

    return reservations.map(reservation => ({
      ...reservation,
      livreTitre:
        booksById.get(reservation.livreId)?.bookName ||
        `Livre #${reservation.livreId}`,
      adherentNom:
        usersById.get(reservation.adherentId)?.name ||
        `Adhérent #${reservation.adherentId}`
    }));
  }

  private getLoadErrorMessage(
    error: HttpErrorResponse
  ): string {

    if (error.status === 401) {
      return 'Votre session n’est plus valide. Veuillez vous reconnecter.';
    }

    if (error.status === 403) {
      return 'Vous n’êtes pas autorisé à consulter les données nécessaires à cet écran.';
    }

    if (error.status === 0) {
      return 'Le serveur est injoignable. Vérifiez que le backend est démarré, puis réessayez.';
    }

    return this.extractServerMessage(error) ||
      'Impossible de charger les réservations. Réessayez.';
  }

  private getBusinessErrorMessage(
    error: HttpErrorResponse
  ): string {

    const serverMessage = this.extractServerMessage(error);

    if (serverMessage) {
      return serverMessage;
    }

    switch (error.status) {
      case 400:
        return 'La demande est invalide. Vérifiez les champs sélectionnés.';

      case 404:
        return 'Le livre ou l’adhérent demandé est introuvable.';

      case 409:
        return 'Cette opération est refusée par une règle métier de l’application.';

      case 0:
        return 'Le serveur est injoignable. Vérifiez que le backend est démarré.';

      default:
        return 'L’opération n’a pas pu être effectuée. Réessayez.';
    }
  }

  private extractServerMessage(
    error: HttpErrorResponse
  ): string {

    if (
      error.error &&
      typeof error.error.message === 'string'
    ) {
      return error.error.message;
    }

    if (typeof error.error === 'string') {
      return error.error;
    }

    return '';
  }

  previousPage(): void {
  if (this.currentPage > 1) {
    this.currentPage--;
  }
}

nextPage(): void {
  if (this.currentPage < this.totalPages) {
    this.currentPage++;
  }
}
}