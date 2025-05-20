package controllers

import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import play.api.mvc.*
import javax.inject.Inject
import javax.inject.Singleton
import play.api.libs.json.{JsNull, JsObject, JsString, Json}
import play.api.libs.mailer.{Email, MailerClient}
import play.api.i18n.I18nSupport
@Singleton
final class AccountController @Inject()(
  controllerComponents: ControllerComponents,
//   val auth: AuthAction,
//   val userService: UserService,
//   val emailService: EmailService,
//   val config: AppConfig,
//   val openIDService: OpenIDService,
//   val orcidService: ORCIDService,
//   val ssoService: SSOService,
//   val oauth2Service: OAuth2Service,
//   val passwordResetTokenService: PasswordResetTokenService
) extends AbstractController(controllerComponents) with I18nSupport {



  def login(): Action[AnyContent] = Action { implicit request =>
    Ok("Login page")
  }

    def passwordLoginPost(): Action[AnyContent] = Action { implicit request =>
        // Handle password login
        Ok("Password login post")
    }

    def openIDCallback(): Action[AnyContent] = Action { implicit request =>
        // Handle OpenID callback
        Ok("OpenID callback")
    }

    def openIDLoginPost(isLogin: Boolean): Action[AnyContent] = Action { implicit request =>
        // Handle OpenID login post
        Ok("OpenID login post")
    }

    def oauth2(provider: String, code: Option[String], state: Option[String], isLogin: Boolean): Action[AnyContent] = Action { implicit request =>
        // Handle OAuth2 login
        Ok("OAuth2 login")
    }

    def oauth2Login(provider: String, code: Option[String], state: Option[String]): Action[AnyContent] = Action { implicit request =>
        // Handle OAuth2 login
        Ok("OAuth2 login")
    }

    def oauth2Register(provider: String, code: Option[String], state: Option[String]): Action[AnyContent] = Action { implicit request =>
        // Handle OAuth2 register
        Ok("OAuth2 register")
    }

    def connectORCIDPost(code: Option[String], state: Option[String]): Action[AnyContent] = Action { implicit request =>
        // Handle ORCID connection
        Ok("Connect ORCID post")
    }

    def disconnectORCIDPost(): Action[AnyContent] = Action { implicit request =>
        // Handle ORCID disconnection
        Ok("Disconnect ORCID post")
    }


    def logout(): Action[AnyContent] = Action { implicit request =>
        // Handle logout
        Ok("Logout")
    }

    def sso(client: String, sso: String, sig: String): Action[AnyContent] = Action { implicit request =>
        // Handle SSO
        Ok("SSO")
    }

    def signup(): Action[AnyContent] = Action { implicit request =>
        // Handle signup
        Ok("Signup page")
    }

    def signupPost(): Action[AnyContent] = Action { implicit request =>
        // Handle signup post
        Ok("Signup post")
    }

    def confirmEmailPost(): Action[AnyContent] = Action { implicit request =>
        // Handle email confirmation
        Ok("Confirm email post")
    }

    def confirmEmail(token: String): Action[AnyContent] = Action { implicit request =>
        // Handle email confirmation
        Ok("Confirm email")
    }

    def changeEmail(): Action[AnyContent] = Action { implicit request =>
        // Handle change email
        Ok("Change email page")
    }

    def changeEmailPost(): Action[AnyContent] = Action { implicit request =>
        // Handle change email post
        Ok("Change email post")
    }

    def changePassword(): Action[AnyContent] = Action { implicit request =>
        // Handle change password
        Ok("Change password page")
    }

    def changePasswordPost(): Action[AnyContent] = Action { implicit request =>
        // Handle change password post
        Ok("Change password post")
    }

    def forgotPassword(): Action[AnyContent] = Action { implicit request =>
        // Handle forgot password
        Ok("Forgot password page")
    }

    def forgotPasswordPost(): Action[AnyContent] = Action { implicit request =>
        // Handle forgot password post
        Ok("Forgot password post")
    }

    def resendVerificationPost(): Action[AnyContent] = Action { implicit request =>
        // Handle resend verification
        Ok("Resend verification post")
    }

    def passwordReminderSent(): Action[AnyContent] = Action { implicit request =>
        // Handle password reminder sent
        Ok("Password reminder sent")
    }

    def resetPassword(token: String): Action[AnyContent] = Action { implicit request =>
        // Handle reset password
        Ok("Reset password")
    }

    def resetPasswordPost(token: String): Action[AnyContent] = Action { implicit request =>
        // Handle reset password post
        Ok("Reset password post")
    }


}