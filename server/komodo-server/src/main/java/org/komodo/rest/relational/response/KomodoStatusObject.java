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
package org.komodo.rest.relational.response;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import org.komodo.rest.KRestEntity;
import org.komodo.utils.ArgCheck;


/**
 * Object to be serialised by GSON that provides key/message pairs
 */
public class KomodoStatusObject implements KRestEntity {

    /**
     * Label for the title
     */
    public static final String TITLE_LABEL = "Title"; //$NON-NLS-1$

    /**
     * Label for the information
     */
    public static final String INFO_LABEL = "Information"; //$NON-NLS-1$

    private String title;

    private Map<String, String> attributes = new LinkedHashMap<>();

    /**
     * Default constructor for deserialization
     */
    public KomodoStatusObject() {
        // do nothing
    }

    /**
     * @param title the subject of this status object
     *
     */
    public KomodoStatusObject(String title) {
        ArgCheck.isNotNull(title);
        this.title = title;
    }

    @Override
    public boolean supports(MediaType mediaType) {
        return MediaType.APPLICATION_JSON_TYPE.equals(mediaType);
    }

    @Override
    public Object getXml() {
        throw new UnsupportedOperationException();
    }

    /**
     * @return the attributes
     */
    public Map<String, String> getAttributes() {
        return this.attributes;
    }

    /**
     * Add a message pair with a prefixed subject
     *
     * @param subject the subject of the message
     * @param message the message
     */
    public void addAttribute(String subject, String message) {
        attributes.put(subject, message);
    }

    /**
     * @param attributes the attributes to set
     */
    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * @param title the title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.attributes == null) ? 0 : this.attributes.hashCode());
        result = prime * result + ((this.title == null) ? 0 : this.title.hashCode());
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
        KomodoStatusObject other = (KomodoStatusObject)obj;
        if (this.attributes == null) {
            if (other.attributes != null)
                return false;
        } else
            if (!this.attributes.equals(other.attributes))
                return false;
        if (this.title == null) {
            if (other.title != null)
                return false;
        } else
            if (!this.title.equals(other.title))
                return false;
        return true;
    }

    @SuppressWarnings( "nls" )
    @Override
    public String toString() {
        return "KomodoStatusObject [title=" + this.title + ", attributes=" + this.attributes + "]";
    }
}
