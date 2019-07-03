/*
 * Copyright Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags and
 * the COPYRIGHT.txt file distributed with this work.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.komodo.core.internal.sequencer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.Session;

import org.modeshape.common.text.ParsingException;
import org.modeshape.common.util.IoUtil;
import org.teiid.modeshape.sequencer.ddl.DdlParser;
import org.teiid.modeshape.sequencer.ddl.StandardDdlLexicon;
import org.teiid.modeshape.sequencer.ddl.TeiidDdlParser;
import org.teiid.modeshape.sequencer.ddl.TeiidDdlSequencer;
import org.teiid.modeshape.sequencer.ddl.node.AstNode;
import org.teiid.modeshape.sequencer.ddl.node.AstNodeFactory;

/**
 * Subclass that only allows the Teiid DDL dialect, avoiding confusion with other DDL parsers.
 */
public class KDdlSequencer extends TeiidDdlSequencer {

    private final DdlParser teiidParser = new TeiidDdlParser();

    @Override
    protected List<DdlParser> getParserList() {
        return Collections.singletonList(teiidParser);
    }

    @Override
    public boolean execute(Property inputProperty, Node outputNode, Context context) throws Exception {
        if (! super.execute(inputProperty, outputNode, context)) {

            //
            // We know the sequencer failed to execute but unforunately the parsing exception
            // are handled and simply pushed to the logger. We want to throw them back up to
            // the calling transaction so they get some visibility
            //

            AstNodeFactory nodeFactory = new AstNodeFactory();
            final AstNode tempNode = nodeFactory.node(StandardDdlLexicon.STATEMENTS_CONTAINER);
            Binary ddlContent = inputProperty.getBinary();

            try (InputStream stream = ddlContent.getStream()) {
                teiidParser.parse(IoUtil.read(stream), tempNode, null);
            } catch (ParsingException e) {
                throw new Exception(e);
            } catch (IOException e) {
                throw new Exception(e);
            }

            //
            // Something went wrong but clearly not a parsing exception
            //
            return false;
        }

        if (! outputNode.hasNode(StandardDdlLexicon.STATEMENTS_CONTAINER))
            return false;

        Node ddlStmtsNode = outputNode.getNode(StandardDdlLexicon.STATEMENTS_CONTAINER);
        NodeIterator children = ddlStmtsNode.getNodes();

        Session session = ddlStmtsNode.getSession();
        if (! session.isLive())
            return false;

        while (children.hasNext()) {
            Node child = children.nextNode();
            session.move(child.getPath(), outputNode.getPath() + "/" + child.getName());
            if (! outputNode.hasNode(child.getName()))
                throw new Exception("Failed to move DDL sequence node to output node");
        }

        session.removeItem(ddlStmtsNode.getPath());
        return outputNode.hasNodes();
    }

}
