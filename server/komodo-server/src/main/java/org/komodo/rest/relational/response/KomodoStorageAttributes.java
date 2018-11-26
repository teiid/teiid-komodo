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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.komodo.rest.relational.AbstractKomodoContentAttribute;
import org.komodo.spi.repository.DocumentType;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;


/**
 * Object to be serialised by GSON that encapsulates a storage type and parameters object
 */
@JsonSerialize
@JsonInclude(value=Include.NON_NULL)
public class KomodoStorageAttributes extends AbstractKomodoContentAttribute {

    /**
     * Label for the storage type
     */
    public static final String STORAGE_TYPE_LABEL = "storageType"; //$NON-NLS-1$

    /**
     * Label for the data path
     */
    public static final String ARTIFACT_PATH_LABEL = "dataPath"; //$NON-NLS-1$

    /**
     * Label for the parameters
     */
    public static final String PARAMETERS_LABEL = "parameters"; //$NON-NLS-1$

    /**
     * Label for the documentType
     */
    public static final String DOCUMENT_TYPE_LABEL = "documentType"; //$NON-NLS-1$

    @JsonProperty(STORAGE_TYPE_LABEL)
    private String storageType;

    @JsonProperty(ARTIFACT_PATH_LABEL)
    private String artifactPath;

    @JsonProperty(PARAMETERS_LABEL)
    private Map<String, String> parameters;

    @JsonProperty(DOCUMENT_TYPE_LABEL)
    private String documentType;

    /**
     * Default constructor for deserialization
     */
    public KomodoStorageAttributes() {
        // do nothing
    }

    /**
     * @return the type
     */
    public String getStorageType() {
        return this.storageType;
    }

    /**
     * @param storageType the type to set
     */
    public void setStorageType(String storageType) {
        this.storageType = storageType;
    }

    /**
     * @return the artifact path
     */
    public String getArtifactPath() {
        return this.artifactPath;
    }

    /**
     * @param artifactPath the artifact path to set
     */
    public void setArtifactPath(String artifactPath) {
        this.artifactPath = artifactPath;
    }

    /**
     * @return the document type
     */
    public String getDocumentType() {
        return documentType;
    }

    /**
     * @param documentType the document type
     */
    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    /**
     * @param documentType the document type
     */
    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType.toString();
    }

    /**
     * @return the parameters
     */
    public Map<String, String> getParameters() {
        if (parameters == null)
            return Collections.emptyMap();

        return Collections.unmodifiableMap(this.parameters);
    }

    /**
     * @return {@link Properties} instance of the parameters
     */
    public Properties convertParameters() {
        Properties props = new Properties();
        if (this.parameters == null)
            return props;

        for (Map.Entry<String, String> entry : this.parameters.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key == null || value == null)
                continue;

            props.setProperty(key, value);
        }

        return props;
    }

    /**
     * Add a parameter with value
     * @param name the name
     * @param value the value
     */
    public void setParameter(String name, String value) {
        if (this.parameters == null)
            this.parameters = new HashMap<>();

        this.parameters.put(name, value);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((artifactPath == null) ? 0 : artifactPath.hashCode());
        result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
        result = prime * result + ((storageType == null) ? 0 : storageType.hashCode());
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
        KomodoStorageAttributes other = (KomodoStorageAttributes)obj;
        if (artifactPath == null) {
            if (other.artifactPath != null)
                return false;
        } else if (!artifactPath.equals(other.artifactPath))
            return false;
        if (parameters == null) {
            if (other.parameters != null)
                return false;
        } else if (!parameters.equals(other.parameters))
            return false;
        if (storageType == null) {
            if (other.storageType != null)
                return false;
        } else if (!storageType.equals(other.storageType))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "KomodoStorageAttributes [storageType=" + storageType + ", artifactPath=" + artifactPath + ", parameters="
               + parameters + "]";
    }
}
