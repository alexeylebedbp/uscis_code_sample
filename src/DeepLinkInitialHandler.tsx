import React, {useEffect, useContext} from 'react'
import {Linking} from 'react-native'
import {InitialDeepLinkContext} from '@bpinc/ad-mob-context-providers'

export const DeepLinkInitialHandler: React.FC = () => {
    const {setInitialDeepLink} = useContext(InitialDeepLinkContext)
    
    useEffect(() => {
        const getUrlAsync = async () => {
            const initialUrl = await Linking.getInitialURL()
            if (!initialUrl) return
            
            console.log('DeepLinkHandler: App launched from Deep Link initialUrl:', initialUrl)
            setInitialDeepLink(initialUrl)
        }

        getUrlAsync()
    }, [])

    return null
}
