mod error;

use keyring::Entry;

#[derive(uniffi::Object)]
pub struct SecretService {
   service_name: String,
}

#[uniffi::export]
impl SecretService {
   #[uniffi::constructor]
   pub fn new(service_name: String) -> Self {
      SecretService { service_name }
   }

   pub fn set(&self, key: &str, value: &[u8]) -> Result<(), SecretServiceError> {
      let entry = get_entry(&self, key);
      entry.set_secret(value)?;
      Ok(())
   }

   pub fn get(&self, key: &str) -> Result<Vec<u8>, SecretServiceError> {
      let entry = get_entry(&self, key);
      let value = entry.get_secret()?;
      Ok(value)
   }

   pub fn del(&self, key: &str) -> Result<(), SecretServiceError> {
      let entry = get_entry(&self, key);
      entry.delete_credential()?;
      Ok(())
   }
}

fn get_entry(secret_service: &SecretService, key: &str) -> Entry {
   Entry::new(&secret_service.service_name, key).unwrap()
}

#[derive(Debug, thiserror::Error, uniffi::Error, PartialEq)]
pub enum SecretServiceError {
   #[error("Platform failure: {0}")]
   PlatformFailure(String),
   #[error("No storage access: {0}")]
   NoStorageAccess(String),
   #[error("No entry was found.")]
   NoEntry,
   #[error("Bad encoding: {0:?}")]
   BadEncoding(Vec<u8>),
   #[error("Too long (limit is {1}): {0}")]
   TooLong(String, u32),
   #[error("{0} is invalid: {1}")]
   Invalid(String, String),
   #[error("Ambiguous credential")]
   Ambiguous,
   #[error("Undefined error: {0:?}")]
   Undefined(String),
}

impl From<keyring::Error> for SecretServiceError {
   fn from(value: keyring::Error) -> Self {
      match value {
         keyring::Error::PlatformFailure(err) => {
            SecretServiceError::PlatformFailure(err.to_string())
         }
         keyring::Error::NoStorageAccess(err) => {
            SecretServiceError::NoStorageAccess(err.to_string())
         }
         keyring::Error::NoEntry => SecretServiceError::NoEntry,
         keyring::Error::BadEncoding(blob) => SecretServiceError::BadEncoding(blob),
         keyring::Error::TooLong(str, len) => SecretServiceError::TooLong(str, len),
         keyring::Error::Invalid(name, reason) => SecretServiceError::Invalid(name, reason),
         keyring::Error::Ambiguous(_) => SecretServiceError::Ambiguous,
         _ => SecretServiceError::Undefined(format!("{value:?}")),
      }
   }
}

uniffi::setup_scaffolding!();