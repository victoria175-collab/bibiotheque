import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  Reservation,
  ReservationCreateRequest
} from '../_model/reservation';

@Injectable({
  providedIn: 'root'
})
export class ReservationService {

  private readonly baseUrl = 'http://localhost:8080/api/reservations';

  constructor(private readonly httpClient: HttpClient) {}

  getReservations(): Observable<Reservation[]> {
    return this.httpClient.get<Reservation[]>(this.baseUrl);
  }

  createReservation(
    request: ReservationCreateRequest
  ): Observable<Reservation> {
    return this.httpClient.post<Reservation>(
      this.baseUrl,
      request
    );
  }

  cancelReservation(id: number): Observable<Reservation> {
    return this.httpClient.patch<Reservation>(
      `${this.baseUrl}/${id}/annuler`,
      {}
    );
  }
}