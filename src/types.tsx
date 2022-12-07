export type DefaultPage = 'Contacts' | 'Recents' | 'InteractionState' | undefined

export interface DefaultPageHandlerProps {
    defaultPage: DefaultPage
    setDefaultPage: (defaultPage: DefaultPage) => void
}
