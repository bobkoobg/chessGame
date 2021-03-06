package dataSource;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PrepareStatement {

    //###Method for counting ?-marks by counting fields
    public String countValuesforSQLString(List<Field> fields) {

        String numberOfValues = "";
        int size = fields.size();
        for (int i = 0; i < size; i++) {
            numberOfValues += ((i != size - 1) ? "?," : "?");
        }
        return numberOfValues;
    }

    //###Generating SQLString for Insert
    public <T> PreparedStatement generateSQLString(String actionType, String tableName,
            List<Field> fields, ArrayList<T> listOfRequests, Connection connect) {
        String blank = "";
        StringBuilder builder = new StringBuilder(blank);
        PreparedStatement statement = null;

        builder.append(actionType)
                .append(" into ")
                .append(tableName)
                .append(" values (")
                .append(countValuesforSQLString(fields))
                .append(")");

        for (int i = 0; i < listOfRequests.size(); i++) {
            try {
                T currentRequest = listOfRequests.get(i);
                statement = connect.prepareStatement(builder.toString());
                statement = traversingThoughFields(statement, fields, currentRequest);

            } catch (SQLException | IllegalArgumentException ex) {
                Logger.getLogger(PrepareStatement.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return statement;
    }

    //###Generating SQLString for Update
    public <T> PreparedStatement generateSQLString(String actionType, String tableName,
            ArrayList<T> listOfRequests, List<Field> fields, List<String> listOfFieldNames,
            Connection connect) {
        String blank = "";
        StringBuilder builder = new StringBuilder(blank);
        PreparedStatement statement = null;

        Collections.rotate(listOfFieldNames, -1);
        Collections.rotate(fields, -1);
        builder.append(actionType)
                .append(" ")
                .append(tableName)
                .append(" set ");
        int size = listOfFieldNames.size();
        for (int i = 0; i < size; i++) {
            if (i != size - 1) {
                if (size > 2) {
                    if (i == size - 2) {
                        builder.append(listOfFieldNames.get(i))
                                .append(" = ? ");
                    } else {
                        builder.append(listOfFieldNames.get(i))
                                .append(" = ?, ");
                    }
                } else {
                    builder.append(listOfFieldNames.get(i))
                            .append(" = ? ");
                }
            } else {
                builder.append("where ")
                        .append(listOfFieldNames.get(i))
                        .append(" = ?");
            }
        }

        for (int i = 0; i < listOfRequests.size(); i++) {
            try {
                T currentRequest = listOfRequests.get(i);
                statement = connect.prepareStatement(builder.toString());
                statement = traversingThoughFields(statement, fields, currentRequest);
            } catch (SQLException | IllegalArgumentException ex) {
                Logger.getLogger(PrepareStatement.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return statement;
    }

    //###Generating SQLString for Delete
    public <T> PreparedStatement generatesQLString(String actionType, String tableName,
            int toBeDeleted, List<String> listOfFieldNames, Connection connect) {
        String blank = "";
        StringBuilder builder = new StringBuilder(blank);
        PreparedStatement statement = null;

        builder.append(actionType)
                .append(" from ")
                .append(tableName)
                .append(" where ")
                .append(listOfFieldNames.get(0)).append(" = ")
                .append(toBeDeleted);
        try {
            statement = connect.prepareStatement(builder.toString());
        } catch (SQLException ex) {
            Logger.getLogger(PrepareStatement.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return statement;

    }

    //###Generating SQLString for Select * (all)
    public <T> PreparedStatement generateSQLString(String actionType, String tableName,
            Connection connect) {

        String blank = "";
        StringBuilder builder = new StringBuilder(blank);
        PreparedStatement statement = null;

        builder.append(actionType)
                .append(" from ")
                .append(tableName);
        try {
            statement = connect.prepareStatement(builder.toString());
        } catch (SQLException ex) {
            Logger.getLogger(PrepareStatement.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return statement;
    }

    //###Unlocking fields in classess, checking their type and saving in statement
    private <T> PreparedStatement traversingThoughFields(PreparedStatement statement,
            List<Field> fields, T currentRequest) {
        try {
            for (int j = 0; j < fields.size(); j++) {
                System.out.println("Field #" + j + ": " + fields.get(j).getType());
                Integer sqlPossitionParameter = j + 1;
                fields.get(j).setAccessible(true);
                if (fields.get(j).getType() == Integer.class) {
                    statement.setInt(sqlPossitionParameter,
                            (Integer) fields.get(j).get(currentRequest));
                } else if (fields.get(j).getType() == String.class) {
                    statement.setString(sqlPossitionParameter,
                            (String) fields.get(j).get(currentRequest));
                } else if (fields.get(j).getType() == java.util.Date.class) {
                    Date actualDate = (Date) fields.get(j).get(currentRequest);
                    java.sql.Date sqlDate = new java.sql.Date(actualDate.getTime());
                    statement.setDate(sqlPossitionParameter, sqlDate);
                } else if (fields.get(j).getType() == Boolean.class) {
                    System.out.println("Tyk sum na index: " + sqlPossitionParameter);
                    System.out.println("Az sum: " + (fields.get(j).get(currentRequest)));
                    statement.setString(sqlPossitionParameter,
                            (((Boolean) fields.get(j).get(currentRequest)) ? "Y" : "N"));
                }
            }
        } catch (SQLException | IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(PrepareStatement.class.getName()).log(Level.SEVERE, null, ex);
        }
        return statement;
    }
}
