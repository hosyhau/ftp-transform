package com.viettel.vtcc.dm.msisdn_encryption;

import org.apache.commons.lang.StringUtils;
import scala.Tuple4;
import vn.cyberspace.up.utils.ConvertNumUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CDRRecordEncryption extends EncryptionAbstract {

    @Override
    public void setUp(String key, String iv) {
        super.__setUp(key, iv);
        this.format = "yyyy/MM/dd";
    }

    @Override
    public String encrypt(String input) {
        String delimiter = ",";
        String[] splitter = input.split(delimiter, -1);
        String date = splitter[8].substring(0, 10);
        long logTime = ConvertNumUtils.getTimestampsFromString(date, format);
        Tuple4<String, String, String, String> tuple2 = ConvertNumUtils.standardizedNumber(splitter[2], 1, logTime, true);
        Tuple4<String, String, String, String> tuple5 = ConvertNumUtils.standardizedNumber(splitter[5], 1, logTime, true);
        splitter[2] = __encrypt(splitter[2], splitter[8]);
        splitter[5] = __encrypt(splitter[5], splitter[8]);
        splitter[28] = __encrypt(splitter[28], splitter[8]);
        splitter[29] = __encrypt(splitter[29], splitter[8]);
        List<String> stringList = convertArrayToList(splitter);
        stringList.add(tuple2._3());
        stringList.add(tuple5._3());

        return StringUtils.join(stringList, delimiter);
    }

    public CDRRecordEncryption() {
    }

    private static List<String> convertArrayToList(String arr[])
    {
        List<String> list = new ArrayList<>();
        Collections.addAll(list, arr);
        return list;
    }
}
