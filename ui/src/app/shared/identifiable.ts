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

/**
 * An object that has an identifiable property.
 */
export interface Identifiable< T > {

  /**
   * @typedef { object } T the type of the property that is used to identify the object
   * @param {Identifiable<T>} that the object being compared to
   * @returns {number} 0 if IDs are equal, -1 if this ID is less than, or 1 if this ID is greater than
   */
  compareTo( that: Identifiable< T > ): number;

  /**
   * @typedef { object } T the type of the property that is used to identify the object
   * @returns {T} the object identifier (can be null)
   */
  getId(): T;

}
