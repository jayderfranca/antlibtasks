package net.kernelits.antlibtasks.utils;

/**
 * Funcoes utilitarias de gerais
 *
 * @author Jayder França <a href="mailto:jayderfranca@gmail.com">jayderfranca@gmail.com</a>
 */
public class CommonUtils {

    // valida se um determinado objeto esta na lista informada
    public static <T> boolean isInList(T value, T... list) {

        for (T item : list)
            if (item.equals(value))
                return true;

        return false;
    }
}
