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
package org.komodo.relational;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.komodo.relational.model.Column;
import org.komodo.relational.model.PrimaryKey;
import org.komodo.relational.model.Table;
import org.komodo.relational.model.UniqueConstraint;
import org.komodo.spi.KException;
import org.komodo.spi.constants.StringConstants;
import org.komodo.spi.lexicon.ddl.StandardDdlLexicon;
import org.komodo.spi.lexicon.sql.teiid.TeiidSqlConstants;
import org.komodo.spi.repository.Property;
import org.komodo.spi.repository.Repository.UnitOfWork;
import org.komodo.utils.StringUtils;

/**
 * ViewDdlBuilder methods for generating view DDL for various scenarios
 */
public class ViewDdlBuilder {

    private static final char SQL_ESCAPE_CHAR = '\"';
    private static final String OPEN_SQUARE_BRACKET = "[";
    private static final String CLOSE_SQUARE_BRACKET = "]";

    /**
     * Inner Join Type
     */
    public static final String JOIN_INNER = "INNER"; //$NON-NLS-1$
    /**
     * Left Outer Join type
     */
    public static final String JOIN_LEFT_OUTER = "LEFT_OUTER"; //$NON-NLS-1$
    /**
     * Right Outer Join type
     */
    public static final String JOIN_RIGHT_OUTER = "RIGHT_OUTER"; //$NON-NLS-1$
    /**
     * Full Outer Join type
     */
    public static final String JOIN_FULL_OUTER = "FULL_OUTER"; //$NON-NLS-1$

    /**
     * Generated View DDL that supports the Teiid OData requirement - that views must have a Primary Key - to get auto-generated.
     * A list of columnNames may be provided of columns to include in the view.  If columnNames is null or empty, then all columns are included.
     *
     * @param uow
     *        the transaction
     * @param viewName
     *        the view name
     * @param table
     *        the table for generating the view
     * @param columnNames
     *        the list of table columns to include in the view (if <code>null</code> or empty, all columns will be included)
     * @return the View DDL
     * @throws KException
     *         if problem occurs
     */
    public static String getODataViewDdl(UnitOfWork uow,
                                         String viewName,
                                         Table table,
                                         List<String> columnNames)
                                         throws KException {

        List<String> includedColumnNames = new ArrayList<String>();
        if( columnNames!=null && !columnNames.isEmpty() ) {
            includedColumnNames.addAll(columnNames);
        }
        boolean includeAllColumns = (includedColumnNames.isEmpty()) ? true : false;
        
        StringBuilder sb = new StringBuilder();
        
        // Determine constraints from table if available
        String constraintStr = getPkConstraint(uow, table);
        if(StringUtils.isEmpty(constraintStr)) {
            constraintStr = getUcConstraint(uow, table);
        }
        
        // If the table has constraints, make sure the constraint columns for the table will be included
        if(constraintStr.length() > 0 && !includeAllColumns) {
            Set<String> constrColNames = getConstraintColumnNames(uow, table);
            for(String constrColName : constrColNames) {
                if(!includedColumnNames.contains(constrColName)) {
                    includedColumnNames.add(constrColName);
                }
            }
        }
        
        // Get table column names and types
        List<String> colNames = new ArrayList<String>();
        List<String> colTypes = new ArrayList<String>();
        Column[] columns = table.getColumns(uow);
        for (int i = 0; i < columns.length; i++) {
            if(includeAllColumns || includedColumnNames.contains(columns[i].getName(uow))) {
                colNames.add(columns[i].getName(uow));
                colTypes.add(getColumnDatatypeString(uow, columns[i]));
            }
        }
        
        // Generate the View DDL
        sb.append("CREATE VIEW "); //$NON-NLS-1$
        sb.append(viewName);
        sb.append(StringConstants.SPACE+StringConstants.OPEN_BRACKET);
        
        // Use generated table constraints if available
        if(constraintStr.length()>0) {
            sb.append(getColWithTypeString(colNames, colTypes));
            sb.append(StringConstants.COMMA+StringConstants.SPACE);
            sb.append(constraintStr);
            sb.append(") AS \n"); //$NON-NLS-1$
            sb.append("SELECT "); //$NON-NLS-1$
            sb.append(getColString(colNames));
        // No table constraint found - generate a primary key
        } else {
            sb.append("RowId integer PRIMARY KEY,"); //$NON-NLS-1$
            sb.append(getColWithTypeString(colNames, colTypes));
            sb.append(") AS \nSELECT ROW_NUMBER() OVER (ORDER BY "); //$NON-NLS-1$
            sb.append(escapeSQLName(colNames.get(0)));
            sb.append(StringConstants.CLOSE_BRACKET + StringConstants.COMMA);
            sb.append(getColString(colNames));
        }
        sb.append(" \n"); //$NON-NLS-1$
        sb.append("FROM "); //$NON-NLS-1$
        String vdbModelName = table.getParent(uow).getName(uow);
        sb.append(escapeSQLName(vdbModelName) + StringConstants.DOT + escapeSQLName(table.getName(uow)));
        sb.append(StringConstants.SEMI_COLON);

        return sb.toString();
    }
    
    /**
     * Generated View DDL that supports the Teiid OData requirement - that views must have a Primary Key - to get auto-generated.
     * @param uow
     *        the transaction
     * @param viewName 
     *        the view name
     * @param lhTable 
     *        the left side table
     * @param lhTableAlias 
     *        the alias to use for the left table
     * @param lhColNames 
     *        the left side column names
     * @param rhTable 
     *        the right side table
     * @param rhTableAlias 
     *        the alias to use for the right table
     * @param rhColNames 
     *        the right side column names
     * @param joinType 
     *        the join type
     * @param criteriaPredicates 
     *        the list of predicates to use for the join criteria
     * @return the View DDL
     * @throws KException
     *         if problem occurs
     */
    public static String getODataViewJoinDdl(UnitOfWork uow, String viewName, 
                                             Table lhTable, String lhTableAlias, List<String> lhColNames, 
                                             Table rhTable, String rhTableAlias, List<String> rhColNames, 
                                             String joinType, List<ViewBuilderCriteriaPredicate> criteriaPredicates) throws KException {

        StringBuilder sb = new StringBuilder();

        // Left and Right table names
        String lhVdbModelName = lhTable.getParent(uow).getName(uow);
        String rhVdbModelName = rhTable.getParent(uow).getName(uow);
        String lhTableNameAliased = lhVdbModelName+StringConstants.DOT+lhTable.getName(uow)+" AS "+lhTableAlias; //$NON-NLS-1$
        String rhTableNameAliased = rhVdbModelName+StringConstants.DOT+rhTable.getName(uow)+" AS "+rhTableAlias; //$NON-NLS-1$
        
        List<String> includedLHColumnNames = new ArrayList<String>();
        if( lhColNames!=null && !lhColNames.isEmpty() ) {
            includedLHColumnNames.addAll(lhColNames);
        }
        boolean includeAllLHColumns = (includedLHColumnNames.isEmpty()) ? true : false;

        List<String> includedRHColumnNames = new ArrayList<String>();
        if( rhColNames!=null && !rhColNames.isEmpty() ) {
            includedRHColumnNames.addAll(rhColNames);
        }
        boolean includeAllRHColumns = (includedRHColumnNames.isEmpty()) ? true : false;

        // Get LHS table column names and types
        List<String> allLeftColNames = new ArrayList<String>();
        List<String> leftColNames = new ArrayList<String>();
        List<String> leftColTypes = new ArrayList<String>();
        Column[] lhCols = lhTable.getColumns(uow);
        for (int i = 0; i < lhCols.length; i++) {
            String lName = lhCols[i].getName(uow);
            allLeftColNames.add(lName);
            if(includeAllLHColumns || includedLHColumnNames.contains(lName)) {
                leftColNames.add(lName);
                leftColTypes.add(lhCols[i].getDatatypeName(uow));
            }
        }
        
        // Get RHS table column names and types
        List<String> allRightColNames = new ArrayList<String>();
        List<String> rightColNames = new ArrayList<String>();
        List<String> rightColTypes = new ArrayList<String>();
        Column[] rhCols = rhTable.getColumns(uow);
        for (int i = 0; i < rhCols.length; i++) {
            String rName = rhCols[i].getName(uow);
            allRightColNames.add(rName);
            if(includeAllRHColumns || includedRHColumnNames.contains(rName)) {
                rightColNames.add(rName);
                rightColTypes.add(rhCols[i].getDatatypeName(uow));
            }
        }
        
        sb.append("CREATE VIEW "); //$NON-NLS-1$
        sb.append(viewName);
        sb.append(StringConstants.SPACE+StringConstants.OPEN_BRACKET);

        sb.append("RowId integer PRIMARY KEY, "); //$NON-NLS-1$
        // Get list of columns that are duplicated in LH and RH and will need to be alias prefixed.
        List<String> duplicateColNames = getDuplicateColumnNames(leftColNames, rightColNames);
        sb.append(getAliasedColWithTypeString(leftColNames, leftColTypes, lhTableAlias, duplicateColNames));
        sb.append(StringConstants.COMMA+StringConstants.SPACE);
        sb.append(getAliasedColWithTypeString(rightColNames, rightColTypes, rhTableAlias, duplicateColNames));
        sb.append(") AS \nSELECT "); //$NON-NLS-1$
        sb.append("ROW_NUMBER() OVER (ORDER BY "); //$NON-NLS-1$
        sb.append(getAliasedFirstColName(leftColNames, lhTableAlias, rightColNames, rhTableAlias));
        sb.append("), "); //$NON-NLS-1$
        if(leftColNames.size()>0) {
            sb.append(getAliasedColString(leftColNames, lhTableAlias));
            sb.append(StringConstants.COMMA+StringConstants.SPACE);
        }
        sb.append(getAliasedColString(rightColNames, rhTableAlias));
        sb.append(" \nFROM \n"); //$NON-NLS-1$
        sb.append(lhTableNameAliased+StringConstants.SPACE);
        if(JOIN_INNER.equals(joinType)) {
            sb.append("\nINNER JOIN \n").append(rhTableNameAliased+StringConstants.SPACE); //$NON-NLS-1$
        } else if(JOIN_LEFT_OUTER.equals(joinType)) {
            sb.append("\nLEFT OUTER JOIN \n").append(rhTableNameAliased+StringConstants.SPACE); //$NON-NLS-1$
        } else if(JOIN_RIGHT_OUTER.equals(joinType)) {
            sb.append("\nRIGHT OUTER JOIN \n").append(rhTableNameAliased+StringConstants.SPACE); //$NON-NLS-1$
        } else if(JOIN_FULL_OUTER.equals(joinType)) {
            sb.append("\nFULL OUTER JOIN \n").append(rhTableNameAliased+StringConstants.SPACE); //$NON-NLS-1$
        } else {
            sb.append("\nINNER JOIN \n").append(rhTableNameAliased+StringConstants.SPACE); //$NON-NLS-1$
        }
        if(criteriaPredicates!=null && criteriaPredicates.size()>0) {
            sb.append("\nON \n"); //$NON-NLS-1$
            int nPredicates = criteriaPredicates.size();
            for(int i=0; i < nPredicates; i++) {
                String lhCol = criteriaPredicates.get(i).getLhColumn();
                String rhCol = criteriaPredicates.get(i).getRhColumn();
                String oper = criteriaPredicates.get(i).getOperator();
                String keyword = criteriaPredicates.get(i).getCombineKeyword();
                
                sb.append(lhTableAlias+StringConstants.DOT).append(escapeSQLName(lhCol))
                .append(StringConstants.SPACE+oper+StringConstants.SPACE)
                .append(rhTableAlias+StringConstants.DOT).append(escapeSQLName(rhCol));
                
                if( i != nPredicates-1 ) {
                    sb.append(StringConstants.SPACE+keyword+StringConstants.SPACE);
                }
            }
        }
        sb.append(StringConstants.SEMI_COLON);

        return sb.toString();
    }
    
    /**
     * Get the list of names that are duplicate(case insensitive) between the two supplied lists.
     * @param lhColNames the list of column names for the left table
     * @param rhColNames the list of column names for the right table
     * @return the list of duplicated (case insensitive) names.  The returned names are lower cased
     */
    private static List<String> getDuplicateColumnNames(List<String> lhColNames, List<String> rhColNames) {
        List<String> duplicateNames = new ArrayList<String>();
        for(String lhColName : lhColNames) {
            if(containsCaseInsensitive(lhColName, rhColNames)) {
                duplicateNames.add(lhColName.toLowerCase());
            }
        }
        return duplicateNames;
    }
    
    /**
     * Determine if the supplied String s is contained by the supplied list (case insensitive)
     * @param s the string to test
     * @param l the list of names to test against
     * @return 'true' if the list contains the string (case insensitive) 
     */
    private static boolean containsCaseInsensitive(String s, List<String> l){
        for (String string : l){
            if (string.equalsIgnoreCase(s)){
                return true;
            }
        }
        return false;
    }
    
    private static Set<String> getConstraintColumnNames(UnitOfWork uow, Table table) throws KException {
        Set<String> constraintCols = new HashSet<String>();
        
        // Add primary key column if present
        PrimaryKey pk = table.getPrimaryKey(uow);
        if(pk!=null) {
            Column[] pkCols = pk.getColumns(uow);
            for(Column pkCol : pkCols) {
                constraintCols.add(pkCol.getName(uow));
            }
        } else {
            // Add UC column names if present
            UniqueConstraint[] ucs = table.getUniqueConstraints(uow);
            for(UniqueConstraint uc : ucs) {
                Column[] ucCols = uc.getColumns(uow);
                for(Column col : ucCols) {
                    constraintCols.add(col.getName(uow));
                }
            }
        }
        
        return constraintCols;
    }
    
    /*
     * Generates Primary Key constraint string if table has a PK
     * Will be of form: "CONSTRAINT pkName PRIMARY KEY (col1)"
     */
    private static String getPkConstraint(UnitOfWork uow, Table table) throws KException {
        StringBuilder sb = new StringBuilder();

        // Look for pk column
        PrimaryKey pk = table.getPrimaryKey(uow);
        if(pk!=null) {
            sb.append("CONSTRAINT "); //$NON-NLS-1$
            sb.append(escapeSQLName(pk.getName(uow)));
            sb.append(" PRIMARY KEY ("); //$NON-NLS-1$
            Column[] pkCols = pk.getColumns(uow);
            for(int i=0; i<pkCols.length; i++) {
                if(i!=0) sb.append(StringConstants.COMMA+StringConstants.SPACE);
                sb.append(escapeSQLName(pkCols[i].getName(uow)));
            }
            sb.append(StringConstants.CLOSE_BRACKET);
        }
        return sb.toString();
    }
    
    /*
     * Generates UniqueConstraint string if table has a PK
     * Will be of form: "CONSTRAINT ucName UNIQUE (col1, col2)"
     */
    private static String getUcConstraint(UnitOfWork uow, Table table) throws KException {
        StringBuilder sb = new StringBuilder();

        // Look for uc
        UniqueConstraint[] ucs = table.getUniqueConstraints(uow);
        for(int iuc=0; iuc<ucs.length; iuc++) {
            if(iuc!=0) sb.append(StringConstants.COMMA);
            sb.append("CONSTRAINT "); //$NON-NLS-1$
            sb.append(escapeSQLName(ucs[iuc].getName(uow)));
            sb.append(" UNIQUE ("); //$NON-NLS-1$
            Column[] ucCols = ucs[iuc].getColumns(uow);
            for(int icol=0; icol<ucCols.length; icol++) {
                if(icol!=0) sb.append(StringConstants.COMMA+StringConstants.SPACE);
                sb.append(escapeSQLName(ucCols[icol].getName(uow)));
            }
            sb.append(StringConstants.CLOSE_BRACKET);
        }
        return sb.toString();
    }
    
    /*
     * Generates comma separated string of the supplied column names
     * Will be of form: "column1, column2, column3"
     */
    private static String getColString(List<String> columnNames) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < columnNames.size(); i++) {
            if (i != 0) {
                sb.append(StringConstants.COMMA);
            }
            sb.append(StringConstants.SPACE + escapeSQLName(columnNames.get(i)));
        }
        return sb.toString();
    }

    /*
     * Generates comma separated string of the supplied column name with corresponding type
     * Will be of form: "column1 string, column2 string, column3 long"
     */
    private static String getColWithTypeString(List<String> columnNames,
                                               List<String> typeNames) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < columnNames.size(); i++) {
            if (i != 0) {
                sb.append(StringConstants.COMMA);
            }
            sb.append(StringConstants.SPACE + escapeSQLName(columnNames.get(i)));
            sb.append(StringConstants.SPACE);
            sb.append(typeNames.get(i));
        }
        return sb.toString();
    }

    /*
     * Generates comma separated string of the supplied column names with their corresponding type.
     * If columns are in the duplicateNames list, then prefix them with the supplied alias.
     * Will be of form: "alias_column1 string, alias_column2 string, column3 long"
     */
    private static String getAliasedColWithTypeString(List<String> columnNames,
                                                      List<String> typeNames, String alias, List<String> duplicateNames) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < columnNames.size(); i++) {
            if (i != 0) {
                sb.append(StringConstants.COMMA);
            }

            // If the columnName is a duplicate, prefix it with "alias_"
            String colName = columnNames.get(i);
            if(duplicateNames.contains(colName.toLowerCase())) {
                sb.append(StringConstants.SPACE + alias + StringConstants.UNDERSCORE + escapeSQLName(colName));
            } else {
                sb.append(StringConstants.SPACE + escapeSQLName(colName));
            }
            sb.append(StringConstants.SPACE);
            sb.append(typeNames.get(i));
        }
        return sb.toString();
    }

    /**
     * Returns the aliased first column name
     * @param lhsColNames the list of LHS column names
     * @param lhsAlias the LHS alias
     * @param rhsColNames the list of RHS column names
     * @param rhsAlias the RHS alias
     * @return the first column with alias
     */
    private static String getAliasedFirstColName(List<String> lhsColNames, String lhsAlias, List<String> rhsColNames, String rhsAlias) {
        String result = null;
        if(lhsColNames.size()>0) {
            List<String> aliasedLhsColNames = getAliasedColNames(lhsColNames,lhsAlias);
            result = aliasedLhsColNames.get(0);
        } else if(rhsColNames.size()>0) {
            List<String> aliasedRhsColNames = getAliasedColNames(rhsColNames,rhsAlias);
            result = aliasedRhsColNames.get(0);
        }
        return result;
    }
    
    /**
     * Prepend each column name with the alias
     * @param colNames the column names
     * @param alias the alias
     * @return the aliased names
     */
    private static List<String> getAliasedColNames(List<String> colNames, String alias) {
        List<String> aliasedNames = new ArrayList<String>(colNames.size());
        for(String colName : colNames) {
            aliasedNames.add(alias+StringConstants.DOT+escapeSQLName(colName));
        }
        return aliasedNames;
    }

    private static String getAliasedColString(List<String> columnNames, String alias) {
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<columnNames.size(); i++) {
            if(i!=0 ) {
                sb.append(StringConstants.COMMA + StringConstants.SPACE);
            }
            sb.append(alias).append(StringConstants.DOT).append(escapeSQLName(columnNames.get(i))); 
        }
        return sb.toString();
    }
    
    private static String escapeSQLName(String part) {
        if (TeiidSqlConstants.isReservedWord(part)) {
            return SQL_ESCAPE_CHAR + part + SQL_ESCAPE_CHAR;
        }
        boolean escape = true;
        char start = part.charAt(0);
        if (start == '#' || start == '@' || isLetter(start)) {
            escape = false;
            for (int i = 1; !escape && i < part.length(); i++) {
                char c = part.charAt(i);
                escape = !isLetterOrDigit(c) && c != '_';
            }
        }
        if (escape) {
            return SQL_ESCAPE_CHAR + part + SQL_ESCAPE_CHAR;
        }
        return part;
    }

    private static boolean isLetter(char c) {
        return isBasicLatinLetter(c) || Character.isLetter(c);
    }

    private static boolean isLetterOrDigit(char c) {
        return isBasicLatinLetter(c) || isBasicLatinDigit(c) || Character.isLetterOrDigit(c);
    }

    private static boolean isBasicLatinLetter(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private static boolean isBasicLatinDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private static String getColumnDatatypeString(UnitOfWork uow, Column col) throws KException {
    	String typeName = col.getDatatypeName(uow);
    	
    	// Determine if array type
        if (col.hasRawProperty(uow, StandardDdlLexicon.DATATYPE_ARRAY_DIMENSIONS)) {
            Property colArrDimsProp = col.getRawProperty(uow, StandardDdlLexicon.DATATYPE_ARRAY_DIMENSIONS);
            long colArrDims = colArrDimsProp != null ? colArrDimsProp.getLongValue(uow) : -1;

            for (long dims = colArrDims; dims > 0; dims--) {
                typeName = typeName.concat(OPEN_SQUARE_BRACKET).concat(CLOSE_SQUARE_BRACKET);
            }
        }
    
        return typeName;
    }
}
