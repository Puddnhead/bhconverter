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

    private static final Pattern CASH_GAME_PATTERN =
            Pattern.compile("^.* RING - \\$(\\d+(\\.\\d\\d)?)-\\$(\\d+(\\.\\d\\d)?) - HOLDEM - NL - TBL No.(\\d+)\\.txt$");

    private static final Pattern TOURNAMENT_GAME_PATTERN =
            Pattern.compile("^.* MTT - (.*) - ((TT)?\\$\\d+-\\$\\d+(\\.\\d\\d)?) - HOLDEM - NL -Tourney No\\.(\\d+)\\.txt$");

    private static final Pattern ZONE_GAME_PATTERN =
            Pattern.compile("^.* ZONE - \\$(\\d+(\\.\\d\\d)?)-\\$(\\d+(\\.\\d\\d)?) - HOLDEMZonePoker - NL - ZonePoker No.(\\d+)\\.txt$");

    public static PokerGame getGameType(File file) {
        PokerGame pokerGame = null;
        double smallBlind, bigBlind;

        String filename = file.getName();
        Matcher cashGameMatcher = CASH_GAME_PATTERN.matcher(filename);
        Matcher tournamentGameMatcher = TOURNAMENT_GAME_PATTERN.matcher(filename);
        Matcher zoneGameMatcher = ZONE_GAME_PATTERN.matcher(filename);

        if (cashGameMatcher.find()) {
            smallBlind = Double.parseDouble(cashGameMatcher.group(1));
            bigBlind = Double.parseDouble(cashGameMatcher.group(3));
            String tableNumber = cashGameMatcher.group(5);
            pokerGame = new PokerGame(smallBlind, bigBlind, tableNumber);
        } else if (tournamentGameMatcher.find()) {
            String entryFee = tournamentGameMatcher.group(2).replace("-", "+");
            String tournamentNumber = tournamentGameMatcher.group(5);
            pokerGame = new PokerGame(entryFee, tournamentNumber);
        } else if (zoneGameMatcher.find()) {
            smallBlind = Double.parseDouble(zoneGameMatcher.group(1));
            bigBlind = Double.parseDouble(zoneGameMatcher.group(3));
            String tableNumber = zoneGameMatcher.group(5);
            pokerGame = new PokerGame(smallBlind, bigBlind, tableNumber);
        }

        return pokerGame;
    }
}
