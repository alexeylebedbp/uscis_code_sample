import {useEffect} from 'react'
import {Platform} from 'react-native'
import {NativeModules} from 'react-native'

export const MemoryUsageLogger: React.FC = () => {

    useEffect(() => {
        if (Platform.OS !== 'ios') return

        setInterval(() => {
            NativeModules.MemoryModule.getMemoryUsage((megabytes: string) => {
                megabytes && console.log(`[MemoryUsageLogger Memory Use: ${Math.ceil(parseFloat(megabytes))} MB]`)
            })
        }, 4000)
    }, [])

    return null
}
