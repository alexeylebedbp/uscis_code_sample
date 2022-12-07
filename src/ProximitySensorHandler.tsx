import React, {useEffect, useContext} from 'react'
import {CallPageContext, FocusedInteractionContext} from '@bpinc/ad-mob-context-providers'
import InCallManager from '@bpinc/lib-mob-rn-incall-manager'

export const ProximitySensorHandler: React.FC = () => {

    const {focusedInteraction} = useContext(FocusedInteractionContext)
    const {activeSwipeIndex} = useContext(CallPageContext)

    useEffect(() => {
        if (focusedInteraction && activeSwipeIndex !== undefined) {
            if (activeSwipeIndex === 0) {
                InCallManager.startProximitySensor()
            } else {
                InCallManager.stopProximitySensor()
            }
        } else {
            InCallManager.stopProximitySensor()
        }
    }, [activeSwipeIndex, focusedInteraction])

    return null
}
