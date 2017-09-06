package org.ufosc.gatortag;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class GatorTag {
    private final byte[] _scannerUserNameRaw;
    private final byte[] _uidRaw;
    private final byte[] _serialNumber;
    private final byte[] _secretCode;
    private final byte[] _shortNameRaw;
    private final byte[] _createTimestampRaw;

    public GatorTag(String userName, long uid, byte[] serialNumber, byte[] secretCode, String shortName, long createTimestamp){
        _scannerUserNameRaw = userName.getBytes();
        _uidRaw = encodeLong(uid);
        _serialNumber = serialNumber;
        _secretCode = secretCode;
        _shortNameRaw = shortName.getBytes();
        _createTimestampRaw = encodeLong(createTimestamp);
    }

    public GatorTag(byte[] userNameRaw, byte[] uidRaw, byte[] serialNumber, byte[] secretCode, byte[] shortNameRaw, byte[] createTimestampRaw){
        _scannerUserNameRaw = userNameRaw;
        _uidRaw = uidRaw;
        _serialNumber = serialNumber;
        _secretCode = secretCode;
        _shortNameRaw = shortNameRaw;
        _createTimestampRaw = createTimestampRaw;
    }

    public String getUserName(){
        return new String(_scannerUserNameRaw);
    }



    /**
     * Calculates the hash of the tag, calculated from the serial number, uid, secret code, and
     * the username that scanned the tag.
     *
     * @return the hash of the tag
     */
    public final byte[] calculateHash(){
        try {
            MessageDigest hashCalcer = MessageDigest.getInstance("SHA-256");
            hashCalcer.update(_serialNumber);
            hashCalcer.update(_uidRaw);
            hashCalcer.update(_secretCode);
            hashCalcer.update(_scannerUserNameRaw);

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
        byte[] encoded = new byte[longLen];
        for(int i = 0; i < encoded.length; i++){
            encoded[i] = (byte)((l >> longLen * i) % 256);
        }

        return encoded;
    }

    /**
     * Converts an array of bytes to a long int
     *
     * @param b the array of bytes to convert to a long
     * @return the long converted from b
     */
    private final long decodeLong(byte[] b){
        long converted = 0;
        for(int i = 0; i < b.length; i++){
            converted += (long)(b[i]) << 8*i;
        }

        return converted;
    }
}
