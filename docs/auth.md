# Authentication

## Git authentication

Pimp my Trad uses basic authentication to push to/pull from repositories. The credentials for this user are specified in the `application.yml` file for the `pimp-my-trad-api` module. 

This means that whatever account is used to push/pull has to have *write access* to the repositories. It should be a service account that repository owners can add to their repository.

## API authentication

Currently, the chosen authentication method for the API is basic authentication + Active Directory.

Keycloak (an OIDC server) was previously used for this task: some code to authenticate with an OIDC server is still available in the main `pimp-my-trad-api` module.

If the auth system is changed, a new implementation of `sncf.oui.pmt.infrastructure.AuthenticationDetails` might be needed.