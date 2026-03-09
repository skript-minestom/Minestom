package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.data.ChunkData;
import net.minestom.server.network.packet.server.play.data.LightData;

import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static net.minestom.server.network.NetworkBuffer.INT;

public record ChunkDataPacket(
        int chunkX, int chunkZ,
        ChunkData chunkData,
        LightData lightData
) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<ChunkDataPacket> SERIALIZER = NetworkBufferTemplate.template(
            INT, ChunkDataPacket::chunkX,
            INT, ChunkDataPacket::chunkZ,
            ChunkData.NETWORK_TYPE, ChunkDataPacket::chunkData,
            LightData.NETWORK_TYPE, ChunkDataPacket::lightData,
            ChunkDataPacket::new
    );
    private static final ChunkData EMPTY_CHUNK_DATA = new ChunkData(Map.of(), new byte[0], Map.of());
    private static final LightData EMPTY_LIGHT_DATA = new LightData(new BitSet(), new BitSet(), new BitSet(), new BitSet(),
            List.of(), List.of());

    public static ChunkDataPacket getEmpty(int chunkX, int chunkZ) {
        return new ChunkDataPacket(chunkX, chunkZ, EMPTY_CHUNK_DATA, EMPTY_LIGHT_DATA);
    }
}