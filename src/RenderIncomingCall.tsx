import React, {useContext, useEffect, useRef, useState} from 'react'
import {
    Image,
    ImageBackground,
    Modal,
    NativeModules,
    Platform,
    StyleSheet,
    Text,
    TouchableOpacity,
    View,
} from 'react-native'
import RNCallKeep from 'react-native-callkeep'
import SafeAreaView from 'react-native-safe-area-view'
import LottieView from 'lottie-react-native'

import {useInteractionStateMethods} from '@bpinc/ad-interaction-state-context'
import {IOSCallStateContext} from '@bpinc/ad-mob-context-providers'
import {formatNumber} from '@bpinc/ad-mob-formatters'
import {getDuration, mediumFontWeight} from '@bpinc/ad-mob-helpers'
import {ItemsUIContext} from '@bpinc/ad-mob-interaction-state-controller'
import type {ItemUI} from '@bpinc/ad-mob-interaction-state-types'
import {useSession} from '@bpinc/ad-session-context'


const INCOMING_CALL_TIMEOUT = 5000

export const RenderIncomingCall: React.FC = () => {
    const timeout = useRef<ReturnType<typeof setTimeout> | undefined>(undefined)

    const {itemsUI, methods: {changeActiveInteraction}} = useContext(ItemsUIContext)
    const {interactionStateMethods: {sendAgentNotificationResult, disconnectItem, acceptItem}} = useInteractionStateMethods()
    const {activeVoipIds} = useContext(IOSCallStateContext)

    const [showIncomingCall, setShowIncomingCall] = useState<'multiple' | 'single' | 'none'>('none')
    const [incomingItem, setIncomingItem] = useState<ItemUI>()

    const {phoneType} = useSession()

    useEffect(() => {
        return () => {
            if (timeout.current) {
                clearTimeout(timeout.current)
                timeout.current = undefined
            }
        }
    }, [])

    useEffect(() => {
        // Must render the incoming call screen if a call comes while the native call is still active
        const checkIfNativeCallExists = async () => {
            if (Platform.OS === 'ios') {
                return RNCallKeep.checkIfBusy().then(res => {
                    return res
                })
            }
        }

        const renderIncomingForExternalDevice = async () => {
            if (itemsUI) {
                let inboundCallWithExistingCall: ItemUI | undefined = undefined
                const isMultipleCalls = itemsUI.filter(item => item.mediaType === 'voice').length > 1

                const nativeCallExists = await checkIfNativeCallExists()
                if (isMultipleCalls || nativeCallExists) {
                    itemsUI?.forEach((item: ItemUI) => {
                        if (item.id && item.mediaType === 'voice' && item.direction === 'inbound' && item.state === 'delivery_pending') {
                            inboundCallWithExistingCall = item
                        }
                    })
                }
                if (inboundCallWithExistingCall) {
                    if (timeout.current === undefined) {
                        timeout.current = setTimeout(() => {
                            setShowIncomingCall(isMultipleCalls ? 'multiple' : 'single')
                            setIncomingItem(inboundCallWithExistingCall)
                        }, 200)
                    }
                } else {
                    if (timeout.current) {
                        clearTimeout(timeout.current)
                        timeout.current = undefined
                    }
                    setIncomingItem(undefined)
                    setShowIncomingCall('none')
                }
            }
        }

        if (phoneType === 'phone_type_browser') {
            let isDeliveryPendingWithoutVoip: ItemUI | undefined = undefined
            itemsUI?.forEach((item: ItemUI) => {
                if (item.id && item.mediaType === 'voice' && item.direction === 'inbound' && item.state === 'delivery_pending') {
                    if (!activeVoipIds.find(id => id === item.id)) {
                        isDeliveryPendingWithoutVoip = item
                    }
                }
            })

            if (isDeliveryPendingWithoutVoip) {
                const {id} = isDeliveryPendingWithoutVoip
                // Render incoming call screen if VoIP notification not received within 5s of delivery_pending state
                if (timeout.current === undefined) {
                    timeout.current = setTimeout(() => {
                        NativeModules.CallModule.fakeCallShown(id)
                        setShowIncomingCall('single')
                        setIncomingItem(isDeliveryPendingWithoutVoip)
                    }, INCOMING_CALL_TIMEOUT)
                }
            } else {
                if (timeout.current) {
                    clearTimeout(timeout.current)
                    timeout.current = undefined
                }
                setIncomingItem(undefined)
                setShowIncomingCall('none')
            }
        } else if (phoneType === 'phone_type_external') {
            renderIncomingForExternalDevice()
        }
    }, [itemsUI, activeVoipIds, phoneType])

    const acceptCall = () => {
        setShowIncomingCall('none')
        if (incomingItem?.item && incomingItem.item.scenarioData) {
            if (Platform.OS === 'android') {
                NativeModules.IncomingCall.showOngoingCallForegroundService(incomingItem.item.displayData.displayName)
            }
            sendAgentNotificationResult({callAccepted: true, id: incomingItem.item.scenarioData.interactionStepId})
            acceptItem(incomingItem.item.id)
            incomingItem.id && changeActiveInteraction({itemId: incomingItem.id, mediaType: 'voice'})
        }
        setIncomingItem(undefined)
    }

    const rejectCall = () => {
        setShowIncomingCall('none')
        if (incomingItem?.item && incomingItem.item.scenarioData) {
            sendAgentNotificationResult({callAccepted: false, id: incomingItem.item.scenarioData.interactionStepId})
            disconnectItem(incomingItem.item.id)
        }
        setIncomingItem(undefined)
    }

    const endAndAccept = () => {
        setShowIncomingCall('none')
        if (incomingItem?.item && incomingItem.item.scenarioData) {
            sendAgentNotificationResult({callAccepted: false, id: incomingItem.item.scenarioData.interactionStepId})
            acceptItem(incomingItem.item.id)
            const otherCall = itemsUI?.find(item => item.mediaType === 'voice' && item.id !== incomingItem.id)
            otherCall?.item && disconnectItem(otherCall?.item.id)
        }
        setIncomingItem(undefined)
    }

    const renderCallModal = () => {
        if (incomingItem && showIncomingCall) {
            return (
                <IncomingCallUI
                    showModal={showIncomingCall}
                    acceptCall={acceptCall}
                    rejectCall={rejectCall}
                    callItem={incomingItem}
                    endAndAccept={endAndAccept}
                />
            )
        } else {
            return null
        }
    }

    return (
        renderCallModal()
    )
}

interface IncomingCallProps {
    rejectCall: () => void
    acceptCall: () => void
    endAndAccept: () => void
    callItem: ItemUI
    showModal: 'multiple' | 'single' | 'none'
}

export const IncomingCallUI: React.FC<IncomingCallProps> = props => {
    const {rejectCall, acceptCall, callItem, showModal, endAndAccept} = props

    const [diffTime, setDiffTime] = useState<number>(0)
    const [receivingTimeDiff, setReceivingTimeDiff] = useState<number>(0)

    const isAnonymous = (phoneNumber: string) => phoneNumber?.toString().toLowerCase() === 'anonymous'

    const totalQueueTime = Number(callItem?.item?.scenarioData?.totalQueueTime)

    // useEffect(() => {
    //     const startTimeMillis = Date.now()

    //     const intervalId = BackgroundTimer.setInterval(() => {
    //         setDiffTime(Date.now() - startTimeMillis)
    //     }, 1000);

    //     (Platform.OS === 'ios' ? NativeModules.CallModule : NativeModules.IncomingCall)
    //     .getReceivingTime((receivingTime: number) => {
    //         setReceivingTimeDiff(Date.now() - receivingTime)
    //     })
    // }, [])

    const displayData = callItem?.item?.displayData

    const hasInitials =
        displayData?.firstName &&
        displayData.firstName.length > 0 &&
        displayData?.lastName &&
        displayData.lastName.length > 0

    const renderButtonsSingle = () => {
        return (
            <View style={styles.iconWrapper}>
                <TouchableOpacity onPress={rejectCall}>
                    <View>
                        <Image style={styles.icon} source={require('../images/reject_call.png')} />
                        <Text style={styles.btnText}>Decline</Text>
                    </View>
                </TouchableOpacity>
                <TouchableOpacity onPress={acceptCall}>
                    <View>
                        <Image style={styles.icon} source={require('../images/accept_call.png')} />
                        <Text style={styles.btnText}>Accept</Text>
                    </View>
                </TouchableOpacity>
            </View>
        )
    }

    const renderButtonsMultiple = () => {
        return (
            <View style={styles.iconWrapperMult}>
                <TouchableOpacity onPress={endAndAccept}>
                    <View style={styles.iconTextWrapper}>
                        <Image style={styles.iconMult} source={require('../images/end-and-accept.png')} />
                        <Text style={styles.btnText}>End & Accept</Text>
                    </View>
                </TouchableOpacity>
                <TouchableOpacity onPress={rejectCall}>
                    <View style={styles.iconTextWrapper}>
                        <Image style={styles.icon} source={require('../images/decline.png')} />
                        <Text style={styles.btnText}>Decline</Text>
                    </View>
                </TouchableOpacity>
                <TouchableOpacity onPress={acceptCall}>
                    <View style={styles.iconTextWrapper}>
                        <Image style={styles.iconMult} source={require('../images/hold-and-accept.png')} />
                        <Text style={styles.btnText}>Hold & Accept</Text>
                    </View>
                </TouchableOpacity>
            </View>
        )
    }

    return (
        <Modal
            statusBarTranslucent
            visible={showModal !== 'none'}
            animationType="fade"
        >
            <ImageBackground
                style={styles.bg}
                source={require('../images/bg.jpg')}
            >
                <SafeAreaView
                    forceInset={{top: 'always', bottom: 'always'}}
                    style={styles.wrapper}
                >

                    <View style={{height: 80}} />

                    <View
                        style={{
                            alignItems: 'center',
                            marginBottom: 30,
                        }}
                    >
                        <View style={styles.callerInitialsCircle}>
                            <LottieView
                                source={require('../images/call_animation.json')}
                                style={{
                                    width: 208,
                                    height: 208,
                                    position: 'absolute',
                                }}
                                autoPlay
                                loop
                            />

                            { !hasInitials && (
                                <Image
                                    source={require('../images/default_user.png')}
                                    style={{
                                        width: 64,
                                        height: 64,
                                    }}
                                />
                            )}

                            { hasInitials && displayData?.firstName && displayData?.lastName && (
                                <Text style={styles.callerInitials}>
                                    {displayData.firstName.charAt(0) + displayData.lastName.charAt(0)}
                                </Text>
                            )}
                        </View>
                    </View>

                    <View style={styles.infoWrapper}>
                        {callItem ? (
                            <>
                                <Text numberOfLines={2} style={styles.infoTextName}>
                                    {displayData?.displayName ? displayData?.displayName : 'Anonymous'}
                                </Text>
                                {(callItem.phoneNumber && !isAnonymous(callItem.phoneNumber))
                                    ? <Text style={styles.infoText}>{formatNumber(callItem.phoneNumber)}</Text>
                                    : <Text style={styles.infoText}>Unknown phone number</Text>
                                }
                                {callItem.item?.scenarioData?.serviceName
                                    ? <Text style={styles.infoText}>{callItem.item?.scenarioData?.serviceName}</Text>
                                    : null
                                }
                                {!callItem.userId && (
                                    <Text style={styles.infoTextWaitTime}>
                                        Wait time:
                                        {' '}
                                        {getDuration(String(totalQueueTime))}
                                    </Text>
                                )}
                            </>
                        ) : null}
                    </View>
                    {showModal === 'single' ? renderButtonsSingle() : renderButtonsMultiple()}
                    <View style={{height: 50}} />
                </SafeAreaView>
            </ImageBackground>
        </Modal>
    )
}

const styles = StyleSheet.create({
    wrapper: {
        flex: 1,
    },
    infoText: {
        fontSize: 24,
        color: 'rgba(255, 255, 255, 0.8)',
        textAlign: 'center',
        marginBottom: 15,
    },
    infoTextName: {
        ...mediumFontWeight,
        fontSize: 32,
        color: 'white',
        textAlign: 'center',
        marginBottom: 15,
    },
    infoTextWaitTime: {
        fontSize: 18,
        color: 'white',
        textAlign: 'center',
        marginBottom: 15,
    },
    infoWrapper: {
        flex: 1,
        marginHorizontal: 30,
    },
    bpWrapper: {
        flexDirection: 'row',
        justifyContent: 'center',
        paddingTop: 12,
    },
    bpLogo: {
        width: 20,
        height: 20,
        marginRight: 8,
    },
    bpText: {
        fontSize: 18,
        color: 'white',
    },
    iconTextWrapper: {
        justifyContent: 'center',
        alignItems: 'center',
    },
    iconWrapper: {
        flexDirection: 'row',
        justifyContent: 'space-around',
        marginBottom: 26,
    },
    iconWrapperMult: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        marginBottom: 26,
        marginHorizontal: 25,
    },
    icon: {
        height: 74,
        width: 74,
        marginBottom: 10,
    },
    iconMult: {
        height: 74,
        width: 71,
        marginBottom: 10,
    },
    bg: {
        width: '100%',
        height: '100%',
    },
    btnText: {
        textAlign: 'center',
        color: 'white',
        fontSize: 16,
    },
    callerInitialsCircle: {
        width: 128,
        height: 128,
        marginBottom: 30,

        backgroundColor: 'rgba(21, 214, 131, 0.4)',
        borderRadius: 64,

        alignItems: 'center',
        justifyContent: 'center',
    },
    callerInitials: {
        color: '#fff',
        fontSize: 65,
    },
})
