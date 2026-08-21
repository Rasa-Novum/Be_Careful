package net.rasanovum.becareful.light;

import com.mojang.serialization.Codec;

import java.util.ArrayList;
import java.util.List;

public record LightFieldStore(List<LightField> fields) {
    public static final Codec<LightFieldStore> CODEC =
            LightField.CODEC.listOf().xmap(LightFieldStore::new, LightFieldStore::fields);

    public LightFieldStore {
        fields = new ArrayList<>(fields);
    }
}
