package org.ufosc.gatortag;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.TagTechnology;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class WriteActivity extends AppCompatActivity{
    private String _tagName;
    private boolean _isNameSet;
    private NfcAdapter nAdapt;
    private IntentFilter[] intentFiltersArray;
    private String[][] techListsArray;
    private PendingIntent nfcCatcher;

    @Override
    protected void onCreate(Bundle saveState){
        super.onCreate(saveState);
        _tagName = "";
        _isNameSet = false;

        setContentView(R.layout.activity_create);
        nfcCatcher = PendingIntent.getActivity(
                this,
                0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                0
        );
        IntentFilter nfcFilter = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);

        try{
            nfcFilter.addDataType("*/*");
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

    public void clickWrite(View v){
        final EditText nameInput = (EditText) findViewById(R.id.nameBox333);

        String nameResult = nameInput.getText().toString();
        if(nameResult.equals("")){
            Toast.makeText(getApplicationContext(), "Please enter a name.", Toast.LENGTH_SHORT).show();
            return;
        }

        _tagName = nameResult;
        _isNameSet = true;

        setContentView(R.layout.activity_scan);
    }

    @Override
    protected void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        if(intent != null && NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction()) && _isNameSet){
            Tag newTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            Ndef tagNdef = Ndef.get(newTag);

            GatorTag result = generateTag(_tagName, "Placeholder User", tagNdef);
        }
    }

    public GatorTag generateTag(String tagName, String writerUsername, Ndef tagToWrite){
        Random r = new Random();
        long genUid = r.nextLong();
        byte[] foundSNum = tagToWrite.getTag().getId();
        byte[] code = new byte[512];
        r.nextBytes(code);
        long timestamp = Calendar.getInstance().getTimeInMillis() / 1000L;

        GatorTag res = new GatorTag(writerUsername, genUid, foundSNum, code, tagName, timestamp);
        writeToTag(res, code, tagToWrite);

        return null;
    }

    private void writeToTag(GatorTag dataToWrite, byte[] secretCode, Ndef tag){
        NdefRecord[] records = new NdefRecord[4];
        records[0] = NdefRecord.createMime("gatortag/name", dataToWrite.getTagNameRaw());
        records[1] = NdefRecord.createMime("gatortag/id", dataToWrite.getUidRaw());
        records[2] = NdefRecord.createMime("gatortag/time", dataToWrite.getTimestampRaw());
        records[3] = NdefRecord.createMime("gatortag/code", secretCode);

        NdefMessage toWrite = new NdefMessage(records);
        WritePair writeOperation = new WritePair(toWrite, tag);

        new AsyncWriteTask(getApplicationContext()).doInBackground(writeOperation);

        displayResult(dataToWrite, secretCode, tag);
    }

    /**
     * Brings up a view to show the information read from a tag.
     * Yes, I just copied this from ScanActivity.java. I'll fix it later.
     *
     * @param tag the GatorTag object to display
     */
    private void displayResult(GatorTag tag, byte[] code, Ndef actualTag){
        setContentView(R.layout.activity_result);

        final TextView nameField = (TextView) findViewById(R.id.shortNameTView);
        final TextView userNField = (TextView) findViewById(R.id.userNameTView);
        final TextView idField = (TextView) findViewById(R.id.UIDTView);
        final TextView serNumField = (TextView) findViewById(R.id.serNumTView);
        final TextView codeField = (TextView) findViewById(R.id.sCodeTView);
        final TextView timeField = (TextView) findViewById(R.id.pTimeTView);
        final TextView hashField = (TextView) findViewById(R.id.hashTView);

        nameField.setText(tag.getTagName());
        userNField.setText(tag.getUserName());
        idField.setText(Long.toString(tag.getUid()));
        serNumField.setText(GatorTag.dumpByteArray(actualTag.getTag().getId()));
        codeField.setText(GatorTag.dumpByteArray(code));
        timeField.setText(tag.formatTimestamp());
        hashField.setText(GatorTag.dumpByteArray(tag.getHash()));
    }

}
