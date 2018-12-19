import { authHeaders, rejectHttpErrors } from '../services/commons';
export var AuthAction;
(function (AuthAction) {
    AuthAction["SET_AUTH_STATUS"] = "SET_AUTH_STATUS";
})(AuthAction || (AuthAction = {}));
export function checkAuth(user, pass) {
    return dispatch => {
        localStorage.setItem('pmt', btoa(`${user}:${pass}`));
        return authHeaders()
            .then(headers => fetch('/check', { headers: { ...headers, 'cache-control': 'no-cache' } }))
            .then(rejectHttpErrors)
            .then(() => {
            dispatch(setAuthSuccessful());
            return Promise.resolve(true);
        })
            .catch(() => {
            localStorage.removeItem('pmt');
            dispatch(setAuthRequired());
            return Promise.resolve(false);
        });
    };
}
export function setAuthRequired() {
    return {
        type: AuthAction.SET_AUTH_STATUS,
        auth: false
    };
}
export function setAuthSuccessful() {
    return {
        type: AuthAction.SET_AUTH_STATUS,
        auth: true
    };
}
