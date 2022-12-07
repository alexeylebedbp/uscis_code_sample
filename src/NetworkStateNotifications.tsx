import type React from 'react'
import {useContext, useEffect} from 'react'
import {
    AppState,
} from 'react-native'
import PushNotificationIOS from '@react-native-community/push-notification-ios'

import {
    NetworkStateContext,
    TelephonyContext,
} from '@bpinc/ad-mob-context-providers'
import {ItemsUIContext} from '@bpinc/ad-mob-interaction-state-controller'
import {useSession} from '@bpinc/ad-session-context'
import {usePrevious} from '@bpinc/lib-helpers'
import {uuid} from '@bpinc/lib-uuid'


const FILE_PATH = 'ad-mobile-app/src/NetworkStateNotifications.tsx'

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

        if (
            networkState === 'badVoice' &&
            phoneType !== 'phone_type_external' &&
            phoneDeviceEditable &&
            telephonyAvaliable
        ) {
            PushNotificationIOS.setNotificationCategories([
                {
                    id: 'BAD_VOICE',
                    actions: [
                        {
                            id: 'accept.action',
                            title: 'Yes',
                            options: {foreground: true},
                        },
                        {
                            id: 'decline.action',
                            title: 'No',
                        },
                    ],
                },
            ])

            PushNotificationIOS.addNotificationRequest({
                id: uuid(),
                title: 'Poor network connection detected',
                body: 'Use your mobile phone for audio in all subsequent calls within this app?',
                category: 'BAD_VOICE',
                userInfo: {
                    localCategory: 'BAD_VOICE',
                },
            })
        }

        if (networkState === 'poor') {
            PushNotificationIOS.addNotificationRequest({
                id: uuid(),
                title: 'High latency detected',
                body: 'This may affect your ability to make/receive calls and use other app functions',
                category: 'POOR_NETWORK',
                userInfo: {
                    localCategory: 'POOR_NETWORK',
                },
            })
        }
    }, [networkState])

    useEffect(() => {
        if (prevNetworkState === 'badVoice' && networkState !== 'badVoice') {
            PushNotificationIOS.getDeliveredNotifications(notifications => {
                notifications.forEach(notification => {
                    if (notification.category === 'BAD_VOICE') {
                        PushNotificationIOS.removeDeliveredNotifications([notification.identifier])
                    }
                })
            })
        }

        if (prevNetworkState === 'poor' && networkState !== 'poor') {
            PushNotificationIOS.getDeliveredNotifications(notifications => {
                notifications.forEach(notification => {
                    if (notification.category === 'POOR_NETWORK') {
                        PushNotificationIOS.removeDeliveredNotifications([notification.identifier])
                    }
                })
            })
        }
    }, [networkState])

    return null
}
