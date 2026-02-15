use lettre::message::{Mailbox, header::ContentType};
use lettre::transport::smtp::authentication::Credentials;
use lettre::{Message, SmtpTransport, Transport};
use crate::{NativeAddress, GenericError};


#[derive(uniffi::Object)]
pub struct NativeMailSender {
    pub mailer: SmtpTransport,
}

#[derive(uniffi::Enum)]
pub enum MailBodyType {
    TEXT,
    HTML,
}

#[derive(uniffi::Record)]
pub struct NativeMail {
    pub sender: String,
    pub receiver: String,
    pub subject: String,
    pub body_type: MailBodyType,
    pub body: String,
}

#[derive(uniffi::Record)]
pub struct MailCredentials {
    pub username: String,
    pub password: String,
}

#[uniffi::export]
impl NativeMailSender {
    #[uniffi::constructor]
    pub fn new(
        host: NativeAddress,
        credentials: Option<MailCredentials>,
        _proxy: Option<NativeAddress>,
    ) -> Result<Self, GenericError> {
        let mailer_builder = SmtpTransport::relay(host.host.as_str());
        match mailer_builder {
            Ok(mailer_builder) => match credentials {
                Some(creds) => {
                    let creds = Credentials::new(creds.username, creds.password);
                    Ok(NativeMailSender {
                        mailer: mailer_builder.credentials(creds.to_owned()).build(),
                    })
                }
                None => Ok(NativeMailSender {
                    mailer: mailer_builder.build(),
                }),
            },
            Err(error) => Err(GenericError::Generic(error.to_string())),
        }
    }

    pub fn send_mail(&self, mail: &NativeMail) -> Result<(), GenericError> {
        let email = Message::builder()
            .from(Mailbox::new(
                Some(mail.sender.clone()),
                mail.sender.parse().unwrap(),
            ))
            .to(Mailbox::new(
                Some(mail.receiver.clone()),
                mail.receiver.parse().unwrap(),
            ))
            .subject(mail.subject.clone())
            .header(to_content_type(&mail.body_type))
            .body(mail.body.clone())
            .unwrap();
        self.mailer
            .send(&email)
            .map(|_| ())
            .map_err(|err| GenericError::Generic(err.to_string()))
    }
}

fn to_content_type(body_type: &MailBodyType) -> ContentType {
    match body_type {
        MailBodyType::TEXT => ContentType::TEXT_PLAIN,
        MailBodyType::HTML => ContentType::TEXT_HTML,
    }
}