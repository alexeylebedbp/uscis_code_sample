import type React from 'react'
import {useContext, useEffect} from 'react'
import {NativeModules} from 'react-native'

import {LoginContext} from '@bpinc/ad-mob-context-providers'
import {useSession} from '@bpinc/ad-session-context'


export const SplashHandler: React.FC = () => {

    const {
        showLoginDialog,
        offerForceLogin,
        isLoggedInWithPhoneType,
    } = useSession()

    const {isLoginScreenAuth} = useContext(LoginContext)

    const showLoginPage = offerForceLogin || showLoginDialog || isLoginScreenAuth
    const showConnectingSplash = !showLoginPage && !isLoggedInWithPhoneType

    useEffect(() => {
        if (showConnectingSplash) {
            NativeModules.MainViewController.splash('connecting', 'Connecting...')
        } else {
            requestAnimationFrame(() => {
                NativeModules.MainViewController.hideSplash('connecting')
            })
        }
    }, [showConnectingSplash])

    return null
}
