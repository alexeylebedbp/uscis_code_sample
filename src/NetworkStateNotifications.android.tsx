import type React from 'react'
import {useContext, useEffect} from 'react'
import {
    AppState,
} from 'react-native'
import PushNotification from 'react-native-push-notification'

import {
    NetworkStateContext,
    TelephonyContext,
} from '@bpinc/ad-mob-context-providers'
import {ItemsUIContext} from '@bpinc/ad-mob-interaction-state-controller'
import {getCategory} from '@bpinc/ad-mob-notifications/Android/helpers'
import {useSession} from '@bpinc/ad-session-context'
import {usePrevious} from '@bpinc/lib-helpers'


const FILE_PATH = 'ad-mobile-app/src/NetworkStateNotifications.android.tsx'

export const NetworkStateNotifications: React.FC = () => {
    const {networkState} = useContext(NetworkStateContext)
    const {itemsUI, methods: {getItem}} = useContext(ItemsUIContext)
    const {telephonyAvaliable} = useContext(TelephonyContext)

    const {
        phoneType,
        availablePhoneTypes,
    } = useSession()

    const prevNetworkState = usePrevious(networkState)

    const phoneDeviceEditable = availablePhoneTypes.phone_type_external === '1'

    useEffect(() => {
        if (AppState.currentState === 'active') return

        if (itemsUI === undefined) return

        const itemsUIVoice = itemsUI.filter(item => item.mediaType === 'voice')

        if (itemsUIVoice.length === 0) return

        const notification = {
            custom: true,
            expanded: true,
            channelId: 'NetworkStateNotifications',
            title: 'Poor network connection detected',
            message: 'Use your mobile phone for audio in all subsequent calls within this app?',
            actions: '["Yes", "No"]',
            actionColors: '["#007aff", "#007aff"]',
            actionInvokes: '[true, false]',
            userInfo: {
                lcat: 'BAD_VOICE',
            },
        }

        if (
            networkState === 'badVoice' &&
            phoneType !== 'phone_type_external' &&
            phoneDeviceEditable &&
            telephonyAvaliable
        ) {
            PushNotification.localNotification(notification)
        }

        if (networkState === 'poor') {
            PushNotification.localNotification({
                channelId: 'NetworkStateNotifications',
                title: 'High latency detected',
                message: 'This may affect your ability to make/receive calls and use other app functions',
                userInfo: {
                    lcat: 'POOR_NETWORK',
                },
            })
        }
    }, [networkState])

    useEffect(() => {
        if (prevNetworkState === 'badVoice' && networkState !== 'badVoice') {
            PushNotification.getDeliveredNotifications(notifications => {
                notifications.forEach(notification => {
                    if (getCategory(notification) === 'BAD_VOICE') {
                        PushNotification.removeDeliveredNotifications([notification.identifier])
                    }
                })
            })
        }

        if (prevNetworkState === 'poor' && networkState !== 'poor') {
            PushNotification.getDeliveredNotifications(notifications => {
                notifications.forEach(notification => {
                    if (getCategory(notification) === 'POOR_NETWORK') {
                        PushNotification.removeDeliveredNotifications([notification.identifier])
                    }
                })
            })
        }
    }, [networkState])

    return null
}
