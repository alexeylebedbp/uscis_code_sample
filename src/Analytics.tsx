import React, {useEffect, useContext} from 'react'
import analytics from '@react-native-firebase/analytics'
import DeviceInfo from 'react-native-device-info'
import VersionNumber from 'react-native-version-number'
import {ItemsUI} from '@bpinc/ad-mob-interaction-state-types'

import {usePrevious} from '@bpinc/lib-helpers'
import {useSession} from '@bpinc/ad-session-context'
import {ItemsUIContext} from '@bpinc/ad-mob-interaction-state-controller'

export const Analytics: React.FC = () => {
    const {itemsUI} = useContext(ItemsUIContext)

    const {wsIsReconnecting} = useSession()
    const prevWsIsReconnecting = usePrevious(wsIsReconnecting)
    const prevItemsUI = usePrevious([...itemsUI || []])

    useEffect(() => {
        analytics().setUserProperty('bp_device_id', DeviceInfo.getDeviceId())
        analytics().setUserProperty('bp_app_version', `${VersionNumber.appVersion} (${VersionNumber.buildVersion})`)
        analytics().logEvent('bp_app_started', {})
    }, [])

    useEffect(() => {
        if (wsIsReconnecting === false && prevWsIsReconnecting === true) {
            analytics().logEvent('bp_reconnected', {})
        }
    }, [wsIsReconnecting])
    
    useEffect(() => {
        if (itemsUI === prevItemsUI) return

        itemsUI
        ?.filter(item => item.mediaType === 'voice')
        .filter(item => prevItemsUI?.some(subItem =>
            subItem.id === item.id
        ) !== true)
        .forEach(item => {
            analytics().logEvent('bp_new_call', {})
        })
    }, [itemsUI])
    
    useEffect(() => {
        if (itemsUI === prevItemsUI) return

        itemsUI
        ?.filter(item => item.mediaType === 'voice')
        .filter(item => prevItemsUI?.some(prevItem =>
            prevItem.id === item.id &&
            prevItem.item?.state === 'delivery_pending' &&
            item.item?.state === 'delivered'
        ) === true)
        .forEach(item => {
            analytics().logEvent('bp_delivered_call', {})
        })
    }, [itemsUI])

    return null
}
