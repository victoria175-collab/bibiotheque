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
@Input() isAdminMode = false;

  @Output() statusChange =
    new EventEmitter<string>();

  @Output() cancelRequested =
    new EventEmitter<number>();

  @Output() deleteRequested =
    new EventEmitter<number>();

  canCancel(status: ReservationStatus): boolean {
    return status === 'EN_ATTENTE' ||
           status === 'DISPONIBLE';
  }

 requestCancellation(
  reservation: ReservationDisplay
): void {
  this.cancelRequested.emit(reservation.id);
}

requestDeletion(
  reservation: ReservationDisplay
): void {
  if (!this.isAdminMode) {
    return;
  }

  this.deleteRequested.emit(reservation.id);
}
}