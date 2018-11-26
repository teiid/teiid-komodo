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
package org.komodo.storage.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import org.komodo.spi.repository.Exportable;
import org.komodo.spi.repository.Repository.UnitOfWork;
import org.komodo.spi.storage.StorageConnector;
import org.komodo.spi.storage.StorageConnectorId;
import org.komodo.spi.storage.StorageNode;
import org.komodo.spi.storage.StorageTree;
import org.komodo.utils.ArgCheck;
import org.komodo.utils.FileUtils;

public class FileStorageConnector implements StorageConnector {

    static final Set<Descriptor> DESCRIPTORS = new HashSet<>();

    static {
        DESCRIPTORS.add(
                   new Descriptor(
                                 StorageConnector.FILES_HOME_PATH_PROPERTY,
                                 false,
                                 "The directory (on the server) where the files are to be hosted. " +
                                 "If not specified then the jboss or native java temporary directory is assumed."));
        DESCRIPTORS.add(
                   new Descriptor(
                                 StorageConnector.FILE_PATH_PROPERTY,
                                 true,
                                 "The relative (to the directory specified by \"files-home-path-property\") path of the file. " +
                                 "It is enough to specify only the name of the file."));
    }

    private final StorageConnectorId id;

    private final Properties parameters;

    public FileStorageConnector(Properties parameters) {
        ArgCheck.isNotNull(parameters);

        this.parameters = parameters;

        this.id = new StorageConnectorId() {

            @Override
            public String type() {
                return FileStorageService.STORAGE_ID;
            }

            @Override
            public String location() {
                return getPath();
            }
        };
    }

    @Override
    public StorageConnectorId getId() {
        return id;
    }

    @Override
    public Set<Descriptor> getDescriptors() {
        return DESCRIPTORS;
    }

    /**
     * @return repository path
     */
    public String getPath() {
        String property = parameters.getProperty(StorageConnector.FILES_HOME_PATH_PROPERTY);
        if (property != null)
            return property;

        return FileUtils.tempDirectory();
    }

    /**
     * @param parameters
     * @return the relative file path from the given parameters
     */
    public String getFilePath(Properties parameters) {
        return parameters.getProperty(FILE_PATH_PROPERTY);
    }

    protected void setDownloadable(String path) {
        parameters.setProperty(DOWNLOADABLE_PATH_PROPERTY, path);
    }

    @Override
    public InputStream read(Properties parameters) throws Exception {
        String fileRef = getFilePath(parameters);
        ArgCheck.isNotNull(fileRef, "RelativeFileRef");

        File destFile = new File(getPath(), fileRef);
        if (destFile.exists())
            return new FileInputStream(destFile);

        throw new FileNotFoundException();
    }

    @Override
    public void write(Exportable artifact, UnitOfWork transaction, Properties parameters) throws Exception {
        ArgCheck.isNotNull(parameters);
        String filePath = getFilePath(parameters);
        ArgCheck.isNotEmpty(filePath);

        ArgCheck.isNotNull(artifact, "artifact");
        ArgCheck.isNotNull(transaction, "transaction");

        File destFile = new File(getPath(), filePath);

        //
        // Write the file contents
        //
        byte[] contents = artifact.export(transaction, parameters);
        FileUtils.write(contents, destFile);

        setDownloadable(destFile.getAbsolutePath());
    }

    @Override
    public boolean refresh() throws Exception {
        return true; // Not applicable to static filesystem
    }

    private void walk(StorageNode<String> node, File parent) {
        File[] list = parent.listFiles();

        if (list == null)
            return;

        for (File file : list) {
            StorageNode<String> child = node.addChild(file.getName());
            if (file.isDirectory())
                walk(child, file);
        }
    }

    @Override
    public StorageTree<String> browse() throws Exception {
        File dir = new File(getPath());
        StorageTree<String> storageTree = new StorageTree<String>();

        if (! dir.isDirectory())
            return storageTree;

        StorageNode<String> node = storageTree.addChild(dir.getName());
        walk(node, dir);

        return storageTree;
    }

    @Override
    public void dispose() {
        // Nothing to do
    }

}
