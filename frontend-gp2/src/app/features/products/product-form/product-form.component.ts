import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { ProductoService } from '../../../core/services/producto.service';
import { AlertService } from '../../../core/services/alert.service';

@Component({
  selector: 'app-product-form',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './product-form.component.html',
  styleUrl: './product-form.component.css'
})
export class ProductFormComponent implements OnInit {
  private readonly catalog = inject(ProductoService);
  private readonly router = inject(Router);
  private readonly alerts = inject(AlertService);
  readonly id = Number(inject(ActivatedRoute).snapshot.paramMap.get('id'));
  readonly form = inject(FormBuilder).nonNullable.group({
    nombre: ['', [Validators.required, Validators.maxLength(240)]],
    descripcion: ['', Validators.maxLength(255)],
    precio: [0, [Validators.required, Validators.min(.01)]],
    stock: [0, [Validators.required, Validators.min(0)]]
  });
  readonly busy = signal(false);
  readonly error = signal('');
  readonly loadError = signal('');
  private photo: File | null = null;

  ngOnInit(): void {
    if (!this.id) return;
    this.busy.set(true);
    this.catalog.getProductoById(this.id).pipe(finalize(() => this.busy.set(false))).subscribe({
      next: product => this.form.patchValue(product),
      error: () => this.loadError.set('No pudimos cargar el producto.')
    });
  }

  onFileSelected(event: Event): void {
    this.photo = (event.target as HTMLInputElement).files?.item(0) ?? null;
  }

  guardar(): void {
    if (this.busy() || this.form.invalid) return;
    this.busy.set(true);
    this.error.set('');
    const data = new FormData();
    for (const [key, value] of Object.entries(this.form.getRawValue())) {
      data.set(key, String(value));
    }
    if (this.photo) data.set('file', this.photo);
    const request = this.id ? this.catalog.updateProducto(this.id, data) : this.catalog.createProducto(data);
    request.pipe(finalize(() => this.busy.set(false))).subscribe({
      next: () => {
        this.alerts.toast(this.id ? 'Producto actualizado' : 'Producto creado');
        this.router.navigate(['/productos']);
      },
      error: (error: HttpErrorResponse) => this.error.set(
        Object.values(error.error?.errors ?? {}).join(' ') || error.error?.message || 'No se pudo guardar el producto.')
    });
  }
}
