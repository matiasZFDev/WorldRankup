package com.worldplugins.rankup.config.data.prestige;

import lombok.NonNull;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class IndividualPrestiges implements Prestiges {
    private final @NonNull Map<Short, Prestige> prestigesById;

    public IndividualPrestiges(@NonNull Collection<? extends Prestige> prestiges) {
        this.prestigesById = prestiges.stream().collect(Collectors.toMap(Prestige::getId, Function.identity()));
    }

    public Prestige getById(short id) {
        return prestigesById.get(id);
    }

    @Override
    public Prestige getPrevious(short prestige) {
        return prestigesById.values().stream()
            .filter(current -> current.getNext() != null && current.getNext() == prestige)
            .findFirst()
            .orElse(null);
    }
}
