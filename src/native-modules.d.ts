// Types for native modules used in ad-mobile-app

import 'react-native'

export interface UserDefaultsModuleInterface {
    saveToUserDefaults: (key: string, value: string) => void
}

export interface ContactsModule {
    clearSavedContact(): void
}

declare module 'react-native' {
    interface NativeModulesStatic {
        UserDefaultsModule: UserDefaultsModuleInterface
        ContactsModule: ContactsModule
    }
}
