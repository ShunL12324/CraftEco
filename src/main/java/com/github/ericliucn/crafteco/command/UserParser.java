package com.github.ericliucn.crafteco.command;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.ValueCompleter;
import org.spongepowered.api.command.parameter.managed.ValueParser;
import org.spongepowered.api.entity.living.player.User;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class UserParser implements ValueParser<User> {

    @Override
    public Optional<? extends User> parseValue(Parameter.Key<? super User> parameterKey, ArgumentReader.Mutable reader, CommandContext.Builder context) throws ArgumentParseException {
        reader.skipWhitespace();
        String userName = reader.parseString();
        try {
            return Sponge.server().userManager().load(userName).get(200, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public static class UserCompleter implements ValueCompleter {

        @Override
        public List<CommandCompletion> complete(CommandContext context, String currentInput) {
            return Sponge.server().userManager().streamAll()
                    .map(profile -> profile.name().orElse("UNKNOWN"))
                    .map(CommandCompletion::of)
                    .collect(Collectors.toList());
        }
    }
}
