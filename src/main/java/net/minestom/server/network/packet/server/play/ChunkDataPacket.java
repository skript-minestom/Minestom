package net.minestom.server.network.packet.server.play;

import net.minestom.server.instance.ChunkLoader;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.data.ChunkData;
import net.minestom.server.network.packet.server.play.data.LightData;
import net.minestom.server.utils.chunk.ChunkSupplier;

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

    public static SendablePacket getEmpty(Instance instance, int chunkX, int chunkZ, ChunkSupplier chunkSupplier) {
        return chunkSupplier.createChunk(instance, chunkX, chunkZ).getFullDataPacket();
    }
}