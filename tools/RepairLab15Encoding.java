import dao.PostgreSQLConnectionBuilder;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class RepairLab15Encoding {
    private static final Charset LEGACY = Charset.forName("windows-1251");
    private record Change(String table, String column, long id, String before, String after) {}

    static String recover(String value) {
        if (value == null) return null;
        String result = value;
        for (int layer = 0; layer < 3; layer++) {
            try {
                var bytes = LEGACY.newEncoder()
                        .onMalformedInput(CodingErrorAction.REPORT)
                        .onUnmappableCharacter(CodingErrorAction.REPORT)
                        .encode(CharBuffer.wrap(result));
                String candidate = StandardCharsets.UTF_8.newDecoder()
                        .onMalformedInput(CodingErrorAction.REPORT)
                        .onUnmappableCharacter(CodingErrorAction.REPORT)
                        .decode(bytes).toString();
                if (candidate.equals(result)) break;
                result = candidate;
            } catch (CharacterCodingException ex) {
                break;
            }
        }
        return result;
    }

    private static void selfTest() {
        String original = "\u0420\u043e\u0441\u0442\u043e\u0432-\u043d\u0430-\u0414\u043e\u043d\u0443";
        String broken = new String(original.getBytes(StandardCharsets.UTF_8), LEGACY);
        String twice = new String(broken.getBytes(StandardCharsets.UTF_8), LEGACY);
        if (!original.equals(recover(broken)) || !original.equals(recover(twice))
                || !original.equals(recover(original)) || !"Park 12".equals(recover("Park 12"))
                || recover(null) != null || !"\ufffd".equals(recover("\ufffd"))) {
            throw new AssertionError("Encoding recovery self-test failed");
        }
        System.out.println("PASS: reversible corruption is repaired; valid text and unrecoverable characters are preserved");
    }

    private static void collect(Connection connection, String table, String[] columns, List<Change> changes) throws Exception {
        try (var statement = connection.createStatement();
                ResultSet rows = statement.executeQuery("SELECT id," + String.join(",", columns) + " FROM " + table + " ORDER BY id")) {
            while (rows.next()) {
                for (String column : columns) {
                    String before = rows.getString(column);
                    String after = recover(before);
                    if (before != null && !before.equals(after)) {
                        changes.add(new Change(table, column, rows.getLong("id"), before, after));
                    }
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 1 && args[0].equals("--self-test")) {
            selfTest();
            return;
        }
        boolean apply = args.length == 1 && args[0].equals("--apply");
        if (args.length != 1 || (!apply && !args[0].equals("--check"))) {
            throw new IllegalArgumentException("Use --check, --apply or --self-test");
        }
        try (Connection connection = new PostgreSQLConnectionBuilder().getConnection()) {
            if (!"cityparks_lab15".equals(connection.getCatalog())) {
                throw new IllegalStateException("Only the lab 1.15 database may be repaired");
            }
            connection.setAutoCommit(false);
            try {
                List<Change> changes = new ArrayList<>();
                collect(connection, "cities", new String[]{"name", "status"}, changes);
                collect(connection, "parks", new String[]{"name", "type"}, changes);
                for (Change change : changes) {
                    System.out.println(change.table + " id=" + change.id + " " + change.column + " -> " + change.after);
                    if (apply) {
                        String sql = "UPDATE " + change.table + " SET " + change.column + "=? WHERE id=? AND " + change.column + "=?";
                        try (PreparedStatement statement = connection.prepareStatement(sql)) {
                            statement.setString(1, change.after);
                            statement.setLong(2, change.id);
                            statement.setString(3, change.before);
                            if (statement.executeUpdate() != 1) {
                                throw new IllegalStateException("The record changed during recovery; rolling back");
                            }
                        }
                    }
                }
                if (apply) connection.commit();
                else connection.rollback();
                System.out.println((apply ? "REPAIRED: " : "PENDING: ") + changes.size() + " text fields");
            } catch (Exception ex) {
                connection.rollback();
                throw ex;
            }
        }
    }
}
