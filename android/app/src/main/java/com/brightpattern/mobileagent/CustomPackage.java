package com.brightpattern.mobileagent;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomPackage implements ReactPackage
{
    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
        return Collections.emptyList();
    }
    @Override
    public List<NativeModule> createNativeModules(
            ReactApplicationContext reactContext) {
        List<NativeModule> modules = new ArrayList<>();

        modules.add(new LockModule(reactContext));
        modules.add(new SoundModule(reactContext));
        modules.add(new ContactsModule(reactContext));
        modules.add(new MainViewController(reactContext));
        modules.add(new CallModule(reactContext));
        modules.add(new UserDefaultsModule(reactContext));
        modules.add(new TelephonyAvailableModule(reactContext));

        return modules;
    }
}
