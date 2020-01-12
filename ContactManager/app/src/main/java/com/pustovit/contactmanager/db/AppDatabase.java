package com.pustovit.contactmanager.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.pustovit.contactmanager.db.entity.Contact;

/**
 * Created by Pustovit Vladimir on 12.01.2020.
 * vovapust1989@gmail.com
 */
@Database(entities = {Contact.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public static final String DATABASE_NAME = "contactsDatabase";

    public abstract ContactDAO getContactDAO();
}
