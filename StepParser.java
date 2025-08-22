import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StepParser {

    public static List<TestStep> parseSteps(String textBlock) {
        List<TestStep> steps = new ArrayList<>();

        // Split lines by newline
        String[] lines = textBlock.split("\\r?\\n");

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            // Remove numbering (e.g., "1.", "2.") at the beginning
            line = line.replaceFirst("^\\d+\\.?\\s*", "");

            String action = "";
            String value = "";
            String xpath = "";

            // Extract the action
            Matcher actionMatcher = Pattern.compile("(?i)\\b(hover|click|verify|type|enter)\\b").matcher(line);
            if (actionMatcher.find()) {
                action = actionMatcher.group(1).toLowerCase();
            }

            // Extract all quoted parts
            Matcher quotedMatcher = Pattern.compile("\"([^\"]*)\"").matcher(line);
            List<String> parts = new ArrayList<>();
            while (quotedMatcher.find()) {
                String raw = quotedMatcher.group(1).trim();
                if (!raw.isEmpty()) {
                    parts.add(raw);
                }
            }

            // Fallback if no quotes found — extract raw xpath manually
            if (parts.isEmpty()) {
                Matcher fallbackXpath = Pattern.compile("(//[^\\s\"]+)").matcher(line);
                while (fallbackXpath.find()) {
                    parts.add(fallbackXpath.group(1));
                }
            }

            // Assign value and xpath from extracted parts
            for (String part : parts) {
                if (part.startsWith("//")) {
                    xpath = part;
                } else if (value.isEmpty()) {
                    value = part;
                }
            }

            // Special case: openurl
            if (line.toLowerCase().startsWith("open url") && !value.isEmpty()) {
                action = "openurl";
                xpath = "";
            }

            // Fallback for hover if xpath missing
            if (action.equals("hover") && xpath.isEmpty() && !value.isEmpty()) {
                xpath = "//*[contains(text(), '" + value + "')]";
            }

            // Skip invalid lines
            if (action.isEmpty()) continue;

            steps.add(new TestStep(action, value, xpath));
        }

        return steps;
    }
}
