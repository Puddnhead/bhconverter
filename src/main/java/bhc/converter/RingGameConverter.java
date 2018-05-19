package bhc.converter;

import bhc.domain.HandContext;
import bhc.domain.PokerGame;
import bhc.hands.HandParsingUtil;
import bhc.hands.HandWriter;
import bhc.hands.RingGameHandWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Logic for converting ring games
 *
 * Created by MVW on 4/15/2018.
 */
public class RingGameConverter extends GameConverter {

    public RingGameConverter(File inputFile, File outputDirectory, PokerGame pokerGame) {
        super(inputFile, outputDirectory, pokerGame);
    }

    void transformNextHand(String firstLine, BufferedReader reader, FileWriter writer) {
        HandWriter handWriter = new RingGameHandWriter(this, writer, pokerGame);
        handWriter.transformFirstLine(firstLine, writer);

        List<String> entireHand = readEntireHand(reader);
        handWriter.writeSecondLine(entireHand, writer);
        Map<String, String> playerMap = HandParsingUtil.generatePlayerMap(entireHand);
        Map<String, String> holeCardsMap = HandParsingUtil.generateHoldCardsMap(entireHand);
        handWriter.setPlayerMap(playerMap);
        handWriter.setHoleCardsMap(holeCardsMap);
        handWriter.writeSeats(entireHand, Optional.empty());

        HandParsingUtil.modifyHeroEntry(playerMap);
        handWriter.writePostingActions(entireHand);
        handWriter.writeHoleCards(entireHand);
        HandContext handContext = handWriter.writeHandAction(entireHand);
        handWriter.writeShowdownAndSummary(entireHand, Optional.empty(), handContext);
    }
}
