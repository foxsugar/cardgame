package com.code.server.db.model;

import com.sun.istack.internal.NotNull;

import javax.persistence.*;

/**
 * Created by win7 on 2017/3/8.
 */

@Entity
@Table(name = "server_info")
public class ServerInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String name;

    @NotNull
    private String address;

    @NotNull
    private String port;

    private int status;

    private int open;

    private int serverType;


}
