import {
  Component,
  EventEmitter,
  Input,
  Output
} from '@angular/core';

import { Books } from '../_model/books';
import { Users } from '../_model/users';
import {
  ReservationCreateRequest
} from '../_model/reservation';

@Component({
  selector: 'app-reservation-form',
  templateUrl: './reservation-form.component.html',
  styleUrls: ['./reservation-form.component.css']
})
export class ReservationFormComponent {

  @Input() books: Books[] = [];
  @Input() users: Users[] = [];
  @Input() currentUserId: number | null = null;
  @Input() isUserMode = false;
  @Input() errorMessage = '';

  @Output() submitted =
    new EventEmitter<ReservationCreateRequest>();

  selectedBookId: number | null = null;
  selectedUserId: number | null = null;

  get formValid(): boolean {
    return this.selectedBookId !== null &&
           this.selectedUserId !== null;
  }

  submit(): void {
    const adherentId = this.isUserMode ? this.currentUserId : this.selectedUserId;

    if (this.selectedBookId === null || adherentId === null) {
      return;
    }

    this.submitted.emit({
      livreId: this.selectedBookId as number,
      adherentId: adherentId as number
    });
  }
}