package org.ufosc.gatortag;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    NfcAdapter nAdapt;
    IntentFilter[] intentFiltersArray;
    String[][] techListsArray;
    PendingIntent nfcCatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nfcCatcher = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter nfcFilter = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);

        try{
            nfcFilter.addDataType("gatortag/code");
            nfcFilter.addDataType("gatortag/id");
            nfcFilter.addDataType("gatortag/time");
            nfcFilter.addDataType("gatortag/name");
        }catch(IntentFilter.MalformedMimeTypeException e){
            throw new RuntimeException("Error creating filters for NFC catcher.");
        }

        intentFiltersArray = new IntentFilter[] {nfcFilter};
        techListsArray = new String[][] { new String[] { Ndef.class.getName(), NdefFormatable.class.getName() } };

        nAdapt = NfcAdapter.getDefaultAdapter(this);
    }

    @Override
    protected void onResume(){
        super.onResume();
        nAdapt.enableForegroundDispatch(this, nfcCatcher, intentFiltersArray, techListsArray);
    }

    @Override
    protected void onPause(){
        super.onPause();
        nAdapt.disableForegroundDispatch(this);
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null && NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Toast.makeText(getApplicationContext(), "Press the \"Scan\" button to scan.", Toast.LENGTH_SHORT).show();
        }
    }

    protected void onActivityResult(int code1, int code2, Intent data){

    }


    public void click(View v){
        startActivityForResult(new Intent(this, ScanActivity.class), 0);
    }

    public void clickWriteButton(View v){
        //startActivityForResult(new Intent(this, WriteActivity.class), 0);
    }

}
