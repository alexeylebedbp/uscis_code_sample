import {useEffect, useContext} from 'react'
import {Platform, Alert} from 'react-native'
import {
    check,
    checkNotifications,
    request,
    requestNotifications,
    PERMISSIONS,
    RESULTS,
    PermissionStatus,
} from 'react-native-permissions'
import {
    NotificationDisabledContext,
} from '@bpinc/ad-mob-context-providers'

import {
    requestMicrophonePermissionFirstTime,
    requestNotificationsPermissionFirstTime,
} from '@bpinc/ad-mob-permissions'

export function usePermissions() {
    const {setPermissions} = useContext(NotificationDisabledContext)

    useEffect(() => {
        if (Platform.OS === 'android') {
            requestMicrophonePermissionFirstTime()
            .then(microphoneResult => {
                setPermissions({
                    microphone: microphoneResult === RESULTS.GRANTED,
                })
            })
            .catch(error => console.log('Error With Microphone Permissions:', error))
        }

        if (Platform.OS === 'ios') {
            requestNotificationsPermissionFirstTime().then(notificationResult => {
                requestMicrophonePermissionFirstTime().then(microphoneResult => {
                    const notificationResultBlocked = notificationResult === RESULTS.BLOCKED || notificationResult === RESULTS.DENIED
                    const microphoneResultBlocked = microphoneResult === RESULTS.BLOCKED || microphoneResult === RESULTS.DENIED
                    
                    setPermissions({
                        notifications: notificationResult === RESULTS.GRANTED,
                        microphone: microphoneResult === RESULTS.GRANTED,
                    })

                    // if (notificationResultBlocked && !microphoneResultBlocked) {
                    //     //Old version rejected by Apple:  Alert.alert('Warning\n Notifications Disabled', 'You won\'t be notified about incoming calls and chats when the app is in background mode. Please enable Notifications by going to Settings -> Brightpattern -> Notifications')
                    //     Alert.alert('Warning\n Notifications Disabled', 'You will not be notified about incoming calls and chats when the app is in background mode.')
                    // } else if (!notificationResultBlocked && microphoneResultBlocked) {
                    //     //Old version rejected by Apple:  Alert.alert('Warning\n Microphone is Disabled', 'Access to Microphone is blocked, you will not be able to make phone calls. Please grant permission to use the Microphone by going to Settings -> Brightpattern -> Microphone')
                    //     Alert.alert('Warning\n Microphone is Disabled', 'Access to Microphone is blocked, you will not be able to make phone calls.')
                    // } else if (notificationResultBlocked && microphoneResultBlocked) {
                    //     //Old version rejected by Apple:    Alert.alert('Warning\n Notifications & Microphone Disabled', 'Notifications and Microphone are disabled, you will not receive notifications or be able to make phone calls. Please enable Notifications and Microphone by going to Settings -> Brightpattern')
                    //     Alert.alert('Warning\n Notifications & Microphone Disabled', 'Notifications and Microphone are disabled, you will not receive notifications or be able to make phone calls.')
                    // }
                })
                .catch(error => console.log('Error With Microphone Permissions:', error))
            })
            .catch(error => console.log('Error with Notification Permissions:', error))
        }
    }, [])
}
