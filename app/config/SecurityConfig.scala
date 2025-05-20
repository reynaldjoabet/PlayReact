package config


final case class SecurityConfig(
    useOauth: Boolean,
    securityType: String,
    clientID: String,
    secret: String,
    discoveryURI: String,
    oidcScope: String,
    oidcEmailAttribute: String,
    showJWTInfoOnLogin: Boolean,
    ssh2Enabled: Boolean
)

object SecurityConfig {
type Scopes="openid"|"email"|"profile"|"groups"|"offline_access"

}