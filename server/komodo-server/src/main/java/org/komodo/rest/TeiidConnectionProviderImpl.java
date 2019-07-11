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
package org.komodo.rest;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.komodo.metadata.TeiidConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.teiid.adminapi.Admin;
import org.teiid.adminapi.AdminException;

@Component
public class TeiidConnectionProviderImpl implements TeiidConnectionProvider {

    @Autowired
    private TeiidServer server;

    private Admin admin;

    protected synchronized void connect() throws AdminException {
    }

    protected synchronized void disconnect() throws AdminException {
    }

	@Override
	public Admin getAdmin() throws AdminException {
	    if (this.admin == null) {
	        this.admin = new TeiidAdminImpl(server.getAdmin(), server);
	    }
		return this.admin;
	}

	@Override
	public Connection getConnection(String vdb, String version) throws SQLException {
		Properties props = new Properties();
		//TODO: when security working the user name needs to be passed in we need to work delegation model for security
		return server.getDriver().connect("jdbc:teiid:"+vdb+"."+version, props);
	}

	@Override
	public void reconnect() throws Exception {
	    disconnect();

	    //
	    // Give disconnect and teiid connection time to clear up
	    //
	    Thread.sleep(500);

	    // Refresh is implied in the getting of the admin object since it will
	    // automatically load and refresh.
	    connect();
	}

	@Override
	public void onStart() {
	}

	@Override
	public void onShutdown() {
	}
}
