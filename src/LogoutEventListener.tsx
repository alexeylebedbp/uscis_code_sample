import type React from 'react'
import {useContext, useEffect} from 'react'

import {ItemsUIContext} from '@bpinc/ad-mob-interaction-state-controller'
import {SessionConnectorContext} from '@bpinc/ad-session-context'
import {useWebRtcStateMethods} from '@bpinc/ad-webrtc-state-context'


export const LogoutEventListener: React.FC = () => {
    const {dropWebRtcConnection} = useWebRtcStateMethods()
    const sessionConnector = useContext(SessionConnectorContext)

    const {itemsUI} = useContext(ItemsUIContext)

    useEffect(() => {
        const subscription = sessionConnector && sessionConnector.dispenser.subscribe({
            onLogout() {
                dropWebRtcConnection()
            },
        })
        return () => {
            subscription && subscription.unsubscribe()
        }
    }, [sessionConnector, itemsUI])

    return null
}
