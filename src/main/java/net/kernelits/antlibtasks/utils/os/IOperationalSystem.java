package net.kernelits.antlibtasks.utils.os;

import java.io.File;

public interface IOperationalSystem {

    // metodo de conexao com a maquina remota
    public void connect(OSConnectParameters parameters) throws Exception;

    // metodo para copia remota
    public void copy(File fromLocal, String toRemote) throws Exception;

    // metodo para copia local
    public void copy(String fromRemote, File toLocal) throws Exception;

    // metodo para listar os arquivo remotos em um determinado caminho
    public String[] list(String dirRemote) throws Exception;

    // metodo para listar os arquivo da maquina em um determinado caminho
    public String[] list(File dirLocal) throws Exception;

    // metodo para remover um arquivo remoto
    public void remove(String fileRemote) throws Exception;

    // metodo para remover arquivo local
    public void remove(File fileLocal) throws Exception;

    // metodo para finalizar a conexao
    public void disconnect() throws Exception;
}
