package net.kernelits.antlibtasks.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Funcoes utilitarias de arquivo
 *
 * @author Jayder França <a href="mailto:jayderfranca@gmail.com">jayderfranca@gmail.com</a>
 */
public class FileUtils {

    // exclui arquivos e diretorios recursivamente
    public static boolean dirDelete(File directory, boolean recursive) {

        // verifica se o diretorio eh nulo
        if (directory == null)
            throw new NullPointerException();

        if (recursive) {
            if (directory.isDirectory()) {
                File[] files = directory.listFiles();

                // validacao de null pointer
                if (files == null)
                    files = new File[0];

                for (File file : files) {
                    if (!dirDelete(file, true))
                        return false;
                }
            }
        }
        return directory.delete();
    }

    // lista arquivos/diretorios recursivamente
    public static File[] listFiles(File dir, boolean recursive, FileFilter filter) {

        // verifica se o diretorio eh nulo
        if (dir == null)
            throw new NullPointerException();

        // array de retorno da lista de arquivos
        ArrayList<File> list = new ArrayList<File>();

        // lista todos arquivos e subdiretorios
        File[] files = dir.listFiles();

        // validacao de null pointer
        if (files == null)
            files = new File[0];

        // loop de cada arquivo
        for (File file : files) {

            // verifica se eh um diretorio e vai listar recursivamente
            if (file.isDirectory() && recursive) {

                // realiza a chamada novamente da funcao
                File[] sublist = listFiles(file, true, filter);

                // verifica se a sublista possui itens
                if (sublist.length > 0) {

                    // acrescenta na lista principal
                    list.addAll(Arrays.asList(sublist));
                }
            } else {

                // nao eh um diretorio ou nao eh recursivo

                // verifica se foi informado um filter
                if (filter != null) {

                    // verifica se o item eh aceito pelo filter
                    if (filter.accept(file)) {

                        // o item eh aceito, adiciona na lista
                        list.add(file);
                    }
                } else {

                    // nao possui filter, adiciona
                    list.add(file);
                }
            }
        }

        // retorno da lista
        return list.toArray(new File[list.size()]);
    }

    // realiza a copia de um stream para o outro
    private static void copyStream(InputStream in, OutputStream out) throws IOException {

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

    // descompacta um arquivo compactado
    public static void unzip(File zip, File dest, boolean replace) throws IOException {

        File entry;

        // exclui o destino ou lanca erro
        if (dest.exists() && replace)
            dirDelete(dest, true);
        else if (dest.exists() && !replace)
            throw new IOException("Destination" + (dest.isDirectory() ? "directory " : "file ") + dest.getAbsolutePath() + " already exists");

        // cria o diretorio de destino
        dest.mkdirs();

        // abre o arquivo jar
        ZipFile zipFile = new ZipFile(zip);

        // lista as entradas do arquivo
        Enumeration<? extends ZipEntry> entries = zipFile.entries();

        // percorre cada entrada para extracao
        while (entries.hasMoreElements()) {

            // obtem a entrada
            ZipEntry zipEntry = entries.nextElement();

            // converte em file para localizacao no disco a entrada do zip
            entry = new File(dest, zipEntry.getName());

            if (zipEntry.isDirectory() && !entry.exists())

                // se for um diretorio cria e continua
                entry.mkdirs();

            else {

                // cria diretorios caso seja necessario
                entry.getParentFile().mkdirs();

                // neste caso eh um arquivo e continua a extracao
                copyStream(zipFile.getInputStream(zipEntry), new FileOutputStream(entry));
            }
        }

        // fecha o arquivo
        zipFile.close();
    }

    // retorna a extensao do nome do arquivo
    public static String getFileExtension(File file) {

        // extensao
        String ext;

        // obtem o separador do sistema operacional de caminho
        String sep = System.getProperty("file.separator");

        // converte para string o nome do arquivo
        String filename;

        // obtem o ultimo index do separador de arquivos
        int last = file.getName().lastIndexOf(sep);

        // caso nao encontre, ja eh o nome do arquivo
        if (last == -1)
            filename = file.getName();
        else
            // retorna o nome do arquivo
            filename = file.getName().substring(last + 1);

        // procura pela extensao do nome do arquivo
        last = filename.lastIndexOf(".");

        // caso nao tenha, retorna vazio
        if (last == -1)
            ext = "";
        else
            // retorna a extensao do arquivo
            ext = filename.substring(last + 1);

        // retorno da extensao
        return ext;
    }

    // realiza a leitura de um arquivo para um array de strings
    public static List<String> readAll(File file) throws IOException {

        ArrayList<String> lines = new ArrayList<String>();

        FileReader reader = new FileReader(file);
        BufferedReader buffer = new BufferedReader(reader);

        String line;

        while ((line = buffer.readLine()) != null)
            lines.add(line);

        buffer.close();

        return lines;
    }
}
