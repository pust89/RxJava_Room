package com.pustovit.contactmanager;

import android.content.DialogInterface;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.pustovit.contactmanager.adapter.ContactsAdapter;
import com.pustovit.contactmanager.db.AppDatabase;
import com.pustovit.contactmanager.db.entity.Contact;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {


    private ContactsAdapter contactsAdapter;
    private List<Contact> contactArrayList;
    private RecyclerView recyclerView;
    private AppDatabase appDatabase;
    private CompositeDisposable compositeDisposable;
    private long rowIdOfTheItemInserted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(" Contacts Manager");

        recyclerView = findViewById(R.id.recycler_view_contacts);

        appDatabase = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class,
                AppDatabase.DATABASE_NAME)
                //.allowMainThreadQueries()//Not recommend used this method, we used it just for to avoid bugs
                //             .enableMultiInstanceInvalidation() //f your app runs in multiple processes, include enableMultiInstanceInvalidation() in your database builder invocation.
                .build();


        //  contactArrayList.addAll(appDatabase.getContactDAO().getContacts());

        contactArrayList = new ArrayList<>();
        contactsAdapter = new ContactsAdapter(this, contactArrayList, MainActivity.this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(contactsAdapter);
        /**
         * Begin RXJAVA!
         */
        compositeDisposable = new CompositeDisposable();
        Flowable<List<Contact>> flowable = appDatabase.getContactDAO().getContacts();

        compositeDisposable.add(flowable.subscribeOn(Schedulers.computation())// computation потомучто мы будем использовать коллбек!!!!!Consumer - это фнукциональный интерефейс, вместо обсервера
                //вызывается, когда все данный полученные. Обычный коллбек...Как делал Тим Бучалка
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Contact>>() {
                               @Override
                               public void accept(List<Contact> contacts) throws Exception {
                                   contactArrayList.clear();
                                   contactArrayList.addAll(contacts);
                                   contactsAdapter.notifyDataSetChanged();
                               }

                           },// Подписываем ещё Consumer, который будет обрабатывать исклоючения!!!
                        new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {

                            }
                        }));


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addAndEditContacts(false, null, -1);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }

    public void addAndEditContacts(final boolean isUpdate, final Contact contact, final int position) {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getApplicationContext());
        View view = layoutInflaterAndroid.inflate(R.layout.layout_add_contact, null);

        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilderUserInput.setView(view);

        TextView contactTitle = view.findViewById(R.id.new_contact_title);
        final EditText newContact = view.findViewById(R.id.name);
        final EditText contactEmail = view.findViewById(R.id.email);

        contactTitle.setText(!isUpdate ? "Add New Contact" : "Edit Contact");

        if (isUpdate && contact != null) {
            newContact.setText(contact.getName());
            contactEmail.setText(contact.getEmail());
        }

        alertDialogBuilderUserInput
                .setCancelable(true)
                .setPositiveButton(isUpdate ? "Update" : "Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {

                    }
                })
                .setNegativeButton("Delete",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {

                                if (isUpdate) {

                                    deleteContact(contact, position);
                                } else {

                                    dialogBox.cancel();

                                }

                            }
                        });


        final AlertDialog alertDialog = alertDialogBuilderUserInput.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (TextUtils.isEmpty(newContact.getText().toString())) {
                    Toast.makeText(MainActivity.this, "Enter contact name!", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    alertDialog.dismiss();
                }


                if (isUpdate && contact != null) {

                    updateContact(newContact.getText().toString(), contactEmail.getText().toString(), position);
                } else {

                    createContact(newContact.getText().toString(), contactEmail.getText().toString());
                }
            }
        });
    }

    /**
     *Все notifyDataSetChanged() и  работа с List<Contact>
     *     находятся в Консюмере!!!
     *     комментим за ненадобностью!
     */
    private void deleteContact(final Contact contact, int position) {
     //   contactArrayList.remove(position);
       // appDatabase.getContactDAO().deleteContact(contact);
      //  contactsAdapter.notifyDataSetChanged();

        /**
         * Begin RXJAVA!
         */
        compositeDisposable.add(Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {

                appDatabase.getContactDAO().deleteContact(contact);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                        Toast.makeText(MainActivity.this, "Contact deleted successfully ",Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(MainActivity.this, "Error occurred",Toast.LENGTH_LONG).show();
                    }
                }));

    }

    private void updateContact(final String name, final String email, int position) {

        final Contact contact = contactArrayList.get(position);

        contact.setName(name);
        contact.setEmail(email);

//        appDatabase.getContactDAO().updateContact(contact);

     //   contactArrayList.set(position, contact);
      //  contactsAdapter.notifyDataSetChanged();


        /**
         * Begin RXJAVA!
         */
        compositeDisposable.add(Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {

                appDatabase.getContactDAO().updateContact(contact);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                        Toast.makeText(MainActivity.this, "Contact updated successfully ",Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(MainActivity.this, "Error occurred",Toast.LENGTH_LONG).show();
                    }
                }));

    }

    private void createContact(final String name, final String email) {


        /*ID is 0 in contact constructor. As we set the primary key auto generate as true.
         * It will not be used. New ID will be generated every time we add a new record.*/
 //       long id = appDatabase.getContactDAO().addContact(new Contact(0, name, email));

//        Contact contact = appDatabase.getContactDAO().getContact(id);
//
//        if (contact != null) {
//            contactArrayList.add(0, contact);
//            contactsAdapter.notifyDataSetChanged();
//        }

        /**
         * Begin RXJAVA!
         */
        compositeDisposable.add(Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {

                rowIdOfTheItemInserted = appDatabase.getContactDAO().addContact(new Contact(0, name, email));
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                    Toast.makeText(MainActivity.this, "Contact added successfully "+rowIdOfTheItemInserted,Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(MainActivity.this, "Error occurred",Toast.LENGTH_LONG).show();
                    }
                }));


    }
}
