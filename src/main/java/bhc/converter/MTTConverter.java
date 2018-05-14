package bhc.converter;

import bhc.domain.Hand;
import bhc.domain.HandContext;
import bhc.domain.PokerGame;
import bhc.hands.HandParsingUtil;
import bhc.hands.MTTHandWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Logic for converting MTT
 *
 * Created by MVW on 4/15/2018.
 */
public class MTTConverter extends GameConverter {

    public MTTConverter(File inputFile, File outputDirectory, PokerGame pokerGame) {
        super(inputFile, outputDirectory, pokerGame);
    }

    @Override
    void transformNextHand(String firstLine, BufferedReader reader, FileWriter writer) {
        MTTHandWriter handWriter = new MTTHandWriter(this, writer, pokerGame);
        handWriter.transformFirstLine(firstLine, writer);
        List<String> entireHand = readEntireHand(reader);
        handWriter.writeSecondLine(entireHand, writer);

        Map<String, String> playerMap = HandParsingUtil.generatePlayerMap(entireHand);
        Map<String, String> holeCardsMap = HandParsingUtil.generateHoldCardsMap(entireHand);
        handWriter.setPlayerMap(playerMap);
        handWriter.setHoleCardsMap(holeCardsMap);
        Map<String, String> seatMap = HandParsingUtil.generateSeatMap(entireHand);
        handWriter.writeSeats(entireHand, Optional.of(seatMap));

        handWriter.writePostingActions(entireHand);
        handWriter.writeHoleCards(entireHand);
        HandContext handContext = handWriter.writeHandAction(entireHand);
        handWriter.writeShowdownAndSummary(entireHand, Optional.of(seatMap), handContext);
    }
}
