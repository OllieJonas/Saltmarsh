package me.olliejonas.saltmarsh.command.debug;

import me.olliejonas.saltmarsh.InteractionResponses;
import me.olliejonas.saltmarsh.command.meta.Command;
import me.olliejonas.saltmarsh.command.meta.CommandFailedException;
import me.olliejonas.saltmarsh.command.meta.CommandPermissions;
import me.olliejonas.saltmarsh.embed.EmbedUtils;
import me.olliejonas.saltmarsh.kingdom.KingdomGame;
import me.olliejonas.saltmarsh.kingdom.KingdomGameRegistry;
import me.olliejonas.saltmarsh.kingdom.RoleAllocation;
import me.olliejonas.saltmarsh.kingdom.roles.*;
import me.olliejonas.saltmarsh.util.MiscUtils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jooq.lambda.tuple.Tuple2;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TestKingdomGameCommand extends Command {

    private final KingdomGameRegistry registry;

    public TestKingdomGameCommand(KingdomGameRegistry registry) {
        super(CommandPermissions.ADMIN, "kingdom");

        this.registry = registry;
    }

    @Override
    public Collection<OptionData> args() {
        return List.of(
                new OptionData(OptionType.STRING, "role", "the role you'd like to select")
        );
    }

    @Override
    public InteractionResponses execute(SlashCommandInteractionEvent event, Member executor, TextChannel channel, Map<String, OptionMapping> args, String aliasUsed) throws CommandFailedException {
        return InteractionResponses.deferEmbed(() -> {
            Map<String, Class<? extends Role>> memberIds = Map.of(
                    "140187632314351617",   Knight.class,       // Ollie
                    "1078360971803906209",      Bandit.class,           // Saltmarsh (DEV)
                    "703645553271111740",       Bandit.class,           // Barry B Benson
                    "806869865393815554",       Challenger.class,           // stt
                    "889253280117555232",       King.class,             // Tristmunk
                    "1171748430520012820",      Usurper.class           // Ukulele
            );

            Map<Member, Class<? extends Role>> members = memberIds.entrySet().stream()
                    .collect(Collectors.toMap(entry -> MiscUtils.getMemberById(executor.getGuild(), entry.getKey()), Map.Entry::getValue));
            try {
                KingdomGame game = registry.startGame(members.keySet(), channel, new RoleAllocation.Determined(members));
                Tuple2<MessageEmbed, FileUpload> announcement = game.getRoleAllocationStrategy().announcement(game);

                channel.sendMessageEmbeds(EmbedUtils.colour("Kingdom Roles",
                        game.getRoleMap().entrySet().stream()
                                .map(e -> e.getKey().getEffectiveName() + " - " + e.getValue().name())
                                .collect(Collectors.joining("\n")))).queue();

                return announcement.v1();
            } catch (Exception e) {
                return EmbedUtils.error("Error: " + e.getMessage() + "!\n" + MiscUtils.shortenedStackTrace(e, 10));
            }

        });
    }
}
