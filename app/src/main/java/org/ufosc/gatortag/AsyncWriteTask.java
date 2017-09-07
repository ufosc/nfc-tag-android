package org.ufosc.gatortag;

import android.content.Context;
import android.nfc.FormatException;
import android.nfc.TagLostException;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.IOException;

class AsyncWriteTask extends AsyncTask<WritePair, Void, Void> {
    private Context _context;
    private final int ERR_TAG_LOST = 1;
    private final int ERR_IOEXCEPT = 2;
    private final int ERR_FORMATEX = 3;
    private int error = 0;

    AsyncWriteTask(Context c){
        _context = c;
    }

    @Override
    protected Void doInBackground(WritePair... toWrite){
        if(toWrite.length != 1){
            return null;
        }
        try{
            toWrite[0].getTag().connect();
            toWrite[0].getTag().writeNdefMessage(toWrite[0].getMessage());
        }catch(TagLostException tl){
            error = ERR_TAG_LOST;
        }catch(IOException io){
            error = ERR_IOEXCEPT;
        }catch(FormatException fe){
            error = ERR_FORMATEX;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void v){
        switch(error){
            case 0:
                Toast.makeText(_context, "Success", Toast.LENGTH_SHORT).show();
                break;
            case ERR_TAG_LOST:
                Toast.makeText(_context, "Tag out of range", Toast.LENGTH_SHORT).show();
                break;
            case ERR_IOEXCEPT:
                Toast.makeText(_context, "IO Exception", Toast.LENGTH_SHORT).show();
                break;
            case ERR_FORMATEX:
                Toast.makeText(_context, "Tag Format Exception", Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(_context, "Unknown Error", Toast.LENGTH_SHORT).show();
        }
    }
}
