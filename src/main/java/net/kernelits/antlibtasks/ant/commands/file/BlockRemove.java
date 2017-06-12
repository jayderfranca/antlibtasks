package net.kernelits.antlibtasks.ant.commands.file;

import net.kernelits.antlibtasks.ant.commands.Command;
import net.kernelits.antlibtasks.ant.tasks.FileTask;
import org.apache.tools.ant.BuildException;

import java.io.*;

/**
 * Task que remove um determinado trecho do arquivo
 *
 * @author Jayder França <a href="mailto:jayderfranca@gmail.com">jayderfranca@gmail.com</a>
 */
public class BlockRemove extends Command {

    // variaveis
    private String beginMarker;
    private String endMarker;

    // sets
    public void setBeginMarker(String beginMarker) {
        this.beginMarker = beginMarker;
    }

    public void setEndMarker(String endMarker) {
        this.endMarker = endMarker;
    }

    @Override
    public void execute() throws BuildException {

        // obtem a task file
        FileTask task = (FileTask) getTask();

        try {

            // realiza a leitura do conteudo do arquivo
            String content = read(task.getIn());

            // procura pela marcacao inicial
            int indexBegin = content.indexOf(beginMarker);

            // procura pela marcacao final
            int indexEnd = content.indexOf(endMarker);

            // em caso de encontrar a marcacao inicial
            // devera ter uma marcao final
            if (indexBegin > 0 && indexEnd > 0) {

                // grava o conteudo sem a marcacao
                content = content.substring(0, indexBegin) + content.substring(indexEnd + endMarker.length());
                write(content, task.getOut());

            } else if (indexBegin > 0 || indexEnd > 0) {

                // informado o bloco inicial mas nao o final ou inverso
                if (indexBegin > 0 && indexEnd <= 0)
                    buildException("Encontrado beginmarker sem endmarker");

                // informado o bloco inicial mas nao o final ou inverso
                if (indexBegin <= 0 && indexEnd > 0)
                    buildException("Encontrado endmarker sem beginmarker");
            }
        } catch (Exception e) {
            buildException(e.getMessage(), e);
        }
    }

    private void write(String content, File output) throws IOException {

        FileOutputStream out = null;

        try {
            out = new FileOutputStream(output);
            out.write(content.getBytes());
        } finally {

            if (out != null) {
                out.flush();
                out.close();
            }
        }
    }

    private String read(File input) throws IOException {

        FileInputStream in = null;

        try {

            ByteArrayOutputStream str = new ByteArrayOutputStream();

            in = new FileInputStream(input);

            int read = 0;
            byte[] bytes = new byte[1024];

            while ((read = in.read(bytes)) > 0)
                str.write(bytes, 0, read);

            return str.toString();
        } finally {

            if (in != null)
                in.close();
        }
    }
}
