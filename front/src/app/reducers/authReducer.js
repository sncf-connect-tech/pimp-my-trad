import { AuthAction } from '../actions/AuthAction';
export const authStateDefaults = {
    auth: (localStorage.getItem('pmt') || '').length > 0
};
export const authReducer = (state, action) => {
    state = { ...authStateDefaults, ...state };
    switch (action.type) {
        case AuthAction.SET_AUTH_STATUS:
            return { ...state, auth: action.auth };
        default:
            return state;
    }
};
