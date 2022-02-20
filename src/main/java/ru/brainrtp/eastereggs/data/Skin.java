package ru.brainrtp.eastereggs.data;

import lombok.Data;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

@Data
public class Skin {
    // TODO: (12.02 18:40) Добавить всякие проверки для переменных как в аналогичных сериалайзерах

    private String value = "eyJ0aW1lc3RhbXAiOjE1MjYyMDQxODkyNDYsInByb2ZpbGVJZCI6IjdjZjc2MTFkYmY2YjQxOWRiNjlkMmQzY2Q4NzUxZjRjIiwicHJvZmlsZU5hbWUiOiJrYXJldGg5OTkiLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzg4M2IxMjhkZjgxNmY4NWYwMTY3YzA1NWM0NTRmZjIxYmU5MDFmMjBjY2NkMDI1YTQzMDE2OWVhMzkwNjdkNzgifX19";
    private String signature = "qwMMfzPkJh9I9s1UufuJbUJddnivm+Y7mybMVHO0IesP1DOqMP2egZejHPO54MoSVGnU0lZEC3RJ0S2EX2e30ATKfOuBkOAkPLci2rjw405ctfYWYQuzWe9ZF2sOo/JtUwduPTy95qXALu+YAe6WXLqpQ10+jkorGh7d0oRQ/l/MmkG41sHDL+G3rsAx91cN4jJyKPdZTIYRAoQk//s7iMKTPJCczL7w0dKQHXB2xXSC/h2zeryuKgjaUvfMOFZ+3L74mDDTlgXtpt4aQ4uiSHm592eTekWGWuwCLgniy7z0UgYz54u31C+gVc/WfGL6wHpKfpZ4rKIZHfFmXKJL/D60T7Qy4jvNRhRVXXPQe0BIpQ8OiomgapWZvnFkALymjCdRW0faMl4p/VOT7qmacvL/G9WiTlCgmVCMarBbhQqixhcEFx/ufTrGWJsjHBb1VLxBlKk6CC76Lp2ZQ4OiKLa79aQ/3koLQ/FMh2JerHl/pmUE+NmQjbOxm0S0KQATUtYx4f/gnrvrwkxp7em6R9Sdmkp3tfUjkK1gUDCNCSnj5DZJIAWrLUpsqf0kkeSovSOClyfBbSdle2bqh7DY6Uad32RFmuZkwWNU7gV2bK1cDTW5/mqsElA+/jDhWvTRzdhOpEjDABuJi9Xu+8liw6Eo7iON7/um0IGa32okK60=";

    private static final String VALUE_NODE = "value";
    private static final String SIGNATURE_NODE = "signature";

    public static class Serializer implements TypeSerializer<Skin> {

        @Override
        public Skin deserialize(Type type, ConfigurationNode node) throws SerializationException {

            Skin skin = new Skin();

            if (!node.node(VALUE_NODE).isNull()) {
                skin.setValue(node.node(VALUE_NODE).get(String.class));
            }
            if (!node.node(SIGNATURE_NODE).isNull()) {
                skin.setSignature(node.node(SIGNATURE_NODE).get(String.class));
            }

            return skin;
        }

        @Override
        public void serialize(Type type, @Nullable Skin skin, ConfigurationNode node) throws SerializationException {
            node.node(VALUE_NODE).set(skin.getValue());
            node.node(SIGNATURE_NODE).set(skin.getSignature());
        }
    }
}
