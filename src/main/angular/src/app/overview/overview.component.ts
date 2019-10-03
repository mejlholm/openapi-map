import {Component, OnInit} from '@angular/core';
import {ServicesService} from '../services/services.service';

@Component({
  selector: 'app-overview',
  templateUrl: './overview.component.html',
  styles: []
})
export class OverviewComponent implements OnInit {
  namespace$ = this.srv.getNamespace();
  service: any;
  ingressedServices$ = this.srv.getIngressed();
  nonIngressedServices$ = this.srv.getNonIngressed();

  constructor( private srv: ServicesService) {
  }

  renderOperation(operation: string): string {
    let className = ''; //default color for the more exotic operations

    if (operation === 'GET'){
      className = "primary";
    } else if (operation === 'DELETE') {
      className = 'warn';
    } else if (operation === 'POST') {
      className = 'Accent';
    } else if (operation === 'PUT') {
      className = '';
    }
    return className;
  }

  ngOnInit() {
  }

}
