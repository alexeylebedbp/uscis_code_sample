import React, {useEffect, useContext} from 'react'
import CookieManager, {Cookies}  from '@react-native-community/cookies'
import {LocalSettingsContext} from '@bpinc/ad-local-settings-context'
import {useSession} from '@bpinc/ad-session-context'

export const ServerCookieManager: React.FC = () => {

    const localSettings = useContext(LocalSettingsContext)

    const {domain} = useSession()

    useEffect(() => {
        domain && CookieManager.get('https://' + domain).then ((res:Cookies) => {
            const bpCookie = res['X-BP-SESSION-ID'] ? 'X-BP-SESSION-ID=' + res['X-BP-SESSION-ID'].value + ';' : ''
            bpCookie && localSettings.set('serverCookie', bpCookie)
        })
    }, [domain, localSettings])

    return null
}