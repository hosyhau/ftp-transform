package com.viettel.vtcc.dm.msisdn_encryption;

import org.apache.commons.lang.StringUtils;

public class SMSRecordEncryption extends EncryptionAbstract {
    @Override
    public void setUp(String key, String iv) {
        super.__setUp(key, iv);
    }

    @Override
    public String encrypt(String input) {
        String[] splitter = input.split(";", -1);

        splitter[0] = __encrypt(splitter[0], splitter[2]);
        splitter[1] = __encrypt(splitter[1], splitter[2]);
        splitter[3] = __encrypt(splitter[3], splitter[2]);
        splitter[4] = __encrypt(splitter[4], splitter[2]);
        splitter[5] = __encrypt(splitter[5], splitter[2]);

        return StringUtils.join(splitter, ";");
    }
}
