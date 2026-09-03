package me.kimyeonsup.home.config.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import java.io.IOException;
import java.util.List;

public class NullToEmptySerializerModifier extends BeanSerializerModifier {

    @Override
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription beanDesc,
                                                     List<BeanPropertyWriter> beanProperties) {
        // 각 필드를 검사하여 null 값을 처리하도록 수정
        for (BeanPropertyWriter writer : beanProperties) {
            if (writer.getType().isTypeOrSubTypeOf(String.class)) {
                writer.assignNullSerializer(new JsonSerializer<Object>() {
                    @Override
                    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers)
                            throws IOException {
                        gen.writeString("");
                    }
                });
            }
        }
        return super.changeProperties(config, beanDesc, beanProperties);
    }
}