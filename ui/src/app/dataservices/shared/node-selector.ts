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

import { SchemaNode } from "@connections/shared/schema-node.model";

/**
 * The node selector interface
 */
export interface NodeSelector {

  /**
   * Determine if any nodes are currently selected
   * @returns {boolean} true if one or more nodes are selected
   */
  hasSelectedNodes( ): boolean;

  /**
   * Get the array of currently selected SchemaNodes
   * @returns {SchemaNode[]} the array of selected SchemaNodes (never null, but may be empty)
   */
  getSelectedNodes(): SchemaNode[];

  /**
   * Deselect the node if a node with the same name is currently selected.
   * @param {SchemaNode} node the table to deselect
   */
  deselectNode(node: SchemaNode): void;

}
