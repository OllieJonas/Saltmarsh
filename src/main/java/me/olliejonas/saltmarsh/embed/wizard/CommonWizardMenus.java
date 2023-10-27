package me.olliejonas.saltmarsh.embed.wizard;

import me.olliejonas.saltmarsh.embed.EmbedUtils;
import me.olliejonas.saltmarsh.embed.wizard.types.StepMenu;
import me.olliejonas.saltmarsh.embed.wizard.types.builders.ButtonBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.List;
import java.util.function.Consumer;

public class CommonWizardMenus {

    private static ButtonBuilder<Boolean> yesNoBuilder(String identifier, MessageEmbed embed) {
        return StepMenu.Button.builder(identifier, embed, Boolean.class).buttons(List.of(
                Button.primary("1", "Yes"),
                Button.primary("2", "No")));
    }

    public static StepMenu.Button<Boolean> YES_NO(String identifier,
                                                  String title,
                                                  String description) {
        return YES_NO(identifier, EmbedUtils.colour().setTitle(title).setDescription(description).build());
    }

    public static StepMenu.Button<Boolean> YES_NO(String identifier, MessageEmbed embed) {
        return yesNoBuilder(identifier, embed).build();
    }

    public static StepMenu.Button<Boolean> YES_NO(String identifier, String title, String description, int skipIfNo) {
        return YES_NO(identifier, EmbedUtils.colour().setTitle(title).setDescription(description).build(), 1, skipIfNo);
    }

    public static StepMenu.Button<Boolean> YES_NO(String identifier, String title, String description, int skipIfYes, int skipIfNo) {
        return YES_NO(identifier, EmbedUtils.colour().setTitle(title).setDescription(description).build(), skipIfYes, skipIfNo);
    }

    public static StepMenu.Button<Boolean> YES_NO(String identifier, MessageEmbed embed,
                                                  int skipIfNo, Consumer<EntryContext<Boolean>> onOption) {
        return YES_NO(identifier, embed, 1, skipIfNo, onOption);
    }

    public static StepMenu.Button<Boolean> YES_NO(String identifier, MessageEmbed embed, int skipIfYes, int skipIfNo) {
        return YES_NO(identifier, embed, skipIfYes, skipIfNo, __ -> {});
    }

    public static StepMenu.Button<Boolean> YES_NO(String identifier, String title, String description,
                                                  int skipIfNo, Consumer<EntryContext<Boolean>> onOption) {
        return YES_NO(identifier, EmbedUtils.colour().setTitle(title).setDescription(description).build(), skipIfNo, onOption);
    }

    public static StepMenu.Button<Boolean> YES_NO(String identifier, String title, String description,
                                                  int skipIfYes, int skipIfNo, Consumer<EntryContext<Boolean>> onOption) {
        return YES_NO(identifier, EmbedUtils.colour().setTitle(title).setDescription(description).build(), skipIfYes, skipIfNo, onOption);
    }

    public static StepMenu.Button<Boolean> YES_NO(String identifier, MessageEmbed embed,
                                                  int skipIfYes, int skipIfNo, Consumer<EntryContext<Boolean>> onOption) {
        return yesNoBuilder(identifier, embed).onOption(onOption
                .andThen(ctx -> ctx.self().setSkip(ctx.result() ? skipIfYes : skipIfNo))).build();
    }
    public static StepMenu.Button<Boolean> CONFIRMATION_SCREEN(String identifier, String title, String description) {
        return CONFIRMATION_SCREEN(identifier, EmbedUtils.colour().setTitle(title).setDescription(description).build(), 1, __ -> {}, __ -> {});
    }

    public static StepMenu.Button<Boolean> CONFIRMATION_SCREEN(String identifier, String title, String description, Consumer<EntryContext<Boolean>> onNo) {
        return CONFIRMATION_SCREEN(identifier, EmbedUtils.colour().setTitle(title).setDescription(description).build(), 1, __ -> {}, onNo);
    }

    public static StepMenu.Button<Boolean> CONFIRMATION_SCREEN(String identifier, MessageEmbed embed) {
        return CONFIRMATION_SCREEN(identifier, embed, 1, __ -> {}, __ -> {});
    }

    public static StepMenu.Button<Boolean> CONFIRMATION_SCREEN(String identifier, MessageEmbed embed,
                                                               int backwardsSkip, Consumer<EntryContext<Boolean>> onYes, Consumer<EntryContext<Boolean>> onNo) {
        return yesNoBuilder(identifier, embed)
                .onOption(ctx -> {
                    ctx.self().setSkip((ctx.result() ? 1 : -Math.abs(backwardsSkip)));
                    if (ctx.result()) onYes.accept(ctx);
                    else onNo.accept(ctx);
                })
                .build();
    }
}
