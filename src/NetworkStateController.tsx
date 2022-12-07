import React, {useContext, useEffect, useState} from 'react'
import {AppState} from 'react-native'
import BackgroundTimer from 'react-native-background-timer'
import AsyncStorage from '@react-native-community/async-storage'
import NetInfo from '@react-native-community/netinfo'

import {NetworkStateContext, PopupContext, PushNotificationContext, TelephonyContext} from '@bpinc/ad-mob-context-providers'
import type {NetworkState, PushNotificationAction} from '@bpinc/ad-mob-context-providers-types'
import {ItemsUIContext} from '@bpinc/ad-mob-interaction-state-controller'
import {NavigationControlContext} from '@bpinc/ad-mob-nav-control'
import {useSession} from '@bpinc/ad-session-context'
import {lastTransportInstance as transport} from '@bpinc/ad-transport-response-memoization'
import type {IncomingAgentWelcome} from '@bpinc/ad-transport-types'
import {useWebRtcStateMethods} from '@bpinc/ad-webrtc-state-context'
import {usePrevious} from '@bpinc/lib-helpers'

import {NetworkStateNotifications} from './NetworkStateNotifications'


export interface LatencyOptions {
    monitorEnabled: boolean
    roundTripThreshold: number
    measurePeriod: number
    decisionThreshold: number

    voiceQualityMonitorEnabled: boolean
    packetLossThreshold: number
    voiceQualityMeasurementInterval: number
}

type Event =
    'onReconnected' |
    'onFailToReconnected' |
    'onInternetIsNotReachable' |
    'onPoorDetected' |
    'onPoorEnded' |
    'onBadVoiceDetected' |
    'onBadVoiceEnded' |
    'onVoiceInteractionsEnded'

interface Transition {
    from: (state: NetworkState) => boolean
    to: NetworkState
}

type Transitions = {
    [key: string]: Transition
}

const FILE_PATH = 'ad-mobile-app/src/NetworkStateController.tsx'

const DEFAULT_LATENCY_OPTIONS: LatencyOptions = {
    monitorEnabled: true,
    roundTripThreshold: 1000,
    measurePeriod: 3000,
    decisionThreshold: 3,

    voiceQualityMonitorEnabled: true,
    packetLossThreshold: 15,
    voiceQualityMeasurementInterval: 10000,
}

const transitions: Transitions = {
    onReconnected: {from: state => state === 'offline', to: 'online'},
    onFailToReconnected: {from: state => state !== 'offline', to: 'offline'},
    onInternetIsNotReachable: {from: state => state !== 'offline', to: 'offline'},
    onPoorDetected: {from: state => state === 'online', to: 'poor'},
    onPoorEnded: {from: state => state === 'poor', to: 'online'},
    onBadVoiceDetected: {from: state => state === 'online' || state === 'poor', to: 'badVoice'},
    onBadVoiceEnded: {from: state => state === 'badVoice', to: 'online'},
    onVoiceInteractionsEnded: {from: state => state === 'badVoice', to: 'online'},
}

export const NetworkStateController: React.FC = props => {
    const {
        networkState,
        setNetworkState,
        setBadVoicePopupWasShown,
    } = useContext(NetworkStateContext)

    const {itemsUI} = useContext(ItemsUIContext)
    const {setPopup} = useContext(PopupContext)
    const {telephonyAvaliable} = useContext(TelephonyContext)
    const {pushNotificationAction} = useContext(PushNotificationContext)
    const {openChangeNumber} = useContext(NavigationControlContext)

    const {
        wsIsReconnecting,
        wsReconnectAttempts,
        forceReconnect,
        sendPing,
        tryToDisconnect,
        phoneType,
        availablePhoneTypes,
        submitPhoneTypeSelection,
        userId,
    } = useSession()

    const {getStats} = useWebRtcStateMethods()

    const [disconnectionPeriod, setDisconnectionPeriod] = useState<number>(0)
    const [disconnectionTimeout, setDisconnectionTimeout] = useState<ReturnType<typeof setTimeout>>()
    const [isInternetReachable, setIsInternetReachable] = useState<boolean | undefined>(undefined)
    const [latencyOptions, setLatencyOptions] = useState<LatencyOptions>(DEFAULT_LATENCY_OPTIONS)
    const [lastPingTimestamp, setLastPingTimestamp] = useState<number | undefined>(undefined)
    const [pingCounter, setPingCounter] = useState<number>(0)
    const [pingRoundTripTimes, setPingRoundTripTimes] = useState<number[]>([])
    const [voiceStats, setVoiceStats] = useState<any>(undefined)
    const [phoneTypeExternal, setPhoneTypeExternal] = useState<boolean>(false)

    const prevVoiceStats = usePrevious(voiceStats)
    const prevItemsUI = usePrevious(itemsUI)

    const phoneDeviceEditable = availablePhoneTypes.phone_type_external === '1'

    const changeState = (event: Event) => {
        const transition = transitions[event]

        if (transition.from(networkState)) {
            setNetworkState(transition.to)
        }
    }

    const onAgentWelcome = (data: IncomingAgentWelcome) => {
        const {
            latency_monitor_enabled,
            latency_round_trip_threshold,
            latency_measure_period,
            latency_decision_threshold,
            mobile_voice_quality_monitor_enabled,
            mobile_packet_loss_threshold,
            mobile_voice_quality_measurement_interval,
        } = (data.tenant_options || {})

        setDisconnectionPeriod(parseInt(data.keep_alive_timeout) * 1000 + 1000)

        const newOptions = DEFAULT_LATENCY_OPTIONS

        if (latency_monitor_enabled !== undefined) newOptions.monitorEnabled = latency_monitor_enabled === '1'
        if (latency_round_trip_threshold !== undefined) newOptions.roundTripThreshold = latency_round_trip_threshold
        if (latency_measure_period !== undefined) newOptions.measurePeriod = latency_measure_period * 1000
        if (latency_decision_threshold !== undefined) newOptions.decisionThreshold = latency_decision_threshold

        if (mobile_voice_quality_monitor_enabled !== undefined) newOptions.voiceQualityMonitorEnabled = mobile_voice_quality_monitor_enabled === '1'
        if (mobile_packet_loss_threshold !== undefined) newOptions.packetLossThreshold = mobile_packet_loss_threshold
        if (mobile_voice_quality_measurement_interval !== undefined) newOptions.voiceQualityMeasurementInterval = mobile_voice_quality_measurement_interval * 1000

        setLatencyOptions(newOptions)
    }

    const onPongMessage = (e: {timestamp: number}) => {
        if (lastPingTimestamp === undefined) return

        disconnectionTimeout && clearTimeout(disconnectionTimeout)

        const pingRoundTripTime = e.timestamp - lastPingTimestamp

        setPingRoundTripTimes([...pingRoundTripTimes, pingRoundTripTime])

        console.log(`${FILE_PATH}: transportPing received incoming pong in ${pingRoundTripTime / 1000} seconds`)
    }

    useEffect(() => {
        if (!disconnectionPeriod) return

        setDisconnectionTimeout(
            setTimeout(() => {
                tryToDisconnect()
            }, disconnectionPeriod),
        )
    }, [disconnectionPeriod])

    useEffect(() => {
        const subscription = transport.dispenser.subscribe({
            agent_welcome: onAgentWelcome,
            onPongMessage: onPongMessage,
        })

        return () => {
            subscription.unsubscribe()
        }
    }, [onAgentWelcome, onPongMessage])

    useEffect(() => {
        const unsubscribe = NetInfo.addEventListener(state => {
            const {isInternetReachable} = state

            if (isInternetReachable === false || isInternetReachable === true) {
                setIsInternetReachable(isInternetReachable)
            }
        })

        return () => {
            unsubscribe()
        }
    }, [])

    useEffect(() => {
        if (latencyOptions.monitorEnabled !== true) return

        setPingCounter(0)

        const pingInterval = BackgroundTimer.setInterval(() => {
            sendPing()
            setLastPingTimestamp(Date.now())
        }, latencyOptions.measurePeriod)

        return () => {
            BackgroundTimer.clearInterval(pingInterval)
        }
    }, [latencyOptions.monitorEnabled])

    useEffect(() => {
        if (latencyOptions.voiceQualityMonitorEnabled !== true) return

        if (itemsUI === undefined) return

        if (phoneType === 'phone_type_external') return

        const itemsUIVoice = itemsUI.filter(item => item.mediaType === 'voice')

        if (itemsUIVoice.length === 0) return

        const voiceCheckInterval = BackgroundTimer.setInterval(async () => {
            let stats: any

            try {
                stats = await getStats()
            } catch (ignore) {}

            if (stats === undefined || stats.length === 0) return

            const statsObject = stats[0]
                .reduce((item: any, accumulator: any) => ({...accumulator, ...item}), {})

            setVoiceStats(statsObject)
        }, latencyOptions.voiceQualityMeasurementInterval)

        return () => {
            BackgroundTimer.clearInterval(voiceCheckInterval)
        }
    }, [latencyOptions.voiceQualityMonitorEnabled, itemsUI, phoneType])

    useEffect(() => {
        if (voiceStats === undefined) return

        const packetsLostDiff = voiceStats.packetsLost - (prevVoiceStats?.packetsLost || 0)
        const packetsReceivedDiff = voiceStats.packetsReceived - (prevVoiceStats?.packetsReceived || 0)

        if (packetsReceivedDiff === 0) return

        const packetsLostInPercentage = packetsLostDiff / packetsReceivedDiff * 100

        if (packetsLostInPercentage > latencyOptions.packetLossThreshold) {
            if (networkState !== 'badVoice') {
                console.log(`${FILE_PATH}: Bad voice condition. Packets lost: ${packetsLostInPercentage}`)
                changeState('onBadVoiceDetected')
            }
        } else {
            if (networkState === 'badVoice') {
                console.log(`${FILE_PATH}: End of the bad voice condition. Packets lost: ${packetsLostInPercentage}`)
                changeState('onBadVoiceEnded')
            }
        }
    }, [voiceStats])

    useEffect(() => {
        setPingCounter(pingCounter + 1)

        if (pingCounter % latencyOptions.decisionThreshold !== 0) return

        if (pingRoundTripTimes.length > 0) {
            if (pingRoundTripTimes.every(el => el > latencyOptions.roundTripThreshold)) {
                if (networkState !== 'poor') {
                    console.log(`${FILE_PATH}: Detected high-latency condition. Ping sequence: ${pingRoundTripTimes}`)
                    changeState('onPoorDetected')
                }
            } else {
                if (networkState === 'poor') {
                    console.log(`${FILE_PATH}: End of the high-latency condition. Ping sequence: ${pingRoundTripTimes}`)
                    changeState('onPoorEnded')
                }
            }

            setPingRoundTripTimes([])
        }
    }, [lastPingTimestamp])

    useEffect(() => {
        if (isInternetReachable === false) {
            changeState('onInternetIsNotReachable')
        }
    }, [isInternetReachable])

    useEffect(() => {
        if (wsReconnectAttempts !== undefined && wsReconnectAttempts >= 3) {
            changeState('onFailToReconnected')
        }
    }, [wsReconnectAttempts])

    useEffect(() => {
        if (wsIsReconnecting === false) {
            changeState('onReconnected')
        }
    }, [wsIsReconnecting])

    useEffect(() => {
        if (itemsUI === undefined || prevItemsUI === undefined) return
        if (itemsUI === prevItemsUI) return

        const prevItemsUIVoice = prevItemsUI.filter(item => item.mediaType === 'voice')
        const itemsUIVoice = itemsUI.filter(item => item.mediaType === 'voice')

        if (itemsUIVoice.length === 0 && prevItemsUIVoice.length > 0) {
            changeState('onVoiceInteractionsEnded')
        }
    }, [itemsUI])

    useEffect(() => {
        if (networkState === 'offline') {
            if (isInternetReachable === true) {
                forceReconnect()
            }
        }
    }, [isInternetReachable])

    useEffect(() => {
        if (networkState === 'online') {
            setPingRoundTripTimes([])
        }

        console.log('networkState', networkState)
    }, [networkState])

    // /////////////////////////////////////////////////////////////

    const changeNumberAndSetPhoneType = async () => {
        const phoneNumber = await AsyncStorage.getItem('@ext_num')

        if (!phoneNumber) {
            openChangeNumber(async number => {
                if (!number) return

                try {
                    await AsyncStorage.setItem('@ext_num', JSON.stringify(number))

                    setPhoneTypeExternal(true)
                } catch (e) {
                    //eslint-disable-next-line
                    console.log('openChangeNumber: Error store number', e)
                }
            })
        } else {
            setPhoneTypeExternal(true)
        }
    }

    const renderBadVoicePopup = () => {
        const buttons = [
            {text: 'Yes', onPress: async () => {
                changeNumberAndSetPhoneType()
            }},
            {text: 'No', onPress: () => {
                setPopup(undefined)
            }},
        ]

        setPopup({
            title: 'Poor network connection detected',
            text: `Use your mobile phone for audio in all subsequent calls within this app?`,
            buttons,
        })

        setBadVoicePopupWasShown(true)
    }

    const submitPhoneTypeExternal = async () => {
        const phoneNumber = await AsyncStorage.getItem('@ext_num')

        if (phoneNumber === null) return

        submitPhoneTypeSelection({
            phoneType: 'phone_type_external',
            userId,
            phoneNumber,
        })
    }

    useEffect(() => {
        if (AppState.currentState !== 'active') return

        if (phoneType === 'phone_type_external') return

        if (networkState !== 'badVoice') return

        if (phoneDeviceEditable && telephonyAvaliable) {
            renderBadVoicePopup()
        }
    }, [phoneType, networkState, phoneDeviceEditable, telephonyAvaliable])

    useEffect(() => {
        if (phoneTypeExternal !== true) return

        if (itemsUI === undefined) return

        const itemsUIVoice = itemsUI.filter(item => item.mediaType === 'voice')

        if (itemsUIVoice.length > 0) return

        submitPhoneTypeExternal()
        setBadVoicePopupWasShown(false)
        setPhoneTypeExternal(false)
    }, [phoneTypeExternal, itemsUI])

    const handlePushNotificationAction = async (pushNotificationAction: PushNotificationAction | undefined) => {
        if (
            pushNotificationAction?.category === 'BAD_VOICE' &&
            pushNotificationAction?.action === 'accept.action'
        ) {
            changeNumberAndSetPhoneType()
            setBadVoicePopupWasShown(true)
        }

        if (
            pushNotificationAction?.category === 'BAD_VOICE' &&
            pushNotificationAction?.action === 'default.action'
        ) {
            renderBadVoicePopup()
        }
    }

    useEffect(() => {
        handlePushNotificationAction(pushNotificationAction)
    }, [pushNotificationAction])

    return (
        <NetworkStateNotifications />
    )
}
