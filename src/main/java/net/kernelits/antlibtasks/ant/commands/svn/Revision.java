package net.kernelits.antlibtasks.ant.commands.svn;

import net.kernelits.antlibtasks.ant.commands.Command;
import net.kernelits.antlibtasks.ant.tasks.SvnTask;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Property;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNClientException;

import java.io.File;

/**
 * Task de atualizacao de uma Workcopy do svn
 *
 * @author Jayder França <a href="mailto:jayderfranca@gmail.com">jayderfranca@gmail.com</a>
 */
public class Revision extends Command {

    // variaveis
    private File source = null;
    private String property = null;

    // sets
    public void setSource(File value) {
        source = value;
    }

    public void setProperty(String value) {
        property = value;
    }

    @Override
    public void execute() throws BuildException {

        // numero da revisao
        Long revision = null;

        // obtem a task de svn
        SvnTask task = (SvnTask) getTask();

        // obtem o cliente de svn
        ISVNClientAdapter client = task.getClient();

        try {

            // obtem a ultima revisao do workcopy
            revision = client.getInfoFromWorkingCopy(source).getRevision().getNumber();

        } catch (SVNClientException ex) {

            // log de erro
            log.info("Nao foi possivel obter a revisao do souce " + source.getAbsolutePath());
        }

        // cria a propriedade
        Property newProperty = (Property) getProject().createTask("property");
        newProperty.setName(property);

        // verifica o retorno da revisao
        if (revision != null && revision > 0)
            // cria a propriedade com a revisao
            newProperty.setValue(String.valueOf(revision));
        else
            // cria um valor invalido
            newProperty.setValue("-1");

        // cria a nova propriedade
        newProperty.execute();
    }

    public void validate() throws BuildException {

        // realiza as validacoes nos parametros
        if (source == null || "".equalsIgnoreCase(source.getAbsolutePath()))
            buildException("Parametro source nao informado");

        if (property == null || "".equalsIgnoreCase(property))
            buildException("Parametro property nao informado");
    }
}
