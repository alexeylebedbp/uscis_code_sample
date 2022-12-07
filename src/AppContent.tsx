import React from 'react'
import {Platform} from 'react-native'
import FlashMessage from 'react-native-flash-message'
import {ActionSheetProvider} from '@expo/react-native-action-sheet'
import NetInfo from '@react-native-community/netinfo'
import {createBottomTabNavigator} from '@react-navigation/bottom-tabs'
import {NavigationContainer} from '@react-navigation/native'
import type {
    StackNavigationOptions} from '@react-navigation/stack'
import {
    CardStyleInterpolators,
    createStackNavigator,
} from '@react-navigation/stack'

import {CaseScreen} from '@bpinc/ad-mob-cases-ui'
import {
    ContactProfileNav,
    Contacts,
    InternalProfileNav,
} from '@bpinc/ad-mob-contacts-ui'
import {
    InteractionStateFocusState,
    SpeakerphoneState,
    TransferScreenState,
} from '@bpinc/ad-mob-context-providers'
import {DirectoryState} from '@bpinc/ad-mob-directory-context'
import {DispositionSelectorNav} from '@bpinc/ad-mob-disposition-selector'
import {Favorites} from '@bpinc/ad-mob-favorites-ui'
import {FlashMessageRender} from '@bpinc/ad-mob-flash-message'
import {useExit} from '@bpinc/ad-mob-helpers'
import {InitiationChecks} from '@bpinc/ad-mob-initiation-checks'
import {InteractionPageControl} from '@bpinc/ad-mob-interaction-page-controller'
import {
    InteractionState,
    Keypad,
    TransferScreen,
} from '@bpinc/ad-mob-interaction-state-ui'
import type {
    StackNavigatorScreensandParams,
    TabNavigatorScreensAndParams} from '@bpinc/ad-mob-nav-control'
import {
    navigationRef,
} from '@bpinc/ad-mob-nav-control'
import {NavbarNav} from '@bpinc/ad-mob-navbar-ui'
import {RenderPopup} from '@bpinc/ad-mob-popup-ui'
import {Recents} from '@bpinc/ad-mob-recents-ui'
import {SelectStateMenu} from '@bpinc/ad-mob-select-state-ui'
import {ServiceSelector} from '@bpinc/ad-mob-service-selector'
import {ServicesDispState} from '@bpinc/ad-mob-services-disp-context'
import {SettingsState} from '@bpinc/ad-mob-settings-context'
import {ChangeNumber, Settings} from '@bpinc/ad-mob-settings-ui'
import {HeaderBackImage, WifiLog} from '@bpinc/ad-mob-ui-elements'
import {wifiEventsReceiver} from '@bpinc/ad-transport-websocket/dist/src/impl'
import {CustomEventConstructor} from '@bpinc/lib-typed-events'

import {RenderIncomingCall} from './RenderIncomingCall'
import {ServerCookieManager} from './ServerCookieManager'
import type {DefaultPage} from './types'


NetInfo.addEventListener(state => {
    wifiEventsReceiver.dispatchEvent(new CustomEventConstructor('wifi', {detail: `NetInfo isInternetReachable, ${state.isInternetReachable}`}))
})


export const AppContent: React.FC<{defaultPage: DefaultPage}> = React.memo(({defaultPage}) => {
    const TabNav = createBottomTabNavigator<TabNavigatorScreensAndParams>()
    const Stack = createStackNavigator<StackNavigatorScreensandParams>()

    const Tabs: React.FC = () => {
        return (
            <TabNav.Navigator
                initialRouteName={defaultPage}
                screenOptions={{tabBarVisible: true}}
                tabBar={props => <NavbarNav {...props} />}
                lazy={defaultPage === 'InteractionState'}
            >
                <TabNav.Screen name="Favorites" component={Favorites} />
                <TabNav.Screen name="Contacts" component={Contacts} />
                <TabNav.Screen name="Recents" component={Recents} />
                <TabNav.Screen name="Keypad" component={Keypad} />
                <TabNav.Screen name="Settings" component={Settings} />
                <TabNav.Screen
                    name="InteractionState"
                    component={InteractionState}
                    initialParams={{itemAddress: {phoneNumber: '', mediaType: 'unknown'}}}
                    options={{tabBarVisible: false}}
                />
            </TabNav.Navigator>
        )
    }

    const transferScreenOptions: StackNavigationOptions = {
        gestureDirection: 'vertical',
        cardStyleInterpolator: CardStyleInterpolators.forVerticalIOS,
        headerShown: false,
        cardStyle: {backgroundColor: '#fff', opacity: 0.99},
    }


    const Screens: React.FC = () => {
        Platform.OS === 'android' && useExit('Press one more time to exit', 3000)

        return (
            <Stack.Navigator
                screenOptions={{cardStyle: {backgroundColor: 'transparent',
                    opacity: 0.99}, headerTitleAllowFontScaling: false}}
                initialRouteName="Tabs"
            >
                <Stack.Screen name="Tabs" component={Tabs} options={{headerShown: false}}/>
                <Stack.Screen
                    name="ServiceSelector"
                    component={ServiceSelector}
                    options={{headerShown: false}}
                    initialParams={{mode: 'selection'}}
                />
                <Stack.Screen
                    name="SelectStateMenu"
                    component={SelectStateMenu}
                    options={{headerShown: false}}
                />
                <Stack.Screen
                    name="CaseScreen"
                    component={CaseScreen}
                    options={({route}) => ({
                        title: route && route.params && route.params.caseNumber ? `#${route.params.caseNumber}` : 'Case',
                        headerBackTitleVisible: false,
                        headerBackImage: () => <HeaderBackImage />,
                        cardStyle: {backgroundColor: 'white'},
                    })}
                    initialParams={{caseId: undefined, caseNumber: undefined}}
                />
                <Stack.Screen
                    name="ExternalProfileStack"
                    component={ContactProfileNav}
                    options={{
                        headerTitle: 'Contact',
                        headerBackTitleVisible: false,
                        headerBackImage: () => <HeaderBackImage />,
                        cardStyle: {backgroundColor: 'white'},
                    }}
                    initialParams={{contact: undefined}}
                />
                <Stack.Screen
                    name="InternalProfileStack"
                    component={InternalProfileNav}
                    options={{
                        headerTitle: 'Contact',
                        headerBackTitleVisible: false,
                        headerBackImage: () => <HeaderBackImage />,
                        cardStyle: {backgroundColor: 'white'},
                    }}
                    initialParams={{contact: undefined}}
                />
                <Stack.Screen
                    name="DispositionSelector"
                    component={DispositionSelectorNav}
                    options={{headerShown: false}}
                    initialParams={{itemId: '', serviceId: ''}}
                />
                <Stack.Screen
                    name="TransferScreen"
                    component={TransferScreen}
                    options={transferScreenOptions}
                />
                <Stack.Screen
                    name="ChangeNumber"
                    component={ChangeNumber}
                    initialParams={{onDone: undefined}}
                    options={transferScreenOptions}
                />
            </Stack.Navigator>
        )
    }

    return (
        <React.Fragment>
            <ServerCookieManager />
            <SettingsState>
                <InteractionStateFocusState>
                    <DirectoryState>
                        <ServicesDispState>
                            <ActionSheetProvider>
                                <TransferScreenState>
                                    <SpeakerphoneState>
                                        <InitiationChecks>
                                            <NavigationContainer ref={navigationRef}>
                                                <WifiLog />
                                                <Screens />
                                            </NavigationContainer>
                                            <InteractionPageControl />
                                            <RenderPopup />
                                            <RenderIncomingCall />
                                            <FlashMessage MessageComponent={FlashMessageRender}/>
                                        </InitiationChecks>
                                    </SpeakerphoneState>
                                </TransferScreenState>
                            </ActionSheetProvider>
                        </ServicesDispState>
                    </DirectoryState>
                </InteractionStateFocusState>
            </SettingsState>
        </React.Fragment>
    )
})
