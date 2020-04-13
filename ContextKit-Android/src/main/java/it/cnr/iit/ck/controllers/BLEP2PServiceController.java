package it.cnr.iit.ck.controllers;

import java.nio.ByteBuffer;
import java.util.UUID;

public abstract class BLEP2PServiceController {

    /**
     * Convert a 16 bytes array to UUID
     * @param bytes
     * @throws java.nio.BufferUnderflowException if array size < 16
     * @return an UUID
     */
    protected static UUID asUuid(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long firstLong = bb.getLong();
        long secondLong = bb.getLong();
        return new UUID(firstLong, secondLong);
    }

    protected static byte[] asBytes(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }

    public abstract void start();
    public abstract void stop();
}
