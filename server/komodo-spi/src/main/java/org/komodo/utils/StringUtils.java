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
package org.komodo.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;
import org.komodo.spi.constants.StringConstants;

/**
 * This is a common place to put String utility methods.
 *
 *
 */
public final class StringUtils implements StringConstants {

//    /**
//     * The String "'"
//     */
//    public static final String SINGLE_QUOTE = "'"; //$NON-NLS-1$
//
//    /**
//     * The name of the System property that specifies the string that should be used to separate lines. This property is a
//     * standard environment property that is usually set automatically.
//     */
//    public static final String LINE_SEPARATOR_PROPERTY_NAME = "line.separator"; //$NON-NLS-1$
//
//    /**
//     * The String that should be used to separate lines; defaults to {@link StringConstants#NEW_LINE}
//     */
//    public static final String LINE_SEPARATOR = System.getProperty(LINE_SEPARATOR_PROPERTY_NAME, NEW_LINE);
//
//    public static final Comparator CASE_INSENSITIVE_ORDER = String.CASE_INSENSITIVE_ORDER;
//
//    public static final Comparator CASE_SENSITIVE_ORDER = new Comparator() {
//        /**
//         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
//         *
//         */
//        @Override
//        public int compare( Object o1,
//                            Object o2 ) {
//            if (o1 == o2) {
//                return 0;
//            }
//            return ((String)o1).compareTo((String)o2);
//        }
//    };
//
//    public static String getLineSeparator() {
//        return LINE_SEPARATOR;
//    }
//
//    /**
//     * Utility to return a string enclosed in ''. Creation date: (12/2/99 12:05:10 PM)
//     */
//    public static String enclosedInSingleQuotes( String aString ) {
//        StringBuffer sb = new StringBuffer();
//        sb.append(SINGLE_QUOTE);
//        sb.append(aString);
//        sb.append(SINGLE_QUOTE);
//        return sb.toString();
//    }

    /**
     * Join string pieces and separate with a delimiter. Similar to the perl function of the same name. If strings or delimiter
     * are null, null is returned. Otherwise, at least an empty string will be returned.
     *
     * @param strings String pieces to join
     * @param delimiter Delimiter to put between string pieces
     * @return One merged string
     */
    public static String join( List<?> strings,
                               String delimiter ) {
        if (strings == null || delimiter == null) {
            return null;
        }

        StringBuffer str = new StringBuffer();

        // This is the standard problem of not putting a delimiter after the last
        // string piece but still handling the special cases. A typical way is to check every
        // iteration if it is the last one and skip the delimiter - this is avoided by
        // looping up to the last one, then appending just the last one.

        // First we loop through all but the last one (if there are at least 2) and
        // put the piece and a delimiter after it. An iterator is used to walk the list.
        int most = strings.size() - 1;
        if (strings.size() > 1) {
            Iterator<?> iter = strings.iterator();
            for (int i = 0; i < most; i++) {
                str.append(iter.next());
                str.append(delimiter);
            }
        }

        // If there is at least one element, put the last one on with no delimiter after.
        if (strings.size() > 0) {
            str.append(strings.get(most));
        }

        return str.toString();
    }
//
//    /**
//     * Return a stringified version of the array.
//     *
//     * @param array the array
//     * @param delim the delimiter to use between array components
//     * @return the string form of the array
//     */
//    public static String toString( final Object[] array,
//                                   final String delim ) {
//        if (array == null) {
//            return ""; //$NON-NLS-1$
//        }
//        if (array.length == 0) {
//            return "[]"; //$NON-NLS-1$
//        }
//        final StringBuffer sb = new StringBuffer();
//        sb.append('[');
//        for (int i = 0; i < array.length; ++i) {
//            if (i != 0) {
//                sb.append(delim);
//            }
//            sb.append(array[i]);
//        }
//        sb.append(']');
//        return sb.toString();
//    }
//
//    /**
//     * Return a stringified version of the array, using a ',' as a delimiter
//     *
//     * @param array the array
//     * @return the string form of the array
//     * @see #toString(Object[], String)
//     */
//    public static String toString( final Object[] array ) {
//        return toString(array, ","); //$NON-NLS-1$
//    }
//
//    /**
//     * Split a string into pieces based on delimiters. Similar to the perl function of the same name. The delimiters are not
//     * included in the returned strings.
//     *
//     * @see #join
//     * @param str Full string
//     * @param splitter Characters to split on
//     * @return List of String pieces from full string
//     */
//    public static List split( String str,
//                              String splitter ) {
//        StringTokenizer tokens = new StringTokenizer(str, splitter);
//        ArrayList l = new ArrayList(tokens.countTokens());
//        while (tokens.hasMoreTokens()) {
//            l.add(tokens.nextToken());
//        }
//        return l;
//    }
//
//    /**
//     * Break a string into pieces based on matching the full delimiter string in the text. The delimiter is not included in the
//     * returned strings.
//     *
//     * @param target The text to break up.
//     * @param delimiter The sub-string which is used to break the target.
//     * @return List of String from the target.
//     */
//    public static List splitOnEntireString( String target,
//                                            String delimiter ) {
//        ArrayList result = new ArrayList();
//        if (delimiter.length() > 0) {
//            int index = 0;
//            int indexOfNextMatch = target.indexOf(delimiter);
//            while (indexOfNextMatch > -1) {
//                result.add(target.substring(index, indexOfNextMatch));
//                index = indexOfNextMatch + delimiter.length();
//                indexOfNextMatch = target.indexOf(delimiter, index);
//            }
//            if (index <= target.length()) {
//                result.add(target.substring(index));
//            }
//        } else {
//            result.add(target);
//        }
//        return result;
//    }
//
//    /**
//     * Split a string into pieces based on delimiters preserving spaces in quoted substring as on element in the returned list.
//     * The delimiters are not included in the returned strings.
//     *
//     * @see #join
//     * @param str Full string
//     * @param splitter Characters to split on
//     * @return List of String pieces from full string
//     */
//    public static List splitPreservingQuotedSubstring( String str,
//                                                       String splitter ) {
//        ArrayList l = new ArrayList();
//        StringTokenizer tokens = new StringTokenizer(str, splitter);
//        StringBuffer token = new StringBuffer();
//        while (tokens.hasMoreTokens()) {
//            token.setLength(0);
//            token.append(tokens.nextToken());
//            if (token.charAt(0) == '"') {
//                token.deleteCharAt(0);
//                while (tokens.hasMoreTokens()) {
//                    token.append(Constants.SPACE + tokens.nextToken());
//                    if (token.charAt(token.length() - 1) == '"') {
//                        token.deleteCharAt(token.length() - 1);
//                        break;
//                    }
//                }
//            }
//            l.add(token.toString().trim());
//        }
//        return l;
//    }
//
//    /*
//     * Replace a single occurrence of the search string with the replace string
//     * in the source string. If any of the strings is null or the search string
//     * is zero length, the source string is returned.
//     * @param source the source string whose contents will be altered
//     * @param search the string to search for in source
//     * @param replace the string to substitute for search if present
//     * @return source string with the *first* occurrence of the search string
//     * replaced with the replace string
//     */
//    public static String replace( String source,
//                                  String search,
//                                  String replace ) {
//        if (source != null && search != null && search.length() > 0 && replace != null) {
//            int start = source.indexOf(search);
//            if (start > -1) {
//                return new StringBuffer(source).replace(start, start + search.length(), replace).toString();
//            }
//        }
//        return source;
//    }

    /**
     * Replace all occurrences of the search string with the replace string
     * in the source string. If any of the strings is null or the search string
     * is zero length, the source string is returned.
     * @param source the source string whose contents will be altered
     * @param search the string to search for in source
     * @param replace the string to substitute for search if present
     * @return source string with *all* occurrences of the search string
     * replaced with the replace string
     */
    public static String replaceAll( String source,
                                     String search,
                                     String replace ) {
        if (source != null && search != null && search.length() > 0 && replace != null) {
            int start = source.indexOf(search);
            if (start > -1) {
                StringBuffer newString = new StringBuffer(source);
                replaceAll(newString, search, replace);
                return newString.toString();
            }
        }
        return source;
    }

    /**
     * @param source the source string whose contents will be altered
     * @param search the string to search for in source
     * @param replace the string to substitute for search if present
     */
    public static void replaceAll( StringBuffer source,
                                   String search,
                                   String replace ) {
        if (source != null && search != null && search.length() > 0 && replace != null) {
            int start = source.toString().indexOf(search);
            while (start > -1) {
                int end = start + search.length();
                source.replace(start, end, replace);
                start = source.toString().indexOf(search, start + replace.length());
            }
        }
    }
//
//    /**
//     * Simple static method to tuncate Strings to given length.
//     *
//     * @param in the string that may need tuncating.
//     * @param len the lenght that the string should be truncated to.
//     * @return a new String containing chars with length <= len or <code>null</code> if input String is <code>null</code>.
//     */
//    public static String truncString( String in,
//                                      int len ) {
//        String out = in;
//        if (in != null && len > 0 && in.length() > len) {
//            out = in.substring(0, len);
//        }
//        return out;
//    }
//
//    /**
//     * Simple utility method to wrap a string by inserting line separators creating multiple lines each with length no greater
//     * than the user specified maximum. The method parses the given string into tokens using a space delimiter then reassembling
//     * the tokens into the resulting string while inserting separators when required. If the number of characters in a single
//     * token is greater than the specified maximum, the token will not be split but instead the maximum will be exceeded.
//     *
//     * @param str the string that may need tuncating.
//     * @param maxCharPerLine the max number of characters per line
//     * @return a new String containing line separators or the original string if its length was less than the maximum.
//     */
//    public static String wrap( String str,
//                               int maxCharPerLine ) {
//        int strLength = str.length();
//        if (strLength > maxCharPerLine) {
//            StringBuffer sb = new StringBuffer(str.length() + (strLength / maxCharPerLine) + 1);
//            strLength = 0;
//            List tokens = StringUtils.split(str, Constants.SPACE);
//            Iterator itr = tokens.iterator();
//            while (itr.hasNext()) {
//                String token = (String)itr.next();
//                if (strLength + token.length() > maxCharPerLine) {
//                    // sb.append(getLineSeparator());
//                    sb.append(NEW_LINE);
//                    strLength = 0;
//                }
//                sb.append(token);
//                sb.append(Constants.SPACE);
//                strLength += token.length() + 1;
//            }
//            return sb.toString();
//        }
//        return str;
//    }
//
//    /**
//     * Return the tokens in a string in a list. This is particularly helpful if the tokens need to be processed in reverse order.
//     * In that case, a list iterator can be acquired from the list for reverse order traversal.
//     *
//     * @param str String to be tokenized
//     * @param delimiter Characters which are delimit tokens
//     * @return List of string tokens contained in the tokenized string
//     */
//    public static List getTokens( String str,
//                                  String delimiter ) {
//        ArrayList l = new ArrayList();
//        StringTokenizer tokens = new StringTokenizer(str, delimiter);
//        while (tokens.hasMoreTokens()) {
//            l.add(tokens.nextToken());
//        }
//        return l;
//    }
//
//    /**
//     * Return the number of tokens in a string that are seperated by the delimiter.
//     *
//     * @param str String to be tokenized
//     * @param delimiter Characters which are delimit tokens
//     * @return Number of tokens seperated by the delimiter
//     */
//    public static int getTokenCount( String str,
//                                     String delimiter ) {
//        StringTokenizer tokens = new StringTokenizer(str, delimiter);
//        return tokens.countTokens();
//    }
//
//    /**
//     * Return the number of occurrences of token string that occurs in input string. Note: token is case sensitive.
//     *
//     * @param input
//     * @param token
//     * @return int
//     */
//    public static int occurrences( String input,
//                                   String token ) {
//        int num = 0;
//        int index = input.indexOf(token);
//        while (index >= 0) {
//            num++;
//            index = input.indexOf(token, index + 1);
//        }
//        return num;
//    }
//
    /**
     * Return the last token in the string.
     *
     * @param str String to be tokenized
     * @param delimiter Characters which are delimit tokens
     * @return the last token contained in the tokenized string
     */
    public static String getLastToken( String str,
                                       String delimiter ) {
        if (str == null) {
            return EMPTY_STRING;
        }
        int beginIndex = 0;
        if (str.lastIndexOf(delimiter) > 0) {
            beginIndex = str.lastIndexOf(delimiter) + 1;
        }
        return str.substring(beginIndex, str.length());
    }

//    /**
//     * Return the first token in the string.
//     *
//     * @param str String to be tokenized
//     * @param delimiter Characters which are delimit tokens
//     * @return the first token contained in the tokenized string
//     */
//    public static String getFirstToken( String str,
//                                        String delimiter ) {
//        if (str == null) {
//            return EMPTY_STRING;
//        }
//        int endIndex = str.indexOf(delimiter);
//        if (endIndex < 0) {
//            endIndex = str.length();
//        }
//        return str.substring(0, endIndex);
//    }
//
//    /**
//     * Compute a displayable form of the specified string. This algorithm attempts to create a string that contains words that
//     * begin with uppercase characters and that are separated by a single space. For example, the following are the outputs of
//     * some sample inputs: <li>"aName" is converted to "A Name"</li> <li>"Name" is converted to "Name"</li> <li>"NAME" is
//     * converted to "NAME"</li> <li>"theName" is converted to "The Name"</li> <li>"theBIGName" is converted to "The BIG Name"</li>
//     * <li>"the BIG Name" is converted to "The BIG Name"</li> <li>"the big Name" is converted to "The Big Name"</li> <li>"theBIG"
//     * is converted to "The BIG"</li> <li>"SQLIndex" is converted to "SQL Index"</li> <li>"SQLIndexT" is converted to
//     * "SQL Index T"</li> <li>"SQLIndex T" is converted to "SQL Index T"</li> <li>"SQLIndex t" is converted to "SQL Index T"</li>
//     *
//     * @param str String to be converted; may be null
//     * @return the displayable form of <code>str</code>, or an empty string if <code>str</code> is either null or zero-length;
//     *         never null
//     */
//    public static String computeDisplayableForm( String str ) {
//        return computeDisplayableForm(str, EMPTY_STRING);
//    }
//
//    /**
//     * Compute a displayable form of the specified string. This algorithm attempts to create a string that contains words that
//     * begin with uppercase characters and that are separated by a single space. For example, the following are the outputs of
//     * some sample inputs: <li>"aName" is converted to "A Name"</li> <li>"Name" is converted to "Name"</li> <li>"NAME" is
//     * converted to "NAME"</li> <li>"theName" is converted to "The Name"</li> <li>"theBIGName" is converted to "The BIG Name"</li>
//     * <li>"the BIG Name" is converted to "The BIG Name"</li> <li>"the big Name" is converted to "The Big Name"</li> <li>"theBIG"
//     * is converted to "The BIG"</li> <li>"SQLIndex" is converted to "SQL Index"</li> <li>"SQLIndexT" is converted to
//     * "SQL Index T"</li> <li>"SQLIndex T" is converted to "SQL Index T"</li> <li>"SQLIndex t" is converted to "SQL Index T"</li>
//     * <p>
//     * An exception is "MetaMatrix", which is always treated as a single word
//     * </p>
//     *
//     * @param str String to be converted; may be null
//     * @param defaultValue the default result if the input is either null or zero-length.
//     * @return the displayable form of <code>str</code>, or the default value if <code>str</code> is either null or zero-length.
//     */
//    public static String computeDisplayableForm( String str,
//                                                 String defaultValue ) {
//        if (str == null || str.length() == 0) {
//            return defaultValue;
//        }
//
//        StringBuffer newName = new StringBuffer(str);
//        boolean previousCharUppercase = false;
//
//        // If the first character is lowercase, replace it with the uppercase ...
//        char prevChar = newName.charAt(0);
//        if (Character.isLowerCase(prevChar)) {
//            newName.setCharAt(0, Character.toUpperCase(prevChar));
//            previousCharUppercase = true;
//        }
//
//        if (newName.length() > 1) {
//            char nextChar;
//            char currentChar;
//            boolean currentCharUppercase;
//            boolean nextCharUppercase;
//            for (int i = 1; i != newName.length(); ++i) {
//                prevChar = newName.charAt(i - 1);
//                currentChar = newName.charAt(i);
//                previousCharUppercase = Character.isUpperCase(prevChar);
//                currentCharUppercase = Character.isUpperCase(currentChar);
//                // In the case where we're not at the end of the string ...
//                if (i != newName.length() - 1) {
//                    nextChar = newName.charAt(i + 1);
//                    nextCharUppercase = Character.isUpperCase(nextChar);
//                } else {
//                    nextCharUppercase = false;
//                    nextChar = ' ';
//                }
//
//                // If the previous character is a space, capitalize the current character
//                if (prevChar == ' ') {
//                    newName.setCharAt(i, Character.toUpperCase(currentChar));
//                    // do nothing
//                }
//                // Otherwise, if the current character is already uppercase ...
//                else if (currentCharUppercase) {
//                    // ... and the previous character is not uppercase, then insert
//                    if (!previousCharUppercase) {
//                        // ... and this is not the 'M' of 'MetaMatrix' ...
//                        if (currentChar != 'M'
//                        || i < 4
//                        || (!newName.substring(i - 4).startsWith(Messages.getString(Messages.StringUtil.displayable)))) {
//                            newName.insert(i, ' ');
//                            ++i; // skip, since we just move the character back one position
//                        }
//                    }
//                    // ... and the previous character is uppercase ...
//                    else {
//                        // ... but the next character neither uppercase or a space ...
//                        if (!nextCharUppercase && nextChar != ' ') {
//                            newName.insert(i, ' ');
//                            ++i; // skip, since we just move the character back one position
//                        }
//                    }
//                }
//            }
//        }
//
//        return newName.toString();
//    }
//
//    /**
//     *
//     */
//    public static String computeDisplayableFormOfConstant( final String text ) {
//        return computeDisplayableFormOfConstant(text, EMPTY_STRING);
//    }
//
//    /**
//     *
//     */
//    public static String computeDisplayableFormOfConstant( final String text,
//                                                           final String defaultValue ) {
//        if (text == null || text.length() == 0) {
//            return defaultValue;
//        }
//        final StringBuffer buf = new StringBuffer();
//        String token;
//        for (final StringTokenizer iter = new StringTokenizer(text, "_"); iter.hasMoreTokens();) { //$NON-NLS-1$
//            token = iter.nextToken().toLowerCase();
//            if (buf.length() > 0) {
//                buf.append(' ');
//            }
//            buf.append(Character.toUpperCase(token.charAt(0)));
//            buf.append(token.substring(1));
//        }
//        return buf.toString();
//    }
//
//    public static String computePluralForm( String str ) {
//        return computePluralForm(str, EMPTY_STRING);
//    }
//
//    public static String computePluralForm( String str,
//                                            String defaultValue ) {
//        if (str == null || str.length() == 0) {
//            return defaultValue;
//        }
//        String result = str;
//        if (result.endsWith("es")) { //$NON-NLS-1$
//            // do nothing
//        } else if (result.endsWith("ss") || //$NON-NLS-1$
//        result.endsWith("x") || //$NON-NLS-1$
//        result.endsWith("ch") || //$NON-NLS-1$
//        result.endsWith("sh")) { //$NON-NLS-1$
//            result = result + "es"; //$NON-NLS-1$
//        } else if (result.endsWith("y") && !( //$NON-NLS-1$
//        result.endsWith("ay") || //$NON-NLS-1$
//        result.endsWith("ey") || //$NON-NLS-1$
//        result.endsWith("iy") || //$NON-NLS-1$
//        result.endsWith("oy") || //$NON-NLS-1$
//        result.endsWith("uy") || //$NON-NLS-1$
//        result.equalsIgnoreCase("any"))) { //$NON-NLS-1$
//            result = result.substring(0, result.length() - 1) + "ies"; //$NON-NLS-1$
//        } else {
//            result += "s"; //$NON-NLS-1$
//        }
//        return result;
//    }
//
//    public static String getStackTrace( final Throwable t ) {
//        final ByteArrayOutputStream bas = new ByteArrayOutputStream();
//        final PrintWriter pw = new PrintWriter(bas);
//        t.printStackTrace(pw);
//        pw.close();
//        return bas.toString();
//    }
//
//    /**
//     * Returns whether the specified text represents a boolean value, i.e., whether it equals "true" or "false"
//     * (case-insensitive).
//     *
//     *
//     */
//    public static boolean isBoolean( final String text ) {
//        return (Boolean.TRUE.toString().equalsIgnoreCase(text) || Boolean.FALSE.toString().equalsIgnoreCase(text));
//    }

    /**
     * <p>
     * Returns whether the specified text is either empty or null.
     * </p>
     *
     * @param text The text to check; may be null;
     * @return True if the specified text is either empty or null.
     *
     */
    public static boolean isEmpty( final String text ) {
        return (text == null || text.length() == 0);
    }

    /**
     * Compare string values - considered equal if either are null or empty.
     *
     * @param thisValue the first value being compared (can be <code>null</code> or empty)
     * @param thatValue the other value being compared (can be <code>null</code> or empty)
     * @return <code>true</code> if values are equal or both values are empty
     */
    public static boolean valuesAreEqual( String thisValue,
                                          String thatValue ) {
        if (isEmpty(thisValue) && isEmpty(thatValue)) {
            return true;
        }

        return equals(thisValue, thatValue);
    }

    /**
     * Compare string values - considered equal if either are null or empty.
     * Ignores case
     * @param thisValue the first value being compared (can be <code>null</code> or empty)
     * @param thatValue the other value being compared (can be <code>null</code> or empty)
     * @return <code>true</code> if values are equal or both values are empty
     */
    public static boolean valuesAreEqualIgnoreCase( String thisValue,
                                                    String thatValue ) {
        if (isEmpty(thisValue) && isEmpty(thatValue)) {
            return true;
        }

        return equalsIgnoreCase(thisValue, thatValue);
    }
//
//    /**
//     * Returns the index within this string of the first occurrence of the specified substring. The integer returned is the
//     * smallest value <i>k</i> such that: <blockquote>
//     *
//     * <pre>
//     * this.startsWith(str, &lt;i&gt;k&lt;/i&gt;)
//     * </pre>
//     *
//     * </blockquote> is <code>true</code>.
//     *
//     * @param text any string.
//     * @param str any string.
//     * @return if the str argument occurs as a substring within text, then the index of the first character of the first such
//     *         substring is returned; if it does not occur as a substring, <code>-1</code> is returned. If the text or str
//     *         argument is null or empty then <code>-1</code> is returned.
//     */
//    public static int indexOfIgnoreCase( final String text,
//                                         final String str ) {
//        if (isEmpty(text)) {
//            return -1;
//        }
//        if (isEmpty(str)) {
//            return -1;
//        }
//        final String lowerText = text.toLowerCase();
//        final String lowerStr = str.toLowerCase();
//        return lowerText.indexOf(lowerStr);
//    }
//
//    /**
//     * Tests if the string starts with the specified prefix.
//     *
//     * @param text the string to test.
//     * @param prefix the prefix.
//     * @return <code>true</code> if the character sequence represented by the argument is a prefix of the character sequence
//     *         represented by this string; <code>false</code> otherwise. Note also that <code>true</code> will be returned if the
//     *         prefix is an empty string or is equal to the text <code>String</code> object as determined by the
//     *         {@link #equals(Object)} method. If the text or prefix argument is null <code>false</code> is returned.
//     * @since JDK1. 0
//     */
//    public static boolean startsWithIgnoreCase( final String text,
//                                                final String prefix ) {
//        if (isEmpty(text)) {
//            return false;
//        }
//        if (prefix == null) {
//            return false;
//        }
//        int textLength = text.length();
//        int prefixLength = prefix.length();
//        if (prefixLength == 0) {
//            return true;
//        }
//        if (prefixLength > textLength) {
//            return false;
//        }
//        char[] chArray = prefix.toCharArray();
//        for (int i = 0; i != chArray.length; ++i) {
//            char ch1 = chArray[i];
//            char ch2 = text.charAt(i);
//            if (ch1 == ch2 || Character.toLowerCase(ch1) == Character.toLowerCase(ch2)) {
//                // continue
//            } else {
//                return false;
//            }
//        }
//        return true;
//    }

    /**
     * Tests if the string ends with the specified suffix.
     *
     * @param text the string to test.
     * @param suffix the suffix.
     * @return <code>true</code> if the character sequence represented by the argument is a suffix of the character sequence
     *         represented by this object; <code>false</code> otherwise. Note that the result will be <code>true</code> if the
     *         suffix is the empty string or is equal to this <code>String</code> object as determined by the
     *         {@link #equals(Object)} method. If the text or suffix argument is null <code>false</code> is returned.
     */
    public static boolean endsWithIgnoreCase( final String text,
                                              final String suffix ) {
        if (isEmpty(text)) {
            return false;
        }
        if (suffix == null) {
            return false;
        }
        int textLength = text.length();
        int suffixLength = suffix.length();
        if (suffixLength == 0) {
            return true;
        }
        if (suffixLength > textLength) {
            return false;
        }
        int offset = textLength - suffixLength;
        char[] chArray = suffix.toCharArray();
        for (int i = 0; i != chArray.length; ++i) {
            char ch1 = chArray[i];
            char ch2 = text.charAt(offset + i);
            if (ch1 == ch2 || Character.toLowerCase(ch1) == Character.toLowerCase(ch2)) {
                // continue
            } else {
                return false;
            }
        }
        return true;
    }
//
//    /**
//     * Determine if the string passed in has all digits as its contents
//     *
//     * @param str
//     * @return true if digits; false otherwise
//     */
//    public static boolean isDigits( String str ) {
//        for (int i = 0; i < str.length(); i++) {
//            if (!StringUtils.isDigit(str.charAt(i))) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    /*
//     * Converts user string to regular expres '*' and '?' to regEx variables.
//     * copied from eclipse's PatternConstructor
//     */
//    static String asRegEx( String pattern ) {
//        // Replace \ with \\, * with .* and ? with .
//        // Quote remaining characters
//        String result1 = Constants.PATTERN_BACK_SLASH.matcher(pattern).replaceAll("\\\\E\\\\\\\\\\\\Q"); //$NON-NLS-1$
//        String result2 = Constants.PATTERN_STAR.matcher(result1).replaceAll("\\\\E.*\\\\Q"); //$NON-NLS-1$
//        String result3 = Constants.PATTERN_QUESTION.matcher(result2).replaceAll("\\\\E.\\\\Q"); //$NON-NLS-1$
//        return "\\Q" + result3 + "\\E"; //$NON-NLS-1$ //$NON-NLS-2$
//    }
//
//    /**
//     * Creates a regular expression pattern from the pattern string (which is our old 'StringMatcher' format). Copied from
//     * Eclipse's PatternConstructor class.
//     *
//     * @param pattern The search pattern
//     * @param isCaseSensitive Set to <code>true</code> to create a case insensitve pattern
//     * @return The created pattern
//     */
//    public static Pattern createPattern( String pattern,
//                                         boolean isCaseSensitive ) {
//        if (isCaseSensitive) return Pattern.compile(asRegEx(pattern));
//        return Pattern.compile(asRegEx(pattern), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
//    }
//
//    /**
//     * Removes extraneous whitespace from a string. By it's nature, it will be trimmed also.
//     *
//     * @param raw
//     * @return
//     *
//     */
//    public static String collapseWhitespace( String raw ) {
//        StringBuffer rv = new StringBuffer(raw.length());
//
//        StringTokenizer izer = new StringTokenizer(raw, " "); //$NON-NLS-1$
//        while (izer.hasMoreTokens()) {
//            String tok = izer.nextToken();
//            // Added one last check here so we don't append a "space" on the end of the string
//            rv.append(tok);
//            if (izer.hasMoreTokens()) {
//                rv.append(' ');
//            }
//        } // endwhile
//
//        return rv.toString();
//    }
//
//    /**
//     * If input == null OR input.length() < desiredLength, pad to desiredLength with spaces. If input.length() > desiredLength,
//     * chop at desiredLength.
//     *
//     * @param input Input text
//     * @param desiredLength Desired length
//     * @return
//     *
//     */
//    public static String toFixedLength( String input,
//                                        int desiredLength ) {
//        if (input == null) {
//            input = ""; //$NON-NLS-1$
//        }
//
//        if (input.length() == desiredLength) {
//            return input;
//        }
//
//        if (input.length() < desiredLength) {
//            StringBuffer str = new StringBuffer(input);
//            int needSpaces = desiredLength - input.length();
//            for (int i = 0; i < needSpaces; i++) {
//                str.append(' ');
//            }
//            return str.toString();
//        }
//
//        // Else too long - chop
//        return input.substring(0, desiredLength);
//    }

    /**
     * @param c the character being checked
     * @return <code>true</code> if the character is a letter
     */
    public static boolean isLetter( char c ) {
        return isBasicLatinLetter(c) || Character.isLetter(c);
    }
//
//    public static boolean isDigit( char c ) {
//        return isBasicLatinDigit(c) || Character.isDigit(c);
//    }

    /**
     * @param c the character being checked
     * @return <code>true</code> if the character is a letter or digit
     */
    public static boolean isLetterOrDigit( char c ) {
        return isBasicLatinLetter(c) || isBasicLatinDigit(c) || Character.isLetterOrDigit(c);
    }

    /**
     * @param text the text being checked (can be empty)
     * @return <code>true</code> if the text can be converted to a number
     */
    public static boolean isNumber( final String text ) {
        return ( !isBlank( text ) && text.matches( "-?\\d+(\\.\\d+)?" ) ); //$NON-NLS-1$
    }

    //
//    public static boolean isValid( String str ) {
//        return (!(str == null || str.trim().length() == 0));
//    }
//
//    public static String toUpperCase( String str ) {
//        String newStr = convertBasicLatinToUpper(str);
//        if (newStr == null) {
//            return str.toUpperCase();
//        }
//        return newStr;
//    }
//
//    public static String toLowerCase( String str ) {
//        String newStr = convertBasicLatinToLower(str);
//        if (newStr == null) {
//            return str.toLowerCase();
//        }
//        return newStr;
//    }

    /**
     * @param str the string being checked (cannot be <code>null</code>)
     * @return <code>true</code> if the string is double quoted
     */
    public static boolean isDoubleQuoted( String str ) {
        return str.startsWith(SPEECH_MARK) && str.endsWith(SPEECH_MARK) && isTwoDoubleQuotes(str);
    }

    private static boolean isTwoDoubleQuotes( String str ) {
        return "\"\"".equals(str); //$NON-NLS-1$
    }
//
//    /**
//     * Create a valid filename from the given String.
//     *
//     * @param str The String to convert to a valid filename.
//     * @param defaultName The default name to use if only special characters exist.
//     * @return String A valid filename.
//     */
//    public static String createFileName( String str ) {
//
//        /** Replace some special chars */
//        str = str.replaceAll(" \\| ", "_"); //$NON-NLS-1$ //$NON-NLS-2$
//        str = str.replaceAll(">", "_"); //$NON-NLS-1$ //$NON-NLS-2$
//        str = str.replaceAll(": ", "_"); //$NON-NLS-1$ //$NON-NLS-2$
//        str = str.replaceAll(" ", "_"); //$NON-NLS-1$ //$NON-NLS-2$
//        str = str.replaceAll("\\?", "_"); //$NON-NLS-1$ //$NON-NLS-2$
//        str = str.replaceAll("/", "_"); //$NON-NLS-1$ //$NON-NLS-2$
//
//        /** If filename only contains of special chars */
//        if (str.matches("[_]+")) //$NON-NLS-1$
//            str = "file"; //$NON-NLS-1$
//
//        return str;
//    }
//
//    /**
//     * Make the first letter uppercase
//     *
//     * @param str
//     * @return The string with the first letter being changed to uppercase
//     *
//     */
//    public static String firstLetterUppercase( String str ) {
//        if (str == null || str.length() == 0) {
//            return null;
//        }
//        if (str.length() == 1) {
//            return str.toUpperCase();
//        }
//        return str.substring(0, 1).toUpperCase() + str.substring(1);
//    }
//
//    private static String convertBasicLatinToUpper( String str ) {
//        char[] chars = str.toCharArray();
//        for (int i = 0; i < chars.length; i++) {
//            if (isBasicLatinLowerCase(chars[i])) {
//                chars[i] = (char)('A' + (chars[i] - 'a'));
//            } else if (!isBasicLatinChar(chars[i])) {
//                return null;
//            }
//        }
//        return new String(chars);
//    }
//
//    private static String convertBasicLatinToLower( String str ) {
//        char[] chars = str.toCharArray();
//        for (int i = 0; i < chars.length; i++) {
//            if (isBasicLatinUpperCase(chars[i])) {
//                chars[i] = (char)('a' + (chars[i] - 'A'));
//            } else if (!isBasicLatinChar(chars[i])) {
//                return null;
//            }
//        }
//        return new String(chars);
//    }
//
//    private static boolean isBasicLatinUpperCase( char c ) {
//        return c >= 'A' && c <= 'Z';
//    }
//
//    private static boolean isBasicLatinLowerCase( char c ) {
//        return c >= 'a' && c <= 'z';
//    }

    private static boolean isBasicLatinLetter( char c ) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private static boolean isBasicLatinDigit( char c ) {
        return c >= '0' && c <= '9';
    }
//
//    private static boolean isBasicLatinChar( char c ) {
//        return c <= '\u007F';
//    }
//
//    /**
//     * Convert the given value to specified type.
//     *
//     * @param value
//     * @param type
//     * @return
//     */
//    @SuppressWarnings( "unchecked" )
//    public static <T> T valueOf( String value,
//                                 Class type ) {
//
//        if (type == String.class) {
//            return (T)value;
//        } else if (type == Boolean.class || type == Boolean.TYPE) {
//            return (T)Boolean.valueOf(value);
//        } else if (type == Integer.class || type == Integer.TYPE) {
//            return (T)Integer.decode(value);
//        } else if (type == Float.class || type == Float.TYPE) {
//            return (T)Float.valueOf(value);
//        } else if (type == Double.class || type == Double.TYPE) {
//            return (T)Double.valueOf(value);
//        } else if (type == Long.class || type == Long.TYPE) {
//            return (T)Long.decode(value);
//        } else if (type == Short.class || type == Short.TYPE) {
//            return (T)Short.decode(value);
//        } else if (type.isAssignableFrom(List.class)) {
//            return (T)new ArrayList<String>(Arrays.asList(value.split(","))); //$NON-NLS-1$
//        } else if (type.isArray()) {
//            String[] values = value.split(","); //$NON-NLS-1$
//            Object array = Array.newInstance(type.getComponentType(), values.length);
//            for (int i = 0; i < values.length; i++) {
//                Array.set(array, i, valueOf(values[i], type.getComponentType()));
//            }
//            return (T)array;
//        } else if (type == Void.class) {
//            return null;
//        } else if (type.isEnum()) {
//            return (T)Enum.valueOf(type, value);
//        }
//
//        else if (type.isAssignableFrom(Map.class)) {
//            List<String> l = Arrays.asList(value.split(",")); //$NON-NLS-1$
//            Map m = new HashMap<String, String>();
//            for (String key : l) {
//                int index = key.indexOf('=');
//                if (index != -1) {
//                    m.put(key.substring(0, index), key.substring(index + 1));
//                }
//            }
//            return (T)m;
//        }
//
//        throw new IllegalArgumentException("Conversion from String to " + type.getName() + " is not supported"); //$NON-NLS-1$ //$NON-NLS-2$
//    }

    /**
     * @param thisString the first string being compared (may be <code>null</code>)
     * @param thatString the other string being compared (may be <code>null</code>)
     * @return <code>true</code> if the supplied strings are both <code>null</code> or have equal values
     */
    public static boolean equals( final String thisString,
                                  final String thatString ) {
        if (thisString == null) {
            return (thatString == null);
        }

        return thisString.equals(thatString);
    }

    /**
     * @param thisString the first string being compared (may be <code>null</code>)
     * @param thatString the other string being compared (may be <code>null</code>)
     * @return <code>true</code> if the supplied strings are both <code>null</code> or have equal values, (ignoring case)
     */
    public static boolean equalsIgnoreCase( final String thisString,
                                            final String thatString ) {
        if (thisString == null) {
            return (thatString == null);
        }

        return thisString.equalsIgnoreCase(thatString);
    }
//
//    /**
//     * Returns the path representing the concatenation of the specified path prefix and suffix. The resulting path is guaranteed to
//     * have exactly one file separator between the prefix and suffix.
//     *
//     * @param prefix The path prefix
//     * @param suffix The path suffix
//     * @return The concatenated path prefix and suffix
//     *
//     */
//    public static String buildPath(final String prefix,
//                                   final String suffix) {
//        final StringBuffer path = new StringBuffer(prefix);
//        if (!prefix.endsWith(File.separator)) path.append(File.separator);
//        if (suffix.startsWith(File.separator)) path.append(suffix.substring(File.separator.length()));
//        else path.append(suffix);
//        return path.toString();
//    }
//
//    /**
//     * @param originalString
//     * @param maxLength
//     * @param endLength
//     * @param middleString
//     * @return
//     *
//     */
//    public static String condenseToLength(final String originalString,
//                                          final int maxLength,
//                                          final int endLength,
//                                          final String middleString) {
//        if (originalString.length() <= maxLength) return originalString;
//        final int originalLength = originalString.length();
//        final StringBuffer sb = new StringBuffer(maxLength);
//        sb.append(originalString.substring(0, maxLength - endLength - middleString.length()));
//        sb.append(middleString);
//        sb.append(originalString.substring(originalLength - endLength, originalLength));
//
//        return sb.toString();
//    }

    /**
     * @param string1 may be <code>null</code>
     * @param string2 may be <code>null</code>
     * @return <code>true</code> if the supplied strings are different.
     */
    public static boolean areDifferent(final String string1,
                                       final String string2) {
        if (string1 == null) return string2 != null;
        return !string1.equals(string2);
    }
//
//    /**
//     * Returns a new string that represents the last fragment of the original string that begins with an uppercase char. Ex:
//     * "getSuperTypes" would return "Types".
//     *
//     * @param value
//     * @return String
//     */
//    public static String getLastUpperCharToken(final String value) {
//        if (value == null) return null;
//
//        final StringBuffer result = new StringBuffer();
//        for (int i = value.length() - 1; i >= 0; i--) {
//            result.insert(0, value.charAt(i));
//            if (Character.isUpperCase(value.charAt(i))) return result.toString();
//        }
//
//        return result.toString();
//    }
//
//    /**
//     * Returns a new string that represents the last fragment of the original string that begins with an uppercase char. Ex:
//     * "getSuperTypes" would return "Types".
//     *
//     * @param value
//     * @param lastToken - the last token tried... if not null will look backwards from the last token instead of the end of the
//     *        value param
//     * @return String
//     */
//    public static String getLastUpperCharToken(final String value,
//                                               final String lastToken) {
//        if (value == null || lastToken == null) return value;
//
//        final int index = value.lastIndexOf(lastToken);
//        if (index == -1) return null;
//
//        final StringBuffer result = new StringBuffer();
//        for (int i = index - 1; i >= 0; i--) {
//            result.insert(0, value.charAt(i));
//            if (Character.isUpperCase(value.charAt(i))) return result.toString() + lastToken;
//        }
//
//        return result.toString() + lastToken;
//    }
//
//    public static String[] getLines(final String value) throws IOException {
//        final StringReader stringReader = new StringReader(value);
//        final BufferedReader reader = new BufferedReader(stringReader);
//        final ArrayList result = new ArrayList();
//        String line = reader.readLine();
//        while (line != null) {
//            result.add(line);
//            line = reader.readLine();
//        }
//        return (String[])result.toArray(new String[result.size()]);
//    }

    /**
     * @param text the text being checked (may be <code>null</code>)
     * @return <code>true</code> if the specified text is <code>null</code>, contains only spaces, or is empty
     */
    public static boolean isBlank(final String text) {
        return ((text == null) || (text.trim().length() == 0));
    }
//
//    /**
//     * Returns a new string that lowercases the first character in the passed in value String
//     *
//     * @param value
//     * @return String
//     */
//    public static String lowerCaseFirstChar(final String value) {
//        if (value == null) return null;
//
//        // Lower case the first char and try to look-up the SF
//        String firstChar = new Character(value.charAt(0)).toString();
//        firstChar = firstChar.toLowerCase();
//        return (firstChar + value.substring(1));
//    }
//
//    /**
//     * Parses a comma-separated list into an array of strings into an array of strings
//     * Values can contain whitespace, but whitespace at the beginning and end of each value is trimmed.
//     * @return array of Strings
//     * @param csvList a string of comma separated values
//     */
//    public static String[] parseCommaDelimitedString(String csvString) {
//        String[] result = parseList(csvString, COMMA);
//        for (int i = 0; i < result.length; i++) {
//            result[i] = result[i].trim();
//        }
//
//        return result;
//    }
//
//    /**
//     * Parses a delimited string using the specified delimiter.
//     * @param list a string of token separated values
//     * @param delimiter the delimiter character(s).  Each character in the string is a single delimiter.
//     * @return an array of strings
//     */
//    public static String[] parseList(String delimitedString,
//                                     String delimiter) {
//        List<String> result = new ArrayList<String>();
//        StringTokenizer tokenizer = new StringTokenizer(delimitedString, delimiter);
//        while (tokenizer.hasMoreTokens()) {
//            result.add(tokenizer.nextToken());
//        }
//
//        return result.toArray(new String[0]);
//    }
//
//    public static String removeChars(final String value,
//                                     final char[] chars) {
//        final StringBuffer result = new StringBuffer();
//        if (value != null && chars != null && chars.length > 0) {
//            final String removeChars = String.valueOf(chars);
//            for (int i = 0; i < value.length(); i++) {
//                final String character = value.substring(i, i + 1);
//                if (removeChars.indexOf(character) == -1) result.append(character);
//            }
//        } else result.append(value);
//        return result.toString();
//    }
//
//    /**
//     * Replaces multiple sequential "whitespace" characters from the specified string with a single space character, where
//     * whitespace includes \r\t\n and other characters
//     *
//     * @param value the string to work with
//     * @see java.util.regex.Pattern
//     */
//    public static String removeExtraWhitespace(final String value) {
//        return value.replaceAll("\\s\\s+", " "); //$NON-NLS-1$//$NON-NLS-2$
//    }
//
//    /**
//     * Replaces all "whitespace" characters from the specified string with space characters, where whitespace includes \r\t\n and
//     * other characters
//     *
//     * @param value the string to work with
//     * @param stripExtras if true, replace multiple whitespace characters with a single character.
//     * @see java.util.regex.Pattern
//     */
//    public static String replaceWhitespace(final String value,
//                                           final boolean stripExtras) {
//        return replaceWhitespace(value, " ", stripExtras); //$NON-NLS-1$
//    }
//
//    /**
//     * Replaces all "whitespace" characters from the specified string with space characters, where whitespace includes \r\t\n and
//     * other characters
//     *
//     * @param value the string to work with
//     * @param replaceWith the character to replace with
//     * @param stripExtras if true, replace multiple whitespace characters with a single character.
//     * @see java.util.regex.Pattern
//     */
//    public static String replaceWhitespace(final String value,
//                                           final String replaceWith,
//                                           final boolean stripExtras) {
//        String rv = value.replaceAll("\\s+", replaceWith); //$NON-NLS-1$
//
//        if (stripExtras) rv = removeExtraWhitespace(rv);
//
//        return rv;
//    }
//
//    /**
//     * Returns a new string that uppercases the first character in the passed in value String
//     *
//     * @param value
//     * @return String
//     */
//    public static String upperCaseFirstChar(final String value) {
//        if (value == null) return null;
//
//        // Lower case the first char and try to look-up the SF
//        String firstChar = new Character(value.charAt(0)).toString();
//        firstChar = firstChar.toUpperCase();
//        return (firstChar + value.substring(1));
//    }

    /**
     * @param value the value being camel-cased (cannot be <code>null</code>)
     * @return CamelCase version of the given string, ie. converting '_' to capital letters as well
     *                 as capitalising the first letter.
     */
    public static String toCamelCase(final String value) {
        StringBuffer sb = new StringBuffer();
        for (String s : value.split(UNDERSCORE)) {
            sb.append(Character.toUpperCase(s.charAt(0)));
            if (s.length() > 1) {
                sb.append(s.substring(1, s.length()).toLowerCase());
            }
        }

        return sb.toString();
    }

    /**
     * @param value the value being lower camel-cased
     * @return CamelCase version of the given string (see {@link #toCamelCase(String)})
     *                 but also lowers the first letter.
     */
    public static String toLowerCamelCase(final String value) {
        if (value == null)
            return null;

        String newValue = toCamelCase(value);
        char c[] = newValue.toCharArray();
        c[0] = Character.toLowerCase(c[0]);
        return new String(c);
    }

    /**
     * @param value the string whose leading spaces are being removed (can be empty)
     * @return the input with leading spaces removed (can be <code>null</code> if input is <code>null</code>)
     */
    public static String trimLeft( final String value ) {
        if (isEmpty(value)) {
            return value;
        }

        return value.replaceAll("^\\s+", ""); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * @param values array of values
     * @return a single comma separated string of the given array of values
     */
    public static String toCommaSeparatedList(Object[] values) {
        if (values == null)
            return EMPTY_STRING;

        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < values.length; ++i) {
            Object value = values[i];
            buffer.append(value);
            if (i < (values.length - 1)) {
                buffer.append(COMMA).append(SPACE);
            }
        }

        return buffer.toString();
    }

    /**
     * @param srcStream the src input stream
     * @return string representation of source stream.
     *
     * Note: Close the source stream on completion
     *
     * @throws IOException
     */
    public static String inputStreamToString(InputStream srcStream) throws IOException {
        if (srcStream == null)
            return EMPTY_STRING;

        try {
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = srcStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            return result.toString("UTF-8");
        } finally {
            if (srcStream != null)
                srcStream.close();
        }
    }

    /**
     * @param throwable
     * @return The stack trace of the given throwable as a string
     */
    public static String exceptionToString(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * Don't allow construction outside of this class.
     */
    private StringUtils() {
        // nothing to do
    }

}
