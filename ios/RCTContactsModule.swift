//
//  RCTContactsModuleSwift.swift
//  mob
//
//  Created by Anthony Lin on 10/1/21.
//  Copyright © 2021 Brightpattern. All rights reserved.
//

import Foundation
import UIKit
import Contacts

@objc(ContactsModule)
class ContactsModule: NSObject {
  
  private var numberSaved = ""

  @objc
  func createContact (_ firstName: String, lastName: String, phoneNumber: String, accessNumber: String) {
    
      deleteContactsWithNumber(number: accessNumber)
    
      let store = CNContactStore()
      let contact = CNMutableContact()
    
      if (!firstName.trimmingCharacters(in: .whitespaces).isEmpty) {
        contact.givenName = firstName
      }
    
      if (!lastName.trimmingCharacters(in: .whitespaces).isEmpty) {
        contact.familyName = lastName
      }
    
      if (firstName.trimmingCharacters(in: .whitespaces).isEmpty && lastName.trimmingCharacters(in: .whitespaces).isEmpty) {
        let number = CNPhoneNumber.init(stringValue: phoneNumber)
        contact.givenName = number.stringValue
      }
    
      contact.phoneNumbers = [CNLabeledValue(
            label: CNLabelPhoneNumberMain,
            value: CNPhoneNumber(stringValue: accessNumber))]
            
        let saveRequest = CNSaveRequest()
        saveRequest.add(contact, toContainerWithIdentifier: nil)
        do {
            try store.execute(saveRequest)
            numberSaved = accessNumber
        } catch {
            print("Saving contact failed, error: \(error)")
            // Handle the error
        }
    }
  
  @objc
  func requestContactsPermission() {
    let store = CNContactStore()
    let authorizationStatus = CNContactStore.authorizationStatus(for: CNEntityType.contacts)

    if (authorizationStatus == .denied || authorizationStatus == .notDetermined) {
        store.requestAccess(for: CNEntityType.contacts, completionHandler: { (access, accessError) -> Void in })
    }
  }
  
  func deleteContactsWithNumber(number: String) {
    let number = CNPhoneNumber.init(stringValue: number)
    let store = CNContactStore()
    do {
        let predicate = CNContact.predicateForContacts(matching: number)
        let keysToFetch = [CNContactGivenNameKey, CNContactFamilyNameKey] as [CNKeyDescriptor]
        let contacts = try store.unifiedContacts(matching: predicate, keysToFetch: keysToFetch)
        print("Fetched contacts: \(contacts)")
        for contact in contacts {
          deleteContact(contact: contact)
        }
    } catch {
        print("Failed to fetch contact, error: \(error)")
        // Handle the error
    }
  }
  
  func deleteContact(contact: CNContact) {
    let deleteRequest = CNSaveRequest()
    let store = CNContactStore()
    guard let mutableContact = contact.mutableCopy() as? CNMutableContact else { return }
    deleteRequest.delete(mutableContact)
    do {
        try store.execute(deleteRequest)
    } catch {
        print("Deleting contact failed, error: \(error)")
        // Handle the error
    }
  }
  
  @objc
  func clearSavedContact() {
    if (!numberSaved.trimmingCharacters(in: .whitespaces).isEmpty) {
      deleteContactsWithNumber(number: numberSaved)
      numberSaved = ""
    }
  }
}

