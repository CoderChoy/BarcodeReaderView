package com.coderchoy.barcodereaderview.decode;

/**
 * Created by Leo
 * on 2017/3/14.
 */

class MessageId {

    //BarcodeReaderHandler
    public static final int MESSAGE_RESTART_PREVIEW = 0x01;
    public static final int MESSAGE_DECODE_SUCCEEDED = 0x02;
    public static final int MESSAGE_DECODE_FAILED = 0x03;

    //DecodeHandler
    public static final int MESSAGE_DECODE = 0x04;
    public static final int MESSAGE_QUIT = 0x05;
}
