package com.example.ragapplication;

public class PromptManager {
    public static String getPrompt(String query, String context) {
        String languageCode = SettingsStore.languageCode;
        String prompt = "";

        if (languageCode.equals(LanguageManager.ENGLISH_CODE)) {
            prompt = "You will be tasked to answer questions related to a specific domain." +
                    " Try answering the following question :\n" +
                    "Question : " + query + "\n" +
                    "You might find the following context useful to answer the question :\n" +
                    "Context : \n" + context + "\n" +
                    "Answer: ";
        } else if (languageCode.equals(LanguageManager.FRENCH_CODE)) {
            prompt = "Vous serez chargé de répondre à des questions liées à un domaine spécifique. " +
                    "Essayez de répondre à la question suivante :\n" +
                    "Question : " + query + "\n" +
                    "Vous pourriez trouver le contexte suivant utile pour répondre à la question :\n" +
                    "Contexte : \n" + context + "\n" +
                    "Réponse : ";
        }

        return prompt;
    }
}
