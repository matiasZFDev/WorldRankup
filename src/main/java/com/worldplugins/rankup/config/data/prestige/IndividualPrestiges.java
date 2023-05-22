package com.worldplugins.rankup.config.data.prestige;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class IndividualPrestiges implements Prestiges {
    private final @NotNull Map<Short, Prestige> prestigesById;

    public IndividualPrestiges(@NotNull Collection<? extends Prestige> prestiges) {
        this.prestigesById = prestiges.stream().collect(Collectors.toMap(Prestige::id, Function.identity()));
    }

    public Prestige getById(short id) {
        return prestigesById.get(id);
    }

    @Override
    public Prestige getPrevious(short prestige) {
        return prestigesById.values().stream()
            .filter(current -> current.next() != null && current.next() == prestige)
            .findFirst()
            .orElse(null);
    }
}
