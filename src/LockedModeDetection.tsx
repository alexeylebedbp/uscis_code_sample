import React, {useContext, useEffect, useState} from 'react'
import {Platform, AppState, AppStateStatus} from 'react-native'
import {IOSCallStateContext} from '@bpinc/ad-mob-context-providers'
import {useInteractionState} from '@bpinc/ad-interaction-state-context'
import {useSession} from '@bpinc/ad-session-context'

interface LockedModeDetectorProps {
    setLockedNotificationAppMode: (arg: boolean) => void
}

export const LockedNotificationModeDetector: React.FC<LockedModeDetectorProps> = props => {

    const {iosCallState} = useContext(IOSCallStateContext)
    const {items} = useInteractionState()
    const {wsIsReconnecting} = useSession()

    const [appState, setAppState] = useState<AppStateStatus>()

    //this is for outbound calls cases when we switch to background mode during outbound call
    const establishedCallExists = () => {
        let exists = false
        items.forEach(item => {
            if (item.callData && item.callData.callState === 'established') {
                exists = true
            }
        })
        return exists
    }

    //the app was unmounted every time the fullscreen CallKit was shown. We mounted the app again on the Accept button
    //const incomingCallLockedMode = () => iosCallState && iosCallState.callUUID

    //this is for cases when the app is locked and we need to reconnect websocket after getting VOIP push, we still need to switch to applocked app
    //fix: the app is moved into locked mode only if the websocket is disconnected
    const incomingCallLockedMode = () => iosCallState && iosCallState.callUUID // && wsIsReconnecting - removed to improve connection performance

    //ios puts memory/CPU limitations when the app is in background mode or the screen is locked. Both cases related to handling calls in these states
    const isLimitedMode = () => (appState === 'inactive' || appState === 'background') && Platform.OS === 'ios'

    useEffect(() => {
        if (isLimitedMode() && (establishedCallExists() || incomingCallLockedMode())) {
            props.setLockedNotificationAppMode(true)
        } else {
            props.setLockedNotificationAppMode(false)
        }
    }, [appState, items])

    useEffect(() => {
        AppState.addEventListener('change', handleAppStateChange)
        return () => { AppState.removeEventListener('change', handleAppStateChange) }
    }, [])

    const handleAppStateChange = (state: AppStateStatus) => { setAppState(state) }

    return null
}
