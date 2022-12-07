import React, {useState} from 'react'
import {App} from './App'
import {AppLocked} from './AppLocked'
import {LockedNotificationModeDetector} from './LockedModeDetection'

interface AppReceiverProperties {
    debug?: boolean
}

export const AppReceiver: React.FC<AppReceiverProperties> = props => {
    const [lockedNotificationAppMode, setLockedNotificationAppMode] = useState(false)

    return (
        <>  
            <LockedNotificationModeDetector setLockedNotificationAppMode={setLockedNotificationAppMode}/>
            {!lockedNotificationAppMode
                ? <App {...props} />
                : <AppLocked />}
        </>
    )
}
