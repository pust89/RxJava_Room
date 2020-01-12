package com.pustovit.contactmanager.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Created by Pustovit Vladimir on 12.01.2020.
 * vovapust1989@gmail.com
 */
@Entity(tableName = "contacts")
public class Contact {
    @ColumnInfo(name = "contact_id")
    @PrimaryKey(autoGenerate = true)
    private long _id;

    @ColumnInfo(name = "contact_name")
    private String name;

    @ColumnInfo(name = "contact_email")
    private String email;

    @Ignore//Говорим Room не использовать этот консртуктор
    public Contact() {
    }

    public Contact(long _id, String name, String email) {
        this._id = _id;
        this.name = name;
        this.email = email;
    }

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
