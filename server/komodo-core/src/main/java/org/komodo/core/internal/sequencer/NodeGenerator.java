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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.jcr.Node;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import org.komodo.spi.constants.StringConstants;
import org.komodo.spi.lexicon.sql.teiid.TeiidSqlLexicon;
import org.komodo.spi.query.CriteriaOperator;
import org.komodo.spi.query.CriteriaOperator.Operator;
import org.komodo.spi.query.JoinTypeTypes;
import org.komodo.spi.query.StatementType;
import org.komodo.spi.runtime.version.MetadataVersion;
import org.komodo.spi.type.DataTypeService;
import org.komodo.spi.type.DataTypeService.DataTypeName;
import org.teiid.language.Like.MatchMode;
import org.teiid.language.SortSpecification.NullOrdering;
import org.teiid.query.sql.LanguageObject;
import org.teiid.query.sql.LanguageVisitor;
import org.teiid.query.sql.lang.AlterProcedure;
import org.teiid.query.sql.lang.AlterTrigger;
import org.teiid.query.sql.lang.AlterView;
import org.teiid.query.sql.lang.ArrayTable;
import org.teiid.query.sql.lang.BetweenCriteria;
import org.teiid.query.sql.lang.CacheHint;
import org.teiid.query.sql.lang.Command;
import org.teiid.query.sql.lang.CompareCriteria;
import org.teiid.query.sql.lang.CompoundCriteria;
import org.teiid.query.sql.lang.Delete;
import org.teiid.query.sql.lang.DynamicCommand;
import org.teiid.query.sql.lang.ExistsCriteria;
import org.teiid.query.sql.lang.ExistsCriteria.SubqueryHint;
import org.teiid.query.sql.lang.ExpressionCriteria;
import org.teiid.query.sql.lang.From;
import org.teiid.query.sql.lang.FromClause;
import org.teiid.query.sql.lang.GroupBy;
import org.teiid.query.sql.lang.Insert;
import org.teiid.query.sql.lang.Into;
import org.teiid.query.sql.lang.IsDistinctCriteria;
import org.teiid.query.sql.lang.IsNullCriteria;
import org.teiid.query.sql.lang.JoinPredicate;
import org.teiid.query.sql.lang.JoinType;
import org.teiid.query.sql.lang.Limit;
import org.teiid.query.sql.lang.MatchCriteria;
import org.teiid.query.sql.lang.NotCriteria;
import org.teiid.query.sql.lang.ObjectTable;
import org.teiid.query.sql.lang.Option;
import org.teiid.query.sql.lang.Option.MakeDep;
import org.teiid.query.sql.lang.OrderBy;
import org.teiid.query.sql.lang.OrderByItem;
import org.teiid.query.sql.lang.Query;
import org.teiid.query.sql.lang.SPParameter;
import org.teiid.query.sql.lang.Select;
import org.teiid.query.sql.lang.SetClause;
import org.teiid.query.sql.lang.SetClauseList;
import org.teiid.query.sql.lang.SetCriteria;
import org.teiid.query.sql.lang.SetQuery;
import org.teiid.query.sql.lang.SetQuery.Operation;
import org.teiid.query.sql.lang.SourceHint;
import org.teiid.query.sql.lang.SourceHint.SpecificHint;
import org.teiid.query.sql.lang.StoredProcedure;
import org.teiid.query.sql.lang.SubqueryCompareCriteria;
import org.teiid.query.sql.lang.SubqueryFromClause;
import org.teiid.query.sql.lang.SubquerySetCriteria;
import org.teiid.query.sql.lang.TableFunctionReference.ProjectedColumn;
import org.teiid.query.sql.lang.TextTable;
import org.teiid.query.sql.lang.TextTable.TextColumn;
import org.teiid.query.sql.lang.UnaryFromClause;
import org.teiid.query.sql.lang.Update;
import org.teiid.query.sql.lang.WithQueryCommand;
import org.teiid.query.sql.lang.XMLTable;
import org.teiid.query.sql.lang.XMLTable.XMLColumn;
import org.teiid.query.sql.proc.AssignmentStatement;
import org.teiid.query.sql.proc.Block;
import org.teiid.query.sql.proc.BranchingStatement;
import org.teiid.query.sql.proc.CommandStatement;
import org.teiid.query.sql.proc.CreateProcedureCommand;
import org.teiid.query.sql.proc.DeclareStatement;
import org.teiid.query.sql.proc.ExceptionExpression;
import org.teiid.query.sql.proc.IfStatement;
import org.teiid.query.sql.proc.LoopStatement;
import org.teiid.query.sql.proc.RaiseStatement;
import org.teiid.query.sql.proc.ReturnStatement;
import org.teiid.query.sql.proc.TriggerAction;
import org.teiid.query.sql.proc.WhileStatement;
import org.teiid.query.sql.symbol.AggregateSymbol;
import org.teiid.query.sql.symbol.AliasSymbol;
import org.teiid.query.sql.symbol.CaseExpression;
import org.teiid.query.sql.symbol.Constant;
import org.teiid.query.sql.symbol.DerivedColumn;
import org.teiid.query.sql.symbol.ElementSymbol;
import org.teiid.query.sql.symbol.ElementSymbol.DisplayMode;
import org.teiid.query.sql.symbol.Expression;
import org.teiid.query.sql.symbol.ExpressionSymbol;
import org.teiid.query.sql.symbol.Function;
import org.teiid.query.sql.symbol.GroupSymbol;
import org.teiid.query.sql.symbol.JSONObject;
import org.teiid.query.sql.symbol.MultipleElementSymbol;
import org.teiid.query.sql.symbol.QueryString;
import org.teiid.query.sql.symbol.Reference;
import org.teiid.query.sql.symbol.ScalarSubquery;
import org.teiid.query.sql.symbol.SearchedCaseExpression;
import org.teiid.query.sql.symbol.TextLine;
import org.teiid.query.sql.symbol.WindowFunction;
import org.teiid.query.sql.symbol.WindowSpecification;
import org.teiid.query.sql.symbol.XMLAttributes;
import org.teiid.query.sql.symbol.XMLCast;
import org.teiid.query.sql.symbol.XMLElement;
import org.teiid.query.sql.symbol.XMLExists;
import org.teiid.query.sql.symbol.XMLForest;
import org.teiid.query.sql.symbol.XMLNamespaces;
import org.teiid.query.sql.symbol.XMLNamespaces.NamespaceItem;
import org.teiid.query.sql.symbol.XMLParse;
import org.teiid.query.sql.symbol.XMLQuery;
import org.teiid.query.sql.symbol.XMLSerialize;
import org.teiid.translator.CacheDirective.Invalidation;
import org.teiid.translator.CacheDirective.Scope;

public class NodeGenerator implements StringConstants {

    /**
     * @param value the value being lower camel-cased
     * @return camelCase version of the value but also ensures that values such as
     *                  SPParameter become spParameter
     */
    protected static String toLowerCamelCase(final String value) {
        if (value == null)
            return null;

        char c[] = value.toCharArray();
        if (c.length == 0)
            return null;

        for (int i = 0; i < c.length; ++i) {
            Character curr = c[i];
            Character next = null;

            if ((i + 1) < c.length)
                next = c[i + 1];

            if (next == null)
                break;

            if (Character.isUpperCase(curr) && Character.isUpperCase(next))
                c[i] = Character.toLowerCase(curr);

            if (i == 0)
                c[i] = Character.toLowerCase(curr);

            if (!Character.isUpperCase(next))
                break;
        }

        return new String(c);
    }

    protected class IndexKey {

        private String parentPath;

        private LanguageObject obj;

        public IndexKey(LanguageObject obj, Node parent) throws Exception {
            this.obj = obj;
            this.parentPath = parent == null ? FORWARD_SLASH : parent.getPath();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((obj == null) ? 0 : obj.hashCode());
            result = prime * result + ((parentPath == null) ? 0 : parentPath.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            IndexKey other = (IndexKey)obj;
            if (this.obj == null) {
                if (other.obj != null)
                    return false;
            } else if (!this.obj.equals(other.obj))
                return false;
            if (parentPath == null) {
                if (other.parentPath != null)
                    return false;
            } else if (!parentPath.equals(other.parentPath))
                return false;
            return true;
        }
    }

    /**
     * @param obj
     * @param node
     * @return a new IndexKey
     * @throws Exception
     */
    protected IndexKey createKey(LanguageObject obj, Node node) throws Exception {
        return new IndexKey(obj, node);
    }

    protected class Context {

        private Node oldParent;

        private String oldReference;

        public Context(Node newParent, String newReference) {
            switchParent(newParent);
            switchReference(newReference);
        }

        private void switchParent(Node newParent) {
            oldParent = getParentNode();
            setParentNode(newParent);
        }

        private void switchReference(String newReference) {
            oldReference = getReference();
            setReference(newReference);
        }

        public void reset() {
            setParentNode(oldParent);
            setReference(oldReference);
        }
    }

    protected Context localContext(Node parent, String reference) {
        return new Context(parent, reference);
    }

    private final Node rootNode;

    private final DataTypeService dataTypeManager;

    private final NodeVisitor nodeVisitor = new NodeVisitor();

    private final MetadataVersion version;

    private Map<IndexKey, Node> objectIndex;

    private Exception error = null;

    //==========================
    //
    // Context fields
    //
    //==========================
    // The parent node which the converted visited object will be added
    private Node parentNode;
    // The reference name under which the node will be added
    private String reference;

    public NodeGenerator(Node parentNode, DataTypeService dataTypeManager, MetadataVersion version) {
        this.rootNode = parentNode;
        this.dataTypeManager = dataTypeManager;
        this.version = version;
        this.parentNode = this.rootNode;
    }

    public MetadataVersion getVersion() {
        return version;
    }

    public DataTypeService getDataTypeManager() {
        return dataTypeManager;
    }

    public boolean errorOccurred() {
        return this.error != null;
    }

    public Exception getError() {
        return this.error;
    }

    public void setError(Exception error) {
        this.error = error;
    }

    public Node getParentNode() {
        return parentNode;
    }

    public void setParentNode(Node parentNode) {
        this.parentNode = parentNode;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    protected void index(LanguageObject obj, Node node) throws Exception {
        if (objectIndex == null)
            objectIndex = new HashMap<>();

        IndexKey key = createKey(obj, getParentNode());
        objectIndex.put(key, node);
    }

    protected Node node(LanguageObject obj) throws Exception {
        if (objectIndex == null)
            return null;

        IndexKey key = createKey(obj, getParentNode());
        return objectIndex.get(key);
    }

    protected Node create(Object obj) throws Exception {
        String jcrName = TeiidSqlLexicon.Namespace.PREFIX + COLON + toLowerCamelCase(obj.getClass().getSimpleName());
        String pathComponent = this.reference;
        if (this.reference == null)
            pathComponent = jcrName;

        Node node = null;

        node = parentNode.addNode(pathComponent);
        node.addMixin(jcrName);

        // Add the teiid version to the sequence node
        node.setProperty(TeiidSqlLexicon.LanguageObject.METADATA_VERSION_PROP_NAME, getVersion().toString());
        return node;
    }

    protected Node createTreeObject(LanguageObject obj) throws Exception {
        Node node = node(obj);
        if (node != null)
            return node;

        node = create((Object)obj);

        // Index the node against the object so can be found again
        index(obj, node);
        return node;
    }

    protected Node transform(LanguageObject obj) throws Exception {
        Node node = node(obj);
        if (node != null)
            return node;

        node = create((Object)obj);

        // Index the node against the object so can be found again
        index(obj, node);
        return node;
    }

    protected List<Object> convertToPropertyValues(Object objectValue) throws Exception {
        List<Object> result = new ArrayList<Object>();
        if (objectValue instanceof Collection) {
            Collection<?> objects = (Collection<?>)objectValue;
            for (Object childObjectValue : objects) {
                List<Object> childValues = convertToPropertyValues(childObjectValue);
                result.addAll(childValues);
            }
        } else if (objectValue instanceof Boolean) {
            result.add((Boolean) objectValue);
        } else if (objectValue instanceof Integer) {
            result.add((Integer) objectValue);
        } else if (objectValue instanceof Long) {
            result.add((Long) objectValue);
        } else if (objectValue instanceof Double) {
            result.add((Double) objectValue);
        } else if (objectValue instanceof Float) {
            result.add((Float) objectValue);
        } else {
            result.add(objectValue.toString());
        }
        return result;
    }

    protected List<Value> convertToPropertyValues(Object objectValue, ValueFactory valueFactory) throws Exception {
        List<Value> result = new ArrayList<Value>();
        if (objectValue instanceof Collection) {
            Collection<?> objects = (Collection<?>)objectValue;
            for (Object childObjectValue : objects) {
                List<Value> childValues = convertToPropertyValues(childObjectValue, valueFactory);
                result.addAll(childValues);
            }
        } else if (objectValue instanceof Boolean) {
            result.add(valueFactory.createValue((Boolean)objectValue));
        } else if (objectValue instanceof Integer) {
            result.add(valueFactory.createValue((Integer)objectValue));
        } else if (objectValue instanceof Long) {
            result.add(valueFactory.createValue((Long)objectValue));
        } else if (objectValue instanceof Double) {
            result.add(valueFactory.createValue((Double)objectValue));
        } else if (objectValue instanceof Float) {
            result.add(valueFactory.createValue((Float)objectValue));
        } else if (LanguageObject.class.isInstance(objectValue)) {
            result.add(valueFactory.createValue(node(LanguageObject.class.cast(objectValue))));
        } else {
            result.add(valueFactory.createValue(objectValue.toString()));
        }
        return result;
    }

    protected void setProperty(Node node, String name, Object value) throws Exception {
        ValueFactory valueFactory = node.getSession().getValueFactory();

        if (value == null) {
            node.setProperty(name, (Value)null);
            return;
        }

        List<Value> valuesList = convertToPropertyValues(value, valueFactory);
        if (valuesList.size() == 1) {
            node.setProperty(name, valuesList.get(0));
        } else {
            node.setProperty(name, valuesList.toArray(new Value[0]));
        }
    }

    protected void setDataTypeProperty(Node node, String reference, Class<?> typeClass) throws Exception {
        DataTypeName dataTypeName = getDataTypeManager().retrieveDataTypeName(typeClass);
        setProperty(node, reference, dataTypeName.name());
    }

    protected void setStatementTypeProperty(Node node, int type) throws Exception {
        StatementType statementType = StatementType.findStatementType(type);
        if (statementType == null)
            return;

        setProperty(node, TeiidSqlLexicon.Statement.TYPE_PROP_NAME, statementType.name());
    }

    protected void visitObject(Node node, String reference, LanguageObject obj) {
        Context context = localContext(node, reference);
        visitObject(obj);
        context.reset();
    }

    protected void visitObjects(Node node, String reference, LanguageObject[] objs) {
        if (objs == null || objs.length == 0)
            return;

        Context context = localContext(node, reference);

        for (LanguageObject obj : objs)
            visitObject(obj);

        context.reset();
    }

    protected void visitObjects(Node node, String reference, Collection<? extends LanguageObject> objs) {
        if (objs == null || objs.size() == 0)
            return;

        Context context = localContext(node, reference);

        for (LanguageObject obj : objs)
            visitObject(obj);

        context.reset();
    }

    public void visitObject(LanguageObject obj) {
        if (obj == null)
            return;

        obj.acceptVisitor(nodeVisitor);
    }

    private class NodeVisitor extends LanguageVisitor {

        private void visitCacheHint(Node parent, CacheHint hint) throws Exception {
            if (hint == null)
                return;

            Context context = localContext(parent, TeiidSqlLexicon.Command.CACHE_HINT_REF_NAME);
            Node cHintNode = create(hint);

            Invalidation invalidation = hint.getInvalidation();
            if (invalidation != null)
                setProperty(cHintNode, TeiidSqlLexicon.CacheHint.INVALIDATION_PROP_NAME, invalidation.name());

            setProperty(cHintNode, TeiidSqlLexicon.CacheHint.MIN_ROWS_PROP_NAME, hint.getMinRows());
            setProperty(cHintNode, TeiidSqlLexicon.CacheHint.PREFERS_MEMORY_PROP_NAME, hint.getPrefersMemory());
            setProperty(cHintNode, TeiidSqlLexicon.CacheHint.READ_ALL_PROP_NAME, hint.getReadAll());

            Scope scope = hint.getScope();
            if (scope != null)
                setProperty(cHintNode, TeiidSqlLexicon.CacheHint.SCOPE_PROP_NAME, scope.name());

            setProperty(cHintNode, TeiidSqlLexicon.CacheHint.TTL_PROP_NAME, hint.getTtl());
            setProperty(cHintNode, TeiidSqlLexicon.CacheHint.UPDATEABLE_PROP_NAME, hint.getUpdatable());

            context.reset();
        }

        private void visitSubqueryHint(Node parent, SubqueryHint hint) throws Exception {
            if (hint == null)
                return;

            Context context = localContext(parent, TeiidSqlLexicon.SubquerySetCriteria.SUBQUERY_HINT_REF_NAME);
            Node sqHintNode = create(hint);

            setProperty(sqHintNode, TeiidSqlLexicon.SubqueryHint.MERGE_JOIN_PROP_NAME, hint.isMergeJoin());
            setProperty(sqHintNode, TeiidSqlLexicon.SubqueryHint.NO_UNNEST_PROP_NAME, hint.isNoUnnest());
            setProperty(sqHintNode, TeiidSqlLexicon.SubqueryHint.DEP_JOIN_PROP_NAME, hint.isDepJoin());

            context.reset();
        }

        private void visitSpecificHint(Node parent, SpecificHint hint) throws Exception {
            if (hint == null)
                return;

            Context context = localContext(parent, TeiidSqlLexicon.SourceHint.SOURCE_HINTS_REF_NAME);
            Node spHintNode = create(hint);

            setProperty(spHintNode, TeiidSqlLexicon.SpecificHint.HINT_PROP_NAME, hint.getHint());
            setProperty(spHintNode, TeiidSqlLexicon.SpecificHint.USE_ALIASES_PROP_NAME, hint.isUseAliases());

            context.reset();
        }

        private void visitSourceHint(Node parent, SourceHint hint) throws Exception {
            if (hint == null)
                return;

            Context context = localContext(parent, TeiidSqlLexicon.Command.SOURCE_HINT_REF_NAME);
            Node srcHintNode = create(hint);

            setProperty(srcHintNode, TeiidSqlLexicon.SourceHint.GENERAL_HINT_PROP_NAME, hint.getGeneralHint());

            for (Map.Entry<String, SpecificHint> entry : hint.getSpecificHints().entrySet()) {
                visitSpecificHint(srcHintNode, entry.getValue());
            }

            context.reset();
        }

        private void visitFromClause(Node node, FromClause obj) throws Exception {
            setProperty(node, TeiidSqlLexicon.FromClause.OPTIONAL_PROP_NAME, obj.isOptional());
            setProperty(node, TeiidSqlLexicon.FromClause.MAKE_IND_PROP_NAME, obj.getMakeInd());
            setProperty(node, TeiidSqlLexicon.FromClause.NO_UNNEST_PROP_NAME, obj.isNoUnnest());
            setProperty(node, TeiidSqlLexicon.FromClause.MAKE_NOT_DEP_PROP_NAME, obj.isMakeNotDep());
            setProperty(node, TeiidSqlLexicon.FromClause.PRESERVE_PROP_NAME, obj.isPreserve());

            MakeDep makeDep = obj.getMakeDep();
            if (makeDep != null) {
                Context context = localContext(node, TeiidSqlLexicon.FromClause.MAKE_DEPENDENCY_REF_NAME);
                Node makeDepNode = create(makeDep);
                setProperty(makeDepNode, TeiidSqlLexicon.MakeDep.MAX_PROP_NAME, makeDep.getMax());
                setProperty(makeDepNode, TeiidSqlLexicon.MakeDep.JOIN_PROP_NAME, makeDep.getJoin());
                context.reset();
            }
        }

        private void visitSPParameter(Node parent, String reference, SPParameter obj) throws Exception {
            Context context = localContext(parent, reference);
            Node node = create(obj);

            String name = obj.getName();
            if (name != null && name.length() > 0)
                setProperty(node, TeiidSqlLexicon.SPParameter.NAME_PROP_NAME, name);

            setProperty(node, TeiidSqlLexicon.SPParameter.PARAMETER_TYPE_PROP_NAME, obj.getParameterType());
            Class<?> classType = obj.getClassType();
            if (classType != null)
                setProperty(node, TeiidSqlLexicon.SPParameter.CLASS_TYPE_CLASS_PROP_NAME, classType.getCanonicalName());

            setProperty(node, TeiidSqlLexicon.SPParameter.INDEX_PROP_NAME, obj.getIndex());
            setProperty(node, TeiidSqlLexicon.SPParameter.METADATAID_PROP_NAME, obj.getMetadataID());
            setProperty(node, TeiidSqlLexicon.SPParameter.USING_DEFAULT_PROP_NAME, obj.isUsingDefault());
            setProperty(node, TeiidSqlLexicon.SPParameter.VAR_ARG_PROP_NAME, obj.isVarArg());

            visitObject(node, TeiidSqlLexicon.SPParameter.EXPRESSION_REF_NAME, obj.getExpression());
            visitObjects(node, TeiidSqlLexicon.SPParameter.RESULT_SET_COLUMN_REF_NAME, obj.getResultSetColumns());

            context.reset();
        }

        @Override
        public void visit(BetweenCriteria obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                setDataTypeProperty(node, TeiidSqlLexicon.Expression.TYPE_CLASS_PROP_NAME, obj.getType());
                setProperty(node, TeiidSqlLexicon.BetweenCriteria.NEGATED_PROP_NAME, obj.isNegated());

                visitObject(node, TeiidSqlLexicon.BetweenCriteria.EXPRESSION_REF_NAME, obj.getExpression());
                visitObject(node, TeiidSqlLexicon.BetweenCriteria.LOWER_EXPRESSION_REF_NAME, obj.getLowerExpression());
                visitObject(node, TeiidSqlLexicon.BetweenCriteria.UPPER_EXPRESSION_REF_NAME, obj.getUpperExpression());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @SuppressWarnings( "unchecked" )
        @Override
        public void visit(CaseExpression obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                DataTypeName dataTypeName = getDataTypeManager().retrieveDataTypeName(obj.getType());
                setProperty(node, TeiidSqlLexicon.Expression.TYPE_CLASS_PROP_NAME, dataTypeName.name());

                visitObject(node, TeiidSqlLexicon.BetweenCriteria.EXPRESSION_REF_NAME, obj.getExpression());
                visitObjects(node, TeiidSqlLexicon.CaseExpression.WHEN_REF_NAME, obj.getWhen());
                visitObjects(node, TeiidSqlLexicon.CaseExpression.THEN_REF_NAME, obj.getThen());
                visitObject(node, TeiidSqlLexicon.CaseExpression.ELSE_EXPRESSION_REF_NAME, obj.getElseExpression());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(CompareCriteria obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                setProperty(node, TeiidSqlLexicon.CompareCriteria.OPTIONAL_PROP_NAME, obj.isOptional());

                Operator operator = CriteriaOperator.Operator.findOperator(obj.getOperator());
                if (operator != null)
                    setProperty(node, TeiidSqlLexicon.AbstractCompareCriteria.OPERATOR_PROP_NAME, operator.name());

                visitObject(node, TeiidSqlLexicon.CompareCriteria.RIGHT_EXPRESSION_REF_NAME, obj.getRightExpression());
                visitObject(node, TeiidSqlLexicon.CompareCriteria.LEFT_EXPRESSION_REF_NAME, obj.getLeftExpression());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(CompoundCriteria obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                setProperty(node, TeiidSqlLexicon.CompoundCriteria.OPERATOR_PROP_NAME, obj.getOperator());
                setDataTypeProperty(node, TeiidSqlLexicon.Expression.TYPE_CLASS_PROP_NAME, obj.getType());

                visitObjects(node, TeiidSqlLexicon.CompoundCriteria.CRITERIA_REF_NAME, obj.getCriteria());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        private void visitCommand(Node node, Command obj) throws Exception {
            visitCacheHint(node, obj.getCacheHint());
            visitSourceHint(node, obj.getSourceHint());

            setProperty(node, TeiidSqlLexicon.Command.TYPE_PROP_NAME, obj.getType());
            setProperty(node, TeiidSqlLexicon.Command.IS_RESOLVED_PROP_NAME, obj.isResolved());

            visitObject(node, TeiidSqlLexicon.Command.OPTION_REF_NAME, obj.getOption());
        }

        @Override
        public void visit(Delete obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                visitCommand(node, obj);
                visitObject(node, TeiidSqlLexicon.TargetedCommand.GROUP_REF_NAME, obj.getGroup());
                visitObject(node, TeiidSqlLexicon.Delete.CRITERIA_REF_NAME, obj.getCriteria());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(ExistsCriteria obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                DataTypeName dataTypeName = getDataTypeManager().retrieveDataTypeName(obj.getType());
                setProperty(node, TeiidSqlLexicon.Expression.TYPE_CLASS_PROP_NAME, dataTypeName.name());
                setProperty(node, TeiidSqlLexicon.ExistsCriteria.NEGATED_PROP_NAME, obj.isNegated());

                visitObject(node, TeiidSqlLexicon.SubqueryContainer.COMMAND_REF_NAME, obj.getCommand());
                visitSubqueryHint(node, obj.getSubqueryHint());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(From obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                visitObjects(node, TeiidSqlLexicon.From.CLAUSES_REF_NAME, obj.getClauses());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(GroupBy obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                setProperty(node, TeiidSqlLexicon.GroupBy.ROLLUP_PROP_NAME, obj.isRollup());

                visitObjects(node, TeiidSqlLexicon.GroupBy.SYMBOLS_REF_NAME, obj.getSymbols());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @SuppressWarnings( "unchecked" )
        @Override
        public void visit(Insert obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                visitCommand(node, obj);

                visitObject(node, TeiidSqlLexicon.TargetedCommand.GROUP_REF_NAME, obj.getGroup());
                visitObjects(node, TeiidSqlLexicon.Insert.VARIABLES_REF_NAME, obj.getVariables());
                visitObjects(node, TeiidSqlLexicon.Insert.VALUES_REF_NAME, obj.getValues());
                visitObject(node, TeiidSqlLexicon.Insert.QUERY_EXPRESSION_REF_NAME, obj.getQueryExpression());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(IsNullCriteria obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                setDataTypeProperty(node, TeiidSqlLexicon.Criteria.TYPE_CLASS_PROP_NAME, obj.getType());
                setProperty(node, TeiidSqlLexicon.IsNullCriteria.NEGATED_PROP_NAME, obj.isNegated());

                visitObject(node, TeiidSqlLexicon.IsNullCriteria.EXPRESSION_REF_NAME, obj.getExpression());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @SuppressWarnings( "unchecked" )
        @Override
        public void visit(JoinPredicate obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                visitObject(node, TeiidSqlLexicon.JoinPredicate.LEFT_CLAUSE_REF_NAME, obj.getLeftClause());
                visitObject(node, TeiidSqlLexicon.JoinPredicate.RIGHT_CLAUSE_REF_NAME, obj.getRightClause());
                visitObject(node, TeiidSqlLexicon.JoinPredicate.JOIN_TYPE_REF_NAME, obj.getJoinType());
                visitObjects(node, TeiidSqlLexicon.JoinPredicate.JOIN_CRITERIA_REF_NAME, obj.getJoinCriteria());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(JoinType obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                JoinTypeTypes joinType = JoinTypeTypes.findType(obj.hashCode());
                if (joinType != null)
                    setProperty(node, TeiidSqlLexicon.JoinType.KIND_PROP_NAME, joinType.name());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(Limit obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                setProperty(node, TeiidSqlLexicon.Limit.STRICT_PROP_NAME, obj.isStrict());
                setProperty(node, TeiidSqlLexicon.Limit.IMPLICIT_PROP_NAME, obj.isImplicit());

                visitObject(node, TeiidSqlLexicon.Limit.OFFSET_REF_NAME, obj.getOffset());
                visitObject(node, TeiidSqlLexicon.Limit.ROW_LIMIT_REF_NAME, obj.getRowLimit());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(MatchCriteria obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                setDataTypeProperty(node, TeiidSqlLexicon.Criteria.TYPE_CLASS_PROP_NAME, obj.getType());
                setProperty(node, TeiidSqlLexicon.MatchCriteria.ESCAPE_CHAR_PROP_NAME, obj.getEscapeChar());
                setProperty(node, TeiidSqlLexicon.MatchCriteria.NEGATED_PROP_NAME, obj.isNegated());

                MatchMode mode = obj.getMode();
                if (mode != null)
                    setProperty(node, TeiidSqlLexicon.MatchCriteria.MODE_PROP_NAME, mode);

                visitObject(node, TeiidSqlLexicon.MatchCriteria.LEFT_EXPRESSION_REF_NAME, obj.getLeftExpression());
                visitObject(node, TeiidSqlLexicon.MatchCriteria.RIGHT_EXPRESSION_REF_NAME, obj.getRightExpression());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(NotCriteria obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                setDataTypeProperty(node, TeiidSqlLexicon.Criteria.TYPE_CLASS_PROP_NAME, obj.getType());

                visitObject(node, TeiidSqlLexicon.NotCriteria.CRITERIA_REF_NAME, obj.getCriteria());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(Option obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                setProperty(node, TeiidSqlLexicon.Option.NO_CACHE_PROP_NAME, obj.isNoCache());
                setProperty(node, TeiidSqlLexicon.Option.NO_CACHE_GROUPS_PROP_NAME, obj.getNoCacheGroups());
                setProperty(node, TeiidSqlLexicon.Option.NOT_DEPENDENT_GROUPS_PROP_NAME, obj.getNotDependentGroups());

                List<MakeDep> makeDeps = obj.getMakeDepOptions();
                if (makeDeps != null) {
                    Context context = localContext(node, TeiidSqlLexicon.Option.DEPENDENT_GROUP_OPTIONS_REF_NAME);
                    for (MakeDep makeDep : makeDeps) {
                        Node makeDepNode = create(makeDep);
                        setProperty(makeDepNode, TeiidSqlLexicon.MakeDep.JOIN_PROP_NAME, makeDep.getJoin());
                        setProperty(makeDepNode, TeiidSqlLexicon.MakeDep.MAX_PROP_NAME, makeDep.getMax());
                    }
                    context.reset();
                }

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(OrderBy obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                visitObjects(node, TeiidSqlLexicon.OrderBy.ORDER_BY_ITEMS_REF_NAME, obj.getOrderByItems());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(Query obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                visitCommand(node, obj);

                visitObjects(node, TeiidSqlLexicon.Query.WITH_REF_NAME, obj.getWith());
                visitObject(node, TeiidSqlLexicon.Query.SELECT_REF_NAME, obj.getSelect());
                visitObject(node, TeiidSqlLexicon.Query.INTO_REF_NAME, obj.getInto());
                visitObject(node, TeiidSqlLexicon.Query.FROM_REF_NAME, obj.getFrom());
                visitObject(node, TeiidSqlLexicon.Query.CRITERIA_REF_NAME, obj.getCriteria());
                visitObject(node, TeiidSqlLexicon.Query.GROUP_BY_REF_NAME, obj.getGroupBy());
                visitObject(node, TeiidSqlLexicon.Query.HAVING_REF_NAME, obj.getHaving());
                visitObject(node, TeiidSqlLexicon.Query.ORDER_BY_REF_NAME, obj.getOrderBy());
                visitObject(node, TeiidSqlLexicon.Query.LIMIT_REF_NAME, obj.getLimit());
                visitObject(node, TeiidSqlLexicon.Query.OPTION_REF_NAME, obj.getOption());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @SuppressWarnings( "unchecked" )
        @Override
        public void visit(SearchedCaseExpression obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                setDataTypeProperty(node, TeiidSqlLexicon.Expression.TYPE_CLASS_PROP_NAME, obj.getType());

                visitObjects(node, TeiidSqlLexicon.SearchedCaseExpression.WHEN_REF_NAME, obj.getWhen());
                visitObjects(node, TeiidSqlLexicon.SearchedCaseExpression.THEN_REF_NAME, obj.getThen());
                visitObject(node, TeiidSqlLexicon.SearchedCaseExpression.ELSE_EXPRESSION_REF_NAME, obj.getElseExpression());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(Select obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                setProperty(node, TeiidSqlLexicon.Select.DISTINCT_PROP_NAME, obj.isDistinct());

                visitObjects(node, TeiidSqlLexicon.Select.SYMBOLS_REF_NAME, obj.getSymbols());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @SuppressWarnings( "unchecked" )
        @Override
        public void visit(SetCriteria obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                setDataTypeProperty(node, TeiidSqlLexicon.Expression.TYPE_CLASS_PROP_NAME, obj.getType());
                setProperty(node, TeiidSqlLexicon.AbstractSetCriteria.NEGATED_PROP_NAME, obj.isNegated());

                visitObject(node, TeiidSqlLexicon.AbstractSetCriteria.EXPRESSION_REF_NAME, obj.getExpression());
                visitObjects(node, TeiidSqlLexicon.SetCriteria.VALUES_REF_NAME, obj.getValues());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(SetQuery obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                visitCommand(node, obj);

                setProperty(node, TeiidSqlLexicon.SetQuery.ALL_PROP_NAME, obj.isAll());

                Operation operation = obj.getOperation();
                if (operation != null)
                    setProperty(node, TeiidSqlLexicon.SetQuery.OPERATION_PROP_NAME, operation.name());

                visitObject(node, TeiidSqlLexicon.SetQuery.LEFT_QUERY_REF_NAME, obj.getLeftQuery());
                visitObject(node, TeiidSqlLexicon.SetQuery.RIGHT_QUERY_REF_NAME, obj.getRightQuery());
                visitObject(node, TeiidSqlLexicon.QueryCommand.ORDER_BY_REF_NAME, obj.getOrderBy());
                visitObject(node, TeiidSqlLexicon.QueryCommand.LIMIT_REF_NAME, obj.getLimit());
                visitObjects(node, TeiidSqlLexicon.QueryCommand.WITH_REF_NAME, obj.getWith());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(StoredProcedure obj) {
            if (errorOccurred())
                return;
            try {
                Node node = transform(obj);

                visitCommand(node, obj);

                setProperty(node, TeiidSqlLexicon.StoredProcedure.CALLED_WITH_RETURN_PROP_NAME, obj.isCalledWithReturn());
                setProperty(node, TeiidSqlLexicon.StoredProcedure.CALLABLE_STATEMENT_PROP_NAME, obj.isCallableStatement());
                setProperty(node,
                            TeiidSqlLexicon.StoredProcedure.DISPLAY_NAMED_PARAMETERS_PROP_NAME,
                            obj.displayNamedParameters());
                setProperty(node, TeiidSqlLexicon.StoredProcedure.PROCEDUREID_PROP_NAME, obj.getProcedureID());
                setProperty(node, TeiidSqlLexicon.StoredProcedure.PROCEDURE_NAME_PROP_NAME, obj.getProcedureName());

                visitObject(node, TeiidSqlLexicon.TargetedCommand.GROUP_REF_NAME, obj.getGroup());

                for (SPParameter parameter : obj.getParameters()) {
                    visitSPParameter(node, TeiidSqlLexicon.StoredProcedure.PARAMETERS_REF_NAME, parameter);
                }

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(SubqueryCompareCriteria obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                setProperty(node, TeiidSqlLexicon.AbstractCompareCriteria.OPERATOR_PROP_NAME, obj.getOperatorAsString());
                setProperty(node,
                            TeiidSqlLexicon.SubqueryCompareCriteria.PREDICATE_QUANTIFIER_PROP_NAME,
                            obj.getPredicateQuantifierAsString());

                visitObject(node, TeiidSqlLexicon.SubqueryContainer.COMMAND_REF_NAME, obj.getCommand());
                visitObject(node, TeiidSqlLexicon.AbstractCompareCriteria.LEFT_EXPRESSION_REF_NAME, obj.getLeftExpression());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(SubqueryFromClause obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                setProperty(node, TeiidSqlLexicon.SubqueryFromClause.NAME_PROP_NAME, obj.getName());

                //
                // Change of API between 8.12.4 and 8.12.7+
                //
                Method getter = null;
                try {
                    getter = obj.getClass().getMethod("isLateral");
                } catch (NoSuchMethodException ex) {
                    getter = obj.getClass().getMethod("isTable");
                }

                setProperty(node, TeiidSqlLexicon.SubqueryFromClause.TABLE_PROP_NAME, getter.invoke(obj));

                visitFromClause(node, obj);

                visitObject(node, TeiidSqlLexicon.SubqueryContainer.COMMAND_REF_NAME, obj.getCommand());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(SubquerySetCriteria obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                visitSubqueryHint(node, obj.getSubqueryHint());

                setProperty(node, TeiidSqlLexicon.AbstractSetCriteria.NEGATED_PROP_NAME, obj.isNegated());

                visitObject(node, TeiidSqlLexicon.AbstractSetCriteria.EXPRESSION_REF_NAME, obj.getExpression());
                visitObject(node, TeiidSqlLexicon.SubqueryContainer.COMMAND_REF_NAME, obj.getCommand());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(UnaryFromClause obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                visitFromClause(node, obj);

                visitObject(node, TeiidSqlLexicon.UnaryFromClause.GROUP_REF_NAME, obj.getGroup());
                visitObject(node, TeiidSqlLexicon.UnaryFromClause.EXPANDED_COMMAND_REF_NAME, obj.getExpandedCommand());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(Update obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                visitCommand(node, obj);

                visitObject(node, TeiidSqlLexicon.Update.CHANGE_LIST_REF_NAME, obj.getChangeList());
                visitObject(node, TeiidSqlLexicon.TargetedCommand.GROUP_REF_NAME, obj.getGroup());
                visitObject(node, TeiidSqlLexicon.Update.CRITERIA_REF_NAME, obj.getCriteria());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(Into obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                visitObject(node, TeiidSqlLexicon.Into.GROUP_REF_NAME, obj.getGroup());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        // Visitor methods for symbol objects
        @Override
        public void visit(AggregateSymbol obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                setProperty(node, TeiidSqlLexicon.Function.NAME_PROP_NAME, obj.getName());
                setProperty(node, TeiidSqlLexicon.Function.IMPLICIT_PROP_NAME, obj.isImplicit());
                setProperty(node, TeiidSqlLexicon.AggregateSymbol.DISTINCT_PROP_NAME, obj.isDistinct());
                setProperty(node, TeiidSqlLexicon.AggregateSymbol.WINDOWED_PROP_NAME, obj.isWindowed());

                DataTypeName dataType = getDataTypeManager().retrieveDataTypeName(obj.getType());
                setProperty(node, TeiidSqlLexicon.Expression.TYPE_CLASS_PROP_NAME, dataType.name());

                AggregateSymbol.Type funcType = obj.getAggregateFunction();
                if (funcType == null)
                    funcType = AggregateSymbol.Type.USER_DEFINED;
                setProperty(node, TeiidSqlLexicon.AggregateSymbol.AGGREGATE_FUNCTION_PROP_NAME, funcType.name());

                Expression[] args = obj.getArgs();
                if (args != null) {
                    Context context = localContext(node, TeiidSqlLexicon.Function.ARGS_REF_NAME);
                    for (Expression expr : args) {
                        visitObject(expr);
                    }
                    context.reset();
                }

                visitObject(node, TeiidSqlLexicon.AggregateSymbol.CONDITION_REF_NAME, obj.getCondition());

                visitObject(node, TeiidSqlLexicon.Function.ARGS_REF_NAME, obj.getOrderBy());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(AliasSymbol obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                setProperty(node, TeiidSqlLexicon.Symbol.NAME_PROP_NAME, obj.getName());
                setProperty(node, TeiidSqlLexicon.Symbol.SHORT_NAME_PROP_NAME, obj.getShortName());
                setProperty(node, TeiidSqlLexicon.Symbol.OUTPUT_NAME_PROP_NAME, obj.getOutputName());

                visitObject(node, TeiidSqlLexicon.AliasSymbol.SYMBOL_REF_NAME, obj.getSymbol());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(MultipleElementSymbol obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                setDataTypeProperty(node, TeiidSqlLexicon.Expression.TYPE_CLASS_PROP_NAME, obj.getType());

                visitObject(node, TeiidSqlLexicon.MultipleElementSymbol.GROUP_REF_NAME, obj.getGroup());
                visitObjects(node, TeiidSqlLexicon.MultipleElementSymbol.ELEMENT_SYMBOLS_REF_NAME, obj.getElementSymbols());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(Constant obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);
                setProperty(node, TeiidSqlLexicon.Constant.MULTI_VALUED_PROP_NAME, obj.isMultiValued());

                setProperty(node, TeiidSqlLexicon.Constant.VALUE_PROP_NAME, obj.getValue());

                DataTypeName dataTypeName = getDataTypeManager().retrieveDataTypeName(obj.getType());
                setProperty(node, TeiidSqlLexicon.Expression.TYPE_CLASS_PROP_NAME, dataTypeName.name());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(ElementSymbol obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                setDataTypeProperty(node, TeiidSqlLexicon.Expression.TYPE_CLASS_PROP_NAME, obj.getType());
                setProperty(node, TeiidSqlLexicon.Symbol.NAME_PROP_NAME, obj.getName());
                setProperty(node, TeiidSqlLexicon.Symbol.SHORT_NAME_PROP_NAME, obj.getShortName());
                setProperty(node, TeiidSqlLexicon.Symbol.OUTPUT_NAME_PROP_NAME, obj.getOutputName());
                setProperty(node, TeiidSqlLexicon.ElementSymbol.METADATAID_PROP_NAME, obj.getMetadataID());
                setProperty(node, TeiidSqlLexicon.ElementSymbol.EXTERNAL_REFERENCE_PROP_NAME, obj.isExternalReference());

                DisplayMode displayMode = obj.getDisplayMode();
                setProperty(node, TeiidSqlLexicon.ElementSymbol.DISPLAY_MODE_PROP_NAME, displayMode.name());
                setProperty(node,
                            TeiidSqlLexicon.ElementSymbol.DISPLAY_FULLY_QUALIFIED_PROP_NAME,
                            DisplayMode.FULLY_QUALIFIED.equals(displayMode));

                visitObject(node, TeiidSqlLexicon.ElementSymbol.GROUP_SYMBOL_REF_NAME, obj.getGroupSymbol());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(ExpressionSymbol obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                setProperty(node, TeiidSqlLexicon.Symbol.NAME_PROP_NAME, obj.getName());
                setProperty(node, TeiidSqlLexicon.Symbol.SHORT_NAME_PROP_NAME, obj.getShortName());
                setProperty(node, TeiidSqlLexicon.Symbol.OUTPUT_NAME_PROP_NAME, obj.getOutputName());

                Expression expression = obj.getExpression();
                setDataTypeProperty(node, TeiidSqlLexicon.Expression.TYPE_CLASS_PROP_NAME, expression.getType());

                visitObject(node, TeiidSqlLexicon.ExpressionSymbol.EXPRESSION_REF_NAME, expression);

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(Function obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                setDataTypeProperty(node, TeiidSqlLexicon.Expression.TYPE_CLASS_PROP_NAME, obj.getType());
                setProperty(node, TeiidSqlLexicon.Function.NAME_PROP_NAME, obj.getName());
                setProperty(node, TeiidSqlLexicon.Function.IMPLICIT_PROP_NAME, obj.isImplicit());

                visitObjects(node, TeiidSqlLexicon.Function.ARGS_REF_NAME, obj.getArgs());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(GroupSymbol obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                setProperty(node, TeiidSqlLexicon.Symbol.NAME_PROP_NAME, obj.getName());
                setProperty(node, TeiidSqlLexicon.Symbol.SHORT_NAME_PROP_NAME, obj.getShortName());
                setProperty(node, TeiidSqlLexicon.Symbol.OUTPUT_NAME_PROP_NAME, obj.getOutputName());

                setProperty(node, TeiidSqlLexicon.GroupSymbol.PROCEDURE_PROP_NAME, obj.isProcedure());
                setProperty(node, TeiidSqlLexicon.GroupSymbol.METADATAID_PROP_NAME, obj.getMetadataID());
                setProperty(node, TeiidSqlLexicon.GroupSymbol.DEFINITION_PROP_NAME, obj.getOutputDefinition());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(Reference obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                setDataTypeProperty(node, TeiidSqlLexicon.Expression.TYPE_CLASS_PROP_NAME, obj.getType());
                setProperty(node, TeiidSqlLexicon.Reference.POSITIONAL_PROP_NAME, obj.isPositional());
                setProperty(node, TeiidSqlLexicon.Reference.INDEX_PROP_NAME, obj.getIndex());

                visitObject(node, TeiidSqlLexicon.Reference.EXPRESSION_REF_NAME, obj.getExpression());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(ScalarSubquery obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                setDataTypeProperty(node, TeiidSqlLexicon.Expression.TYPE_CLASS_PROP_NAME, obj.getType());

                visitObject(node, TeiidSqlLexicon.SubqueryContainer.COMMAND_REF_NAME, obj.getCommand());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @SuppressWarnings( "deprecation" )
        private void visitAssignmentStatement(Node node, AssignmentStatement obj) throws Exception {
            setStatementTypeProperty(node, obj.getType());
            setDataTypeProperty(node, TeiidSqlLexicon.ExpressionStatement.EXPECTED_TYPE_CLASS_PROP_NAME, obj.getExpectedType());

            visitObject(node, TeiidSqlLexicon.AssignmentStatement.VARIABLE_REF_NAME, obj.getVariable());
            visitObject(node, TeiidSqlLexicon.AssignmentStatement.COMMAND_REF_NAME, obj.getCommand());

            // Cannot store the same reference under 2 references as adding the value will 'move' the child to the new parent
            visitObject(node, TeiidSqlLexicon.AssignmentStatement.VALUE_REF_NAME, (Expression)obj.getExpression().clone());
        }

        @Override
        public void visit(AssignmentStatement obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                visitAssignmentStatement(node, obj);

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(Block obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                setStatementTypeProperty(node, obj.getType());
                setProperty(node, TeiidSqlLexicon.Block.ATOMIC_PROP_NAME, obj.isAtomic());
                setProperty(node, TeiidSqlLexicon.Block.EXCEPTION_GROUP_PROP_NAME, obj.getExceptionGroup());
                setProperty(node, TeiidSqlLexicon.Labeled.LABEL_PROP_NAME, obj.getLabel());

                visitObjects(node, TeiidSqlLexicon.Block.STATEMENTS_REF_NAME, obj.getStatements());
                visitObjects(node, TeiidSqlLexicon.Block.EXCEPTION_STATEMENTS_REF_NAME, obj.getExceptionStatements());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(CommandStatement obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                setStatementTypeProperty(node, obj.getType());
                setProperty(node, TeiidSqlLexicon.CommandStatement.RETURNABLE_PROP_NAME, obj.isReturnable());

                visitObject(node, TeiidSqlLexicon.SubqueryContainer.COMMAND_REF_NAME, obj.getCommand());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(CreateProcedureCommand obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                visitCommand(node, obj);

                visitObject(node, TeiidSqlLexicon.CreateProcedureCommand.BLOCK_REF_NAME, obj.getBlock());
                visitObject(node, TeiidSqlLexicon.CreateProcedureCommand.VIRTUAL_GROUP_REF_NAME, obj.getVirtualGroup());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @SuppressWarnings( "deprecation" )
        @Override
        public void visit(DeclareStatement obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                setStatementTypeProperty(node, obj.getType());
                setDataTypeProperty(node,
                                    TeiidSqlLexicon.ExpressionStatement.EXPECTED_TYPE_CLASS_PROP_NAME,
                                    obj.getExpectedType());
                setProperty(node, TeiidSqlLexicon.DeclareStatement.VARIABLE_TYPE_PROP_NAME, obj.getVariableType());

                visitObject(node, TeiidSqlLexicon.AssignmentStatement.VARIABLE_REF_NAME, obj.getVariable());
                visitObject(node, TeiidSqlLexicon.ExpressionStatement.EXPRESSION_REF_NAME, obj.getExpression());
                visitObject(node, TeiidSqlLexicon.AssignmentStatement.COMMAND_REF_NAME, obj.getCommand());

                // Cannot store the same reference under 2 references as adding the value will 'move' the child to the new parent
                if (obj.getExpression() != null)
                    visitObject(node,
                                TeiidSqlLexicon.AssignmentStatement.VALUE_REF_NAME,
                                (Expression)obj.getExpression().clone());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(IfStatement obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                setStatementTypeProperty(node, obj.getType());

                visitObject(node, TeiidSqlLexicon.IfStatement.CONDITION_REF_NAME, obj.getCondition());
                visitObject(node, TeiidSqlLexicon.IfStatement.IF_BLOCK_REF_NAME, obj.getIfBlock());
                visitObject(node, TeiidSqlLexicon.IfStatement.ELSE_BLOCK_REF_NAME, obj.getElseBlock());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(RaiseStatement obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                setStatementTypeProperty(node, obj.getType());
                setProperty(node, TeiidSqlLexicon.RaiseStatement.WARNING_PROP_NAME, obj.isWarning());

                visitObject(node, TeiidSqlLexicon.ExpressionStatement.EXPRESSION_REF_NAME, obj.getExpression());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(BranchingStatement obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                setStatementTypeProperty(node, obj.getType());
                setProperty(node, TeiidSqlLexicon.BranchingStatement.LABEL_PROP_NAME, obj.getLabel());
                setProperty(node, TeiidSqlLexicon.BranchingStatement.MODE_PROP_NAME, obj.getMode().name());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(WhileStatement obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                setStatementTypeProperty(node, obj.getType());
                setProperty(node, TeiidSqlLexicon.Labeled.LABEL_PROP_NAME, obj.getLabel());

                visitObject(node, TeiidSqlLexicon.WhileStatement.CONDITION_REF_NAME, obj.getCondition());
                visitObject(node, TeiidSqlLexicon.WhileStatement.BLOCK_REF_NAME, obj.getBlock());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(LoopStatement obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                setStatementTypeProperty(node, obj.getType());
                setProperty(node, TeiidSqlLexicon.Labeled.LABEL_PROP_NAME, obj.getLabel());
                setProperty(node, TeiidSqlLexicon.LoopStatement.CURSOR_NAME_PROP_NAME, obj.getCursorName());

                visitObject(node, TeiidSqlLexicon.SubqueryContainer.COMMAND_REF_NAME, obj.getCommand());
                visitObject(node, TeiidSqlLexicon.WhileStatement.BLOCK_REF_NAME, obj.getBlock());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @SuppressWarnings( "unchecked" )
        @Override
        public void visit(DynamicCommand obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                visitCommand(node, obj);

                setProperty(node, TeiidSqlLexicon.DynamicCommand.AS_CLAUSE_SET_PROP_NAME, obj.isAsClauseSet());
                setProperty(node, TeiidSqlLexicon.DynamicCommand.UPDATING_MODEL_COUNT_PROP_NAME, obj.getUpdatingModelCount());

                visitObject(node, TeiidSqlLexicon.DynamicCommand.INTO_GROUP_REF_NAME, obj.getIntoGroup());
                visitObject(node, TeiidSqlLexicon.DynamicCommand.SQL_REF_NAME, obj.getSql());
                visitObject(node, TeiidSqlLexicon.DynamicCommand.USING_REF_NAME, obj.getUsing());

                visitObjects(node, TeiidSqlLexicon.DynamicCommand.AS_COLUMNS_REF_NAME, obj.getAsColumns());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(SetClauseList obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                visitObjects(node, TeiidSqlLexicon.SetClauseList.SET_CLAUSES_REF_NAME, obj.getClauses());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(SetClause obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                visitObject(node, TeiidSqlLexicon.SetClause.SYMBOL_REF_NAME, obj.getSymbol());
                visitObject(node, TeiidSqlLexicon.SetClause.VALUE_REF_NAME, obj.getValue());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(OrderByItem obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                setProperty(node, TeiidSqlLexicon.OrderByItem.ASCENDING_PROP_NAME, obj.isAscending());
                NullOrdering nullOrdering = obj.getNullOrdering();
                if (nullOrdering != null)
                    setProperty(node, TeiidSqlLexicon.OrderByItem.NULL_ORDERING_PROP_NAME, nullOrdering.name());

                visitObject(node, TeiidSqlLexicon.OrderByItem.SYMBOL_REF_NAME, obj.getSymbol());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(XMLElement obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                setProperty(node, TeiidSqlLexicon.Expression.TYPE_CLASS_PROP_NAME, DataTypeName.XML.name());
                setProperty(node, TeiidSqlLexicon.XMLElement.NAME_PROP_NAME, obj.getName());

                visitObject(node, TeiidSqlLexicon.XMLElement.NAMESPACES_REF_NAME, obj.getNamespaces());
                visitObject(node, TeiidSqlLexicon.XMLElement.ATTRIBUTES_REF_NAME, obj.getAttributes());

                visitObjects(node, TeiidSqlLexicon.XMLElement.CONTENT_REF_NAME, obj.getContent());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(XMLAttributes obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                visitObjects(node, TeiidSqlLexicon.XMLAttributes.ARGS_REF_NAME, obj.getArgs());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(XMLForest obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                setDataTypeProperty(node, TeiidSqlLexicon.Expression.TYPE_CLASS_PROP_NAME, obj.getType());

                visitObjects(node, TeiidSqlLexicon.XMLForest.ARGUMENTS_REF_NAME, obj.getArgs());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(XMLNamespaces obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                Context context = localContext(node, TeiidSqlLexicon.XMLNamespaces.NAMESPACE_ITEMS_REF_NAME);
                for (NamespaceItem item : obj.getNamespaceItems()) {
                    Node itemNode = create(item);
                    setProperty(itemNode, TeiidSqlLexicon.NamespaceItem.URI_PROP_NAME, item.getUri());
                    setProperty(itemNode, TeiidSqlLexicon.NamespaceItem.PREFIX_PROP_NAME, item.getPrefix());
                }
                context.reset();

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(TextTable obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                setProperty(node, TeiidSqlLexicon.TableFunctionReference.NAME_PROP_NAME, obj.getName());
                setProperty(node, TeiidSqlLexicon.TextTable.DELIMITER_PROP_NAME, obj.getDelimiter());
                setProperty(node, TeiidSqlLexicon.TextTable.ESCAPE_PROP_NAME, obj.isEscape());
                setProperty(node, TeiidSqlLexicon.TextTable.HEADER_PROP_NAME, obj.getHeader());
                setProperty(node, TeiidSqlLexicon.TextTable.SKIP_PROP_NAME, obj.getSkip());
                setProperty(node, TeiidSqlLexicon.TextTable.QUOTE_PROP_NAME, obj.getQuote());
                setProperty(node, TeiidSqlLexicon.TextTable.USING_ROW_DELIMITER_PROP_NAME, obj.isUsingRowDelimiter());
                setProperty(node, TeiidSqlLexicon.TextTable.SELECTOR_PROP_NAME, obj.getSelector());
                setProperty(node, TeiidSqlLexicon.TextTable.FIXED_WIDTH_PROP_NAME, obj.isFixedWidth());

                visitFromClause(node, obj);

                visitObject(node, TeiidSqlLexicon.TextTable.FILE_REF_NAME, obj.getFile());

                Context context = localContext(node, TeiidSqlLexicon.TextTable.COLUMNS_REF_NAME);
                for (TextColumn column : obj.getColumns()) {
                    Node columnNode = create(column);

                    setProperty(columnNode, TeiidSqlLexicon.TextColumn.NAME_PROP_NAME, column.getName());
                    setProperty(columnNode, TeiidSqlLexicon.TextColumn.TYPE_PROP_NAME, column.getType());
                    setProperty(columnNode, TeiidSqlLexicon.TextColumn.ORDINAL_PROP_NAME, column.isOrdinal());
                    setProperty(columnNode, TeiidSqlLexicon.TextColumn.NO_TRIM_PROP_NAME, column.isNoTrim());
                    setProperty(columnNode, TeiidSqlLexicon.TextColumn.SELECTOR_PROP_NAME, column.getSelector());
                    setProperty(columnNode, TeiidSqlLexicon.TextColumn.POSITION_PROP_NAME, column.getPosition());
                    setProperty(columnNode, TeiidSqlLexicon.TextColumn.WIDTH_PROP_NAME, column.getWidth());
                    setProperty(columnNode, TeiidSqlLexicon.TextColumn.HEADER_PROP_NAME, column.getHeader());
                }
                context.reset();

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(TextLine obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                setDataTypeProperty(node, TeiidSqlLexicon.Expression.TYPE_CLASS_PROP_NAME, obj.getType());
                setProperty(node, TeiidSqlLexicon.TextLine.DELIMITER_PROP_NAME, obj.getDelimiter());
                setProperty(node, TeiidSqlLexicon.TextLine.QUOTE_PROP_NAME, obj.getQuote());
                setProperty(node, TeiidSqlLexicon.TextLine.INCLUDE_HEADER_PROP_NAME, obj.isIncludeHeader());
                setProperty(node, TeiidSqlLexicon.TextLine.ENCODING_PROP_NAME, obj.getEncoding());

                visitObjects(node, TeiidSqlLexicon.TextLine.EXPRESSIONS_REF_NAME, obj.getExpressions());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(XMLTable obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                setProperty(node, TeiidSqlLexicon.TableFunctionReference.NAME_PROP_NAME, obj.getName());
                setProperty(node, TeiidSqlLexicon.XMLTable.XQUERY_PROP_NAME, obj.getXquery());
                setProperty(node, TeiidSqlLexicon.XMLTable.USING_DEFAULT_COLUMN_PROP_NAME, obj.isUsingDefaultColumn());

                visitFromClause(node, obj);

                visitObject(node, TeiidSqlLexicon.XMLTable.NAMESPACES_REF_NAME, obj.getNamespaces());
                visitObjects(node, TeiidSqlLexicon.XMLTable.PASSING_REF_NAME, obj.getPassing());

                Context context = localContext(node, TeiidSqlLexicon.XMLTable.COLUMNS_REF_NAME);
                for (XMLColumn column : obj.getColumns()) {
                    Node columnNode = create(column);

                    setProperty(columnNode, TeiidSqlLexicon.ProjectedColumn.NAME_PROP_NAME, column.getName());
                    setProperty(columnNode, TeiidSqlLexicon.ProjectedColumn.TYPE_PROP_NAME, column.getType());
                    setProperty(columnNode, TeiidSqlLexicon.XMLColumn.ORDINAL_PROP_NAME, column.getPath());
                    setProperty(columnNode, TeiidSqlLexicon.XMLColumn.PATH_PROP_NAME, column.getPath());

                    visitObject(columnNode, TeiidSqlLexicon.XMLColumn.DEFAULT_EXPRESSION_REF_NAME, column.getDefaultExpression());
                }
                context.reset();

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(DerivedColumn obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                setProperty(node, TeiidSqlLexicon.DerivedColumn.ALIAS_PROP_NAME, obj.getAlias());
                setProperty(node, TeiidSqlLexicon.DerivedColumn.PROPAGATE_NAME_PROP_NAME, obj.isPropagateName());

                visitObject(node, TeiidSqlLexicon.DerivedColumn.EXPRESSION_REF_NAME, obj.getExpression());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(XMLSerialize obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                setDataTypeProperty(node, TeiidSqlLexicon.Expression.TYPE_CLASS_PROP_NAME, obj.getType());
                setProperty(node, TeiidSqlLexicon.XMLSerialize.ENCODING_PROP_NAME, obj.getEncoding());
                setProperty(node, TeiidSqlLexicon.XMLSerialize.VERSION_PROP_NAME, obj.getVersion());
                setProperty(node, TeiidSqlLexicon.XMLSerialize.DECLARATION_PROP_NAME, obj.getDeclaration());
                setProperty(node, TeiidSqlLexicon.XMLSerialize.DOCUMENT_PROP_NAME, obj.getDocument());

                visitObject(node, TeiidSqlLexicon.XMLSerialize.EXPRESSION_REF_NAME, obj.getExpression());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(XMLQuery obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                setDataTypeProperty(node, TeiidSqlLexicon.Expression.TYPE_CLASS_PROP_NAME, obj.getType());
                setProperty(node, TeiidSqlLexicon.XMLQuery.XQUERY_PROP_NAME, obj.getXquery());
                setProperty(node, TeiidSqlLexicon.XMLQuery.EMPTY_ON_EMPTY_PROP_NAME, obj.getEmptyOnEmpty());

                visitObject(node, TeiidSqlLexicon.XMLQuery.NAMESPACES_REF_NAME, obj.getNamespaces());
                visitObjects(node, TeiidSqlLexicon.XMLQuery.PASSING_REF_NAME, obj.getPassing());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(QueryString obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                setDataTypeProperty(node, TeiidSqlLexicon.Expression.TYPE_CLASS_PROP_NAME, obj.getType());

                visitObject(node, TeiidSqlLexicon.QueryString.PATH_REF_NAME, obj.getPath());
                visitObjects(node, TeiidSqlLexicon.QueryString.ARGS_REF_NAME, obj.getArgs());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(XMLParse obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                setDataTypeProperty(node, TeiidSqlLexicon.Expression.TYPE_CLASS_PROP_NAME, obj.getType());
                setProperty(node, TeiidSqlLexicon.XMLParse.DOCUMENT_PROP_NAME, obj.isDocument());
                setProperty(node, TeiidSqlLexicon.XMLParse.WELL_FORMED_PROP_NAME, obj.isWellFormed());

                visitObject(node, TeiidSqlLexicon.XMLParse.EXPRESSION_REF_NAME, obj.getExpression());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(ExpressionCriteria obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                setDataTypeProperty(node, TeiidSqlLexicon.Expression.TYPE_CLASS_PROP_NAME, obj.getType());

                visitObject(node, TeiidSqlLexicon.ExpressionCriteria.EXPRESSION_REF_NAME, obj.getExpression());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(WithQueryCommand obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                visitObject(node, TeiidSqlLexicon.WithQueryCommand.GROUP_SYMBOL_REF_NAME, obj.getGroupSymbol());
                visitObject(node, TeiidSqlLexicon.WithQueryCommand.QUERY_EXPRESSION_REF_NAME, obj.getCommand());
                visitObject(node, TeiidSqlLexicon.SubqueryContainer.COMMAND_REF_NAME, (Command)obj.getCommand().clone());

                visitObjects(node, TeiidSqlLexicon.WithQueryCommand.COLUMNS_REF_NAME, obj.getColumns());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(TriggerAction obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                visitCommand(node, obj);

                visitObject(node, TeiidSqlLexicon.TriggerAction.BLOCK_REF_NAME, obj.getBlock());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(ArrayTable obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                setProperty(node, TeiidSqlLexicon.TableFunctionReference.NAME_PROP_NAME, obj.getName());

                visitFromClause(node, obj);

                visitObject(node, TeiidSqlLexicon.ArrayTable.ARRAY_VALUE_REF_NAME, obj.getArrayValue());

                Context context = localContext(node, TeiidSqlLexicon.ArrayTable.COLUMNS_REF_NAME);
                for (ProjectedColumn column : obj.getColumns()) {
                    Node columnNode = create(column);

                    setProperty(columnNode, TeiidSqlLexicon.ProjectedColumn.NAME_PROP_NAME, column.getName());
                    setProperty(columnNode, TeiidSqlLexicon.ProjectedColumn.TYPE_PROP_NAME, column.getType());
                }
                context.reset();

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(AlterView obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                visitCommand(node, obj);

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(AlterProcedure obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                visitCommand(node, obj);

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(AlterTrigger obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                visitCommand(node, obj);

                setProperty(node, TeiidSqlLexicon.AlterTrigger.EVENT_PROP_NAME, obj.getEvent().name());
                setProperty(node, TeiidSqlLexicon.AlterTrigger.CREATE_PROP_NAME, obj.isCreate());
                setProperty(node, TeiidSqlLexicon.AlterTrigger.ENABLED_PROP_NAME, obj.getEnabled());

                visitObject(node, TeiidSqlLexicon.Alter.TARGET_REF_NAME, obj.getTarget());
                visitObject(node, TeiidSqlLexicon.Alter.DEFINITION_REF_NAME, obj.getDefinition());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(WindowFunction obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                setDataTypeProperty(node, TeiidSqlLexicon.Expression.TYPE_CLASS_PROP_NAME, obj.getType());

                visitObject(node, TeiidSqlLexicon.WindowFunction.FUNCTION_REF_NAME, obj.getFunction());
                visitObject(node, TeiidSqlLexicon.WindowFunction.WINDOW_SPECIFICATION_REF_NAME, obj.getWindowSpecification());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(WindowSpecification obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                visitObject(node, TeiidSqlLexicon.WindowSpecification.ORDER_BY_REF_NAME, obj.getOrderBy());
                visitObjects(node, TeiidSqlLexicon.WindowSpecification.PARTITION_REF_NAME, obj.getPartition());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(ObjectTable obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                visitFromClause(node, obj);

                setProperty(node, TeiidSqlLexicon.TableFunctionReference.NAME_PROP_NAME, obj.getName());
                setProperty(node, TeiidSqlLexicon.ObjectTable.ROW_SCRIPT_PROP_NAME, obj.getRowScript());
                setProperty(node, TeiidSqlLexicon.ObjectTable.SCRIPTING_LANGUAGE_PROP_NAME, obj.getScriptingLanguage());

                visitObjects(node, TeiidSqlLexicon.ObjectTable.PASSING_REF_NAME, obj.getPassing());

                Context context = localContext(node, TeiidSqlLexicon.ObjectTable.COLUMNS_REF_NAME);
                for (ProjectedColumn column : obj.getColumns()) {
                    Node columnNode = create(column);

                    setProperty(columnNode, TeiidSqlLexicon.ProjectedColumn.NAME_PROP_NAME, column.getName());
                    setProperty(columnNode, TeiidSqlLexicon.ProjectedColumn.TYPE_PROP_NAME, column.getType());
                }
                context.reset();

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(ExceptionExpression obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                setDataTypeProperty(node, TeiidSqlLexicon.Expression.TYPE_CLASS_PROP_NAME, obj.getType());

                visitObject(node, TeiidSqlLexicon.ExceptionExpression.MESSAGE_REF_NAME, obj.getMessage());
                visitObject(node, TeiidSqlLexicon.ExceptionExpression.SQL_STATE_REF_NAME, obj.getSqlState());
                visitObject(node, TeiidSqlLexicon.ExceptionExpression.ERROR_CODE_REF_NAME, obj.getErrorCode());
                visitObject(node, TeiidSqlLexicon.ExceptionExpression.PARENT_EXPRESSION_REF_NAME, obj.getParent());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(ReturnStatement obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                visitAssignmentStatement(node, obj);

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(JSONObject obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                setDataTypeProperty(node, TeiidSqlLexicon.Expression.TYPE_CLASS_PROP_NAME, obj.getType());

                visitObjects(node, TeiidSqlLexicon.JSONObject.ARGS_REF_NAME, obj.getArgs());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(XMLExists obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                setProperty(node, TeiidSqlLexicon.Expression.TYPE_CLASS_PROP_NAME, obj.getType());

                visitObject(node, TeiidSqlLexicon.XMLExists.XML_QUERY_REF_NAME, obj.getXmlQuery());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(XMLCast obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                setProperty(node, TeiidSqlLexicon.Expression.TYPE_CLASS_PROP_NAME, obj.getType());

                visitObject(node, TeiidSqlLexicon.XMLCast.EXPRESSION_PROP_NAME, obj.getExpression());

            } catch (Exception ex) {
                setError(ex);
            }
        }

        @Override
        public void visit(IsDistinctCriteria obj) {
            if (errorOccurred())
                return;

            try {
                Node node = transform(obj);

                setProperty(node, TeiidSqlLexicon.IsDistinctCriteria.NEGATED_PROP_NAME, obj.isNegated());

                visitObject(node, TeiidSqlLexicon.IsDistinctCriteria.LEFT_ROW_VALUE_REF_NAME, obj.getLeftRowValue());
                visitObject(node, TeiidSqlLexicon.IsDistinctCriteria.RIGHT_ROW_VALUE_REF_NAME, obj.getRightRowValue());

            } catch (Exception ex) {
                setError(ex);
            }
        }
    }
}
