package com.code.server.db.model;

import com.sun.istack.internal.NotNull;

import javax.persistence.*;

/**
 * Created by win7 on 2017/3/8.
 */
@Entity
@Table(name = "users")
public class User {
    // ==============
    // PRIVATE FIELDS
    // ==============
    // An autogenerated id (unique for each user in the db)
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NotNull
    private long userId222;

    @NotNull
    private String account;

    @NotNull
    private String password;

    @NotNull
    private String username;

    @NotNull
    private String ipConfig;

    @NotNull
    private int money;//虚拟货币

    @NotNull
    private int cash;//货币

    @NotNull
    private String image;//头像

    @NotNull
    private String vip;//vip

    @NotNull
    private String fatherId;//代理id

    @NotNull
    private String uuid;//uuid

    @NotNull
    private String openId;//openId

    @NotNull
    private String sex;//

    @NotNull
    private String aliId;

    @NotNull
    private String email;

    @NotNull
    private String column1;

    @NotNull
    private String column2;

    @NotNull
    private String column3;

    @NotNull
    private String column4;

    @NotNull
    private String column5;

    // ==============
    // PUBLIC METHODS
    // ==============
    public User() { }
    public User(long id) {
        this.id = id;
    }
    // Getter and setter methods
    // ...
} // class User