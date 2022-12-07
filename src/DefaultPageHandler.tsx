import React, {useEffect, useContext, useState} from 'react'
import {Platform} from 'react-native'
import VoipPushNotification from 'react-native-voip-push-notification'

import {useSession} from '@bpinc/ad-session-context'
import {useRecentState} from '@bpinc/ad-recent-state-context'
import {LocalSettingsContext} from '@bpinc/ad-local-settings-context'
import {ItemsUIContext} from '@bpinc/ad-mob-interaction-state-controller'
import {useInteractionState} from '@bpinc/ad-interaction-state-context'
import {
    FocusedInteractionContext,
} from '@bpinc/ad-mob-context-providers'

import {
    DefaultPageHandlerProps,
    DefaultPage,
} from './types'

import {RecentsContext} from '@bpinc/ad-mob-recents-context'
import {InitialProps} from '@bpinc/ad-mob-initial-props'

export const DefaultPageHandler: React.FC<DefaultPageHandlerProps> = props => {
    const initialProps = InitialProps.get()

    const {isLoggedIn, isAuthorizing} = useSession()
    const localSettings = useContext(LocalSettingsContext)
    const {itemsUI} = useContext(ItemsUIContext)
    const {agentWelcomeReceived} = useInteractionState()
    const {focusedInteraction} = useContext(FocusedInteractionContext)
    
    const [inCall, setInCall] = useState<boolean | undefined>(Platform.OS === 'ios' ? undefined : initialProps?.accepted || false)
    const [weHaveRecents, setWeHaveRecents] = useState<boolean>()

    const {initialRecentsExist} = useContext(RecentsContext)

    const setDefaultPage = ({defaultPage}: {defaultPage: DefaultPage}) => {
        props.setDefaultPage(defaultPage)
    }

    useEffect(() => {
        if (props.defaultPage !== undefined) return

        const inCall = (itemsUI || [])
        .some(item => 
            item.mediaType === 'voice' &&
                item.state !== 'wrap_up' &&
                item.state !== 'wrap_up_hold',
        )

        setInCall(inCall)
    }, [props.defaultPage, itemsUI])

    useEffect(() => {
        if (Platform.OS !== 'ios') return

        VoipPushNotification.registerVoipToken()
    }, [])

    useEffect(() => {
        if (inCall === undefined) return

        if (inCall) {
            setDefaultPage({defaultPage: 'InteractionState'})
            return
        }

        isLoggedIn && localSettings.get('defaultPage').then(result => {
            const resultIsValid = result  === 'Contacts' || result === 'Recents'
            resultIsValid && setDefaultPage({defaultPage: result as DefaultPage})
        }).catch(err => {
            console.log('localsettings error', err)
        })
    }, [isLoggedIn, inCall])

    useEffect(() => {
        if (!isAuthorizing && isLoggedIn === false) {
            setInCall(false)
            setDefaultPage({defaultPage: undefined})
        }
    }, [isLoggedIn, isAuthorizing])

    useEffect(() => {
        if (
            props.defaultPage === 'InteractionState' &&
            isLoggedIn === true &&
            inCall === true &&
            agentWelcomeReceived === true
        ) {
            if (itemsUI?.some(item => item.mediaType === 'voice')) return

            setInCall(false)
        }
    }, [isLoggedIn, inCall, agentWelcomeReceived, props.defaultPage])

    useEffect(() => {
        if (!initialRecentsExist) {
            setWeHaveRecents(false)
        } else if (initialRecentsExist) {
            setWeHaveRecents(true)
        }
    }, [initialRecentsExist])

    useEffect(() => {
        if (isLoggedIn && weHaveRecents === false) {
            isLoggedIn && weHaveRecents === false && setDefaultPage({defaultPage: 'Contacts'})
        } 

        if (inCall === undefined) return

        if (inCall) {
            setDefaultPage({defaultPage: 'InteractionState'})
            return
        }

        weHaveRecents && setDefaultPage({defaultPage: 'Recents'})
        weHaveRecents && localSettings.set('defaultPage', 'Recents')
    }, [weHaveRecents, isLoggedIn, inCall])
    
    return null
}
