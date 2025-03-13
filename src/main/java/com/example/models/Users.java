package com.example.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

@Entity
@Table(name = "users")
public class Users {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @NotEmpty(message = "Поле не должно быть пустым")
    @Size(min = 5,max = 30,message = "Логин должен быть в пределах от 5 до 30 символов")
    @Pattern(regexp = "^[A-Za-z]+$",message = "Логин должен содержать только англиские буквы")
    @Column(name = "login")
    private String login;

    @NotEmpty(message = "Поле не должно быть пустым")
    @Size(min = 6,max = 60,message = "Пароль должен быть в пределах от 6 до 60 символов")
    @Column(name = "password")
    private String password;
    @Transient
    private String confirmPassword;

    @OneToMany(mappedBy = "user", cascade = CascadeType.PERSIST)
    private List<Locations> locations;

    @OneToMany(mappedBy = "user", cascade = CascadeType.PERSIST)
    private List<Sessions> sessions;


    public Users() {
    }

    public Users(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public List<Locations> getLocations() {
        return locations;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public void setLocations(List<Locations> locations) {
        this.locations = locations;
    }

    public List<Sessions> getSessions() {
        return sessions;
    }

    public void setSessions(List<Sessions> sessions) {
        this.sessions = sessions;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


}
