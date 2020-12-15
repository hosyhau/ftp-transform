package com.viettel.vtcc.dm.msisdn_encryption;

public class EncryptionFactory {
    public static final String MSC_VOICE = "msc_voice";
    public static final String MSC_SMS = "msc_sms";
    public static final String CDR_SMS = "cdr_sms";
    public static final String CDR_VOICE = "cdr_voice";

    public static EncryptionAbstract factory(String type) {
        EncryptionAbstract encryption = null;
        switch (type) {
            case MSC_VOICE:
                encryption = new VoiceRecordEncryption();
                break;
            case MSC_SMS:
                encryption = new SMSRecordEncryption();
                break;
            case CDR_SMS:
                encryption = new CDRRecordEncryption();
                break;
            default:
                encryption = new NullRecordEncryption();
        }

        return encryption;
    }
}
