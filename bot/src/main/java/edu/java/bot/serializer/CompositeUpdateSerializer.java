package edu.java.bot.serializer;

import edu.java.bot.model.dto.request.LinkUpdateRequest;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.Serializer;

public class CompositeUpdateSerializer implements Serializer<Object> {

    private final Serializer<LinkUpdateRequest> linkUpdateRequestSerializer = new LinkUpdateRequestSerializer();
    private final ByteArraySerializer byteArraySerializer = new ByteArraySerializer();

    @Override
    public byte[] serialize(String topic, Object data) {
        if (data instanceof LinkUpdateRequest) {
            return linkUpdateRequestSerializer.serialize(topic, (LinkUpdateRequest) data);
        }
        return byteArraySerializer.serialize(topic, (byte[]) data);
    }
}
