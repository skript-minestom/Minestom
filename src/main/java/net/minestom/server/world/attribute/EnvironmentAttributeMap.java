package net.minestom.server.world.attribute;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.Result;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.registry.RegistryTranscoder;
import net.minestom.server.utils.Either;
import net.minestom.server.world.attribute.EnvironmentAttribute.Modifier;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public record EnvironmentAttributeMap(
        Map<EnvironmentAttribute<?>, Entry<?, ?>> entries,
        Set<EnvironmentAttribute<?>> explicitKeys
) {
    public static final EnvironmentAttributeMap EMPTY = new EnvironmentAttributeMap(Map.of(), Set.of());

    private static final Codec<Map<EnvironmentAttribute<?>, Entry<?, ?>>> ENTRIES_CODEC = EnvironmentAttribute.CODEC
            .mapValueTyped(Entry::codec0, true);

    public static final Codec<EnvironmentAttributeMap> CODEC = new Codec<>() {
        @Override
        public <D> Result<EnvironmentAttributeMap> decode(Transcoder<D> coder, D value) {
            final Result<Map<EnvironmentAttribute<?>, Entry<?, ?>>> decoded = ENTRIES_CODEC.decode(coder, value);
            if (!(decoded instanceof Result.Ok(Map<EnvironmentAttribute<?>, Entry<?, ?>> entries)))
                return decoded.cast();
            final Set<EnvironmentAttribute<?>> explicitKeys = isDatapackInit(coder)
                    ? Set.of()
                    : Set.copyOf(entries.keySet());
            return new Result.Ok<>(new EnvironmentAttributeMap(entries, explicitKeys));
        }

        @Override
        public <D> Result<D> encode(Transcoder<D> coder, EnvironmentAttributeMap value) {
            Map<EnvironmentAttribute<?>, Entry<?, ?>> entries = value.entries();
            if (isClientRegistryEncode(coder)) {
                entries = value.clientRegistryEntries();
            }
            return ENTRIES_CODEC.encode(coder, entries);
        }
    };

    public EnvironmentAttributeMap {
        entries = Map.copyOf(entries);
        explicitKeys = Set.copyOf(explicitKeys);
    }

    private Map<EnvironmentAttribute<?>, Entry<?, ?>> clientRegistryEntries() {
        if (explicitKeys.isEmpty()) return Map.of();
        return entries.entrySet().stream()
                .filter(entry -> explicitKeys.contains(entry.getKey()))
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    static boolean isClientRegistryEncode(Transcoder<?> coder) {
        return coder instanceof RegistryTranscoder<?> context && context.forClient();
    }

    static boolean isDatapackInit(Transcoder<?> coder) {
        return coder instanceof RegistryTranscoder<?> context && context.init();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(EnvironmentAttributeMap existing) {
        return new Builder(existing);
    }

    public record Entry<T, Arg>(Arg argument, Modifier<T, Arg> modifier) {

        @SuppressWarnings("unchecked")
        public static <T> Codec<Entry<T, ?>> codec(EnvironmentAttribute<T> attribute) {
            // A value is represented by either a single value which acts as an override,
            // or a struct with `modifier` and `argument` keys (full codec).

            Codec<Entry<T, ?>> fullCodec = attribute.type().modifierCodec()
                    .unionType("modifier", Entry::fullCodec, Entry::modifier);

            final var override = new Modifier.Override<>(attribute.valueCodec());
            return Codec.Either(attribute.valueCodec(), fullCodec).transform(
                    either -> either.unify(
                            value -> new Entry<>(value, override),
                            u -> u),
                    entry -> entry.modifier instanceof Modifier.Override
                            ? Either.left((T) entry.argument) : Either.right(entry));
        }

        private static Codec<Entry<?, ?>> codec0(EnvironmentAttribute<?> attribute) {
            //noinspection unchecked,rawtypes
            return (Codec) codec(attribute);
        }

        private static <T, Arg> StructCodec<Entry<T, Arg>> fullCodec(Modifier<T, Arg> modifier) {
            return StructCodec.struct(
                    "argument", modifier.argumentCodec(), Entry::argument,
                    (argument) -> new Entry<>(argument, modifier)
            );
        }

    }

    public static final class Builder {
        private final Map<EnvironmentAttribute<?>, Entry<?, ?>> entries = new HashMap<>();
        private final Set<EnvironmentAttribute<?>> explicitKeys = new HashSet<>();

        private Builder() {
        }

        private Builder(EnvironmentAttributeMap existing) {
            entries.putAll(existing.entries);
            explicitKeys.addAll(existing.explicitKeys);
        }

        public <T> Builder set(EnvironmentAttribute<T> attribute, T value) {
            entries.put(attribute, new Entry<>(value, new Modifier.Override<>(attribute.valueCodec())));
            explicitKeys.add(attribute);
            return this;
        }

        public <T, Arg> Builder modify(EnvironmentAttribute<T> attribute, Modifier<T, Arg> modifier, Arg argument) {
            entries.put(attribute, new Entry<>(argument, modifier));
            explicitKeys.add(attribute);
            return this;
        }

        public EnvironmentAttributeMap build() {
            return new EnvironmentAttributeMap(entries, explicitKeys);
        }
    }

}
