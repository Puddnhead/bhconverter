package bhc.util;

import bhc.domain.PokerGame;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Util for parsing a game type
 *
 * Created by MVW on 4/15/2018.
 */
public class GameTypeUtil {

    private static final Pattern cashGamePattern =
            Pattern.compile("^.* RING - \\$(\\d+(\\.\\d\\d)?)-\\$(\\d+(\\.\\d\\d)?) - HOLDEM - NL - TBL No.(\\d+)\\.txt$");

    private static final Pattern tournamentGamePattern =
            Pattern.compile("^.* MTT - (.*) - (\\$\\d+-\\$\\d+(\\.\\d\\d)?) - HOLDEM - NL -Tourney No\\.(\\d+)\\.txt$");

    public static PokerGame getGameType(File file) {
        PokerGame pokerGame = null;

        String filename = file.getName();
        Matcher cashGameMatcher = cashGamePattern.matcher(filename);
        Matcher tournamentGameMatcher = tournamentGamePattern.matcher(filename);

        if (cashGameMatcher.find()) {
            double smallBlind = Double.parseDouble(cashGameMatcher.group(1));
            double bigBlind = Double.parseDouble(cashGameMatcher.group(3));
            String tableNumber = cashGameMatcher.group(5);
            pokerGame = new PokerGame(smallBlind, bigBlind, tableNumber);
        } else if (tournamentGameMatcher.find()) {
            String entryFee = tournamentGameMatcher.group(2).replace("-", "+");
            String tournamentNumber = tournamentGameMatcher.group(4);
            pokerGame = new PokerGame(entryFee, tournamentNumber);
        }

        return pokerGame;
    }
}
