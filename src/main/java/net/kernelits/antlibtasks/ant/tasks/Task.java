package net.kernelits.antlibtasks.ant.tasks;

import net.kernelits.antlibtasks.ant.commands.Command;
import net.kernelits.antlibtasks.utils.AntLog;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * Classe abstrata de tasks
 *
 * @author Jayder França <a href="mailto:jayderfranca@gmail.com">jayderfranca@gmail.com</a>
 */
public abstract class Task extends org.apache.tools.ant.Task {

    // variavel dos comandos da task
    private List<Command> commands = new ArrayList<Command>();
    private List<Task> tasks = new ArrayList<Task>();

    // log da task
    protected AntLog log = new AntLog(this);

    // propriedade local
    private static Hashtable<String, String> properties = null;

    // metodo para adicionar um comando a task
    public void addCommand(Command cmd) {
        cmd.setTask(this);
        commands.add(cmd);
    }

    // metodo para adicionar uma subtask a task
    public void addTask(Task task) {
        tasks.add(task);
    }

    // metodo para retornar a lista de comandos
    public List<Command> getCommands() {
        return commands;
    }

    // metodo para retonar a lista de subtasks
    public List<Task> getTasks() {
        return tasks;
    }

    // singleton de propriedades
    private Hashtable<String, String> getProperties() {

        // verifica se ja foi criado uma instancia de propriedades
        if (properties == null)
            properties = new Hashtable<String, String>();

        // retorna o objeto criado
        return properties;
    }

    // atribui uma propriedade
    public void setProperty(String name, String value) {

        // caso nao foi criado o projeto atribui no sistema
        if (getProject() == null || !(getProject() instanceof Project))
            System.getProperties().put(name, value);
        else
            getProject().setProperty(name, value);

        // atribui localmente tambem
        getProperties().put(name, value);
    }

    // obtem uma propriedade
    public String getProperty(String name) {

        // variaveis
        String value = "";

        // caso nao foi criado o projeto obtem no sistema
        if (getProject() == null || !(getProject() instanceof Project))
            value = System.getProperties().getProperty(name);
        else
            value = getProject().getProperty(name);

        // verifica se nao retornou valor
        if (value == null) {

            // obtem o valor local
            value = getProperties().get(name);
        }

        // retorno do valor
        return value;
    }

    // lanca uma exception padronizada
    public void buildException(String message, Throwable cause) {

        // lanca a exception
        if (cause != null) {

            // mostra o stactrace
            cause.printStackTrace();
            throw new BuildException(message, cause);

        } else {

            // nao possui throwable
            throw new BuildException(message);
        }
    }

    // lanca uma exception sem throwable
    public void buildException(String message) {

        // passa null para throwable
        buildException(message, null);
    }
}
