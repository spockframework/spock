/*
 * Copyright 2006-2007,  Unitils.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.unitils.core.dbsupport;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Set;

import org.unitils.core.UnitilsException;
import static org.unitils.thirdparty.org.apache.commons.dbutils.DbUtils.closeQuietly;

// Copied from: http://jira.unitils.org/browse/UNI-79
/**
 * Implementation of {@link org.unitils.core.dbsupport.DbSupport} for a H2
 * database
 *
 * @author Mark Thomas
 */
public class H2DbSupport extends DbSupport {

    /**
     * Creates support for H2 databases.
     */
    public H2DbSupport() {
        super("h2");
    }

    /**
     * Returns the names of all tables in the database.
     *
     * @return The names of all tables in the database
     */
    @Override
    public Set<String> getTableNames() {
        return getSQLHandler().getItemsAsStringSet("select TABLE_NAME from "
          + "INFORMATION_SCHEMA.TABLES where TABLE_TYPE = 'TABLE' AND "
          + "TABLE_SCHEMA = '" + getSchemaName() + "'");
    }

    /**
     * Gets the names of all columns of the given table.
     *
     * @param tableName The table, not null
     * @return The names of the columns of the table with the given name
     */
    @Override
    public Set<String> getColumnNames(String tableName) {
        return getSQLHandler().getItemsAsStringSet("select COLUMN_NAME from "
          + "INFORMATION_SCHEMA.COLUMNS where TABLE_NAME = '" + tableName
          + "' AND TABLE_SCHEMA = '" + getSchemaName() + "'");
    }

    /**
     * Gets the names of all primary columns of the given table.
     *
     * @param tableName The table, not null
     * @return The names of the primary key columns of the table with the given
     * name
     */
    @Override
    public Set<String> getIdentityColumnNames(String tableName) {
        return getSQLHandler().getItemsAsStringSet("select COLUMN_NAME from "
          + "INFORMATION_SCHEMA.INDEXES where PRIMARY_KEY = 'TRUE' AND "
          + "TABLE_NAME = '" + tableName + "' AND TABLE_SCHEMA = '"
          + getSchemaName() + "'");
    }

    /**
     * Retrieves the names of all the views in the database schema.
     *
     * @return The names of all views in the database
     */
    @Override
    public Set<String> getViewNames() {
        return getSQLHandler().getItemsAsStringSet("select TABLE_NAME from "
          + "INFORMATION_SCHEMA.VIEWS WHERE TABLE_SCHEMA = '"
          + getSchemaName() + "'");
    }

    /**
     * Retrieves the names of all the sequences in the database schema.
     *
     * @return The names of all sequences in the database
     */
    @Override
    public Set<String> getSequenceNames() {
        return getSQLHandler().getItemsAsStringSet("select SEQUENCE_NAME from "
          + "INFORMATION_SCHEMA.SEQUENCES where SEQUENCE_SCHEMA = '"
          + getSchemaName() + "'");
    }

    /**
     * Retrieves the names of all the triggers in the database schema.
     *
     * @return The names of all triggers in the database
     */
    @Override
    public Set<String> getTriggerNames() {
        return getSQLHandler().getItemsAsStringSet("select TRIGGER_NAME from "
          + "INFORMATION_SCHEMA.TRIGGERS where TRIGGER_SCHEMA = '"
          + getSchemaName() + "'");
    }

    /**
     * Returns the value of the sequence with the given name.
     * <p/>
     * Note: this can have the side-effect of increasing the sequence value.
     *
     * @param sequenceName The sequence, not null
     * @return The value of the sequence with the given name
     */
    @Override
    public long getSequenceValue(String sequenceName) {
        return getSQLHandler().getItemAsLong("select CURRENT_VALUE from "
          + "INFORMATION_SCHEMA.SEQUENCES where SEQUENCE_SCHEMA = '"
          + getSchemaName() + "' and SEQUENCE_NAME = '" + sequenceName + "'");
    }

    /**
     * Sets the next value of the sequence with the given sequence name to the
     * given sequence value.
     *
     * @param sequenceName     The sequence, not null
     * @param newSequenceValue The value to set
     */
    @Override
    public void incrementSequenceToValue(String sequenceName,
      long newSequenceValue) {
        getSQLHandler().executeUpdate("alter sequence "
          + qualified(sequenceName) + " restart with " + newSequenceValue);
    }

    /**
     * Increments the identity value for the specified identity column on the
     * specified table to the given value.
     *
     * @param tableName          The table with the identity column, not null
     * @param identityColumnName The column, not null
     * @param identityValue      The new value
     */
    @Override
    public void incrementIdentityColumnToValue(String tableName,
      String identityColumnName, long identityValue) {
        getSQLHandler().executeUpdate("alter table " + qualified(tableName)
          + " alter column " + quoted(identityColumnName) + " RESTART WITH "
          + identityValue);
    }

    /**
     * Disables all referential constraints (e.g. foreign keys) on all tables
     * in the schema
     */
    @Override
    public void disableReferentialConstraints() {
        getSQLHandler().executeUpdate(
          "SET REFERENTIAL_INTEGRITY FALSE");
    }


    /**
     * Disables all value constraints (e.g. not null) on all tables in the schema
     */
    @Override
    public void disableValueConstraints() {
        disableCheckAndUniqueConstraints();
        disableNotNullConstraints();
    }

    /**
     * Disables all check and unique constraints on all tables in the schema
     */
    protected void disableCheckAndUniqueConstraints() {
        Connection connection = null;
        Statement queryStatement = null;
        Statement alterStatement = null;
        ResultSet resultSet = null;
        try {
            connection = getSQLHandler().getDataSource().getConnection();
            queryStatement = connection.createStatement();
            alterStatement = connection.createStatement();

            resultSet = queryStatement.executeQuery("select TABLE_NAME, "
              + "CONSTRAINT_NAME from INFORMATION_SCHEMA.CONSTRAINTS where "
              + "CONSTRAINT_TYPE IN ('CHECK', 'UNIQUE') AND CONSTRAINT_SCHEMA "
              + "= '" + getSchemaName() + "'");
            while (resultSet.next()) {
                String tableName = resultSet.getString("TABLE_NAME");
                String constraintName = resultSet.getString("CONSTRAINT_NAME");
                alterStatement.executeUpdate("alter table "
                  + qualified(tableName) + " drop constraint "
                  + quoted(constraintName));
            }
        } catch (Exception e) {
            throw new UnitilsException("Error while disabling check and unique "
              + "constraints on schema " + getSchemaName(), e);
        } finally {
            closeQuietly(queryStatement);
            closeQuietly(connection, alterStatement, resultSet);
        }
    }

    /**
     * Disables all not null constraints on all tables in the schema
     */
    protected void disableNotNullConstraints() {
        Connection connection = null;
        Statement queryStatement = null;
        Statement alterStatement = null;
        ResultSet resultSet = null;
        try {
            connection = getSQLHandler().getDataSource().getConnection();
            queryStatement = connection.createStatement();
            alterStatement = connection.createStatement();

            // Do not remove PK constraints
            resultSet = queryStatement.executeQuery("select col.TABLE_NAME, "
              + "col.COLUMN_NAME from INFORMATION_SCHEMA.COLUMNS col where "
              + "col.IS_NULLABLE = 'NO' and col.TABLE_SCHEMA = '"
              + getSchemaName() + "' " + "AND NOT EXISTS (select COLUMN_NAME "
              + "from INFORMATION_SCHEMA.INDEXES pk where pk.TABLE_NAME = "
              + "col.TABLE_NAME and pk.COLUMN_NAME = col.COLUMN_NAME and "
              + "pk.TABLE_SCHEMA = '" + getSchemaName()
              + "' AND pk.PRIMARY_KEY = TRUE)");
            while (resultSet.next()) {
                String tableName = resultSet.getString("TABLE_NAME");
                String columnName = resultSet.getString("COLUMN_NAME");
                alterStatement.executeUpdate("alter table "
                  + qualified(tableName) + " alter column " + quoted(columnName)
                  + " set null");
            }
        } catch (Exception e) {
            throw new UnitilsException("Error while disabling not null "
              + "constraints on schema " + getSchemaName(), e);
        } finally {
            closeQuietly(queryStatement);
            closeQuietly(connection, alterStatement, resultSet);
        }
    }

    /**
     * Sequences are supported.
     *
     * @return True
     */
    @Override
    public boolean supportsSequences() {
        return true;
    }

    /**
     * Triggers are supported.
     *
     * @return True
     */
    @Override
    public boolean supportsTriggers() {
        return true;
    }

    /**
     * Identity columns are supported.
     *
     * @return True
     */
    @Override
    public boolean supportsIdentityColumns() {
        return true;
    }

    /**
     * Cascade are supported.
     *
     * @return True
     */
    @Override
    public boolean supportsCascade() {
        return true;
    }
}
