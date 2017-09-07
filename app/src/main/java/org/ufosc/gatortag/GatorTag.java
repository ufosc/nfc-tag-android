package org.ufosc.gatortag;


import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

final class GatorTag {
    private final byte[] _scannerUserNameRaw;
    private final byte[] _uidRaw;
    private final byte[] _shortNameRaw;
    private final byte[] _createTimestampRaw;
    private final byte[] _hash;

    /**
     * Constructs a GatorTag
     * @param userName the name of the user that scanned / created the tag
     * @param uid the UID of the tag (NOT the serial number)
     * @param serialNumber the serial number of the tag (NOT the UID)
     * @param secretCode the random "secret code" byte array of the tag
     * @param shortName the short name stored on the tag
     * @param createTimestamp the UNIX timestamp of the tag's creation
     */
    GatorTag(String userName, long uid, byte[] serialNumber, byte[] secretCode, String shortName, long createTimestamp){
        _scannerUserNameRaw = userName.getBytes();
        _uidRaw = encodeLong(uid);
        _shortNameRaw = shortName.getBytes();
        _createTimestampRaw = encodeLong(createTimestamp);

        _hash = calculateHash(serialNumber, _uidRaw, secretCode, _scannerUserNameRaw);
    }

    /**
     * Constructs a GatorTag from raw values
     * @param userNameRaw the name of the user that scanned / created the tag
     * @param uidRaw the UID of the tag (NOT the serial number)
     * @param serialNumber the serial number of the tag (NOT the UID)
     * @param secretCode the random "secret code" byte array of the tag
     * @param shortNameRaw the short name stored on the tag
     * @param createTimestampRaw the UNIX timestamp of the tag's creation
     */
    GatorTag(byte[] userNameRaw, byte[] uidRaw, byte[] serialNumber, byte[] secretCode, byte[] shortNameRaw, byte[] createTimestampRaw){
        _scannerUserNameRaw = userNameRaw;
        _uidRaw = uidRaw;
        _shortNameRaw = shortNameRaw;
        _createTimestampRaw = createTimestampRaw;

        _hash = calculateHash(serialNumber, uidRaw, secretCode, userNameRaw);
    }

    String getUserName(){
        return new String(_scannerUserNameRaw);
    }

    byte[] getUserNameRaw(){
        return _scannerUserNameRaw;
    }

    String getTagName(){
        return new String(_shortNameRaw);
    }

    byte[] getTagNameRaw(){
        return _shortNameRaw;
    }

    long getUid(){
        return decodeLong(_uidRaw);
    }

    byte[] getUidRaw(){
        return _uidRaw;
    }

    long getTimestamp(){
        return decodeLong(_createTimestampRaw);
    }

    byte[] getTimestampRaw(){
        return _createTimestampRaw;
    }

    byte[] getHash(){
        return _hash;
    }



    /**
     * Calculates the hash of the tag, calculated from the serial number, uid, secret code, and
     * the username that scanned the tag.
     *
     * @return the hash of the tag
     */
    private final byte[] calculateHash(byte[] serialNumber, byte[] uidRaw, byte[] secretCode, byte[] userNameRaw){
        try {
            MessageDigest hashCalcer = MessageDigest.getInstance("SHA-256");
            hashCalcer.update(serialNumber);
            hashCalcer.update(uidRaw);
            hashCalcer.update(secretCode);
            hashCalcer.update(userNameRaw);

            return hashCalcer.digest();
        }catch (NoSuchAlgorithmException e){
            throw new RuntimeException("SHA-256 Algorithm Not Found.");
        }
    }

    /**
     * Converts a long int into an array of bytes
     * @param l the long to convert to an array of bytes
     * @return the array of bytes converted from l
     */
    private final byte[] encodeLong(long l){
        int longLen = Long.SIZE/Byte.SIZE;
        ByteBuffer buf = ByteBuffer.allocate(longLen);
        buf.putLong(l);
        return buf.array();
    }

    /**
     * Converts an array of bytes to a long int
     *
     * @param b the array of bytes to convert to a long
     * @return the long converted from b
     */
    private final long decodeLong(byte[] b){
        int longLen = Long.SIZE/Byte.SIZE;
        ByteBuffer buf = ByteBuffer.allocate(longLen);
        buf.put(b);
        buf.flip();
        return buf.getLong();
    }

    final String formatTimestamp(){
        Date d = new Date(decodeLong(_createTimestampRaw) * 1000L);
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(d);
    }

    /**
     * Creates a formatted string from the given byte[]
     *
     * @param toDump the byte[] to convert
     * @return the String converted from toDump
     */
    static String dumpByteArray(byte[] toDump){
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
}
