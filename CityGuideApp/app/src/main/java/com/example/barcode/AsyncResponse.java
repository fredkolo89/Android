package com.example.barcode;

import java.util.List;

public interface AsyncResponse {

    void processFinish(BarcodeItem output);

    void processFinish(List<BarcodeItem> output);
}
