import java.io.FileInputStream;
import java.util.List;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Main {
    public static void main(String[] args) throws Exception {
        FileInputStream fis = new FileInputStream("APPLICATION.xlsx");
        Workbook workbook = new XSSFWorkbook(fis);
        Sheet sheet = workbook.getSheetAt(0); // First sheet: SETUP

        TestExecutor executor = new TestExecutor(); // ✅ Initialize once

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            Cell testCaseCell = row.getCell(2); // Column C = Test Case Name
            Cell stepBlockCell = row.getCell(4); // Column E = Step Block

            if (testCaseCell == null || stepBlockCell == null) continue;

            String testCaseName = testCaseCell.getStringCellValue().trim();
            String stepBlock = stepBlockCell.getStringCellValue().trim();

            System.out.println("\n=== 🧪 Running: " + testCaseName + " ===");
            List<TestStep> steps = StepParser.parseSteps(stepBlock);

            if (steps.isEmpty()) {
                System.out.println("❌ No valid steps parsed! Test is auto-failed.");
                System.out.println("❓ StepBlock was: " + stepBlock);
                continue;
            }

            for (TestStep step : steps) {
                System.out.println("🔹 " + step);
            }

            boolean passed = executor.run(steps); // ✅ Use same executor

            if (passed) {
                System.out.println("✅ " + testCaseName + " PASSED");
            } else {
                System.out.println("❌ " + testCaseName + " FAILED");
            }
        }

        workbook.close();
        executor.close(); // ✅ Close browser only once after all tests
    }
}
