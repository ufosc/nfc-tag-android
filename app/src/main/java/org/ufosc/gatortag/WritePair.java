package org.ufosc.gatortag;

import android.nfc.NdefMessage;
import android.nfc.tech.Ndef;

final class WritePair {
    private final NdefMessage _toWrite;
    private final Ndef _tag;

    WritePair(NdefMessage toWrite, Ndef tag){
        _toWrite = toWrite;
        _tag = tag;
    }

    final NdefMessage getMessage(){
        return _toWrite;
    }

    final Ndef getTag(){
        return _tag;
    }
}
