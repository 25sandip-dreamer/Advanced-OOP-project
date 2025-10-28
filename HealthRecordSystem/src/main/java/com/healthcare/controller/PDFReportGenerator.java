package com.healthcare.controller;

import com.healthcare.model.AssessmentResponse;
import com.healthcare.model.MedicationAssessment;
import com.healthcare.model.Patient;
import com.healthcare.controller.AIAnalysisEngine.AnalysisResult;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * PDFReportGenerator - Generates professional PDF reports
 * Creates beautiful AI analysis reports for doctors
 */
public class PDFReportGenerator {

    /**
     * Generate comprehensive AI analysis report as PDF
     */
    public static File generateAIAnalysisReport(
            MedicationAssessment assessment,
            AnalysisResult analysisResult,
            List<AssessmentResponse> responses,
            String patientName,
            String outputPath) {

        try {
            File pdfFile = new File(outputPath);
            PdfWriter writer = new PdfWriter(pdfFile);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Set margins
            document.setMargins(40, 40, 40, 40);

            // Add header
            addReportHeader(document, assessment, patientName, analysisResult);

            // Add risk summary section
            addRiskSummarySection(document, analysisResult);

            // Add scores section
            addScoresSection(document, analysisResult);

            // Add key findings
            addKeyFindingsSection(document, analysisResult);

            // Add recommendations
            addRecommendationsSection(document, analysisResult);

            // Add patient responses
            addPatientResponsesSection(document, responses);

            // Add footer
            addReportFooter(document);

            document.close();

            System.out.println("✅ PDF report generated: " + pdfFile.getAbsolutePath());
            return pdfFile;

        } catch (Exception e) {
            System.err.println("❌ Error generating PDF: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Add report header with title and patient info
     */
    private static void addReportHeader(Document document, MedicationAssessment assessment,
                                        String patientName, AnalysisResult result) {

        // Get risk color
        DeviceRgb riskColor = getRiskColorRGB(result.getRiskLevel());

        // Title
        Paragraph title = new Paragraph("MEDICATION ASSESSMENT REPORT")
                .setFontSize(24)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(new DeviceRgb(102, 126, 234));
        document.add(title);

        // Subtitle with AI badge
        Paragraph subtitle = new Paragraph("AI-Powered Analysis & Clinical Insights")
                .setFontSize(14)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GRAY)
                .setItalic();
        document.add(subtitle);

        // Risk level banner
        Paragraph riskBanner = new Paragraph("RISK LEVEL: " + result.getRiskLevel().toUpperCase())
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.WHITE)
                .setBackgroundColor(riskColor)
                .setPadding(15)
                .setMarginTop(20)
                .setMarginBottom(20);
        document.add(riskBanner);

        // Patient Information Table
        Table patientTable = new Table(new float[]{1, 2});
        patientTable.setWidth(UnitValue.createPercentValue(100));
        patientTable.setMarginBottom(20);

        addTableRow(patientTable, "Patient Name:", patientName);
        addTableRow(patientTable, "Medication:", assessment.getMedicationName() + " " + assessment.getDosage());
        addTableRow(patientTable, "Assessment Day:", "Day " + assessment.getAssessmentDay());
        addTableRow(patientTable, "Report Date:", LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy HH:mm")));
        addTableRow(patientTable, "Overall Sentiment:", result.getOverallSentiment());

        document.add(patientTable);

        // Divider
        document.add(new Paragraph("\n"));
    }

    /**
     * Add risk summary section
     */
    private static void addRiskSummarySection(Document document, AnalysisResult result) {
        addSectionTitle(document, "RISK ASSESSMENT SUMMARY");

        Paragraph riskScore = new Paragraph(String.format("Overall Risk Score: %.1f/100", result.getOverallRiskScore()))
                .setFontSize(16)
                .setBold()
                .setFontColor(getRiskColorRGB(result.getRiskLevel()));
        document.add(riskScore);

        if (result.isRequiresImmediateAttention()) {
            Paragraph urgent = new Paragraph("⚠ REQUIRES IMMEDIATE ATTENTION")
                    .setFontSize(14)
                    .setBold()
                    .setFontColor(ColorConstants.RED)
                    .setBackgroundColor(new DeviceRgb(255, 235, 235))
                    .setPadding(10)
                    .setMarginTop(10)
                    .setMarginBottom(10);
            document.add(urgent);
        }

        document.add(new Paragraph("\n"));
    }

    /**
     * Add scores section with visual bars
     */
    private static void addScoresSection(Document document, AnalysisResult result) {
        addSectionTitle(document, "DETAILED SCORES");

        Table scoresTable = new Table(new float[]{2, 1, 1});
        scoresTable.setWidth(UnitValue.createPercentValue(100));

        // Header
        scoresTable.addHeaderCell(createCell("Metric", true));
        scoresTable.addHeaderCell(createCell("Score", true));
        scoresTable.addHeaderCell(createCell("Status", true));

        // Adherence
        addScoreRow(scoresTable, "Medication Adherence", result.getAdherenceScore(),
                result.getAdherenceScore() >= 70);

        // Side Effects
        addScoreRow(scoresTable, "Side Effect Severity", result.getSideEffectScore(),
                result.getSideEffectScore() < 50);

        // Efficacy
        addScoreRow(scoresTable, "Treatment Efficacy", result.getEfficacyScore(),
                result.getEfficacyScore() >= 60);

        document.add(scoresTable);
        document.add(new Paragraph("\n"));
    }

    /**
     * Add key findings section
     */
    private static void addKeyFindingsSection(Document document, AnalysisResult result) {
        addSectionTitle(document, "KEY FINDINGS");

        com.itextpdf.layout.element.List list = new com.itextpdf.layout.element.List()
                .setSymbolIndent(12)
                .setListSymbol("•")
                .setFontSize(12);

        for (String category : result.getKeyFindings().keySet()) {
            String finding = result.getKeyFindings().get(category);
            list.add((ListItem) new ListItem(finding).setMarginBottom(8));
        }

        document.add(list);
        document.add(new Paragraph("\n"));
    }

    /**
     * Add recommendations section
     */
    private static void addRecommendationsSection(Document document, AnalysisResult result) {
        addSectionTitle(document, "CLINICAL RECOMMENDATIONS");

        Paragraph recommendations = new Paragraph(result.getRecommendation())
                .setFontSize(12)
                .setBackgroundColor(new DeviceRgb(249, 249, 249))
                .setPadding(15)
                .setBorder(new com.itextpdf.layout.borders.SolidBorder(ColorConstants.LIGHT_GRAY, 1));

        document.add(recommendations);
        document.add(new Paragraph("\n"));
    }

    /**
     * Add patient responses section
     */
    private static void addPatientResponsesSection(Document document, List<AssessmentResponse> responses) {
        addSectionTitle(document, "PATIENT RESPONSES");

        int questionNum = 1;
        for (AssessmentResponse response : responses) {
            // Question
            Paragraph question = new Paragraph("Q" + questionNum++ + ": " + response.getQuestionText())
                    .setFontSize(11)
                    .setBold()
                    .setMarginBottom(5);
            document.add(question);

            // Response
            String responseText = response.getResponseText() != null ? response.getResponseText() : "No response provided";
            Paragraph answer = new Paragraph("A: " + responseText)
                    .setFontSize(10)
                    .setMarginLeft(15)
                    .setMarginBottom(5);
            document.add(answer);

            // Score if available
            if (response.getResponseScore() != null) {
                Paragraph score = new Paragraph("Rating: " + response.getResponseScore() + "/10")
                        .setFontSize(10)
                        .setMarginLeft(15)
                        .setFontColor(new DeviceRgb(102, 126, 234))
                        .setMarginBottom(5);
                document.add(score);
            }

            // Sentiment
            if (response.getSentiment() != null) {
                DeviceRgb sentimentColor = getSentimentColor(response.getSentiment());
                Paragraph sentiment = new Paragraph("Sentiment: " + response.getSentiment())
                        .setFontSize(9)
                        .setMarginLeft(15)
                        .setFontColor(sentimentColor)
                        .setItalic()
                        .setMarginBottom(15);
                document.add(sentiment);
            }
        }
    }

    /**
     * Add report footer
     */
    private static void addReportFooter(Document document) {
        document.add(new Paragraph("\n\n"));

        Paragraph footer = new Paragraph("This report was automatically generated by the Healthcare AI System.\n" +
                "The recommendations provided are based on AI analysis and should be reviewed by a qualified healthcare professional.\n" +
                "Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy HH:mm:ss")))
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GRAY)
                .setItalic();
        document.add(footer);
    }

    /**
     * Helper: Add section title
     */
    private static void addSectionTitle(Document document, String title) {
        Paragraph sectionTitle = new Paragraph(title)
                .setFontSize(16)
                .setBold()
                .setFontColor(new DeviceRgb(51, 51, 51))
                .setMarginTop(10)
                .setMarginBottom(10);
        document.add(sectionTitle);
    }

    /**
     * Helper: Add table row
     */
    private static void addTableRow(Table table, String label, String value) {
        table.addCell(createCell(label, true));
        table.addCell(createCell(value, false));
    }

    /**
     * Helper: Create table cell
     */
    private static Cell createCell(String content, boolean isHeader) {
        Cell cell = new Cell().add(new Paragraph(content));
        if (isHeader) {
            cell.setBold()
                    .setBackgroundColor(new DeviceRgb(240, 240, 240))
                    .setFontSize(11);
        } else {
            cell.setFontSize(10);
        }
        cell.setPadding(8);
        return cell;
    }

    /**
     * Helper: Add score row to table
     */
    private static void addScoreRow(Table table, String metric, double score, boolean isGood) {
        table.addCell(createCell(metric, false));
        table.addCell(createCell(String.format("%.1f%%", score), false));

        String status = isGood ? "✓ Good" : "⚠ Needs Attention";
        DeviceRgb color = isGood ? new DeviceRgb(76, 175, 80) : new DeviceRgb(255, 152, 0);

        Cell statusCell = new Cell().add(new Paragraph(status));
        statusCell.setFontColor(color).setBold().setPadding(8).setFontSize(10);
        table.addCell(statusCell);
    }

    /**
     * Helper: Get risk color as DeviceRgb
     */
    private static DeviceRgb getRiskColorRGB(String riskLevel) {
        switch (riskLevel) {
            case "Critical": return new DeviceRgb(211, 47, 47);  // Red
            case "High": return new DeviceRgb(255, 152, 0);      // Orange
            case "Moderate": return new DeviceRgb(255, 193, 7);  // Yellow
            case "Low": return new DeviceRgb(76, 175, 80);       // Green
            default: return new DeviceRgb(117, 117, 117);        // Gray
        }
    }

    /**
     * Helper: Get sentiment color
     */
    private static DeviceRgb getSentimentColor(String sentiment) {
        switch (sentiment) {
            case "Positive": return new DeviceRgb(76, 175, 80);   // Green
            case "Negative": return new DeviceRgb(244, 67, 54);   // Red
            default: return new DeviceRgb(158, 158, 158);         // Gray
        }
    }

    /**
     * Generate simple patient summary report
     */
    public static File generatePatientSummaryReport(Patient patient,
                                                    List<com.healthcare.model.PatientMedication> medications,
                                                    String outputPath) {
        try {
            File pdfFile = new File(outputPath);
            PdfWriter writer = new PdfWriter(pdfFile);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.setMargins(40, 40, 40, 40);

            // Title
            Paragraph title = new Paragraph("PATIENT MEDICAL SUMMARY")
                    .setFontSize(24)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(new DeviceRgb(33, 150, 243));
            document.add(title);

            Paragraph date = new Paragraph("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")))
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.GRAY)
                    .setMarginBottom(30);
            document.add(date);

            // Patient Info
            addSectionTitle(document, "PATIENT INFORMATION");
            Table patientTable = new Table(new float[]{1, 2});
            patientTable.setWidth(UnitValue.createPercentValue(100));

            addTableRow(patientTable, "Name:", patient.getFullName());
            addTableRow(patientTable, "Date of Birth:", patient.getDateOfBirth().toString());
            addTableRow(patientTable, "Age:", patient.getAge() + " years");
            addTableRow(patientTable, "Gender:", patient.getGender());
            addTableRow(patientTable, "Blood Group:", patient.getBloodGroup() != null ? patient.getBloodGroup() : "N/A");
            addTableRow(patientTable, "Phone:", patient.getPhone() != null ? patient.getPhone() : "N/A");
            addTableRow(patientTable, "Email:", patient.getEmail() != null ? patient.getEmail() : "N/A");

            document.add(patientTable);
            document.add(new Paragraph("\n"));

            // Current Medications
            addSectionTitle(document, "CURRENT MEDICATIONS");

            if (medications.isEmpty()) {
                document.add(new Paragraph("No active medications").setItalic().setFontColor(ColorConstants.GRAY));
            } else {
                Table medTable = new Table(new float[]{2, 1, 2, 1});
                medTable.setWidth(UnitValue.createPercentValue(100));

                medTable.addHeaderCell(createCell("Medication", true));
                medTable.addHeaderCell(createCell("Dosage", true));
                medTable.addHeaderCell(createCell("Frequency", true));
                medTable.addHeaderCell(createCell("Status", true));

                for (com.healthcare.model.PatientMedication med : medications) {
                    medTable.addCell(createCell(med.getMedicationName(), false));
                    medTable.addCell(createCell(med.getDosage(), false));
                    medTable.addCell(createCell(med.getFrequency(), false));
                    medTable.addCell(createCell(med.getStatus(), false));
                }

                document.add(medTable);
            }

            // Footer
            document.add(new Paragraph("\n\n"));
            Paragraph footer = new Paragraph("Confidential Medical Record\n" +
                    "Healthcare Management System\n" +
                    "Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .setFontSize(8)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.GRAY)
                    .setItalic();
            document.add(footer);

            document.close();

            System.out.println("✅ Patient summary PDF generated: " + pdfFile.getAbsolutePath());
            return pdfFile;

        } catch (Exception e) {
            System.err.println("❌ Error generating patient summary PDF: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}