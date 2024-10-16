/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
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

package org.keycloak.quarkus.runtime.configuration.test;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.keycloak.common.Profile;
import org.keycloak.common.profile.PropertiesProfileConfigResolver;
import org.keycloak.config.DatabaseOptions;
import org.keycloak.quarkus.runtime.configuration.IgnoredArtifacts;
import org.keycloak.quarkus.runtime.configuration.MicroProfileConfigProvider;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.keycloak.quarkus.runtime.configuration.IgnoredArtifacts.JDBC_H2;
import static org.keycloak.quarkus.runtime.configuration.IgnoredArtifacts.JDBC_MARIADB;
import static org.keycloak.quarkus.runtime.configuration.IgnoredArtifacts.JDBC_MSSQL;
import static org.keycloak.quarkus.runtime.configuration.IgnoredArtifacts.JDBC_MYSQL;
import static org.keycloak.quarkus.runtime.configuration.IgnoredArtifacts.JDBC_ORACLE;
import static org.keycloak.quarkus.runtime.configuration.IgnoredArtifacts.JDBC_POSTGRES;

public class IgnoredArtifactsTest {

    @Test
    public void fipsDisabled() {
        var profile = Profile.defaults();
        assertThat(profile.isFeatureEnabled(Profile.Feature.FIPS), is(false));

        var ignoredArtifacts = IgnoredArtifacts.getDefaultIgnoredArtifacts();
        assertThat(ignoredArtifacts.containsAll(IgnoredArtifacts.FIPS_DISABLED), is(true));
    }

    @Test
    public void fipsEnabled() {
        Properties properties = new Properties();
        properties.setProperty("keycloak.profile.feature.fips", "enabled");
        var profile = Profile.configure(new PropertiesProfileConfigResolver(properties));

        assertThat(profile.isFeatureEnabled(Profile.Feature.FIPS), is(true));

        var ignoredArtifacts = IgnoredArtifacts.getDefaultIgnoredArtifacts();
        assertThat(ignoredArtifacts.containsAll(IgnoredArtifacts.FIPS_ENABLED), is(true));
    }

    @Test
    public void jdbcH2() {
        assertJdbc("h2", JDBC_H2);
    }

    @Test
    public void jdbcMssql() {
        assertJdbc("mssql", JDBC_MSSQL);
    }

    @Test
    public void jdbcMariadb() {
        assertJdbc("mariadb", JDBC_MARIADB);
    }

    @Test
    public void jdbcMysql() {
        assertJdbc("mysql", JDBC_MYSQL);
    }

    @Test
    public void jdbcOracle() {
        assertJdbc("oracle", JDBC_ORACLE);
    }

    @Test
    public void jdbcPostgres() {
        assertJdbc("postgres", JDBC_POSTGRES);
    }

    private void assertJdbc(String vendor, Set<String> notIgnored) {
        System.setProperty(MicroProfileConfigProvider.NS_KEYCLOAK_PREFIX + DatabaseOptions.DB.getKey(), vendor);
        try {
            final var resultArtifacts = IgnoredArtifacts.getDefaultIgnoredArtifacts();
            assertThat(String.format("Ignored artifacts does not comply with the specified artifacts for '%s' JDBC driver", vendor),
                    resultArtifacts,
                    not(CoreMatchers.hasItems(notIgnored.toArray(new String[0]))));

            final var includedArtifacts = new HashSet<>(IgnoredArtifacts.JDBC_DRIVERS);
            includedArtifacts.removeAll(notIgnored);
            assertThat("Ignored artifacts does not contain items for the other JDBC drivers",
                    resultArtifacts,
                    CoreMatchers.hasItems(includedArtifacts.toArray(new String[0])));
        } finally {
            System.setProperty(MicroProfileConfigProvider.NS_KEYCLOAK_PREFIX + DatabaseOptions.DB.getKey(), "");
        }
    }
}
