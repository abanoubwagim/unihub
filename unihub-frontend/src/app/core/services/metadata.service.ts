import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface CountryResponse {
  id: number;
  name: string;
  code: string;
}

@Injectable({ providedIn: 'root' })
export class MetadataService {
  private readonly http = inject(HttpClient);
  private readonly BASE = `${environment.apiUrl}/metadata`;

  getCountries(): Observable<CountryResponse[]> {
    return this.http.get<CountryResponse[]>(`${this.BASE}/countries`);
  }
}
