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

export class ObjectUtils {

  public static isNullOrUndefined( object: any ): boolean {
    return object === undefined || object === null;
  }

  public static objectEquals( x: any, y: any ): boolean {
    if ( x === null || x === undefined || y === null || y === undefined ) {
      return x === y;
    }
    // after this just checking type of one would be enough
    if ( x.constructor !== y.constructor ) {
      return false;
    }
    // if they are functions, they should exactly refer to same one (because of closures)
    if ( x instanceof Function ) {
      return x === y;
    }
    // if they are regexps, they should exactly refer to same one (it is hard to better equality check on current ES)
    if ( x instanceof RegExp ) {
      return x === y;
    }
    if ( x === y || x.valueOf() === y.valueOf() ) {
      return true;
    }
    if ( Array.isArray( x ) && x.length !== y.length ) {
      return false;
    }

    // if they are dates, they must had equal valueOf
    if ( x instanceof Date ) {
      return false;
    }

    // if they are strictly equal, they both need to be object at least
    if ( !(x instanceof Object) ) {
      return false;
    }
    if ( !(y instanceof Object) ) {
      return false;
    }

    const p = Object.keys( x );
    return Object.keys( y ).every( (i) => p.indexOf( i ) !== -1 ) &&
      p.every( (i) => ObjectUtils.objectEquals( x[ i ], y[ i ] ) );
  }

}
