package com.viettel.vtcc.dm.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thuyenhx on 26/05/2017.
 */
public class MessageInfo {

    private List<String> data;

    public MessageInfo(List<String> data) {
        this.data = new ArrayList<>(data);
    }

    public List<String> getData() {
        return data;
    }

    public void setData(List<String> data) {
        this.data = data;
    }
}
