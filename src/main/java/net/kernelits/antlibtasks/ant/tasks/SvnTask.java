package net.kernelits.antlibtasks.ant.tasks;

import net.kernelits.antlibtasks.ant.commands.Command;
import net.kernelits.antlibtasks.ant.commands.svn.Revision;
import net.kernelits.antlibtasks.ant.commands.svn.Update;
import org.apache.tools.ant.BuildException;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.commandline.CmdLineClientAdapterFactory;

/**
 * Task para operacoes de svn
 *
 * @author Jayder França <a href="mailto:jayderfranca@gmail.com">jayderfranca@gmail.com</a>
 */
public class SvnTask extends Task {

    // variaveis
    private ISVNClientAdapter client = null;
    private String username = null;
    private String password = null;

    // sets
    public void setUsername(String value) {
        username = value;
    }

    public String getUsername() {
        return username;
    }

    public void setPassword(String value) {
        password = value;
    }

    public String getPassword() {
        return password;
    }

    // adiciona os comandos
    public void addUpdate(Update update) {
        addCommand(update);
    }

    public void addRevision(Revision revision) {
        addCommand(revision);
    }

    /* (non-Javadoc)
     * @see org.apache.tools.ant.Task#execute()
     */
    @Override
    public void execute() throws BuildException {

        for (Command command : getCommands()) {
            command.execute();
        }
    }

    // obtem o cliente de svn
    public ISVNClientAdapter getClient() {

        // verifica se nao foi criado o
        // client de svn
        if (client == null) {

            // verifica a disponibilidade do cliente
            if (isAvaliable()) {

                // cria um novo cliente de svn
                client = SVNClientAdapterFactory.createSVNClient(CmdLineClientAdapterFactory.COMMANDLINE_CLIENT);
            } else {

                // caso nao tenha disponibilidade, lanca uma exception
                throw new BuildException("Linha de comando do subversion nao encontrada");
            }
        }

        // caso ja tenha sido criado,
        // apenas retorna o objeto
        return client;
    }

    // verifica se a linha de comando do svn esta disponivel
    private boolean isAvaliable() {

        // verifica se nao foi criado o cliente de svn
        if (client == null) {

            // inicializa o factory da linha de comando
            // em caso de erro ignora.
            try {
                if (!SVNClientAdapterFactory.isSVNClientAvailable(CmdLineClientAdapterFactory.COMMANDLINE_CLIENT))
                    CmdLineClientAdapterFactory.setup();
            } catch (SVNClientException ex) {
                throw new BuildException("Erro ao inicializar a linha de comando do subversion", ex);
            }

            // verifica a disponibilidade da linha de comando
            return SVNClientAdapterFactory.isSVNClientAvailable(CmdLineClientAdapterFactory.COMMANDLINE_CLIENT);

        } else {

            // como o cliente ja foi criado nao existe
            // a necessidade de inicializar o factory
            // e nem se a linha de comando esta disponivel
            return true;
        }
    }
}
