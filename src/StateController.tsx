import React, {useRef, useState, useEffect, useContext} from 'react'

import {useDirectoryState} from '@bpinc/ad-directory-state-context'
import {useInteractionState} from '@bpinc/ad-interaction-state-context'
import {useRecentState} from '@bpinc/ad-recent-state-context'
import {useFavoritesState} from '@bpinc/ad-favorites-state-provider'
import {useInteractionInternalChatState} from '@bpinc/ad-interaction-internal-chat-state-provider'
import {FocusedInteractionContext, FetchContext} from '@bpinc/ad-mob-context-providers'
import {ItemAddress} from '@bpinc/ad-mob-interaction-state-types'
import {useSession} from '@bpinc/ad-session-context'

export const StateController: React.FC = props => {
    const prevFocusedInteraction = useRef<ItemAddress | undefined>()

    const {items, agentWelcomeReceived} = useInteractionState()
    const {directoryStateMethods: {getDirectoryCategories}} = useDirectoryState()
    const {recentStateMethods: {getRecentList}} = useRecentState()
    const {favoritesStateMethods: {getFavoritesList}} = useFavoritesState()
    const {focusedInteraction} = useContext(FocusedInteractionContext)
    const {interactionInternalChatStateMethods: {getUnreadChatSessions}} = useInteractionInternalChatState()
    const {statesFetched, setStatesFetched} = useContext(FetchContext)
    const {isLoggedIn} = useSession()

    const noItems = items
    .filter(item => item.state !== 'wrap_up' && item.state !== 'wrap_up_hold')
    .length === 0

    const someItemIsDeliveredWithoutFocus =
        items.some(item => item.state === 'delivered') &&
        focusedInteraction === undefined

    const interactionWasUnfocused =
        focusedInteraction === undefined &&
        prevFocusedInteraction.current !== undefined

    useEffect(() => {
        if (!isLoggedIn) return
        if (!agentWelcomeReceived) return
        if (!(
            noItems ||
            someItemIsDeliveredWithoutFocus ||
            interactionWasUnfocused
        )) return
        if (statesFetched) return

        getDirectoryCategories()
        getUnreadChatSessions()
        
        getRecentList()
        getFavoritesList({})

        setStatesFetched(true)
    }, [agentWelcomeReceived, items, statesFetched, focusedInteraction, isLoggedIn])

    useEffect(() => {
        if (isLoggedIn === false) {
            setStatesFetched(false)
        }
    }, [isLoggedIn])

    useEffect(() => {
        prevFocusedInteraction.current = focusedInteraction
    }, [focusedInteraction])

    return null
}
