package com.viettel.vtcc.dm.msisdn_encryption;

public class NullRecordEncryption extends EncryptionAbstract {
    @Override
    public void setUp(String key, String iv) {

    }

    @Override
    public String encrypt(String input) {
        return null;
    }
}
