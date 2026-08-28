export type ReservationStatus =
  | 'EN_ATTENTE'
  | 'DISPONIBLE'
  | 'ANNULEE'
  | 'EXPIREE'
  | 'HONOREE';

export interface Reservation {
  id: number;
  livreId: number;
  adherentId: number;
  dateReservation: string;
  dateExpiration: string;
  statut: ReservationStatus;
}

export interface ReservationCreateRequest {
  livreId: number;
  adherentId: number;
}

export interface ReservationDisplay extends Reservation {
  livreTitre: string;
  adherentNom: string;
}