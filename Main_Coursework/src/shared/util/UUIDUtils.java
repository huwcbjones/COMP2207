package shared.util;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;

/**
 * Utility Class for converting UUID to other formats and back
 *
 * @author Huw Jones
 * @since 10/04/2016
 */
public class UUIDUtils {

    /**
     * Convert a byte array to a UUID
     * @param bytes Bytes to convert
     * @return UUID
     */
    public static UUID BytesToUUID(byte[] bytes){
        if(bytes.length != 16){
            return null;
        }
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.put(bytes, 0, bytes.length);
        buffer.flip();

        long msb = buffer.getLong(0);
        long lsb = buffer.getLong(8);

        return new UUID(msb, lsb);
    }

    /**
     * Convert a UUID to a byte array
     * @param uuid UUID to convert
     * @return Byte Array
     */
    public static byte[] UUIDToBytes(UUID uuid){
        if(uuid == null){
            return new byte[0];
        }
        ByteBuffer buffer = ByteBuffer.allocate(16);
        return buffer.putLong(uuid.getMostSignificantBits()).putLong(uuid.getLeastSignificantBits()).array();
    }

    /**
     * Convert a UUID to a Base64 String
     * @param uuid UUID to convert
     * @return Base64 String
     */
    public static String UUIDToBase64String(UUID uuid){
        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(UUIDToBytes(uuid));
    }

    /**
     * Convert a Base64 String to a UUID
     * @param uuid Base64 String to convert
     * @return UUID
     */
    public static UUID Base64StringToUUID(String uuid){
        // All Base64 encoded UUIDs are 24 chars long because a UUID is 128 bits (16 bytes),
        // and Base64 encoding works using bytes.
        if(uuid.length() != 24) return null;
        Base64.Decoder decoder = Base64.getDecoder();
        return UUIDUtils.BytesToUUID(decoder.decode(uuid));
    }

}