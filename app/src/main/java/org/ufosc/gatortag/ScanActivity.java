package org.ufosc.gatortag;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ScanActivity extends AppCompatActivity {
    NfcAdapter nAdapt;
    IntentFilter[] intentFiltersArray;
    String[][] techListsArray;
    PendingIntent nfcCatcher;

    @Override
    protected void onCreate(Bundle saveState){
        super.onCreate(saveState);
        setContentView(R.layout.activity_scan);
        nfcCatcher = PendingIntent.getActivity(
                this,
                0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                0
        );
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
        techListsArray = new String[][] {
                new String[] {
                        Ndef.class.getName(), NdefFormatable.class.getName()
                }
        };

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

    @Override
    protected void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        if(intent != null && NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())){
            Tag newTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            Parcelable[] rawMessages =
                    intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

            if(rawMessages == null){
                return;
            }

            NdefMessage[] gaMessages = new NdefMessage[rawMessages.length];

            for(int i = 0; i < rawMessages.length; i++){
                gaMessages[i] = (NdefMessage) rawMessages[i];
            }

            for(int i = 0; i < gaMessages.length; i++){
                processGatortag(newTag, gaMessages[i]);
            }
        }
    }

    /**
     * Processes the contents of a tag, and displays a result screen.
     *
     * @param gaTag the tag which was scanned
     * @param gaData the message that was retrieved from the tag
     */
    private void processGatortag(Tag gaTag, NdefMessage gaData){
        NdefRecord[] records = gaData.getRecords();
        if(records == null){
            return;
        }

        String name = "Error: No Name Field Found";
        String username = "Placeholder User";
        byte[] uidRaw = {};
        byte[] serialNumber = gaTag.getId();
        byte[] code = {};
        byte[] timeRaw = {};

        for(int i = 0; i < records.length; i++){
            if(records[i].toMimeType().equals("gatortag/name")){
                name = new String(records[i].getPayload());
            }else if(records[i].toMimeType().equals("gatortag/id")){
                uidRaw = records[i].getPayload();
            }else if(records[i].toMimeType().equals("gatortag/code")){
                code = records[i].getPayload();
            }else if(records[i].toMimeType().equals("gatortag/time")){
                timeRaw = records[i].getPayload();
            }
        }

        GatorTag tag = new GatorTag(username.getBytes(), uidRaw, serialNumber, code, name.getBytes(), timeRaw);

        displayResult(tag);

    }

    /**
     * Brings up a view to show the information read from a tag.
     *
     * @param tag the GatorTag object to display
     */
    private void displayResult(GatorTag tag){
        setContentView(R.layout.activity_result);

        final TextView nameField = (TextView) findViewById(R.id.shortNameTView);
        final TextView userNField = (TextView) findViewById(R.id.userNameTView);
        final TextView idField = (TextView) findViewById(R.id.UIDTView);
        // final TextView serNumField = (TextView) findViewById(R.id.serNumTView);
        // final TextView codeField = (TextView) findViewById(R.id.sCodeTView);
        final TextView timeField = (TextView) findViewById(R.id.pTimeTView);
        final TextView hashField = (TextView) findViewById(R.id.hashTView);

        nameField.setText(tag.getTagName());
        userNField.setText(tag.getUserName());
        idField.setText(Long.toString(tag.getUid()));
        timeField.setText(Long.toString(tag.getTimestamp()));
        hashField.setText(GatorTag.dumpByteArray(tag.getHash()));
    }

}
