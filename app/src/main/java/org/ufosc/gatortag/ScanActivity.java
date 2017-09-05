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
        long id = 0;
        byte[] uidRaw = {};
        byte[] serialNumber = gaTag.getId();
        byte[] code = {};
        long time = 0;



        for(int i = 0; i < records.length; i++){
            if(records[i].toMimeType().equals("gatortag/name")){
                name = new String(records[i].getPayload());
            }else if(records[i].toMimeType().equals("gatortag/id")){
                id = byteArrayToUnsignedLong(records[i].getPayload());
                uidRaw = records[i].getPayload();
            }else if(records[i].toMimeType().equals("gatortag/code")){
                code = records[i].getPayload();
            }else if(records[i].toMimeType().equals("gatortag/time")){
                time = byteArrayToUnsignedLong(records[i].getPayload());
            }
        }

        byte[] hash = calcHash(serialNumber, uidRaw, code, username);

        displayResult(
                name,
                username,
                Long.toString(id),
                dumpByteArray(serialNumber),
                dumpByteArray(code),
                Long.toString(time),
                dumpByteArray(hash)
        );

    }

    /**
     * Brings up a view to show the information read from a tag.
     *
     * @param name the short-form name of the tag (gatortag/name)
     * @param username the name of the user that scanned the tag
     * @param id the UID read from the tag (gatortag/id)
     * @param serialNumber the serial number of the tag
     * @param code the secret code (gatortag/code)
     * @param time the placement time-stamp for the tag (gatortag/time)
     * @param hash the hash calculated from the serial number, uid, code, and username
     */
    private void displayResult(String name, String username, String id, String serialNumber, String code, String time, String hash){
        setContentView(R.layout.activity_result);

        final TextView nameField = (TextView) findViewById(R.id.shortNameTView);
        final TextView userNField = (TextView) findViewById(R.id.userNameTView);
        final TextView idField = (TextView) findViewById(R.id.UIDTView);
        final TextView serNumField = (TextView) findViewById(R.id.serNumTView);
        final TextView codeField = (TextView) findViewById(R.id.sCodeTView);
        final TextView timeField = (TextView) findViewById(R.id.pTimeTView);
        final TextView hashField = (TextView) findViewById(R.id.hashTView);

        nameField.setText(name);
        userNField.setText(username);
        idField.setText(id);
        serNumField.setText(serialNumber);
        codeField.setText(code);
        timeField.setText(time);
        hashField.setText(hash);
    }

    /**
     * Converts an array of bytes to an unsigned long (used to calculate UID and timestamp)
     *
     * @param toConvert the array of bytes to convert to a long
     * @return the long converted from toConvert.
     */
    private long byteArrayToUnsignedLong(byte[] toConvert){
        long converted = 0;
        for(int i = 0; i < toConvert.length; i++){
            converted += (long)(toConvert[i]) << 8*i;
        }

        return converted;
    }

    /**
     * Creates a formatted string from the given byte[]
     *
     * @param toDump the byte[] to convert
     * @return the String converted from toDump
     */
    private String dumpByteArray(byte[] toDump){
        String outString = "";
        for(int i = 0; i < toDump.length; i++){
            int posDig = toDump[i] + 128;
            outString += (posDig < 16 ? "0" : "") + Integer.toHexString(posDig) + " ";
            if((i+1) % 16 == 0 && i != toDump.length - 1){
                outString += "\n";
            }
        }

        return outString;
    }

    /**
     * Calculates the hash for a tag. Uses a SHA-256 algorithm.
     *
     * @param serialNumber the serial number of the tag
     * @param tagUid the uid stored on the tag
     * @param secretCode the secret code stored on the tag
     * @param userName the name of the user who scanned the tag
     * @return the hash of the tag
     */
    private byte[] calcHash(byte[] serialNumber, byte[] tagUid, byte[] secretCode, String userName){
        try {
            MessageDigest hashCalcer = MessageDigest.getInstance("SHA-256");
            hashCalcer.update(serialNumber);
            hashCalcer.update(tagUid);
            hashCalcer.update(secretCode);
            hashCalcer.update(userName.getBytes());

            return hashCalcer.digest();
        }catch (NoSuchAlgorithmException e){
            throw new RuntimeException("SHA-256 Algorithm Not Found.");
        }
    }

}
