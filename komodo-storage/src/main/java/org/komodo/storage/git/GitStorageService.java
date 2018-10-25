/*
 * JBoss, Home of Professional Open Source.
 * See the COPYRIGHT.txt file distributed with this work for information
 * regarding copyright ownership.  Some portions may be licensed
 * to Red Hat, Inc. under one or more contributor license agreements.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA.
 */
package org.komodo.storage.git;

import java.util.Properties;
import java.util.Set;
import org.komodo.spi.storage.StorageConnector;
import org.komodo.spi.storage.StorageConnector.Descriptor;
import org.komodo.spi.storage.StorageService;

public class GitStorageService implements StorageService {

    public static final String STORAGE_ID = StorageConnector.Types.GIT.id();

    public static final String DESCRIPTION = "Storage of files in a git repository";

    public GitStorageService() {
    }

    @Override
    public String getStorageId() {
        return STORAGE_ID;
    }

    @Override
    public String getDescription() throws Exception {
        return DESCRIPTION;
    }

    @Override
    public Set<Descriptor> getDescriptors() throws Exception {
        return GitStorageConnector.DESCRIPTORS;
    }

    @Override
    public StorageConnector getConnector(Properties parameters) throws Exception {
        return new GitStorageConnector(parameters);
    }
}
