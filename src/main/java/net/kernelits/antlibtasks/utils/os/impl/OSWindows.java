package net.kernelits.antlibtasks.utils.os.impl;

import jcifs.UniAddress;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileOutputStream;
import jcifs.smb.SmbSession;
import net.kernelits.antlibtasks.ant.exceptions.NotImplementedException;
import net.kernelits.antlibtasks.utils.os.IOperationalSystem;
import net.kernelits.antlibtasks.utils.os.OSConnectParameters;
import net.kernelits.antlibtasks.utils.pwd.Password;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;

public class OSWindows implements IOperationalSystem {

    // variaveis jcifs
    NtlmPasswordAuthentication auth = null;
    String hostname = "";

    // metodo de conexao com a maquina remota
    public void connect(OSConnectParameters parameters) throws Exception {

        // cria a autenticacao
        UniAddress domain = UniAddress.getByName(parameters.domain, true);
        auth = new NtlmPasswordAuthentication(parameters.domain, parameters.username, Password.getPasswordText(parameters.password));
        SmbSession.logon(domain, auth);
        hostname = parameters.hostname;
    }

    // metodo para copia remota
    public void copy(File fromLocal, String toRemote) throws Exception {

        // verifica se o arquivo existe
        if (!fromLocal.exists())
            throw new FileNotFoundException("File not found: " + fromLocal.getAbsolutePath());

        // verifica se o objeto session foi criado
        if (auth == null)
            throw new NullPointerException("Object auth is null, try to call connect first");

        // define o arquivo de origem e destino
        File fromFile = fromLocal;
        SmbFile toFile = new SmbFile("smb://" + hostname + toRemote + fromFile.getName(), auth);

        // abertura dos streams
        FileInputStream in = new FileInputStream(fromFile);
        SmbFileOutputStream out = new SmbFileOutputStream(toFile);

        // grava o conteudo
        byte[] buffer = new byte[8192];
        int bytes = 0;
        while ((bytes = in.read(buffer)) > 0) {
            out.write(buffer, 0, bytes);
        }

        // fecha o arquivo de entrada
        in.close();

        // fecha o arquivo gravado
        out.flush();
        out.close();
    }

    // metodo para copia local
    public void copy(String fromRemote, File toLocal) throws Exception {
        throw new NotImplementedException();
    }

    // metodo para listar os arquivo remotos em um determinado caminho
    public String[] list(String dirRemote) throws Exception {

        // variavel com a lista de arquivos encontrados
        ArrayList<String> list = new ArrayList<String>();

        // verifica se o objeto session foi criado
        if (auth == null)
            throw new NullPointerException("Object auth is null, try to call connect first");

        // diretorio remoto
        SmbFile remote = new SmbFile("smb://" + hostname + dirRemote, auth);

        // verifica se o diretorio existe
        if (remote.exists() && remote.isDirectory()) {

            // lista os arquivos do diretorio remoto
            list.addAll(Arrays.asList(remote.list()));
        }

        // retorna a lista
        return list.size() > 0 ? list.toArray(new String[list.size()]) : null;
    }

    // metodo para listar arquivos da maquina
    public String[] list(File dirLocal) throws Exception {
        throw new NotImplementedException();
    }

    // metodo para remover um arquivo remoto
    public void remove(String fileRemote) throws Exception {

        // verifica se o objeto session foi criado
        if (auth == null)
            throw new NullPointerException("Object auth is null, try to call connect first");

        // arquivo remoto
        SmbFile remote = new SmbFile("smb://" + hostname + fileRemote, auth);

        // verifica se existe o arquivo para exclusao
        if (remote.exists() && remote.isFile() && remote.canWrite())
            remote.delete();
    }

    // metodo para remover arquivo local
    public void remove(File fileLocal) throws Exception {
        throw new NotImplementedException();
    }

    // metodo para finalizar a conexao
    public void disconnect() throws Exception {
        throw new NotImplementedException();
    }
}
