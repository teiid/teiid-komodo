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

import { Command } from "../../../../dataservices/virtualization/view-editor/command/command";
import { AddCompositionCommand } from "../../../../dataservices/virtualization/view-editor/command/add-composition-command";
import { AddSourcesCommand } from "../../../../dataservices/virtualization/view-editor/command/add-sources-command";
import { UpdateViewDescriptionCommand } from "../../../../dataservices/virtualization/view-editor/command/update-view-description-command";
import { UpdateViewNameCommand } from "../../../../dataservices/virtualization/view-editor/command/update-view-name-command";
import { RemoveSourcesCommand } from "../../../../dataservices/virtualization/view-editor/command/remove-sources-command";
import { SchemaNode } from "../../../../connections/shared/schema-node.model";
import { NoOpCommand } from "../../../../dataservices/virtualization/view-editor/command/no-op-command";
import { Undoable } from "../../../../dataservices/virtualization/view-editor/command/undo-redo/undoable";
import { Composition } from "../../../../dataservices/shared/composition.model";
import { RemoveCompositionCommand } from "../../../../dataservices/virtualization/view-editor/command/remove-composition-command";
import { UpdateProjectedColumnsCommand } from "../../../../dataservices/virtualization/view-editor/command/update-projected-columns-command";

export class CommandFactory {

  /**
   * @param  addedSources the JSON representation of or the schema nodes of the sources being added
   * @returns  the add sources command or a no op command if sources are empty
   */
  public static createAddSourcesCommand( addedSources: string | SchemaNode[], id?: string ): Command {
    if ( !addedSources || addedSources.length === 0 ) {
      return NoOpCommand.NO_OP;
    }

    return new AddSourcesCommand( addedSources, id );
  }

  /**
   * @param  addedSources the JSON representation of or the composition being added
   */
  public static createAddCompositionCommand( addedComposition: string | Composition, id?: string ): Command {
    if ( !addedComposition || addedComposition === null ) {
      return NoOpCommand.NO_OP;
    }

    return new AddCompositionCommand( addedComposition, id );
  }

  public static createNoOpCommand(): Command {
    return NoOpCommand.NO_OP;
  }

  /**
   * @param  removedSources the JSON representation of or the schema nodes of the sources being removed
   */
  public static createRemoveSourcesCommand( removedSources: string | SchemaNode[], id: string ): Command {
    if ( !removedSources || removedSources.length === 0 ) {
      return NoOpCommand.NO_OP;
    }

    return new RemoveSourcesCommand( removedSources, id );
  }

  /**
   * @param  removedSources the JSON representation of or the schema nodes of the sources being removed
   */
  public static createRemoveCompositionCommand( removedComposition: string | Composition, id: string ): Command {
    if ( !removedComposition || removedComposition === null ) {
      return NoOpCommand.NO_OP;
    }

    return new RemoveCompositionCommand( removedComposition, id );
  }

  /**
   *
   * @param  cmd the command whose undoable is being requested
   */
  public static createUndoable( cmd: Command ): Undoable | Error {
    if ( cmd.isUndoable() ) {
      const undoOrError = CommandFactory.createUndoCommand( cmd );

      if ( undoOrError instanceof Command ) {
        return new Undoable( undoOrError as Command, cmd );
      }

      return undoOrError;
    }

    return new Error( "The '" + cmd.id + "' command does not have an undo command" );
  }

  /**
   * @param  cmd the command whose undo command is being requested
   */
  public static createUndoCommand( cmd: Command ): Command | Error {
    switch ( cmd.id ) {
      case AddSourcesCommand.id: {
        const value = cmd.getArg( AddSourcesCommand.addedSourcePaths );
        const id = cmd.getArg(Command.identArg);
        return CommandFactory.createRemoveSourcesCommand( value, id );
      }
      case RemoveSourcesCommand.id: {
        const value = cmd.getArg( RemoveSourcesCommand.removedSourcePaths );
        return CommandFactory.createAddSourcesCommand( value );
      }
      case AddCompositionCommand.id: {
        const value = cmd.getArg( AddCompositionCommand.addedComposition );
        const id = cmd.getArg(Command.identArg);
        return CommandFactory.createRemoveCompositionCommand( value, id );
      }
      case RemoveCompositionCommand.id: {
        const value = cmd.getArg( RemoveCompositionCommand.removedComposition );
        return CommandFactory.createAddCompositionCommand( value );
      }
      case UpdateViewDescriptionCommand.id: {
        return CommandFactory.createUpdateViewDescriptionCommand( cmd.getArg( UpdateViewDescriptionCommand.oldDescription ),
                                                                  cmd.getArg( UpdateViewDescriptionCommand.newDescription ) );
      }
      case UpdateViewNameCommand.id: {
        return CommandFactory.createUpdateViewNameCommand( cmd.getArg( UpdateViewNameCommand.oldName ),
          cmd.getArg( UpdateViewNameCommand.newName ) );
      }
      case UpdateProjectedColumnsCommand.id: {
        return CommandFactory.createUpdateProjectedColumnsCommand( cmd.getArg( UpdateProjectedColumnsCommand.oldProjectedColumns ),
                                                                   cmd.getArg( UpdateProjectedColumnsCommand.newProjectedColumns ) );
      }
      default: {
        return new Error( "The '" + cmd.id + "' command does not have an undo command" );
      }
    }
  }

  /**
   * @param  newDescription the new view description (can be `null` or empty if old description has a value)
   * @param  oldDescription the view description being replaced (can be `null` or empty if new description has a value)
   */
  public static createUpdateViewDescriptionCommand( newDescription: string,
                                                    oldDescription: string ): Command | Error {
    if ( newDescription || oldDescription ) {
      if ( newDescription === oldDescription ) {
        return CommandFactory.createNoOpCommand();
      }

      return new UpdateViewDescriptionCommand( newDescription, oldDescription );
    }

    return new Error( "Must have either a new description or an old description when creating an UpdateViewDescriptionCommand" );
  }

  /**
   * @param  newName the new view name (can be `null` or empty if the old name has a value)
   * @param  oldName the view name being replaced (can be `null` or empty if the new name has a value)
   */
  public static createUpdateViewNameCommand( newName: string,
                                             oldName: string ): Command | Error {
    if ( newName || oldName ) {
      if ( newName === oldName ) {
        return CommandFactory.createNoOpCommand();
      }

      return new UpdateViewNameCommand( newName, oldName );
    }

    return new Error( "Must have either a new name or an old name when creating an UpdateViewNameCommand" );
  }

  /**
   * @param  newProjColumns the stringified representation of or the new projected columns
   * @param  oldProjColumns the stringified representation of or the old projected columns
   * @returns  the update projected columns command or a no op command if args are not supplied
   */
  public static createUpdateProjectedColumnsCommand( newProjColumns: string,
                                                     oldProjColumns: string ): Command {
    if ( !newProjColumns || newProjColumns === null || !oldProjColumns || oldProjColumns === null ) {
      return NoOpCommand.NO_OP;
    }

    return new UpdateProjectedColumnsCommand( newProjColumns, oldProjColumns );
  }

  /**
   * Constructs a command object from JSON.
   *
   * @param  json the JSON being converted to a command
   * @returns  the decoded command or an error if one could not be constructed
   */
  public static decode( json: object = {} ): Command | Error {
    const cmdId = json[ Command.idPropJson ];
    const args = json[ Command.argsPropJson ];

    switch ( cmdId ) {
      case AddSourcesCommand.id: {
        const addSourcePaths = args[ AddSourcesCommand.addedSourcePaths ];
        const addIdent = args[ Command.identArg ];

        if ( addSourcePaths )
          return CommandFactory.createAddSourcesCommand( addSourcePaths, addIdent );

        return new Error( "Unable to decode AddSourcesCommand: " + json );
      }
      case RemoveSourcesCommand.id: {
        const removedSourcePaths = args[ RemoveSourcesCommand.removedSourcePaths ];
        const removedIdent = args[ Command.identArg ];

        if (removedSourcePaths && removedIdent) {
          return CommandFactory.createRemoveSourcesCommand(removedSourcePaths, removedIdent);
        }

        return new Error( "Unable to decode RemoveSourcesCommand: " + json );
      }
      case AddCompositionCommand.id: {
        const addComposition = args[ AddCompositionCommand.addedComposition ];
        const addIdent = args[ Command.identArg ];

        if ( addComposition )
          return CommandFactory.createAddCompositionCommand( addComposition, addIdent );

        return new Error( "Unable to decode AddCompositionCommand: " + json );
      }
      case RemoveCompositionCommand.id: {
        const removedComposition = args[ RemoveCompositionCommand.removedComposition ];
        const removedIdent = args[ Command.identArg ];

        if (removedComposition && removedIdent) {
          return CommandFactory.createRemoveCompositionCommand(removedComposition, removedIdent);
        }

        return new Error( "Unable to decode RemoveCompositionCommand: " + json );
      }
      case UpdateViewDescriptionCommand.id: {
        const newViewDescription = args[ UpdateViewDescriptionCommand.newDescription ];
        const replacedViewDescription = args[ UpdateViewDescriptionCommand.oldDescription ];

        if ( newViewDescription || replacedViewDescription ) {
          return CommandFactory.createUpdateViewDescriptionCommand( newViewDescription, replacedViewDescription );
        }

        return new Error( "Unable to decode UpdateViewDescriptionCommand: " + json );
      }
      case UpdateViewNameCommand.id: {
        const newViewName = args[ UpdateViewNameCommand.newName ];
        const replacedViewName = args[UpdateViewNameCommand.oldName];

        if ( newViewName || replacedViewName ) {
          return CommandFactory.createUpdateViewNameCommand( newViewName, replacedViewName );
        }

        return new Error( "Unable to decode UpdateViewNameCommand: " + json );
      }
      case UpdateProjectedColumnsCommand.id: {
        const newProjectedCols = args[ UpdateProjectedColumnsCommand.newProjectedColumns ];
        const replacedProjectedCols = args[ UpdateProjectedColumnsCommand.oldProjectedColumns ];

        if ( newProjectedCols || replacedProjectedCols ) {
          return CommandFactory.createUpdateProjectedColumnsCommand( newProjectedCols, replacedProjectedCols );
        }

        return new Error( "Unable to decode UpdateProjectedColumnsCommand: " + json );
      }
      default: {
        return new Error( "Unhandled command: " + cmdId );
      }
    }
  }

  /**
   * @param  json the JSON representation of a Undoable
   */
  public static decodeUndoable( json: object = {} ): Undoable | Error {
    let undo: Command = null;
    let redo: Command = null;
    for (const field of Object.keys(json)) {
      if (field === "undo") {
        // length of 2 or shorter - no object.  TODO: better way to do this?
        if (JSON.stringify(json[field]).length > 2) {
          const temp = CommandFactory.decode( json[ field ] );

          if ( temp instanceof Command ) {
            undo = temp;
          } else {
            return new Error( "Unable to decode undo command: " + temp );
          }
        }
      } else if (field === "redo") {
        // length of 2 or shorter - no object.  TODO: better way to do this?
        if (JSON.stringify(json[field]).length > 2) {
          const temp = CommandFactory.decode( json[ field ] );

          if ( temp instanceof Command ) {
            redo = temp;
          } else {
            return new Error( "Unable to decode redo command: " + temp );
          }
        }
      }
    }

    if (!undo || !redo) {
      return new Error( "Unable to create undoable" );
    }

    return new Undoable(undo, redo);
  }

}