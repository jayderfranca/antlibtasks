package net.kernelits.antlibtasks.utils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

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
    @Contract("null -> false")
    public static boolean delete(File item) {

        // verifica se o item eh nulo
        if (item == null)
            return false;

        // caso o item seja um diretorio,
        // remove primeiramente os arquivos e
        // depois o diretorio
        if (item.isDirectory()) {

            // lista todas as entradas
            File[] entries = item.listFiles();

            // validacao de null
            if (entries == null)
                entries = new File[0];

            // remocao recursiva
            for (File entry : entries)
                if (!delete(entry))
                    return false;
        }

        // remove o item em si
        return item.delete();
    }

    // lista arquivos/diretorios recursivamente
    @NotNull
    public static File[] list(File dir, FileFilter filter) {

        // verifica se o diretorio eh nulo
        if (dir == null)
            return new File[0];

        // array de retorno da lista de arquivos
        ArrayList<File> list = new ArrayList<File>();

        // lista todos os arquivos e subdiretorios
        File[] files;

        // em caso de arquivo, nao aciona list
        if (!dir.isDirectory()) {

            // cria um unico indice
            files = new File[1];

            // adiciona o proprio arquivo
            files[0] = dir;

        } else {

            // lista todos arquivos e subdiretorios
            files = dir.listFiles();

            // validacao de null
            if (files == null)
                files = new File[0];
        }

        // loop de cada arquivo
        for (File file : files) {

            // verifica se eh um diretorio e vai listar recursivamente
            if (file.isDirectory()) {

                // realiza a chamada novamente da funcao
                File[] sublist = list(file, filter);

                // verifica se a sublista possui itens
                if (sublist.length > 0)

                    // acrescenta na lista principal
                    list.addAll(Arrays.asList(sublist));

            } else {

                // nao eh um diretorio ou nao eh recursivo

                // verifica se foi informado um filter
                if (filter != null) {

                    // verifica se o item eh aceito pelo filter
                    if (filter.accept(file))

                        // o item eh aceito, adiciona na lista
                        list.add(file);

                } else {

                    // nao possui filter, adiciona
                    list.add(file);
                }
            }
        }

        // retorno da lista
        return list.toArray(new File[list.size()]);
    }

    // descompacta um arquivo compactado
    public static void unzip(File zip, File dest, boolean replace) throws IOException {

        File entry;

        // possibilidade de excluir o destino
        if (dest.exists() && !replace)
            throw new IOException("Destination" + (dest.isDirectory() ? "directory " : "file ") + dest.getAbsolutePath() + " already exists");

        // exclui o destino caso exista
        if (dest.exists())
            delete(dest);

        // cria o diretorio de destino
        if (!dest.mkdirs())
            throw new IOException("Cannot create directories from path " + dest.getAbsolutePath());

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
                if (entry.mkdirs())
                    throw new IOException("Cannot create directories from path " + entry.getAbsolutePath());

            else {

                // cria diretorios caso seja necessario
                if (entry.getParentFile().mkdirs())
                    throw new IOException("Cannot create directories from path " + entry.getParentFile().getAbsolutePath());

                // neste caso eh um arquivo e continua a extracao
                StreamUtils.copy(zipFile.getInputStream(zipEntry), new FileOutputStream(entry));
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
    public static List<String> read(File file) throws IOException {

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
