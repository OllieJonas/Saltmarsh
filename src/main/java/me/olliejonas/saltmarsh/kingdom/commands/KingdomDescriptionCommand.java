package me.olliejonas.saltmarsh.kingdom.commands;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandPermissions;
import me.olliejonas.saltmarsh.command.meta.TimeoutCommand;
import me.olliejonas.saltmarsh.embed.EmbedUtils;
import me.olliejonas.saltmarsh.kingdom.roles.Role;
import me.olliejonas.saltmarsh.kingdom.roles.RoleFactory;
import me.olliejonas.saltmarsh.util.MiscUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.jooq.lambda.tuple.Tuple2;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class KingdomDescriptionCommand extends TimeoutCommand {

    public static final int MAX_USER_ROLES = 2;

    static final String TABLE_OF_CONTENTS_TITLE = "Index";

    public KingdomDescriptionCommand() {
        super(CommandPermissions.EVENTS, "kingdom-description");
    }

    @Override
    public Collection<OptionData> args() {
        return List.of(
                new OptionData(OptionType.STRING, "role", "the role you'd like to get the description for!")
        );
    }

    @Override
    public InteractionResponses execute(SlashCommandInteractionEvent event, Member executor, TextChannel channel, Map<String, OptionMapping> args, String aliasUsed) throws CommandFailedException {
        return InteractionResponses.deferEmbed(true, () -> {
            String options = "all";

            if (args.containsKey("role"))
                options = args.get("role").getAsString().strip();

            Collection<Class<? extends Role>> roles;

            if (options.equals("all")) {
                if (!CommandPermissions.ADMIN.hasPermission(executor))
                    return EmbedUtils.error("You need to be an admin to use the \"all\" tag!");

                roles = RoleFactory.ALL_ROLE_CLASSES;

            } else {
                try {
                    roles = Arrays.stream(options.split(","))
                            .map(String::strip)
                            .map(RoleFactory::factory)
                            .collect(Collectors.toSet());
                } catch (Exception e) {
                    return EmbedUtils.error(e);
                }

                if (roles.size() <= MAX_USER_ROLES && !CommandPermissions.EVENTS.hasPermission(executor))
                    return EmbedUtils.error("You need to be able to create events to request more than one role description!");
            }

            List<MessageEmbed> embeds = roles.stream()
                    .map(clazz -> RoleFactory.factory(clazz, null))
                    .map(role -> {
                        MessageEmbed description = role.description();

                        MessageEmbed.Field desc = description.getFields().get(0);

                        EmbedBuilder builder = new EmbedBuilder()
                                .setColor(description.getColor())
                                .setTitle(role.name())
                                .setDescription(desc.getValue());

                        description.getFields().subList(1, description.getFields().size()).forEach(builder::addField);

//                        if (description.getAuthor() != null)
//                            builder.setAuthor(description.getAuthor().getName());

                        return builder.build();
                    }).toList();

            Map<String, MessageEmbed> embedTitles = embeds.stream().collect(Collectors.toMap(MessageEmbed::getTitle, e -> e));

            Map<String, String> embedLinks = new HashMap<>();

            AtomicReference<Message> indexRef = new AtomicReference<>();

            AtomicInteger updated = new AtomicInteger();
            AtomicInteger created = new AtomicInteger();

            CompletableFuture<Set<Message>> future = channel.getHistory().retrievePast(RoleFactory.ALL_ROLE_CLASSES.size() + 5).submit()
                    .thenApply(messages -> messages.stream()
                            .filter(message -> !message.getEmbeds().isEmpty())
                            .filter(message -> embedTitles.containsKey(message.getEmbeds().get(0).getTitle()))
                            .map(message -> new Tuple2<>(message, embedTitles.get(message.getEmbeds().get(0).getTitle())))
                            .peek(tuple -> indexRef.set(Objects.equals(tuple.v2().getTitle(), TABLE_OF_CONTENTS_TITLE) ? tuple.v1() : null))
                            .sorted(Comparator.comparing(m -> m.v2().getTitle()))
                            .collect(Collectors.toCollection(LinkedHashSet::new)))
                    .thenApply(set -> set.stream()
                            .map(tuple -> tuple.v1().editMessage(MessageEditData.fromEmbeds(tuple.v2())))
                            .map(RestAction::submit)
                            .map(CompletableFuture::join)
                            .collect(Collectors.toSet()))
                    .whenComplete((messages, t) -> {
                        if (t != null)
                            throw new RuntimeException(t);

                        messages.forEach(message -> embedLinks.put(message.getEmbeds().get(0).getTitle(), MiscUtils.getMessageLink(message)));

                        Set<MessageEmbed> missingEmbeds = new HashSet<>(embedTitles.values());

                        missingEmbeds.removeAll(messages.stream()
                                .map(message -> message.getEmbeds().get(0).getTitle())
                                .filter(embedTitles::containsKey)
                                .map(embedTitles::get)
                                .collect(Collectors.toSet()));

                        Set<Message> missingMessages = missingEmbeds.stream().map(channel::sendMessageEmbeds)
                                        .map(RestAction::submit)
                                        .map(CompletableFuture::join)
                                        .peek(message -> embedLinks.put(message.getEmbeds().get(0).getTitle(), MiscUtils.getMessageLink(message)))
                                        .collect(Collectors.toSet());


                        MessageEmbed indexEmbed = EmbedUtils.colour()
                                .setTitle(TABLE_OF_CONTENTS_TITLE)
                                .setDescription(embedLinks.entrySet().stream()
                                        .map(entry -> entry.getKey() + " - " + entry.getValue())
                                        .sorted()
                                        .collect(Collectors.joining("\n")))
                                .build();

                        Message index = indexRef.get();

                        if (index != null)
                            index.editMessage(MessageEditData.fromEmbeds(indexEmbed)).queue();
                        else
                            channel.sendMessageEmbeds(indexEmbed).queue();

                        updated.set(messages.size());
                        created.set(missingEmbeds.size());
                    });

            future.join();

            return EmbedUtils.from(
                    "Created " + created.get() + " embeds.\n" +
                            "Updated " + updated.get() + " embeds.\n" +
                            (indexRef.get() != null ? "Updated" : "Created" + " table of contents."));
        });
    }

    @Override
    public long timeout() {
        return 5;
    }

    @Override
    public TimeUnit timeoutUnit() {
        return TimeUnit.SECONDS;
    }
}
