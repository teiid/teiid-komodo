/**
 * @license
 * Copyright 2017 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { Component, EventEmitter, OnInit, Output, ViewChild, ViewEncapsulation } from "@angular/core";
import { Connection } from "@connections/shared/connection.model";
import { ConnectionService } from "@connections/shared/connection.service";
import { LoggerService } from "@core/logger.service";
import { JdbcTableSelectorComponent } from "@dataservices/jdbc-table-selector/jdbc-table-selector.component";
import { Table } from "@dataservices/shared/table.model";
import { LoadingState } from "@shared/loading-state.enum";

@Component({
  encapsulation: ViewEncapsulation.None,
  selector: "app-connection-table-selector",
  templateUrl: "./connection-table-selector.component.html",
  styleUrls: ["./connection-table-selector.component.css"]
})
export class ConnectionTableSelectorComponent implements OnInit {

  @Output() public tableSelectionChanged: EventEmitter<void> = new EventEmitter<void>();

  @ViewChild(JdbcTableSelectorComponent) public jdbcTableSelector: JdbcTableSelectorComponent;

  public readonly nameProp = "name"; // must match html template
  public rows: any[] = [];

  public readonly customClasses = {
    sortAscending: "fa fa-sort-asc",
    sortDescending: "fa fa-sort-desc",
    pagerLeftArrow: "fa fa-chevron-left",
    pagerRightArrow: "fa fa-chevron-right",
    pagerPrevious: "fa fa-step-backward",
    pagerNext: "fa fa-step-forward"
  };

  private connectionService: ConnectionService;
  private allConnections: Connection[] = [];
  private selectedConn: Connection;
  private connectionLoadingState: LoadingState = LoadingState.LOADING;
  private logger: LoggerService;

  constructor( connectionService: ConnectionService, logger: LoggerService ) {
    this.connectionService = connectionService;
    this.logger = logger;
  }

  /*
   * Component initialization
   */
  public ngOnInit(): void {
    // Load the connections
    this.connectionLoadingState = LoadingState.LOADING;
    const self = this;
    this.connectionService
      .getAllConnections()
      .subscribe(
        (conns) => {
          self.allConnections = conns;
          self.connectionLoadingState = LoadingState.LOADED_VALID;

          // load table after setting loading state so table has been constructed
          self.allConnections.forEach( ( connection ) => {
            const row = {};
            row[ this.nameProp ] = connection.getId();
            self.rows.push( row );
          } );
        },
        (error) => {
          self.logger.error("[ConnectionTableSelectorComponent] Error getting connections: %o", error);
          self.connectionLoadingState = LoadingState.LOADED_INVALID;
        }
      );
  }

  // callback from connections table selection
  public onSelect( { selected }) {
    // connection is single select so get first element
    const connectionName = selected[ 0 ][ this.nameProp ];

    // find and set selected connection (see setter)
    this.selectedConnection = this.allConnections.find(( conn ) => conn.getId() === connectionName );
  }

  /**
   * selector is valid if at least one table is selected.
   * @returns {boolean} the selector status (true if one or more tables selected)
   */
  public valid( ): boolean {
    return this.getSelectedTables().length > 0;
  }

  /**
   * Determine if connections are loading
   */
  public get connectionsLoading( ): boolean {
    return this.connectionLoadingState === LoadingState.LOADING;
  }

  /**
   * Determine if connections are loaded and valid
   */
  public get connectionsLoadedValid( ): boolean {
    return this.connectionLoadingState === LoadingState.LOADED_VALID;
  }

  /**
   * Determine if connections are loaded and invalid
   */
  public get connectionsLoadedInvalid( ): boolean {
    return this.connectionLoadingState === LoadingState.LOADED_INVALID;
  }

  /**
   * Determine if the supplied connection is currently selected.
   * @param {Connection} connection the connection
   * @returns {boolean} true if the connection is selected
   */
  public isConnectionSelected(connection: Connection): boolean {
    return this.selectedConn && this.selectedConn === connection;
  }

  /**
   * Determine if a JDBC connection is currently selected
   * @returns {boolean} true if a JDBC connection is selected
   */
  public hasJdbcConnectionSelected(): boolean {
    return (this.selectedConn && this.selectedConn.isJdbc());
  }

  /**
   * Determine if a non-JDBC connection is currently selected
   * @returns {boolean} true if a non-JDBC connection is selected
   */
  public hasNonJdbcConnectionSelected(): boolean {
    return (this.selectedConn && !this.selectedConn.isJdbc());
  }

  /**
   * Determine if anything is selected
   * @returns {boolean} true if a connection is selected
   */
  public hasSelectedConnection( ): boolean {
    return this.selectedConn !== null;
  }

  /**
   * Get the currently selected Connection
   * @returns {Connection} the current selection (may be null)
   */
  public get selectedConnection( ): Connection {
    return this.selectedConn;
  }

  /**
   * Set the currently selected Connection
   * @param {Connection} conn the current selection (may be null)
   */
  public set selectedConnection(conn: Connection) {
    this.selectedConn = conn;

    // Set the specific selector with the current connection
    if (this.jdbcTableSelector) {
      if (this.selectedConn && this.selectedConn.isJdbc()) {
        this.jdbcTableSelector.setConnection(this.selectedConnection);
      } else {
        this.jdbcTableSelector.setConnection(null);
      }
    }

    // notify upstream that connection selection has changed
    this.tableSelectionChanged.emit();
  }

  /*
   * Return all available Connections
   * @returns {Connection[]} the list of all Connections
   */
  public getAllConnections(): Connection[] {
    return this.allConnections;
  }

  /*
   * Return all currently selected Tables
   * @returns {Table[]} the list of selected Tables
   */
  public getSelectedTables(): Table[] {
    const selectedTables = [];
    if (this.jdbcTableSelector) {
      return this.jdbcTableSelector.getSelectedTables();
    }
    return selectedTables;
  }

  // used by table
  public get tableMessages(): { emptyMessage: string; totalMessage: string | string } {
    const msg = this.allConnections.length === 1 ? "connection" : "connections";

    return {
      // no data message
      emptyMessage: "No connections found",

      // footer total message
      totalMessage: msg
    };
  }

}
