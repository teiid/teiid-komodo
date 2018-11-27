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
import {AfterViewInit, Component, OnDestroy, OnInit, ViewEncapsulation} from '@angular/core';
import {LoadingState} from "@shared/loading-state.enum";
import {ViewDefinition} from "@dataservices/shared/view-definition.model";
import {ViewEditorPart} from "@dataservices/virtualization/view-editor/view-editor-part.enum";
import {EmptyStateConfig, NgxDataTableConfig, TableConfig} from "patternfly-ng";
import {ViewEditorI18n} from "@dataservices/virtualization/view-editor/view-editor-i18n";
import {ViewEditorProgressChangeId} from "@dataservices/virtualization/view-editor/event/view-editor-save-progress-change-id.enum";
import {ViewEditorState} from "@dataservices/shared/view-editor-state.model";
import {ViewEditorService} from "@dataservices/virtualization/view-editor/view-editor.service";
import {Dataservice} from "@dataservices/shared/dataservice.model";
import {LoggerService} from "@core/logger.service";
import {ViewEditorEvent} from "@dataservices/virtualization/view-editor/event/view-editor-event";
import {Subscription} from "rxjs/Subscription";
import {BsModalService} from "ngx-bootstrap";
import {DataserviceService} from "@dataservices/shared/dataservice.service";
import {SelectionService} from "@core/selection.service";
import {ConfirmDialogComponent} from "@shared/confirm-dialog/confirm-dialog.component";
import {CommandFactory} from "@dataservices/virtualization/view-editor/command/command-factory";
import {Command} from "@dataservices/virtualization/view-editor/command/command";
import {CreateViewDialogComponent} from "@dataservices/virtualization/view-editor/create-view-dialog/create-view-dialog.component";
import {ChangeDetectorRef} from "@angular/core";

@Component({
  encapsulation: ViewEncapsulation.None,
  selector: 'app-views-list',
  templateUrl: './views-list.component.html',
  styleUrls: ['./views-list.component.css']
})
export class ViewsListComponent implements OnInit, OnDestroy, AfterViewInit {

  // used by html
  public readonly viewDescriptionLabel = ViewEditorI18n.viewDescriptionLabel;
  public readonly viewDescriptionPlaceholder = ViewEditorI18n.viewDescriptionPlaceholder;

  public ngxTableConfig: NgxDataTableConfig;
  public tableConfig: TableConfig;
  public tableColumns: any[] = [];
  public tableRows: ViewDefinition[] = [];
  private emptyStateConfig: EmptyStateConfig;

  private readonly logger: LoggerService;
  private readonly editorService: ViewEditorService;
  private subscription: Subscription;
  private modalService: BsModalService;
  private dataserviceService: DataserviceService;
  private selectionService: SelectionService;
  private viewsLoadingState: LoadingState = LoadingState.LOADING;
  private selectedVirtualization: Dataservice;
  private viewSavedUponCompletion: ViewDefinition;
  private cdRef: ChangeDetectorRef;

  constructor( editorService: ViewEditorService,
               dataserviceService: DataserviceService,
               selectionService: SelectionService,
               logger: LoggerService,
               modalService: BsModalService,
               cdRef: ChangeDetectorRef) {
    this.editorService = editorService;
    this.dataserviceService = dataserviceService;
    this.selectionService = selectionService;
    this.logger = logger;
    this.modalService = modalService;
    this.cdRef = cdRef;
  }

  /**
   * @param {ViewEditorEvent} event the event being processed
   */
  public handleEditorEvent( event: ViewEditorEvent ): void {
    this.logger.debug( "ViewsListComponent received event: " + event.toString() );

    if ( event.typeIsEditorViewSaveProgressChanged() ) {
      if ( event.args.length !== 0 ) {
        // Detect changes in view editor save progress
        if ( event.args[ 0 ] === ViewEditorProgressChangeId.COMPLETED_SUCCESS ||
          event.args[ 0 ] === ViewEditorProgressChangeId.COMPLETED_FAILED ) {
          if (this.viewSavedUponCompletion && this.viewSavedUponCompletion !== null) {
            this.createNewView(this.viewSavedUponCompletion);
          }
        }
      }
    }
  }

  /**
   * Cleanup code when destroying the view editor header.
   */
  public ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  /**
   * Initialization code run after construction.
   */
  public ngOnInit(): void {
    this.subscription = this.editorService.editorEvent.subscribe( ( event ) => this.handleEditorEvent( event ) );

    // ----------------------------------
    // View Table configurations
    // ----------------------------------
    this.tableColumns = [
      {
        draggable: false,
        name: "Views",
        prop: "viewName",
        resizeable: true,
        sortable: false,
        width: "100"
      }
    ];

    this.ngxTableConfig = {
      headerHeight: 0,
      rowHeight: 20,
      reorderable: false,
      selectionType: "'single'"
    } as NgxDataTableConfig;

    this.emptyStateConfig = {
      title: ViewEditorI18n.noViewsDefined
    } as EmptyStateConfig;

    this.tableConfig = {
      emptyStateConfig: this.emptyStateConfig
    } as TableConfig;

  }

  public ngAfterViewInit(): void {
    // init the available views
    this.initViews();
    this.cdRef.detectChanges();
  }

  /*
   * Initialize the views for the current dataservice.  Makes a rest call to get the ViewEditorStates for the serviceVdb
   */
  private initViews( ): void {
    this.viewsLoadingState = LoadingState.LOADING;
    this.selectedVirtualization = this.selectionService.getSelectedVirtualization();
    if ( !this.selectedVirtualization || this.selectedVirtualization === null ) {
      this.tableRows = [];
    }

    const selectedView = this.selectionService.getSelectedViewDefinition();

    const vdbName = this.selectedVirtualization.getServiceVdbName();
    const editorStatesPattern = vdbName.toLowerCase() + "*";

    const self = this;
    this.dataserviceService
      .getViewEditorStates(editorStatesPattern)
      .subscribe(
        (viewEditorStates) => {
          const viewDefns: ViewDefinition[] = [];
          for ( const viewState of viewEditorStates ) {
            const viewDefn = viewState.getViewDefinition();
            if ( viewDefn ) {
              viewDefns.push( viewDefn );
            }
          }
          self.tableRows = viewDefns.sort( (left, right): number => {
            if (left.getName() < right.getName()) return -1;
            if (left.getName() > right.getName()) return 1;
            return 0;
          });

          let initialView: ViewDefinition = null;
          if (!selectedView || selectedView === null) {
            initialView = (self.tableRows && self.tableRows.length > 0) ? self.tableRows[0] : null;
          } else {
            initialView =  self.tableRows.find((x) => x.getName() === selectedView.getName());
          }
          self.viewsLoadingState = LoadingState.LOADED_VALID;
          if( initialView !== null) {
            self.selectView(initialView);
          }
        },
        (error) => {
          self.logger.error("[VirtualizationComponent] Error updating the views for the virtualization: %o", error);
          self.viewsLoadingState = LoadingState.LOADED_INVALID;
          self.tableRows = [];
        }
      );
  }

  /**
   * @returns {boolean} `true` if view being edited is readonly
   */
  public get readOnly(): boolean {
    return !this.editorService.getEditorView() || this.editorService.isReadOnly();
  }

  /**
   * @returns {boolean} `true` if views are being loaded
   */
  public get viewsLoading(): boolean {
    return this.viewsLoadingState === LoadingState.LOADING;
  }

  /**
   * Handles view selection from table
   * @param $event
   */
  public viewSelectionChanged( $event ): void {
    const selectedViews: ViewDefinition[] = $event.selected;
    // If the current view has pending changes, auto save it
    if ( this.editorService.hasChanges() ) {
      this.editorService.saveEditorState();
    }
    this.selectView(selectedViews[0]);
  }

  public get deleteViewButtonEnabled(): boolean {
    return ( this.getSelectedView() !== null );
  }

  private createNewView(viewDefn: ViewDefinition): void {
    const selectedDs = this.selectionService.getSelectedVirtualization();
    const editorId = this.getEditorStateId(selectedDs, viewDefn);

    // Create new editor state to save
    const editorState = new ViewEditorState();
    editorState.setId(editorId);
    editorState.setViewDefinition(viewDefn);

    const editorStates: ViewEditorState[] = [];
    editorStates.push(editorState);

    this.viewsLoadingState = LoadingState.LOADING;

    const self = this;
    this.dataserviceService
      .saveViewEditorStatesRefreshViews(editorStates, selectedDs.getId())
      .subscribe(
        (wasSuccess) => {
          // Add the new ViewDefinition to the table
          self.addViewDefinitionToList(viewDefn);
          self.viewSavedUponCompletion = null;
          self.viewsLoadingState = LoadingState.LOADED_VALID;
        },
        (error) => {
          self.logger.error("[VirtualizationComponent] Error saving the editor state: %o", error);
          self.viewSavedUponCompletion = null;
          self.viewsLoadingState = LoadingState.LOADED_INVALID;
        }
      );
  }

  /**
   * Handle creation of a new View.  Displays the createView dialog,
   * then saves the viewDefinition and adds it to the list
   */
  public onCreateView(): void {
    // Open New View dialog
    const initialState = {
      title: ViewEditorI18n.createViewDialogTitle,
      cancelButtonText: ViewEditorI18n.cancelButtonText,
      okButtonText: ViewEditorI18n.okButtonText
    };

    // Show Dialog, act upon confirmation click
    const modalRef = this.modalService.show(CreateViewDialogComponent, {initialState});
    modalRef.content.okAction.take(1).subscribe((viewDefn) => {
      // If the current view has pending changes, save them first
      if ( this.editorService.hasChanges() ) {
        this.viewSavedUponCompletion = viewDefn;
        this.editorService.saveEditorState();
      } else {
        this.createNewView(viewDefn);
      }
      // addition of a view undeploys active serviceVdb
      this.editorService.undeploySelectedVirtualization();
    });
  }

  /**
   * Construct id for the editor state
   * @param {Dataservice} dataservice the dataservice
   * @param {ViewDefinition} viewDefn the view definition
   * @returns {string} the ID used to persist the editor state
   */
  private getEditorStateId(dataservice: Dataservice, viewDefn: ViewDefinition): string {
    return dataservice.getServiceVdbName().toLowerCase() + "." + viewDefn.getName();
  }

  /**
   * Handle Delete of the selected View
   * @param {string} viewName
   */
  public onDeleteView( ): void {
    const viewName = this.getSelectedView().getName();

    // Dialog Content
    const message = "Do you really want to delete View '" + viewName + "'?";
    const initialState = {
      title: "Confirm Delete",
      bodyContent: message,
      cancelButtonText: "Cancel",
      confirmButtonText: "Delete"
    };

    // Show Dialog, act upon confirmation click
    const modalRef = this.modalService.show(ConfirmDialogComponent, {initialState});
    modalRef.content.confirmAction.take(1).subscribe((value) => {
      this.doDeleteView(viewName);
    });
  }

  /**
   * Deletes the specified ViewEditorState from the userProfile, and removes ViewDefinition from the current list.
   * @param {string} viewDefnName the name of the view
   */
  private doDeleteView(viewDefnName: string): void {
    const selectedViewDefn =  this.tableRows.find((x) => x.getName() === viewDefnName);
    const selectedDs = this.selectionService.getSelectedVirtualization();
    const vdbName = selectedDs.getServiceVdbName();
    const editorStateId = vdbName.toLowerCase() + "." + viewDefnName;
    const dataserviceName = selectedDs.getId();

    this.viewsLoadingState = LoadingState.LOADING;
    // Note: we can only doDelete selected items that we can see in the UI.
    this.logger.debug("[VirtualizationComponent] Deleting selected Virtualization View.");
    const self = this;
    this.dataserviceService
      .deleteViewEditorStateRefreshViews(editorStateId, dataserviceName)
      .subscribe(
        (wasSuccess) => {
          self.removeViewDefinitionFromList(selectedViewDefn);
          // deletion of a view undeploys active serviceVdb
          self.editorService.undeploySelectedVirtualization();
          this.viewsLoadingState = LoadingState.LOADED_VALID;
        },
        (error) => {
          self.logger.error("[VirtualizationComponent] Error deleting the editor state: %o", error);
          this.viewsLoadingState = LoadingState.LOADED_INVALID;
        }
      );
  }

  /*
   * Add the specified ViewDefinition to the view definitions table
   * @param {ViewDefinition} viewDefn the view definition to add
   */
  private addViewDefinitionToList(viewDefn: ViewDefinition): void {
    const newRows: ViewDefinition[] = [];
    newRows.push(viewDefn);
    for ( const row of this.tableRows ) {
      if ( row.getName() !== viewDefn.getName() ) {
        newRows.push( row );
      }
    }
    this.tableRows = newRows.sort( (left, right): number => {
      if (left.getName() < right.getName()) return -1;
      if (left.getName() > right.getName()) return 1;
      return 0;
    });
    this.selectView(viewDefn);
  }

  /*
   * Remove the specified ViewDefinition from the view definitions table
   * @param {ViewDefinition} viewDefn the view definition to remove
   */
  private removeViewDefinitionFromList(viewDefn: ViewDefinition): void {
    const origIndex = this.tableRows.findIndex( ( defn ) => defn.getName() === viewDefn.getName() );

    const newRows: ViewDefinition[] = [];
    for ( const row of this.tableRows ) {
      if ( row.getName() !== viewDefn.getName() ) {
        newRows.push( row );
      }
    }
    this.tableRows = newRows;

    // auto select another row
    if ( this.tableRows.length > origIndex ) {
      this.selectView( this.tableRows[origIndex] );
    } else if ( this.tableRows.length > 0 ) {
      this.selectView( this.tableRows[origIndex - 1] );
    } else if ( this.tableRows.length === 0 ) {
      this.selectView( null );
    }
  }

  private selectView( selView: ViewDefinition ): void {
    // Updates table selection display
    let viewSelection = null;
    if ( selView && selView !== null ) {
      for (const view of this.tableRows) {
        if (view.getName() === selView.getName()) {
          view.setSelected(true);
          viewSelection = view;
        } else {
          view.setSelected(false);
        }
      }
    }
    // Update selection service, then fire event
    this.selectionService.setSelectedViewDefinition(this.selectedVirtualization, viewSelection);
    this.editorService.setEditorView(viewSelection, ViewEditorPart.HEADER);
  }

  private getSelectedView( ): ViewDefinition {
    let selectedView: ViewDefinition = null;
    for (const view of this.tableRows) {
      if (view.selected) {
        selectedView = view;
        break;
      }
    }
    return selectedView;
  }

  /**
   * @returns {string} the view description
   */
  public get viewDescription(): string {
    if ( this.editorService.getEditorView() ) {
      return this.editorService.getEditorView().getDescription();
    }

    return "";
  }

  /**
   * @param {string} newDescription the new description
   */
  public set viewDescription( newDescription: string ) {
    if ( this.editorService.getEditorView() ) {
      if ( newDescription !== this.editorService.getEditorView().getDescription() ) {
        const oldDescription = this.editorService.getEditorView().getDescription();
        const temp = CommandFactory.createUpdateViewDescriptionCommand( newDescription, oldDescription );

        if ( temp instanceof Command ) {
          this.editorService.fireViewStateHasChanged( ViewEditorPart.HEADER, temp as Command );
        } else {
          const error = temp as Error;
          this.logger.error( error.message );
        }
      }
    } else {
      // shouldn't get here as description text input should be disabled if no view being edited
      this.logger.error( "Trying to set description but there is no view being edited" );
    }
  }

  /**
   * Called when text in the view description textarea changes.
   *
   * @param {string} newDescription the new description of the view
   */
  public viewDescriptionChanged( newDescription: string ): void {
    this.viewDescription = newDescription;
  }
}
