import React, {useState, useEffect, useContext, useRef} from 'react'
import {NativeModules} from 'react-native'
import {useInteractionState} from '@bpinc/ad-interaction-state-context'
import {
    IOSCallStateContext,
    FocusedInteractionContext,
} from '@bpinc/ad-mob-context-providers'
import {Item} from '@bpinc/ad-interaction-state-types'
import {useSession} from '@bpinc/ad-session-context'
import {ItemsUIContext} from '@bpinc/ad-mob-interaction-state-controller'
import {CallScreen} from '@bpinc/ad-mob-interaction-state-ui'

export const AppLocked: React.FC = () => {
    const {focusedInteraction} = useContext(FocusedInteractionContext)
    const {iosCallState} =  useContext(IOSCallStateContext)
    const {items, interactionStateMethods: {completeItem}} = useInteractionState()
    const [inboundCalls, setInboundCalls] = useState<Item[]>()
    const {phoneType, wsIsReconnecting} = useSession()
    const [activeCall, setActiveCall] = useState<Item>()
    const {itemsUI} = useContext(ItemsUIContext)

    const prevActiveCall = useRef<Item | undefined>(activeCall)

    const idsMatched = (item: Item) => {
        return ((item.scenarioData?.interactionStepId && iosCallState?.notification?._data?.item_id === item.scenarioData?.interactionStepId) || 
            (item.scenarioData?.previousItemId && iosCallState?.notification?._data?.item_id === item.scenarioData?.previousItemId))
    }

    useEffect(() => {
        const inboundCalls = items.filter(item => item.mediaType === 'voice' && item.direction === 'inbound')
        setInboundCalls([...inboundCalls])
    }, [items])

    useEffect(() => {
        prevActiveCall.current = activeCall
    }, [activeCall])

    useEffect(() => {
        console.log('items locked screen', items)
    }, [items])

    useEffect(() => {
        console.log('itemsUI locked screen', itemsUI)
    }, [itemsUI])

    useEffect(() => {
        inboundCalls && inboundCalls.forEach(item => {
            if ((item.state === 'delivery_pending' || item.state === 'delivered') && idsMatched(item)) {
                setActiveCall(item)
            }
        })
    }, [inboundCalls, iosCallState])

    useEffect(() => {
        if (wsIsReconnecting === false && activeCall) {
            if ((!inboundCalls || !inboundCalls[0] || !inboundCalls.find(item => item.state != 'wrap_up' && item.state != 'wrap_up_hold'))) {
                setActiveCall(undefined)
            } else {
                inboundCalls && inboundCalls.forEach(item => {
                    if (idsMatched(item) && (item.state === 'wrap_up' || item.state === 'wrap_up_hold')) {
                        setActiveCall(undefined)
                    }
                })
            }
        }
    }, [activeCall, inboundCalls, iosCallState, wsIsReconnecting])

    useEffect(() => {
        inboundCalls && inboundCalls.forEach(item => {
            item.callData && item.callData.errorMessage && completeItem(item.id)
        })
    }, [inboundCalls, iosCallState])

    useEffect(() => {
        //if the app is locked we should run endCall to prevent double-call IOS interface and active-locked-call interface
        if (phoneType === 'phone_type_external' && iosCallState && iosCallState.callAccepted?.accepted === true) {
            iosCallState && iosCallState.endCall()
        }
    }, [inboundCalls, iosCallState])


    const inCall = (itemsUI || [])
    .some(item => 
        item.mediaType === 'voice' &&
            item.state !== 'wrap_up' &&
            item.state !== 'wrap_up_hold',
    )

    if (inCall) {
        return (
            <CallScreen />
        )
    }

    return null
}
