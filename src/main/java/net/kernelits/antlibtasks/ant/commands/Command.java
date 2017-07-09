package net.kernelits.antlibtasks.ant.commands;

import net.kernelits.antlibtasks.ant.tasks.Task;
import net.kernelits.antlibtasks.AntLog;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.ProjectComponent;

/**
 * Classe abstrata de comandos de tasks
 *
 * @author Jayder França <a href="mailto:jayderfranca@gmail.com">jayderfranca@gmail.com</a>
 */
public abstract class Command extends ProjectComponent {

    // task owner
    private Task taskOwner = null;

    // log do comando
    protected AntLog log = new AntLog(this);

    // atribui e obtem a task owner
    public void setTask(Task value) {
        taskOwner = value;
    }

    public Task getTask() {
        return taskOwner;
    }

    // obtem o nome da classe
    public String getClassName() {

        // obtem o ultimo nome da classe
        String[] classFullName = getClass().getName().split("\\.");
        String className = classFullName[classFullName.length - 1];

        // retorno
        return className.toLowerCase();
    }

    // lanca uma exception padronizada
    public void buildException(String message, Throwable cause) {
        getTask().buildException("<" + getClassName() + "> " + message, cause);
    }

    public void buildException(String message) {
        buildException(message, null);
    }

    // atribui uma propriedade
    public void setProperty(String name, String value) {
        getTask().setProperty(name, value);
    }

    // obtem uma propriedade
    public String getProperty(String name) {
        return getTask().getProperty(name);
    }

    // executa o comando
    public abstract void execute() throws BuildException;
}
