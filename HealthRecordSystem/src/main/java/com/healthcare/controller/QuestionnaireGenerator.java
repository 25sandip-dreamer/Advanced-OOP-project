package com.healthcare.controller;

import java.util.ArrayList;
import java.util.List;

/**
 * QuestionnaireGenerator - Generates smart questions for medication assessment
 * Creates personalized questions based on medication type and assessment day
 */
public class QuestionnaireGenerator {

    /**
     * Question class to hold question data
     */
    public static class Question {
        private String questionText;
        private QuestionType type;
        private boolean requiresScore; // Does it need 1-10 rating?

        public enum QuestionType {
            ADHERENCE,      // Medication compliance
            SIDE_EFFECTS,   // Adverse reactions
            EFFICACY,       // How well it's working
            LIFESTYLE,      // Daily life impact
            OPEN_ENDED      // Free text
        }

        public Question(String questionText, QuestionType type, boolean requiresScore) {
            this.questionText = questionText;
            this.type = type;
            this.requiresScore = requiresScore;
        }

        public String getQuestionText() {
            return questionText;
        }

        public QuestionType getType() {
            return type;
        }

        public boolean requiresScore() {
            return requiresScore;
        }
    }

    /**
     * Generate questionnaire for Day 7 assessment
     * @param medicationName Name of the medication
     * @param assessmentDay Day of assessment (7, 14, 30, etc.)
     * @return List of questions
     */
    public static List<Question> generateQuestionnaire(String medicationName, int assessmentDay) {
        List<Question> questions = new ArrayList<>();

        // Standard questions for all medications
        questions.addAll(getStandardQuestions(assessmentDay));

        // Add medication-specific questions if needed
        questions.addAll(getMedicationSpecificQuestions(medicationName));

        return questions;
    }

    /**
     * Get standard questions that apply to all medications
     */
    private static List<Question> getStandardQuestions(int day) {
        List<Question> questions = new ArrayList<>();

        // Adherence Questions
        questions.add(new Question(
                "How many doses of your medication did you miss in the past " + day + " days?",
                Question.QuestionType.ADHERENCE,
                true
        ));

        questions.add(new Question(
                "Did you take your medication at the same time each day?",
                Question.QuestionType.ADHERENCE,
                false
        ));

        questions.add(new Question(
                "If you missed any doses, what was the main reason? (e.g., forgot, side effects, felt better)",
                Question.QuestionType.ADHERENCE,
                false
        ));

        // Side Effects Questions
        questions.add(new Question(
                "Have you experienced any side effects from this medication? If yes, please describe them.",
                Question.QuestionType.SIDE_EFFECTS,
                false
        ));

        questions.add(new Question(
                "On a scale of 1-10, how severe are any side effects you've experienced? (1 = no side effects, 10 = very severe)",
                Question.QuestionType.SIDE_EFFECTS,
                true
        ));

        questions.add(new Question(
                "Have the side effects improved, stayed the same, or gotten worse over the past week?",
                Question.QuestionType.SIDE_EFFECTS,
                false
        ));

        // Efficacy Questions
        questions.add(new Question(
                "On a scale of 1-10, how much have your symptoms improved since starting this medication? (1 = no improvement, 10 = completely resolved)",
                Question.QuestionType.EFFICACY,
                true
        ));

        questions.add(new Question(
                "Are you satisfied with how well this medication is working for you?",
                Question.QuestionType.EFFICACY,
                false
        ));

        questions.add(new Question(
                "Describe any changes you've noticed in your condition since starting this medication.",
                Question.QuestionType.EFFICACY,
                false
        ));

        // Lifestyle Impact Questions
        questions.add(new Question(
                "On a scale of 1-10, how much has this medication affected your daily activities? (1 = no impact, 10 = severely affected)",
                Question.QuestionType.LIFESTYLE,
                true
        ));

        questions.add(new Question(
                "Have you noticed any changes in your sleep, appetite, or energy levels?",
                Question.QuestionType.LIFESTYLE,
                false
        ));

        // Open-Ended Questions
        questions.add(new Question(
                "Is there anything else you'd like your doctor to know about your experience with this medication?",
                Question.QuestionType.OPEN_ENDED,
                false
        ));

        questions.add(new Question(
                "Do you have any concerns or questions about continuing this medication?",
                Question.QuestionType.OPEN_ENDED,
                false
        ));

        return questions;
    }

    /**
     * Get medication-specific questions based on drug type
     */
    private static List<Question> getMedicationSpecificQuestions(String medicationName) {
        List<Question> questions = new ArrayList<>();

        if (medicationName == null) return questions;

        String medLower = medicationName.toLowerCase();

        // Blood pressure medications
        if (medLower.contains("lisinopril") || medLower.contains("amlodipine") ||
                medLower.contains("atenolol")) {
            questions.add(new Question(
                    "Have you checked your blood pressure at home? If yes, what were the readings?",
                    Question.QuestionType.EFFICACY,
                    false
            ));
            questions.add(new Question(
                    "Have you experienced any dizziness when standing up?",
                    Question.QuestionType.SIDE_EFFECTS,
                    false
            ));
        }

        // Diabetes medications
        if (medLower.contains("metformin") || medLower.contains("insulin")) {
            questions.add(new Question(
                    "Have you been checking your blood sugar regularly? What have been your typical readings?",
                    Question.QuestionType.EFFICACY,
                    false
            ));
            questions.add(new Question(
                    "Have you experienced any episodes of low blood sugar (hypoglycemia)?",
                    Question.QuestionType.SIDE_EFFECTS,
                    false
            ));
        }

        // Antibiotics
        if (medLower.contains("amoxicillin") || medLower.contains("azithromycin")) {
            questions.add(new Question(
                    "Have your infection symptoms improved since starting the antibiotic?",
                    Question.QuestionType.EFFICACY,
                    false
            ));
            questions.add(new Question(
                    "Have you experienced any stomach upset or diarrhea?",
                    Question.QuestionType.SIDE_EFFECTS,
                    false
            ));
        }

        // Pain medications
        if (medLower.contains("ibuprofen") || medLower.contains("naproxen")) {
            questions.add(new Question(
                    "On a scale of 1-10, what is your current pain level compared to before starting medication?",
                    Question.QuestionType.EFFICACY,
                    true
            ));
            questions.add(new Question(
                    "Have you noticed any stomach discomfort or heartburn?",
                    Question.QuestionType.SIDE_EFFECTS,
                    false
            ));
        }

        // Antidepressants/Anxiety medications
        if (medLower.contains("sertraline") || medLower.contains("fluoxetine") ||
                medLower.contains("escitalopram")) {
            questions.add(new Question(
                    "On a scale of 1-10, how would you rate your mood over the past week?",
                    Question.QuestionType.EFFICACY,
                    true
            ));
            questions.add(new Question(
                    "Have you noticed any changes in your sleep patterns or energy levels?",
                    Question.QuestionType.SIDE_EFFECTS,
                    false
            ));
        }

        // Thyroid medications
        if (medLower.contains("levothyroxine")) {
            questions.add(new Question(
                    "Have you noticed changes in your energy level, weight, or temperature sensitivity?",
                    Question.QuestionType.EFFICACY,
                    false
            ));
        }

        // Statins (cholesterol)
        if (medLower.contains("atorvastatin") || medLower.contains("simvastatin")) {
            questions.add(new Question(
                    "Have you experienced any unusual muscle pain, weakness, or cramping?",
                    Question.QuestionType.SIDE_EFFECTS,
                    false
            ));
        }

        return questions;
    }

    /**
     * Get a quick assessment questionnaire (fewer questions)
     */
    public static List<Question> getQuickAssessment(String medicationName) {
        List<Question> questions = new ArrayList<>();

        questions.add(new Question(
                "How many doses did you miss this week?",
                Question.QuestionType.ADHERENCE,
                true
        ));

        questions.add(new Question(
                "Rate any side effects (1-10, 1=none, 10=severe)",
                Question.QuestionType.SIDE_EFFECTS,
                true
        ));

        questions.add(new Question(
                "Rate symptom improvement (1-10, 1=no improvement, 10=fully resolved)",
                Question.QuestionType.EFFICACY,
                true
        ));

        questions.add(new Question(
                "Describe how you're feeling overall on this medication",
                Question.QuestionType.OPEN_ENDED,
                false
        ));

        return questions;
    }
}