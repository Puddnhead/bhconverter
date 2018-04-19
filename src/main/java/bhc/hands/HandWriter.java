package bhc.hands;

import bhc.converter.GameConverter;
import bhc.domain.PokerGame;

import java.io.FileWriter;

/**
 * Base class for hand writer
 *
 * Created by MVW on 4/18/2018.
 */
abstract class HandWriter {

    protected GameConverter gameConverter;
    protected PokerGame pokerGame;
    protected FileWriter fileWriter;

    public HandWriter(GameConverter gameConverter, FileWriter fileWriter, PokerGame pokerGame) {
        this.gameConverter = gameConverter;
        this.pokerGame = pokerGame;
        this.fileWriter = fileWriter;
    }
}
