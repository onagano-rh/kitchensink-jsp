package org.jboss.as.quickstarts.kitchensinkjsp.mycode;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;

@RequestScoped
public class MyController {

    @Inject
    private Logger log;

    @Resource(lookup = "java:jboss/datasources/KitchensinkJSPQuickstartDS")
    DataSource dataSource;

    @Produces
    @Named
    public List<MyMember> getMyList() {
        log.info(String.format("Got %s object", dataSource));

        List<MyMember> members = new ArrayList<MyMember>();

        try {
            for (Map<String, String> m : accessDatabase()) {

                MyMember mem = new MyMember(
                    m.get("name"),
                    m.get("email"),
                    m.get("phone_number")
                );
                members.add(mem);

            }
        }
        catch (Exception e) {
            log.log(Level.WARNING, "something wrong", e);
        }

        return members;
    }

    private List<Map<String, String>> accessDatabase() throws Exception {
        List<Map<String, String>> list = new ArrayList<>();

        try (Connection con = dataSource.getConnection()) {
            Statement s = con.createStatement();
            ResultSet rs = s.executeQuery("SELECT name, email, phone_number FROM memberjsp");
            while (rs.next()) {
                Map<String, String> row = new HashMap<String, String>();
                row.put("name", rs.getString(1));
                row.put("email", rs.getString(2));
                row.put("phone_number", rs.getString(3));
                list.add(row);
            }
        }

        return list;
    }

}
