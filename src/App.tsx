import React, {useContext, useEffect, useState} from 'react'
import {
    Alert,
    DeviceEventEmitter,
    NativeModules,
    Platform,
    StatusBar,
    StyleSheet,
} from 'react-native'
import CallState from 'react-native-call-state'
import {enableScreens} from 'react-native-screens'

import {
    CallStateContext,
    IOSCallStateContext,
    LoginContext,
} from '@bpinc/ad-mob-context-providers'
import {InitialProps} from '@bpinc/ad-mob-initial-props'
import {CallScreen} from '@bpinc/ad-mob-interaction-state-ui'
import {LoginPage} from '@bpinc/ad-mob-login-ui'
import {useSession} from '@bpinc/ad-session-context'

import {AppContent} from './AppContent'
import {DefaultPageHandler} from './DefaultPageHandler'
import {usePermissions} from './Permissions'
import {StateController} from './StateController'
import type {
    DefaultPage,
} from './types'


export const App: React.FC = () => {
    const initialProps = InitialProps.get()

    const {iosCallState} = useContext(IOSCallStateContext)
    const {callState, setCallState} = useContext(CallStateContext)
    const {isLoginScreenAuth} = useContext(LoginContext)

    const [defaultPage, setDefaultPage] = useState<DefaultPage>(
        Platform.OS === 'ios'
            ? iosCallState
                ? 'InteractionState'
                : undefined
            : initialProps?.accepted === true
                ? 'InteractionState'
                : undefined,
    )

    usePermissions()
    Platform.OS === 'ios' && enableScreens()

    const {
        //Session State
        isLoggedIn,
        showLoginDialog,
        offerForceLogin,
        //Session Control Methods
        checkToken,
        isLoggedInWithPhoneType,
        forceLogin,
        rejectForceLogin,
        displayNameFormat,
    } = useSession()

    useEffect(() => {
        NativeModules.MainViewController.hideSplash('splash')

        if (Platform.OS === 'android') {
            CallState.startListener()

            DeviceEventEmitter.addListener('callStateUpdated', data => {
                setCallState(data.state)
            })
        }

        return () => {
            if (Platform.OS === 'android') {
                CallState.stopListener()
            }
        }
    }, [])

    useEffect(() => {
        checkToken()
    }, [checkToken])

    useEffect(() => {
        if (Platform.OS === 'ios' && displayNameFormat !== undefined) {
            // Store displayNameFormat to be accessed by iOS push notification service extension
            NativeModules.UserDefaultsModule.saveToUserDefaults('displayNameFormat', displayNameFormat)
        }
    }, [displayNameFormat])

    useEffect(() => {
        offerForceLogin && forceLoginAlert()
    }, [offerForceLogin])

    const forceLoginHandler = () => {
        forceLogin()
    }

    const forceLoginAlert = () => {
        const alertMsg = 'User is already logged in. Force log in?'
        Alert.alert (
            'Warning', alertMsg,
            [{text: 'Cancel', style: 'cancel', onPress: rejectForceLogin},
                {text: 'Ok', style: 'destructive', onPress: forceLoginHandler}],
        )
    }

    const showLoginPage = offerForceLogin || showLoginDialog || isLoginScreenAuth

    return (
        <>
            <StateController />

            { Platform.OS === 'android' && <StatusBar backgroundColor="transparent" barStyle="dark-content" /> }

            <DefaultPageHandler
                defaultPage={defaultPage}
                setDefaultPage={setDefaultPage}
            />

            {showLoginPage
                ? <LoginPage />
                : isLoggedInWithPhoneType
                    ? (
                        <>
                            {defaultPage === 'InteractionState' && <CallScreen />}

                            <AppContent defaultPage={defaultPage} />
                        </>
                    )
                    : defaultPage === 'InteractionState' && <CallScreen />
            }
        </>
    )
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
        backgroundColor: 'white',
    },
    text: {
        fontSize: 18,
        textAlign: 'center',
        color: 'rgb(136, 136, 136)',
        margin: 10,
        fontFamily: Platform.OS === 'ios'
            ? 'Trebuchet MS'
            : 'default',
    },
    instructions: {
        textAlign: 'center',
        color: '#333333',
        marginBottom: 5,
    },
})
