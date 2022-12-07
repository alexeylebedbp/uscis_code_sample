import type React from 'react'
import {useContext, useEffect, useRef, useState} from 'react'

import {ItemsUIContext} from '@bpinc/ad-mob-interaction-state-controller'
import type {ItemUI} from '@bpinc/ad-mob-interaction-state-types'
import {useSession} from '@bpinc/ad-session-context'
import {useWebRtcStateMethods} from '@bpinc/ad-webrtc-state-context'


export const WebRtcStats: React.FC = () => {

    const {getStats} = useWebRtcStateMethods()
    const {itemsUI} = useContext(ItemsUIContext)
    const {phoneType} = useSession()

    const interval = useRef<number | undefined>()

    const [onCallEstablished, setOnCallEstablished] = useState(false)

    useEffect(() => {
        if (phoneType === 'phone_type_external') return

        const deliveredVoice = itemsUI?.find((item: ItemUI) => item.mediaType === 'voice' && item.state === 'delivered')
        if (deliveredVoice) {
            setOnCallEstablished(true)
        } else {
            clearInterval(interval.current)
            setOnCallEstablished(false)
        }
    }, [itemsUI, phoneType])

    useEffect(() => {
        getStats()
        if (onCallEstablished) {
            interval.current = setInterval(() => {
                getStats()
            }, 5000)
        }
        return () => {
            interval && clearInterval(interval.current)
        }
    }, [onCallEstablished])

    return null
}
