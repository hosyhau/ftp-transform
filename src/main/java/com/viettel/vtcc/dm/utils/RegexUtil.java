package com.viettel.vtcc.dm.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by thuyenhx on 25/05/2017.
 */
public class RegexUtil {

    public static boolean dateFileMatcher(String time, String name) {
        return dateFileMatcher(null, time, name);
    }

    public static boolean dateFileMatcher(String prefix, String time, String name) {
        StringBuilder sb = new StringBuilder();
        if (prefix != null) {
            sb.append("(");
            sb.append(prefix);
            sb.append("[_,\\-,+,#]*)(");
            sb.append(time);
            sb.append(")(.*)");
        }else {
            sb.append("(.*)(" + time + ")(.*)");
        }
        Pattern p = Pattern.compile(sb.toString());
        Matcher m = p.matcher(name);
        if (m.find()) {
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        System.out.println(RegexUtil.dateFileMatcher("F_ACCOUNT", "20170524", "F_ACCOUNT_20170523.txt"));
        System.out.println(RegexUtil.dateFileMatcher("F_ACCOUNT", "20170524", "F_ACCOUNT_20170524.txt"));
    }
}
