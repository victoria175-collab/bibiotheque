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

    if (!this.formValid) {
      return;
    }

    this.submitted.emit({
      livreId: this.selectedBookId as number,
      adherentId: this.selectedUserId as number
    });
  }
}