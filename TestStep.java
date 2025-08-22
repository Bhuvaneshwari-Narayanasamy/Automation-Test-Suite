public class TestStep {
    public String action;
    public String value;
    public String xpath;

    public TestStep(String action, String value, String xpath) {
        this.action = action;
        this.value = value;
        this.xpath = xpath;
    }

    @Override
    public String toString() {
        return String.format("▶ Action: %s | Value: %s | XPath: %s", action, value, xpath);
    }
}
