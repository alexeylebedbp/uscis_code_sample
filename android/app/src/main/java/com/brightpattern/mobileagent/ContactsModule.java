package com.brightpattern.mobileagent;

import android.content.pm.ApplicationInfo;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.tomash.androidcontacts.contactgetter.entity.ContactData;
import com.tomash.androidcontacts.contactgetter.entity.PhoneNumber;
import com.tomash.androidcontacts.contactgetter.main.ContactDataFactory;
import com.tomash.androidcontacts.contactgetter.main.contactsDeleter.ContactsDeleter;
import com.tomash.androidcontacts.contactgetter.main.contactsGetter.ContactsGetterBuilder;
import com.tomash.androidcontacts.contactgetter.main.contactsSaver.ContactsSaverBuilder;

import java.util.Collections;
import java.util.List;

import kotlin.Unit;

public class ContactsModule extends ReactContextBaseJavaModule {
    final private String ACCOUNT_TYPE = "com.brightpattern";

    private static ReactApplicationContext reactContext;
    private final ContactsDeleter contactsDeleter;
    private String savedPhoneNumber;

    private ContactsDeleter createContactsDeleter(ReactApplicationContext context) {
        return ContactsDeleter.Companion.invoke(context);
    }

    ContactsModule (ReactApplicationContext context) {
        super(context);
        reactContext = context;
        contactsDeleter = createContactsDeleter(context);
    }

    @Override
    public String getName () {
        return "ContactsModule";
    }

    @ReactMethod
    public void createContact (String firstName, String lastName, String phoneNumber, String accessNumber) {
        clearSavedContact();

        savedPhoneNumber = phoneNumber;
        ContactData data = ContactDataFactory.createEmpty();

        String accountName = firstName + " " + lastName;

        data.setAccountType(null); // ACCOUNT_TYPE
        data.setAccountName(null); // getApplicationName()

        if (!accountName.trim().isEmpty()) {
            data.setCompositeName(accountName);
        } else {
            data.setCompositeName(phoneNumber);
        }

        List<PhoneNumber> phoneList = Collections.singletonList(
                new PhoneNumber(phoneNumber, "")
        );

        data.setPhoneList(phoneList);

        new ContactsSaverBuilder(reactContext)
            .saveContact(data);
    }

    @ReactMethod
    public void clearSavedContact () {
        ContactData contactData = new ContactsGetterBuilder(reactContext)
                .withPhone(savedPhoneNumber)
                .firstOrNull();

        if (contactData == null) return;

        contactsDeleter.deleteContact(contactData, contactDataExceptionACResult -> {
            contactDataExceptionACResult.doFinally(() -> {
                savedPhoneNumber = null;
                return Unit.INSTANCE;
            });
            return Unit.INSTANCE;
        });
    }

    private String getApplicationName () {
        ApplicationInfo applicationInfo = reactContext.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : reactContext.getString(stringId);
    }
}
