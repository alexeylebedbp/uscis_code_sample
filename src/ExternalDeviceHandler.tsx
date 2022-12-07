import React, {useEffect, useContext, useState} from 'react'
import {NativeModules} from 'react-native'
import {useSession, SessionConnectorContext} from '@bpinc/ad-session-context'
import {useRecentState} from '@bpinc/ad-recent-state-context'
import {Item} from '@bpinc/ad-interaction-state-types'
import {useInteractionState, InteractionStateConnectorContext} from '@bpinc/ad-interaction-state-context'
import _ from 'lodash'
 
export const ExternalDeviceHandler: React.FC = () => {

    const sessionConnector = useContext(SessionConnectorContext)
    const interactionStateConnector = useContext(InteractionStateConnectorContext)

    const {phoneType} = useSession()

    const {items} = useInteractionState()

    const {recentList, recentStateMethods: {addCompletedItem}} = useRecentState()

    const [savedItems, setSavedItems] = useState<Item[]>([])

    const isExternalPhoneDevice = phoneType === 'phone_type_external'
    
    useEffect(() => {
        const sessionSubscription = sessionConnector && sessionConnector.dispenser.subscribe({
            onAgentWelcomeReceived(agentWelcomeItems) {
                if (isExternalPhoneDevice) {
                    if (agentWelcomeItems) {
                        let callFound = false
                        Object.keys(agentWelcomeItems).forEach(key => {
                            const item = agentWelcomeItems[key]
                            if (item?.media_type === 'voice') {
                                callFound = true
                            }
                        })
                        if (!callFound) {
                            NativeModules.ContactsModule.clearSavedContact()
                        }
                    }
                    // Need to save recent here since app goes to background mode when using external phone device
                    if (savedItems.length && _.isEmpty(agentWelcomeItems)) {
                        savedItems.forEach(item => {
                            addCompletedItem({item})
                        })
                        setSavedItems([])
                    }
                }
            },
        })

        const interactionStateSubscription = interactionStateConnector && interactionStateConnector.dispenser.subscribe({
            onItemCompleted(e) {
                if (isExternalPhoneDevice) {
                    const itemId = e.item.scenarioData?.interactionStepId
                    setSavedItems(prevState => prevState.filter(item => 
                        item.scenarioData?.interactionStepId && item.scenarioData?.interactionStepId !== itemId))
                    if (e.item.mediaType === 'voice') {
                        NativeModules.ContactsModule.clearSavedContact()
                    }
                }
            },
        })

        return () => {
            sessionSubscription && sessionSubscription.unsubscribe()
            interactionStateSubscription && interactionStateSubscription.unsubscribe()
        }
    }, [sessionConnector, interactionStateConnector, isExternalPhoneDevice, savedItems, addCompletedItem])
    

    useEffect(() => {
        if (isExternalPhoneDevice) {
            const voiceItems: Item[] = []
            items.forEach(item => {
                if (item.mediaType === 'voice') {
                    voiceItems.push(item)
                }
            })
            if (voiceItems.length) {
                setSavedItems(voiceItems)
            }
        }
    }, [isExternalPhoneDevice, items])

    useEffect(() => {
        const ids = recentList.map(recent => recent.itemId)
        setSavedItems(prevState => prevState.filter(item => item.scenarioData?.interactionStepId && !ids.includes(item.scenarioData?.interactionStepId)))
    }, [recentList])

    return (
        null
    )
}
