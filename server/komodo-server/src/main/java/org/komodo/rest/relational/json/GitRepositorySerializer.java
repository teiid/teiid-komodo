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
package org.komodo.rest.relational.json;

import java.io.IOException;
import org.komodo.rest.relational.response.RestGitRepository;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class GitRepositorySerializer extends AbstractEntitySerializer<RestGitRepository> {

    @Override
    protected RestGitRepository createEntity() {
        return new RestGitRepository();
    }

    @Override
    protected boolean isComplete(RestGitRepository entity) {
        return entity.getName() != null && entity.getUrl() != null &&
                            entity.getUser() != null && entity.getPassword() != null;
    }

    @Override
    protected String readExtension(String name, RestGitRepository entity, JsonReader in) {
        // Nothing to do
        return null;
    }

    @Override
    protected void writeExtensions(JsonWriter out, RestGitRepository entity) throws IOException {
        // Nothing to do
    }
}
