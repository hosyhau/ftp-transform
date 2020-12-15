package com.viettel.vtcc.dm.msisdn_encryption;

import org.apache.commons.lang.StringUtils;

public class VoiceRecordEncryption extends EncryptionAbstract {
    @Override
    public void setUp(String key, String iv) {
        super.__setUp(key, iv);
    }

    @Override
    public String encrypt(String input) {
        String[] splitter = input.split(";", -1);

        splitter[0] = __encrypt(splitter[0], splitter[2]);
        splitter[1] = __encrypt(splitter[1], splitter[2]);
        splitter[6] = __encrypt(splitter[6], splitter[2]);
        splitter[7] = __encrypt(splitter[7], splitter[2]);
        return StringUtils.join(splitter, ";");
    }
}
