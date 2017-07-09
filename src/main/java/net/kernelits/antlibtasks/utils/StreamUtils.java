package net.kernelits.antlibtasks.utils;

import org.jetbrains.annotations.Contract;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Funcoes utilitarias de stream
 *
 * @author Jayder França <a href="mailto:jayderfranca@gmail.com">jayderfranca@gmail.com</a>
 */
public class StreamUtils {

    // realiza a copia de um stream para o outro
    @Contract("null, _ -> fail; !null, null -> fail")
    public static void copy(InputStream in, OutputStream out) throws IOException {

        if (in == null)
            throw new IOException("Invalid InputStream");

        if (out == null)
            throw new IOException("Invalid OutputStream");

        // tamanho do buffer de copia: padrao 4K
        byte[] buffer = new byte[4 * 1024];

        // bytes copiados
        int size;

        // loop de copia de bytes
        while ((size = in.read(buffer)) >= 0)
            out.write(buffer, 0, size);

        // fecha os streams
        in.close();
        out.close();
    }
}
