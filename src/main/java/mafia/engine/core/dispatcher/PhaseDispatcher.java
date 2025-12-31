package mafia.engine.core.dispatcher;

import java.util.function.Predicate;

import mafia.engine.core.GameChannels;
import mafia.engine.game.event.TimeRemainingUpdate;
import mafia.engine.property.Properties;

public abstract class PhaseDispatcher {

    protected final Properties gameProperties;
    protected final GameChannels gameChannels;

    protected PhaseDispatcher(Properties gameProperties, GameChannels gameChannels) {
        this.gameProperties = gameProperties;
        this.gameChannels = gameChannels;
    }
    
    abstract void start();
    abstract void stop();

    protected final void runTimer(
        String propertyKey,
        String label,
        long seconds,
        Predicate<Long> stopCondition
    ) {
        gameProperties.addProperty(propertyKey, seconds);

        for (long i = seconds; i >= 0; i--) {
            if (stopCondition != null && stopCondition.test(i)) {
                gameProperties.addProperty(propertyKey, 0L);
                gameChannels.gameUpdateChannel().send(new TimeRemainingUpdate(label, 0));
                break;
            }

            sleepInSeconds(1);
            gameProperties.addProperty(propertyKey, i);
            gameChannels.gameUpdateChannel().send(new TimeRemainingUpdate(label, i));
        }
    }

    private void sleepInSeconds(int seconds) {
        sleep(seconds * 1000);
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
