package net.kernelits.antlibtasks.utils;

/**
 * Funcoes utilitarias de string
 *
 * @author Jayder França <a href="mailto:jayderfranca@gmail.com">jayderfranca@gmail.com</a>
 */
public class StringUtils {

    // remove a acentuacao
    public static String normalizeString(String src) {
        // Normalizer.normalise() converts each accented
        // character into 1 non-accented character followed
        // by 1 or more characters representing the accent(s)
        // alone. These characters representing only
        // an accent belong to the Unicode category
        // CombiningDiacriticalMarks. The call to replaceAll
        // strips out all characters in that category.
        @SuppressWarnings("restriction")
        String normalized = sun.text.Normalizer.normalize(
                src,
                sun.text.Normalizer.DECOMP_COMPAT, // Normalizer.Form.NFKD in 1.6
                0);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    // realiza o join de uma lista de strings com um delimitador
    public static String join(String[] elements, String demiliter) {

        StringBuffer buffer = new StringBuffer();

        for (String element : elements) {

            buffer.append(element);
            buffer.append(demiliter);
        }

        buffer.deleteCharAt(buffer.length() - 1);
        return buffer.toString();
    }
}
