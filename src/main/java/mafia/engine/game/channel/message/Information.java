package mafia.engine.game.channel.message;

import mafia.engine.player.Player;

public record Information(Player target, String info) implements Message {

    public Information(Player target, Object info) {
        // TODO: transform
        // temp
        this(target, info.toString());
    }
    
}
