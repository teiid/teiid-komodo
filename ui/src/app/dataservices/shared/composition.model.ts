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

import { CompositionType } from "@dataservices/shared/composition-type.enum";
import { CompositionOperator } from "@dataservices/shared/composition-operator.enum";
import { PathUtils } from "@dataservices/shared/path-utils";

/**
 * Composition model
 */
export class Composition {

  private name: string;
  private initialSourcePath: string;
  private leftSourcePath: string;
  private rightSourcePath: string;
  private leftCriteriaColumn: string;
  private rightCriteriaColumn: string;
  private type: CompositionType = CompositionType.INNER_JOIN;
  private operator: CompositionOperator = CompositionOperator.EQ;

  /**
   * @param {Object} json the JSON representation of a Composition
   * @returns {Composition} the new Composition (never null)
   */
  public static create( json: object = {} ): Composition {
    const composition = new Composition();
    composition.setValues( json );
    return composition;
  }

  constructor() {
    // nothing to do
  }

  /**
   * @returns {string} the composition name
   */
  public getName(): string {
    return this.name;
  }

  /**
   * @param {string} name the composition name
   */
  public setName( name?: string ): void {
    this.name = name ? name : null;
  }

  /**
   * @returns {string} the composition type
   */
  public getType(): CompositionType {
    return this.type;
  }

  /**
   * @param {string} type the composition type
   */
  public setType( type: CompositionType ): void {
    this.type = type;
  }

  /**
   * @returns {CompositionOperator} the composition operator
   */
  public getOperator(): CompositionOperator {
    return this.operator;
  }

  /**
   * @param {CompositionOperator} operator the composition operator
   */
  public setOperator(operator: CompositionOperator ): void {
    this.operator = operator;
  }

  /**
   * @return {boolean} 'true' if initial source is set and on the left
   */
  public get initialSourceOnLeft(): boolean {
    return this.initialSourcePath !== null && this.leftSourcePath !== null && this.initialSourcePath === this.leftSourcePath;
  }

  /**
   * @return {boolean} 'true' if initial source is set and on the right
   */
  public get initialSourceOnRight(): boolean {
    return this.initialSourcePath !== null && this.rightSourcePath !== null && this.initialSourcePath === this.rightSourcePath;
  }

  /**
   * @returns {string} the left source path of the composition
   */
  public getLeftSourcePath(): string {
    return this.leftSourcePath;
  }

  /**
   * @param {string} sourcePath the left source path of the composition
   */
  public setLeftSourcePath(sourcePath: string, isInitialSource = false ): void {
    this.leftSourcePath = sourcePath;
    if (isInitialSource) {
      this.initialSourcePath = this.leftSourcePath;
    }
  }

  /**
   * @returns {string} the right source source of the composition
   */
  public getRightSourcePath(): string {
    return this.rightSourcePath;
  }

  /**
   * @param {string} sourcePath the right source of the composition
   */
  public setRightSourcePath(sourcePath: string, isInitialSource = false ): void {
    this.rightSourcePath = sourcePath;
    if (isInitialSource) {
      this.initialSourcePath = this.rightSourcePath;
    }
  }

  /**
   * @returns {string} the left criteria column
   */
  public getLeftCriteriaColumn(): string {
    return this.leftCriteriaColumn;
  }

  /**
   * @param {string} column the left criteria column
   */
  public setLeftCriteriaColumn(column: string ): void {
    this.leftCriteriaColumn = column;
  }

  /**
   * @returns {string} the right criteria column
   */
  public getRightCriteriaColumn(): string {
    return this.rightCriteriaColumn;
  }

  /**
   * @param {string} column the right criteria column
   */
  public setRightCriteriaColumn(column: string ): void {
    this.rightCriteriaColumn = column;
  }

  /**
   * Swap the left and right sources (and corresponding column selections)
   */
  public swapTables( ): void {
    // Switch the left and right tables
    const leftSource = this.getLeftSourcePath();
    const rightSource = this.getRightSourcePath();
    this.setRightSourcePath(leftSource);
    this.setLeftSourcePath(rightSource);

    // Switch the column selections
    const selectedLeftColumn = this.getLeftCriteriaColumn();
    const selectedRightColumn = this.getRightCriteriaColumn();
    this.setRightCriteriaColumn(selectedLeftColumn);
    this.setLeftCriteriaColumn(selectedRightColumn);
  }

  /**
   * Determine whether the composition is in a complete state
   * @returns {boolean} true if complete
   */
  public get complete(): boolean {
    if ( this.name !== null &&
         this.type && this.type !== null &&
         this.operator && this.operator !== null &&
         this.leftSourcePath && this.leftSourcePath !== null &&
         this.rightSourcePath && this.rightSourcePath !== null &&
         this.leftCriteriaColumn && this.leftCriteriaColumn !== null &&
         this.rightCriteriaColumn && this.rightCriteriaColumn !== null) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Determine if the supplied Composition is equal to this
   * @param {Object} values
   */
  public isEqual( otherComp: Composition ): boolean {
    let equal = false;
    if (this.getName() === otherComp.getName() &&
      this.getLeftSourcePath() === otherComp.getLeftSourcePath() &&
      this.getRightSourcePath() === otherComp.getRightSourcePath() &&
      this.getLeftCriteriaColumn() === otherComp.getLeftCriteriaColumn() &&
      this.getRightCriteriaColumn() === otherComp.getRightCriteriaColumn() &&
      this.getType() === otherComp.getType() &&
      this.getOperator() === otherComp.getOperator() ) {
      equal = true;
    }
    return equal;
  }

  /**
   * Set all object values using the supplied View json
   * @param {Object} values
   */
  public setValues(values: object = {}): void {
    Object.assign(this, values);
  }

  /**
   * @returns {{}} a JSON representation of the composition
   */
  public toJSON(): {} {
    return {
      name: this.name,
      leftSourcePath: this.leftSourcePath,
      rightSourcePath: this.rightSourcePath,
      leftCriteriaColumn: this.leftCriteriaColumn,
      rightCriteriaColumn: this.rightCriteriaColumn,
      type: this.type,
      operator: this.operator
    };
  }

  public toString(): string {
    return JSON.stringify(this.toJSON());
  }

  public getLeftSourceDisplay(): string {
    const leftSrcPath = this.getLeftSourcePath();
    const leftConn = PathUtils.getConnectionName(leftSrcPath);
    const leftSrc = PathUtils.getSourceName(leftSrcPath);
    return "[" + leftConn + "] " + leftSrc;
  }

  public getRightSourceDisplay(): string {
    const rightSrcPath = this.getRightSourcePath();
    const rightConn = PathUtils.getConnectionName(rightSrcPath);
    const rightSrc = PathUtils.getSourceName(rightSrcPath);
    return "[" + rightConn + "] " + rightSrc;
  }

  public getCriteriaDisplay(): string {
    const leftColumn = this.getLeftCriteriaColumn();
    const rightColumn = this.getRightCriteriaColumn();
    return leftColumn + " " + CompositionOperator.toSql(this.getOperator()) + " " + rightColumn;
  }

}
