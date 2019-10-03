import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {PathResult} from '../models/path-result.model';

@Injectable()
export class ServicesService {


  private baseUrl = '/api/services';

  constructor(private http: HttpClient) {
  }

  getNamespace(): Observable<string> {
    return this.http.get<any>(`${this.baseUrl}/namespace`)
      .pipe(
        map(data => {
          return <string> data.namespace;
        })
      );
  }

  getIngressed(): Observable<PathResult[]> {
    return this.http.get<PathResult[]>(`${this.baseUrl}/ingressed`);
  }

  getNonIngressed(): Observable<PathResult[]> {
    return this.http.get<PathResult[]>(`${this.baseUrl}/nonIngressed`);
  }
}
