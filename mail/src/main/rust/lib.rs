use lettre::message::{Mailbox, header::ContentType};
use lettre::transport::smtp::authentication::Credentials;
use lettre::{Message, SmtpTransport, Transport};

#[derive(uniffi::Record)]
struct Address {
    host: String,
    port: u16,
}

#[derive(uniffi::Object)]
struct NativeMailSender {
    mailer: SmtpTransport,
}

#[derive(uniffi::Enum)]
enum MailBodyType {
    TEXT,
    HTML,
}

#[derive(uniffi::Record)]
struct Mail {
    sender: String,
    receiver: String,
    subject: String,
    body_type: MailBodyType,
    body: String,
}

#[derive(uniffi::Record)]
struct SmtpCredentials {
    username: String,
    password: String,
}

#[uniffi::export]
impl NativeMailSender {
    #[uniffi::constructor]
    fn new(
        host: Address,
        credentials: Option<SmtpCredentials>,
        _proxy: Option<Address>,
    ) -> Result<Self, String> {
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
            Err(error) => Err(error.to_string()),
        }
    }

    fn send_mail(&self, mail: &Mail) -> Result<(), String> {
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
            .map_err(|err| err.to_string())
    }
}

fn to_content_type(body_type: &MailBodyType) -> ContentType {
    match body_type {
        MailBodyType::TEXT => ContentType::TEXT_PLAIN,
        MailBodyType::HTML => ContentType::TEXT_HTML,
    }
}

uniffi::setup_scaffolding!();
