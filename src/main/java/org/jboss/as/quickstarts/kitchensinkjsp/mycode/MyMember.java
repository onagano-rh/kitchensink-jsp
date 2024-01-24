package org.jboss.as.quickstarts.kitchensinkjsp.mycode;

public class MyMember {

    public MyMember(String name, String email, String phoneNumbe) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumbe;
    }

    private String name;
    public String getName() {
        return name;
    }

    private String email;
    public String getEmail() {
        return email;
    }

    private String phoneNumber;
    public String getPhoneNumber() {
        return phoneNumber;
    }

}
