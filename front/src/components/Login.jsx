import * as React from 'react';
import {Modal, ModalHeader, ModalFooter, ModalBody, Button, FormGroup, Input, Label, FormFeedback} from 'reactstrap';

var Error;
(function (Error) {
    Error["EMPTY"] = "Veuillez renseigner un nom d'utilisateur et un mot de passe";
    Error["INVALID"] = "Identifiant ou mot de passe invalide";
})(Error || (Error = {}));

class _Login extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            username: '',
            password: '',
            error: null
        };
    }

    login() {
        const {username, password} = this.state;
        if (username.length === 0 || password.length === 0) {
            return () => this.setState(({error: Error.EMPTY}));
        }
        else {
            return () => this.props.login(username, password)
                .then(success => this.setState({error: success ? null : Error.INVALID}));
        }
    }

    updateState(field) {
        return (event) => this.setState({
            [field]: event.currentTarget.value,
            error: null
        });
    }

    render() {
        return (<Modal fade={true} isOpen={!this.props.auth}>
            <ModalHeader>Connexion requise</ModalHeader>
            <ModalBody>
                <FormGroup>
                    <Label>Identifiant AD</Label>
                    <Input type="text" value={this.state.username} onChange={this.updateState('username')}
                           placeholder="prenom_nom"/>
                </FormGroup>
                <FormGroup>
                    <Label>Mot de passe</Label>
                    <Input invalid={this.state.error !== null} value={this.state.password}
                           onChange={this.updateState('password')} type="password"/>
                    <FormFeedback>{this.state.error}</FormFeedback>
                </FormGroup>
            </ModalBody>
            <ModalFooter>
                <Button color="primary" onClick={this.login()}>Connexion</Button>
            </ModalFooter>
        </Modal>);
    }
}

export const Login = _Login;
