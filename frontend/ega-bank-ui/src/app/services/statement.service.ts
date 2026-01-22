import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';

@Injectable({ providedIn: 'root' })
export class StatementService {
  constructor(private api: ApiService) { }

  downloadStatement(numeroCompte: string, debut: string, fin: string): Observable<Blob> {
    // Backend exposes GET /api/statements/{numeroCompte}?debut=...&fin=...
    return this.api.get<Blob>(
      `/statements/${encodeURIComponent(numeroCompte)}`,
      { debut, fin },
      { responseType: 'blob' }
    );
  }
}
