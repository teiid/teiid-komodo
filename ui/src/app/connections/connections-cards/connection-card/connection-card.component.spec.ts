import { async, ComponentFixture, TestBed } from "@angular/core/testing";
import { RouterTestingModule } from "@angular/router/testing";
import { ConnectionStatus } from "@connections/shared/connection-status";
import { Connection } from "@connections/shared/connection.model";
import { LoggerService } from "@core/logger.service";
import {
  ActionModule,
  CardModule,
  EmptyStateModule,
  FilterModule,
  ListModule,
  NotificationModule,
  SortModule,
  WizardModule } from "patternfly-ng";
import { ConnectionCardComponent } from "./connection-card.component";

describe("ConnectionCardComponent", () => {
  let component: ConnectionCardComponent;
  let fixture: ComponentFixture<ConnectionCardComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        RouterTestingModule,
        ActionModule,
        CardModule,
        EmptyStateModule,
        FilterModule,
        ListModule,
        NotificationModule,
        SortModule,
        WizardModule
      ],
      declarations: [ ConnectionCardComponent ],
      providers: [ LoggerService ]
    })
    .compileComponents().then(() => {
      // nothing to do
    });
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ConnectionCardComponent);
    component = fixture.componentInstance;

    // Create a connection
    const connection = new Connection();
    connection.setId( "MyConnection" );

    // Set a status on the connection
    const connStatus = ConnectionStatus.createLoadingStatus("MyConnection");
    connection.setStatus(connStatus);

    component.connection = connection;
    component.selectedConnections = [ connection ];

    fixture.detectChanges();
  });

  it("should be created", () => {
    expect(component).toBeTruthy();
  });
});
