package net.seesharpsoft.intellij.plugins.csv.intention;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import net.seesharpsoft.intellij.plugins.csv.CsvColumnInfo;
import net.seesharpsoft.intellij.plugins.csv.settings.CsvCodeStyleSettings;
import net.seesharpsoft.intellij.plugins.csv.psi.CsvFile;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class CsvShiftColumnIntentionAction extends CsvIntentionAction {

    protected CsvShiftColumnIntentionAction(String text) {
        super(text);
    }

    protected static void changeLeftAndRightColumnOrder(@NotNull Project project, CsvFile psiFile, CsvColumnInfo<PsiElement> leftColumnInfo, CsvColumnInfo<PsiElement> rightColumnInfo) {
        Document document = PsiDocumentManager.getInstance(project).getDocument(psiFile);
        document.setText(changeLeftAndRightColumnOrder(document.getText(), CsvCodeStyleSettings.getCurrentSeparator(project, psiFile.getLanguage()), leftColumnInfo, rightColumnInfo));
    }

    @NotNull
    protected static String changeLeftAndRightColumnOrder(String text, String separator, CsvColumnInfo<PsiElement> leftColumnInfo, CsvColumnInfo<PsiElement>  rightColumnInfo) {
        List<PsiElement> rightElements = rightColumnInfo.getElements();
        List<PsiElement> leftElements = leftColumnInfo.getElements();
        int lastIndex = 0, maxRows = leftElements.size();
        StringBuilder newText = new StringBuilder();

        for (int row = 0; row < maxRows; ++row) {
            PsiElement leftElement = leftElements.get(row);
            if (leftElement == null) {
                continue;
            }
            PsiElement rightElement = rightElements.size() > row ? rightElements.get(row) : null;

            TextRange leftSeparator = findPreviousSeparatorOrCRLF(leftElement);
            TextRange middleSeparator = findNextSeparatorOrCRLF(leftElement);
            TextRange rightSeparator = rightElement == null ?
                    TextRange.create(middleSeparator.getEndOffset(), middleSeparator.getEndOffset()) :
                    findNextSeparatorOrCRLF(rightElement);

            newText.append(text.substring(lastIndex, leftSeparator.getEndOffset()))
                    .append(text.substring(middleSeparator.getEndOffset(), rightSeparator.getStartOffset()))
                    .append(separator)
                    .append(text.substring(leftSeparator.getEndOffset(), middleSeparator.getStartOffset()));

            lastIndex = rightSeparator.getStartOffset();
        }
        newText.append(text.substring(lastIndex));
        return newText.toString();
    }

    protected static TextRange findPreviousSeparatorOrCRLF(PsiElement psiElement) {
        TextRange textRange;
        PsiElement separator = CsvIntentionHelper.getPreviousSeparator(psiElement);
        if (separator == null) {
            separator = CsvIntentionHelper.getPreviousCRLF(psiElement.getParent());
            if (separator == null) {
                separator = psiElement.getParent().getParent().getFirstChild();
                textRange = TextRange.create(separator.getTextRange().getStartOffset(), separator.getTextRange().getStartOffset());
            } else {
                textRange = separator.getTextRange();
            }
        } else {
            textRange = separator.getTextRange();
        }
        return textRange;
    }

    protected static TextRange findNextSeparatorOrCRLF(PsiElement psiElement) {
        TextRange textRange;
        PsiElement separator = CsvIntentionHelper.getNextSeparator(psiElement);
        if (separator == null) {
            separator = CsvIntentionHelper.getNextCRLF(psiElement.getParent());
            if (separator == null) {
                separator = psiElement.getParent().getParent().getLastChild();
                textRange = TextRange.create(separator.getTextRange().getEndOffset(), separator.getTextRange().getEndOffset());
            } else {
                textRange = TextRange.create(separator.getTextRange().getStartOffset(), separator.getTextRange().getStartOffset());
            }
        } else {
            textRange = separator.getTextRange();
        }
        return textRange;
    }
}
