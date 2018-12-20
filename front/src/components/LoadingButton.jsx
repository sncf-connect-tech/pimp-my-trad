import {connect} from 'react-redux';
import * as React from 'react';
import {Button} from 'reactstrap';

const withLoading = connect((state) => ({loading: state.main.loading > 0}));
export const LoadingButton = withLoading((props) => {
    return <Button {...props} disabled={props.loading}/>;
});
