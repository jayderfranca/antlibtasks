package net.kernelits.antlibtasks.ant.commands.svn;

//import java.io.BufferedWriter;

import net.kernelits.antlibtasks.ant.commands.Command;
import net.kernelits.antlibtasks.ant.tasks.SvnTask;
import net.kernelits.antlibtasks.pwd.Password;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.tigris.subversion.svnclientadapter.*;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * Task de atualizacao de uma Workcopy do svn
 *
 * @author Jayder França <a href="mailto:jayderfranca@gmail.com">jayderfranca@gmail.com</a>
 */
public class Update extends Command {

    // variaveis
    private SVNUrl url = null;
    private File dest = null;
    private boolean showUpdateLog = true;
    private File changeLog = null;

    // sets
    public void setUrl(SVNUrl value) {
        url = value;
    }

    public void setDest(File value) {
        dest = value;
    }

    public void setShowUpdateLog(boolean value) {
        showUpdateLog = value;
    }

    public void setChangeLog(File value) {
        changeLog = value;
    }

    @Override
    public void execute() throws BuildException {

        // obtem a task de svn
        SvnTask task = (SvnTask) getTask();

        // obtem o cliente de svn
        ISVNClientAdapter client = task.getClient();

        // verifica se o usuario foi informado
        if ("".equals(task.getUsername()))
            buildException("Usuario para o svn nao foi informado");

        // atribui o usuario
        client.setUsername(task.getUsername());

        // verifica se a senha foi informada
        if (!"".equals(task.getPassword().trim())) {

            try {
                client.setPassword(Password.getPasswordText(task.getPassword().trim()));
            } catch (Exception ex) {
                buildException("Erro ao decifrar a senha", ex);
            }
        }

        // verifica se o diretorio existe
        // neste caso realiza apenas o update
        // caso nao, realiza o checkout
        if (dest.exists()) {

            try {

                // log
                log.info("Update: " + dest.getAbsolutePath());

                // obtem a ultima revisao do repositorio
                SVNRevision revUrl = client.getInfo(url).getLastChangedRevision();

                // obtem a ultima revisao do workcopy
                SVNRevision revWorkCopy = new SVNRevision.Number(client.getInfoFromWorkingCopy(dest).getRevision().getNumber() + 1);

                // verifica se a workcopy esta atualizada
                if (Long.valueOf(revUrl.toString()).longValue() > Long.valueOf(revWorkCopy.toString()).longValue()) {

                    // log
                    log.info("Revisao da area de trabalho: " + revWorkCopy.toString());
                    log.info("Revisao no SVN: " + revUrl.toString());

                    // obtem o log de atualizacoes
                    parseLog(task, client.getLogMessages(url, revWorkCopy, SVNRevision.HEAD), showUpdateLog, changeLog);
                } else {

                    // informa que nao existe atualizacoes
                    log.info("Sem atualizacoes");
                }

                // linha em branco
                log.info("");

                // realiza a atualizacao
                client.update(dest, SVNRevision.HEAD, true);

            } catch (SVNClientException ex) {
                // log de erro
                buildException("Nao foi possivel atualizar " + dest.getAbsolutePath(), ex);
            } catch (IOException ex) {
                // log de erro
                buildException("Nao foi possivel atualizar " + dest.getAbsolutePath(), ex);
            }

        } else {

            // log
            log.info("Checkout: " + dest.getAbsolutePath());

            try {

                // realiza checkout do projeto
                client.checkout(url, dest, SVNRevision.HEAD, true);

            } catch (SVNClientException ex) {

                // log de erro
                buildException("Nao foi possivel atualizar " + dest.getAbsolutePath(), ex);

            }
        }
    }

    public void validate() throws BuildException {

        // realiza as validacoes nos parametros
        if (url == null || "".equalsIgnoreCase(url.toString()))
            buildException("Parametro url nao informado");

        if (dest == null || "".equalsIgnoreCase(dest.getAbsolutePath()))
            buildException("Parametro dest nao informado");
    }

    private void parseLog(Task task, ISVNLogMessage[] messages, boolean showUpdateLog, File changeLog) throws IOException {

        // saida do arquivo de change log
        //BufferedWriter writer = null;

        // formatacao da data
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy hh:mm");

        // verifica se existe mensagens
        // para gera o change log
        // verifica se foi informado o arquivo de change log
        //if (messages != null && messages.length > 0) {
        //
        //	// verifica se o mesmo existe
        //	// caso sim remove o arquivo
        //	if (changeLog.exists())
        //		changeLog.delete();
        //
        //	// cria um novo arquivo de changelog
        //	try {
        //		writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(changeLog)));
        //	} catch (FileNotFoundException ex) {}
        //}

        // mostra o log de update
        for (ISVNLogMessage message : messages) {

            // obtem as informacoes
            String revision = message.getRevision().toString();
            String author = message.getAuthor();
            String date = format.format(message.getDate());
            String messageLog = message.getMessage();
            int changes = message.getChangedPaths().length;


            // verifica se foi aberto arquivo para
            // gerar o changelog
            //if (writer != null) {
            //	try {
            //		// data
            //		writer.write(date);
            //		// separador
            //		writer.write(" - ");
            //		// revisao
            //		writer.write("r" + revision);
            //		// separador
            //		writer.write(" - ");
            //
            //	} catch (IOException ex) {}
            //}

            // mostra no log somente se foi solicitado
            if (showUpdateLog) {

                // linha inicial
                task.log("------------------------------------------------------------------------");

                // linha de cabecalho
                StringBuffer line = new StringBuffer();
                line.append("r" + revision);
                line.append(" | ");
                line.append(author);
                line.append(" | ");
                line.append(date);
                line.append(" | ");
                line.append(changes);
                line.append((changes > 1 ? " lines" : " line"));
                task.log(line.toString());

                // caminhos alterados

                // linha do cabecalho
                task.log("Alteracoes: ");

                // loop de alteracoes
                for (ISVNLogMessageChangePath changedPath : message.getChangedPaths()) {

                    // log da alteracao
                    task.log("   " + changedPath.getAction() + " " + changedPath.getPath());
                }

                // imprime o comentario
                log.info("");
                task.log(messageLog);
            }
        }
    }
}
