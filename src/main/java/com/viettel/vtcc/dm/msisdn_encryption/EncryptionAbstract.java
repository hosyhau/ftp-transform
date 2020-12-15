package com.viettel.vtcc.dm.msisdn_encryption;

import scala.Tuple4;
import vn.cyberspace.encryption.AESCustom;
import vn.cyberspace.up.utils.ConvertNumUtils;

abstract public class EncryptionAbstract {

    AESCustom aes;
    protected String format = "dd/MM/yyyy";

    /** Internal set up function */
    public void __setUp(String key, String iv) {
        aes = new AESCustom(key, iv);
    }

    /** Internal encryption function */
    String __encrypt(String input, String timeStamp) {
        String date = timeStamp.substring(0, 10);
        long logTime = ConvertNumUtils.getTimestampsFromString(date, format);
        Tuple4<String, String, String, String> tuple4 = ConvertNumUtils.standardizedNumber(input, 1,logTime, true);
        String encryptedMsisdn = aes.encrypt(tuple4._1());
        String countryCode = tuple4._2();
        String encryptedValue = countryCode + ":" + encryptedMsisdn;

        return encryptedValue;
    }

    abstract public void setUp(String key, String iv);
    abstract public String encrypt(String input);
}
