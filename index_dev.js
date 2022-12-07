import {AppRegistry,Platform} from 'react-native'
import {Connector} from './src/Connector'
import {name as appName} from './app.json'
import 'react-native-url-polyfill/auto'
import 'proxy-polyfill'
import 'localstorage-polyfill'
import 'reflect.ownkeys/auto'
import 'custom-event-polyfill'

if(Platform.OS==='android'){require('@bpinc/ad-mob-notifications/Android/FromDeadHandler')}

AppRegistry.registerComponent(appName, () => initialProps => Connector({...initialProps, debug: true}))
