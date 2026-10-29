package model.entity;

import java.io.Serializable;
import java.util.Objects;

public class Employee  implements Entity {

    private Integer id;

    private String identifiant;
    private  String password;
    public Employee() {
    }
    public Employee(Integer id,String log, String pwd) {
        this.id=id;
        this.identifiant = log;
        this.password = pwd;
    }
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLog() {
        return identifiant;
    }

    public void setLog(String log) {
        this.identifiant = log;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "id=" + id +
                ", log='" + identifiant + '\'' +
                ", pwd='" + password + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Employee employee = (Employee) o;
        return Objects.equals(identifiant, employee.identifiant) && Objects.equals(password, employee.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifiant, password);
    }


}
