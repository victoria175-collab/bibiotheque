import {
  Component,
  EventEmitter,
  Input,
  Output
} from '@angular/core';

@Component({
  selector: 'app-message-modal',
  templateUrl: './message-modal.component.html',
  styleUrls: ['./message-modal.component.css']
})
export class MessageModalComponent {

  @Input() visible = false;
  @Input() title = 'Information';
  @Input() message = '';
  @Input() type: 'success' | 'error' | 'warning' | 'confirm' = 'success';
  @Input() showCancel = false;

  @Output() closed = new EventEmitter<void>();
  @Output() confirmed = new EventEmitter<void>();

  close(): void {
    this.closed.emit();
  }

  confirm(): void {
    this.confirmed.emit();
  }
}