import React, {useEffect} from 'react'
import {NativeModules, Platform} from 'react-native'
import RNCallKeep from 'react-native-callkeep'
import {SafeAreaProvider} from 'react-native-safe-area-context'
import {enableES5} from 'immer'

import {AgentStateProvider} from '@bpinc/ad-agent-state-context'
import {AppConfigStateProvider} from '@bpinc/ad-app-config-state-provider'
import {DIDStateProvider} from '@bpinc/ad-did-state-provider'
import {FavoritesStateProvider} from '@bpinc/ad-favorites-state-provider'
import {LocalProviders} from '@bpinc/ad-local-providers'
import {
    CallPageState,
    CallStateState,
    Debug,
    FetchState,
    FocusedInteractionState,
    InitialDeepLinkState,
    IOSCallState,
    IOSPushNotificationState,
    LastInteractionState,
    LoginState,
    NetworkState,
    NotificationDisabledState,
    PopupState,
    PushNotificationState,
    SettingsState,
    TelephonyState,
} from '@bpinc/ad-mob-context-providers'
import {InitialProps} from '@bpinc/ad-mob-initial-props'
import {InteractionStateController} from '@bpinc/ad-mob-interaction-state-controller'
import {NavigationControl} from '@bpinc/ad-mob-nav-control'
import {NotificationDataHandler} from '@bpinc/ad-mob-notifications/'
import {Notifications} from '@bpinc/ad-mob-notifications/dist/Notifications'
import {RecentsState} from '@bpinc/ad-mob-recents-context'
import {StateCleaner} from '@bpinc/ad-mob-state-cleaner'
import {AgentStateWidget} from '@bpinc/ad-mob-widget'
import {createPlatformSpecific} from '@bpinc/ad-platform-specific-mob-impl'
import {ServiceDetailsStateProvider} from '@bpinc/ad-service-details-state-provider'
import {UserPresenceStateProvider} from '@bpinc/ad-user-presence-state-context'

import {AppReceiver} from './AppReceiver'
import {DeepLinkInitialHandler} from './DeepLinkInitialHandler'
import {LogoutEventListener} from './LogoutEventListener'
import {MemoryUsageLogger} from './MemoryUsageLogger'
import {NetworkStateController} from './NetworkStateController'
import {ProximitySensorHandler} from './ProximitySensorHandler'
import {SplashHandler} from './SplashHandler'
import {WebRtcStats} from './WebRtcStats'


interface RConsole {
    disableYellowBox: boolean
}

(console as unknown as RConsole).disableYellowBox = true

interface ConnectorProperties {
    debug: boolean
    initialProps?: any
}

const options = {
    ios: {
        appName: 'BrightPattern',
        imageName: 'call_icon',
        supportsVideo: false,
        maximumCallGroups: '2',
        maximumCallsPerCallGroup: '3',
    },
    android: {
        alertTitle: 'Permissions Required',
        alertDescription: 'Bright Pattern Mobile app needs to access your phone calling accounts to make calls',
        cancelButton: 'Cancel',
        okButton: 'Ok',
        imageName: 'sim_icon',
        additionalPermissions: [],
    },
}

Platform.OS === 'android' && enableES5()

export const Connector: React.FC<ConnectorProperties> = props => {
    InitialProps.set(props)
    const {debug} = props

    useEffect(() => {
        if (Platform.OS === 'android') return

        RNCallKeep.setup(options)
    }, [])

    useEffect(() => {
        NativeModules.MainViewController.setAppIsMounted(true)

        return () => {
            NativeModules.MainViewController.setAppIsMounted(false)
        }
    }, [])

    return (
        <LocalProviders platformSpecific={createPlatformSpecific()} app="mob">
            {() => (
                <Debug debugBuild={debug}>
                    <IOSCallState>
                        <NetworkState>
                            <TelephonyState>
                                <SettingsState>
                                    <LastInteractionState>
                                        <NotificationDisabledState>
                                            <IOSPushNotificationState>
                                                <InteractionStateController>
                                                    <InitialDeepLinkState>
                                                        <FetchState>
                                                            <AppConfigStateProvider>
                                                                <DIDStateProvider>
                                                                    <AgentStateProvider>
                                                                        <UserPresenceStateProvider>
                                                                            <FavoritesStateProvider>
                                                                                <ServiceDetailsStateProvider>
                                                                                    <FocusedInteractionState>
                                                                                        <LoginState>
                                                                                            <RecentsState>
                                                                                                <CallPageState>
                                                                                                    <CallStateState>
                                                                                                        <PopupState>
                                                                                                            <PushNotificationState>
                                                                                                                <NavigationControl>
                                                                                                                    <NetworkStateController />
                                                                                                                    <WebRtcStats />
                                                                                                                    <MemoryUsageLogger />
                                                                                                                    <LogoutEventListener />
                                                                                                                    <ProximitySensorHandler />
                                                                                                                    <Notifications />
                                                                                                                    <NotificationDataHandler />
                                                                                                                    <AgentStateWidget/>
                                                                                                                    <StateCleaner />
                                                                                                                    <SplashHandler />
                                                                                                                    <DeepLinkInitialHandler />
                                                                                                                    <SafeAreaProvider>
                                                                                                                        <AppReceiver />
                                                                                                                    </SafeAreaProvider>
                                                                                                                </NavigationControl>
                                                                                                            </PushNotificationState>
                                                                                                        </PopupState>
                                                                                                    </CallStateState>
                                                                                                </CallPageState>
                                                                                            </RecentsState>
                                                                                        </LoginState>
                                                                                    </FocusedInteractionState>
                                                                                </ServiceDetailsStateProvider>
                                                                            </FavoritesStateProvider>
                                                                        </UserPresenceStateProvider>
                                                                    </AgentStateProvider>
                                                                </DIDStateProvider>
                                                            </AppConfigStateProvider>
                                                        </FetchState>
                                                    </InitialDeepLinkState>
                                                </InteractionStateController>
                                            </IOSPushNotificationState>
                                        </NotificationDisabledState>
                                    </LastInteractionState>
                                </SettingsState>
                            </TelephonyState>
                        </NetworkState>
                    </IOSCallState>
                </Debug>
            )}
        </LocalProviders>
    )
}
