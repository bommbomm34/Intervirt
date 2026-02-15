mod send;

#[derive(Debug, thiserror::Error, uniffi::Error)]
pub enum GenericError {
    #[error("{0}")]
    Generic(String),
}

#[derive(uniffi::Record)]
pub struct NativeAddress {
    pub host: String,
    pub port: u16,
}

uniffi::setup_scaffolding!();
