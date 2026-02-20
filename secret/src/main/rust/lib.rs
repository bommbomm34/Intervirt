use keyring::Entry;

#[derive(Debug, thiserror::Error, uniffi::Error)]
pub enum GenericError {
   #[error("{0}")]
   Generic(String)
}

#[uniffi::export]
pub fn load_key(id: String) -> Result<Vec<u8>, GenericError> {
    let entry = get_entry(&id).map_err(|err|to_generic_error(err))?;
    entry.get_secret().map_err(|err| to_generic_error(err.to_string()))
}

#[uniffi::export]
pub fn save_key(id: String, key: Vec<u8>) -> Result<(), GenericError> {
    let entry = get_entry(&id).map_err(|err|to_generic_error(err))?;
    entry.set_secret(&key).map_err(|err| to_generic_error(err.to_string()))?;
    Ok(())
}


#[uniffi::export]
pub fn delete_key(id: String) -> Result<(), GenericError> {
   let entry = get_entry(&id).map_err(|err|to_generic_error(err))?;
   entry.delete_credential().map_err(|err| to_generic_error(err.to_string()))?;
   Ok(())
}

fn get_entry(id: &str) -> Result<Entry, String> { Entry::new(&id, "master_key").map_err(|err| err.to_string()) }

fn to_generic_error(str: String) -> GenericError { GenericError::Generic(str) }

uniffi::setup_scaffolding!();
