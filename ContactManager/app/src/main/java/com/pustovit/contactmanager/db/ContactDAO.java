package com.pustovit.contactmanager.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.pustovit.contactmanager.db.entity.Contact;

import java.util.List;

import io.reactivex.Flowable;

/**
 * Created by Pustovit Vladimir on 12.01.2020.
 * vovapust1989@gmail.com
 */

@Dao
public interface ContactDAO {

    @Insert
    long addContact(Contact contact);

    @Update
    void updateContact(Contact contact);

    @Delete
    void deleteContact(Contact contact);

//    @Query("SELECT * FROM contacts;")
//    List<Contact> getContacts();


    @Query("SELECT * FROM contacts;")
    Flowable<List<Contact>> getContacts();


    @Query("SELECT * FROM contacts WHERE contact_id == :contactId;")
    Contact getContact(long contactId);
}
