import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  templateUrl: './confirm-dialog.html',
  styleUrl: './confirm-dialog.css',
})
export class ConfirmDialog {
  @Input() titulo = 'Confirmar ação';
  @Input() mensagem = '';
  @Input() confirmarTexto = 'Confirmar';
  @Input() cancelarTexto = 'Cancelar';
  @Input() perigo = false;
  @Input() processando = false;

  @Output() confirmado = new EventEmitter<void>();
  @Output() cancelado = new EventEmitter<void>();
}
