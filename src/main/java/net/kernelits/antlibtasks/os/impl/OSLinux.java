package net.kernelits.antlibtasks.os.impl;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import net.kernelits.antlibtasks.ant.exceptions.NotImplementedException;
import net.kernelits.antlibtasks.os.IOperationalSystem;
import net.kernelits.antlibtasks.os.OSConnectParameters;
import net.kernelits.antlibtasks.pwd.Password;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Classe de operacoes para um host Linux
 *
 * @author Jayder França <a href="mailto:jayderfranca@gmail.com">jayderfranca@gmail.com</a>
 */
public class OSLinux implements IOperationalSystem {

    // variavel ssh
    JSch ssh = null;
    Session session = null;
    File filePubKeyTemp = null;

    // propriedades da sessao
    private static Hashtable<String, String> config = new Hashtable<String, String>();

    // atribuicao das propriedades
    static {

        // nao verifica o host
        config.put("StrictHostKeyChecking", "no");
    }

    public OSLinux() {

        // atribui as propriedades
        JSch.setConfig(config);

        // cria o objeto de ssh
        ssh = new JSch();
    }

    // metodo que cria um arquivo temporario do inputstream informado
    private File createTemporaryFilePublicKey(InputStream pubKey) throws Exception {

        // cria um arquivo temporario de stream para ser informado no identity
        File filePubKeyTemp = File.createTempFile("java_", ".tmp");
        FileOutputStream outStream = new FileOutputStream(filePubKeyTemp);

        // grava o conteudo
        byte[] buffer = new byte[1024];
        int bytes = 0;
        while ((bytes = pubKey.read(buffer)) > 0) {
            outStream.write(buffer, 0, bytes);
        }

        // fecha o arquivo gravado
        outStream.flush();
        outStream.close();

        // retorna o nome do arquivo
        return filePubKeyTemp;
    }

    // metodo de conexao com a maquina remota
    public void connect(OSConnectParameters parameters) throws Exception {

        // verifica as condicoes
        if (parameters.publicKey != null)

            // atribui a public key
            ssh.addIdentity(parameters.publicKey.getAbsolutePath());

        else if (parameters.streamPublicKey != null) {

            // cria o arquivo temporario da chave
            filePubKeyTemp = createTemporaryFilePublicKey(parameters.streamPublicKey);

            // atribui a public key do arquivo temporario
            ssh.addIdentity(filePubKeyTemp.getAbsolutePath());
        }

        // cria a sessao de acordo com as informacoes
        session = ssh.getSession(parameters.username, parameters.hostname, parameters.port);

        if (parameters.publicKey == null && parameters.streamPublicKey == null)
            // somente no caso de nao ter sido informado
            // o arquivo da chave publica informa a senha
            session.setPassword(Password.getPasswordText(parameters.password));
    }

    // metodo para copia remota
    public void copy(File fromLocal, String toRemote) throws Exception {

        // verifica se o arquivo existe
        if (!fromLocal.exists())
            throw new FileNotFoundException("File not found: " + fromLocal.getAbsolutePath());

        // verifica se o objeto session foi criado
        if (session == null)
            throw new NullPointerException("Object session is null, try to call connect first");

        // conecta com a maquina remota
        if (!session.isConnected())
            session.connect();

        // define o canal de comunicacao sftp para copia
        Channel channel = session.openChannel("sftp");
        channel.connect();
        ChannelSftp sftp = (ChannelSftp) channel;

        // inicia a copia do arquivo
        sftp.put(fromLocal.getAbsolutePath(), toRemote, ChannelSftp.OVERWRITE);

        // apos o termino finaliza
        sftp.disconnect();
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
        if (session == null)
            throw new NullPointerException("Object session is null, try to call connect first");

        // conecta com a maquina remota
        if (!session.isConnected())
            session.connect();

        // define o canal de comunicacao sftp para copia
        Channel channel = session.openChannel("sftp");
        channel.connect();
        ChannelSftp sftp = (ChannelSftp) channel;

        // lista os arquivos
        @SuppressWarnings("unchecked")
        Vector<ChannelSftp.LsEntry> entries = (Vector<ChannelSftp.LsEntry>) sftp.ls(dirRemote);

        // loop dos arquivos encontrados
        for (ChannelSftp.LsEntry entry : entries) {

            if (entry.getFilename().equals(".") || entry.getFilename().equals(".."))
                continue;

            // adiciona na lista de retorno
            list.add(entry.getFilename());
        }

        // apos o termino finaliza
        sftp.disconnect();

        // retorno da lista
        return list.size() > 0 ? list.toArray(new String[list.size()]) : null;
    }

    // metodo para listar arquivos da maquina
    public String[] list(File dirLocal) throws Exception {
        throw new NotImplementedException();
    }

    // metodo para remover um arquivo remoto
    public void remove(String fileRemote) throws Exception {

        // verifica se o objeto session foi criado
        if (session == null)
            throw new NullPointerException("Object session is null, try to call connect first");

        // conecta com a maquina remota
        if (!session.isConnected())
            session.connect();

        // define o canal de comunicacao sftp para copia
        Channel channel = session.openChannel("sftp");
        channel.connect();
        ChannelSftp sftp = (ChannelSftp) channel;

        // remove o arquivo remoto
        sftp.rm(fileRemote);

        // apos o termino finaliza
        sftp.disconnect();
    }

    // metodo para remover arquivo local
    public void remove(File fileLocal) throws Exception {
        throw new NotImplementedException();
    }

    // metodo para finalizar a conexao
    public void disconnect() throws Exception {

        // caso tenha sido criado um arquivo temporario
        // com a chave, apaga este arquivo
        if (filePubKeyTemp != null)
            filePubKeyTemp.delete();

        // desconecta da sessao
        session.disconnect();
    }
}
