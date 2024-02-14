package com.example.ragapplication;

public class Markdown2HTML {
    public static String markdownToHtml(String markdownText) {
        // Replace **text** with <b>text</b> (bold)
        markdownText = markdownText.replaceAll("\\*\\*(.*?)\\*\\*", "<b>$1</b>");

        // Replace *text* with <i>text</i> (italic)
        markdownText = markdownText.replaceAll("\\*(.*?)\\*", "<i>$1</i>");

        // Replace `text` with <code>text</code> (monospace)
        markdownText = markdownText.replaceAll("`(.*?)`", "<code>$1</code>");

        // Replace [text](url) with <a href="url">text</a> (hyperlink)
        markdownText = markdownText.replaceAll("\\[(.*?)\\]\\((.*?)\\)", "<a href=\"$2\">$1</a>");

        // Replace newlines with <br> (line break)
        markdownText = markdownText.replaceAll("\n", "<br>");

        return markdownText;
    }
}
