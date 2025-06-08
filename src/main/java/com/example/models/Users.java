package com.example.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

}
