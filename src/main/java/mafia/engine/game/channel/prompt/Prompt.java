package mafia.engine.game.channel.prompt;

import java.util.List;

import mafia.engine.player.Player;

public record Prompt(Player target, String prompt, List<PromptOption> options) {
    
}