package me.olliejonas.saltmarsh.util;

import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.entities.User;

@UtilityClass
public class UserUtils {

    public void privateMessageUser(User user, CharSequence message) {
        user.openPrivateChannel().queue((channel -> channel.sendMessage(message).queue()));
    }
}
