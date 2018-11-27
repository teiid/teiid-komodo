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

import { Injectable } from "@angular/core";
import { Http } from "@angular/http";
import { ApiService } from "@core/api.service";
import { AppSettingsService } from "@core/app-settings.service";
import { LoggerService } from "@core/logger.service";
import { NotifierService } from "@dataservices/shared/notifier.service";
import { VdbModelSource } from "@dataservices/shared/vdb-model-source.model";
import { VdbModel } from "@dataservices/shared/vdb-model.model";
import { VdbStatus } from "@dataservices/shared/vdb-status.model";
import { Vdb } from "@dataservices/shared/vdb.model";
import { VdbsConstants } from "@dataservices/shared/vdbs-constants";
import { Virtualization } from "@dataservices/shared/virtualization.model";
import { environment } from "@environments/environment";
import { Observable } from "rxjs/Rx";
import { Subscription } from "rxjs/Subscription";
import { QueryResults } from "@dataservices/shared/query-results.model";

@Injectable()
/**
 * VdbService
 */
export class VdbService extends ApiService {

  protected deploymentSubscription: Subscription;
  protected notifierService: NotifierService;
  private http: Http;

  constructor(http: Http, appSettings: AppSettingsService,
              notifierService: NotifierService, logger: LoggerService ) {
    super( appSettings, logger  );
    this.http = http;
    this.notifierService = notifierService;
  }

  /**
   * Get the vdbs from the komodo rest interface
   * @returns {Observable<Vdb[]>}
   */
  public getVdbs(): Observable<Vdb[]> {
    return this.http
      .get(environment.komodoWorkspaceUrl + VdbsConstants.vdbsRootPath, this.getAuthRequestOptions())
      .map((response) => {
        const vdbs = response.json();
        return vdbs.map((vdb) => Vdb.create( vdb ));
      })
      .catch( ( error ) => this.handleError( error ) );
  }

  /**
   * Determine if the workspace has a vdb with the supplied name
   * @param {string} vdbName the name of the VDB
   * @returns {Observable<boolean>}
   */
  public hasWorkspaceVdb(vdbName: string): Observable<boolean> {
    return this.http
      .get(environment.komodoWorkspaceUrl + VdbsConstants.vdbsRootPath + "/" + vdbName, this.getAuthRequestOptions())
      .map((response) => {
          return response.ok;
      })
      .catch((error) => {
        // VDB not found returns a 404
        if (error.status === 404) {
          return Observable.of(false);
        }
        this.handleError( error );
      } );
  }

  /**
   * Determine if the workspace has the specified vdb model view
   * @param {string} vdbName the name of the VDB
   * @param {string} modelName the name of the Model
   * @param {string} viewName the name of the View
   * @returns {Observable<boolean>}
   */
  public hasWorkspaceVdbModelView(vdbName: string, modelName: string, viewName: string): Observable<boolean> {
    const url = environment.komodoWorkspaceUrl + VdbsConstants.vdbsRootPath + "/" + vdbName
                                               + VdbsConstants.vdbModelsRootPath + "/" + modelName + "/Views/" + viewName;

    return this.http
      .get(url, this.getAuthRequestOptions())
      .map((response) => {
        return response.ok;
      })
      .catch((error) => {
        // VDB not found returns a 404
        if (error.status === 404) {
          return Observable.of(false);
        }
        this.handleError( error );
      } );
  }

  /**
   * Get the status of any deployed vdbs
   * @returns {Observable<Vdb[]>}
   */
  public getTeiidVdbStatuses(): Observable<VdbStatus[]> {
    return this.http
      .get(environment.komodoTeiidUrl + VdbsConstants.statusPath + VdbsConstants.vdbsRootPath, this.getAuthRequestOptions())
      .map((response) => {
        const vdbStatuses = response.json();
        return vdbStatuses.vdbs.map((vdbStatus) => VdbStatus.create( vdbStatus ));
      })
      .catch( ( error ) => this.handleError( error ) );
  }

  /**
   * Get the status of all published vdbs (virtualizations)
   * @returns {Observable<Vdb[]>}
   */
  public getVirtualizations(): Observable<Virtualization[]> {
    return this.http
      .get(environment.komodoTeiidUrl + "/" + VdbsConstants.vdbPublish, this.getAuthRequestOptions())
      .map((response) => {
        const virtuals = response.json();
        return virtuals.map((virtualStatus) => Virtualization.create( virtualStatus ));
      })
      .catch( ( error ) => this.handleError( error ) );
  }

  /**
   * Validates the specified view name within the specified vdb model. If the name contains valid characters
   * and the name is unique, the service returns 'null'. Otherwise, a 'string' containing an error message is returned.
   *
   * @param {string} vdbName the vdb name
   * @param {string} modelName the model name
   * @param {string} viewName the view name
   * @returns {Observable<String>}
   */
  public isValidViewName( vdbName: string, modelName: string, viewName: string ): Observable< string > {
    // Check that valid names were supplied
    if ( !vdbName || vdbName.length === 0 ) {
      return Observable.of( "VDB name cannot be empty" );
    }
    if ( !modelName || modelName.length === 0 ) {
      return Observable.of( "Model name cannot be empty" );
    }
    if ( !viewName || viewName.length === 0 ) {
      return Observable.of( "View name cannot be empty" );
    }

    const url = environment.komodoWorkspaceUrl + "/vdbs/" + vdbName + "/Models/" + modelName +
                                                 "/Views/nameValidation/" + encodeURIComponent( viewName );

    return this.http.get( url, this.getAuthRequestOptions() )
      .map( ( response ) => {
        if ( response.ok ) {
          if ( response.text() ) {
            return response.text();
          }

          return "";
        } } )
      .catch( ( error ) => this.handleError( error ) );
  }

  /**
   * Query the vdb via the komodo rest interface
   * @param {string} query the SQL query
   * @param {string} vdbName the vdb name
   * @param {number} limit the limit for the number of result rows
   * @param {number} offset the offset for the result rows
   * @returns {Observable<boolean>}
   */
  public queryVdb(query: string, vdbName: string, limit: number, offset: number): Observable<any> {
    // The payload for the rest call
    const payload = {
      "query": query,
      "target": vdbName,
      "limit": limit,
      "offset": offset
    };

    const url = environment.komodoTeiidUrl + "/query";

    return this.http
      .post(url, payload, this.getAuthRequestOptions())
      .map((response) => {
        const queryResults = response.json();
        return new QueryResults(queryResults);
      })
      .catch( ( error ) => this.handleError( error ) );
  }

  /**
   * Create a vdb via the komodo rest interface
   * @param {Vdb} vdb
   * @returns {Observable<boolean>}
   */
  public createVdb(vdb: Vdb): Observable<boolean> {
    return this.http
      .post(environment.komodoWorkspaceUrl + VdbsConstants.vdbsRootPath + "/" + vdb.getId(),
        vdb, this.getAuthRequestOptions())
      .map((response) => {
        return response.ok;
      })
      .catch( ( error ) => this.handleError( error ) );
  }

  /**
   * Create a vdb via the komodo rest interface
   * @param {string} vdbName
   * @param {VdbModel} vdbModel
   * @returns {Observable<boolean>}
   */
  public createVdbModel(vdbName: string, vdbModel: VdbModel): Observable<boolean> {
    const str = JSON.stringify(vdbModel);
    return this.http
      .post(environment.komodoWorkspaceUrl + VdbsConstants.vdbsRootPath + "/" + vdbName
                                               + VdbsConstants.vdbModelsRootPath + "/" + vdbModel.getId(),
        str, this.getAuthRequestOptions())
      .map((response) => {
        return response.ok;
      })
      .catch( ( error ) => this.handleError( error ) );
  }

  /**
   * Create a vdbModelSource via the komodo rest interface
   * @param {string} vdbName the vdb name
   * @param {string} modelName the model name
   * @param {VdbModelSource} vdbModelSource the modelsource name
   * @returns {Observable<boolean>}
   */
  public createVdbModelSource(vdbName: string, modelName: string, vdbModelSource: VdbModelSource): Observable<boolean> {
    return this.http
      .post(environment.komodoWorkspaceUrl + VdbsConstants.vdbsRootPath + "/" + vdbName
        + VdbsConstants.vdbModelsRootPath + "/" + modelName
        + VdbsConstants.vdbModelSourcesRootPath + "/" + vdbModelSource.getId(),
        vdbModelSource, this.getAuthRequestOptions())
      .map((response) => {
        return response.ok;
      })
      .catch( ( error ) => this.handleError( error ) );
  }

  /**
   * Create a vdb model view via the komodo rest interface
   * @param {string} vdbName
   * @param {string} modelName
   * @param {string} viewName
   * @returns {Observable<boolean>}
   */
  public createVdbModelView(vdbName: string, modelName: string, viewName: string): Observable<boolean> {
    // The payload for the rest call
    const userWorkspacePath = this.getKomodoUserWorkspacePath();
    const payload = {
      "keng__id": viewName,
      "keng__kType": "View",
      "keng__dataPath": userWorkspacePath + "/" + vdbName + "/" + modelName + "/" + viewName,
    };

    const url = environment.komodoWorkspaceUrl + VdbsConstants.vdbsRootPath + "/" + vdbName
                                               + VdbsConstants.vdbModelsRootPath + "/" + modelName + "/Views/" + viewName;
    const paystr = JSON.stringify(payload);

    return this.http
      .post(url, paystr, this.getAuthRequestOptions())
      .map((response) => {
        return response.ok;
      })
      .catch( ( error ) => this.handleError( error ) );
  }

  /**
   * Delete a vdb via the komodo rest interface
   * @param {string} vdbId
   * @returns {Observable<boolean>}
   */
  public deleteVdb(vdbId: string): Observable<boolean> {
    return this.http
      .delete(environment.komodoWorkspaceUrl + VdbsConstants.vdbsRootPath + "/" + vdbId,
               this.getAuthRequestOptions())
      .map((response) => {
        return response.ok;
      })
      .catch( ( error ) => this.handleError( error ) );
  }

  /**
   * Deploys the workspace VDB with the provided name
   * @param {string} vdbName
   * @returns {Observable<boolean>}
   */
  public deployVdb(vdbName: string): Observable<boolean> {
    const vdbPath = this.getKomodoUserWorkspacePath() + "/" + vdbName;
    return this.http
      .post(environment.komodoTeiidUrl + VdbsConstants.vdbRootPath,
        { path: vdbPath}, this.getAuthRequestOptions())
      .map((response) => {
        const status = response.json();
        if (status.Information.deploymentSuccess !== "true") {
          this.handleError(response);
        }

        return status.Information.deploymentSuccess === "true";
      })
      .catch( ( error ) => this.handleError( error ) );
  }

  /**
   * Undeploy a vdb from the teiid server
   * @param {string} vdbId
   * @returns {Observable<boolean>}
   */
  public undeployVdb(vdbId: string): Observable<boolean> {
    return this.http
      .delete(environment.komodoTeiidUrl + VdbsConstants.vdbsRootPath + "/" + vdbId,
        this.getAuthRequestOptions())
      .map((response) => {
        return response.ok;
      })
      .catch( ( error ) => this.handleError( error ) );
  }

  /**
   * Polls the server for the specified VDB.  Polling will terminate if
   * (1) The VDB is active
   * (2) The VDB is in a failed state
   * (3) The polling duration has lapsed
   * @param {string} vdbName the name of the VDB
   * @param {number} pollDurationSec the duration (sec) to poll the server
   * @param {number} pollIntervalSec the interval (sec) between polling attempts
   */
  public pollForActiveVdb(vdbName: string, pollDurationSec: number, pollIntervalSec: number): void {
    const pollIntervalMillis = pollIntervalSec * 1000;
    const pollIterations = pollDurationSec / pollIntervalSec;

    let pollCount = 0;
    const self = this;
    // start a timer after one second
    const timer = Observable.timer(1000, pollIntervalMillis);
    this.deploymentSubscription = timer.subscribe((t: any) => {
      this.getTeiidVdbStatuses()
        .subscribe(
          (resp) => {
            for ( const vdbStatus of resp ) {
              if ( vdbStatus.getName() !== vdbName ) {
                continue;
              }
              if ( vdbStatus.isActive() ) {
                this.notifierService.sendVdbDeploymentStatus(vdbStatus);
                self.deploymentSubscription.unsubscribe();
              } else if ( vdbStatus.isFailed() ) {
                this.notifierService.sendVdbDeploymentStatus(vdbStatus);
                self.deploymentSubscription.unsubscribe();
              }
            }
            pollCount++;
            if (pollCount > pollIterations) {
              // Timed out status
              const status: VdbStatus = new VdbStatus();
              status.setName(vdbName);
              status.setActive(false);
              status.setLoading(false);
              status.setFailed(true);
              const errors: string[] = [];
              errors.push("Deployment polling timed out");
              status.setErrors(errors);
              // broadcast the status
              this.notifierService.sendVdbDeploymentStatus(status);
              self.deploymentSubscription.unsubscribe();
            }
          },
          (error) => {
            // Error status
            const status: VdbStatus = new VdbStatus();
            status.setName(vdbName);
            status.setActive(false);
            status.setLoading(false);
            status.setFailed(true);
            const errors: string[] = [];
            errors.push("Deployment failed");
            status.setErrors(errors);
            // Broadcast the status
            this.notifierService.sendVdbDeploymentStatus(status);
            self.deploymentSubscription.unsubscribe();
          }
        );
    });
  }

  /**
   * Deletes the workspace VDB if found.  Checks the workspace first, before attempting the delete.
   * If the VDB is not found the delete attempt is skipped.
   * @param {string} vdbName the name of the vdb
   * @returns {Observable<boolean>}
   */
  public deleteVdbIfFound(vdbName: string): Observable<boolean> {
    return this.hasWorkspaceVdb(vdbName)
      .switchMap( (resp) => {
        if (resp === true) {
          return this.deleteVdb(vdbName);
        } else {
          return Observable.of(true);
        }
      });
  }

  /**
   * Delete a view within a vdb model via the komodo rest interface
   * @param {string} vdbName the vdb name
   * @param {string} modelName the model name
   * @param {string} viewName the view name
   * @returns {Observable<boolean>} 'true' if successful
   */
  public deleteView(vdbName: string, modelName: string, viewName: string): Observable<boolean> {
    return this.http
      .delete(environment.komodoWorkspaceUrl + VdbsConstants.vdbsRootPath + "/" + vdbName
                                                 + "/Models/" + modelName + "/Views/" + viewName,
        this.getAuthRequestOptions())
      .map((response) => {
        return response.ok;
      })
      .catch( ( error ) => this.handleError( error ) );
  }

  /**
   * Deletes the workspace VDB model view if found.  Checks the workspace first, before attempting the delete.
   * If the View is not found the delete attempt is skipped.
   * @param {string} vdbName the name of the vdb
   * @param {string} modelName the name of the model
   * @param {string} viewName the name of the view
   * @returns {Observable<boolean>}
   */
  public deleteViewIfFound(vdbName: string, modelName: string, viewName: string): Observable<boolean> {
    return this.hasWorkspaceVdbModelView(vdbName, modelName, viewName)
      .switchMap( (resp) => {
        if (resp === true) {
          return this.deleteView(vdbName, modelName, viewName);
        } else {
          return Observable.of(true);
        }
      });
  }

}
