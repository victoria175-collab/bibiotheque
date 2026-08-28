import {
  Component,
  EventEmitter,
  Input,
  Output
} from '@angular/core';

import {
  ReservationDisplay,
  ReservationStatus
} from '../_model/reservation';

@Component({
  selector: 'app-reservation-list',
  templateUrl: './reservation-list.component.html',
  styleUrls: ['./reservation-list.component.css']
})
export class ReservationListComponent {

  @Input() reservations: ReservationDisplay[] = [];
  @Input() selectedStatus = '';
  @Input() statuses: ReservationStatus[] = [];
  @Input() errorMessage = '';

  @Output() statusChange =
    new EventEmitter<string>();

  @Output() cancelRequested =
    new EventEmitter<number>();

  canCancel(status: ReservationStatus): boolean {
    return status === 'EN_ATTENTE' ||
           status === 'DISPONIBLE';
  }

  confirmCancellation(
    reservation: ReservationDisplay
  ): void {

    const confirmed = window.confirm(
      `Confirmer l’annulation de la réservation de « ${reservation.livreTitre} » ?`
    );

    if (confirmed) {
      this.cancelRequested.emit(reservation.id);
    }
  }
}