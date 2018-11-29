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
package org.komodo.openshift;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.komodo.relational.vdb.Vdb;
import org.komodo.rest.AuthHandlingFilter.OAuthCredentials;
import org.komodo.spi.constants.StringConstants;
import org.komodo.spi.repository.Repository.UnitOfWork;

import io.fabric8.kubernetes.api.model.EnvVar;

public class PublishConfiguration implements StringConstants {

    protected OAuthCredentials oauthCreds;
    protected UnitOfWork uow;
    protected Vdb vdb;
    protected boolean enableOdata = true;
    protected String containerMemorySize = "1024Mi";
    protected List<EnvVar> allEnvironmentVariables = new ArrayList<>();
    protected HashMap<String, String> buildNodeSelector = new HashMap<>();
    private String buildImageStream = "syndesis-s2i:latest";

    // cpu units
    private int cpuUnits = 500; //100m is 0.1 of CPU, at 500m we have 1/2 CPU as default

    public String getBuildImageStream() {
        String stream = System.getenv("BUILD_IMAGE_STREAM");
        if (stream != null) {
            buildImageStream = stream;
        }
        return buildImageStream;
    }

    public void setVDB(Vdb vdb) {
        this.vdb = vdb;
    }

    public void setEnableOData(boolean flag) {
        this.enableOdata = flag;
    }

    public void setContainerMemorySize(String size) {
        this.containerMemorySize = size;
    }

    public void addEnvironmentVariables(Collection<EnvVar> envs) {
        if (envs != null && !envs.isEmpty()) {
            this.allEnvironmentVariables.addAll(envs);
        }
    }

    protected String getUserJavaOptions() {
        StringBuilder sb = new StringBuilder();
        sb.append(" -XX:+UnlockExperimentalVMOptions");
        sb.append(" -XX:+UseCGroupMemoryLimitForHeap");
        sb.append(" -Djava.net.preferIPv4Addresses=true");
        sb.append(" -Djava.net.preferIPv4Stack=true");

        // CPU specific JVM options
        sb.append(" -XX:ParallelGCThreads="+cpuLimit());
        sb.append(" -XX:ConcGCThreads="+cpuLimit());
        sb.append(" -Djava.util.concurrent.ForkJoinPool.common.parallelism="+cpuLimit());
        sb.append(" -Dio.netty.eventLoopThreads="+(2*cpuLimit()));
        return sb.toString();
    }


    protected Map<String, String> getUserEnvironmentVariables() {
        Map<String, String> envs = new TreeMap<>();
        envs.put("AB_JOLOKIA_OFF", "true");
        envs.put("AB_OFF", "true");
        envs.put("GC_MAX_METASPACE_SIZE", "256");
        envs.put("AB_PROMETHEUS_OFF", "true");
        return envs;
    }

    protected List<EnvVar> getUserEnvVars() {
        ArrayList<EnvVar> envs = new ArrayList<>();
        getUserEnvironmentVariables().forEach((k, v) -> envs.add(new EnvVar(k, v, null)));
        return envs;
    }

    protected String cpuUnits() {
        return Integer.toString(cpuUnits)+"m";
    }

    private int cpuLimit() {
        return Math.max(cpuUnits/1000, 1);
    }

    public void setOAuthCredentials(OAuthCredentials creds) {
        this.oauthCreds = creds;
    }

    public void setTransaction(UnitOfWork uow) {
        this.uow = uow;
    }

    public void dispose() {
        if (this.uow == null)
            return;

        this.uow.rollback();
    }

    public HashMap<String, String> getBuildNodeSelector() {
        return buildNodeSelector;
    }

}
